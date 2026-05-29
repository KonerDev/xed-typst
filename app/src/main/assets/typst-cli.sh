set -e

source "$LOCAL/bin/utils"

info 'Preparing...'
apt update && apt upgrade -y

TYPST_VERSION="v0.14.2"
INSTALL_DIR="$HOME/.local/bin"

get_arch() {
  case "$(uname -m)" in
    x86_64)
      echo "x86_64-unknown-linux-musl"
      ;;
    aarch64 | arm64)
      echo "aarch64-unknown-linux-musl"
      ;;
    *)
      error "Unsupported architecture: $(uname -m)"
      exit 1
      ;;
  esac
}

install() {
  info 'Installing Typst...'

  ARCH=$(get_arch)
  URL="https://github.com/typst/typst/releases/download/${TYPST_VERSION}/typst-${ARCH}.tar.xz"

  mkdir -p "$INSTALL_DIR"
  cd "$INSTALL_DIR"

  apt install -y curl ca-certificates tar xz-utils

  curl -L -o typst.tar.xz "$URL"

  info "Extracting..."
  tar -xJf typst.tar.xz
  rm typst.tar.xz

  EXTRACT_DIR=$(find . -maxdepth 1 -type d -name "typst-*" | head -n 1)
  mv "$EXTRACT_DIR/typst" ./typst
  rm -rf "$EXTRACT_DIR"

  chmod +x typst

  info 'Typst installed successfully.'
  exit 0
}

uninstall() {
  info 'Uninstalling Typst...'

  rm -rf "$INSTALL_DIR/typst"

  info 'Typst uninstalled successfully.'
  exit 0
}

update() {
  info 'Updating Typst...'

  if [ ! -x "$INSTALL_DIR/typst" ]; then
    error "Typst is not installed."
    exit 1
  fi

  "$INSTALL_DIR/typst" update

  info 'Typst updated successfully.'
  exit 0
}

case "$1" in
  --uninstall) uninstall ;;
  --update) update ;;
  *) install ;;
esac