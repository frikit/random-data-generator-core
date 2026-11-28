plugins {
    // JVM attribute rules and Maven POM variant derivation for the root-owned API baseline classpaths
    // below; adds no tasks.
    `jvm-ecosystem`
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.nmcp.aggregation)
    alias(libs.plugins.pitest) apply false
    alias(libs.plugins.errorprone) apply false
}

allprojects {
    group = "io.github.frikit"
    version =
        (findProperty("releaseVersion") as String?) ?: rootProject.providers.gradleProperty("developmentVersion").get()

    configurations.configureEach {
        resolutionStrategy.failOnNonReproducibleResolution()
    }
}

apply(plugin = "com.diffplug.spotless")

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    // Sources outside the Gradle subprojects (standalone consumer examples and the native-image smoke
    // program) follow the same formatter and license header as the modules; see `subprojects` below.
    java {
        target("examples/**/src/**/*.java", "scripts/**/*.java")
        targetExclude("**/build/**")
        palantirJavaFormat(
            libs.versions.palantir.java.format
                .get(),
        )
        formatAnnotations()
        licenseHeaderFile(file("LICENSE_HEADER"))
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.gradle.kts", "examples/**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
    format("markdown") {
        target("README.md", "docs/**/*.md", "docs-site/**/*.md", "examples/**/*.md")
        // Remove trailing whitespace
        trimTrailingWhitespace()
        // Ensure files end with newline
        endWithNewline()
        // Normalize line endings
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
}

// Modules published to Maven Central, read from `publishedModules` in gradle.properties so the build,
// scripts, and documentation checks share one list. Their POM, sources/javadoc jars, manifest,
// and signing setup live in buildSrc/src/main/kotlin/rdg-publishing-conventions.gradle.kts.
val publishedModules =
    providers
        .gradleProperty("publishedModules")
        .get()
        .split(',')
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toSet()
val jarModules = publishedModules - "bom"

val releaseComponentGroup = group.toString()
val releaseModuleVersions =
    publishedModules.associateWith { moduleName ->
        project(":$moduleName").version.toString()
    }
val releaseSbomDirectory = layout.buildDirectory.dir("reports/sbom")

dependencies {
    publishedModules.forEach { moduleName ->
        add("nmcpAggregation", project(":$moduleName"))
    }
}

nmcpAggregation {
    centralPortal {
        username.set(providers.environmentVariable("CENTRAL_PORTAL_USERNAME").orElse(""))
        password.set(providers.environmentVariable("CENTRAL_PORTAL_PASSWORD").orElse(""))
        // USER_MANAGED: upload appears in the Central Portal UI for manual "Publish".
        publishingType.set(providers.environmentVariable("CENTRAL_PORTAL_PUBLISHING_TYPE").orElse("USER_MANAGED"))
    }
}

tasks.named<org.cyclonedx.gradle.CyclonedxDirectTask>("cyclonedxDirectBom") {
    enabled = false
}

subprojects {
    tasks.withType<org.cyclonedx.gradle.CyclonedxDirectTask>().configureEach {
        enabled = project.name in publishedModules
        if (enabled) {
            includeConfigs.set(if (project.name == "bom") listOf("classpath") else listOf("runtimeClasspath"))
            skipConfigs.set(listOf(".*test.*", ".*Test.*"))
            includeBuildEnvironment.set(false)
            includeMetadataResolution.set(true)
            componentGroup.set(project.group.toString())
            componentName.set("random-data-generator-${project.name}")
            componentVersion.set(project.version.toString())
            jsonOutput.set(
                rootProject.layout.buildDirectory.file("reports/sbom/random-data-generator-${project.name}.cdx.json"),
            )
            xmlOutput.set(
                rootProject.layout.buildDirectory.file("reports/sbom/random-data-generator-${project.name}.cdx.xml"),
            )
        }
    }
}

val releaseSbomTasks =
    publishedModules.map { moduleName ->
        project(":$moduleName").tasks.named("cyclonedxDirectBom")
    }

tasks.register("generateReleaseSboms") {
    group = "distribution"
    description = "Generates CycloneDX JSON/XML SBOMs for every published module."
    dependsOn(releaseSbomTasks)
}

