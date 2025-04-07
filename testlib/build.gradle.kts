plugins {
	`java-library`
	id("io.spring.dependency-management")
	id("banka4.code-style-conventions")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}


group = "rs.banka4"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

configurations {
	/* Prevent accidentally using JUnit 4 (dependency of Testcontainers).  */
	testCompileClasspath {
		exclude(group = "junit", module = "junit")
		exclude(
			group = "org.junit.vintage",
			module = "junit-vintage-engine"
		)
	}
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
}

dependencies {
	implementation(platform("org.junit:junit-bom:5.10.0"))
	api("org.junit.jupiter:junit-jupiter")

	api("org.testcontainers:postgresql:1.19.8")
	api("org.testcontainers:junit-jupiter:1.20.6")

	implementation("org.flywaydb:flyway-core")
	implementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.springframework.boot:spring-boot-testcontainers")
}
