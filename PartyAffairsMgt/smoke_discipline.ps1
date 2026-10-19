# 第13章 纪检监察管理 冒烟脚本（可重入）
# 验证 6 子域：信访举报/问题线索/审查调查/案件审理/纪律教育/纪检驾驶舱
# E-DI-01..25 错误码全覆盖

$ErrorActionPreference = 'Continue'
$base = "http://127.0.0.1:8150/api/party/discipline"
$pass = 0; $fail = 0
$results = @()

function Req {
    param([string]$Method, [string]$Url, [object]$Body = $null, [string]$Role = "R06", [switch]$Raw)
    try {
        $req = [System.Net.HttpWebRequest]::Create($Url)
        $req.Method = $Method
        $req.Headers.Add("X-Party-Role", $Role)
        $req.ContentType = "application/json; charset=utf-8"
        if ($Method -ne "GET" -and $null -ne $Body) {
            $json = if ($Body -is [string]) { $Body } else { ($Body | ConvertTo-Json -Depth 10 -Compress) }
            $bytes = [Text.Encoding]::UTF8.GetBytes($json)
            $req.ContentLength = $bytes.Length
            $s = $req.GetRequestStream()
            $s.Write($bytes, 0, $bytes.Length)
            $s.Close()
        }
        $resp = $req.GetResponse()
        $sr = New-Object System.IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
        $body = $sr.ReadToEnd()
        $sr.Close()
        return @{ ok = $true; status = 200; body = $body }
    } catch [System.Net.WebException] {
        $status = 0; $body = ""
        $resp = $_.Exception.Response
        if ($resp -ne $null) {
            $status = [int]$resp.StatusCode
            try {
                $sr = New-Object System.IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
                $body = $sr.ReadToEnd()
                $sr.Close()
            } catch {}
        }
        if (-not $body) { $body = $_.Exception.Message }
        return @{ ok = $false; status = $status; body = $body; err = $_.Exception.Message }
    } catch {
        return @{ ok = $false; status = 0; body = $_.Exception.Message; err = $_.Exception.Message }
    }
}

function Assert {
    param([string]$Name, [bool]$Cond, [string]$Detail = "")
    if ($Cond) {
        $script:pass++
        $script:results += "[PASS] $Name"
        Write-Host "  [PASS] $Name" -ForegroundColor Green
    } else {
        $script:fail++
        $script:results += "[FAIL] $Name :: $Detail"
        Write-Host "  [FAIL] $Name :: $Detail" -ForegroundColor Red
    }
}

function HasCode { param([object]$Resp, [string]$Code)
    if ($null -eq $Resp.body) { return $false }
    return ($Resp.body -like "*$Code*")
}

Write-Host "===== 第13章 纪检监察管理 冒烟 =====" -ForegroundColor Cyan

# 0. 清理（mysql）
Write-Host "`n--- 0. 清理测试数据 ---" -ForegroundColor Yellow
$mysql = "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe"
& $mysql -uroot -p"Sun-0126King-0325" party_affairs_mgt -e "DELETE FROM petition_report WHERE reported_name LIKE 'smoke%'; DELETE FROM problem_clue WHERE target_name LIKE 'smoke%'; DELETE FROM discipline_talk WHERE target_name LIKE 'smoke%'; DELETE FROM discipline_investigation WHERE target_name LIKE 'smoke%' OR case_name LIKE 'smoke%'; DELETE FROM case_adjudication WHERE target_name LIKE 'smoke%'; DELETE FROM discipline_education WHERE title LIKE 'smoke%';" 2>$null
Write-Host "  cleaned"

# 1. 信访举报 Petition
Write-Host "`n--- 1. 信访举报 ---" -ForegroundColor Yellow

# E-DI-01 必填缺失
$r = Req -Method POST -Url "$base/petition" -Body @{reportedName=""} -Role "R23"
Assert "E-DI-01 信访登记必填缺失" (HasCode $r "E-DI-01") $r.body

# 正常登记（R23）
$r = Req -Method POST -Url "$base/petition" -Body @{
    reportedName = "smoke张三"; problemType = "贪污受贿"; content = "smoke举报：张三在2026年招标中收受好处费";
    channel = "来信"; isAnonymous = "否"; reporterName = "李四"; contact = "13800138000"
} -Role "R23"
$ok = $r.ok -and ($r.body -like "*JB*")
Assert "信访登记（R23）" $ok $r.body
$petitionId = 0
if ($ok) { $petitionId = (($r.body | ConvertFrom-Json).data).id }

