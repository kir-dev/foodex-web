plugins {
	kotlin("jvm") version "2.4.20"
	kotlin("plugin.spring") version "2.4.20"
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "2.4.20"
}

group = "hu.kirdev"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

tasks {
    bootJar {
        archiveFileName.set("foodex.jar")
    }
}

// The migration utility also has a `main`, so name the application's entry
// point explicitly; otherwise bootJar/bootRun see two candidate main classes.
springBoot {
    mainClass.set("hu.kirdev.foodex.FoodExApplicationKt")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.mockk:mockk:1.14.11")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootBuildImage>("bootBuildImage") {
    builder = "paketobuildpacks/builder-jammy-tiny:latest"

    environment = mapOf(
        "BP_JVM_VERSION" to "25",
        "LC_ALL" to "en_US.UTF-8",
        "BPE_LC_ALL" to "en_US.UTF-8",
    )
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// One-off companion for moving an existing H2 production database to Postgres.
// The backend image contains this class, but it never runs during normal startup.
tasks.register<JavaExec>("migrateH2ToPostgres") {
	group = "migration"
	description = "Copies every FoodEx table from H2 (H2_URL) to Postgres (POSTGRES_URL)."
	classpath = sourceSets.main.get().runtimeClasspath
	mainClass.set("hu.kirdev.foodex.migration.H2ToPostgresKt")
}