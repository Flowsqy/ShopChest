plugins {
    java
}

// Here we trick dependency resolution.
// Although we only include class compile with java 21+,
// the said class will only be used if needed
// As the server jar enforce the java version, it's pretty safe
tasks.withType<JavaCompile> {
    options.release = 17
}

tasks.named<Jar>("jar") {
    val moduleJarTasks = subprojects.map({it.tasks.jar})
    dependsOn(moduleJarTasks)
    from({
        moduleJarTasks.map({zipTree{it.get().archiveFile}})
    })
}
