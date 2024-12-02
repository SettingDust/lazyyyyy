plugins {
    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)

    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

val id: String by rootProject.properties

architectury {
    forge()
}

dependencies {
    forge(catalog.lexforge)
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

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

        rename("$id-${project.name}-${project.path.substring(1).replace(':', '_')}-refmap.json", "$id.refmap.json")
    }
}

loom {
    accessWidenerPath = project(":xplat").file("src/main/resources/$id.accesswidener")
}