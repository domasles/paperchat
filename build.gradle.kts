plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("com.gradleup.shadow") version "9.1.0"
}

group = "lt.domax"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.mikesamuel:json-sanitizer:1.2.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
}

tasks.shadowJar {
    archiveFileName.set("paperchat-${version}.jar")

    relocate("okhttp3", "lt.domax.paperchat.libs.okhttp3")
    relocate("com.google.gson", "lt.domax.paperchat.libs.gson")
    relocate("com.mikesamuel", "lt.domax.paperchat.libs.mikesamuel")

    minimize()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
