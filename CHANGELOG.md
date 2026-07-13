# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.2.3 (v95) - 2026-07-13

### Added

- Added multi-format compilation support with dedicated commands for PDF, HTML, PNG, and SVG output
- Added Typst preview runner using Typst's HTML watch mode for live document previews
- Added automatic `PATH` configuration for the Typst CLI installation, allowing the `typst` binary
  to be called directly from the terminal

## Removed

- Removed Typst watch runners and migrated from compile runners to commands

### Maintenance

- Updated internal code formatting and project structure consistency.

## 1.1.3 (v95) - 2026-07-06

### Fixed

- Updated code to match SDK changes

## 1.1.2 (v90) - 2026-06-03

### Added

- Uninstall Typst language server automatically on extension uninstallation

### Changed

- Improved uninstallation question message

### Removed

- Stop showing installation prompt on startup

### Fixed

- Fixed Typst CLI dialogs not following theme

## 1.1.1 (v90) - 2026-06-02

### Added

- Implemented `onUpdated()` to ensure resources are cleaned up during extension updates

### Changed

- Removed the requirement for the maximum version.

## 1.1.0 (v90) - 2026-05-29

### Added

- Automatic setup of the `Typst` command-line tool on startup
- Automatic detection and updating of the latest available `Typst` and `Tinymist` versions
- Document compilation runner
- Document watch runner
- Manual commands for maintaining the `Typst` environment (update and install/uninstall)
- Added project changelog to track changes

### Changed

- The extension now automatically manages tool versions instead of relying on hardcoded versions
- Updated `README.md` with screenshots and extended feature list

## 1.0.1 (v88) - 2026-05-27

### Changed

- Updated the version terminology in accordance with the schema update in Xed-Editor.

## 1.0.0 (v87) - 2026-05-26

### Added

- Syntax highlighting for `Typst` files
- Language Server Protocol (LSP) support via [
  `Tinymist`](https://github.com/Myriad-Dreamin/tinymist)