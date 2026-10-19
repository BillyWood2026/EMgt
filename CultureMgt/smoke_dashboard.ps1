# smoke_dashboard.ps1 - 子模块10 跨模块文化数据看板 冒烟测试
# 覆盖：K1权限矩阵 / K1指标数值 / K2组织过滤与越权 / K3钻取与导出confirm机制 / 审计落库
$ErrorActionPreference = 'Stop'
$OutputEncoding = [Text.Encoding]::UTF8
[Console]::OutputEncoding = [Text.Encoding]::UTF8
$base = 'http://localhost:8160/api/culture/dashboard'
$script:pass = 0
$script:fail = 0

function Assert {
    param($cond, $name)
    if ($cond) { $script:pass++; Write-Host ("PASS  " + $name) }
    else { $script:fail++; Write-Host ("FAIL  " + $name) -ForegroundColor Red }
}

function Send-Api {
    param([string]$method, [string]$url, $headers, $body)
    try {
        if ($body -ne $null) {
            $bytes = [Text.Encoding]::UTF8.GetBytes($body)
            $r = Invoke-WebRequest -Uri $url -Method $method -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $bytes -UseBasicParsing
        } else {
            $r = Invoke-WebRequest -Uri $url -Method $method -Headers $headers -UseBasicParsing
        }
        $sr = New-Object IO.StreamReader($r.RawContentStream, [Text.Encoding]::UTF8)
        return @{ status = [int]$r.StatusCode; content = $sr.ReadToEnd() }
    } catch {
        $resp = $_.Exception.Response
        if ($resp -eq $null) { throw }
        $sr = New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
        return @{ status = [int]$resp.StatusCode; content = $sr.ReadToEnd() }
    }
}

function Hdr {
    param([string]$role, [string]$uid = 'smoke_admin', [string]$orgId = $null)
    $h = @{ 'X-Culture-Role' = $role; 'X-User-Id' = $uid }
    if ($orgId) { $h['X-Org-Id'] = $orgId }
    return $h
}

$orgEast = [uri]::EscapeDataString('华东子公司')

# ============ 1. K1 集团总看板权限矩阵 ============
$r = Send-Api 'GET' "$base/overview" (Hdr 'C01')
Assert ($r.status -eq 403) "K1-C01员工访问403"
$r = Send-Api 'GET' "$base/overview" (Hdr 'C02')
Assert ($r.status -eq 403) "K1-C02专员访问403"
$r = Send-Api 'GET' "$base/overview" (Hdr 'C04')
Assert ($r.status -eq 403) "K1-C04干部访问403"
$r = Send-Api 'GET' "$base/overview" (Hdr 'C03')
Assert ($r.status -eq 200) "K1-C03集团管理员200"
$kpi = ($r.content | ConvertFrom-Json).data
Assert ($null -ne $kpi.kpi.study.rate) "K1-学习完成率字段存在"
Assert ($kpi.kpi.caseTotal -gt 0) "K1-案例投稿数>0 实际=$($kpi.kpi.caseTotal)"
Assert ($kpi.kpi.activityJoin -gt 0) "K1-活动参与人次>0 实际=$($kpi.kpi.activityJoin)"
Assert ($kpi.kpi.news.published -gt 0) "K1-新闻产出>0 实际=$($kpi.kpi.news.published)"
Assert ($kpi.kpi.asset.views -gt 0) "K1-资产浏览量>0 实际=$($kpi.kpi.asset.views)"
Assert ($kpi.deptScores.Count -gt 0) "K1-部门综合得分非空 实际=$($kpi.deptScores.Count)部门"
Assert ($kpi.climateIndex -gt 0) "K1-氛围指数>0 实际=$($kpi.climateIndex)"
Assert ($null -ne $kpi.deptPassRate.rate) "K1-部门达标率字段存在"
Assert ($null -ne $kpi.duty.rate) "K1-干部履职完成率字段存在"
Assert ($kpi.scoreDist.Count -gt 0) "K1-积分分布非空"
$alerts = $kpi.alerts
Assert ($null -ne $alerts.studyOverdue -and $null -ne $alerts.surveyLow -and $null -ne $alerts.lowScore -and $null -ne $alerts.rectifyOverdue -and $null -ne $alerts.newsPending) "K1-五类预警卡齐全"
$r = Send-Api 'GET' "$base/overview" (Hdr 'C07')
Assert ($r.status -eq 200) "K1-C07高管200"
$r = Send-Api 'GET' "$base/overview" (Hdr 'C99')
Assert ($r.status -eq 200) "K1-C99超管200"

