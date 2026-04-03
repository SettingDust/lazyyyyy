@file:Suppress("UnstableApiUsage", "INVISIBLE_REFERENCE")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.forgeMinecraft
import earth.terrarium.cloche.target.LazyConfigurableInternal
import earth.terrarium.cloche.tasks.GenerateFabricModJson
import earth.terrarium.cloche.util.target
import groovy.lang.Closure
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.fabric.task.JarInJar
import net.msrandom.minecraftcodev.forge.task.JarJar
import net.msrandom.minecraftcodev.runs.MinecraftRunConfiguration
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.jvm.tasks.Jar
import java.nio.charset.StandardCharsets

plugins {
    java
    idea

    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"

    id("com.palantir.git-version") version "4.2.0"

    id("com.gradleup.shadow") version "9.3.0"

    id("earth.terrarium.cloche") version "0.18.10-dust.4"
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val source: String by rootProject.properties

group = "settingdust.lazyyyyy"

val gitVersion: Closure<String> by extra
version = gitVersion()

base { archivesName = archive_name }

//region Container DSL

private fun MinecraftModLoader.containerFeatureName(): String =
    lowerCamelCaseGradleName("container", toString().lowercase())

class ContainerScope(
    private val project: Project,
    val loader: MinecraftModLoader,
) {
    val featureName: String = loader.containerFeatureName()

    val intermediateOutputsDirectory = project.layout.buildDirectory.dir("libs/intermediates")

    private val includeConfigurationProvider =
        project.configurations.register(lowerCamelCaseGradleName(featureName, "include")) {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false

            attributes {
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                attribute(REMAPPED_ATTRIBUTE, false)
                attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            }
        }

    private val includeDevConfigurationProvider =
        project.configurations.register(lowerCamelCaseGradleName(featureName, "includeDev")) {
            isCanBeResolved = true
            isCanBeConsumed = false
            isTransitive = false

            attributes {
                attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                attribute(REMAPPED_ATTRIBUTE, true)
                attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
            }
        }

    private val embedConfigurations = mutableMapOf<String, NamedDomainObjectProvider<Configuration>>()

    val jarTask = project.tasks.register<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
        group = "build"
        archiveClassifier = loader.toString().lowercase()
        destinationDirectory = intermediateOutputsDirectory
    }

    val includeJarTask: TaskProvider<out Jar> =
        createPackageTask("includeJar", includeConfigurationProvider)
    val includeDevJarTask: TaskProvider<out Jar> =
        createPackageTask(
            "includesDevJar",
            includeDevConfigurationProvider,
            archiveClassifier = "${loader.toString().lowercase()}-dev",
            toIntermediateOutputs = true,
        )

    init {
        project.tasks.build {
            dependsOn(includeJarTask, includeDevJarTask)
        }

        val containerCapability = "${project.group}:${project.name}-${loader.toString().lowercase()}:${project.version}"

        project.configurations.register(lowerCamelCaseGradleName(featureName, "runtimeElements")) {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                applyRuntimeVariantAttributes(remapped = false)
            }
            outgoing.artifact(includeJarTask)
            outgoing.capability(containerCapability)
        }

        project.configurations.register(lowerCamelCaseGradleName(featureName, "devRuntimeElements")) {
            isCanBeResolved = false
            isCanBeConsumed = true
            attributes {
                applyRuntimeVariantAttributes(remapped = true)
            }
            outgoing.artifact(includeDevJarTask)
            outgoing.capability(containerCapability)
        }
    }

    private fun AttributeContainer.applyRuntimeVariantAttributes(remapped: Boolean) {
        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
        attribute(TargetAttributes.MOD_LOADER, loader)
        attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
        attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        attribute(REMAPPED_ATTRIBUTE, remapped)
        if (remapped) {
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
        }
    }

    private fun createPackageTask(
        name: String,
        configuration: NamedDomainObjectProvider<Configuration>,
        archiveClassifier: String = loader.toString().lowercase(),
        toIntermediateOutputs: Boolean = false,
    ): TaskProvider<out Jar> = when (loader) {
        MinecraftModLoader.fabric -> project.tasks.register<JarInJar>(lowerCamelCaseGradleName(featureName, name)) {
            group = "build"
            this.archiveClassifier = archiveClassifier
            if (toIntermediateOutputs) {
                destinationDirectory = intermediateOutputsDirectory
            }
            input = jarTask.flatMap { it.archiveFile }
            fromResolutionResults(configuration)
        }

        else -> project.tasks.register<JarJar>(lowerCamelCaseGradleName(featureName, name)) {
            group = "build"
            this.archiveClassifier = archiveClassifier
            if (toIntermediateOutputs) {
                destinationDirectory = intermediateOutputsDirectory
            }
            input = jarTask.flatMap { it.archiveFile }
            fromResolutionResults(configuration)
        }
    }

    private fun ModuleDependency.withIncludeAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, false)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        }
    }

    private fun ModuleDependency.withIncludeDevAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, true)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
        }
    }

    private fun embedConfigurationName(name: String): String =
        if (name.isBlank()) {
            lowerCamelCaseGradleName(featureName, "embed")
        } else {
            lowerCamelCaseGradleName(featureName, "embed", name)
        }

    private fun Configuration.applyDefaultEmbedAttributes() {
        attributes {
            attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                project.objects.named(LibraryElements.CLASSES_AND_RESOURCES)
            )
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
        }
    }

    private fun Configuration.applyTransformedJarAttributes() {
        attributes {
            attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
            attribute(REMAPPED_ATTRIBUTE, false)
            attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
            attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
        }
    }

    inner class DependenciesScope(private val handler: DependencyHandler) : DependencyHandler by handler {
        private fun addTo(
            configuration: NamedDomainObjectProvider<Configuration>,
            dependencyNotation: Any,
            configure: ModuleDependency.() -> Unit = {},
        ): Dependency? {
            val dependency = handler.add(configuration.get().name, dependencyNotation)
            if (dependency is ModuleDependency) {
                dependency.configure()
            }
            return dependency
        }

        fun include(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            addTo(includeConfigurationProvider, dependencyNotation, configure)

        fun includeDev(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            addTo(includeDevConfigurationProvider, dependencyNotation, configure)

        fun embed(dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? =
            embed("", dependencyNotation, configure)

        fun embed(name: String, dependencyNotation: Any, configure: ModuleDependency.() -> Unit = {}): Dependency? {
            val configuration = embedConfigurations[name]
                ?: throw IllegalArgumentException("embed('$name') is not registered for $featureName")
            return addTo(configuration, dependencyNotation, configure)
        }

        fun includeTarget(target: MinecraftTarget) {
            includeJarTask.configure {
                dependsOn(target.includeJarTaskName)
            }
            includeDevJarTask.configure {
                dependsOn(target.jarTaskName)
            }

            include(target(target)) {
                withIncludeAttributes()
            }
            includeDev(target(target)) {
                withIncludeDevAttributes()
            }
        }

        override fun variantOf(
            dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>,
            variantSpec: Action<in ExternalModuleDependencyVariantSpec>
        ): Provider<MinimalExternalModuleDependency> {
            return handler.variantOf(dependencyProviderConvertible, variantSpec)
        }

        override fun platform(dependencyProvider: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.platform(dependencyProvider)
        }

        override fun platform(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.platform(dependencyProviderConvertible)
        }

        override fun enforcedPlatform(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.enforcedPlatform(dependencyProviderConvertible)
        }

        override fun testFixtures(dependencyProvider: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.testFixtures(dependencyProvider)
        }

        override fun testFixtures(dependencyProviderConvertible: ProviderConvertible<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> {
            return handler.testFixtures(dependencyProviderConvertible)
        }
    }

    fun embed(
        name: String = "",
        configureConfiguration: Configuration.() -> Unit = { applyDefaultEmbedAttributes() },
        configure: CopySpec.() -> Unit = {},
    ) {
        require(name !in embedConfigurations) { "embed('$name') is already registered for $featureName" }

        val configuration = project.configurations.register(embedConfigurationName(name)) {
            isCanBeResolved = true
            isTransitive = false
            configureConfiguration()
        }
        embedConfigurations[name] = configuration

        jarTask.configure {
            from(configuration) {
                configure()
            }
        }
    }

    fun dependencies(block: DependenciesScope.() -> Unit) {
        DependenciesScope(project.dependencies).block()
    }

    fun jar(block: Jar.() -> Unit) {
        jarTask.configure(block)
    }
}

fun ClocheExtension.container(
    loader: MinecraftModLoader,
    block: ContainerScope.() -> Unit,
) {
    ContainerScope(project, loader).apply(block)
}

//endregion

//region Repositories

repositories {
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven")
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://repo.nyon.dev/releases") {
        content {
            includeGroup("dev.nyon")
        }
    }

    maven("https://maven.lenni0451.net/snapshots/") {
        content {
            includeGroupAndSubgroups("net.lenni0451")
        }
    }

    maven("https://maven.su5ed.dev/releases") {
        content {
            includeGroupAndSubgroups("dev.su5ed.sinytra")
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://maven.sinytra.org/") {
        content {
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://raw.githubusercontent.com/settingdust/maven/main/repository/") {
        name = "SettingDust's Maven"
    }

    mavenCentral()

    cloche {
        librariesMinecraft()
        main()
        mavenFabric()
        mavenForge()
        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenParchment()
    }

    mavenLocal()
}

//endregion

//region Dependency Attributes

class MinecraftVersionCompatibilityRule : AttributeCompatibilityRule<String> {
    override fun execute(details: CompatibilityCheckDetails<String>) {
        details.compatible()
    }
}

class MinecraftModLoaderCompatibilityRule : AttributeCompatibilityRule<MinecraftModLoader> {
    override fun execute(details: CompatibilityCheckDetails<MinecraftModLoader>) {
        if (details.producerValue == MinecraftModLoader.common) {
            details.compatible()
        }
    }
}

dependencies {
    attributesSchema {
        attribute(TargetAttributes.MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MINECRAFT_VERSION) {
            compatibilityRules.add(MinecraftVersionCompatibilityRule::class)
        }
        attribute(TargetAttributes.CLOCHE_MOD_LOADER) {
            compatibilityRules.add(MinecraftModLoaderCompatibilityRule::class)
        }
    }
}

//endregion

//region Cloche Configuration

cloche {
    metadata {
        modId = id
        name = rootProject.property("name").toString()
        description = rootProject.property("description").toString()
        license = "Apache License 2.0"
        icon = "assets/$id/icon.png"
        sources = source
        issues = "$source/issues"
        author("SettingDust")

        dependency {
            modId = "minecraft"
            type = CommonMetadata.Dependency.Type.Required
            version {
                start = "1.20.1"
            }
        }
    }

    mappings {
        official()
    }

    //region Common Targets

    common()

    val commonMain = common("common:common") {
        configurations.named("commonCommonRuntimeElements") {
            attributes {
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
            }
        }
    }

    val common1201 = common("common:1.20.1") {
        dependsOn(commonMain)
        // mixins.from("src/common/1.20.1/main/resources/$id.1_20.mixins.json")
    }
    val common121 = common("common:1.21.1") {
        dependsOn(commonMain)
        // mixins.from("src/common/1.21.1/main/resources/$id.1_21.mixins.json")
    }

    val game = common("game") {
        mixins.from(files("src/game/main/resources/$id.game.mixins.json"))
        accessWideners.from(file("src/game/main/resources/$id.game.accessWidener"))

        project.dependencies {
            val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

            implementation(project(":")) {
                capabilities {
                    requireFeature(commonMain.capabilitySuffix)
                }
            }

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }

    val game1201 = common("game:1.20.1") {
        dependsOn(game)
        mixins.from(files("src/game/1.20.1/main/resources/$id.game.1.20.1.mixins.json"))

        project.dependencies {
            val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

            implementation(project(":")) {
                capabilities {
                    requireFeature(commonMain.capabilitySuffix)
                }
            }

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }

    val game121 = common("game:1.21.1") {
        dependsOn(game)
        mixins.from(files("src/game/1.21.1/main/resources/$id.game.1.21.1.mixins.json"))

        project.dependencies {
            val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

            implementation(project(":")) {
                capabilities {
                    requireFeature(commonMain.capabilitySuffix)
                }
            }

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
    }

    val fasterModuleResolver = common("faster-module-resolver") {
        dependencies {
            compileOnly(catalog.mixin.fabric)
        }

        project.dependencies {
            val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

            implementation(project(":")) {
                capabilities {
                    requireFeature(commonMain.capabilitySuffix)
                }
            }
        }
    }

    val fasterMixin = common("faster-mixin") {
        project.dependencies {
            val compileOnly = lowerCamelCaseGradleName(name, JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

            compileOnly(catalog.mixin.fabric)
            compileOnly(catalog.hash4j)
        }
    }

    //endregion

    //region Fabric Targets

    run fabric@{
        val fabricCommon = common("fabric:common") {
            dependsOn(commonMain, game, fasterModuleResolver)
            stubSources(fasterMixin)

            project.dependencies {
                val implementation = lowerCamelCaseGradleName(name, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }
            }
        }

        val fabric1201 = fabric("fabric:1.20.1") {
            dependsOn(common1201, game1201, fasterModuleResolver, fabricCommon)

            minecraftVersion = "1.20.1"

            mappings {
                mcpSearge()
            }

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.20.1"
                        end = "1.21"
                    }
                }
            }

            dependencies {
                fabricApi("0.92.7")

                remapClasspath(forgeMinecraft("1.20.1", "47.4.4"))

                implementation(catalog.preloadingTricks)
                implementation(catalog.betterLog4jConfig)
                localImplementation(catalog.hash4j)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }
            }
        }

        val fabric121 = fabric("fabric:1.21") {
            dependsOn(common121, game121, fasterModuleResolver, fabricCommon)

            minecraftVersion = "1.21.1"

            mappings {
                neoforgeSearge("20240808.144430")
            }

            metadata {
                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.21"
                    }
                }
            }

            dependencies {
                fabricApi("0.116.6")

                implementation(catalog.preloadingTricks)
                implementation(catalog.betterLog4jConfig)
                localImplementation(catalog.hash4j)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }

                modImplementation(catalog.dynamictrees.mc121.fabric)
            }
        }

        fabric("version:fabric:1.20.1") {
            minecraftVersion = "1.20.1"

            runs { client() }

            dependencies {
                fabricApi("0.92.6")

                runtimeOnly(skipIncludeTransformation(project(":"))) {
                    capabilities {
                        requireFeature("fabric")
                    }

                    exclude("settingdust.lazyyyyy")
                }

                runtimeOnly(catalog.preloadingTricks)
                runtimeOnly(catalog.betterLog4jConfig)
            }

            tasks {
                named(generateModsManifestTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }

        fabric("version:fabric:1.21") {
            minecraftVersion = "1.21.1"

            runs { client() }

            dependencies {
                fabricApi("0.116.6")

                runtimeOnly(skipIncludeTransformation(project(":"))) {
                    capabilities {
                        requireFeature("fabric")
                    }

                    exclude("settingdust.lazyyyyy")
                }

                runtimeOnly(catalog.preloadingTricks)
                runtimeOnly(catalog.betterLog4jConfig)
            }

            tasks {
                named(generateModsManifestTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }
            }
        }

        val containerFabricFeatureName = MinecraftModLoader.fabric.containerFeatureName()
        val containerFabricMetadataDirectory = project.layout.buildDirectory.dir("generated")
            .map { it.dir("metadata").dir(containerFabricFeatureName) }
        val generateContainerFabricModJson =
            tasks.register<GenerateFabricModJson>(
                lowerCamelCaseGradleName(
                    containerFabricFeatureName,
                    "generateModJson"
                )
            ) {
                modId = "${id}_container"
                metadata = objects.newInstance(FabricMetadata::class.java, fabric1201).apply {
                    license.value(cloche.metadata.license)
                    dependencies.value(cloche.metadata.dependencies)
                }
                loaderDependencyVersion = "0.18"
                output.set(containerFabricMetadataDirectory.map { it.file("fabric.mod.json") })
            }

        container(loader = MinecraftModLoader.fabric) {
            embed("boot", configureConfiguration = {
                applyTransformedJarAttributes()
            }) {
                into("boot")
            }

            dependencies {
                includeTarget(fabric1201)
                includeTarget(fabric121)

                embed("boot", project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }
                embed("boot", catalog.hash4j)
            }

            jar {
                dependsOn(generateContainerFabricModJson)
                from(containerFabricMetadataDirectory)
            }
        }

        targets.withType<FabricTarget> {
            loaderVersion = "0.18.4"

            includedClient()

            metadata {
                entrypoint("main") {
                    adapter = "kotlin"
                    value = "$group.fabric.LazyyyyyFabric::init"
                }

                entrypoint("client") {
                    adapter = "kotlin"
                    value = "$group.fabric.LazyyyyyFabricClient::init"
                }

                dependency {
                    modId = "fabric-api"
                    type = CommonMetadata.Dependency.Type.Required
                }

                dependency {
                    modId = "fabric-language-kotlin"
                    type = CommonMetadata.Dependency.Type.Required
                }
            }

            dependencies {
                modImplementation("net.fabricmc:fabric-language-kotlin:1.13.7+kotlin.2.2.21")
            }
        }
    }

    //endregion

    //region Forge Targets

    run forge@{
        val forgeService = forge("forge:service") {
            dependsOn(commonMain, common1201, fasterModuleResolver)

            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            dependencies {
                implementation(catalog.mixin.fabric)
                compileOnly(catalog.mixinextras.common)
                implementation(catalog.mixinextras.forge)

                implementation(catalog.preloadingTricks)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }

                implementation(catalog.hash4j)
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }
            }
        }

        val forgeGame = forge("forge:game") {
            dependsOn(game1201)

            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            metadata {
                modLoader = "klf"
                loaderVersion {
                    start = "1"
                }

                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.20.1"
                        end = "1.21"
                    }
                }

                dependency {
                    modId = "preloading_tricks"
                    type = CommonMetadata.Dependency.Type.Required
                }
            }

            dependencies {
                implementation(catalog.mixin.fabric)
                compileOnly(catalog.mixinextras.common)
                implementation(catalog.mixinextras.forge)

                implementation(catalog.preloadingTricks)

                modImplementation(catalog.klf.mc120.forge)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(forgeService.capabilitySuffix!!)
                    }
                }

                implementation(project(":")) {
                    capabilities {
                        requireFeature(commonMain.capabilitySuffix)
                    }
                }

                implementation(catalog.hash4j)

                modImplementation(catalog.dynamictrees.mc120.forge)

                modImplementation(catalog.forgifiedFabricApi.mc120)
            }

            tasks {
                named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    manifest {
                        attributes(
                            "ForgeVariant" to "LexForge"
                        )
                    }
                }

                named(accessWidenTaskName) {
                    dependsOn(forgeService.accessWidenTaskName)
                }
            }
        }

        forge("version:forge:1.20.1") {
            minecraftVersion = "1.20.1"
            loaderVersion = "47.4.4"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                legacyClasspath(catalog.mixin.fabric)
                runtimeOnly(skipIncludeTransformation(project(":"))) {
                    capabilities {
                        requireFeature("forge")
                    }
                }

                runtimeOnly(catalog.preloadingTricks)

                modRuntimeOnly(catalog.klf.mc120.forge)
            }

            tasks {
                named(generateModsManifestTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }

                named(accessWidenTaskName) {
                    dependsOn(forgeService.accessWidenTaskName, forgeGame.accessWidenTaskName)
                }
            }

            configurations.named(sourceSet.runtimeClasspathConfigurationName) {
                exclude("org.spongepowered", "mixin")
            }

            configurations.named(lowerCamelCaseGradleName(featureName, "minecraftLibraries")) {
                exclude("org.spongepowered", "mixin")
            }
        }

        container(loader = MinecraftModLoader.forge) {
            embed()

            embed("mixin", configureConfiguration = {
                attributes {
                    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    attribute(REMAPPED_ATTRIBUTE, false)
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                }
            }) {
                rename { "sponge-mixin.jar" }
            }

            embed("boot", configureConfiguration = {
                attributes {
                    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    attribute(REMAPPED_ATTRIBUTE, false)
                    attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
                }
            }) {
                into("boot")
            }

            dependencies {
                includeTarget(forgeGame)

                embed(project(":")) {
                    capabilities {
                        requireFeature(forgeService.capabilitySuffix!!)
                    }
                }

                embed("mixin", catalog.mixin.fabric)

                embed("boot", project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }
                embed("boot", catalog.hash4j)
            }
        }
    }

    //endregion

    //region NeoForge Targets

    run neoforge@{
        val neoforgeService = neoforge("neoforge:service") {
            dependsOn(commonMain, common121, fasterModuleResolver)

            minecraftVersion = "1.21.1"
            loaderVersion = "21.1.192"

            dependencies {
                compileOnly(catalog.mixinextras.common)
                implementation(catalog.mixinextras.forge)

                implementation(catalog.preloadingTricks)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(fasterMixin.capabilitySuffix)
                    }
                }

                implementation(catalog.hash4j)
            }

            tasks {
                named(generateModsTomlTaskName) {
                    enabled = false
                }
            }
        }

        val neoforgeGame = neoforge("neoforge:game") {
            dependsOn(game121)

            minecraftVersion = "1.21.1"
            loaderVersion = "21.1.192"

            metadata {
                modLoader = "klf"
                loaderVersion {
                    start = "1"
                }

                dependency {
                    modId = "minecraft"
                    type = CommonMetadata.Dependency.Type.Required
                    version {
                        start = "1.21"
                    }
                }

                dependency {
                    modId = "preloading_tricks"
                    type = CommonMetadata.Dependency.Type.Required
                }
            }

            repositories {
                maven("https://repo.spongepowered.org/maven") {
                    content {
                        includeGroup("org.spongepowered")
                    }
                }
            }

            dependencies {
                compileOnly(catalog.mixinextras.common)
                implementation(catalog.mixinextras.forge)

                implementation(catalog.preloadingTricks)

                modImplementation(catalog.klf.mc121.neoforge)

                implementation(project(":")) {
                    capabilities {
                        requireFeature(neoforgeService.capabilitySuffix!!)
                    }
                }

                implementation(project(":")) {
                    capabilities {
                        requireFeature(commonMain.capabilitySuffix)
                    }
                }

                implementation(catalog.hash4j)

                implementation(catalog.dynamictrees.mc121.neoforge)

                modImplementation(catalog.forgifiedFabricApi.mc121)
            }

            tasks {
                named<Jar>(lowerCamelCaseGradleName(featureName, "jar")) {
                    manifest {
                        attributes(
                            "ForgeVariant" to "NeoForge"
                        )
                    }
                }

                named(accessWidenTaskName) {
                    dependsOn(neoforgeService.accessWidenTaskName)
                }
            }
        }

        neoforge("version:neoforge:1.21.1") {
            minecraftVersion = "1.21.1"
            loaderVersion = "21.1.192"

            runs {
                client {
                    env("MOD_CLASSES", "")
                }
            }

            dependencies {
                legacyClasspath(skipIncludeTransformation(project(":"))) {
                    capabilities {
                        requireFeature("neoforge")
                    }
                }

                legacyClasspath(catalog.preloadingTricks) {
                    isTransitive = false
                }
            }

            tasks {
                named(generateModsManifestTaskName) {
                    enabled = false
                }

                named(jarTaskName) {
                    enabled = false
                }

                named(remapJarTaskName) {
                    enabled = false
                }

                named(includeJarTaskName) {
                    enabled = false
                }

                named(accessWidenTaskName) {
                    dependsOn(neoforgeService.accessWidenTaskName, neoforgeGame.accessWidenTaskName)
                }
            }
        }

        container(loader = MinecraftModLoader.neoforge) {
            embed()

            embed("boot", configureConfiguration = {
                attributes
                    .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(REMAPPED_ATTRIBUTE, false)
                    .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, true)
                    .attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            }) {
                into("boot")
            }

            dependencies {
                embed("boot", target(fasterMixin))
                embed("boot", catalog.hash4j)

                includeTarget(neoforgeGame)

                embed(target(neoforgeService))
            }
        }
    }

    //endregion

    //region Shared Target Defaults

    targets.all {
        runs {
            (client as LazyConfigurableInternal<MinecraftRunConfiguration>).onConfigured {
                it.jvmArguments(
                    "-Dmixin.debug.verbose=true",
                    "-Dmixin.debug.export=true",
                    "-Dclasstransform.dumpClasses=true"
                )
            }
        }

        mappings {
            minecraftVersion.orNull
                ?.let {
                    when (it) {
                        "1.20.1" -> "2023.09.03"
                        "1.21.1" -> "2024.11.17"
                        "1.21.10" -> "2025.10.12"
                        else -> null
                    }
                }
                ?.let(::parchment)
        }
    }

    //endregion
}

