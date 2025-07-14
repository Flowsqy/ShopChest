plugins {
    id("buildlogic.java-library-conventions")
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

repositories {
    mavenLocal {
        content {
            includeGroup("de.epiceric")
        }
    }
    // Jitpack (Vault, uSkyBlock, GriefPrevention, PlotSquared v4, Towny)
    maven {
        url = uri("https://jitpack.io")
    }
    // CodeMc repo (AuthMe, ASkyBlock, BentoBox, WorldGuardWrapper)
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    // EngineHub repo (WorldEdit, WorldGuard from WorldGuardWrapper)
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
    // Paper Repo (Adventure-MiniMessage from PlotSquared v6)
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    // Inventive Talent repo (ReflectionHelper)
    maven {
        url = uri("https://repo.inventivetalent.org/content/groups/public/")
    }    
}

val localDependencies = 
tasks.shadowJar {
    dependencies {
        include(project(":nms:interface"))
        include(project(":nms:reflection"))
        include(dependency("de.epiceric:shopchest-nms-spigot-all"))
    }
}

dependencies {
    // Nms modules
    implementation(project(":nms:interface"))
    implementation(project(":nms:reflection"))
    implementation("de.epiceric:shopchest-nms-spigot-all:1.0.4")
    // Shaded api
    implementation("org.inventivetalent:reflectionhelper:1.18.13-SNAPSHOT")
    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.2.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    implementation("com.zaxxer:HikariCP:6.3.0")
    // Used api
    implementation("com.github.MilkBowl:VaultAPI:1.7")
    // Optionnal plugin compatibility
    implementation("fr.xephi:authme:5.4.0")
    implementation("com.plotsquared:PlotSquared-Core:6.5.0")
    implementation("com.sk89q.worldedit:worldedit-core:7.3.0")
    implementation("com.github.rlf.uSkyBlock:uSkyBlock-API:v2.8.9")
    implementation("com.wasteofplastic:askyblock:3.0.9.4")
    implementation("com.github.TechFortress:GriefPrevention:16.17.1")
    implementation("world.bentobox:bentobox:1.17.2-SNAPSHOT")
    implementation("com.github.IntellectualSites.PlotSquared:Core:4.453")
    implementation("com.github.TownyAdvanced:Towny:0.97.5.0")
    // Add libs that does not belong to any valid maven repository
    // Using implementation makes shadow to include them in the final jar.
    // Local dependencies are not handled well and can't be excluded (see https://github.com/GradleUp/shadow/issues/142)
    compileOnly(files("../lib/AreaShop-2.6.0.jar", "../lib/IslandWorld-8.5.jar"))
}
