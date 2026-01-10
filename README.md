# **RELEASES WILL NO LONGER BE RELEASED ON GITHUB. TO SEE LATEST RELEASES, VISIT THIS PAGE: https://modrinth.com/mod/command-maker/versions**

or this page:
https://commandmakerwiki.lucasgeitgey.com/download.html

# 🧩 Minecraft Command Maker – Fabric 26.1

A lightweight, portable command generator mod for Minecraft Fabric servers.  
Built entirely in Java—no external functions, no dependencies (besides Fabric API), and no admin rights required.
Visit the [**wiki**](https://commandmakerwiki.lucasgeitgey.com) for more information.

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

- ✅ Vanilla-compatible `/cmd` command (JSON-only logic)
- 🧪 Modular command generation for Fabric 26.1
- 🔒 Admin-free setup—no elevated permissions required
- 📁 Portable structure for locked-down environments
- 🛠️ Designed for reproducibility and community sharing

---

## Installation

1. Go to the [Releases page](commandmakerwiki.lucasgeitgey.com/download)
2. Download the latest `.jar` file
3. Drop it into your server’s `mods/` folder
4. Make sure you’re running **Minecraft 26.1+** with **Fabric Loader**
5. Restart the server and test with `/cmd` or other generated commands

### Client-Only Version

For single-player or when you want to use Command Maker on any server (even without server-side mod), there's a **client-only version**:

1. Download the `CMDMaker-Fabric-client.jar` file from releases
2. Place it in your client's `mods/` folder (not the server's)
3. The mod will work on any server, sending commands directly from your client
4. All features work the same as the server version

**Note:** The client version requires Fabric API on the client side.

---

## Usage

This mod generates commands using Minecraft’s built-in JSON structure.  
You can define custom behaviors without writing external functions.

### Commands

- `/cmd reload` - Reload all aliases from config
- `/cmd add <alias> <command>` - Add a new alias
- `/cmd del <alias>` - Remove an alias
- `/cmd function <functionName>` - Execute a function from the Functions folder

Example usage:

```mcfunction
/cmd add hello say hello
/cmd function test
```

Functions are stored in `config/CommandMaker/Functions/<name>.mcfunction` and can contain multiple commands.

Visit the [**wiki**](https://commandmakerwiki.lucasgeitgey.com/) for more information.

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
   - Or use the provided build script:
     ```bat
     build.bat
     ```

3. **Find the compiled `.jar` files**
   - After building, the mods will be located in:
     ```
     server/build/libs/CMDMaker-Fabric-server.jar  # Server version
     client/build/libs/CMDMaker-Fabric-client.jar  # Client-only version
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
