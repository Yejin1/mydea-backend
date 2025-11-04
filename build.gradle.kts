plugins {
	java
	id("org.springframework.boot") version "3.4.9"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mydea"
version = "0.0.1-SNAPSHOT"
description = "Mydea API"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")
	implementation("com.azure:azure-storage-blob:12.27.0")

	runtimeOnly("com.mysql:mysql-connector-j")

	implementation("org.springframework.boot:spring-boot-starter-security")

	implementation ("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.11.5")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
