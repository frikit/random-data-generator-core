plugins {
    application
}

val rdgVersion =
    providers
        .gradleProperty("rdgVersion")
        .orElse(providers.environmentVariable("RDG_VERSION"))
        .orElse("1.0.0-SNAPSHOT")

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.frikit:random-data-generator-core:${rdgVersion.get()}")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    modularity.inferModulePath.set(true)
}

application {
    mainModule.set("io.github.frikit.rdg.examples.jpms.open")
    mainClass.set("io.github.frikit.rdg.examples.jpms.openconsumer.OpenConsumer")
}