tasks.register("verifyReleaseSboms") {
    group = "verification"
    description = "Generates and validates the CycloneDX SBOMs attached to a release."
    dependsOn("generateReleaseSboms")
    inputs.files(
        publishedModules.flatMap { moduleName ->
            listOf(
                layout.buildDirectory.file("reports/sbom/random-data-generator-$moduleName.cdx.json"),
                layout.buildDirectory.file("reports/sbom/random-data-generator-$moduleName.cdx.xml"),
            )
        },
    )

    doLast {
        val xmlFactory =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            }
        val xpath =
            javax.xml.xpath.XPathFactory
                .newInstance()
                .newXPath()

        publishedModules.forEach { moduleName ->
            val expectedName = "random-data-generator-$moduleName"
            val expectedVersion = releaseModuleVersions.getValue(moduleName)
            val jsonFile = releaseSbomDirectory.get().file("$expectedName.cdx.json").asFile
            val xmlFile = releaseSbomDirectory.get().file("$expectedName.cdx.xml").asFile

            check(jsonFile.isFile && jsonFile.length() > 0) { "Missing release SBOM: $jsonFile" }
            check(xmlFile.isFile && xmlFile.length() > 0) { "Missing release SBOM: $xmlFile" }

            @Suppress("UNCHECKED_CAST")
            val json = groovy.json.JsonSlurper().parse(jsonFile) as Map<String, Any?>
            check(json["bomFormat"] == "CycloneDX") { "$jsonFile is not a CycloneDX document" }
            check(json["specVersion"] == "1.6") { "$jsonFile must use CycloneDX 1.6" }
            @Suppress("UNCHECKED_CAST")
            val jsonMetadata = json["metadata"] as Map<String, Any?>

            @Suppress("UNCHECKED_CAST")
            val jsonComponent = jsonMetadata["component"] as Map<String, Any?>
            check(jsonComponent["group"] == releaseComponentGroup) { "$jsonFile has the wrong component group" }
            check(jsonComponent["name"] == expectedName) { "$jsonFile has the wrong component name" }
            check(jsonComponent["version"] == expectedVersion) { "$jsonFile has the wrong component version" }
            val componentNames =
                (json["components"] as? List<*>)
                    .orEmpty()
                    .mapNotNull { component -> (component as? Map<*, *>)?.get("name") as? String }
            check("logback-classic" !in componentNames) { "$jsonFile contains the test-only logging backend" }

            val xml = xmlFactory.newDocumentBuilder().parse(xmlFile)
            check(xml.documentElement.localName == "bom") { "$xmlFile is not a CycloneDX document" }
            check(xml.documentElement.namespaceURI == "http://cyclonedx.org/schema/bom/1.6") {
                "$xmlFile must use CycloneDX 1.6"
            }

            fun xmlComponentValue(name: String): String =
                xpath.evaluate(
                    "/*[local-name()='bom']/*[local-name()='metadata']/*[local-name()='component']/*[local-name()='$name']/text()",
                    xml,
                )
            check(xmlComponentValue("group") == releaseComponentGroup) { "$xmlFile has the wrong component group" }
            check(xmlComponentValue("name") == expectedName) { "$xmlFile has the wrong component name" }
            check(xmlComponentValue("version") == expectedVersion) { "$xmlFile has the wrong component version" }
        }
    }
}

// Public API compatibility (VERSIONING.md, docs/release-runbook.md): every published jar keeps the
// public API of the apiBaselineVersion release. Additions pass; a removal or an incompatible change
// fails `checkApiCompatibility`, which `check` runs.
// - Java modules: japicmp compares the jar with the released jar from Maven Central, together with the
//   dependency classpath of each, limited to public and protected API in the public-API packages: the
//   unqualified exports of the released random-data-generator-core module descriptor. The JPMS module name must
//   stay, and so must the export of each of those packages.
// During a major line each check subtracts the breaks that <module>/api/accepted-breaks-N.0.txt accepts
// (see acceptedBreaksVerdict below).
// Before the first release apiBaselineVersion is empty: there is no released API to protect, so
// checkApiCompatibility only says so, and the first release sets it (docs/release-runbook.md).
val apiBaselineVersion = providers.gradleProperty("apiBaselineVersion").getOrElse("")
val javaApiModules = setOf("core")
val apiBaselineModules = if (apiBaselineVersion.isEmpty()) emptySet() else javaApiModules
check(javaApiModules == jarModules) {
    "The API compatibility check must cover exactly the published jar modules: $jarModules"
}
val apiCompatibilityAdvice =
    "To fix it, keep each $apiBaselineVersion declaration as it was: retire one by deprecating it and naming its " +
        "replacement (only a major release removes it), and give a method added to an interface or abstract class " +
        "an implementation (docs/release-runbook.md, Public API compatibility)."

