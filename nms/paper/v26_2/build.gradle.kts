plugins {
    id("buildlogic.java-library-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:interface")) {
        exclude(module = "spigot-api")
    }
    paperweight.paperDevBundle("26.2.build.+")
}

tasks.withType<JavaCompile> {
    options.release = 25
}

