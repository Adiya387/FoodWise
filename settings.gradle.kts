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
        maven("https://jitpack.io") // 🔧 Добавлено для MPAndroidChart и других GitHub-библиотек
    }
}

rootProject.name = "HealthyFoodAI"
include(":app")
