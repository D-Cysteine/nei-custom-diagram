plugins {
    idea
    java
    id("com.gtnewhorizons.retrofuturagradle") version "1.3.35"
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val neiCustomDiagramVersion: String by project
group = "com.github.dcysteine.neicustomdiagram"
version = neiCustomDiagramVersion

val minecraftVersion: String by project
minecraft {
    mcVersion.set(minecraftVersion)

    injectedTags.put("VERSION", project.version)
}

repositories {
    maven("https://nexus.gtnewhorizons.com/repository/public/") {
        name = "GTNH Maven"
    }
    maven("https://cursemaven.com") { name = "Curse Maven" }
    /*
     * This repo seems to be broken...
    maven("https://maven.ic2.player.to") {
        name = "IC2 Maven"
        metadataSources {
            mavenPom()
            artifact()
        }
        content {
            includeGroup("net.industrial-craft")
        }
    }
     */
    maven("https://maven2.ic2.player.to") {
        name = "IC2 Maven2"
        metadataSources {
            mavenPom()
            artifact()
        }
        content {
            includeGroup("net.industrial-craft")
        }
    }
    //maven("https://gregtech.overminddl1.com/") { name = "GregTech" }  // GT6
}

dependencies {
    val autoValueVersion: String by project
    compileOnly("com.google.auto.value:auto-value-annotations:$autoValueVersion")
    annotationProcessor("com.google.auto.value:auto-value:$autoValueVersion")

    val neiVersion: String by project
    implementation("com.github.GTNewHorizons:NotEnoughItems:$neiVersion:dev") {}

    val gregTech5Version: String by project
    implementation("com.github.GTNewHorizons:GT5-Unofficial:$gregTech5Version:dev")

    val bartworksVersion: String by project
    implementation("com.github.GTNewHorizons:bartworks:$bartworksVersion:dev") {
        isTransitive = false
    }

    val gtPlusPlusVersion: String by project
    implementation("com.github.GTNewHorizons:GTplusplus:$gtPlusPlusVersion") {
        isTransitive = false
    }

    val detravScannerVersion: String by project
    implementation("com.github.GTNewHorizons:DetravScannerMod:$detravScannerVersion:dev") {
        isTransitive = false
    }

    val gtnhCoreModVersion: String by project
    implementation("com.github.GTNewHorizons:NewHorizonsCoreMod:$gtnhCoreModVersion:dev") {
        isTransitive = false
    }

    /*
    val gregTech6Version: String by project
    implementation("com.gregoriust.gregtech:gregtech_$minecraftVersion:$gregTech6Version:dev")
     */

    val enderStorageVersion: String by project
    implementation("com.github.GTNewHorizons:EnderStorage:$enderStorageVersion:dev") {
        isTransitive = false
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    // Replace version in mcmod.info
    filesMatching("mcmod.info") {
        expand(
            mapOf(
                "version" to project.version,
                "mcversion" to minecraftVersion,
            )
        )
    }
    archiveBaseName.set("NEICustomDiagram")
}

tasks.injectTags.configure {
    outputClassName.set("${project.group}.Tags")
}

val sourcesJar by tasks.creating(Jar::class) {
    from(sourceSets.main.get().allSource)
    from("$projectDir/LICENSE.md")
    archiveClassifier.set("sources")
}

artifacts {
    archives(sourcesJar)
}
