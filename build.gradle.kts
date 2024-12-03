plugins {
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

version = property("mod.version").toString()

publishMods {
    changelog = providers.environmentVariable("CHANGELOG")
    type = BETA
    displayName = "[${providers.environmentVariable("MC_VERSION")}] AI-NPC $version"
    modLoaders.add("fabric")

    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = providers.environmentVariable("GITHUB_REPOSITORY")
        commitish = "main"
        tagName = "v$version"

        allowEmptyFiles = true
    }
    discord {
        webhookUrl = providers.environmentVariable("DISCORD_WEBHOOK")
    }
}
