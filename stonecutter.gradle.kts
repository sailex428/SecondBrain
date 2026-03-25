plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1"

val allMcVersions = stonecutter.versions.map { it.version }.distinct()

for (version in allMcVersions) {
    tasks.register("publish$version") {
        group = "publishing"
        dependsOn(stonecutter.tasks.named("publishMods")
            { metadata.version == version }
        )
    }
}

tasks.register("publishAllVersions") {
    group = "publishing"
    description = "Publishes all Stonecutter versions in one run."
    dependsOn(allMcVersions.map { "publish$it" })
}
