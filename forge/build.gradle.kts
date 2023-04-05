plugins {
    id("cobblemon.platform-conventions")
    id("cobblemon.publish-conventions")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    forge {
        convertAccessWideners.set(true)
        mixinConfig("mixins.cobblemon-forge.json")
        mixinConfig("mixins.cobblemon-common.json")
    }
}

repositories {
    maven(url = "${rootProject.projectDir}/deps")
    maven(url = "https://thedarkcolour.github.io/KotlinForForge/")
    mavenLocal()
}

dependencies {
    forge(libs.forge)
    modApi(libs.architecturyForge)
//    modApi(libs.kotlinForForge)

    //shadowCommon group: 'commons-io', name: 'commons-io', version: '2.6'

    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    implementation(libs.kotlinForForge)
    "developmentForge"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionForge")) {
        isTransitive = false
    }
    testImplementation(project(":common", configuration = "namedElements"))

    listOf(
        libs.graal,
        libs.molang
    ).forEach {
        forgeRuntimeLibrary(it)
        bundle(it)
    }

    listOf(
        libs.stdlib,
        libs.serializationCore,
        libs.serializationJson,
        libs.reflect
    ).forEach(::forgeRuntimeLibrary)
}

tasks {
    shadowJar {
        exclude("architectury.common.json")
        exclude("architectury-common.accessWidener")

        relocate ("com.ibm.icu", "com.cobblemon.mod.relocations.ibm.icu")
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