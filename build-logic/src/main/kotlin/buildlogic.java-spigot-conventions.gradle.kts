plugins {
    id("buildlogic.java-library-conventions")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }    
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
}
