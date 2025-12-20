# **RELEASES WILL NO LONGER BE RELEASED ON GITHUB. TO SEE LATEST RELEASES, VISIT THIS PAGE: https://modrinth.com/mod/command-maker/versions**

# 🧩 Minecraft Command Maker – Fabric 26.1

A lightweight, portable command generator mod for Minecraft Fabric servers.  
Built entirely in JSON—no external functions, no dependencies, and no admin rights required.

---

## 📚 Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Compile from Source](#compile-from-source)
- [Bug Reports & Feedback](#bug-reports--feedback)
- [Contributing](#contributing)
- [Disclaimer](#disclaimer)

---

## Features

- ✅ Vanilla-compatible `/crash` command (JSON-only logic)
- 🧪 Modular command generation for Fabric 1.21.7
- 🔒 Admin-free setup—no elevated permissions required
- 📁 Portable structure for locked-down environments
- 🛠️ Designed for reproducibility and community sharing

---

## Installation

1. Go to the [Releases tab](https://github.com/Diamondstar-Mods/Minecraft-Command-Maker/releases)
2. Download the latest `.jar` file
3. Drop it into your server’s `mods/` folder
4. Make sure you’re running **Minecraft 26.1** with **Fabric Loader**
5. Restart the server and test with `/crash` or other generated commands

---

## Usage

This mod generates commands using Minecraft’s built-in JSON structure.  
You can define custom behaviors without writing external functions.

Example usage:
```mcfunction
/crash @p
```

This command triggers a crash effect using only in-line JSON logic.  
More commands and templates coming soon!

---

## Compile from Source

You can build this mod yourself using the included Gradle wrapper. Here's how:

### 🧰 Requirements

- Java Development Kit (JDK) 17 or higher
- Git
- A terminal or command prompt
- (Optional) VS Code or IntelliJ IDEA for editing

### 🔧 Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/Diamondstar-Mods/Minecraft-Command-Maker.git
   cd Minecraft-Command-Maker
   ```

2. **Build the mod**
   - On Linux/macOS:
     ```bash
     ./gradlew build
     ```
   - On Windows:
     ```bat
     gradlew.bat build
     ```

3. **Find the compiled `.jar`**
   - After building, the mod will be located in:
     ```
     build/libs/
     ```

### 🛠️ Troubleshooting

- If you get a `JAVA_HOME` error, make sure JDK 17+ is installed and your environment variables are set.
- If dependencies fail to download, check your internet connection or proxy settings.
- If using an IDE, open the folder as a Gradle project and refresh dependencies.

---

## Bug Reports & Feedback

Found a bug? Have a suggestion?  
Please [open an issue](https://github.com/Diamondstar-Mods/Minecraft-Command-Maker/issues) with:

- Minecraft + Fabric version
- Steps to reproduce
- Any error logs or screenshots

Your feedback helps improve the mod for everyone!

---

## Contributing

Pull requests are welcome!  
If you’d like to add features, fix bugs, or improve documentation:

1. Fork the repo
2. Create a new branch
3. Make your changes
4. Submit a PR with a clear description

Please keep changes modular and well-commented.

---

## Disclaimer

This is my **first Minecraft mod**, and I’m still learning the ropes.  
There may be bugs, quirks, or unexpected behavior.  
Thanks for your patience—and feel free to help improve it!

---

Made with ❤️ by [Lucas Geitgey](https://github.com/lucgei231)
