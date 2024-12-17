plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
    kotlin("jvm") version "2.1.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.1" apply false
}
stonecutter active "1.20.4" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) { 
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
