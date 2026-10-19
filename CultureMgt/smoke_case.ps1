# CCMS 子模块2 文化案例库 冒烟测试（UTF-8 BOM）
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

$longContent = '这是SMOKE案例正文，长度需要不少于八十字用于通过系统校验。事件发生在项目一线，团队按照核心价值观要求快速响应客户，过程材料完整、结果可验证，特提交评审作为全流程测试数据使用。'
$contrib = 'smoke-case-user'
$draftBody = @{
    caseTitle = 'SMOKE全流程评审案例：一线快速响应客户突发需求保障交付'
    valueTags = '以客户为中心'
    caseType = '正向标杆'
    occurTime = '2024-10-01T10:00:00'
    content = $longContent
    attachments = '["/files/case/smoke-evidence.pdf"]'
    isPublic = 1
    orgName = 'SMOKE测试部'
}

# 1. 广场可见性
$p1 = Send-Api 'GET' '/api/culture/case/square?publicOnly=true' 'C01' $null
Check '广场员工只见公开案例2条' ($p1.data.Count -eq 2) $p1.data.Count
$p2 = Send-Api 'GET' '/api/culture/case/square?publicOnly=false' 'C03' $null
Check '广场管理岗可见全部已发布3条(含内部反面)' ($p2.data.Count -eq 3) $p2.data.Count
$neg = ($p2.data | Where-Object { $_.caseType -eq '反面警示' })[0]
Check '反面案例强制内部不可见' ($neg.isPublic -eq 0) $neg.isPublic
$p3 = Send-Api 'GET' '/api/culture/case/square?publicOnly=true&type=正向标杆' 'C01' $null
Check '广场类型筛选正向2条' ($p3.data.Count -eq 2) $p3.data.Count

