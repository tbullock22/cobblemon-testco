plugins {
    id("cobblemon.platform-conventions")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        convertAccessWideners.set(true)
        mixinConfig("mixins.cobblemon-common.json")
    }
}

repositories {
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
}

dependencies {
    forge(libs.forge)
    modApi(libs.architecturyForge)

    //shadowCommon group: 'commons-io', name: 'commons-io', version: '2.6'

    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    "developmentForge"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionForge")) {
        isTransitive = false
    }
    testImplementation(project(":common", configuration = "namedElements"))

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
        forgeRuntimeLibrary(it)
        bundle(it) {
            exclude("com.ibm.icu", "icu4j")
        }
    }
}

tasks {
    shadowJar {
        exclude("architectury-common.accessWidener")
    }
    processResources {
        inputs.property("version", rootProject.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to rootProject.version)
        }
    }
}

//jar {
//    classifier("dev")
//    manifest {
//        attributes(
//                "Specification-Title" to rootProject.mod_id,
//                "Specification-Vendor" to "Cable MC",
//                "Specification-Version" to "1",
//                "Implementation-Title" to rootProject.mod_id,
//                "Implementation-Version" to project.version,
//                "Implementation-Vendor" to "Cable MC",
//        )
//    }
//}
//
//sourcesJar {
//    def commonSources = project(":common").sourcesJar
//    dependsOn commonSources
//    from commonSources.archiveFile.map { zipTree(it) }
//}
//
//components.java {
//    withVariantsFromConfiguration(project.configurations.shadowRuntimeElements) {
//        skip()
//    }
//}