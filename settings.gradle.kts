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

fun interface ArtifactFormatter {
    fun format(artifact: String, loader: String, mcVersion: String): String
    
    companion object {
        val simple = ArtifactFormatter { artifact, _, _ -> artifact }
        val dashLoader = ArtifactFormatter { artifact, loader, _ -> "$artifact-$loader" }
        val dashLoaderMc = ArtifactFormatter { artifact, loader, mcVersion -> "$artifact-$loader-$mcVersion" }
    }
}

fun interface VersionFormatter {
    fun format(version: String, loader: String): String
    
    companion object {
        val simple = VersionFormatter { version, _ -> version }
        val dashLoader = VersionFormatter { version, loader -> "$version-$loader" }
        val plusLoader = VersionFormatter { version, loader -> "$version+$loader" }
        val loaderUnderscore = VersionFormatter { version, loader -> "${loader}_$version" }
    }
}

class LoaderVariantBuilder {
    var artifactFormatter: ArtifactFormatter = ArtifactFormatter.simple
    var versionFormatter: VersionFormatter = VersionFormatter.simple
    
    fun artifact(formatter: ArtifactFormatter) {
        artifactFormatter = formatter
    }
    
    fun artifact(block: (artifact: String, loader: String, mcVersion: String) -> String) {
        artifactFormatter = ArtifactFormatter(block)
    }
    
    fun version(formatter: VersionFormatter) {
        versionFormatter = formatter
    }
    
    fun version(block: (version: String, loader: String) -> String) {
        versionFormatter = VersionFormatter(block)
    }
    
    internal fun build() = LoaderVariant(artifactFormatter, versionFormatter)
}

data class LoaderVariant(
    val artifactFormatter: ArtifactFormatter,
    val versionFormatter: VersionFormatter
)

class McVersionBuilder(private val mcVersion: String) {
    private val loaders = mutableMapOf<String, LoaderVariant>()
    var modVersion: String = ""
    
    fun loader(name: String, block: LoaderVariantBuilder.() -> Unit = {}) {
        loaders[name] = LoaderVariantBuilder().apply(block).build()
    }
    
    internal fun build() = McVersionConfig(mcVersion, modVersion, loaders)
}

data class McVersionConfig(
    val mcVersion: String,
    val modVersion: String,
    val loaders: Map<String, LoaderVariant>
)

class MultiVersionDepBuilder(val id: String, val group: String) {
    var artifact: String = id
    var versionFormat: (String, String) -> String = { _, v -> v }
    
    private val configs = mutableListOf<McVersionConfig>()
    
    fun version(mcVersion: String, block: McVersionBuilder.() -> Unit) {
        configs.add(McVersionBuilder(mcVersion).apply(block).build())
    }
    
    internal fun build() = MultiVersionDep(id, group, artifact, configs, versionFormat)
}

data class MultiVersionDep(
    val id: String,
    val group: String,
    val artifact: String,
    val configs: List<McVersionConfig>,
    val versionFormat: (String, String) -> String
)

fun VersionCatalogBuilder.dependency(id: String, group: String, block: MultiVersionDepBuilder.() -> Unit) {
    val dep = MultiVersionDepBuilder(id, group).apply(block).build()
    
    val allLoaders = dep.configs.flatMap { it.loaders.keys }.toSet()
    val isSingleLoader = allLoaders.size == 1
    val isSingleMcVersion = dep.configs.size == 1

    dep.configs.forEach { config ->
        val version = dep.versionFormat(config.mcVersion, config.modVersion)
        val mcVersionName = "mc${config.mcVersion.replace("_", "")}"
        
        config.loaders.forEach { (loaderName, variant) ->
            val finalArtifact = variant.artifactFormatter.format(dep.artifact, loaderName, config.mcVersion)
            val finalVersion = variant.versionFormatter.format(version, loaderName)
            
            val catalogId = when {
                isSingleMcVersion && isSingleLoader -> dep.id
                isSingleMcVersion -> "${dep.id}-$loaderName"
                isSingleLoader -> "${dep.id}-$mcVersionName"
                else -> "${dep.id}-$mcVersionName-$loaderName"
            }
            
            library(catalogId, dep.group, finalArtifact).version(finalVersion)
        }
    }
}

@Suppress("SpellCheckingInspection")
fun VersionCatalogBuilder.modrinth(id: String, block: MultiVersionDepBuilder.() -> Unit) {
    dependency(id, "maven.modrinth", block)
}

dependencyResolutionManagement.versionCatalogs.create("catalog") {
    dependency("mixinextras", "io.github.llamalad7") {
        artifact = "mixinextras"
        
        version("*") {
            modVersion = "0.5.0"
            loader("forge") { artifact(ArtifactFormatter.dashLoader) }
            loader("fabric") { artifact(ArtifactFormatter.dashLoader) }
            loader("common") { artifact(ArtifactFormatter.dashLoader) }
        }
    }

    library("preloadingTricks", "settingdust.preloading_tricks", "PreloadingTricks")
        .version("3.4.5")

    dependency("klf", "dev.nyon") {
        artifact = "KotlinLangForge"
        versionFormat = { _, ver -> "2.10.6-k2.2.21-$ver" }
        
        version("1_20") {
            modVersion = "2.0"
            loader("forge") { version(VersionFormatter.plusLoader) }
        }
        
        version("1_21") {
            modVersion = "3.1"
            loader("neoforge") { version(VersionFormatter.plusLoader) }
        }
    }

    library("mixin-fabric", "net.fabricmc", "sponge-mixin")
        .version("0.17.0+mixin.0.8.7")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Lazyyyyy"