# 2. 标签配置
$tags = Send-Api 'GET' '/api/culture/case-tag/list?status=启用' 'C03' $null
Check '启用标签4个' ($tags.data.Count -eq 4) $tags.data.Count
$t = Send-Api 'POST' '/api/culture/case-tag' 'C01' @{ tagName = 'SMOKE无权标签'; scorePositive = 10 }
Check '标签创建角色拦截403' ($t.code -eq 403) ($t | ConvertTo-Json -Compress)
$t = Send-Api 'POST' '/api/culture/case-tag' 'C03' @{ tagName = 'SMOKE测试标签'; scorePositive = 12; scoreNegative = 6; scoreNews = 7; scoreTraining = 4 }
$tagId = $t.data.id
Check '标签创建成功' ($t.code -eq 200 -and $t.data.scorePositive -eq 12) ($t | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/case-tag/$tagId/toggle" 'C03' $null | Out-Null
$t2 = Send-Api 'GET' "/api/culture/case-tag/list" 'C03' $null
Check '标签停用生效' ((($t2.data | Where-Object { $_.id -eq $tagId })[0].status) -eq '停用') 'status mismatch'

# 3. 字段校验
$bad = $draftBody.Clone(); $bad.caseTitle = '短标题'; $bad.content = '短正文'
$r = Send-Api 'POST' '/api/culture/case' 'C01' $bad $contrib
Check '案例标题/正文字数拦截E-CC-01' ($r.code -eq 400 -and $r.message -like '*标题*') ($r | ConvertTo-Json -Compress)
$bad2 = $draftBody.Clone(); $bad2.occurTime = '2099-01-01T00:00:00'
$r = Send-Api 'POST' '/api/culture/case' 'C01' $bad2 $contrib
Check '未来发生时间拦截' ($r.code -eq 400 -and $r.message -like '*发生时间*') ($r | ConvertTo-Json -Compress)

# 4. 提交完整性：无附件草稿提交 E-CC-05
$noAtt = $draftBody.Clone(); $noAtt.attachments = '[]'
$r = Send-Api 'POST' '/api/culture/case' 'C01' $noAtt $contrib
$noAttId = $r.data.id
$r = Send-Api 'POST' "/api/culture/case/$noAttId/submit" 'C01' $null $contrib
Check '无附件提交拦截E-CC-05' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 5. 全流程三级评审 + 积分
$r = Send-Api 'POST' '/api/culture/case' 'C01' $draftBody $contrib
$caseId = $r.data.id
Check '案例草稿创建(投稿人)' ($r.code -eq 200 -and $r.data.status -eq '草稿') ($r | ConvertTo-Json -Compress)

$r = Send-Api 'PUT' "/api/culture/case/$caseId" 'C01' ($draftBody.Clone()) 'other-user'
Check '非本人编辑草稿拦截E-CC-04' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

$mine = Send-Api 'GET' '/api/culture/case/mine' 'C01' $null $contrib
Check '我的投稿含本人案例' (@($mine.data | Where-Object { $_.id -eq $caseId }).Count -eq 1) ("ids=" + (($mine.data | ForEach-Object { $_.id }) -join ','))

Send-Api 'POST' "/api/culture/case/$caseId/submit" 'C01' $null $contrib | Out-Null
$r = Send-Api 'POST' "/api/culture/case/$caseId/approve?pass=true" 'C02' $null $contrib
Check '部门初审角色错误403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/case/$caseId/approve?pass=true" 'C04' $null $contrib
Check '部门初审通过进复审' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/case/$caseId/approve?pass=true" 'C02' $null $contrib
Check '子公司复审通过进终审' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/case/$caseId/approve?pass=false" 'C03' $null $contrib
Check '终审驳回无意见拦截E-CC-03' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
# 上一条因意见校验未改状态，仍处待评审 stage3，直接终审通过
$r = Send-Api 'POST' "/api/culture/case/$caseId/approve?pass=true&opinion=同意发布" 'C03' $null $contrib
Check '集团终审发布' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/case/$caseId" 'C03' $null $contrib
Check '发布积分基础10' ($r.data.status -eq '已发布' -and $r.data.pointTotal -eq 10 -and $r.data.pointBase -eq 10) ($r | ConvertTo-Json -Compress)

# 6. 引用加分
$r = Send-Api 'POST' "/api/culture/case/$caseId/cite?type=news" 'C01' $null $contrib
Check '引用加分角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/case/$caseId/cite?type=news" 'C03' $null $contrib | Out-Null
$r = Send-Api 'GET' "/api/culture/case/$caseId" 'C03' $null $contrib
Check '新闻引用+5总15' ($r.data.pointTotal -eq 15 -and $r.data.refNewsCount -eq 1) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/case/$caseId/cite?type=training" 'C03' $null $contrib | Out-Null
$r = Send-Api 'GET' "/api/culture/case/$caseId" 'C03' $null $contrib
Check '培训引用+3总18' ($r.data.pointTotal -eq 18 -and $r.data.refTrainingCount -eq 1 -and $r.data.pointDetail -like '*合计18*') ($r | ConvertTo-Json -Compress)

# 7. 归档/恢复 + 广场复核
Send-Api 'POST' "/api/culture/case/$caseId/toggle-archive" 'C03' $null $contrib | Out-Null
$r = Send-Api 'GET' "/api/culture/case/$caseId" 'C03' $null $contrib
Check '案例归档不在广场' ($r.data.status -eq '已归档') $r.data.status
$pc = Send-Api 'GET' '/api/culture/case/square?publicOnly=false' 'C03' $null
Check '归档后广场仍3条' ($pc.data.Count -eq 3) $pc.data.Count
Send-Api 'POST' "/api/culture/case/$caseId/toggle-archive" 'C03' $null $contrib | Out-Null
$r = Send-Api 'GET' "/api/culture/case/$caseId" 'C03' $null $contrib
Check '案例恢复发布' ($r.data.status -eq '已发布') $r.data.status

# 8. 反面案例：公开开关被强制关闭 + 基础分5，终审后归档隐藏
$negBody = $draftBody.Clone()
$negBody.caseTitle = 'SMOKE反面警示案例：流程推诿导致客户诉求超期未闭环处理'
$negBody.caseType = '反面警示'; $negBody.isPublic = 1
$r = Send-Api 'POST' '/api/culture/case' 'C01' $negBody $contrib
$negId = $r.data.id
Check '反面案例草稿强制内部可见' ($r.code -eq 200 -and $r.data.isPublic -eq 0) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/case/$negId/submit" 'C01' $null $contrib | Out-Null
Send-Api 'POST' "/api/culture/case/$negId/approve?pass=true" 'C04' $null $contrib | Out-Null
Send-Api 'POST' "/api/culture/case/$negId/approve?pass=true" 'C02' $null $contrib | Out-Null
Send-Api 'POST' "/api/culture/case/$negId/approve?pass=true" 'C03' $null $contrib | Out-Null
$r = Send-Api 'GET' "/api/culture/case/$negId" 'C03' $null $contrib
Check '反面发布积分基础5且内部' ($r.data.status -eq '已发布' -and $r.data.pointBase -eq 5 -and $r.data.isPublic -eq 0) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/case/$negId/toggle-archive" 'C03' $null $contrib | Out-Null
# 冲烟收尾：正面案例也重新归档，保持广场基线2条
Send-Api 'POST' "/api/culture/case/$caseId/toggle-archive" 'C03' $null $contrib | Out-Null

# 9. 台账复核：待评审列表含种子 admin 案例
$pending = [uri]::EscapeDataString('待评审')
$wl = Send-Api 'GET' "/api/culture/case/list?status=$pending" 'C03' $null
Check '待评审台账含种子案例' (@($wl.data | Where-Object { $_.contributor -eq 'admin' }).Count -ge 1) ("contribs=" + (($wl.data | ForEach-Object { $_.contributor }) -join ','))

$pf = Send-Api 'GET' '/api/culture/case/square?publicOnly=true' 'C01' $null
Check '收尾广场公开案例仍2条' ($pf.data.Count -eq 2) $pf.data.Count

Write-Host ''
Write-Host "==== SMOKE RESULT: PASS=$script:pass FAIL=$script:fail ===="
if ($script:fail -gt 0) { $script:failMsgs | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }; exit 1 }
