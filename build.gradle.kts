import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.1"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.22"
	kotlin("kapt") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"
	kotlin("plugin.allopen") version "1.8.22"
	kotlin("plugin.noarg") version "1.8.22"
}

group = "com.trift.backend"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

allOpen {
	annotation("javax.persistence.Entity")
}

noArg {
	annotation("javax.persistence.Entity")
}

repositories {
	mavenCentral()
}

val JJWT_RELEASE_VERSION = "0.11.5"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

	implementation("io.hypersistence:hypersistence-utils-hibernate-62:3.5.1")
	implementation("com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations:2.15.2")

	implementation("io.jsonwebtoken:jjwt-api:${JJWT_RELEASE_VERSION}")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:${JJWT_RELEASE_VERSION}")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:${JJWT_RELEASE_VERSION}")

	// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-afterburner
	implementation("com.fasterxml.jackson.module:jackson-module-afterburner:2.15.2")
	implementation("ru.oleg-cherednik.jackson:jackson-utils:2.6")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// https://mvnrepository.com/artifact/org.springframework.data/spring-data-redis
	implementation("org.springframework.data:spring-data-redis:3.1.5")
	// https://mvnrepository.com/artifact/com.github.fppt/jedis-mock
	implementation("com.github.fppt:jedis-mock:1.0.10")

	// https://mvnrepository.com/artifact/io.lettuce/lettuce-core
	implementation("io.lettuce:lettuce-core:6.2.6.RELEASE")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.postgresql:postgresql:42.6.0")
	runtimeOnly("org.postgresql:postgresql")

	implementation("dev.akkinoc.spring.boot:logback-access-spring-boot-starter:4.0.0")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")

	implementation("io.sentry:sentry-spring-boot-starter-jakarta:6.27.0")
	implementation("io.sentry:sentry-logback:6.27.0")

	implementation("org.springframework.boot:spring-boot-starter-actuator")

	implementation("com.bucket4j:bucket4j_jdk8-core:8.6.0")
	implementation("com.bucket4j:bucket4j-redis:8.6.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.rest-assured:rest-assured:5.3.0")
	testImplementation("io.rest-assured:kotlin-extensions:5.3.0")

	testImplementation(platform("org.junit:junit-bom:5.9.0"))
	testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly ("org.junit.vintage:junit-vintage-engine:5.9.0")

	testImplementation("io.mockk:mockk:1.13.5")

	implementation("de.grundid.opendatalab:geojson-jackson:1.14")
}

tasks.withType<Test> {
	testLogging {
		showExceptions = true
		showStackTraces = true
		showCauses = true
		exceptionFormat = TestExceptionFormat.FULL
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}
