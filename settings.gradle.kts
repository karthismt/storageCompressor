pluginManagement {
    repositories {
        maven {
            url = uri("${System.getenv("ANDROID_HOME") ?: "C:/Users/${System.getProperty("user.name")}/AppData/Local/Android/Sdk"}/extras/m2repository")
        }
        maven {
            url = uri("C:/Program Files/Android/Android Studio/plugins/android/resources/offline-gmaven-stable")
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("C:/Program Files/Android/Android Studio/plugins/android/resources/offline-gmaven-stable")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "StorageDoctor"
include(":app")
