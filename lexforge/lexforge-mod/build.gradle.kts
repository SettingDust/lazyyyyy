plugins {
    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)

    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
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

    implementation(catalog.mixinextras.common)

    implementation(project(":xplat", "namedElements")) { isTransitive = false }
    include(project(":xplat", "transformProductionForge")) { isTransitive = false }

    minecraftLibraries(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    implementation(catalog.kotlin.forge)

    implementation(catalog.mixin.constraints)

    modImplementation(catalog.yacl.forge) {
        exclude(module = "kotlin-stdlib")
        exclude(module = "annotations")
        exclude(module = "gson")
    }

    modImplementation(catalog.kiwi.forge)

    runtimeOnly(project(":lexforge:lexforge-core"))
}

loom {
    mods {
        create(id) {
            sourceSet("main")
            sourceSet("main", project(":lexforge:lexforge-core"))
        }
    }

    forge {
        mixinConfig("$id.mixins.json", "$id.mixins.forge.json")
    }
}