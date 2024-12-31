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

    compileOnly("org.sinytra:sponge-mixin:0.12.11+mixin.0.8.5")
}