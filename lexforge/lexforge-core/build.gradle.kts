plugins {
    alias(catalog.plugins.shadow)

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

    compileOnly("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7")

    implementation(project(":xplat", "namedElements")) { isTransitive = false }

    implementation(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    implementation(catalog.kotlin.forge)

    include(project(":lexforge:lexforge-mod"))
    implementation(project(":lexforge:lexforge-mc-bootstrap"))

    shadow("net.bytebuddy:byte-buddy-agent:1.17.1") {
        isTransitive = false
    }
}

tasks {
    jar {
        val mcBootstrapJar = project(":lexforge:lexforge-mc-bootstrap").tasks.jar.flatMap { it.archiveFile }
        val bootstrapJar = project(":lexforge:lexforge-bootstrap").tasks.jar.flatMap { it.archiveFile }
        from(mcBootstrapJar, bootstrapJar)
        doFirst {
            rename(mcBootstrapJar.get().asFile.name, "lazyyyyy-lexforge-mc-bootstrap.jar")
            rename(bootstrapJar.get().asFile.name, "lazyyyyy-lexforge-bootstrap.jar")
        }

        manifest {
            attributes(
                "FMLModType" to "LIBRARY"
            )
        }
    }

    shadowJar {
        archiveClassifier = "dev-shadow"
        configurations = listOf(project.configurations.shadow.get())
        destinationDirectory = jar.flatMap { it.destinationDirectory }

        // FIXME Workaround for https://github.com/GradleUp/shadow/issues/111
        from(jar)

        manifest {
            attributes(
                "FMLModType" to "LIBRARY"
            )
        }
    }

    remapJar {
        inputFile = shadowJar.flatMap { it.archiveFile }
    }
}