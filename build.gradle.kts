plugins {
    id("fabric-loom") version "1.8-SNAPSHOT"
    id("maven-publish")
    id("com.diffplug.spotless") version "7.0.0.BETA2"
}

version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

base {
    archivesName.set(project.extra["archives_base_name"] as String)
}

repositories {

    mavenCentral()
    flatDir {
        dirs("libs")
    }
}


dependencies {
    minecraft("com.mojang:minecraft:${project.extra["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.extra["yarn_mappings"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.extra["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fabric_version"]}")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    modImplementation("cabaletta:baritone-api:1.10.2")

    implementation("io.github.sashirestela:simple-openai:3.9.0") {
        exclude(group = "org.slf4j")
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.extra["minecraft_version"])
    inputs.property("loader_version", project.extra["loader_version"])
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
                "version" to project.version,
                "minecraft_version" to project.extra["minecraft_version"],
                "loader_version" to project.extra["loader_version"]
        )
    }
}

val targetJavaVersion = 21

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

val archivesBaseName: String by project

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archivesBaseName}" }
    }
    from(fileTree("libs") { include("baritone-api-*.jar") })
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archivesBaseName
            from(components["java"])
        }
    }
    repositories {
    }
}

spotless {
    java {
        palantirJavaFormat()
        indentWithTabs()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}