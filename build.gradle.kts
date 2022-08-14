import dev.architectury.pack200.java.Pack200Adapter
/* Uncomment for shade compatibility with other dependencies.
import net.fabricmc.loom.task.RemapJarTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
*/

plugins {
    kotlin("jvm") version ("1.6.21")
    id("dev.architectury.architectury-pack200") version ("0.1.3")
    // id("com.github.johnrengelman.shadow") version ("7.1.+") Uncomment for shade compatibility with other dependencies.
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
            // Add arguments here, ex arg("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
        }
    }

    runConfigs {
        getByName("client") { isIdeConfigGenerated = true }
    }

    forge { pack200Provider.set(Pack200Adapter()) }
}

repositories {
    mavenCentral()
}

/* Uncomment for shade compatibility with other dependencies.
val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)
*/

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
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

    /* Uncomment for shade compatibility with other dependencies.
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
    */

    named<Jar>("jar") {
        archiveBaseName.set(projectName)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes(
                    "ModSide" to "CLIENT",
                    "ForceloadAsMod" to true,
                    "TweakOrder" to "0",
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