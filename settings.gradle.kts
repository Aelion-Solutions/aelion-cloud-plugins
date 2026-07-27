pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven {
            name = "GitHubPackagesAero"
            url = uri("https://maven.pkg.github.com/Aelion-Solutions/aelion-aero")
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("gpr.user"))
                    .orElse("token")
                    .get()
                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("gpr.key"))
                    .orElse("")
                    .get()
            }
        }
    }
}

rootProject.name = "aelion-cloud-plugins"

include("plugins-common")
include("npcs-paper")
include("signs-paper")
