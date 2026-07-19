import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}
stonecutter active "1.20.1"

val modVersion = property("mod.version").toString()

publishMods {
    changelog.set(file("CHANGELOG.md").readText())
    displayName.set("v$modVersion SecondBrain")
    type.set(ReleaseType.ALPHA)
    version.set(modVersion)
    modLoaders.add("fabric")

    github {
        accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
        repository.set(providers.gradleProperty("github.repo"))
        tagName.set("v$modVersion")
        commitish.set("main")
        allowEmptyFiles.set(true)
    }

    discord {
        webhookUrl.set(providers.environmentVariable("DISCORD_WEBHOOK"))
        username.set("Update Bot")
        val dcMessage = changelog.map { "## New version of SecondBrain is out! [$modVersion] \n$it" }.get()
        val shortenedMessage = if (dcMessage.length > 1900) dcMessage.take(1900) + " ...\n\nCheck the full changelog on Github or Modrinth." else dcMessage
        content.set(shortenedMessage)
    }
}