// Accepted breaks (docs/release-runbook.md, Accepted breaks): while the development line is a major
// N.0.0-SNAPSHOT, <module>/api/accepted-breaks-N.0.txt lists the intended breaks of that major release, one
// per line exactly as the check reports it; blank lines and lines that start with '#' are ignored. The
// check subtracts the listed breaks and fails on an unlisted one, on a listed one that no longer occurs,
// and on any accepted-breaks file while the line is not a major N.0.0-SNAPSHOT, so a minor or patch
// release breaks nothing. The line is developmentVersion, not the build version: a release build of the
// major release (-PreleaseVersion=N.0.0) still checks its accepted breaks against the last release.
val developmentVersion = providers.gradleProperty("developmentVersion").get()
val acceptedBreaksLine = Regex("""(\d+)\.0\.0-SNAPSHOT""").matchEntire(developmentVersion)?.groupValues?.get(1)
val acceptedBreaksFileName = acceptedBreaksLine?.let { major -> "accepted-breaks-$major.0.txt" }

fun acceptedBreaksDirectory(moduleName: String): File =
    project(":$moduleName")
        .layout.projectDirectory
        .dir("api")
        .asFile

fun acceptedBreaksFiles(moduleName: String): List<File> =
    acceptedBreaksDirectory(moduleName)
        .listFiles { file -> file.isFile && file.name.startsWith("accepted-breaks-") && file.name.endsWith(".txt") }
        .orEmpty()
        .sortedBy(File::getName)

// The entries of an accepted-breaks file: its trimmed lines that are neither blank nor comments.
fun acceptedBreaksEntries(file: File): List<String> =
    file
        .readLines()
        .map(String::trim)
        .filter { line -> line.isNotEmpty() && !line.startsWith("#") }

// Compares the breaks a check found with the module's accepted-breaks file. Returns the failure text,
// if any, and the number of breaks that the file accepts.
fun acceptedBreaksVerdict(
    moduleName: String,
    found: List<String>,
): Pair<String?, Int> {
    fun plural(
        count: Int,
        noun: String,
    ) = "$count $noun${if (count == 1) "" else "s"}"

    // Entries are compared trimmed, so the indented lines of a failure can be pasted as they are.
    val breaks = found.map(String::trim).distinct()
    val sections = mutableListOf<String>()
    val files = acceptedBreaksFiles(moduleName)
    val expected = acceptedBreaksFileName?.let { name -> acceptedBreaksDirectory(moduleName).resolve(name) }
    files.filter { file -> file != expected }.forEach { file ->
        sections +=
            if (expected == null) {
                "${file.relativeTo(rootDir)} exists, but the development line $developmentVersion is not a major " +
                    "N.0.0-SNAPSHOT: only a major release may break the $apiBaselineVersion public API. " +
                    "Delete the file (docs/release-runbook.md, Accepted breaks)."
            } else {
                "${file.relativeTo(rootDir)} does not belong to the development line $developmentVersion, whose " +
                    "accepted breaks are in ${expected.relativeTo(rootDir)}. Delete it."
            }
    }
    val listed = mutableListOf<String>()
    val duplicates = mutableListOf<String>()
    expected?.takeIf(File::isFile)?.let(::acceptedBreaksEntries)?.forEach { line ->
        if (line in listed) {
            if (line !in duplicates) duplicates += line
        } else {
            listed += line
        }
    }
    val path = expected?.relativeTo(rootDir)?.path
    if (duplicates.isNotEmpty()) {
        sections +=
            buildString {
                appendLine("$path lists ${plural(duplicates.size, "break")} more than once; keep one line each:")
                duplicates.forEach { line -> appendLine("  $line") }
            }.trimEnd()
    }
    val unlisted = breaks.filterNot(listed::contains)
    if (unlisted.isNotEmpty()) {
        sections +=
            buildString {
                appendLine(
                    "random-data-generator-$moduleName breaks the $apiBaselineVersion public API " +
                        "(${plural(unlisted.size, "incompatible change")}" +
                        "${if (path == null) "" else " not listed in $path"}):",
                )
                unlisted.forEach { line -> appendLine("  $line") }
                append(apiCompatibilityAdvice)
                if (path != null) {
                    append(
                        "\nIf the $acceptedBreaksLine.0 plan names this break, add each line above to $path, under a " +
                            "comment that gives the reason (docs/release-runbook.md, Accepted breaks).",
                    )
                }
            }
    }
    val stale = listed.filterNot(breaks::contains)
    if (stale.isNotEmpty()) {
        sections +=
            buildString {
                appendLine(
                    "$path lists ${plural(
                        stale.size,
                        "break",
                    )} that random-data-generator-$moduleName no longer makes; " +
                        "delete each line below, with its comment:",
                )
                stale.forEach { line -> appendLine("  $line") }
            }.trimEnd()
    }
    return sections.takeIf { it.isNotEmpty() }?.joinToString("\n") to breaks.count(listed::contains)
}

