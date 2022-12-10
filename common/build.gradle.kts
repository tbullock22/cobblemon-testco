plugins {
    id("cobblemon.base-conventions")
    id("maven-publish")
}

architectury {
    common()
}

repositories {
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
}

dependencies {
    implementation(libs.stdlib)
    implementation(libs.reflect)

    modImplementation(libs.fabricLoader)
    modApi(libs.architectury)
    modApi(libs.molang)
    modApi(libs.mclib)

    // For Showdown
    modCompileOnly(libs.javet) // Linux or Windows
    modCompileOnly(libs.javetMac) // Mac OS (x86_64 Only)

    //shadowCommon group: 'commons-io', name: 'commons-io', version: '2.6'

    testRuntimeOnly(libs.junitEngine)
    testImplementation(libs.junitApi)
    testImplementation(libs.junitParams)
    testImplementation(libs.mockito)
    testImplementation(libs.mockk)

    compileOnly("net.luckperms:api:${rootProject.property("luckperms_version")}")

    // For Showdown
//    modCompileOnly 'com.caoccao.javet:javet:1.0.6' // Linux or Windows
//    modCompileOnly 'com.caoccao.javet:javet-macos:1.0.6' // Mac OS (x86_64 Only)
//    modCompileOnly group: 'commons-io', name: 'commons-io', version: '2.6'
}

publishing {
    repositories {
        maven("https://maven.impactdev.net/repository/development/") {
            name = "ImpactDev-Public"
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PW")
            }
        }
    }

    publications {
        create<MavenPublication>("mod") {
            from(components["java"])
            groupId = "com.cobblemon"
            artifactId = "mod"
            version = rootProject.version.toString()
        }
    }
}