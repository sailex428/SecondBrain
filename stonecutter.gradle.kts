plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1"

for (version in stonecutter.versions.map { it.version }.distinct()) {
    tasks.register("publish$version") {
        group = "publishing"
        dependsOn(stonecutter.tasks.named("publishMods")
            { metadata.version == version }
        )
    }
}
