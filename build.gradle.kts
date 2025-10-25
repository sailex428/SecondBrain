import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.prod.ClientProductionRunTask
import net.fabricmc.loom.task.prod.ServerProductionRunTask
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

version = rootProject.extra["mod.version"] as String
val modVersion = version as String
val mcVersion = property("mc.version").toString()
val fabricLoaderVersion = property("deps.fabric_loader").toString()
val stage = rootProject.extra["deps.stage"].toString()
val modName = rootProject.extra["mod.name"].toString()
val jarName = ("$modName-$mcVersion-v$version-$stage")
val owoLib = properties["owo_version"].toString()
val fabricLangKotlin = properties["fabric_lang_kotlin"].toString()

val stringJavaVersion = if (stonecutter.eval(mcVersion, ">=1.20.6")) "21" else "17"

repositories {
    gradlePluginPortal()
    mavenCentral()
    flatDir { dirs("../../libs") }
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.wispforest.io")
    maven { url = uri("https://jitpack.io") }
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

    if (stonecutter.eval(mcVersion, "=1.20.1")) {
        include(modImplementation("io.wispforest:endec:0.1.12")!!)
        include(modImplementation("io.wispforest.endec:gson:0.1.7")!!)
        include(modImplementation("io.wispforest.endec:netty:0.1.6")!!)
    }

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    include(modImplementation("org.xerial:sqlite-jdbc:3.46.1.3")!!)
    include(modRuntimeOnly("dev_babbaj:nether-pathfinder:1.4.1")!!)

    include(modImplementation("io.github.ollama4j:ollama4j:1.0.97")!!)

    //needed deps for openai communication
    include(modRuntimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")!!)
    include(modImplementation("com.fasterxml.jackson.core:jackson-core:2.18.0")!!)
    include(modImplementation("com.fasterxml.jackson.core:jackson-annotations:2.18.0")!!)
    include(modImplementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")!!)
    include(modRuntimeOnly("com.fasterxml:classmate:1.7.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-generator:4.37.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-module-jackson:4.36.0")!!)
    include(modRuntimeOnly("io.github.sashirestela:slimvalidator:1.2.2")!!)
    include(modRuntimeOnly("io.github.sashirestela:cleverclient:1.4.4")!!)
    include(modImplementation("io.github.sashirestela:simple-openai:3.9.0")!!)

    include(modImplementation("org.apache.httpcomponents:httpcore:4.4")!!)

    include(modImplementation("me.sailex:secondbrainengine:${property("deps.engine")}")!!)
    include(modImplementation("com.github.gnembon:fabric-carpet:${project.property("carpet_version")}")!!)
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../server"
    }
    mixin {
        defaultRefmapName = "$modName-$mcVersion-refmap.json"
    }
}

java {
    withSourcesJar()
    val javaVersion = JavaVersion.toVersion(stringJavaVersion)
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(stringJavaVersion)
    }
    jvmToolchain(stringJavaVersion.toInt())
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
        val dcMessage = changelog.map { "## New version of SecondBrain is out! [$modVersion] \n$it" }.get()
        val shortenedMessage = if (dcMessage.length > 1900) dcMessage.take(1900) + " ...\n\nCheck the full changelog on Github or Modrinth." else dcMessage
        content.set(shortenedMessage)
    }

    modrinth {
        displayName.set("v$modVersion [$mcVersion] SecondBrain")
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        projectId.set(property("publish.modrinth").toString())
        minecraftVersions.add(mcVersion)
    }

    curseforge {
        displayName.set("v$modVersion [$mcVersion] SecondBrain")
        accessToken.set(providers.environmentVariable("CURSEFORGE_API_KEY"))
        projectId.set(property("publish.curseforge").toString())
        minecraftVersions.add(mcVersion)
    }
}

val jarPath = "versions/$mcVersion/build/libs"

tasks.register<ServerProductionRunTask>("prodServer") {
    group = "testing"
    mods.from(file(jarPath))

    installerVersion.set("1.0.1")
    loaderVersion.set(fabricLoaderVersion)

    runDir.set(file("prod-server-run"))
}

tasks.register<ClientProductionRunTask>("prodClient") {
    group = "testing"
    mods.from(file(jarPath))
    // Use XVFB on Linux CI for headless client runs (optional)
    useXVFB.set(false)
    runDir.set(file("prod-client-run"))
}
