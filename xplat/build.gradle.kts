import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(catalog.plugins.architectury.plugin)
    alias(catalog.plugins.architectury.loom)
}

architectury {
    common("forge", "fabric")
}

dependencies {
    minecraft(catalog.minecraft)
    mappings(loom.officialMojangMappings())

    modImplementation(catalog.fabric.loader)
    modImplementation(catalog.fabric.api)
    modImplementation(catalog.fabric.kotlin)

    implementation(catalog.mixin.constraints)

    modImplementation(catalog.yacl.fabric)

    modImplementation(catalog.kiwi.forge)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}