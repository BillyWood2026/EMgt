#!/usr/bin/env bash
set -euo pipefail

Root="$(cd "$(dirname "$0")/.." && pwd)"
fail=0

ok()   { echo "[PASS] $1"; }
fail() { echo "[FAIL] $1: $2"; fail=$((fail + 1)); }

# 1. services.json: 44 entries
svc="$Root/src/config/services.json"
if [ ! -f "$svc" ]; then fail "services.json" "missing"; else
  n=$(jq 'length' "$svc" 2>/dev/null || echo 0)
  if [ "$n" -eq 44 ]; then ok "services.json 44 services"; else fail "services.json" "count=$n"; fi
fi

# 2. proxy-rules.json: >= 80 rules
pr="$Root/src/config/proxy-rules.json"
if [ ! -f "$pr" ]; then fail "proxy-rules.json" "missing"; else
  n=$(jq 'length' "$pr" 2>/dev/null || echo 0)
  if [ "$n" -ge 80 ]; then ok "proxy-rules.json $n rules"; else fail "proxy-rules.json" "count=$n"; fi
fi

# 3. jars: 44 files
jarDir="$Root/vendor/jars"
if [ ! -d "$jarDir" ]; then fail "jars dir" "missing"; else
  n=$(find "$jarDir" -maxdepth 1 -name '*.jar' | wc -l | tr -d ' ')
  if [ "$n" -eq 44 ]; then ok "44 jars present"; else fail "jars" "count=$n"; fi
fi

# 4. db-dump: manifest + 44 gz files
mf="$Root/vendor/db-dump/manifest.json"
if [ ! -f "$mf" ]; then fail "manifest.json" "missing"; else
  n=$(jq '.databases | length' "$mf" 2>/dev/null || echo 0)
  if [ "$n" -eq 44 ]; then ok "manifest 44 databases"; else fail "manifest" "count=$n"; fi
fi
n=$(find "$Root/vendor/db-dump" -maxdepth 1 -name '*.sql.gz' | wc -l | tr -d ' ')
if [ "$n" -eq 44 ]; then ok "44 gz dumps"; else fail "gz dumps" "count=$n"; fi

# 5. frontend dist
fe="$Root/vendor/frontend"
if [ -f "$fe/index.html" ] && [ -d "$fe/assets" ]; then ok "frontend dist present"; else fail "frontend" "index.html or assets missing"; fi

# 6. runtime (win + linux + darwin-x64 + darwin-arm64)
for plat in win linux darwin-x64 darwin-arm64; do
  jre="$Root/runtime/$plat/jre/bin"
  mysqld="$Root/runtime/$plat/mysql/bin"
  if [ -d "$jre" ] && [ -d "$mysqld" ]; then ok "runtime $plat present"; else fail "runtime $plat" "jre or mysql missing"; fi
done

echo ""
if [ "$fail" -eq 0 ]; then echo "VERIFY_BUNDLE_PASS"; exit 0; else echo "VERIFY_BUNDLE_FAIL ($fail failures)"; exit 1; fi
