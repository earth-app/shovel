# shovel

> Kotlin Multiplatform Scraping Framework

## Introduction

`shovel` is a Kotlin Multiplatform framework for scraming web applications. 
It is used in The Earth App to retrieve, standardize, and relay public information and websites.

## Installation

Maven (JVM)
```xml
<dependencies>
    <dependency>
        <groupId>com.earth-app.shovel</groupId>
        <artifactId>shovel-jvm</artifactId>
        <version>[VERSION]</version> <!-- Replace with latest version -->
    </dependency>
</dependencies>
```

Gradle (Groovy, JVM)
```groovy
dependencies {
    implementation "com.earth-app.shovel:shovel-jvm:[VERSION]" // Replace with latest version
}
```

Gradle (Kotlin DSL, Multiplatform)
```kotlin
kotlin {
    jvm()
    js()
    linuxX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.earth-app.shovel:shovel:[VERSION]") // Replace with latest version
            }
        }
    }
}
```

## Contributing

Contributions are welcome. Please fork this repository and submit a pull request for any changes you would like to make.