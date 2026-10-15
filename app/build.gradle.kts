import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firma de release: se lee de keystore.properties en la raíz, que no se versiona.
// El formato está en keystore.properties.example.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}
val keystoreRequiredKeys = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")

// null cuando la firma de release está lista; si no, el motivo, que se muestra al compilar release
val releaseSigningProblem: String? = when {
    !keystorePropertiesFile.exists() ->
        "Falta keystore.properties en la raíz del proyecto, así que el build de release no se puede firmar. " +
            "Copia keystore.properties.example a keystore.properties y completa los datos de tu keystore. " +
            "El build de release nunca se firma con la clave de debug."
    else -> {
        val missing = keystoreRequiredKeys.filter { keystoreProperties.getProperty(it).isNullOrBlank() }
        if (missing.isNotEmpty()) {
            "keystore.properties no define: ${missing.joinToString()}."
        } else if (!rootProject.file(keystoreProperties.getProperty("storeFile")).exists()) {
            "keystore.properties apunta a un storeFile que no existe: ${keystoreProperties.getProperty("storeFile")}."
        } else {
            null
        }
    }
}

android {
    namespace = "com.segundoaire"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.segundoaire"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        // MigrationTestHelper lee los esquemas exportados como assets de los tests instrumentados
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    signingConfigs {
        if (releaseSigningProblem == null) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Sin firma de release no hay respaldo: signingConfig queda vacío y el build se detiene (abajo)
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

// Falla antes de compilar nada, con un mensaje claro, en lugar de firmar con debug o dejar el APK sin firma
gradle.taskGraph.whenReady {
    val releasePackaging = Regex("(assemble|bundle|package|install|validateSigning)Release")
    val needsSigning = allTasks.any { it.project == project && releasePackaging.matches(it.name) }
    if (needsSigning && releaseSigningProblem != null) {
        throw GradleException(releaseSigningProblem)
    }
}

ksp {
    // Esquemas de Room versionados en git: base de las migraciones y de sus tests
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.haze)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.room.testing)
}