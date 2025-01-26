import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    kotlin("jvm") version "2.1.0"
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
    id("com.diffplug.spotless") version "7.0.0.BETA4"
}

version =  rootProject.extra["mod.version"] as String
val mcVersion = property("mc.version").toString()
val fabricLoaderVersion = property("deps.fabric_loader").toString()
val stage = rootProject.extra["deps.stage"].toString()
val jarName = ("second-brain-$mcVersion-v$version-$stage").toString()
val automatone = rootProject.extra["deps.automatone"].toString()

val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.6")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17

repositories {
    gradlePluginPortal()
    mavenCentral()
    flatDir { dirs("../../libs") }
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    maven { url = uri("https://maven.ladysnake.org/releases") }
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
    include(modImplementation("net.fabricmc:fabric-language-kotlin:1.13.0+kotlin.2.1.0")!!)

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    include(modImplementation("org.xerial:sqlite-jdbc:3.46.1.3")!!)
    include(modRuntimeOnly("dev_babbaj:nether-pathfinder:1.4.1")!!)

    include(modImplementation("io.github.ollama4j:ollama4j:1.0.90-with-json-schema")!!)

    //needed deps for openai communication
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-core:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-annotations:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml.jackson.core:jackson-databind:2.18.1")!!)
    include(modRuntimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")!!)
    include(modRuntimeOnly("com.fasterxml:classmate:1.7.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-generator:4.37.0")!!)
    include(modRuntimeOnly("com.github.victools:jsonschema-module-jackson:4.36.0")!!)
    include(modRuntimeOnly("io.github.sashirestela:slimvalidator:1.2.2")!!)
    include(modRuntimeOnly("io.github.sashirestela:cleverclient:1.4.4")!!)
    include(modImplementation("io.github.sashirestela:simple-openai:3.9.0")!!)

    modImplementation("io.github.ladysnake:automatone:${property("deps.automatone")}")
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-base:${property("cca_version")}")
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-entity:${property("cca_version")}")
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-world:${property("cca_version")}")
    include(modRuntimeOnly("com.github.gnembon:fabric-carpet:${property("carpet_version")}")!!)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("org.mockito:mockito-core:5.14.2")
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
    inputs.property("loader_version", fabricLoaderVersion)
    inputs.property("automatone", automatone)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to version,
            "mcDep" to mcVersion,
            "loader_version" to fabricLoaderVersion,
            "automatone" to automatone
        )
    }
}

spotless {
    java {
        target("**/*.java")
        targetExclude("**/build/**")
        palantirJavaFormat()
        indentWithTabs()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        endWithNewline()
        trimTrailingWhitespace()
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
    displayName.set("v$version SecondBrain")
    type.set(ReleaseType.ALPHA)
    version.set(version)
    modLoaders.add("fabric")

    github {
        file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)
        accessToken.set(providers.gradleProperty("GITHUB_TOKEN"))
        repository.set(providers.gradleProperty("GITHUB_REPOSITORY"))
        tagName.set("v$version")
        commitish.set("main")
    }

    discord {
        webhookUrl.set(providers.gradleProperty("DISCORD_WEBHOOK"))
        username.set("Update Bot")
        avatarUrl.set("https://www.sailex.me/img/sailex_head.png")
        content.set(changelog.map { "## New version of SecondBrain (AI-NPC) is out! [2.0.0] \n$it" })
    }

    modrinth {
        displayName.set("v$version [$mcVersion] SecondBrain")
        accessToken.set(providers.gradleProperty("MODRINTH_TOKEN"))
        projectId.set(property("publish.modrinth").toString())
        minecraftVersions.add(mcVersion)
        requires("fabric-api")
    }
}


