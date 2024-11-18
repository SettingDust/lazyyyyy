plugins {
    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        mixinConfig("lazyyyyy.mixins.forge.json")
    }
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    implementation(project(":xplat", "namedElements")) { isTransitive = false }

    minecraftLibraries(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    modImplementation(catalog.kotlin.forge)

    implementation(catalog.mixin.constraints)

    modImplementation(catalog.yacl.forge)

    modImplementation(catalog.kiwi.forge)
}