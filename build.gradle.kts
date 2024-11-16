import groovy.lang.Closure
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    alias(catalog.plugins.kotlin.jvm)

    alias(catalog.plugins.git.version)

    alias(catalog.plugins.unmined)
}

val archive_name: String by rootProject.properties
val id: String by rootProject.properties
val name: String by rootProject.properties
val author: String by rootProject.properties
val description: String by rootProject.properties
val source: String by rootProject.properties

group = "settingdust.lazyyyyy"

val gitVersion: Closure<String> by extra
version = gitVersion()

base {
    archivesName = archive_name
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

repositories {
    unimined.curseMaven()
    unimined.modrinthMaven()

    maven("https://maven.su5ed.dev/releases") {
        content {
            includeGroupAndSubgroups("dev.su5ed")
            includeGroupAndSubgroups("org.sinytra")
        }
    }

    maven("https://thedarkcolour.github.io/KotlinForForge/") {
        content { includeGroup("thedarkcolour") }
    }
}

sourceSets {
    create("fabric")
    create("lexforge")
}

val mainImplementation by configurations.creating
val fabricImplementation by configurations.getting {
    isCanBeResolved = true
}
val lexforgeImplementation by configurations.getting {
    isCanBeResolved = true
}

unimined.minecraft {
    version(catalog.versions.minecraft.get())

    mappings {
        intermediary()
        mojmap()
        parchment(version = "2023.09.03")

        devFallbackNamespace("official")
    }

    if (sourceSet == sourceSets.main.get()) {
        fabric {
            loader(catalog.versions.fabric.loader.get())
        }

        runs {
            off = true
        }

        defaultRemapJar = false
    }
}

unimined.minecraft(sourceSets.getByName("lexforge")) {
    combineWith(sourceSets.main.get())

    minecraftForge {
        mixinConfig("$id.mixins.json")
        loader(catalog.versions.lexforge.get())
    }

    defaultRemapJar = false
    createJarTask = false

    runs {
        all {
            jvmArgs("-Dmixin.env.disableRefMap=true")
            systemProperties["mixin.env.disableRefMap"] = true
        }
    }
}

unimined.minecraft(sourceSets.getByName("fabric")) {
    combineWith(sourceSets.main.get())

    fabric {
        loader(catalog.versions.fabric.loader.get())
    }
}

val minecraftLibraries by configurations.getting

val include by configurations.getting
val modImplementation by configurations.getting
val fabricModImplementation by configurations.getting
val lexforgeModImplementation by configurations.getting

val lexforgeRuntimeOnly by configurations.getting
val lexforgeMinecraftLibraries by configurations.getting

dependencies {
    mainImplementation(catalog.mixin)
    mainImplementation(catalog.mixinextras.common)

    unimined.fabricApi.fabric(catalog.versions.fabric.api.get()).let {
        modImplementation(it)
        fabricModImplementation(it)
    }

    catalog.fabric.kotlin.let {
        modImplementation(it)
        fabricModImplementation(it)
    }

    lexforgeMinecraftLibraries(catalog.sinytra.connector)
    lexforgeModImplementation(catalog.forgified.fabric.api) {
        exclude(module = "fabric-loader")
    }
    lexforgeModImplementation(catalog.kotlin.forge)

    "com.moulberry:mixinconstraints:1.0.1".let {
        include(implementation(it)!!)
        fabricImplementation(it)
        lexforgeImplementation(it)
    }

    "maven.modrinth:yacl:3.6.1+1.20.1-fabric".let {
        modImplementation(it)
        fabricModImplementation(it)
        modImplementation("maven.modrinth:flow:2.1.2+1.20.1")
        modImplementation("maven.modrinth:mru:1.0.4+1.20.1-fabric")
    }
}

tasks {
    withType<ProcessResources> {
        val properties = mapOf(
            "id" to id,
            "version" to rootProject.version,
            "group" to rootProject.group,
            "name" to rootProject.name,
            "description" to rootProject.property("description").toString(),
            "author" to rootProject.property("author").toString(),
            "source" to rootProject.property("source").toString(),
            "fabric_loader" to ">=0.15",
            "minecraft" to ">=1.20.1",
            "fabric_kotlin" to "*"
        )
        from(rootProject.sourceSets.main.get().resources)
        inputs.properties(properties)

        filesMatching(
            listOf(
                "fabric.mod.json",
                "META-INF/neoforge.mods.toml",
                "META-INF/mods.toml",
                "*.mixins.json",
                "META-INF/MANIFEST.MF"
            )
        ) {
            expand(properties)
        }
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    named<Jar>("sourcesJar") {
        from(sourceSets.map { it.allSource })

        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    jar {
        archiveClassifier = "mojmap"
    }

    named<Jar>("fabricJar") {
        duplicatesStrategy = DuplicatesStrategy.WARN

        archiveClassifier = "dev"
    }

    named<Jar>("remapFabricJar") {
        archiveClassifier = ""
    }
}