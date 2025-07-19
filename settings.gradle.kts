pluginManagement {
    // Include 'plugins build' to define convention plugins.
    includeBuild("build-logic")
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "ShopChest"
include("plugin")
include("nms:interface", "nms:reflection")
// Include all paper nms module
val paperNmsVersions = arrayOf("v1_21_7")
val paperNmsProjects = paperNmsVersions.map({"nms:paper:${it}"})
include("nms:paper")
include(paperNmsProjects)
