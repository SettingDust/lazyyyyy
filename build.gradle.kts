import groovy.lang.Closure

plugins {
    java
    alias(catalog.plugins.kotlin.jvm) apply false
    alias(catalog.plugins.kotlin.plugin.serialization) apply false

    alias(catalog.plugins.git.version)

    alias(catalog.plugins.explosion) apply false
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties

group = "settingdust.lazyyyyy"

val gitVersion: Closure<String> by extra
version = gitVersion()

base {
    archivesName = archive_name
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    repositories {
        exclusiveContent {
            forRepository {
                maven("https://cursemaven.com")
            }
            filter {
                includeGroup("curse.maven")
            }
        }

        exclusiveContent {
            forRepository {
                maven("https://api.modrinth.com/maven")
            }
            filter {
                includeGroup("maven.modrinth")
            }
        }

        maven("https://maven.su5ed.dev/releases") {
            content {
                includeGroupAndSubgroups("dev.su5ed")
                includeGroupAndSubgroups("org.sinytra")
            }
        }

        maven("https://thedarkcolour.github.io/KotlinForForge/") {
            content { includeGroup("thedarkcolour") }
        }

        maven("https://maven.terraformersmc.com/") {
            content { includeGroupAndSubgroups("com.terraformersmc") }
        }

        maven("https://maven.isxander.dev/releases")

        mavenCentral()

        maven("https://maven.minecraftforge.net/")

        mavenLocal()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version

    base { archivesName.set("${rootProject.base.archivesName.get()}${project.path.replace(":", "-")}") }

    tasks {
        withType<ProcessResources> {
            val properties = mapOf(
                "id" to id,
                "version" to rootProject.version,
                "group" to rootProject.group,
                "name" to rootProject.name,
                "description" to rootProject.property("description").toString(),
                "author" to rootProject.property("author").toString(),
                "source" to rootProject.property("source").toString(),
                "fabric_loader" to ">=0.15",
                "minecraft" to ">=1.20.1",
                "fabric_kotlin" to "*"
            )

            inputs.properties(properties)

            filesMatching(
                listOf(
                    "fabric.mod.json",
                    "META-INF/neoforge.mods.toml",
                    "META-INF/mods.toml",
                    "*.mixins.json",
                    "META-INF/MANIFEST.MF"
                )
            ) {
                expand(properties)
            }
        }
    }
}