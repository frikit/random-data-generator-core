/*
 * Publishing conventions for random-data-generator's published modules.
 *
 * Applies maven-publish + signing, attaches sources/javadoc jars, sets the
 * Automatic-Module-Name manifest attribute, and creates the "mavenJava"
 * publication with the module's Central-Portal-ready POM. Signing activates
 * only when GPG_SIGNING_KEY and GPG_SIGNING_PASSWORD are present in the
 * environment (CI release workflow).
 *
 * Maven Central uploads are exposed to the root com.gradleup.nmcp.aggregation
 * plugin. Run
 * `./gradlew publishAggregationToCentralPortal` to push the signed bundle
 * to https://central.sonatype.com.
 */
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
    signing
}

apply(plugin = "com.gradleup.nmcp")

val moduleDescriptions =
    mapOf(
        "bom" to
            "random-data-generator bill of materials for aligning all published random-data-generator module versions.",
        "core" to
            "random-data-generator core: random and fake-data generation toolkit for Java 17 and later with seedable generators, locale-aware data, ObjectGenerator/ObjectFaker, and Schema export.",
    )
val moduleNames =
    mapOf(
        "core" to "io.github.frikit.rdg",
    )

afterEvaluate {
    val componentName =
        when {
            components.names.contains("java") -> "java"
            components.names.contains("kotlin") -> "kotlin"
            components.names.contains("javaPlatform") -> "javaPlatform"
            else -> null
        }
    if (componentName == null || project.name !in moduleDescriptions.keys) {
        return@afterEvaluate
    }

    if (componentName != "javaPlatform") {
        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<Jar>().configureEach {
            from(rootProject.file("LICENSE")) {
                into("META-INF")
            }
        }

        tasks.named<Jar>("jar").configure {
            manifest {
                attributes(
                    "Automatic-Module-Name" to moduleNames.getValue(project.name),
                    "Implementation-Version" to project.version,
                )
            }
        }
    }

    publishing {
        publications {
            if (findByName("mavenJava") == null) {
                create<MavenPublication>("mavenJava") {
                    val publishedArtifactId = "random-data-generator-${project.name}"
                    artifactId = publishedArtifactId
                    from(components[componentName])
                    pom {
                        name.set(publishedArtifactId)
                        description.set(moduleDescriptions.getValue(project.name))
                        url.set("https://github.com/frikit/random-data-generator-core")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/license/mit/")
                            }
                        }
                        developers {
                            developer {
                                id.set("frikit")
                                name.set("Victor Osipov")
                                email.set("ofrikit94@gmail.com")
                            }
                        }
                        scm {
                            url.set("https://github.com/frikit/random-data-generator-core")
                            connection.set("scm:git:https://github.com/frikit/random-data-generator-core.git")
                            developerConnection.set(
                                "scm:git:ssh://git@github.com/frikit/random-data-generator-core.git",
                            )
                        }
                    }
                }
            }
        }
    }

    signing {
        val signingKey = providers.environmentVariable("GPG_SIGNING_KEY").orNull
        val signingPassword = providers.environmentVariable("GPG_SIGNING_PASSWORD").orNull
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications["mavenJava"])
        }
    }
}
