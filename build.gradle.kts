import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import net.fabricmc.loom.task.prod.ServerProductionRunTask
import net.fabricmc.loom.task.prod.ClientProductionRunTask

plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

version = rootProject.extra["mod.version"] as String
val modVersion = version as String
val mcVersion = property("mc.version").toString()
val fabricLoaderVersion = property("deps.fabric_loader").toString()
val stage = rootProject.extra["deps.stage"].toString()
val jarName = ("secondbrain-$mcVersion-v$version-$stage")
val owoLib = properties["owo_version"].toString()
val fabricLangKotlin = properties["fabric_lang_kotlin"].toString()

val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.6")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17

repositories {
    gradlePluginPortal()
    mavenCentral()
    flatDir { dirs("../../libs") }
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.wispforest.io")
}

dependencies {
    configurations.all {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.${property("deps.yarn_build")}:v2")
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    include(modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fapi")}+$mcVersion")!!)
    include(modImplementation("net.fabricmc:fabric-language-kotlin:$fabricLangKotlin")!!)

    include(modImplementation("io.wispforest:owo-lib:$owoLib")!!)

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    include(modImplementation("org.xerial:sqlite-jdbc:3.46.1.3")!!)
    include(modRuntimeOnly("dev_babbaj:nether-pathfinder:1.4.1")!!)

    include(modImplementation("io.github.ollama4j:ollama4j:1.0.97")!!)

    //needed deps for openai communication
    include(modRuntimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")!!)
    include(modRuntimeOnly("com.fasterxml:classmate:1.7.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-generator:4.37.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-module-jackson:4.36.0")!!)
    include(modRuntimeOnly("io.github.sashirestela:slimvalidator:1.2.2")!!)
    include(modRuntimeOnly("io.github.sashirestela:cleverclient:1.4.4")!!)
    include(modImplementation("io.github.sashirestela:simple-openai:3.9.0")!!)

    include(modImplementation("org.apache.httpcomponents:httpcore:4.4")!!)

    include(modImplementation("me.sailex:secondbrainengine:${property("deps.engine")}")!!)
}

tasks.register<ServerProductionRunTask>("prodServer") {
    mods.from(file("build/libs/" + jarName))

    installerVersion.set("1.0.1")
    loaderVersion.set("0.16.10")
    minecraftVersion.set("1.21.1")

    runDir.set(file("prod-server-run"))
}

tasks.register<ClientProductionRunTask>("prodClient") {
    mods.from(file("build/libs/" + jarName))


    runDir.set(file("prod-client-run"))

    // Use XVFB on Linux CI for headless client runs (optional)
    useXVFB.set(true)
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../server"
    }
}

java {
    withSourcesJar()
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

kotlin {
    compilerOptions {
        jvmTarget = if (stonecutter.eval(mcVersion, ">=1.20.6")) JvmTarget.JVM_21 else JvmTarget.JVM_17
    }
    jvmToolchain(21)
}

tasks.jar {
    archiveVersion.set("")
}

tasks.remapJar {
    archiveBaseName.set(jarName)
    archiveVersion.set("")
}

tasks.remapSourcesJar {
    archiveBaseName.set(jarName)
    archiveVersion.set("")
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("mcDep", mcVersion)
    inputs.property("fabricLoader", fabricLoaderVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "mcDep" to mcVersion,
            "fabricLoader" to fabricLoaderVersion
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.property("mod.group").toString()
            artifactId = property("archives_base_name").toString()
            version = "$version+$mcVersion"

            artifact(tasks.remapJar.get().archiveFile)
            artifact(tasks.remapSourcesJar.get().archiveFile) {
                classifier = "sources"
            }
        }
    }
}

publishMods {
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    displayName.set("v$modVersion SecondBrain")
    type.set(ReleaseType.ALPHA)
    version.set(modVersion)
    modLoaders.add("fabric")
    file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        repository.set(providers.gradleProperty("github.repo"))
        tagName.set("v$modVersion")
        commitish.set("main")
    }

    discord {
        webhookUrl.set(providers.environmentVariable("DISCORD_WEBHOOK"))
        username.set("Update Bot")
        avatarUrl.set("https://www.sailex.me/img/sailex_head.png")
        content.set(changelog.map { "## New version of SecondBrain is out! [$modVersion] \n$it" })
    }

    modrinth {
        displayName.set("v$modVersion [$mcVersion] SecondBrain")
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set(property("publish.modrinth").toString())
        minecraftVersions.add(mcVersion)
    }
}


