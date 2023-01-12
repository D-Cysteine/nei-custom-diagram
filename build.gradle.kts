import net.minecraftforge.gradle.user.UserExtension

buildscript {
    repositories {
        maven("https://maven.minecraftforge.net") { name = "Forge" }
        maven("http://jenkins.usrv.eu:8081/nexus/content/groups/public/") { name = "GTNH Maven" }
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:1.2.13")
    }
}

plugins {
    idea
    java
}

apply(plugin = "forge")

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

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val neiCustomDiagramVersion: String by project
group = "com.github.dcysteine.neicustomdiagram"
version = neiCustomDiagramVersion

val minecraftVersion: String by project
val forgeVersion: String by project
minecraft.version = "$minecraftVersion-$forgeVersion-$minecraftVersion"

configure<UserExtension> {
    replacements.putAll(
        mapOf(
            Pair("@version@", version)
        )
    )
    runDir = "run"
}

val Project.minecraft: UserExtension
    get() = extensions.getByName<UserExtension>("minecraft")

repositories {
    maven("https://maven.minecraftforge.net") {
        name = "Forge"
        metadataSources { artifact() }
    }
    maven("http://jenkins.usrv.eu:8081/nexus/content/groups/public/") { name = "GTNH Maven" }
    maven("https://maven.ic2.player.to") {
        name = "IC2 Maven"
        metadataSources {
            //mavenPom()
            artifact()
        }
    }
    //maven("https://gregtech.overminddl1.com/") { name = "GregTech" }  // GT6
}

dependencies {
    val autoValueVersion: String by project
    compileOnly("com.google.auto.value:auto-value-annotations:$autoValueVersion")
    annotationProcessor("com.google.auto.value:auto-value:$autoValueVersion")

    val neiVersion: String by project
    implementation("com.github.GTNewHorizons:NotEnoughItems:$neiVersion:dev")

    val gregTech5Version: String by project
    implementation("com.github.GTNewHorizons:GT5-Unofficial:$gregTech5Version:dev") {
        isTransitive = false
    }
    // The following are compile-time dependencies of GT5.
    val industrialCraft2Version: String by project
    compileOnly("net.industrial-craft:industrialcraft-2:${industrialCraft2Version}-experimental:api") {
        isTransitive = false
    }
    val forestryVersion: String by project
    compileOnly("com.github.GTNewHorizons:ForestryMC:$forestryVersion:api") {
        isTransitive = false
    }
    val railcraftVersion: String by project
    compileOnly("com.github.GTNewHorizons:Railcraft:$railcraftVersion:api") {
        isTransitive = false
    }
    val buildCraftVersion: String by project
    compileOnly("com.github.GTNewHorizons:BuildCraft:$buildCraftVersion:api") {
        isTransitive = false
    }
    val enderIoVersion: String by project
    compileOnly("com.github.GTNewHorizons:EnderIO:${enderIoVersion}:api") {
        isTransitive = false
    }
    val projectRedVersion: String by project
    compileOnly("com.github.GTNewHorizons:ProjectRed:${projectRedVersion}:dev") {
        isTransitive = false
    }

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
    implementation("com.github.GTNewHorizons:NewHorizonsCoreMod:${gtnhCoreModVersion}:dev") {
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

tasks.withType<Jar> {
    // Replace version in mcmod.info
    filesMatching("mcmod.info") {
        expand(
            mapOf(
                "version" to project.version,
                "mcversion" to project.minecraft.version
            )
        )
    }
    archiveBaseName.set("NEICustomDiagram")
}

val sourcesJar by tasks.creating(Jar::class) {
    from(sourceSets.main.get().allSource)
    from("$projectDir/LICENSE.md")
    archiveClassifier.set("sources")
}

val devJar by tasks.creating(Jar::class) {
    from(sourceSets.main.get().output)
    archiveClassifier.set("dev")
}

artifacts {
    archives(sourcesJar)
    archives(devJar)
}
