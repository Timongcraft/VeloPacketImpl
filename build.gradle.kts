plugins {
    id("java")
    id("maven-publish")
}

group = "de.timongcraft"
version = libs.versions.project.get()

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.skyblocksquad.de/repo")
    }

    mavenLocal()
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)
    compileOnly(libs.adventure.nbt) // internal velocity dep
    compileOnly(libs.fastutil) // internal velocity dep
    compileOnly(libs.netty) // internal velocity dep

    testImplementation(libs.velocity.api)
    testImplementation(libs.velocity.proxy)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets["test"].output)
}

publishing {
    publications {
        create<MavenPublication>("local") {
            from(components["java"])
            artifact(testJar)
        }
        create<MavenPublication>("remote") {
            from(components["java"])
        }
    }

    repositories {
        mavenLocal()

        maven {
            name = "skyblocksquad"
            url = uri("https://repo.skyblocksquad.de/repo")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}