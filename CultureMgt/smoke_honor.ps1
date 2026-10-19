# CCMS 子模块4 荣誉与榜样 冒烟测试（UTF-8 BOM，可重入）
$base = 'http://localhost:8160'
$script:pass = 0
$script:fail = 0
$script:failMsgs = @()

function Send-Api($method, $url, $role, $bodyObj, $userId) {
    $req = [System.Net.HttpWebRequest]::Create($base + $url)
    $req.Method = $method
    $req.Headers.Add('X-Culture-Role', $role)
    $req.Headers.Add('X-User-Id', $(if ($userId) { $userId } else { 'smoke-' + $role }))
    $req.ContentType = 'application/json; charset=utf-8'
    $req.Timeout = 10000
    if ($bodyObj) {
        $json = $bodyObj | ConvertTo-Json -Depth 8 -Compress
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
        $req.ContentLength = $bytes.Length
        $s = $req.GetRequestStream(); $s.Write($bytes, 0, $bytes.Length); $s.Close()
    }
    try { $resp = $req.GetResponse() } catch [System.Net.WebException] { $resp = $_.Exception.Response }
    $rs = $resp.GetResponseStream()
    $sr = New-Object System.IO.StreamReader($rs, [System.Text.Encoding]::UTF8)
    $txt = $sr.ReadToEnd(); $sr.Close(); $resp.Close()
    return $txt | ConvertFrom-Json
}

function Check($name, $cond, $detail) {
    if ($cond) { $script:pass++; Write-Host "[PASS] $name" }
    else { $script:fail++; $script:failMsgs += "$name :: $detail"; Write-Host "[FAIL] $name :: $detail" -ForegroundColor Red }
}

$deeds = 'SMOKE测试先进事迹：该员工本季度主动承接跨部门文化宣讲任务八场，覆盖新员工三百余人次；牵头整理价值观践行案例十二篇入选集团案例库，其中三篇被评为优秀标杆案例；同时组织部门文化开放日活动，推动团队文化认同度调研得分环比提升十二个百分点，事迹真实完整，达到荣誉申报标准要求。'

