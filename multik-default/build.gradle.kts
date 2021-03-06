dependencies {
    api(project(":multik-api"))
    implementation(project(":multik-jvm"))
    implementation(project(":multik-native"))
}

tasks.jar {
    manifest {
        attributes(
            mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version)
        )
    }
}
