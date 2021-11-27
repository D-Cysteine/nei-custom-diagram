import net.minecraftforge.gradle.user.UserExtension

buildscript {
    repositories {
        maven("https://maven.minecraftforge.net") { this.name = "Forge" }
        maven("https://jitpack.io") { this.name = "jitpack.io" }
    }
    dependencies {
        classpath("com.github.GTNewHorizons:ForgeGradle:1.2.4")
    }
}

plugins {
    idea
    java
}

apply(plugin = "forge")

idea {
    module {
        this.isDownloadJavadoc = true
        this.isDownloadSources = true
    }
}

java {
    this.sourceCompatibility = JavaVersion.VERSION_1_8
    this.targetCompatibility = JavaVersion.VERSION_1_8
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
    this.replacements.putAll(
        mapOf(
            Pair("@version@", version)
        )
    )
    this.runDir = "run"
}

val Project.minecraft: UserExtension
    get() = extensions.getByName<UserExtension>("minecraft")

repositories {
    maven("http://chickenbones.net/maven/") { this.name = "ChickenBones" }
    ivy {
        this.name = "gtnh_download_source"
        this.artifactPattern(
            "http://downloads.gtnewhorizons.com/Mods_for_Jenkins/[module]-[revision]-[classifier].[ext]")
    }
    maven("https://jitpack.io") { this.name = "jitpack.io" }
    //maven("https://gregtech.overminddl1.com/") { this.name = "GregTech" }
}

dependencies {
    val autoValueVersion: String by project
    compileOnly("com.google.auto.value:auto-value-annotations:$autoValueVersion")
    annotationProcessor("com.google.auto.value:auto-value:$autoValueVersion")
    compileOnly(fileTree("libs") { this.include("*.jar") })

    val codeChickenCoreVersion: String by project
    val codeChickenLibVersion: String by project
    compile("codechicken:CodeChickenCore:$minecraftVersion-$codeChickenCoreVersion:dev")
    compile("codechicken:CodeChickenLib:$minecraftVersion-$codeChickenLibVersion:dev")

    val gregTech5Version: String by project
    compile("com.github.GTNewHorizons:GT5-Unofficial:$gregTech5Version:dev") {
        this.isTransitive = false
    }
    /*
     * If we need to get this from downloads.gtnewhorizons.com instead:
    compile("gregtech:gregtech:$gregTech5Version:dev") {
        this.isTransitive = false
    }
     */

    // The following are compile-time dependencies of GT5.
    val enderIoVersion: String by project
    val forestryVersion: String by project
    val railcraftVersion: String by project
    compileOnly("crazypants.enderio:EnderIO-$minecraftVersion:${enderIoVersion}_beta:dev")
    compileOnly("net.sengir.forestry:forestry_$minecraftVersion:$forestryVersion:dev")
    compileOnly("mods.railcraft:Railcraft_$minecraftVersion:$railcraftVersion:dev")

    val bartworksVersion: String by project
    compile("com.github.GTNewHorizons:bartworks:$bartworksVersion:dev") {
        this.isTransitive = false
    }

    val enderStorageVersion: String by project
    compile("com.github.GTNewHorizons:EnderStorage:$enderStorageVersion:dev")

    /*
    val gregTech6Version: String by project
    compile("com.gregoriust.gregtech:gregtech_$minecraftVersion:$gregTech6Version:dev")
    */
}

tasks.withType<Jar> {
    // Make sure this task is re-run when versions change.
    this.inputs.properties += "version" to project.version
    this.inputs.properties += "mcversion" to project.minecraft.version

    // Replace version in mcmod.info
    this.filesMatching("mcmod.info") {
        this.expand(
            mapOf(
                "version" to project.version,
                "mcversion" to project.minecraft.version
            )
        )
    }

    this.archiveBaseName.set("NEICustomDiagram")
}

val sourcesJar by tasks.creating(Jar::class) {
    this.from(sourceSets.main.get().allSource)
    this.archiveClassifier.set("sources")
}

val devJar by tasks.creating(Jar::class) {
    this.from(sourceSets.main.get().output)
    this.archiveClassifier.set("dev")
}

artifacts {
    this.archives(sourcesJar)
    this.archives(devJar)
}
