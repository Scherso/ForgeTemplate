import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.architectury.pack200.java.Pack200Adapter
import net.fabricmc.loom.task.RemapJarTask

plugins {
    kotlin("jvm") version ("1.6.21")
    id("dev.architectury.architectury-pack200") version ("0.1.3")
    id("com.github.johnrengelman.shadow") version ("7.1.+")
    id("gg.essential.loom") version ("0.10.0.+")
    id("net.kyori.blossom") version ("1.3.0")
    java
}

val projectName: String by project
val projectId: String by project
val projectVersion: String by project
val projectGroup: String by project
val mcVersion: String = property("minecraft.version")?.toString() ?: throw IllegalStateException("minecraft.version is not set...")

version = projectVersion
group = projectGroup

blossom {
    replaceToken("@VERSION@", projectVersion)
    replaceToken("@NAME@", projectName)
    replaceToken("@ID@", projectId)
}

loom {
    silentMojangMappingsLicense()
    launchConfigs {
        getByName("client") {
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            arg("--mixin", "mixins.${projectId}.json")
        }
    }

    runConfigs {
        getByName("client") {
            isIdeConfigGenerated = true
        }
    }

    forge {
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.${projectId}.json")
        mixin.defaultRefmapName.set("mixins.${projectId}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shade(libs.mixin)
}

tasks {
    processResources {
        inputs.property("version", projectVersion)
        inputs.property("mcversion", mcVersion)
        inputs.property("name", projectName)
        inputs.property("id", projectId)

        filesMatching("mcmod.info") {
            expand(
                    "id" to projectId,
                    "name" to projectName,
                    "version" to projectVersion,
                    "mcversion" to mcVersion
            )
        }

        filesMatching("mixins.${projectId}.json") {
            expand(
                    "id" to projectId
            )
        }
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    named<RemapJarTask>("remapJar") {
        archiveBaseName.set(projectName)
        input.set(shadowJar.get().archiveFile)
    }

    named<Jar>("jar") {
        archiveBaseName.set(projectName)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes(
                    "ModSide" to "CLIENT",
                    "ForceloadAsMod" to true,
                    "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                    "TweakOrder" to "0",
                    "MixinConfigs" to "mixins.${projectId}.json"
            )
        }
    }
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}