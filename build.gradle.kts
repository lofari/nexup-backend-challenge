plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.nexup"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
