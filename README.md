# SshTerminal

SSH terminal desktop app in Java 25 + Swing.

- **SSH** via [JSch](https://github.com/mwiede/jsch) (maintained fork): password, public key (`id_ed25519`, `id_ecdsa`, `id_rsa`) and keyboard-interactive auth, with host keys checked against `~/.ssh/known_hosts`
- **Terminal emulation** via [JediTerm](https://github.com/JetBrains/jediterm), the terminal used in JetBrains IDEs: xterm, 256 colours, mouse, copy/paste, scrollback, resizing
- **Look and feel** via [FlatLaf](https://github.com/JFormDesigner/FlatLaf) dark theme

Full-screen programs like `htop`, `vim` and `mc` work. Resizing the window resizes the remote terminal.

## Requirements

- JDK 25. If it isn't installed, Gradle downloads it automatically through the foojay toolchain resolver.
- No separate Gradle install is needed, because the project includes the Gradle wrapper.

## Run

```
gradlew run          # Windows
./gradlew run        # Linux / macOS
```

### In an IDE

- **IntelliJ IDEA:** **File → Open** the project folder and trust it. Then pick the **SshTerminal** run configuration and press Run.
- **NetBeans:** **File → Open Project** the folder. NetBeans opens Gradle projects natively. Then click **Run**.

## Project layout

```
src/main/java/sshterminal/
  SshTerminal.java        connect dialog, SSH session setup, terminal window
  JschTtyConnector.java   bridges the JSch shell channel and the JediTerm widget
.run/SshTerminal.run.xml  shared IntelliJ run configuration
```

## Build a Windows app / installer

```
gradlew installDist
jpackage --type app-image --name SshTerminal --input build\install\SshTerminal\lib ^
  --main-jar SshTerminal-1.0.0.jar --main-class sshterminal.SshTerminal
```

`--type app-image` produces a portable folder with `SshTerminal.exe`. Use `--type msi` for an installer, which requires the [WiX Toolset](https://wixtoolset.org/).

## License

[MIT](LICENSE). Third-party libraries keep their own licences: JSch (BSD-style), JediTerm (Apache 2.0 / LGPLv3) and FlatLaf (Apache 2.0).
