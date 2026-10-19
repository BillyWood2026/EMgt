﻿# 第10章 党建考核评价 冒烟脚本（可重入）
$base = "http://localhost:8150/api/party/assessment"

# 重置考核任务状态（可重入）
& "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -uroot -pSun-0126King-0325 party_affairs_mgt -e "
DELETE FROM assess_self_eval WHERE task_id IN (1,2);
DELETE FROM assess_check WHERE task_id IN (1,2);
DELETE FROM assess_score WHERE task_id IN (1,2);
DELETE FROM assess_appeal WHERE task_id IN (1,2);
DELETE FROM assess_rectify WHERE task_id IN (1,2);
UPDATE assess_task SET status='待自评',self_submit_time=NULL,check_time=NULL,score_time=NULL,approve_time=NULL,approver=NULL,approve_opinion=NULL,appeal_deadline=NULL,publish_time=NULL,total_score=NULL,adjust_score=0,grade=NULL,veto_triggered=0,rank_no=NULL WHERE id IN (1,2);
DELETE FROM assess_scheme WHERE scheme_name LIKE 'SMK-%';
" 2>$null | Out-Null

function PostJson($url, $obj, $role = "R02") {
    $h = @{ "X-Party-Role" = $role; "Content-Type" = "application/json" }
    if ($null -eq $obj) { $json = '{}' } else { $json = ConvertTo-Json -InputObject $obj -Depth 10 -Compress }
    $bytes = [Text.Encoding]::UTF8.GetBytes($json)
    try { return Invoke-RestMethod $url -Method Post -Headers $h -Body $bytes -TimeoutSec 15 }
    catch {
        $raw = $null
        try { $sr = New-Object IO.StreamReader($_.Exception.Response.GetResponseStream(), [Text.Encoding]::UTF8); $raw = $sr.ReadToEnd() } catch {}
        if ($raw) { try { return ($raw | ConvertFrom-Json) } catch { return @{ code = -1; msg = $raw } } }
        return @{ code = -1; msg = $_.Exception.Message }
    }
}
function GetJson($url, $role = "R02") {
    $h = @{ "X-Party-Role" = $role }
    try { return Invoke-RestMethod $url -Headers $h -TimeoutSec 15 }
    catch {
        $raw = $null
        try { $sr = New-Object IO.StreamReader($_.Exception.Response.GetResponseStream(), [Text.Encoding]::UTF8); $raw = $sr.ReadToEnd() } catch {}
        if ($raw) { try { return ($raw | ConvertFrom-Json) } catch { return @{ code = -1; msg = $raw } } }
        return @{ code = -1; msg = $_.Exception.Message }
    }
}
$script:pass = 0; $script:fail = 0
function ExpectCode($resp, $code, $name) {
    if ($resp.code -eq $code) { $script:pass++; echo "PASS $name" } else { $script:fail++; echo "FAIL $name expected=$code got=$($resp.code) msg=$($resp.msg)" }
}
function ExpectBlock($resp, $code, $name) {
    $hit = ($resp.code -eq 400 -and ("$($resp.msg)" -match $code -or "$($resp.data.errorCode)" -eq $code))
    if ($hit) { $script:pass++; echo "PASS $name" } else { $script:fail++; echo "FAIL $name expected=$code got=$(ConvertTo-Json -InputObject $resp -Compress -Depth 5)" }
}
function Expect403($resp, $name) {
    if ($resp.code -ne 200) { $script:pass++; echo "PASS $name" } else { $script:fail++; echo "FAIL $name expected-403" }
}

echo "=== 10.1 indicator ==="
ExpectCode (GetJson "$base/indicator/list?level=1") 200 "indicator-list-L1"
ExpectCode (GetJson "$base/indicator/list?level=2") 200 "indicator-list-L2"
ExpectCode (GetJson "$base/indicator/list?level=1" "R99") 200 "indicator-read-R99"
Expect403 (PostJson "$base/indicator" @{ indicatorCode = "L2-99"; indicatorName = "x"; level = 2; parentCode = "L1-01"; weight = 1 } "R99") "indicator-save-R99-403"
ExpectBlock (PostJson "$base/indicator" @{ indicatorCode = "L2-05"; indicatorName = "dup"; level = 2; parentCode = "L1-03" } "R02") "E-AS-01" "indicator-dup-code"
ExpectBlock (PostJson "$base/indicator" @{ indicatorCode = "L2-98"; indicatorName = "noparent"; level = 2; parentCode = "NOPE" } "R02") "E-AS-01" "indicator-bad-parent"