# 重复件识别 E-DI-05
$r = Req -Method POST -Url "$base/petition" -Body @{
    reportedName = "smoke张三"; problemType = "贪污受贿"; content = "smoke举报：张三在2026年招标中收受好处费";
    channel = "来信"; isAnonymous = "是"
} -Role "R23"
Assert "E-DI-05 重复件识别" (HasCode $r "E-DI-05") $r.body

# force=1 放行
$r = Req -Method POST -Url "$base/petition?force=true" -Body @{
    reportedName = "smoke张三"; problemType = "贪污受贿"; content = "smoke举报：张三在2026年招标中收受好处费";
    channel = "来访"; isAnonymous = "是"
} -Role "R23"
Assert "E-DI-05 force=1 放行" $r.ok $r.body

# 越权（R99不能登记）
$r = Req -Method POST -Url "$base/petition" -Body @{reportedName="smoke X";problemType="X";content="X"} -Role "R99"
Assert "R99 登记被拒（403）" (-not $r.ok) $r.body

# 受理研判
if ($petitionId -gt 0) {
    $r = Req -Method POST -Url "$base/petition/$petitionId/judge" -Body @{conclusion="予以受理";judgeBy="R20主任"} -Role "R20"
    Assert "受理研判（R20）" ($r.ok -and ($r.body -like "*已受理*")) $r.body

    # E-DI-04 转办非下级
    $r = Req -Method POST -Url "$base/petition/$petitionId/transfer" -Body @{transferOrg="非下级单位"} -Role "R20"
    Assert "E-DI-04 转办非下级阻断" (HasCode $r "E-DI-04") $r.body

    # 转办正确下级
    $r = Req -Method POST -Url "$base/petition/$petitionId/transfer" -Body @{transferOrg="纪检监察室"} -Role "R20"
    Assert "转办下级（R20）" $r.ok $r.body

    # 反馈
    $r = Req -Method POST -Url "$base/petition/$petitionId/feedback" -Body @{handleResult="smoke已办结：经查属实，已处理";feedback="已向实名举报人反馈"} -Role "R21"
    Assert "办理反馈（R21）" ($r.ok -and ($r.body -like "*已办结*")) $r.body

    # 归档
    $r = Req -Method POST -Url "$base/petition/$petitionId/archive" -Role "R20"
    Assert "信访归档（R20）" ($r.ok -and ($r.body -like "*已归档*")) $r.body
}

# 信访统计
$r = Req -Method GET -Url "$base/petition/stats" -Role "R23"
Assert "信访台账统计" ($r.ok -and ($r.body -like "*byType*")) $r.body

# 2. 问题线索 Clue
Write-Host "`n--- 2. 问题线索 ---" -ForegroundColor Yellow

# E-DI-01 必填缺失
$r = Req -Method POST -Url "$base/clue" -Body @{targetName=""} -Role "R20"
Assert "E-DI-01 线索必填缺失" (HasCode $r "E-DI-01") $r.body

# E-DI-08 信访转线索未关联
$r = Req -Method POST -Url "$base/clue" -Body @{
    targetName="smoke李四"; description="smoke线索"; source="信访举报"
} -Role "R20"
Assert "E-DI-08 信访转线索未关联阻断" (HasCode $r "E-DI-08") $r.body

# 登记（关联已归档信访件）
$r = Req -Method POST -Url "$base/clue" -Body @{
    targetName="smoke李四"; targetOrg="第一党支部"; description="smoke线索：违规发放津补贴";
    source="巡察移交"; amount=50000
} -Role "R20"
$ok = $r.ok -and ($r.body -like "*XS*")
Assert "线索登记（R20）" $ok $r.body
$clueId = 0
if ($ok) { $clueId = (($r.body | ConvertFrom-Json).data).id }

# E-DI-10 同被反映人重复登记
$r = Req -Method POST -Url "$base/clue" -Body @{
    targetName="smoke李四"; description="smoke重复"; source="巡察移交"
} -Role "R20"
Assert "E-DI-10 重复登记提示" (HasCode $r "E-DI-10") $r.body

