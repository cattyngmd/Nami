# Nami Client 1.21.8

<p align="center">
  <a href="https://github.com/Kiriyaga7615/2bEssentials/releases">
    <img src="https://img.shields.io/github/v/release/Kiriyaga7615/2bEssentials?color=blue&label=Latest%20Release" alt="Latest Release" />
  </a>
  <a href="https://github.com/Kiriyaga7615/2bEssentials/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/Kiriyaga7615/2bEssentials?color=blue" alt="License" />
  </a>
  <a href="https://discord.gg/qy3eS42beW">
    <img src="https://img.shields.io/discord/your_discord_id_here?color=7289DA&label=Discord" alt="Discord" />
  </a>
</p>

![# badge](Assets/Readme/no-stops-no-regrets.svg)
![# badge](Assets/Readme/ensuring-code-integrity.svg)
![# badge](Assets/Readme/works-on-selfmerging.svg)


**Nami** is a modular and lightweight anarchy client base built for PVE and automation.  
The 1.21.4 branch is outdated!

Most popular Minecraft clients are closed-source, paid, and obfuscated, making them difficult to audit or trust. Some may include backdoors or malicious code.

This project started as a clean, open-source alternative aiming to be transparent, secure, and easy to extend without relying on unsafe third-party clients.

---

## Screenshots

<details>
<summary>View screenshots</summary>

<img width="1920" height="1080" alt="2025-07-25_16 23 39" src="https://github.com/user-attachments/assets/d20a1bd6-ead5-4328-9c9d-f9b29b7f5f4d" />

</details>

---

## Requirements

- Java 21  
- Gradle 8+  
- Minecraft 1.21.7  
- Fabric loader, API

---

## How to Build

1. Clone the repository:

    git clone https://github.com/Kiriyaga7615/Nami.git  
    cd nami

2. Build with Gradle:

    Linux / macOS  
    ./gradlew build

    Windows  
    gradlew.bat build

The compiled JAR will be located at:  
`build/libs/nami-<version>.jar`

---

## FAQ

<details>
<summary>How to open ClickGUI?</summary>

ClickGUI is not bound by default. Use the command:  
`-bind clickgui KEY`  
to bind it to your preferred key.

</details>

<details>
<summary>What is the command prefix?</summary>

The default command prefix is `-`.

</details>

<details>
<summary>Can I use a custom font?</summary>

Please use the Caxton mod instead of the built-in client font renderers.

</details>

---

## License

This project is licensed under the MIT License. You are free to contribute, distribute, fork, or reuse any part.

---

## Special Thanks

[cattyngmd](https://github.com/cattyngmd) for assistance with rotations and Y motion boost methods.

[CatFormat](https://github.com/cattyngmd/CatFormat) for awesome cat formatter.