# 动态末级指标ID（避免自增漂移）
$inds = (GetJson "$base/indicator/list?level=2").data
$indIds = @($inds | ForEach-Object { $_.id })

echo "=== 10.2 scheme ==="
ExpectCode (GetJson "$base/scheme/list") 200 "scheme-list"
ExpectCode (GetJson "$base/scheme/list" "R99") 200 "scheme-read-R99"
Expect403 (PostJson "$base/scheme" @{ schemeName = "x" } "R99") "scheme-save-R99-403"
ExpectBlock (PostJson "$base/scheme" @{ schemeName = "" } "R02") "E-AS-03" "scheme-empty-name"
$r = PostJson "$base/scheme" @{ schemeName = "SMK-EMPTY-SCOPE"; targetScope = "dangzhibu"; assessYear = 2099 } "R02"
ExpectCode $r 200 "scheme-save-draft"
$sid = $r.data.id
ExpectBlock (PostJson "$base/scheme/$sid/publish" $indIds "R02") "E-AS-03" "scheme-publish-empty-scope"
$r = PostJson "$base/scheme" @{ schemeName = "SMK-BAD-WEIGHT"; targetScope = "dangzhibu"; targetOrgIds = "1"; targetOrgNames = "org1"; assessYear = 2099 } "R02"
$sid2 = $r.data.id
ExpectBlock (PostJson "$base/scheme/$sid2/publish" @($indIds[0], $indIds[1]) "R02") "E-AS-02" "scheme-weight-ne100"
$r = PostJson "$base/scheme" @{ schemeName = "SMK-OVERLAP"; targetScope = "党支部"; targetOrgIds = "9"; targetOrgNames = "org9"; assessYear = 2026 } "R02"
$sid3 = $r.data.id
$r = PostJson "$base/scheme/$sid3/publish?force=false" $indIds "R02"
ExpectBlock $r "E-AS-04" "scheme-overlap-warn"
$r = PostJson "$base/scheme/$sid3/publish?force=true" $indIds "R02"
ExpectCode $r 200 "scheme-overlap-force-publish"