if ($clueId -gt 0) {
    # 研判（未填处置方式）
    $r = Req -Method POST -Url "$base/clue/$clueId/judge" -Body @{disposeMethod="错误值"} -Role "R20"
    Assert "E-DI-06 处置方式错误" (HasCode $r "E-DI-06") $r.body

    # 予以了结缺理由
    $r = Req -Method POST -Url "$base/clue/$clueId/judge" -Body @{disposeMethod="予以了结"} -Role "R20"
    Assert "E-DI-09 了结须理由" (HasCode $r "E-DI-09") $r.body

    # 正确研判：初步核实
    $r = Req -Method POST -Url "$base/clue/$clueId/judge" -Body @{
        disposeMethod="初步核实"; judgeMeeting="smoke 2026-10-07集体研判会"; judgeBy="R20主任"
    } -Role "R20"
    Assert "线索研判（初步核实）" ($r.ok -and ($r.body -like "*初核中*")) $r.body

    # 处置报告
    $r = Req -Method POST -Url "$base/clue/$clueId/report" -Body @{disposeReport="smoke初核发现违规事实清楚"} -Role "R21"
    Assert "处置报告（R21）" $r.ok $r.body

    # 转立案
    $r = Req -Method POST -Url "$base/clue/$clueId/to-case" -Body @{fileBasis="《中国共产党纪律处分条例》第四十条";team="smoke审查组";dept="纪检监察室"} -Role "R20"
    $ok2 = $r.ok -and ($r.body -like "*AJ*")
    Assert "线索转立案（R20）" $ok2 $r.body
    $script:caseId = 0
    if ($ok2) { $script:caseId = (($r.body | ConvertFrom-Json).data).id }
}

# 3. 审查调查 Investigation
Write-Host "`n--- 3. 审查调查 ---" -ForegroundColor Yellow

# 手动建案（另一条线索）
$r = Req -Method POST -Url "$base/clue" -Body @{
    targetName="smoke王五"; targetOrg="第二党支部"; description="smoke手动线索"; source="审计移送"; amount=30000
} -Role "R20"
$clue2Id = 0
if ($r.ok) { $clue2Id = (($r.body | ConvertFrom-Json).data).id }
if ($clue2Id -gt 0) {
    $r = Req -Method POST -Url "$base/clue/$clue2Id/judge" -Body @{disposeMethod="初步核实"; judgeMeeting="smoke"; judgeBy="R20"} -Role "R20"
    $r = Req -Method POST -Url "$base/clue/$clue2Id/to-case" -Body @{fileBasis="处分条例";team="smoke审查组2";dept="纪检监察室"} -Role "R20"
}

# 用 caseId（前面转立案得到）
if ($script:caseId -gt 0) {
    $cid = $script:caseId

    # 立案审批（R06）
    $r = Req -Method POST -Url "$base/investigation/$cid/approve-file" -Body @{fileBasis="《中国共产党纪律检查机关监督执纪工作规则》";approveBy="R06纪委书记"} -Role "R06"
    Assert "立案审批（R06）" ($r.ok -and ($r.body -like "*立案审查*")) $r.body

    # 谈话：E-DI-12 笔录/录像编号缺失
    $r = Req -Method POST -Url "$base/investigation/$cid/talks" -Body @{
        targetName="smoke李四"; place="纪检监察室"; note="smoke谈话笔录"
    } -Role "R21"
    Assert "E-DI-12 笔录/录像编号缺失" (HasCode $r "E-DI-12") $r.body

    # 谈话：正常
    $r = Req -Method POST -Url "$base/investigation/$cid/talks" -Body @{
        targetName="smoke李四"; place="纪检监察室"; talkNo="BL2026-001"; videoNo="LX2026-001"; note="smoke谈话笔录"; recorder="记录员A"
    } -Role "R21"
    Assert "谈话记录（一一对应）" $r.ok $r.body

    # 谈话：talkNo重复
    $r = Req -Method POST -Url "$base/investigation/$cid/talks" -Body @{
        targetName="smoke李四"; place="纪检监察室"; talkNo="BL2026-001"; videoNo="LX2026-002"; note="smoke重复"
    } -Role "R21"
    Assert "E-DI-12 笔录编号重复阻断" (HasCode $r "E-DI-12") $r.body

    # 延期：超过6个月
    $r = Req -Method POST -Url "$base/investigation/$cid/extend" -Body @{months=7; reason="smoke测试超限"} -Role "R20"
    Assert "E-DI-11 延期超过6月阻断" (HasCode $r "E-DI-11") $r.body

    # 延期：合法 3 月
    $r = Req -Method POST -Url "$base/investigation/$cid/extend" -Body @{months=3; reason="smoke案情复杂"} -Role "R20"
    Assert "审查延期3月（R20）" $r.ok $r.body

    # 移送：缺审查报告
    $r = Req -Method POST -Url "$base/investigation/$cid/transfer" -Body @{sanctionBasis="处分条例第40条"} -Role "R20"
    Assert "E-DI-13 缺审查报告阻断移送" (HasCode $r "E-DI-13") $r.body

    # 上传审查报告（缺财物清单）
    $r = Req -Method POST -Url "$base/investigation/$cid/report" -Body @{
        reviewReport="smoke审查报告：违规事实清楚"; facts="smoke违规发放5万元"; result="建议给予党纪处分"
    } -Role "R21"
    Assert "审查报告上传" $r.ok $r.body

    $r = Req -Method POST -Url "$base/investigation/$cid/transfer" -Body @{sanctionBasis="处分条例第40条"} -Role "R20"
    Assert "E-DI-13 缺财物清单阻断移送" (HasCode $r "E-DI-13") $r.body

    # 补财物清单
    $r = Req -Method POST -Url "$base/investigation/$cid/report" -Body @{
        reviewReport="smoke审查报告：违规事实清楚"; propertyList="smoke涉案财物：现金5万元已暂扣"
    } -Role "R21"
    Assert "补财物清单" $r.ok $r.body

    # 移送审理
    $r = Req -Method POST -Url "$base/investigation/$cid/transfer" -Body @{sanctionBasis="处分条例第40条"} -Role "R20"
    $ok3 = $r.ok
    Assert "移送审理（R20）" $ok3 $r.body
    $script:adjId = 0
    if ($ok3) { $script:adjId = (($r.body | ConvertFrom-Json).data).id }

    # E-DI-14 移送后只读
    $r = Req -Method POST -Url "$base/investigation/$cid/extend" -Body @{months=1; reason="smoke再延"} -Role "R20"
    Assert "E-DI-14 移送后只读阻断" (HasCode $r "E-DI-14") $r.body
}

