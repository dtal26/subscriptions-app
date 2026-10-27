plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp) // генерация кода для Room
}

android {
    namespace = "com.example.subscriptions"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.subscriptions"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Обфускация и оптимизация кода R8 + удаление неиспользуемых ресурсов.
            // В AGP 9.3+ блок optimization заменяет isMinifyEnabled и isShrinkResources.
            optimization {
                enable = true
            }
            // Учебный проект: подписываем release-сборку отладочным ключом,
            // чтобы её можно было установить на эмулятор
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- добавлено ---
    // ViewModel (MVVM)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Навигация
    implementation(libs.androidx.navigation.compose)
    // База данных Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // DataStore (сессия пользователя, настройки)
    implementation(libs.androidx.datastore.preferences)
    // Иконки Material
    implementation(libs.androidx.compose.material.icons.extended)
    // Сеть: Retrofit + Gson (курсы валют ЦБ РФ)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    // Фоновые задачи: ежедневная проверка напоминаний
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}