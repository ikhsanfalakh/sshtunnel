# SSH Tunnel NG

![maven-publish](https://github.com/ikhsanfalakh/sshtunnel/actions/workflows/maven-publish.yml/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg)](https://kotlinlang.org/)

A modern, user-friendly, and cross-platform SSH tunnel manager built with Kotlin. Manage multiple SSH sessions and tunnels with ease through a clean, intuitive graphical interface.

<img src="img/sshtunnel-ng_screenshot.png" width="70%" alt="SSH Tunnel NG Screenshot">

## 🌟 Features

### Core Features
- **🖥️ Simple and Clear UI** - Intuitive interface with system tray integration for quick connect/disconnect
- **🔐 Multiple Session Management** - Handle multiple SSH sessions and tunnels simultaneously
- **🔄 Port Forwarding** - Support for both local and remote port forwarding
- **📊 Configuration Management** - Export/import tunnel configurations to/from CSV files
- **🚀 Auto-Running** - Automatically connect tunnels on application startup
- **📦 Minimize to System Tray** - Keep the application running in the background

### SSH Capabilities
- **Authentication Methods**
  - Username/password authentication
  - Private key authentication (RSA, DSA, ECDSA, Ed25519)
- **Advanced Options**
  - Custom cipher selection
  - Compression enable/disable
  - Connection timeout configuration
  - Keep-alive settings

### Performance & Reliability
- **⚡ Fast & Lightweight** - Multithreaded architecture with minimal memory footprint (~16 MB RAM)
- **🔄 Auto-Reconnection** - Automatic reconnection on connection failures
- **🛡️ Session Hang Prevention** - Built-in mechanism to prevent and recover from session hangs
- **💻 Cross-Platform** - Runs on Linux, Windows, and macOS

### Developer-Friendly
- **🔌 Portable Installation** - No admin/root access required
- **💾 External Drive Support** - Run directly from USB drives or external disks
- **📝 Logging** - Comprehensive logging with Logback

## 📥 Download

### Latest Release (v0.8)

| Platform | Download |
|----------|----------|
| 🐧 Linux x86-64 | [Download](https://github.com/ikhsanfalakh/sshtunnel/releases/latest/download/sshtunnel-ng-linux-x64.zip) |
| 🪟 Windows 64-bit | [Download](https://github.com/ikhsanfalakh/sshtunnel/releases/latest/download/sshtunnel-ng-windows-x64.zip) |
| 🍎 macOS x86-64 | [Download](https://github.com/ikhsanfalakh/sshtunnel/releases/latest/download/sshtunnel-ng-mac-x64.zip) |
| 🍎 macOS ARM64 | [Download](https://github.com/ikhsanfalakh/sshtunnel/releases/latest/download/sshtunnel-ng-mac-arm64.zip) |

[View all releases](https://github.com/ikhsanfalakh/sshtunnel/releases)

## 📋 Requirements

- **Java Runtime (JRE) 8** or newer - [Download JRE](https://www.java.com/en/download/manual.jsp)
- **macOS ARM64**: Minimum required Java version is [JDK 17](https://www.oracle.com/uk/java/technologies/downloads/#jdk17-mac)

## 🚀 Quick Start

### Running the Application

1. **Extract the distribution** for your platform:
   ```bash
   unzip sshtunnel-ng-0.8-dist-<platform>.zip
   cd sshtunnel-ng-0.8-dist-<platform>
   ```

2. **Launch the application**:

   **Windows:**
   ```cmd
   launch.bat
   ```

   **Linux/macOS:**
   ```bash
   chmod +x launch.sh
   ./launch.sh
   ```

   **Manual launch (all platforms):**
   ```bash
   java -jar sshtunnel-ng.jar
   ```

### Basic Usage

1. **Add a Session**
   - Click the "Add" button
   - Enter SSH server details (hostname, port, username)
   - Choose authentication method (password or private key)
   - Save the session

2. **Add Tunnels**
   - Select a session
   - Click "Add Tunnel"
   - Configure local/remote forwarding
   - Specify local and remote ports

3. **Connect**
   - Select session and click "Connect"
   - Use system tray icon for quick access
   - Auto-connect on startup (optional)

## 🛠️ Building from Source

### Prerequisites
- **JDK 18** or newer
- **Gradle** (included via Gradle Wrapper)

### Build Commands

**Compile the project:**
```bash
./gradlew clean build
```

**Create JAR file:**
```bash
./gradlew jar
```

**Create platform-specific distributions:**
```bash
# Windows distribution
./gradlew distWindows

# Linux distribution
./gradlew distLinux

# macOS distribution
./gradlew distMac
```

Distribution packages will be created in:
```
build/dist/<platform>/
```

### ⚠️ Important Note for Developers

Due to differences in the SWT library implementation across platforms, there's a platform-specific code adjustment required in `ApplicationComposite.kt` (lines 165-166):

**For macOS development/build:**
```kotlin
sashForm.setWeights(*configuration.weights) // Use this line
// sashForm.weights = configuration.weights // Comment this line
```

**For Windows/Linux development/build:**
```kotlin
// sashForm.setWeights(*configuration.weights) // Comment this line
sashForm.weights = configuration.weights // Use this line
```

This is due to the different API implementations in the Eclipse SWT library for macOS vs Windows/Linux. Make sure to adjust this before building for your target platform.

### Project Structure
```
sshtunnel-ng/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── org/
│       │       ├── agung/sshtunnel/addon/    # CSV import functionality
│       │       └── programmerplanet/sshtunnel/
│       │           ├── model/                 # Core models (Session, Tunnel, etc.)
│       │           ├── ui/                    # UI components
│       │           └── util/                  # Utilities
│       └── resources/
│           ├── appinfo.properties
│           └── images/                        # Application icons
├── libs/                                      # SWT native libraries
├── build.gradle.kts                          # Gradle build configuration
└── README.md
```

## 🔧 Technology Stack

- **Language**: Kotlin 2.0.20
- **SSH Library**: JSch 0.2.25 (Mwiede fork)
- **GUI Framework**: Eclipse SWT 3.129.0 / 4.3
- **Logging**: Kotlin-logging with Logback
- **Build Tool**: Gradle 8.10

## 📖 What is SSH Tunneling?

SSH tunneling (also known as SSH port forwarding) is a method of transporting arbitrary networking data over an encrypted SSH connection. It can be used to add encryption to legacy applications, bypass firewalls, and access services on internal networks securely.

**Learn more:**
- [Wikipedia - SSH Tunneling](https://en.wikipedia.org/wiki/Tunneling_protocol#Secure_Shell_tunneling)
- [SSH.com - SSH Tunneling Explained](https://www.ssh.com/academy/ssh/tunneling)

## 📝 Configuration Files

SSH Tunnel NG stores its configuration in your user home directory:

- **Windows**: `%USERPROFILE%\.sshtunnel\config.xml`
- **Linux/macOS**: `~/.sshtunnel/config.xml`

You can export/import configurations in CSV format for easy backup and sharing.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

See the [LICENSE](LICENSE) file for details.

## 📧 Contact

**Ikhsan Falakh** - mr.ikhsanfalakh@gmail.com

Project Link: [https://github.com/ikhsanfalakh/sshtunnel](https://github.com/ikhsanfalakh/sshtunnel)

## 🙏 Acknowledgments

- Original SSH Tunnel project by Joseph Fifield
- JSch library by Mwiede
- Eclipse SWT team for the UI framework
- All contributors who have helped improve this project

## 📋 Changelog

See [Releases](https://github.com/ikhsanfalakh/sshtunnel/releases) for a detailed changelog of each version.

### Recent Updates (v0.8)
- ✨ Kotlin migration and modernization
- 🚀 Performance improvements
- 🐛 Bug fixes and stability improvements
- 📦 Enhanced build system with Gradle

---

**Star ⭐ this repository if you find it useful!**