# 4. 案件审理 Adjudication
Write-Host "`n--- 4. 案件审理与处分执行 ---" -ForegroundColor Yellow

if ($script:adjId -gt 0) {
    $aid = $script:adjId

    # 审理（R22）
    $r = Req -Method POST -Url "$base/adjudication/$aid/review" -Body @{
        reviewReport="smoke审理报告"; reviewMeeting="smoke 2026-10-07审理会"; adjudicationOpinion="smoke建议开除党籍"
    } -Role "R22"
    Assert "审理（R22）" $r.ok $r.body

    # 处分决定（R06）：警告
    $r = Req -Method POST -Url "$base/adjudication/$aid/decide" -Body @{punishmentType="警告"; sanctionBasis="处分条例第40条"} -Role "R06"
    $ok4 = $r.ok -and ($r.body -like "*集团纪委*")
    Assert "处分决定（R06）文号" $ok4 $r.body
    # 影响期 = 12月
    if ($ok4) {
        $data = ($r.body | ConvertFrom-Json).data
        $inflEnd = [DateTime]::Parse($data.influenceEnd)
        $inflStart = [DateTime]::Parse($data.influenceStart)
        $months = (($inflEnd - $inflStart).Days / 30.0)
        Assert "E-DI-20 警告影响期≈12月" ([Math]::Abs($months - 12) -lt 1.5) "months=$months"
    }

    # 未宣布不可执行
    $r = Req -Method POST -Url "$base/adjudication/$aid/execute" -Body @{executeOrg="第一党支部";executor="R22";executeReceipt="smoke回执"} -Role "R22"
    Assert "E-DI-16 未宣布不可执行" (HasCode $r "E-DI-16") $r.body

    # 宣布
    $r = Req -Method POST -Url "$base/adjudication/$aid/announce" -Body @{announceBy="R06书记"} -Role "R06"
    Assert "处分宣布（R06）" $r.ok $r.body

    # 执行
    $r = Req -Method POST -Url "$base/adjudication/$aid/execute" -Body @{executeOrg="第一党支部";executor="R22主任";executeReceipt="smoke执行回执：已扣发"} -Role "R22"
    Assert "处分执行（R22）" ($r.ok -and ($r.body -like "*已执行*")) $r.body

    # 以案促改
    $r = Req -Method POST -Url "$base/adjudication/$aid/reform" -Body @{reformMeasure="smoke完善招标流程"} -Role "R22"
    Assert "以案促改启动" $r.ok $r.body

    # 销号：缺佐证
    $r = Req -Method POST -Url "$base/adjudication/$aid/close" -Body @{closeBy="R06"} -Role "R06"
    Assert "E-DI-19 销号须佐证" (HasCode $r "E-DI-19") $r.body

    # 销号：正确
    $r = Req -Method POST -Url "$base/adjudication/$aid/close" -Body @{reformEvidence="smoke已整改并上传佐证";closeBy="R06书记"} -Role "R06"
    Assert "复核销号（R06）" ($r.ok -and ($r.body -like "*已结案*")) $r.body
}

