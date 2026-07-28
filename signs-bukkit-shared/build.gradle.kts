plugins {
    `java-library`
}

// aero-api is published with Java 21 toolchain metadata but bytecode release 8.
java {
    disableAutoTargetJvm()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    api(project(":plugins-common"))
    compileOnly("com.aelion.aero:aero-api:${property("aeroApiVersion")}")

    testImplementation("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    testImplementation("com.aelion.aero:aero-api:${property("aeroApiVersion")}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

tasks.test {
    useJUnitPlatform()
}
