plugins {
    java
}

val rdgVersion =
    providers
        .gradleProperty("rdgVersion")
        .orElse(providers.environmentVariable("RDG_VERSION"))
        .orElse("1.0.0-SNAPSHOT")

repositories {
    if (providers.gradleProperty("rdgRepository").orNull != "central") {
        mavenLocal()
    }
    mavenCentral()
}

dependencies {
    implementation(platform("io.github.frikit:random-data-generator-bom:${rdgVersion.get()}"))
    implementation("io.github.frikit:random-data-generator-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.3")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        // Java 21 unless -PjavaVersion names another: scripts/verify_examples_local.sh also runs these tests
        // with -PjavaVersion=17, the oldest Java that random-data-generator's Java modules run on.
        languageVersion.set(
            providers
                .gradleProperty("javaVersion")
                .map { version -> JavaLanguageVersion.of(version) }
                .orElse(JavaLanguageVersion.of(21)),
        )
    }
}
