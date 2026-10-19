#!/usr/bin/env bash
set -euo pipefail

JreVersion='jdk-17.0.20.1+1'
JreFileVer='17.0.20.1_1'
MysqlVersion='8.4.6'

Plats="${1:-win linux darwin-x64 darwin-arm64}"
ScriptDir="$(cd "$(dirname "$0")" && pwd)"
RuntimeDir="$(cd "$ScriptDir/.." && pwd)/runtime"
mkdir -p "$RuntimeDir"

download_file() {
    local url="$1"
    local out="$2"
    echo "Downloading: $url"
    curl -L --fail -o "$out" --max-time 3600 "$url"
}

extract_strip_top() {
    local archive="$1"
    local dest="$2"
    local tmpdir
    tmpdir=$(mktemp -d)
    if [[ "$archive" == *.zip ]]; then
        unzip -q "$archive" -d "$tmpdir"
    else
        tar -xf "$archive" -C "$tmpdir"
    fi
    local top
    top=$(find "$tmpdir" -mindepth 1 -maxdepth 1 -type d | head -n 1 || true)
    local entries
    entries=$(find "$tmpdir" -mindepth 1 -maxdepth 1 | wc -l | tr -d ' ')
    if [ -n "$top" ] && [ "$entries" -eq 1 ]; then
        cp -R "$top"/. "$dest/"
    else
        cp -R "$tmpdir"/. "$dest/"
    fi
    rm -rf "$tmpdir"
}

dir_size_mb() {
    du -sm "$1" 2>/dev/null | cut -f1
}

for plat in $Plats; do
    echo "=== Platform: $plat ==="

    jre_dir="$RuntimeDir/$plat/jre"
    if [ -d "$jre_dir/bin" ]; then
        echo "JRE ($plat) already exists, skipping."
    else
        case "$plat" in
            win)          os='windows'; arch='x64';    ext='zip' ;;
            linux)        os='linux';   arch='x64';    ext='tar.gz' ;;
            darwin-x64)   os='mac';     arch='x64';    ext='tar.gz' ;;
            darwin-arm64) os='mac';     arch='aarch64'; ext='tar.gz' ;;
            *) echo "Unknown platform: $plat" >&2; exit 1 ;;
        esac
        url="https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/$arch/$os/OpenJDK17U-jre_${arch}_${os}_hotspot_$JreFileVer.$ext"
        url_fallback="https://api.adoptium.net/v3/binary/version/$(printf '%s' "$JreVersion" | sed 's/+/%2B/g')/$os/$arch/jre/hotspot/normal/eclipse"
        out="/tmp/jre-$plat.$ext"
        if ! download_file "$url" "$out"; then
            download_file "$url_fallback" "$out"
        fi
        rm -rf "$jre_dir"
        mkdir -p "$jre_dir"
        extract_strip_top "$out" "$jre_dir"
        rm -f "$out"
        echo "JRE ($plat) size: $(dir_size_mb "$jre_dir") MB"
    fi

    mysql_dir="$RuntimeDir/$plat/mysql"
    if [ -d "$mysql_dir/bin" ]; then
        echo "MySQL ($plat) already exists, skipping."
    else
        urls=""
        case "$plat" in
            win)
                urls="https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-winx64.zip"
                ;;
            linux)
                urls="https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-linux-glibc2.17-x86_64-minimal.tar.xz
https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-linux-glibc2.17-x86_64.tar.xz"
                ;;
            darwin-x64)
                urls="https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-macos15-x86_64.tar.gz"
                ;;
            darwin-arm64)
                urls="https://cdn.mysql.com/archives/mysql-8.4/mysql-$MysqlVersion-macos15-arm64.tar.gz"
                ;;
        esac
        downloaded=0
        out=""
        for url in $urls; do
            fname=$(basename "$url")
            out="/tmp/$fname"
            if download_file "$url" "$out"; then
                downloaded=1
                break
            else
                echo "Download failed, trying fallback URL if available..."
                rm -f "$out"
            fi
        done
        if [ "$downloaded" -eq 0 ]; then
            echo "MySQL download failed for platform: $plat" >&2
            exit 1
        fi
        rm -rf "$mysql_dir"
        mkdir -p "$mysql_dir"
        extract_strip_top "$out" "$mysql_dir"
        rm -f "$out"
        echo "MySQL ($plat) size: $(dir_size_mb "$mysql_dir") MB"
    fi
done

echo "All done. Runtime directory: $RuntimeDir"
