plugins {
    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

architectury {
    platformSetupLoomIde()
    forge()
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    implementation(project(":xplat", "namedElements")) { isTransitive = false }
    include(project(":xplat", "transformProductionForge")) { isTransitive = false }

    minecraftLibraries(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    modImplementation(catalog.kotlin.forge)
}