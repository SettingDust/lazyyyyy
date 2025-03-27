plugins {
    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)

    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)

    alias(catalog.plugins.shadow)
}

val id: String by rootProject.properties

architectury {
    forge()
    platformSetupLoomIde()
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())
}

loom {
    accessWidenerPath = project(":xplat").file("src/main/resources/$id.accesswidener")

    forge {
        convertAccessWideners = true

        mixinConfig("$id.forge.mixins.json")
    }

    addRemapConfiguration("modForgeRuntimeLibrary") {
        targetConfigurationName = "forgeRuntimeLibrary"
        onCompileClasspath = true
        onRuntimeClasspath = true
    }

    mixin {
        defaultRefmapName = "$id.forge.refmap.json"
    }
}

dependencies {
    catalog.mixinextras.common.let {
        compileOnly(it)
        annotationProcessor(it)
    }

    catalog.mixinextras.lexforge.let {
        include(it)
        implementation(it)
    }

    catalog.kotlinx.coroutines.debug.let {
//        shadow(it) {
//            isTransitive = false
//        }
        implementation(it)
    }

    implementation(project(":xplat")) { isTransitive = false }
    include(project(":xplat:xplat-lexforge")) { isTransitive = false }

    modImplementation(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) { exclude(module = "fabric-loader") }
    implementation(catalog.kotlin.forge)

    catalog.mixin.constraints.let {
        include(it)
        implementation(it)
    }

    modImplementation(catalog.yacl.forge) {
        exclude(module = "kotlin-stdlib")
        exclude(module = "annotations")
        exclude(module = "gson")
    }

    modImplementation(catalog.kiwi.forge)

    modImplementation(catalog.prism.forge)

    modImplementation(catalog.quark)
    modImplementation(catalog.zeta)

    modImplementation(catalog.bigbrain)

    modImplementation(catalog.bovinesAndButtercups.forge)

    modImplementation("maven.modrinth:supplementaries:1.20-3.1.10-forge")
    modImplementation("maven.modrinth:moonlight:forge_1.20-2.13.48")

    modImplementation("curse.maven:recrafted-creatures-835564:5853646")
    modImplementation("curse.maven:duclib-823186:5258376")

    modImplementation("maven.modrinth:what-are-you-voting-for-2023:1.2.5-forge,1.20.1")

    modImplementation(rootProject.files("libs/taniwha-forge-1.20.0-5.4.9.jar"))

    modImplementation("maven.modrinth:weapon-master:1.4.3-1.20.1")

    modImplementation("maven.modrinth:dynamictrees:1.20.1-1.4.1")

    modImplementation("fuzs.puzzleslib:puzzleslib-forge:8.1.25")

    modImplementation("maven.modrinth:alexs-cloud-storage:1.4.0")
    modRuntimeOnly("maven.modrinth:citadel:2.6.1")
}

tasks {
    jar {
        from(loom.accessWidenerPath)
    }

    shadowJar {
        from(loom.accessWidenerPath)
        configurations = listOf(project.configurations.shadow.get())

        relocate("kotlinx.coroutines.debug", "shadow.kotlinx.coroutines.debug") {
            exclude("kotlinx.coroutines.debug.internal.*")
        }

        mergeServiceFiles()
    }

    remapJar {
        inputFile = shadowJar.flatMap { it.archiveFile }
    }
}