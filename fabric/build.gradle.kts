plugins {
    id("cobblemon.platform-conventions")
    id("cobblemon.publish-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val generatedResources = file("src/generated/resources")

sourceSets {
    main {
        resources {
            srcDir(generatedResources)
        }
    }
}

repositories {
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://api.modrinth.com/maven")
}

dependencies {
    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "developmentFabric"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    modApi(libs.fabricApi)
    modApi(libs.fabricKotlin)
    modApi(libs.architecturyFabric)
    modApi(libs.fabricPermissionsApi)
    modCompileOnly(libs.adornFabric)

    listOf(
        libs.stdlib,
        libs.reflect,
        libs.jetbrainsAnnotations,
        libs.serializationCore,
        libs.serializationJson,
        libs.graal,
        libs.molang,
        libs.mclib
    ).forEach {
        include(modImplementation(it.get())!!)
    }
    // graal seems to need the different dependencies to be manually imported here
    include("org.graalvm.regex:regex:${libs.graal.get().version}")
    include("org.graalvm.sdk:graal-sdk:${libs.graal.get().version}")
    include("org.graalvm.truffle:truffle-api:${libs.graal.get().version}")
}

tasks {
    // The AW file is needed in :fabric project resources when the game is run.
    val copyAccessWidener by registering(Copy::class) {
        from(loom.accessWidenerPath)
        into(generatedResources)
    }

    processResources {
        dependsOn(copyAccessWidener)
        inputs.property("version", rootProject.version)

        filesMatching("fabric.mod.json") {
            expand("version" to rootProject.version)
        }
    }
}