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
val versions = arrayOf("v1_21_7")
for (version in versions) {
    include("nms:paper:${version}")
}
