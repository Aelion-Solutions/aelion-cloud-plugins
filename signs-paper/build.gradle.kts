dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":plugins-common"))
    // Compile against published Aero API; runtime types come from AelionAero (depend).
    compileOnly("com.aelion.aero:aero-api:${property("aeroApiVersion")}")
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    dependsOn(configurations.runtimeClasspath)
    dependsOn(":plugins-common:jar")
    // Must match aelion-cloud catalog assetPrefixes.signs paper: aelion-signs-
    archiveFileName.set("aelion-signs-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
