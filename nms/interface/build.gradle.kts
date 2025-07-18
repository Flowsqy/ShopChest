plugins {
    id("buildlogic.java-spigot-conventions")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.epiceric"
            artifactId = "shopchest-nms-interface"
            version = "1.0.0"

            from(components["java"])
        }
    }
}

