plugins {
    id("buildlogic.java-library-conventions")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
}

dependencies {
    implementation(project(":nms:interface")) {
        exclude(module = "spigot-api")
    }
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks.withType<JavaCompile> {
    options.release = 21
}

