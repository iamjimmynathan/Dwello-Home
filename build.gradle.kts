// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

gradle.projectsEvaluated {
  val appProject = findProject(":app")
  val testTask = appProject?.tasks?.findByName("testDebugUnitTest")
  val assembleTask = appProject?.tasks?.findByName("assembleDebug")
  if (testTask != null && assembleTask != null) {
    assembleTask.mustRunAfter(testTask)
  }
}

tasks.register("buildVerifiedLatestApk") {
  group = "build"
  description = "Runs application tests, builds a fresh debug APK, and copies it to DwelloHomes-latest.apk in the project root."
  dependsOn(":app:testDebugUnitTest", ":app:assembleDebug")

  val sourceApk = layout.projectDirectory.file("app/build/outputs/apk/debug/app-debug.apk").asFile
  val targetApk = layout.projectDirectory.file("DwelloHomes-latest.apk").asFile

  doLast {
    if (!sourceApk.exists() || sourceApk.length() <= 0L) {
      throw GradleException("Fresh debug APK missing or empty at: ${sourceApk.absolutePath}")
    }
    sourceApk.copyTo(targetApk, overwrite = true)
    if (!targetApk.exists() || targetApk.length() <= 0L) {
      throw GradleException("Failed to copy verified APK to: ${targetApk.absolutePath}")
    }
    println("DWELLO_APK_PUBLISHED=${targetApk.absolutePath} sizeBytes=${targetApk.length()}")
  }
}

