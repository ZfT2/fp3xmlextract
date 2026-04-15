# Fp3xmlextract

> ⚠ **Alpha Status**
>
> **Dieses Projekt befindet sich aktuell im Alpha-Stadium.**
>

![Build](https://github.com/ZfT2/fp3xmlextract/actions/workflows/release.yml/badge.svg)
![Release](https://img.shields.io/github/v/release/ZfT2/fp3xmlextract)
![Java](https://img.shields.io/badge/Java-21-blue)
![License](https://img.shields.io/badge/License-GPLv3-blue)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey)

🇩🇪 [Deutsch](#deutsch) | 🇬🇧 [English](#english)

---

# Deutsch

## Überblick

Bibliothek zur Konvertierung von Moneyplex XML Export Dateien in das CSV Format.
Incl. Erkennung von Gegenkonten / Umbuchungen.
Wird von GBanking verwendet.

## Funktionen


## Voraussetzungen

Für den Betrieb wird benötigt:

- **Java 17 oder höher**
- `java` muss im `PATH` verfügbar sein

Die Anwendung liefert **keine eigene JRE** mit.

## Releases

Vorgefertigte Bibliotheken (JAR files) sind auf GitHub verfügbar:

https://github.com/ZfT2/fp3xmlextract/releases


## Projekt selbst bauen

Voraussetzung: Maven und Java 17 oder höher.

### JAR

```
mvn clean package
```

Die erzeugten Archive befinden sich anschließend im Verzeichnis:

```
target/
```

## Projektstruktur (vereinfacht)

```
fp3xmlextract
├─ src
│  ├─ main
│  │  ├─ java
│  │  └─ resources
│  └─ test
├─ .github/workflows
│  └─ release.yml
├─ pom.xml
├─ README.md
├─ CHANGELOG.md
└─ LICENSE
```

## Entwicklung

Beiträge sind willkommen.

Siehe:

```
CONTRIBUTING.md
```

## Screenshot

*(optional – kann später ergänzt werden)*

```
docs/screenshot.png
```

## Lizenz

Dieses Projekt steht unter der

**GNU General Public License v3.0**

Siehe Datei:

```
LICENSE
```

---

# English

## Overview

Library to convert Moneyplex XML files to CSV format.
Incudes Rebooking detection.
Used by GBanking.


## Features



## Requirements

To run the application you need:

- **Java 17 or newer**
- `java` available in the system `PATH`

The application **does not bundle its own JRE**.


## Releases

Pre-built binaries are available on GitHub:

https://github.com/ZfT2/fp3xmlextract/releases


## Building from source

Requirements: Maven and Java 17 or newer.

### JAR distribution

```
mvn clean package
```

Build artifacts will be generated in:

```
target/
```

## Contributing

Contributions are welcome.

Please see:

```
CONTRIBUTING.md
```

## Screenshot

*(optional – may be added later)*

```
docs/screenshot.png
```

## License

This project is licensed under the

**GNU General Public License v3.0**

See the file:

```
LICENSE
```