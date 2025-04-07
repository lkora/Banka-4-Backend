plugins {
	java
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("banka4.test-conventions")
	id("banka4.code-style-conventions")
	id("banka4.migration-generator")
}

group = "rs.banka4"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
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
	implementation(project(":common"))

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:4.10.0")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-jcache:4.10.0")
	implementation("com.google.guava:guava:30.1-jre")
	implementation("org.springframework.amqp:spring-amqp:3.2.3")
	implementation("org.springframework.amqp:spring-rabbit:3.2.3")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.5")
	implementation("org.springdoc:springdoc-openapi-starter-common:2.8.5")
	implementation("dev.samstevens.totp:totp-spring-boot-starter:1.7.1")

	runtimeOnly("javax.cache:cache-api:1.1.1")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	testAnnotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Local Variables:
// mode: prog
// indent-tabs-mode: t
// End:
