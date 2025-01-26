plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublishMod", stonecutter.chiseled) {
    group = "publishing"
    ofTask("publishMods")
}
