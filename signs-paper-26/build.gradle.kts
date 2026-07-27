java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.84-stable")
    implementation(project(":signs-bukkit-shared"))
    compileOnly("com.aelion.aero:aero-api:${property("aeroApiVersion")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
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
    archiveFileName.set("aelion-signs-paper-26-${project.version}.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
