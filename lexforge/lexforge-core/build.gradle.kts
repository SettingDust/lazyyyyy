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

    compileOnly("net.fabricmc:sponge-mixin:0.15.5+")

    implementation(project(":xplat", "namedElements")) { isTransitive = false }

    implementation(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    implementation(catalog.kotlin.forge)

    include(project(":lexforge:lexforge-mod"))
    implementation(project(":lexforge:lexforge-mixin"))
}

tasks {
    jar {
        val mixinJar = project(":lexforge:lexforge-mixin").tasks.jar.flatMap { it.archiveFile }
        from(mixinJar)
        doFirst { rename(mixinJar.get().asFile.name, "lazyyyyy-lexforge-mixin.jar") }

        manifest {
            attributes(
                "FMLModType" to "LIBRARY"
            )
        }
    }
}