# 5. 纪律教育 Education
Write-Host "`n--- 5. 纪律教育 ---" -ForegroundColor Yellow

# E-DI-21 必填缺失
$r = Req -Method POST -Url "$base/education" -Body @{title=""} -Role "R20"
Assert "E-DI-21 教育必填缺失" (HasCode $r "E-DI-21") $r.body

# 计划
$r = Req -Method POST -Url "$base/education" -Body @{
    title="smoke 2026年警示教育大会"; form="警示教育大会"; targetScope="全体党员"; planDate="2026-10-15"; educationType="警示教育"
} -Role "R20"
$ok5 = $r.ok -and ($r.body -like "*JY2026*")
Assert "教育计划（R20）" $ok5 $r.body
$eduId = 0
if ($ok5) { $eduId = (($r.body | ConvertFrom-Json).data).id }

if ($eduId -gt 0) {
    # 审批（R06）
    $r = Req -Method POST -Url "$base/education/$eduId/approve" -Body @{approveBy="R06书记"; pass=$true} -Role "R06"
    Assert "教育审批（R06）" $r.ok $r.body

    # 完成
    $r = Req -Method POST -Url "$base/education/$eduId/finish" -Body @{attendCount=85} -Role "R20"
    Assert "教育完成登记" ($r.ok -and ($r.body -like "*已完成*")) $r.body
}

# 纪法测试低参与率
$r = Req -Method POST -Url "$base/education" -Body @{
    title="smoke纪法测试2026"; form="纪法测试"; targetScope="全体党员"; planDate="2026-10-20"; educationType="纪法测试"
} -Role "R20"
$edu2Id = 0
if ($r.ok) { $edu2Id = (($r.body | ConvertFrom-Json).data).id }
if ($edu2Id -gt 0) {
    $r = Req -Method POST -Url "$base/education/$edu2Id/approve" -Body @{approveBy="R06"; pass=$true} -Role "R06"
    $r = Req -Method POST -Url "$base/education/$edu2Id/finish" -Body @{attendCount=80; testTotal=100; testActual=85; avgScore=88} -Role "R20"
    Assert "E-DI-22 测试参与率<90%橙色" ($r.ok -and ($r.body -like "*E-DI-22*")) $r.body
}

# E-DI-23 处分结案建议警示教育
if ($script:adjId -gt 0) {
    $r = Req -Method POST -Url "$base/education/suggest-from-case" -Body @{adjudicationId=$script:adjId; targetScope="全体党员"; planDate="2026-11-01"} -Role "R20"
    Assert "E-DI-23 处分案件生成警示教育建议" ($r.ok -and ($r.body -like "*警示教育*")) $r.body
}

# 6. 纪检驾驶舱 Stats
Write-Host "`n--- 6. 纪检驾驶舱 ---" -ForegroundColor Yellow

$r = Req -Method GET -Url "$base/stats/dashboard" -Role "R20"
Assert "驾驶舱六指标" ($r.ok -and ($r.body -like "*petitionTotal*" -and $r.body -like "*inInfluence*")) $r.body

# R99 不能看驾驶舱
$r = Req -Method GET -Url "$base/stats/dashboard" -Role "R99"
Assert "R99 驾驶舱被拒" (-not $r.ok) ""

# E-DI-25 导出未确认
$r = Req -Method GET -Url "$base/stats/export?type=petition&confirm=false&exportBy=R20" -Role "R20"
Assert "E-DI-25 导出未二次确认阻断" (HasCode $r "E-DI-25") $r.body

# 导出（确认）
$r = Req -Method GET -Url "$base/stats/export?type=petition&confirm=true&exportBy=R20" -Role "R20"
Assert "导出CSV（petition）" ($r.ok -and ($r.body -like "*JB*")) ($r.body.Substring(0,[Math]::Min(200,$r.body.Length)))

$r = Req -Method GET -Url "$base/stats/export?type=clue&confirm=true&exportBy=R06" -Role "R06"
Assert "导出CSV（clue）" ($r.ok -and ($r.body -like "*XS*")) ""

$r = Req -Method GET -Url "$base/stats/export?type=adjudication&confirm=true&exportBy=R06" -Role "R06"
Assert "导出CSV（adjudication）" ($r.ok -and ($r.body -like "*集团纪委*")) ""

# 总结
Write-Host "`n===== 结果 =====" -ForegroundColor Cyan
Write-Host "PASS: $pass  FAIL: $fail" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Red" })
if ($fail -gt 0) {
    Write-Host "`n失败明细:" -ForegroundColor Red
    $results | Where-Object { $_ -like "[FAIL]*" } | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}
exit $fail
