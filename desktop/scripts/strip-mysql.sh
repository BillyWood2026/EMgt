#!/usr/bin/env bash
set -euo pipefail

Plats="${1:-linux darwin-x64 darwin-arm64}"
ScriptDir="$(cd "$(dirname "$0")" && pwd)"
RuntimeDir="$(cd "$ScriptDir/.." && pwd)/runtime"

keep_bins=(
  mysqld mysql mysqladmin mysqldump
  mysqlcheck mysql_config_editor my_print_defaults
  mysql_ssl_rsa_setup mysql_tzinfo_to_sql
)

for plat in $Plats; do
  mysqlDir="$RuntimeDir/$plat/mysql"
  [ -d "$mysqlDir" ] || { echo "Skip $plat (not found)"; continue; }

  before=$(du -sm "$mysqlDir" 2>/dev/null | cut -f1)
  echo "=== Strip MySQL: $plat (before: ${before} MB) ==="

  # Build keep list
  keepFile=$(mktemp)
  trap 'rm -f "$keepFile"' EXIT

  # bin keep list
  for name in "${keep_bins[@]}"; do
    for f in "$mysqlDir/bin/$name" "$mysqlDir/bin/$name.exe"; do
      [ -f "$f" ] && echo "$f" >> "$keepFile"
    done
  done

  # Windows dlls
  if [ "$plat" = "win" ]; then
    find "$mysqlDir/bin" -maxdepth 1 -name '*.dll' -type f 2>/dev/null >> "$keepFile" || true
  fi

  # lib keep: *.so*, *.dylib, plugin/
  if [ -d "$mysqlDir/lib" ]; then
    find "$mysqlDir/lib" -type f \( \
      -name '*.dll' -o -name '*.dylib' -o -name '*.so' -o -name '*.so.*' \
    \) 2>/dev/null >> "$keepFile" || true
    find "$mysqlDir/lib/plugin" -type f 2>/dev/null >> "$keepFile" || true
  fi

  # share keep: english/errmsg.sys + charsets/*
  if [ -d "$mysqlDir/share" ]; then
    [ -f "$mysqlDir/share/english/errmsg.sys" ] && echo "$mysqlDir/share/english/errmsg.sys" >> "$keepFile"
    find "$mysqlDir/share/charsets" -type f 2>/dev/null >> "$keepFile" || true
  fi

  # Delete everything not in keep list
  removed=0
  while IFS= read -r -d '' f; do
    rel="${f#$mysqlDir/}"
    if ! grep -qxF "$f" "$keepFile" 2>/dev/null; then
      rm -f "$f"
      removed=$((removed + 1))
    fi
  done < <(find "$mysqlDir" -type f -print0)

  # Remove empty directories
  while true; do
    count=0
    while IFS= read -r -d '' d; do
      if [ -z "$(find "$d" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null)" ]; then
        rmdir "$d" 2>/dev/null && count=$((count + 1))
      fi
    done < <(find "$mysqlDir" -type d -print0)
    [ "$count" -eq 0 ] && break
  done

  after=$(du -sm "$mysqlDir" 2>/dev/null | cut -f1)
  echo "Removed $removed files. After: ${after} MB (saved $((before - after)) MB)"

  # Verify mysqld still runs
  if [ "$plat" = "win" ]; then
    mysqldBin="$mysqlDir/bin/mysqld.exe"
  else
    mysqldBin="$mysqlDir/bin/mysqld"
  fi
  if [ -f "$mysqldBin" ]; then
    ver=$("$mysqldBin" --version 2>/dev/null || true)
    if [ -n "$ver" ]; then
      echo "Verification: $ver"
    else
      echo "WARNING: mysqld --version produced no output after strip."
    fi
  else
    echo "WARNING: mysqld not found after strip."
  fi
done

echo "Strip complete."
