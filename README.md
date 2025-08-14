# 🧩 Fabric Command Generator – Minecraft 1.21.7

A lightweight, portable command generator mod for Minecraft Fabric servers.  
Designed to work entirely in JSON—no external functions, no dependencies, and no admin rights required.

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

## ✨ Features

- ✅ Vanilla-compatible `/crash` command (JSON-only logic)
- 🧪 Modular command generation for Fabric 1.21.7
- 🔒 Admin-free setup—no elevated permissions required
- 📁 Portable structure for locked-down environments
- 🛠️ Designed for reproducibility and community sharing

---

## 📦 Installation

1. Go to the [Releases tab](https://github.com/YOUR_USERNAME/YOUR_REPO/releases)
2. Download the latest `.jar` file
3. Drop it into your server’s `mods/` folder
4. Make sure you’re running **Minecraft 1.21.7** with **Fabric Loader**
5. Restart the server and test with `/crash` or other generated commands

---

## ⚙️ Usage

This mod generates commands using Minecraft’s built-in JSON structure.  
You can define custom behaviors without writing external functions.

Example usage:
```mcfunction
/crash @p
