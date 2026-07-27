pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

fun loadDotEnv(file: File): Map<String, String> {
    if (!file.isFile) return emptyMap()
    val out = linkedMapOf<String, String>()
    file.forEachLine { raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) return@forEachLine
        val idx = line.indexOf('=')
        if (idx <= 0) return@forEachLine
        val key = line.substring(0, idx).trim()
        var value = line.substring(idx + 1).trim()
        if ((value.startsWith("\"") && value.endsWith("\""))
            || (value.startsWith("'") && value.endsWith("'"))
        ) {
            value = value.substring(1, value.length - 1)
        }
        out[key] = value
    }
    return out
}

val dotenv = loadDotEnv(settingsDir.resolve(".env"))

fun envOrDot(name: String, fallback: String = ""): String {
    val fromEnv = System.getenv(name)?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) return fromEnv
    return dotenv[name]?.trim().orEmpty().ifEmpty { fallback }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven {
            name = "GitHubPackagesAero"
            url = uri("https://maven.pkg.github.com/Aelion-Solutions/aelion-aero")
            credentials {
                username = envOrDot("GITHUB_ACTOR", "token")
                password = envOrDot("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "aelion-cloud-plugins"

include("plugins-common")
include("npcs-paper")
include("signs-bukkit-shared")
include("signs-bukkit-1_8")
include("signs-bukkit-1_13")
include("signs-paper-1_17")
include("signs-paper-1_21")
include("signs-paper-26")
