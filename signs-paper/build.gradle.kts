dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":plugins-common"))
    // Runtime: AelionAero provides com.aelion.aero.api.* (depend in plugin.yml).
    // Prefer vendored API stub jar; fall back to sibling aero-common build output.
    val vendored = file("${rootDir}/libs/aero-common-api.jar")
    val sibling = file("${rootDir}/../aelion-aero/aero-common/build/libs")
        .listFiles()
        ?.filter {
            it.isFile
                    && it.name.startsWith("aero-common-")
                    && it.name.endsWith(".jar")
                    && !it.name.contains("sources")
                    && !it.name.contains("#")
        }
        ?.maxByOrNull { it.lastModified() }
    when {
        vendored.exists() -> compileOnly(files(vendored))
        sibling != null -> compileOnly(files(sibling))
        else -> logger.error("Missing aero API jar. Build aelion-aero and copy aero-common jar to libs/aero-common-api.jar")
    }
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
