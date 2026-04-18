plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1"

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublishMod", stonecutter.chiseled) {
    group = "publishing"
    ofTask("publishMods")
}