//endregion

//region Task Helpers

val SourceSet.includeJarTaskName: String
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "includeJar")

val MinecraftTarget.includeJarTaskName: String
    get() = when (this) {
        is FabricTarget -> sourceSet.includeJarTaskName
        is ForgeLikeTarget -> sourceSet.includeJarTaskName
        else -> throw IllegalArgumentException("Unsupported target $this")
    }

val FabricTarget.generateModsJsonTaskName: String
    get() = lowerCamelCaseGradleName("generate", featureName, "ModJson")

val ForgeLikeTarget.generateModsTomlTaskName: String
    get() = lowerCamelCaseGradleName("generate", featureName, "modsToml")

val MinecraftTarget.generateModsManifestTaskName: String
    get() = when (this) {
        is FabricTarget -> generateModsJsonTaskName
        is ForgeLikeTarget -> generateModsTomlTaskName
        else -> throw IllegalArgumentException("Unsupported target $this")
    }

val MinecraftTarget.jarTaskName: String
    get() = lowerCamelCaseGradleName(featureName, "jar")

val MinecraftTarget.remapJarTaskName: String
    get() = lowerCamelCaseGradleName(featureName, "remapJar")

val MinecraftTarget.accessWidenTaskName: String
    get() = lowerCamelCaseGradleName("accessWiden", featureName, "minecraft")