# ============ 2. K2 分组织看板 ============
$r = Send-Api 'GET' "$base/org?org=$orgEast" (Hdr 'C03')
Assert ($r.status -eq 200) "K2-C03按组织查询200"
$k2 = ($r.content | ConvertFrom-Json).data
Assert ($k2.org -eq '华东子公司') "K2-组织名回显 实际=$($k2.org)"
$r = Send-Api 'GET' "$base/org" (Hdr 'C02')
Assert ($r.status -eq 400 -and $r.content -match 'E-DB-04') "K2-C02无org参数400 E-DB-04"
$r = Send-Api 'GET' "$base/org?org=$orgEast" (Hdr 'C02' 'sunqi')
Assert ($r.status -eq 200) "K2-C02本组织查询200"
$r = Send-Api 'GET' "$base/org?org=%E9%9B%86%E5%9B%A2%E6%80%BB%E9%83%A8" (Hdr 'C02' 'sunqi' $orgEast)
Assert ($r.status -eq 400 -and $r.content -match 'E-DB-02') "K2-C02越权组织400 E-DB-02"

# ============ 3. K3 明细钻取 ============
$types = @('study','exam','survey','case','activity','news','asset','score','duty',
           'alert_study_overdue','alert_survey_low','alert_low_score','alert_rectify_overdue','alert_news_pending')
foreach ($t in $types) {
    $r = Send-Api 'GET' "$base/drill?type=$t" (Hdr 'C03')
    Assert ($r.status -eq 200) "K3-drill[$t]200"
}
$r = Send-Api 'GET' "$base/drill?type=study" (Hdr 'C03')
$rows = (($r.content | ConvertFrom-Json).data).rows
Assert ($rows.Count -gt 0) "K3-学习明细>0 实际=$($rows.Count)行"
Assert ($null -ne $rows[0].col1) "K3-明细列别名生效"
$r = Send-Api 'GET' "$base/drill?type=bogus" (Hdr 'C03')
Assert ($r.status -eq 400 -and $r.content -match 'E-DB-01') "K3-非法类型400 E-DB-01"
$r = Send-Api 'GET' "$base/drill?type=study" (Hdr 'C01')
Assert ($r.status -eq 403) "K3-C01访问403"

# ============ 4. K3 导出 confirm 机制 ============
$r = Send-Api 'GET' "$base/drill?type=score&export=true" (Hdr 'C03')
Assert ($r.status -eq 400 -and $r.content -match 'E-DB-03') "K3-导出无confirm400 E-DB-03"
$r = Send-Api 'GET' "$base/drill?type=score&export=true&confirm=true" (Hdr 'C03')
Assert ($r.status -eq 200) "K3-导出confirm=true 200"
$exp = ($r.content | ConvertFrom-Json).data
Assert ($exp.rows -gt 0) "K3-导出行数>0 实际=$($exp.rows)"
Assert ($exp.csv -match 'col1') "K3-导出CSV含表头"
Assert ($exp.fileName -like 'dashboard_score_*.csv') "K3-导出文件名 实际=$($exp.fileName)"

# ============ 5. 审计留痕 ============
$check = cmd /c '"C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -u root -pSun-0126King-0325 -N -e "SELECT COUNT(*) FROM culture_mgt.cul_audit_log WHERE action=''cul_dashboard'' AND target_type=''EXPORT'' AND detail LIKE ''%dashboard_score%''" 2>nul'
Assert ([int]($check | Select-Object -First 1) -ge 1) "审计-导出留痕落库 实际=$check"

Write-Host ""
Write-Host "========================================"
Write-Host "smoke_dashboard 结果: PASS=$script:pass FAIL=$script:fail"
Write-Host "========================================"
if ($script:fail -gt 0) { exit 1 } else { exit 0 }
