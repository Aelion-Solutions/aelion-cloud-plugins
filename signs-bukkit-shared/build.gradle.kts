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
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}
