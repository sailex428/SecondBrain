import me.modmuss50.mpp.ReleaseType

plugins {
    id("me.modmuss50.mod-publish-plugin") version "0.8.1"
}

val modVersion = rootProject.property("mod.version").toString()

repositories {
    gradlePluginPortal()
}

publishMods {
    changelog.set(rootProject.file("CHANGELOG.md").readText())
    displayName.set("v$modVersion")
    type.set(ReleaseType.BETA)
    version.set(modVersion)

    github {
        accessToken.set(providers.gradleProperty("GITHUB_TOKEN"))
        repository.set(providers.gradleProperty("GITHUB_REPOSITORY"))
        tagName.set("v$modVersion")
        commitish.set("main")

        allowEmptyFiles = true
    }
}