// The result line of a check that passes: "(N additions)", or with accepted breaks
// "(N additions; M accepted breaks in <file>)".
fun apiCompatibleLine(
    moduleName: String,
    additions: Int,
    accepted: Int,
): String =
    "random-data-generator-$moduleName: compatible with the $apiBaselineVersion public API " +
        "($additions addition${if (additions == 1) "" else "s"}" +
        (
            if (accepted == 0) {
                ""
            } else {
                "; $accepted accepted break${if (accepted == 1) "" else "s"} in " +
                    acceptedBreaksDirectory(moduleName).resolve(acceptedBreaksFileName!!).relativeTo(rootDir)
            }
        ) + ")."

fun apiTaskSuffix(moduleName: String): String =
    moduleName
        .split('-')
        .joinToString("") { part -> part.replaceFirstChar(Char::uppercaseChar) }

val japicmpClasspath =
    configurations.create("japicmpClasspath") {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }

dependencies {
    add(japicmpClasspath.name, "com.github.siom79.japicmp:japicmp:${libs.versions.japicmp.get()}:jar-with-dependencies")
}

// Root-owned resolvable configurations request the jars a Java consumer receives for `usage`
// (java-api: compile classpath; java-runtime: runtime classpath).
fun Configuration.requestJvmJars(usage: String) {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(usage))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(
            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
            objects.named(TargetJvmEnvironment.STANDARD_JVM),
        )
    }
}

// japicmp receives the dependency classpath of both jars, so inherited external supertypes and
// annotations are analysed instead of failing as missing classes. Each side unions the compile
// (java-api) and runtime graphs: JUnit publishes its org.apiguardian annotations only for
// compilation, and implementation dependencies only at runtime. compileOnly dependencies are absent
// from published metadata, yet japicmp must load the types they contribute to the API. Keep this in
// sync with the module build files: a missing entry fails the check with "Could not load".
val apiCompileOnlyDependencies = emptyMap<String, List<Provider<MinimalExternalModuleDependency>>>()

fun apiGraphs(
    moduleName: String,
    role: String,
    dependencyNotation: Any,
): List<Configuration> =
    listOf("Compile" to Usage.JAVA_API, "Runtime" to Usage.JAVA_RUNTIME).map { (suffix, usage) ->
        configurations
            .create("${moduleName.replace("-", "")}Api$role$suffix") {
                isCanBeConsumed = false
                isCanBeResolved = true
                requestJvmJars(usage)
            }.also { graph ->
                dependencies.add(graph.name, dependencyNotation)
                if (usage == Usage.JAVA_API) {
                    apiCompileOnlyDependencies[moduleName].orEmpty().forEach { compileOnly ->
                        dependencies.addProvider(graph.name, compileOnly)
                    }
                }
            }
    }

// Splits resolved graphs into the selected component's jar and the classpath of everything else.
fun jarAndDependencyClasspath(
    graphs: List<Configuration>,
    isSelected: (ComponentIdentifier) -> Boolean,
): Pair<File, String> {
    val artifacts = graphs.flatMap { graph -> graph.incoming.artifacts.artifacts }
    val selected =
        artifacts
            .filter { artifact -> isSelected(artifact.id.componentIdentifier) }
            .map { artifact -> artifact.file }
            .distinct()
            .single()
    val dependencyClasspath =
        artifacts
            .filterNot { artifact -> isSelected(artifact.id.componentIdentifier) }
            .map { artifact -> artifact.file.absolutePath }
            .distinct()
            .joinToString(File.pathSeparator)
    return selected to dependencyClasspath
}

