set -e

source "$LOCAL/bin/utils"

info 'Preparing...'
apt update && apt upgrade -y

TINYMIST_VERSION="v0.14.18"
INSTALL_DIR="$HOME/.lsp/typst"

get_arch() {
  case "$(uname -m)" in
    x86_64) echo "x86_64-unknown-linux-gnu" ;;
    aarch64 | arm64) echo "aarch64-unknown-linux-gnu" ;;
    *)
      error "Unsupported architecture: $(uname -m)"
      exit 1
      ;;
  esac
}

install() {
  info 'Installing Tinymist language server...'

  ARCH=$(get_arch)
  URL="https://github.com/Myriad-Dreamin/tinymist/releases/download/${TINYMIST_VERSION}/tinymist-${ARCH}.tar.gz"

  mkdir -p "$INSTALL_DIR"
  cd "$INSTALL_DIR"

  apt install -y curl ca-certificates tar

  curl -L -o tinymist.tar.gz "$URL"

  info "Extracting..."
  tar -xzf tinymist.tar.gz
  rm tinymist.tar.gz

  EXTRACT_DIR=$(find . -maxdepth 1 -type d -name "tinymist-*" | head -n 1)
  mv "$EXTRACT_DIR/tinymist" ./tinymist
  rm -rf "$EXTRACT_DIR"

  chmod +x tinymist

  echo "$TINYMIST_VERSION" > version.txt

  info 'Tinymist language server installed successfully.'
  exit 0
}


uninstall() {
  info 'Uninstalling Tinymist language server...'

  rm -rf "$INSTALL_DIR"

  info 'Tinymist uninstalled successfully.'
  exit 0
}

update() {
  info 'Updating Tinymist language server...'

  ARCH=$(get_arch)
  URL="https://github.com/Myriad-Dreamin/tinymist/releases/download/${TINYMIST_VERSION}/tinymist-${ARCH}.tar.gz"

  cd "$INSTALL_DIR"

  rm -f tinymist tinymist.tar.gz

  curl -L -o tinymist.tar.gz "$URL"

  info "Extracting..."
  tar -xzf tinymist.tar.gz
  rm tinymist.tar.gz

  chmod +x tinymist

  echo "$TINYMIST_VERSION" > version.txt

  info 'Tinymist updated successfully.'
  exit 0
}

case "$1" in
  --uninstall) uninstall;;
  --update) update;;
  *) install;;
esac
