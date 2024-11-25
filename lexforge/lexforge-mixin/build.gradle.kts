plugins {
    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

architectury {
    forge()
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    compileOnly("org.sinytra:sponge-mixin:0.12.9+")

    implementation(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    implementation(catalog.kotlin.forge)
}