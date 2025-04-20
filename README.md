# SSH Tunnel NG

![maven-publish](https://github.com/agung-m/sshtunnel-ng/actions/workflows/maven-publish.yml/badge.svg)

A user-friendly, fast, reliable, and cross-platform SSH tunnel manager.

What is SSH tunneling? \
https://en.wikipedia.org/wiki/Tunneling_protocol#Secure_Shell_tunneling \
https://www.ssh.com/academy/ssh/tunneling

<img src="img/sshtunnel-ng_screenshot.png" width=70% height=70%>

## Features

1. Simple and clear UI
   - Quick connect/disconnect from the system tray
3. Manage multiple sessions and tunnels
   - Local and remote forwarding are supported
   - Export/import tunnel configuration to/from a CSV file
5. Support common and additional SSH options:
   - Username/password and private key authentications
   - Ciphers
   - Enable/disable compression
9. Fast and lightweight (multithreaded with a small memory footprint of ~16 MB RAM)
10. Reliable (automatic reconnection, session hang prevention)
11. Cross-platform (Linux, Windows, and macOS)
12. Portable installation, no admin/root access required (runnable from an external disk or USB drive)

## Download

### Latest version

* [Linux x86-64](https://github.com/agung-m/sshtunnel-ng/releases/download/0.7/sshtunnel-ng-0.7-dist-linux-64.zip)
* [Windows 64-bit](https://github.com/agung-m/sshtunnel-ng/releases/download/0.7/sshtunnel-ng-0.7-dist-windows-64.zip)
* [macOS x86-64](https://github.com/agung-m/sshtunnel-ng/releases/download/0.7/sshtunnel-ng-0.7-dist-mac-64.zip)
* [macOS ARM64](https://github.com/agung-m/sshtunnel-ng/releases/download/0.7/sshtunnel-ng-0.7-dist-mac-aarch64.zip)

## Requirements

[Java Runtime (JRE)](https://www.java.com/en/download/manual.jsp) 8.0 or newer

For macOS ARM64, the minimum required Java version is [JDK17](https://www.oracle.com/uk/java/technologies/downloads/#jdk17-mac).

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## Contact
sshtunnel@agungmulya.com

--------------------------------------------------------------------------------
## Running

Unzip the distribution for your platform:

```
cd sshtunnel-ng-0.7-dist-windows-64
launch.bat
```

or:

```
cd sshtunnel-ng-0.7-dist-mac-aarch64
./launch.sh
```

Or run manually:

```
java -jar lib/sshtunnel-ng-0.7.jar
```

## Compiling from source (Gradle)

### For the current platform used for compiling:

```
./gradlew distCurrent
```

### For cross-platform builds:

```
./gradlew distWindows
./gradlew distLinux
./gradlew distMac
```

Each command will generate a full distribution in:

```
build/dist/<platform>/
```

### Supported platforms:

- windows
- linux
- mac

## Changes
See [Releases](https://github.com/agung-m/sshtunnel-ng/releases).
