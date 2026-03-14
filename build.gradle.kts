plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.detekt)
	alias(libs.plugins.ktlint)
	java
}

buildscript {
	dependencies {
		classpath(libs.kotlin.gradle)
	}
}

java {
	toolchain {
		languageVersion.set(
			JavaLanguageVersion.of(
				libs.versions.java.jdk
					.get(),
			),
		)
	}
}

detekt {
	buildUponDefaultConfig = true
	ignoreFailures = false
	config.setFrom(files("$rootDir/detekt.yaml"))
	basePath = rootDir.absolutePath
	parallel = true

	source.setFrom(
		fileTree(projectDir) {
			include("**/*.kt", "**/*.kts")
		},
	)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
	reports {
		sarif.required.set(true)
	}
}

ktlint {
	android.set(true)
}

subprojects {
	apply(plugin = "org.jlleitschuh.gradle.ktlint")

	configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
		android.set(true)
	}

	tasks.matching { it.name.contains("KotlinScript") }.configureEach {
		enabled = false
	}
}

tasks.withType<Test> {
	// Ensure Junit emits the full stack trace when a unit test fails through gradle
	useJUnit()

	testLogging {
		events(
			org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
			org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR,
			org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
		)
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true
	}
}

tasks.register("checkAll") {
	group = "verification"
	description = "Ktlint + Detekt + Lint — run before push"
	dependsOn("ktlintCheck", "detekt", ":app:lint")
}