fun isReleasedModule(moduleName: String): (ComponentIdentifier) -> Boolean =
    { id ->
        id is ModuleComponentIdentifier && id.group == "io.github.frikit" &&
            id.module == "random-data-generator-$moduleName"
    }

fun isProjectModule(moduleName: String): (ComponentIdentifier) -> Boolean =
    { id -> id is ProjectComponentIdentifier && id.projectPath == ":$moduleName" }

// The JPMS name of a jar: its module descriptor's, else the Automatic-Module-Name manifest attribute.
fun jpmsModuleName(jar: File): String? =
    java.util.jar.JarFile(jar).use { file ->
        file.getEntry("module-info.class")?.let { entry ->
            file.getInputStream(entry).use { input ->
                java.lang.module.ModuleDescriptor
                    .read(input)
                    .name()
            }
        } ?: file.manifest?.mainAttributes?.getValue("Automatic-Module-Name")
    }

// Packages a jar exports to every module: the unqualified exports of its module descriptor or, for an
// automatic module without one, every package that holds a class.
fun exportedPackages(jar: File): Set<String> =
    java.util.jar.JarFile(jar).use { file ->
        val descriptor =
            file.getEntry("module-info.class")?.let { entry ->
                file.getInputStream(entry).use { input ->
                    java.lang.module.ModuleDescriptor
                        .read(input)
                }
            }
        descriptor
            ?.exports()
            ?.filterNot { export -> export.isQualified }
            ?.map { export -> export.source() }
            ?.toSortedSet()
            ?: file
                .entries()
                .asSequence()
                .map { entry -> entry.name }
                .filter { name -> name.endsWith(".class") && "/" in name && !name.startsWith("META-INF/") }
                .map { name -> name.substringBeforeLast('/').replace('/', '.') }
                .toSortedSet()
    }

fun xmlChildren(
    element: org.w3c.dom.Element,
    tagName: String,
): List<org.w3c.dom.Element> =
    (0 until element.childNodes.length)
        .map { index -> element.childNodes.item(index) }
        .filterIsInstance<org.w3c.dom.Element>()
        .filter { child -> child.tagName == tagName }

// Describes the API element that owns a japicmp compatibility change, such as
// `io.github.frikit.rdg.generator.GeneratorConfig#seed(long)` or `... superclass`.
fun japicmpSubject(owner: org.w3c.dom.Element): String {
    fun parameters(member: org.w3c.dom.Element): String =
        xmlChildren(member, "parameters")
            .flatMap { list -> xmlChildren(list, "parameter") }
            .joinToString(", ") { parameter -> parameter.getAttribute("type") }

    val qualifier =
        when (owner.tagName) {
            "superclass" -> " superclass ${owner.getAttribute("superclassOld")}"
            "interface" -> " interface ${owner.getAttribute("fullyQualifiedName")}"
            "returnType" -> " return type"
            "parameter" -> " parameter ${owner.getAttribute("type")}"
            "genericTemplate" -> " type parameter ${owner.getAttribute("name")}"
            "exception" -> " exception ${owner.getAttribute("name")}"
            "annotation" -> " annotation @${owner.getAttribute("fullyQualifiedName")}"
            else -> ""
        }
    var member: org.w3c.dom.Element = owner
    while (member.tagName !in setOf("class", "method", "constructor", "field")) {
        member = member.parentNode as org.w3c.dom.Element
    }
    var type = member
    while (type.tagName != "class") {
        type = type.parentNode as org.w3c.dom.Element
    }
    val className = type.getAttribute("fullyQualifiedName")
    val memberName =
        when (member.tagName) {
            "method" -> "#${member.getAttribute("name")}(${parameters(member)})"
            "constructor" -> "#<init>(${parameters(member)})"
            "field" -> "#${member.getAttribute("name")}"
            else -> ""
        }
    return "$className$memberName$qualifier"
}

