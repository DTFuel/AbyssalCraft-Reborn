plugins {
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
}

val loader = property("loom.platform").toString()            // forge / neoforge (from node props)
val mcVersion = property("vers.mcVersion").toString()
val modId = property("mod.id").toString()                    // captured here: inside run-config lambdas `property(..)` binds to RunConfigSettings, not Project
val configuredRunDir = providers.gradleProperty("abyssalcraft.runDir").orNull

group = property("mod.group").toString()
version = "${property("mod.version")}+$mcVersion"
base.archivesName = "${property("mod.id")}-$loader"

val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.6")) 21 else 17

loom {
    silentMojangMappingsLicense()
    mixin {
        useLegacyMixinAp = true
        defaultRefmapName = "${property("mod.id")}.refmap.json"
    }

    // Forge dev MUST register the mixin config here (Loom won't read mods.toml [[mixins]]).
    if (loader == "forge") {
        forge { mixinConfig("${property("mod.id")}.mixins.json") }
    }

    // Datagen run (`runData`): data() picks the loader-correct datagen main; output to src/main/generated.
    runConfigs.create("data") {
        data()
        programArgs("--all", "--mod", modId)
        programArgs("--output", rootProject.file("src/main/generated").absolutePath)
        programArgs("--existing", rootProject.file("src/main/resources").absolutePath)
    }

    runConfigs.all {
        runDir(configuredRunDir ?: "../../run")              // runners may request an isolated game directory
        if (stonecutter.current.isActive) {
            ideConfigGenerated(true)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.blamejared.com")            // JEI (PP-5 optional integration)
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")   // GeckoLib (bone-model rendering)
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    if (loader == "neoforge") {
        "neoForge"("net.neoforged:neoforge:${property("vers.deps.fml")}")
    } else {
        "forge"("net.minecraftforge:forge:$mcVersion-${property("vers.deps.fml")}")
    }

    val terraBlender = property("vers.deps.terrablender").toString()
    "modImplementation"("com.github.glitchfiend:TerraBlender-$loader:$mcVersion-$terraBlender")

    // Patchouli owns the Necronomicon presentation. The five custom book items remain AbyssalCraft
    // items because they also store PE and execute server-authoritative sneak-use actions.
    val patchouli = property("vers.deps.patchouli").toString()
    "modImplementation"("vazkii.patchouli:Patchouli:$patchouli")

    // JEI (PP-5): compile against the API only (modCompileOnly); load the full jar just in dev runs
    // (modLocalRuntime). Optional at ship time -- integration/jei is @JeiPlugin-gated, so absent JEI
    // simply means the plugin never loads. Coordinates differ per loader / mc version.
    val jei = property("vers.deps.jei").toString()
    "modCompileOnly"("mezz.jei:jei-$mcVersion-common-api:$jei")
    if (loader == "neoforge") {
        "modCompileOnly"("mezz.jei:jei-$mcVersion-neoforge-api:$jei")
        "modLocalRuntime"("mezz.jei:jei-$mcVersion-neoforge:$jei")
    } else {
        "modCompileOnly"("mezz.jei:jei-$mcVersion-forge-api:$jei")
        "modLocalRuntime"("mezz.jei:jei-$mcVersion-forge:$jei")
    }

    // GeckoLib: bone-model rendering for faithful arbitrary-rotation 1.12.2 models (see
    // docs/spec/geckolib-model-porting.md). No common artifact for 1.20.1, so per-loader builds.
    if (loader == "neoforge") {
        // 4.9.x internalised its math into software.bernie.geckolib.loading.math (no external mclib).
        "modImplementation"("software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.2")
    } else {
        "modImplementation"("software.bernie.geckolib:geckolib-forge-1.20.1:4.8.4")
        // 4.8.x depends on com.eliotlash.mclib (math). GeckoLib JarJars it for production, but in a
        // classpath (dev) launch Forge's JarInJar locator skips it, so it never reaches GeckoLib's
        // transforming classloader. forgeRuntimeLibrary puts the plain lib on the dev runtime classpath.
        "forgeRuntimeLibrary"("com.eliotlash.mclib:mclib:20")
    }
}

tasks {
    val rrCompatAudit by registering(Exec::class) {
        group = "verification"
        description = "Audits loader-import boundaries and the reachable platform fork closure."
        workingDir(rootProject.projectDir)
        commandLine("node", "scripts/audit_compat.js")
    }

    val rrRestrictedAssetPlaceholderAudit by registering(Exec::class) {
        group = "verification"
        description = "Audits license-safe placeholders for restricted legacy textures, models, and sounds."
        workingDir(rootProject.projectDir)
        commandLine("node", "scripts/replace_restricted_assets.js", "--check")
    }

    val rrNecronomiconTitleAudit by registering(Exec::class) {
        group = "verification"
        description = "Audits localized Patchouli entry titles for missing or duplicate display text."
        workingDir(rootProject.projectDir)
        commandLine("node", "scripts/audit_necronomicon_titles.js")
    }

    register<Exec>("releaseAudit") {
        group = "verification"
        description = "Runs read-only R8/PV-3 release audits against existing production JARs."
        workingDir(rootProject.projectDir)
        commandLine("node", "scripts/run_release_audit.js")
    }

    named("check") {
        dependsOn(rrCompatAudit)
        dependsOn(rrRestrictedAssetPlaceholderAudit)
        dependsOn(rrNecronomiconTitleAudit)
    }

    // CRITICAL: build from Stonecutter-preprocessed sources, not raw sources.
    // Loom 1.11 registers createMinecraftArtifacts lazily, so hook via configureEach.
    configureEach {
        if (name == "createMinecraftArtifacts") dependsOn("stonecutterGenerate")
    }

    processResources {
        exclude(".cache/**")
        exclude("data/abyssalcraft/recipe/*_recycling.json")
        exclude("data/abyssalcraft/recipes/*_recycling.json")
        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "group" to project.property("mod.group"),
            "authors" to project.property("mod.authors"),
            "description" to project.property("mod.description"),
            "license" to project.property("mod.license"),
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) { expand(props) }
        filesMatching(listOf(
            "assets/abyssalcraft/models/block/*.json",
            "assets/abyssalcraft/models/block/statue/*.json",
            "assets/abyssalcraft/models/item/*.json",
        )) {
            filter { line -> line.replace("__LOADER__", loader) }
        }
        if (loader == "forge") exclude("META-INF/neoforge.mods.toml")
        else exclude("META-INF/mods.toml")
        // Binary structure templates use the last remaining datapack directory fork:
        // 1.20.1 reads structures/, while 1.21.1 reads structure/. Keep only the active node's copy.
        if (loader == "forge") exclude("data/abyssalcraft/structure/**/*.nbt")
        else exclude("data/abyssalcraft/structures/**/*.nbt")
    }

    withType<JavaCompile> { options.release = javaVersion }
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

// Datagen output (`runData` writes here) must be on the resource path so generated blockstates /
// models / item-models / loot / tags ship in the jar and bake at runtime. Loader-agnostic JSON is
// shared by both nodes, exactly like src/main/resources. (CR-12: wiring the PA-4 datagen framework's
// output into resources -- previously the generated dir was produced but never consumed.)
sourceSets {
    main {
        resources.srcDir(rootProject.file("src/main/generated"))
    }
}
