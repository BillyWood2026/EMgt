#!/usr/bin/env bash
set -euo pipefail

Root="${EMGTV_ROOT:-/home/runner/work/EMgtv3/EMgtv3}"
Desk="$Root/desktop"
Vendor="$Desk/vendor"
JarDir="$Vendor/jars"
FeDir="$Vendor/frontend"
DumpDir="$Vendor/db-dump"
CfgDir="$Desk/src/config"

MysqlExe="${MYSQL_EXE:-mysql}"
DumpExe="${MYSQLDUMP_EXE:-mysqldump}"
MysqlPwd="${MYSQL_PWD:-Sun-0126King-0325}"

SkipFrontend=0
SkipDump=0
while [ $# -gt 0 ]; do
  case "$1" in
    --skip-frontend-build) SkipFrontend=1 ;;
    --skip-dump) SkipDump=1 ;;
  esac
  shift
done

mkdir -p "$JarDir" "$FeDir" "$DumpDir" "$CfgDir"

die() { echo "[FATAL] $1" >&2; exit 1; }
ok()  { echo "[OK] $1"; }
info(){ echo "[..] $1"; }

# 44 services: name, wave, xmx
WaveDefs=(
  "IAM:1:96" "module-config-center:1:96" "SysMgt:1:96" "SysMonitor:1:96"
  "OrgMgt:2:128" "EmployeeMgt:2:128" "AttendanceMgt:2:128" "RecruitmentMgt:2:128" "PerfMgt:2:128"
  "TrainingMgt:3:128" "QualificationMgt:3:128" "SalaryMgt:3:128" "SkillCertMgt:3:128" "TalentMgt:3:128"
  "ProjectMgt:4:128" "HRPlanningMgt:4:128" "HolidayMgt:4:128" "TalentReviewMgt:4:128" "SuccessionPlanMgt:4:128"
  "TalentDevMgt:5:128" "OnlineLearnMgt:5:128" "OnlineExamMgt:5:128" "AuditMgt:5:128" "DisciplineMgt:5:128"
  "PartyAffairsMgt:6:128" "CultureMgt:6:128" "ProcessMgt:6:128" "ImproveMgt:6:128" "FlowEngineMgt:6:128"
  "StrategyBlmMgt:7:96" "StrategyDsteMgt:7:96" "StrategySixsMgt:7:96" "StrategyBudgetMgt:7:96" "StrategyWorkplanMgt:7:96"
  "EnvironMgt:8:96" "SafetyMgt:8:96" "OccHealthMgt:8:96" "EnergyMgt:8:96"
  "DataMgt:9:72" "FileMgt:9:72" "MessageMgt:9:72" "IotMgt:9:72" "BiMgt:9:72" "ScreenMgt:9:72"
)

# 1. Frontend build
if [ "$SkipFrontend" -eq 0 ]; then
  info "Building frontend (vite)..."
  Fe="$Root/frontend"
  if [ ! -d "$Fe/node_modules" ]; then
    info "node_modules missing, running npm ci..."
    (cd "$Fe" && npm ci)
  fi
  (cd "$Fe" && npm run build)
  [ -f "$Fe/dist/index.html" ] || die "dist/index.html missing after build"
  rm -rf "$FeDir"
  mkdir -p "$FeDir"
  cp -R "$Fe/dist/"* "$FeDir/"
  feSize=$(du -sm "$FeDir" 2>/dev/null | cut -f1)
  ok "frontend dist copied (${feSize} MB)"
else
  info "Skip frontend build"
fi

# 2. Collect jars
info "Collecting jars..."
jarCount=0
missing=""
for def in "${WaveDefs[@]}"; do
  IFS=':' read -r name wave xmx <<< "$def"
  jar=$(find "$Root/$name/target" -maxdepth 1 -name "*-1.0.0.jar" ! -name "*-sources*" ! -name "*-javadoc*" -print -quit 2>/dev/null || true)
  if [ -z "$jar" ] || [ ! -f "$jar" ]; then
    missing="$missing $name"
    continue
  fi
  cp -f "$jar" "$JarDir/$name.jar"
  jarCount=$((jarCount + 1))
done
[ -z "$missing" ] || die "missing jars:$missing"
[ "$jarCount" -eq 44 ] || die "jar count=$jarCount, expected 44"
ok "44 jars collected"

# 3. services.json
info "Generating services.json..."
jsonFile="$CfgDir/services.json"
echo '[' > "$jsonFile"
first=1
for def in "${WaveDefs[@]}"; do
  IFS=':' read -r name wave xmx <<< "$def"
  yml="$Root/$name/src/main/resources/application.yml"
  [ -f "$yml" ] || die "application.yml missing for $name"
  port=$(grep -m1 -oE '^\s*port:\s*[0-9]+' "$yml" | grep -oE '[0-9]+' || true)
  [ -n "$port" ] || die "port not found for $name"
  db=$(sed -n 's/.*jdbc:mysql:\/\/[^/]*\/\([a-zA-Z0-9_-]\+\).*/\1/p' "$yml" | head -n1 || true)
  if [ -z "$db" ]; then
    db=$(sed -n 's/.*\${DB_NAME:\([a-zA-Z0-9_-]\+\)}.*/\1/p' "$yml" | head -n1 || true)
  fi
  [ -n "$db" ] || die "db name not found for $name"
  tier="full"; [ "$wave" -le 3 ] && tier="core"
  [ "$first" -eq 1 ] || echo ',' >> "$jsonFile"
  first=0
  cat >> "$jsonFile" <<JSON
  {
    "name": "$name",
    "port": $port,
    "db": "$db",
    "wave": $wave,
    "xmx": $xmx,
    "jar": "jars/$name.jar",
    "tier": "$tier"
  }