// Incompatible changes in a japicmp XML report, one readable line each, and the number of additions.
fun japicmpFindings(report: File): Pair<List<String>, Int> {
    val document =
        javax.xml.parsers.DocumentBuilderFactory
            .newInstance()
            .apply { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) }
            .newDocumentBuilder()
            .parse(report)
    val changes = document.getElementsByTagName("compatibilityChange")
    val findings =
        (0 until changes.length)
            .map { index -> changes.item(index) as org.w3c.dom.Element }
            .mapNotNull { change ->
                val broken =
                    listOfNotNull(
                        "binary".takeIf { change.getAttribute("binaryCompatible") == "false" },
                        "source".takeIf { change.getAttribute("sourceCompatible") == "false" },
                    )
                if (broken.isEmpty()) {
                    null
                } else {
                    val owner = change.parentNode.parentNode as org.w3c.dom.Element
                    "${japicmpSubject(owner)}: ${change.getAttribute("type")} (breaks ${broken.joinToString(" and ")})"
                }
            }.distinct()
    val classes = document.getElementsByTagName("class")
    val additions =
        (0 until classes.length)
            .map { index -> classes.item(index) as org.w3c.dom.Element }
            .sumOf { type ->
                when (type.getAttribute("changeStatus")) {
                    "NEW" -> {
                        1
                    }

                    "MODIFIED" -> {
                        listOf("constructors" to "constructor", "methods" to "method", "fields" to "field")
                            .flatMap { (list, item) -> xmlChildren(type, list).flatMap { xmlChildren(it, item) } }
                            .count { member -> member.getAttribute("changeStatus") == "NEW" }
                    }

                    else -> {
                        0
                    }
                }
            }
    return findings to additions
}

val javaApiCompatibilityTasks =
    apiBaselineModules.map { moduleName ->
        val baselineGraphs =
            apiGraphs(moduleName, "Baseline", "io.github.frikit:random-data-generator-$moduleName:$apiBaselineVersion")
        val candidateGraphs = apiGraphs(moduleName, "Candidate", dependencies.project(":$moduleName"))
        val reportDirectory = layout.buildDirectory.dir("reports/api-compatibility/$moduleName")

        tasks.register<JavaExec>("check${apiTaskSuffix(moduleName)}ApiCompatibility") {
            group = "verification"
            description =
                "Checks that random-data-generator-$moduleName keeps the public API of $apiBaselineVersion (japicmp)."
            dependsOn(":$moduleName:jar")
            inputs.files(baselineGraphs, candidateGraphs, acceptedBreaksFiles(moduleName))
            // No declared outputs: the check always runs, so every run prints its result line.
            classpath = japicmpClasspath
            mainClass.set("japicmp.JApiCmp")
            // The XML report decides the outcome (doLast); japicmp's own output goes to japicmp.txt.
            isIgnoreExitValue = true
            val japicmpOutput = java.io.ByteArrayOutputStream()
            standardOutput = japicmpOutput
            errorOutput = japicmpOutput
            val moduleProblems = mutableListOf<String>()

            doFirst {
                val (oldJar, oldClasspath) = jarAndDependencyClasspath(baselineGraphs, isReleasedModule(moduleName))
                val (newJar, newClasspath) = jarAndDependencyClasspath(candidateGraphs, isProjectModule(moduleName))
                val directory = reportDirectory.get().asFile
                directory.deleteRecursively()
                directory.mkdirs()

                moduleProblems.clear()
                val oldModuleName = jpmsModuleName(oldJar)
                val newModuleName = jpmsModuleName(newJar)
                if (newModuleName != oldModuleName) {
                    moduleProblems += "JPMS module name: $oldModuleName is now $newModuleName"
                }
                val apiPackages =
                    exportedPackages(oldJar).also { exported ->
                        check(
                            exported.isNotEmpty(),
                        ) { "random-data-generator-$moduleName $apiBaselineVersion exports no package" }
                    }
                val stillExported = exportedPackages(newJar)
                apiPackages.filterNot(stillExported::contains).forEach { lost ->
                    moduleProblems += "$lost: no longer exported by module $newModuleName"
                }

                args =
                    listOf(
                        "--old",
                        oldJar.absolutePath,
                        "--new",
                        newJar.absolutePath,
                        "--old-classpath",
                        oldClasspath,
                        "--new-classpath",
                        newClasspath,
                        "-a",
                        "protected",
                        // Exactly these packages: a sub-package that is not exported is not public API.
                        "--include",
                        apiPackages.joinToString(";"),
                        "--include-exclusively",
                        "--only-modified",
                        "--error-on-binary-incompatibility",
                        "--error-on-source-incompatibility",
                        "--reproducible-report",
                        "--html-file",
                        directory.resolve("report.html").absolutePath,
                        "--xml-file",
                        directory.resolve("report.xml").absolutePath,
                    )
            }

            doLast {
                val directory = reportDirectory.get().asFile
                directory.resolve("japicmp.txt").writeBytes(japicmpOutput.toByteArray())
                val xmlReport = directory.resolve("report.xml")
                val exitValue = executionResult.get().exitValue
                if (!xmlReport.isFile) {
                    throw GradleException(
                        "japicmp produced no report for random-data-generator-$moduleName (exit $exitValue):\n" +
                            japicmpOutput.toString(Charsets.UTF_8),
                    )
                }
                val (findings, additions) = japicmpFindings(xmlReport)
                val problems = moduleProblems + findings
                // japicmp exits non-zero for each incompatible change, accepted ones included.
                if (problems.isEmpty() && exitValue != 0) {
                    throw GradleException(
                        "japicmp failed for random-data-generator-$moduleName (exit $exitValue):\n" +
                            japicmpOutput.toString(Charsets.UTF_8),
                    )
                }
                val (failure, accepted) = acceptedBreaksVerdict(moduleName, problems)
                if (failure != null) {
                    throw GradleException("$failure\nReport: ${directory.resolve("report.html")}")
                }
                logger.lifecycle(apiCompatibleLine(moduleName, additions, accepted))
            }
        }
    }

