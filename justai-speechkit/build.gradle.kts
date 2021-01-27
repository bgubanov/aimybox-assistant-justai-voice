val aimyboxVersion: String by rootProject.extra

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
//    id("com.google.protobuf")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions {
        isCheckAllWarnings = true
        isWarningsAsErrors = false
        isAbortOnError = true
    }
}

dependencies {
    implementation("com.justai.aimybox:core:$aimyboxVersion")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.1")
    implementation("com.squareup.okhttp3:okhttp" version { okHttp })

    implementation("com.squareup.okhttp3:logging-interceptor" version { okHttp })
    implementation("io.grpc:grpc-okhttp" version { grpc })
    implementation("io.grpc:grpc-protobuf-lite" version { grpc })
    implementation("io.grpc:grpc-stub" version { grpc })

    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

/*protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.9.1"
    }
    plugins {
        id("javalite") { artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0" }
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.24.0" }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc") { option("lite") }
                id("javalite")
            }
        }
    }
}*/