val MinecraftTarget.decompileMinecraftTaskName: String
    get() = lowerCamelCaseGradleName("decompile", featureName, "minecraft")

//endregion

//region Build Tasks

tasks {
    withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    shadowJar {
        enabled = false
    }

    val shadowContainersJar by registering(ShadowJar::class) {
        archiveClassifier = ""

        val fabricJar = project.tasks.named<Jar>(lowerCamelCaseGradleName("containerFabric", "includeJar"))
        from(fabricJar.map { zipTree(it.archiveFile) })
        manifest.from(fabricJar.get().manifest)

        val forgeJar = project.tasks.named<Jar>(lowerCamelCaseGradleName("containerForge", "includeJar"))
        from(forgeJar.map { zipTree(it.archiveFile) })
        manifest.from(forgeJar.get().manifest)

        val neoforgeJar =
            project.tasks.named<Jar>(lowerCamelCaseGradleName("containerNeoforge", "includeJar"))
        from(neoforgeJar.map { zipTree(it.archiveFile) }) {
            include("settingdust/$id/neoforge/**/*")
            include("META-INF/services/*")
            include("META-INF/jarjar/*")
            include("META-INF/jars/*")
        }

        mergeServiceFiles()

        append("META-INF/accesstransformer.cfg")

        transform(object : ResourceTransformer {
            private val gson = GsonBuilder().setPrettyPrinting().create()
            private val collected = JsonArray()
            private val path = "META-INF/jarjar/metadata.json"
            private var transformed = false

            override fun canTransformResource(element: FileTreeElement): Boolean {
                return element.path == path
            }

            override fun transform(context: TransformerContext) {
                context.inputStream.use { input ->
                    val json = gson.fromJson(input.reader(Charsets.UTF_8), JsonObject::class.java)
                    val jars = json.getAsJsonArray("jars")
                    jars?.forEach { collected.add(it) }
                    transformed = true
                }
            }

            override fun hasTransformedResource(): Boolean = transformed

            override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
                if (collected.size() == 0) return

                val merged = JsonObject().apply {
                    add("jars", collected)
                }

                os.putNextEntry(ZipEntry(path))
                os.write(gson.toJson(merged).toByteArray(StandardCharsets.UTF_8))
                os.closeEntry()
            }
        })
    }

    val shadowSourcesJar by registering(ShadowJar::class) {
        dependsOn(cloche.targets.map { it.generateModsManifestTaskName })

        mergeServiceFiles()
        archiveClassifier.set("sources")
        from(sourceSets.map { it.allSource })

        doFirst {
            manifest {
                from(source.filter { it.name.equals("MANIFEST.MF") }.toList())
            }
        }
    }

    build {
        dependsOn(shadowContainersJar, shadowSourcesJar)
    }

    afterEvaluate {
        (components["java"] as AdhocComponentWithVariants).apply {
            val testTargets = cloche.targets.filter { it.name.startsWith("version:") }

            testTargets.forEach { target ->
                listOf(
                    "${target.featureName}ApiElements",
                    "${target.featureName}RuntimeElements"
                ).forEach { variantName ->
                    configurations.findByName(variantName)?.let { config ->
                        withVariantsFromConfiguration(config) {
                            skip()
                        }
                    }
                }
            }
        }
    }
}

//endregion