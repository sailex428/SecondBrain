import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("fabric-loom") version "1.8-SNAPSHOT"
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

var modVersion = rootProject.property("mod.version").toString()
var mcVersion = property("mc.version").toString()
var fabricLoaderVersion = property("deps.fabric_loader").toString()
var jarName = ("ai-npc-$mcVersion-v$modVersion-fabric-beta").toString()

repositories {
    flatDir {
        dirs("libs")
        dirs("../../libs")
    }
}

configurations.all {
    resolutionStrategy {
        force("org.slf4j:slf4j-api:2.0.16")
        force("org.apache.logging.log4j:log4j-core:2.19.0")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.${property("deps.yarn_build")}:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    include(modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fapi")}+$mcVersion")!!)

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    include(modImplementation("org.xerial:sqlite-jdbc:3.46.1.3")!!)
    include(modImplementation("cabaletta:baritone-api-fabric:${property("deps.baritone")}")!!)
    include(modRuntimeOnly("dev_babbaj:nether-pathfinder-1.4.1")!!)

    include(modImplementation("io.github.ollama4j:ollama4j:1.0.89")!!)

    //needed deps for openai communication
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-core:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-annotations:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-databind:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml:classmate:1.7.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-generator:4.36.0")!!)

    include(modRuntimeOnly("com.github.victools:jsonschema-module-jackson:4.36.0")!!)
    include(modRuntimeOnly("io.github.sashirestela:slimvalidator:1.2.2")!!)
    include(modRuntimeOnly("io.github.sashirestela:cleverclient:1.4.4")!!)

    include(modImplementation("io.github.sashirestela:simple-openai:3.9.0")!!)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("org.mockito:mockito-core:5.14.2")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../../client"
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(mcVersion, ">=1.20.6")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.remapJar {
    archiveBaseName.set(jarName)
}

tasks.remapSourcesJar {
    archiveBaseName.set(jarName)
}

tasks.processResources {
    inputs.property("version", modVersion)
    inputs.property("mcDep", mcVersion)
    inputs.property("loader_version", fabricLoaderVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "mcDep" to mcVersion,
            "loader_version" to fabricLoaderVersion
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.property("mod.group").toString()
            artifactId = property("archives_base_name").toString()
            version = "$modVersion+$mcVersion"

            artifact(tasks.remapJar.get().archiveFile)
            artifact(tasks.remapSourcesJar.get().archiveFile) {
                classifier = "sources"
            }
        }
    }
}

publishMods {
    file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)
    changelog.set(providers.environmentVariable("CHANGELOG"))
    type.set(ReleaseType.STABLE)
    displayName.set("$modVersion - [$mcVersion] AI-NPC-Client")
    modLoaders.add("fabric")

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        repository.set(providers.environmentVariable("GITHUB_REPOSITORY"))
        commitish.set("main")
        tagName.set("v$modVersion-$mcVersion")
    }
}