echo "=== 10.3 process ==="
ExpectCode (GetJson "$base/process/task/list?schemeId=1") 200 "task-list"
ExpectBlock (PostJson "$base/process/2/submit-self" $null "R07") "E-AS-11" "self-submit-incomplete"
foreach ($i in $inds) { PostJson "$base/process/self-eval/save?taskId=2" @{ indicatorId = $i.id; selfScore = 85; selfDesc = "smk" } "R07" | Out-Null }
ExpectCode (PostJson "$base/process/2/submit-self" $null "R07") 200 "self-submit-ok"
# task1 自评：填满13项并提交
foreach ($i in $inds) { PostJson "$base/process/self-eval/save?taskId=1" @{ indicatorId = $i.id; selfScore = 90; selfDesc = "smk" } "R07" | Out-Null }
ExpectCode (PostJson "$base/process/1/submit-self" $null "R07") 200 "self-submit-task1"
PostJson "$base/process/check/save?taskId=1" @{ indicatorId = $indIds[0]; checkScore = 88 } "R04" | Out-Null
ExpectBlock (PostJson "$base/process/1/finish-check" $null "R04") "E-AS-06" "finish-check-empty-opinion"
foreach ($i in $inds) { PostJson "$base/process/check/save?taskId=1" @{ indicatorId = $i.id; checkScore = 88; checkOpinion = "verified"; vetoHit = 0 } "R04" | Out-Null }
ExpectCode (PostJson "$base/process/1/finish-check" $null "R04") 200 "finish-check-ok"
ExpectBlock (PostJson "$base/process/1/finish-check" $null "R04") "E-AS-11" "finish-check-wrong-status"
# 综合评分：每项同时给 judgeScores 与 autoScore（定性取前者，定量/加减分取后者，规避PS中文比较）
$scores = @()
foreach ($i in $inds) { $scores += @{ indicatorId = $i.id; judgeScores = "85,90,88"; autoScore = 88 } }
$r = PostJson "$base/process/1/finish-score?adjustScore=8" $scores "R04"
ExpectCode $r 200 "finish-score-ok"
if ("$($r.data.adjustScore)" -eq "5.00" -or "$($r.data.adjustScore)" -eq "5") { $script:pass++; echo "PASS adjust-cap-truncated(E-AS-12)" } else { $script:fail++; echo "FAIL adjust-cap got=$($r.data.adjustScore)" }
# 一票否决：task2 核查时否决指标 vetoHit=1
foreach ($i in $inds) { $vh = $(if ($i.vetoFlag -eq 1) { 1 } else { 0 }); PostJson "$base/process/check/save?taskId=2" @{ indicatorId = $i.id; checkScore = 90; checkOpinion = "verified"; vetoHit = $vh } "R04" | Out-Null }
PostJson "$base/process/2/finish-check" $null "R04" | Out-Null
$scores2 = @(); foreach ($i in $inds) { $scores2 += @{ indicatorId = $i.id; judgeScores = "95,96,94"; autoScore = 95 } }
$r = PostJson "$base/process/2/finish-score?adjustScore=0" $scores2 "R04"
if ($r.data.vetoTriggered -eq 1 -and $r.data.grade) { $script:pass++; echo "PASS veto-E-AS-08 grade-set" } else { $script:fail++; echo "FAIL veto got=$($r.data.vetoTriggered)" }
ExpectCode (PostJson "$base/process/1/approve?approver=dw&opinion=agree" $null "R03") 200 "approve-task1"
ExpectCode (PostJson "$base/process/2/approve?approver=dw&opinion=agree" $null "R03") 200 "approve-task2"
Expect403 (PostJson "$base/process/1/approve?approver=x" $null "R07") "approve-R07-403"
$r = PostJson "$base/process/1/appeal?reason=score-wrong" $null "R07"
ExpectCode $r 200 "appeal-submit"
$aid = $r.data.id
ExpectCode (PostJson "$base/process/appeal/$aid/handle?upheld=true&newScore=92.5&handleResult=upheld&handleBy=dw" $null "R02") 200 "appeal-handle"
ExpectBlock (PostJson "$base/result/1/publish" $null "R02") "E-AS-10" "publish-appeal-period"
& "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe" -uroot -pSun-0126King-0325 party_affairs_mgt -e "UPDATE assess_task SET appeal_deadline='2026-01-01' WHERE id IN (1,2);" 2>$null | Out-Null
$r = PostJson "$base/result/1/publish" $null "R02"
ExpectCode $r 200 "publish-ok"
echo "  published=$($r.data.published) excellentMax=$($r.data.excellentMax)"

echo "=== 10.4 algorithm ==="
ExpectCode (GetJson "$base/algorithm/linear-map?ratio=80&maxScore=10") 200 "algo-linear"
$r = GetJson "$base/algorithm/trim-mean?values=85,90,88,92,80"
if ("$($r.data.mean)" -eq "87.67") { $script:pass++; echo "PASS algo-trim-mean=87.67" } else { $script:fail++; echo "FAIL trim-mean got=$($r.data.mean)" }

echo "=== 10.5 result/rectify ==="
ExpectCode (GetJson "$base/result/rectify/list") 200 "rectify-list"
$r = PostJson "$base/result/rectify/add" @{ taskId = 1; problem = "smk-problem"; dutyPerson = "zhangsan"; deadline = "2026-12-31" } "R04"
ExpectCode $r 200 "rectify-add"
$rid = $r.data.id
ExpectBlock (PostJson "$base/result/rectify/$rid/close?closeBy=li" $null "R04") "E-AS-11" "rectify-close-no-proof"
ExpectCode (PostJson "$base/result/rectify/$rid/finish?proof=done.pdf" $null "R07") 200 "rectify-finish"
ExpectCode (PostJson "$base/result/rectify/$rid/close?closeBy=li" $null "R04") 200 "rectify-close"

echo "=== 403 matrix ==="
ExpectCode (GetJson "$base/scheme/list" "R99") 200 "R99-read-ok"
Expect403 (PostJson "$base/scheme" @{ schemeName = "x" } "R99") "R99-write-403"
Expect403 (PostJson "$base/process/check/save?taskId=1" @{ indicatorId = 1 } "R08") "R08-check-403"
Expect403 (PostJson "$base/result/rectify/add" @{ taskId = 1; problem = "x" } "R99") "R99-rectify-403"

echo ""
echo "=== TOTAL: pass=$script:pass fail=$script:fail ==="
