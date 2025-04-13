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

    plugin("architectury-loom", "dev.architectury.loom").version("1.9.+")
    plugin("architectury-plugin", "architectury-plugin").version("3.+")

    plugin("explosion", "lol.bai.explosion").version("0.2.0")

    val minecraft = "1.20.1"
    version("minecraft", minecraft)

    val kotlin = "2.1.0"
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

    val kotlinxCoroutines = "1.10.1"
    library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version(kotlinxCoroutines)
    library("kotlinx-coroutines-debug", "org.jetbrains.kotlinx", "kotlinx-coroutines-debug").version(kotlinxCoroutines)

    // https://modrinth.com/mod/kinecraft-serialization/versions
    library("kinecraft-serialization", "maven.modrinth", "kinecraft-serialization").version("1.16.0")

    library("minecraft", "com.mojang", "minecraft").version("1.20.1")

    // https://linkie.shedaniel.dev/dependencies?loader=fabric
    version("fabric-loader", "0.16.9")
    version("fabric-api", "0.92.2+$minecraft")
    library("fabric-loader", "net.fabricmc", "fabric-loader").version("0.16.9")
    library("fabric-api", "net.fabricmc.fabric-api", "fabric-api").version("0.92.2+$minecraft")
    library("fabric-kotlin", "net.fabricmc", "fabric-language-kotlin").version("1.13.0+kotlin.$kotlin")

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

    val mixinsquared = "0.2.0"
    library("mixinsquared-common", "com.github.bawnorton.mixinsquared", "mixinsquared-common").version(mixinsquared)
    library("mixinsquared-lexforge", "com.github.bawnorton.mixinsquared", "mixinsquared-forge").version(mixinsquared)
    library("mixinsquared-fabric", "com.github.bawnorton.mixinsquared", "mixinsquared-fabric").version(mixinsquared)


    library("modmenu", "com.terraformersmc", "modmenu").version("7.2.2")

    library("mixin-constraints", "com.moulberry", "mixinconstraints").version("1.0.7")

    library("yacl-fabric", "dev.isxander", "yet-another-config-lib").version("3.6.1+1.20.1-fabric")
    library("yacl-forge", "dev.isxander", "yet-another-config-lib").version("3.6.1+1.20.1-forge")

    library("kiwi-forge", "maven.modrinth", "kiwi").version("11.8.20+forge")

    val modernfix = "5.19.5+mc1.20.1"
    library("modernfix-fabric", "maven.modrinth", "modernfix").version("$modernfix-fabric")
    library("modernfix-forge", "maven.modrinth", "modernfix").version("$modernfix-forge")

    val prism = "1.0.5"
    library("prism-fabric", "maven.modrinth", "prism-lib").version("$prism-fabric")
    library("prism-forge", "maven.modrinth", "prism-lib").version("$prism-forge")

    val esf = "0.4"
    library("entity-features-sound-fabric", "maven.modrinth", "esf").version("$esf-fabric")
    library("entity-features-sound-forge", "maven.modrinth", "esf").version("$esf-forge")

    val emf = "2.4.1"
    library("entity-features-model-fabric", "maven.modrinth", "entity-model-features").version("$emf-fabric")
    library("entity-features-model-forge", "maven.modrinth", "entity-model-features").version("$emf-forge")

    val etf = "6.2.9"
    library("entity-features-texture-fabric", "maven.modrinth", "entitytexturefeatures").version("$etf-fabric")
    library("entity-features-texture-forge", "maven.modrinth", "entitytexturefeatures").version("$etf-forge")

    val moremcmeta = "v1.20.1-4.5.1"
    library("moremcmeta-fabric", "maven.modrinth", "moremcmeta").version("$moremcmeta-fabric")
    library("moremcmeta-forge", "maven.modrinth", "moremcmeta").version("$moremcmeta-forge")

    val toomanyplayers = "1.20-1.1.5"
    library("toomanyplayers", "maven.modrinth", "tmp").version(toomanyplayers)

    val axiom = "4.1.1"
    library("axiom", "maven.modrinth", "axiom").version(axiom)

    val quark = "1.20.1-4.0-460"
    library("quark", "maven.modrinth", "quark").version(quark)

    val zeta = "1.20.1-1.0-24"
    library("zeta", "maven.modrinth", "zeta").version(zeta)

    val fusion = "1.1.1"
    library("fusion-fabric", "maven.modrinth", "fusion-connected-textures").version("$fusion-fabric-mc1.20.1")
    library("fusion-forge", "maven.modrinth", "fusion-connected-textures").version("$fusion-forge-mc1.20.1")

    library("bigbrain", "maven.modrinth", "big-brain").version("1.20.1-1.7.4")

    library("caffeine-coroutines", "dev.hsbrysk", "caffeine-coroutines").version("1.2.1")
    library("caffeine", "com.github.ben-manes.caffeine", "caffeine").version("3.1.8")

    val bovinesAndButtercups = "1.11.2"
    library(
        "bovinesAndButtercups-forge",
        "maven.modrinth",
        "bovines-and-buttercups"
    ).version("$bovinesAndButtercups+1.20.1-forge")

    val continuity = "3.0.0"
    library("continuity-fabric", "maven.modrinth", "continuity").version("$continuity+1.20.1")
    library("continuity-forge", "maven.modrinth", "continuity").version("$continuity+1.20.1.forge")

    val almostUnified = "1.20.1-0.9.4"
    library("almostUnified-fabric", "maven.modrinth", "almost-unified").version("$almostUnified+fabric")
    library("almostUnified-forge", "maven.modrinth", "almost-unified").version("$almostUnified+forge")

    val badOptimizations = "2.2.1-1.20.1"
    library("badOptimizations", "maven.modrinth", "badoptimizations").version(badOptimizations)

    val reflect = "1.4.0"
    library("reflect", "net.lenni0451", "Reflect").version(reflect)

    val hash4j = "0.21.0"
    library("hash4j", "com.dynatrace.hash4j", "hash4j").version(hash4j)

    val moonlight = "1.20-2.13.81"
    library("moonlight-fabric", "maven.modrinth", "moonlight").version("fabric_$moonlight")
    library("moonlight-forge", "maven.modrinth", "moonlight").version("forge_$moonlight")

    val everyCompat = "1.20-2.7.25"
    library("everyCompat-fabric", "maven.modrinth", "every-compat").version("$everyCompat-fabric")
    library("everyCompat-forge", "maven.modrinth", "every-compat").version("$everyCompat-forge")

    val simplySwords = "1.56.0-1.20.1"
    library("simplySwords-fabric", "maven.modrinth", "simply-swords").version("$simplySwords-fabric")
    library("simplySwords-forge", "maven.modrinth", "simply-swords").version("$simplySwords-forge")

    val lowDragLib = "mc1.20.1-1.0.38.d"
    library("lowDragLib-forge", "maven.modrinth", "ldlib").version("$lowDragLib-forge")

    val tenshilib = "1.20.1-1.7.6"
    library("tenshilib-forge", "maven.modrinth", "tenshilib").version("$tenshilib-forge")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

val name: String by settings

rootProject.name = name

include(":xplat")
include(":xplat:xplat-lexforge")

include(":lexforge:lexforge-core")
include(":lexforge:lexforge-mod")
include(":lexforge:lexforge-bootstrap")
include(":lexforge:lexforge-mc-bootstrap")