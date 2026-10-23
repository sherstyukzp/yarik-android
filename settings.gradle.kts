pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "yarik-android"
include(":app")
include(":yarik-mesh")
// Upstream Wear OS companion remains in repo; not wired until shell + mesh API stabilizes.
