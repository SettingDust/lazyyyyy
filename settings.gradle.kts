import maven

dependencyResolutionManagement {
    pluginManagement {
        repositories {
            mavenCentral()
            gradlePluginPortal()
            maven("https://maven.msrandom.net/repository/cloche")
            maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
                name = "SettingDust's Maven"
            }
            mavenLocal()
        }
    }
}

object VersionFormats {
    val versionPlusMc = { mcVer: String, ver: String -> "$ver+$mcVer" }
    val mcDashVersion = { mcVer: String, ver: String -> "$mcVer-$ver" }
}

object VersionTransformers {
    val versionDashLoader = { ver: String, variant: String -> "$ver-$variant" }
    val loaderUnderlineVersion = { ver: String, variant: String -> "${variant}_$ver" }
    val versionPlusLoader = { ver: String, variant: String -> "$ver+$variant" }
}

object ArtifactTransformers {
    val artifactDashLoaderDashMcVersion =
        { artifact: String, variant: String, mcVersion: String -> "$artifact-$variant-$mcVersion" }
    val artifactDashLoader = { artifact: String, variant: String, _: String -> "$artifact-$variant" }
}

open class VariantConfig(
    val artifactTransformer: (artifact: String, variant: String, mcVersion: String) -> String = { artifact, _, _ -> artifact },
    val versionTransformer: (version: String, variant: String) -> String = { ver, _ -> ver }
) {
    companion object : VariantConfig()
}

data class VariantMapping(
    val mcVersion: String,
    val loaders: Map<String, VariantConfig>
)

fun VersionCatalogBuilder.modrinth(
    id: String,
    artifact: String = id,
    mcVersionToVersion: Map<String, String>,
    versionFormat: (String, String) -> String = { _, v -> v },
    mapping: List<VariantMapping> = emptyList()
) {
    val allLoaders = mapping.flatMap { it.loaders.keys }.toSet()
    val isSingleLoader = allLoaders.size == 1
    val isSingleMcVersion = mcVersionToVersion.size == 1

    if (isSingleMcVersion) {
        val (mcVersion, modVersion) = mcVersionToVersion.entries.single()
        val config = mapping.find { it.mcVersion == mcVersion }
            ?: error("No loader config found for MC $mcVersion")

        val version = versionFormat(mcVersion, modVersion)

        config.loaders.forEach { (loaderName, loader) ->
            library(
                if (isSingleLoader) "$id"
                else "${id}_$loaderName",
                "maven.modrinth",
                loader.artifactTransformer(artifact, loaderName, mcVersion)
            ).version(loader.versionTransformer(version, loaderName))
        }
        return
    }

    mcVersionToVersion.forEach { (mcVersion, modVersion) ->
        val config = mapping.find { it.mcVersion == mcVersion }
            ?: error("No loader config found for MC $mcVersion")

        val version = versionFormat(mcVersion, modVersion)

        config.loaders.forEach { (loaderName, loader) ->
            library(
                if (isSingleLoader) "${id}_${mcVersion}"
                else "${id}_${mcVersion}_$loaderName",
                "maven.modrinth",
                loader.artifactTransformer(artifact, loaderName, mcVersion)
            ).version(loader.versionTransformer(version, loaderName))
        }
    }
}

fun VersionCatalogBuilder.maven(
    id: String,
    group: String,
    artifact: String = id,
    mcVersionToVersion: Map<String, String>,
    versionFormat: (String, String) -> String = { _, v -> v },
    mapping: List<VariantMapping> = emptyList()
) {
    val allLoaders = mapping.flatMap { it.loaders.keys }.toSet()
    val isSingleLoader = allLoaders.size == 1
    val isSingleMcVersion = mcVersionToVersion.size == 1

    if (isSingleMcVersion) {
        val (mcVersion, modVersion) = mcVersionToVersion.entries.single()
        val config = mapping.find { it.mcVersion == mcVersion }
            ?: error("No loader config found for MC $mcVersion")

        val version = versionFormat(mcVersion, modVersion)

        config.loaders.forEach { (loaderName, loader) ->
            library(
                if (isSingleLoader) id
                else "${id}_$loaderName",
                group,
                loader.artifactTransformer(artifact, loaderName, mcVersion)
            ).version(loader.versionTransformer(version, loaderName))
        }
        return
    }

    mcVersionToVersion.forEach { (mcVersion, baseVersion) ->
        val config = mapping.find { it.mcVersion == mcVersion }
            ?: error("No loader config found for MC $mcVersion")

        val version = versionFormat(mcVersion, baseVersion)

        config.loaders.forEach { (loaderName, loader) ->
            library(
                if (mcVersion == "*") {
                    if (isSingleLoader) id
                    else "${id}_$loaderName"
                } else {
                    if (isSingleLoader) "${id}_${mcVersion}"
                    else "${id}_${mcVersion}_$loaderName"
                },
                group,
                loader.artifactTransformer(artifact, loaderName, mcVersion)
            ).version(loader.versionTransformer(version, loaderName))
        }
    }
}

dependencyResolutionManagement.versionCatalogs.create("catalog") {
    maven(
        id = "mixinextras",
        group = "io.github.llamalad7",
        artifact = "mixinextras",
        mcVersionToVersion = mapOf("*" to "0.5.0"),
        versionFormat = { _, v -> v },
        mapping = listOf(
            VariantMapping(
                "*", mapOf(
                    "forge" to VariantConfig(ArtifactTransformers.artifactDashLoader),
                    "fabric" to VariantConfig(ArtifactTransformers.artifactDashLoader),
                    "common" to VariantConfig(ArtifactTransformers.artifactDashLoader)
                )
            )
        )
    )

    library("preloadingTricks", "settingdust.preloading_tricks", "PreloadingTricks").version("3.4.5")

    maven(
        id = "klf",
        group = "dev.nyon",
        artifact = "KotlinLangForge",
        mcVersionToVersion = mapOf(
            "1.20" to "2.0",
            "1.21" to "3.1"
        ),
        versionFormat = { _, ver -> "2.10.6-k2.2.21-$ver" },
        mapping = listOf(
            VariantMapping(
                "1.20", mapOf(
                    "forge" to VariantConfig(versionTransformer = VersionTransformers.versionPlusLoader)
                )
            ),
            VariantMapping(
                "1.21", mapOf(
                    "neoforge" to VariantConfig(versionTransformer = VersionTransformers.versionPlusLoader),
                )
            )
        )
    )
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Lazyyyyy"