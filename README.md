# Typst Xed-Editor Extension

This extension adds support for the [Typst](https://typst.app/) language in Xed-Editor.

## Features

- Management of your Typst CLI installation
- Syntax highlighting for Typst files
- Language Server Protocol (LSP) support via [Tinymist](https://github.com/Myriad-Dreamin/tinymist)
- Multi-format compilation commands for PDF, HTML, PNG, and SVG output
- Live document preview
- Direct access to the Typst CLI binary through the system `PATH`

## Typst CLI API

The extension manages the Typst CLI installation and exposes the `typst` binary globally through
your shell `PATH`.

After installation, Typst can be used directly from the built-in terminal:

```bash
typst --version
typst help
typst compile document.typ
typst compile --format html --features html document.typ compiled.html
typst watch document.typ
```

## Screenshots

<div align="center">
  <img src="https://github.com/KonerDev/xed-typst/blob/main/assets/lsp.png" alt="Tinymist language server" width="30%">
  <img src="https://github.com/KonerDev/xed-typst/blob/main/assets/commands.png" alt="Typst runner" width="30%">
  <img src="https://github.com/KonerDev/xed-typst/blob/main/assets/runner.png" alt="Typst management commands" width="30%">
</div>

## Installation

Install the extension through the Xed-Editor's extension marketplace, and you're ready to go!
Alternatively, you can download the latest release ZIP file and install it via
**`Settings > Extensions > Install from storage`**.