JSON
done
echo ']' >> "$jsonFile"
ok "services.json written (44 services)"

# DB list (unique) + optional extras
dbs=$(jq -r '.[].db' "$jsonFile" | sort -u)
extraFile="$Vendor/extra-databases.txt"
if [ -f "$extraFile" ]; then
  dbs=$(echo "$dbs"; grep -v '^#' "$extraFile" | grep -v '^$') | sort -u
fi
echo "===== DATABASE LIST TO EXPORT ====="
echo "$dbs"
echo "total: $(echo "$dbs" | wc -l)"

# 4. proxy-rules.json
info "Generating proxy-rules.json from vite.config.js..."
viteCfg="$Root/frontend/vite.config.js"
proxyFile="$CfgDir/proxy-rules.json"
echo '[' > "$proxyFile"
first=1
while IFS= read -r line; do
  if [[ "$line" =~ ^\ *\'([^\']+)\':\ *\{\ *target:\ *\'http://localhost:([0-9]+)\' ]]; then
    key="${BASH_REMATCH[1]}"
    tport="${BASH_REMATCH[2]}"
    mtype="prefix"; [[ "$key" == ^* ]] && mtype="regex"
    [ "$first" -eq 1 ] || echo ',' >> "$proxyFile"
    first=0
    cat >> "$proxyFile" <<JSON
  {
    "match": "$key",
    "type": "$mtype",
    "target": "http://127.0.0.1:$tport"
  }
JSON
  fi
done < "$viteCfg"
echo ']' >> "$proxyFile"
ruleCount=$(jq 'length' "$proxyFile")
[ "$ruleCount" -ge 50 ] || die "only $ruleCount proxy rules extracted, expected ~90"
ok "proxy-rules.json written ($ruleCount rules)"

# 5. mysqldump
manifestFile="$DumpDir/manifest.json"
if [ "$SkipDump" -eq 0 ]; then
  command -v "$DumpExe" >/dev/null 2>&1 || die "mysqldump not found: $DumpExe"
  verOut=$("$DumpExe" --version 2>/dev/null | head -n1 || true)
  info "source: $verOut"
  echo '{' > "$manifestFile"
  echo "  \"mysqlSourceVersion\": \"$verOut\"," >> "$manifestFile"
  echo "  \"exportedAt\": \"$(date -Iseconds)\"," >> "$manifestFile"
  echo '  "databases": [' >> "$manifestFile"
  first=1
  echo "$dbs" | while IFS= read -r db; do
    [ -n "$db" ] || continue
    chk=$("$MysqlExe" -h127.0.0.1 -uroot -p"$MysqlPwd" -N -e "SHOW DATABASES LIKE '$db'" 2>/dev/null || true)
    if [ "$(echo "$chk" | tr -d ' ')" != "$db" ]; then
      echo "[WARN] database missing on server, skipped: $db" >&2
      continue
    fi
    sqlFile="$DumpDir/$db.sql"
    gzFile="$DumpDir/$db.sql.gz"
    info "dumping $db ..."
    "$DumpExe" -h127.0.0.1 -uroot -p"$MysqlPwd" --single-transaction --routines --triggers --events \
      --set-gtid-purged=OFF --column-statistics=0 --no-tablespaces --hex-blob --default-character-set=utf8mb4 \
      --databases "$db" > "$sqlFile"
    [ -f "$sqlFile" ] || die "dump failed for $db"
    size=$(wc -c < "$sqlFile" | tr -d ' ')
    [ "$size" -gt 100 ] || die "dump too small for $db"
    gzip -f "$sqlFile"
    bytes=$(wc -c < "$gzFile" | tr -d ' ')
    if command -v sha256sum >/dev/null 2>&1; then
      sha=$(sha256sum "$gzFile" | awk '{print $1}')
    else
      sha=$(shasum -a 256 "$gzFile" | awk '{print $1}')
    fi
    [ "$first" -eq 1 ] || echo ',' >> "$manifestFile"
    first=0
    cat >> "$manifestFile" <<JSON
    {
      "db": "$db",
      "file": "$db.sql.gz",
      "bytes": $bytes,
      "sha256": "$sha"
    }
JSON
    ok "  $db -> $((bytes/1024)) KB"
  done
  echo '' >> "$manifestFile"
  echo '  ]' >> "$manifestFile"
  echo '}' >> "$manifestFile"
  dbCount=$(jq '.databases | length' "$manifestFile")
  ok "manifest.json written ($dbCount databases)"
else
  info "Skip mysqldump"
fi

ok "COLLECT_RESOURCES_DONE"
