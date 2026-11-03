plugins {
    application
}

group = "sshterminal"
version = "1.0.0"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

repositories {
    mavenCentral()
    // Fallback: JetBrains' own repository also hosts JediTerm
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
}

dependencies {
    implementation("com.github.mwiede:jsch:2.28.3")
    implementation("org.jetbrains.jediterm:jediterm-core:3.62")
    implementation("org.jetbrains.jediterm:jediterm-ui:3.62")
    implementation("com.formdev:flatlaf:3.6")
}

application {
    mainClass = "sshterminal.SshTerminal"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
