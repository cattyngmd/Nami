# 2bEssentials

**2bEssentials** is a modular and lightweight anarchy client base built build for PVE and automatization.

## Why

Most commonly used Minecraft clients are paid, closed-source, and obfuscated. This makes them difficult to audit or trust and in some cases, they may include backdoors or malicious code.

This project was started as a clean, open-source alternative. It aims to be transparent, secure, and easy to extend, without relying on potentially unsafe third-party clients.

## Requirements

- Java 17 (because of Minecraft 1.20.4 and Fabric)
- Gradle 8+ (wrapper included)

## How to Build

### 1. Clone the repository

```bash
git clone https://github.com/Kiriyaga7615/2bEssentials.git
cd 2bEssentials

```

### 2. Build Gradle
``` bash
Using the Gradle wrapper:
./gradlew build

On Windows:
gradlew.bat build

The compiled JAR will be located in:
build/libs/2bEssentials-<version>.jar
```

## License
This project is licensed under the MIT License. You are free to contribute, distribute, fork or skid anything you will.