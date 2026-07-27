java {
    disableAutoTargetJvm()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation(project(":signs-bukkit-shared"))
    compileOnly("com.aelion.aero:aero-api:${property("aeroApiVersion")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    dependsOn(":signs-bukkit-shared:jar")
    dependsOn(":plugins-common:jar")
    archiveFileName.set("aelion-signs-bukkit-1_8-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
