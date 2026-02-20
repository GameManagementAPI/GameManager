<p align="center">
  <a href="https://gma.c4vxl.de/">
    <img src="https://cdn.c4vxl.de/GameManagementAPI/logo_subtract.png" alt="Logo">
  </a>
</p>

<h2 align="center">GMA - GameManagementAPI</h2>

<p align="center">A powerful and flexible game infrastructure library for Minecraft plugin developers.</p>
<br>

<p align="center">
  <img src="https://img.shields.io/badge/Programming%20Language:-Kotlin-blue" alt="Kotlin Badge" />
  <img src="https://img.shields.io/badge/Type:-Library-red" alt="Type Badge" />
  <img src="https://img.shields.io/badge/Topic:-Minecraft-orange" alt="Topic Badge" />
</p>

---

<br>

### Table of contents (TOC)
- [About GMA](#what-is-gma)
    - [How it works](#how-gma-works)
    - [Why GMA?](#why-gma)
- [Where to find What](#where-to-find-what)
- [Installation](#installation)
    - [Add library](#1-add-library)
    - [Add Plugin](#2-download-plugin)
- [Build yourself](#3-add-as-dependency)
- [Resources](#resources)
- [License](#license)

<br>

# What is GMA?
**GameManagementAPI (GMA)** is a modular infrastructure library designed to simplify the development of game-based Minecraft plugins.

It provides a comprehensive set of systems that handle the repetitive and complex foundation most game plugins require, allowing developers to focus purely on gameplay logic instead of boilerplate infrastructure.

GMA is designed with flexibility, scalability, and clean architecture in mind, making it suitable for both small projects and large multi-game networks.

## How GMA works
GMA acts as a core plugin that runs on your server and exposes an API which can be accessed by other plugins.

It provides prebuilt systems such as player lifecycle management, map loading, game lifecycle control, and localization.

Developers implement the API to define their game logic, while GMA handles the underlying infrastructure.

Maps are automatically loaded from a provided directory and instantiated into isolated worlds, enabling multiple games to run simultaneously on a single server.

## Why GMA?
Developing game plugins often requires rebuilding the same core systems repeatedly:

- Player tracking
- Team logic
- Spawn handling
- Map loading
- Game lifecycle management
- Instance management

GMA eliminates this repeated effort by providing a stable and extensible foundation.

It also introduces a game-encapsulated architecture, allowing multiple independent game instances to run concurrently within one server environment.
<br>
--> An alternative to the traditional one-server-per-game model. _But keep in mind that you can still do that if you want to!_

## Architecture Overview

GMA is built around the concept of **game encapsulation**.

Each game instance operates independently within its own environment, including:

- Dedicated world
- Isolated states
- Independent lifecycle
- Separate player groups

This design enables multiple games to run in parallel without interference while maintaining strong performance and clear separation of concerns.

GMA provides an extensive custom event system. Most internal actions and lifecycle changes trigger dedicated events, allowing external plugins to listen, react, and extend behavior without modifying core logic.

## Where to find what
| Component | Description |
| --------- | ----------- |
| [**/gma/**](https://github.com/GameManagementAPI/GameManager/tree/main/src/main/kotlin/de/c4vxl/gamemanager/gma/) | The core API module. |
| [**/language/**](https://github.com/GameManagementAPI/GameManager/tree/main/src/main/kotlin/de/c4vxl/gamemanager/language/) | The system for loading language translations. |
| [**/gma/plugin/**](https://github.com/GameManagementAPI/GameManager/tree/main/src/main/kotlin/de/c4vxl/gamemanager/plugin/) | The plugin side code. |
| [**/gma/plugin/commands/**](https://github.com/GameManagementAPI/GameManager/tree/main/src/main/kotlin/de/c4vxl/gamemanager/plugin/commands/) | The code of in-game commands. |
| [**/gma/plugin/handler/**](https://github.com/GameManagementAPI/GameManager/tree/main/src/main/kotlin/de/c4vxl/gamemanager/plugin/handler/) | Event listeners. |

<br>

# Installation

##### 1. Add library
Add GMA as a dependency in your plugin project to access the API.
**build.gradle.kts**:
```gradle
// ...

repositories {
    maven("https://mvn.c4vxl.de/gma/") // Add GMA-repo
}

dependencies {
    implementation("de.c4vxl:gamemanagementapi:1.0.0") // Import specific version
}

// ...
```

<details>
<summary><span style="font-size: 1.3rem">Other</span></summary>

**build.gradle**:
```gradle
// ...

repositories {
    maven {
        url = uri("https://mvn.c4vxl.de/gma/") // Add GMA-repo
    }
}

dependencies {
    implementation 'de.c4vxl:gamemanagementapi:1.0.0' // Import specific version
}

// ...
```

or

*Maven:*

**pom.xml**:
```xml
<project>
  ...
  <repositories>
    <repository>
      <id>GMA</id>
      <url>https://mvn.c4vxl.de/gma/</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>de.c4vxl</groupId>
      <artifactId>gamemanagementapi</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
  ...
</project>
```
</details>

##### 2. Download plugin
Install the GMA plugin on your server:
- Download the [latest version](https://github.com/GameManagementAPI/GameManager/releases/latest) of the plugin from this repo
- Drag it into your plugin folder

##### 3. Add as dependency
Edit your `plugin.yml` file to make your plugin depend on `GameManager`:
```yml
...

softdepend:
  - GameManager
```

<br>

# Build GMA yourself
To compile **GMA** youself, simply follow these steps:
1. Clone this repository
2. Build it using gradle: `./gradlew clean build`
3. The compiled `.jar` file can be found at /build/libs/

<br>

# Resources
- [Docs](http://gma.c4vxl.de/docs/)
- [Plugin template](https://github.com/GameManagementAPI/GMA-template/)

<br>

# License
You are welcome to use and experiment with this library freely for personal, educational, or research purposes.

Note that:
- If you use GMA in your own projects, you must credit the original library / author (c4vxl).
- For any larger-scale, commercial, or production use, or if you plan to redistribute or modify GMA, you are required contact the author first to discuss terms.
- This library is provided as-is, without warranties.

---

A project by [c4vxl](https://info.c4vxl.de/)
