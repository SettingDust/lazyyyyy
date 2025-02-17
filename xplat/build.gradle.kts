import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(catalog.plugins.kotlin.jvm)
    alias(catalog.plugins.kotlin.plugin.serialization)

    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

val id: String by rootProject.properties

subprojects {
    base { archivesName.set("${rootProject.base.archivesName.get()}-${project.name}") }
}

architectury {
    common("forge", "fabric")
}

dependencies {
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    implementation(catalog.mixinsquared.common)
    annotationProcessor(catalog.mixinsquared.common)

    modImplementation(catalog.fabric.loader)
    modImplementation(catalog.fabric.api)
    modImplementation(catalog.fabric.kotlin)

    implementation(catalog.mixin.constraints)

    modImplementation(catalog.yacl.fabric)

    modImplementation(catalog.entity.features.sound.fabric)
    modImplementation(catalog.entity.features.model.fabric)
    modImplementation(catalog.entity.features.texture.fabric)

    modImplementation(catalog.moremcmeta.fabric)

    modImplementation(catalog.toomanyplayers)

    modImplementation(catalog.axiom)

    modImplementation(catalog.modernfix.fabric)

    modImplementation(catalog.fusion.fabric)

    modImplementation(catalog.continuity.fabric)

    implementation(catalog.caffeine)
    implementation(catalog.caffeine.coroutines)

    modImplementation(catalog.almostUnified.fabric)

    modImplementation(catalog.badOptimizations)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jar {
        manifest {
            attributes(
                "MixinConfigs" to "$id.mixins.json",
                "FMLModType" to "GAMELIBRARY"
            )
        }
    }
}

loom {
    accessWidenerPath = file("src/main/resources/$id.accesswidener")

    mixin {
        defaultRefmapName = "$id.refmap.json"
    }
}