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
    include(project(":xplat:xplat-lexforge")) { isTransitive = false }

    minecraftLibraries(catalog.sinytra.connector)
    modImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
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

    runtimeOnly(project(":lexforge:lexforge-core"))
}

loom {
    mods {
        create(id) {
            sourceSet("main")
            sourceSet("main", project(":lexforge:lexforge-core"))
        }
    }

    accessWidenerPath = project(":xplat").file("src/main/resources/$id.accesswidener")

    forge {
        convertAccessWideners = true

        mixinConfig("$id.forge.mixins.json")
    }
// TODO https://github.com/architectury/architectury-loom/issues/242
//    mixin {
//        defaultRefmapName = "$id.forge.refmap.json"
//    }
}

tasks {
    jar {
        from(loom.accessWidenerPath)

        rename("$id-${project.name}-${project.path.substring(1).replace(':', '_')}-refmap.json", "$id.forge.refmap.json")
    }
}