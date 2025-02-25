plugins {
    id("java")
    id("war")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.2")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Jackson (для работы с JSON)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.0")

    // JWT (JSON Web Token)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Hibernate Validator (валидация данных)
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // MapStruct (для маппинга DTO)
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // OpenAPI & Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // File Upload
    implementation("commons-fileupload:commons-fileupload:1.4")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.11")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.11.0")
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}