plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    id("com.modrinth.minotaur") version "2.+"
}
base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}
val log: File = file("changelog.md")
val mcVersions: String by project
val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup
println("## Changelog for Amethyst Core $modVersion \n\n" + log.readText())
repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Ladysnake Libs"
        url = uri("https://ladysnake.jfrog.io/artifactory/mods")
    }

    flatDir {
        dirs("F:\\Documents\\Mod Libraries\\fc\\build\\libs")
    }

    flatDir {
        dirs("F:\\Documents\\Mod Libraries\\gc\\build\\libs")
    }

}
dependencies {
    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    modImplementation("dev.emi:trinkets:3.5.0"){
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation(":gear_core:0.1.0+1.19.3"){
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation(":fzzy_core:0.1.0+1.19.3"){
        exclude("net.fabricmc.fabric-api")
    }

}
tasks {
    val javaVersion = JavaVersion.VERSION_17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }
    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("amethyst-core")
    versionNumber.set(modVersion)
    versionName.set("${base.archivesName.get()}-$modVersion")
    versionType.set("beta")
    uploadFile.set(tasks.remapJar.get())
    gameVersions.addAll(mcVersions.split(","))
    loaders.addAll("fabric","quilt")
    detectLoaders.set(false)
    changelog.set("## Changelog for Amethyst Core $modVersion \n\n" + log.readText())
    dependencies{
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
        required.project("fzzy-core")
        optional.project("trinkets")
    }
    debugMode.set(false)
}