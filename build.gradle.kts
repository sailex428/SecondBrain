import me.modmuss50.mpp.ReleaseType

plugins {
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
    id("com.diffplug.spotless") version "7.0.0.BETA4"
}

val modVersion = rootProject.property("mod.version").toString()

repositories {
    gradlePluginPortal()
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

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

    repositories {
        mavenCentral()
    }
}

publishMods {
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    displayName.set("v$modVersion AI NPC")
    type.set(ReleaseType.STABLE)
    version.set(modVersion)

    github {
        accessToken.set(providers.gradleProperty("GITHUB_TOKEN"))
        repository.set(providers.gradleProperty("GITHUB_REPOSITORY"))
        tagName.set("v$modVersion")
        commitish.set("main")

        allowEmptyFiles = true
    }

    discord {
        webhookUrl.set(providers.gradleProperty("DISCORD_WEBHOOK"))
        username.set("Update Bot")
        avatarUrl.set("https://www.sailex.me/img/sailex_head.png")
        content.set(changelog.map { "## New version of AI-NPC is out! [1.0.9] \n$it" })
        setPlatformsAllFrom(project(":ai-npc-client"))
    }
}