tasks.register("checkApiCompatibility") {
    group = "verification"
    if (apiBaselineVersion.isEmpty()) {
        description = "Reports that no release exists yet whose public API could be checked."
        doLast {
            logger.lifecycle("No released public API to check yet: the first release sets apiBaselineVersion.")
        }
    } else {
        description = "Checks that every published jar keeps the public API of $apiBaselineVersion."
        dependsOn(javaApiCompatibilityTasks)
    }
}

// The Java modules run on javaMinimumVersion (gradle.properties) or later. The JDK 21 toolchain that runs
// Error Prone, NullAway and PIT compiles their main and test sources with --release, against that version's
// API, which also sets the Java version that their published Gradle metadata requires; only core's
// java21Test sources raise it. Their tests run on that toolchain too, unless -PtestJavaVersion=<version>
// names another Java, as CI's test jobs do.
val javaMinimumVersion = providers.gradleProperty("javaMinimumVersion").get().toInt()
val testJavaVersion = providers.gradleProperty("testJavaVersion")
subprojects {
    if (name in javaApiModules) {
        tasks.withType<JavaCompile>().configureEach {
            options.release.convention(javaMinimumVersion)
        }
        pluginManager.withPlugin("java") {
            val toolchains = the<JavaToolchainService>()
            tasks.withType<Test>().configureEach {
                if (testJavaVersion.isPresent) {
                    javaLauncher.set(
                        toolchains.launcherFor {
                            languageVersion.set(JavaLanguageVersion.of(testJavaVersion.get()))
                        },
                    )
                    // A test that fails only on that Java shows its expected and actual values in the log.
                    testLogging.exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
                doFirst {
                    val runtime = javaLauncher.get().metadata
                    logger.lifecycle("$path runs on Java ${runtime.javaRuntimeVersion} at ${runtime.installationPath}")
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn("checkApiCompatibility")
}

// Only :core has a coverage rule: the exact 100% gate in core/build.gradle.kts, uploaded to Codecov.
// Another published jar module needs a coverage rule of its own before it can be added.
check(jarModules == setOf("core")) {
    "Only core has a coverage rule (core/build.gradle.kts); give each published jar module one: $jarModules"
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    if (name in publishedModules) {
        apply(plugin = "rdg-publishing-conventions")
    }

    afterEvaluate {
        // Palantir Java Format for Java, ktlint for Gradle scripts; `.editorconfig`
        // holds the shared indentation and line-length settings. Formatting runs before the license
        // header step so the header is applied to the final layout.
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            java {
                target("src/**/*.java")
                palantirJavaFormat(
                    libs.versions.palantir.java.format
                        .get(),
                )
                // Keeps type-use annotations such as @Nullable on the line of the type they annotate.
                formatAnnotations()
                licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
            }
            kotlinGradle {
                ktlint(libs.versions.ktlint.get())
            }
        }
    }
}