# ========== 一、荣誉项目配置 ==========
$r = Send-Api 'POST' '/api/culture/honor-project' 'C01' @{ projectName='SMOKE项目X'; periodType='年度'; applyType='个人'; applyStart='2026-01-01'; applyEnd='2026-12-31' } 'smoke-c01'
Check 'C01建项目403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$b = @{ projectName='SMOKE荣誉项目A'; periodType='双年度'; applyType='个人'; applyStart='2026-01-01'; applyEnd='2026-12-31'; autoSyncCase=1 }
$r = Send-Api 'POST' '/api/culture/honor-project' 'C03' $b 'smoke-c03'
Check '周期枚举拦截E-HO-01' ($r.code -eq 400 -and $r.message -like '*周期*') ($r | ConvertTo-Json -Compress)
$b.periodType = '年度'
$r = Send-Api 'POST' '/api/culture/honor-project' 'C03' $b 'smoke-c03'
Check 'C03建SMOKE项目成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$projId = $r.data.id
$r = Send-Api 'POST' '/api/culture/honor-project' 'C03' $b 'smoke-c03'
Check '项目名重复拦截E-HO-13' ($r.code -eq 400 -and $r.message -like '*已存在*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor-project/$projId/toggle" 'C07' $null 'smoke-c07'
Check 'C07停用项目' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$pl = Send-Api 'GET' '/api/culture/honor-project/list' 'C01' $null
$stopped = $pl.data | Where-Object { $_.id -eq $projId }
Check '项目状态已停用' (@($stopped).Count -eq 1 -and $stopped.status -eq '已停用') ($pl.data.Count)
$r = Send-Api 'POST' "/api/culture/honor-project/$projId/toggle" 'C07' $null 'smoke-c07'
Check 'C07恢复启用' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
# 团队类型项目B：供红线与驳回路径使用（个人项目会强制 candidate=本人，无法测指定对象）
$r = Send-Api 'POST' '/api/culture/honor-project' 'C03' @{ projectName='SMOKE荣誉项目B'; periodType='年度'; applyType='团队'; applyStart='2026-01-01'; applyEnd='2026-12-31'; autoSyncCase=0 } 'smoke-c03'
$projB = $r.data.id
Check 'C03建团队项目B' ($r.code -eq 200 -and $projB) ($r | ConvertTo-Json -Compress)

# ========== 二、申报草稿校验 ==========
$u = 'smoke-honor-user'
$bad = @{ projectId=$projId; userDept='SMOKE测试部'; deeds='事迹太短'; attachments='[]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $bad $u
Check '事迹不足100字E-HO-02' ($r.code -eq 400 -and $r.message -like '*100*') ($r | ConvertTo-Json -Compress)
$bad2 = @{ projectId=$projId; userDept='SMOKE测试部'; deeds=$deeds; attachments='[]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $bad2 $u
Check '附件必填E-HO-12' ($r.code -eq 400 -and $r.message -like '*附件*') ($r | ConvertTo-Json -Compress)
$ok = @{ projectId=$projId; userDept='SMOKE测试部'; deeds=$deeds; attachments='[{"name":"smoke-proof.pdf","size":"1MB"}]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $ok $u
Check '个人申报candidate自动本人' ($r.code -eq 200 -and $r.data.candidate -eq $u) ($r | ConvertTo-Json -Compress)
$applyId = $r.data.id
$ok2 = $ok.Clone(); $ok2.deeds = $deeds + ' 补充：另主导完成一次价值观辩论赛组织工作，效果良好。'
$r = Send-Api 'PUT' "/api/culture/honor/$applyId" 'C01' $ok2 $u
Check '草稿可编辑' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/submit" 'C05' $null 'other-user'
Check '非本人提交被拦截' ($r.code -eq 403 -or ($r.code -eq 400 -and $r.message -like '*仅本人*')) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/submit" 'C01' $null $u
Check '本人提交成功已申报' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/submit" 'C01' $null $u
Check '重复提交仅草稿E-HO-14' ($r.code -eq 400 -and $r.message -like '*草稿*') ($r | ConvertTo-Json -Compress)

# 重复申报拦截：同项目同对象第二条
$dup = @{ projectId=$projId; userDept='SMOKE测试部'; deeds=$deeds; attachments='[{"name":"d.pdf","size":"1MB"}]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $dup $u
$dupId = $r.data.id
$r = Send-Api 'POST' "/api/culture/honor/$dupId/submit" 'C01' $null $u
Check '重复申报拦截E-HO-06' ($r.code -eq 400 -and $r.message -like '*重复申报*') ($r | ConvertTo-Json -Compress)

# ========== 三、红线否决（团队项目B + 指定对象 sunqi，其存在反面警示案例） ==========
$red = @{ projectId=$projB; applyType='团队'; candidate='sunqi'; userDept='SMOKE测试部'; deeds=$deeds; attachments='[{"name":"r.pdf","size":"1MB"}]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $red $u
$redId = $r.data.id
$r = Send-Api 'POST' "/api/culture/honor/$redId/submit" 'C01' $null $u
Check '红线违规拦截E-HO-03' ($r.code -eq 400 -and $r.message -like '*红线*') ($r | ConvertTo-Json -Compress)

# ========== 四、评审链全流程 ==========
$r = Send-Api 'POST' "/api/culture/honor/$applyId/first-review?pass=true&opinion=好" 'C02' $null 'smoke-c02'
Check 'C02不可初审403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/first-review?pass=true&opinion=好" 'C04' $null 'smoke-c04'
Check '初审意见不足5字E-HO-07' ($r.code -eq 400 -and $r.message -like '*5字*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/first-review?pass=true&opinion=SMOKE初审通过，事迹扎实。" 'C04' $null 'smoke-c04'
Check 'C04初审通过' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/review?pass=true" 'C02' $null 'smoke-c02'
Check 'C02复核通过进评审中' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$det = Send-Api 'GET' "/api/culture/honor/$applyId" 'C02' $null
Check '文化综合得分自动回填' ($det.data.cultureScore -ne $null) ($det | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/publicize?start=2026-10-01&end=2026-09-01" 'C02' $null 'smoke-c02'
Check '公示止早于起E-HO-08' ($r.code -eq 400 -and $r.message -like '*公示*') ($r | ConvertTo-Json -Compress)
$s = (Get-Date).AddDays(-1).ToString('yyyy-MM-dd'); $e = (Get-Date).AddDays(5).ToString('yyyy-MM-dd')
$r = Send-Api 'POST' "/api/culture/honor/$applyId/publicize?start=$s&end=$e" 'C02' $null 'smoke-c02'
Check 'C02发起公示' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/objection" 'C01' @{ reason='短' } $u
Check '异议理由不足E-HO-09' ($r.code -eq 400 -and $r.message -like '*5字*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/objection" 'C01' @{ reason='SMOKE测试异议：事迹中数据需进一步核实。' } $u
Check 'C01公示期提异议' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$pub = Send-Api 'GET' '/api/culture/honor/publicity' 'C01' $null
Check '公示列表含该申报' (@($pub.data | Where-Object { $_.id -eq $applyId }).Count -eq 1) ($pub.data.Count)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/finalize" 'C07' @{ awardTime=((Get-Date).AddDays(7).ToString('yyyy-MM-dd')); certFiles='[{"name":"cert.pdf","size":"500KB"}]'; syncCase=$true } 'smoke-c07'
Check 'C07终审表彰' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$applyId/finalize" 'C07' @{ awardTime='2026-12-01' } 'smoke-c07'
Check '重复终审E-HO-10' ($r.code -eq 400 -and $r.message -like '*公示中*') ($r | ConvertTo-Json -Compress)

# ========== 五、驳回路径（团队项目B 独立对象，避免与在办重复拦截冲突） ==========
$rj = @{ projectId=$projB; applyType='团队'; candidate='SMOKE团队B'; userDept='SMOKE测试部'; deeds=$deeds; attachments='[{"name":"b.pdf","size":"1MB"}]' }
$r = Send-Api 'POST' '/api/culture/honor' 'C01' $rj $u
$rjId = $r.data.id
$r = Send-Api 'POST' "/api/culture/honor/$rjId/submit" 'C03' $null 'smoke-c03'
Check '文化岗可代提交' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/honor/$rjId/first-review?pass=false&opinion=SMOKE驳回测试：事迹佐证不足。" 'C04' $null 'smoke-c04'
Check '初审驳回' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$det2 = Send-Api 'GET' "/api/culture/honor/$rjId" 'C04' $null
Check '状态评审驳回' ($det2.data.status -eq '评审驳回') ($det2 | ConvertTo-Json -Compress)

# ========== 六、广场与列表 ==========
$sq = Send-Api 'GET' '/api/culture/honor/square' 'C01' $null
Check '广场含SMOKE表彰' (@($sq.data | Where-Object { $_.candidate -eq $u }).Count -eq 1) ($sq.data.Count)
$ls = Send-Api 'GET' '/api/culture/honor/list' 'C01' $null
Check 'C01管理列表403' ($ls.code -eq 403) ($ls | ConvertTo-Json -Compress)
$ls2 = Send-Api 'GET' '/api/culture/honor/list' 'C04' $null
Check 'C04管理列表200' ($ls2.code -eq 200) ($ls2 | ConvertTo-Json -Compress)
$mine = Send-Api 'GET' '/api/culture/honor/mine' 'C01' $null 'smoke-honor-user'
Check '我的申报含本人记录' (@($mine.data | Where-Object { $_.contributor -eq 'smoke-honor-user' }).Count -ge 2) ($mine.data.Count)

# ========== 清理 ==========
$m='C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
& $m -uroot -p"Sun-0126King-0325" --default-character-set=utf8mb4 -e "UPDATE culture_mgt.cul_honor_apply SET deleted=1 WHERE contributor LIKE 'smoke-%'; UPDATE culture_mgt.cul_honor_project SET deleted=1 WHERE project_name LIKE 'SMOKE%'; UPDATE culture_mgt.cul_case SET deleted=1 WHERE case_title LIKE '%SMOKE%'; DELETE FROM culture_mgt.cul_audit_log WHERE operator LIKE 'smoke-%';" 2>$null
Write-Output "cleanup done"

Write-Output "========== RESULT: PASS=$script:pass FAIL=$script:fail =========="
if ($script:fail -gt 0) { $script:failMsgs | ForEach-Object { Write-Output "  FAIL: $_" } }
