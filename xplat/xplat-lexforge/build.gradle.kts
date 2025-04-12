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
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    implementation(catalog.kotlinx.coroutines.debug)

    implementation(project(":xplat"))

    implementation(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    implementation(catalog.kotlin.forge)

    implementation(catalog.mixin.constraints)
    implementation(catalog.mixinextras.common)
    annotationProcessor(catalog.mixinextras.common)

    implementation(catalog.mixinsquared.common)
    implementation(catalog.mixinsquared.lexforge)
    annotationProcessor(catalog.mixinsquared.common)

    modImplementation(catalog.yacl.forge)

    modImplementation(catalog.entity.features.sound.forge)
    modImplementation(catalog.entity.features.model.forge)
    modImplementation(catalog.entity.features.texture.forge)

    modImplementation(catalog.moremcmeta.forge)

    implementation(catalog.toomanyplayers)

    implementation(catalog.axiom)

    modImplementation(catalog.modernfix.forge)

    modImplementation(catalog.fusion.forge)

    modImplementation("curse.maven:catalogue-459701:4766090")

    modImplementation(catalog.continuity.forge)

    modImplementation(catalog.almostUnified.forge)

    modImplementation(catalog.badOptimizations)

    implementation(catalog.reflect)

    implementation(catalog.hash4j)

    modImplementation(catalog.moonlight.forge)
    modImplementation(catalog.everyCompat.forge)

    modImplementation(catalog.simplySwords.forge)
}

sourceSets {
    main {
        java.srcDir(project(":xplat").sourceSets.main.get().java)
        kotlin.srcDir(project(":xplat").sourceSets.main.get().kotlin)
        resources.srcDir(project(":xplat").sourceSets.main.get().resources)
    }
}

tasks {
    jar {
        manifest {
            attributes(
                "MixinConfigs" to "$id.mixins.json",
                "FMLModType" to "GAMELIBRARY"
            )
        }
    }

    shadowJar {
        from(loom.accessWidenerPath)
        configurations = listOf(project.configurations.shadow.get())

        manifest {
            attributes(
                "MixinConfigs" to "$id.mixins.json",
                "FMLModType" to "GAMELIBRARY"
            )
        }

//        relocate("kotlinx.coroutines.debug", "shadow.kotlinx.coroutines.debug")

        mergeServiceFiles()
    }

    remapJar {
        inputFile = shadowJar.flatMap { it.archiveFile }
    }
}

loom {
    accessWidenerPath = project(":xplat").file("src/main/resources/$id.accesswidener")

    mixin {
        defaultRefmapName = "$id.refmap.json"
    }
}