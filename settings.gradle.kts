dependencyResolutionManagement {
    pluginManagement {
        repositories {
            maven("https://maven.architectury.dev/")
            maven("https://maven.fabricmc.net/")
            maven("https://maven.minecraftforge.net/")
            maven("https://maven2.bai.lol")
            mavenCentral()
            gradlePluginPortal()
        }
    }
}

dependencyResolutionManagement.versionCatalogs.create("catalog") {
    // https://github.com/palantir/gradle-git-version
    plugin("git-version", "com.palantir.git-version").version("3.+")

    plugin("shadow", "com.gradleup.shadow").version("8.+")

    plugin("architectury-loom", "dev.architectury.loom").version("1.7.+")
    plugin("architectury-plugin", "architectury-plugin").version("3.+")

    plugin("explosion", "lol.bai.explosion").version("0.2.0")

    val minecraft = "1.20.1"
    version("minecraft", minecraft)

    val kotlin = "2.0.21"
    version("kotlin", kotlin)
    plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlin)
    plugin("kotlin-plugin-serialization", "org.jetbrains.kotlin.plugin.serialization").version(kotlin)

    library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").version(kotlin)

    val kotlinxSerialization = "1.7.3"
    library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").version(
        kotlinxSerialization
    )
    library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version(
        kotlinxSerialization
    )

    library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.9.0")

    // https://modrinth.com/mod/kinecraft-serialization/versions
    library("kinecraft-serialization", "maven.modrinth", "kinecraft-serialization").version("1.16.0")

    library("minecraft", "com.mojang", "minecraft").version("1.20.1")

    // https://linkie.shedaniel.dev/dependencies?loader=fabric
    version("fabric-loader", "0.16.9")
    version("fabric-api", "0.92.2+$minecraft")
    library("fabric-loader", "net.fabricmc", "fabric-loader").version("0.16.9")
    library("fabric-api", "net.fabricmc.fabric-api", "fabric-api").version("0.92.2+$minecraft")
    library("fabric-kotlin", "net.fabricmc", "fabric-language-kotlin").version("1.12.3+kotlin.$kotlin")

    // https://linkie.shedaniel.dev/dependencies?loader=forge
    version("lexforge", "47.3.12")
    library("lexforge", "net.minecraftforge", "forge").version("1.20.1-47.3.12")
    library("forgified-fabric-api", "dev.su5ed.sinytra.fabric-api", "fabric-api").version("0.92.2+1.11.8+$minecraft")
    library("sinytra-connector", "org.sinytra", "Connector").version("1.0.0-beta.46+$minecraft")
    library("kotlin-forge", "thedarkcolour", "kotlinforforge").version("4.11.0")

    library("mixin", "org.spongepowered", "mixin").version("0.8.7")
    val mixinextras = "0.5.0-beta.4"
    library("mixinextras-common", "io.github.llamalad7", "mixinextras-common").version(mixinextras)
    library("mixinextras-lexforge", "io.github.llamalad7", "mixinextras-forge").version(mixinextras)
    library("mixinextras-fabric", "io.github.llamalad7", "mixinextras-fabric").version(mixinextras)

    library("modmenu", "com.terraformersmc", "modmenu").version("7.2.2")

    library("mixin-constraints", "com.moulberry", "mixinconstraints").version("1.0.1")

    library("yacl-fabric", "dev.isxander", "yet-another-config-lib").version("3.6.1+1.20.1-fabric")
    library("yacl-forge", "dev.isxander", "yet-another-config-lib").version("3.6.1+1.20.1-forge")

    library("kiwi-forge", "maven.modrinth", "kiwi").version("11.8.20+forge")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val name: String by settings

rootProject.name = name

include(":xplat")

include(":lexforge:lexforge-core")
include(":lexforge:lexforge-mod")
include(":lexforge:lexforge-mixin")