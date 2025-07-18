plugins {
    id("buildlogic.java-spigot-conventions")
}

repositories {
    // Inventive Talent repo (ReflectionHelper)
    maven {
        url = uri("https://repo.inventivetalent.org/content/groups/public/")
    }    
}

dependencies {
    implementation(project(":nms:interface"))
    implementation("org.inventivetalent:reflectionhelper:1.18.13-SNAPSHOT")
}
