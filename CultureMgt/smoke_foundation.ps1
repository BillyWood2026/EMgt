# CCMS 子模块1 文化底座 冒烟测试（UTF-8 BOM）
$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8160'
$script:pass = 0
$script:fail = 0
$script:failMsgs = @()

function Send-Api($method, $url, $role, $bodyObj) {
    $req = [System.Net.HttpWebRequest]::Create($base + $url)
    $req.Method = $method
    $req.Headers.Add('X-Culture-Role', $role)
    $req.Headers.Add('X-User-Id', 'smoke-' + $role)
    $req.ContentType = 'application/json; charset=utf-8'
    $req.Timeout = 10000
    if ($bodyObj) {
        $json = $bodyObj | ConvertTo-Json -Depth 8 -Compress
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
        $req.ContentLength = $bytes.Length
        $s = $req.GetRequestStream(); $s.Write($bytes, 0, $bytes.Length); $s.Close()
    }
    try {
        $resp = $req.GetResponse()
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
    }
    $rs = $resp.GetResponseStream()
    $sr = New-Object System.IO.StreamReader($rs, [System.Text.Encoding]::UTF8)
    $txt = $sr.ReadToEnd(); $sr.Close(); $resp.Close()
    return $txt | ConvertFrom-Json
}

function Check($name, $cond, $detail) {
    if ($cond) { $script:pass++; Write-Host ("[PASS] " + $name) }
    else { $script:fail++; $script:failMsgs += $name + ' :: ' + $detail; Write-Host ("[FAIL] " + $name + ' :: ' + $detail) -ForegroundColor Red }
}

# 1. 全景聚合（C01 可读）
$p = Send-Api 'GET' '/api/culture/foundation/panorama/overview' 'C01' $null
Check '全景200' ($p.code -eq 200) ($p | ConvertTo-Json -Compress)
Check '全景含生效纲领V1.0' ($p.data.activeCreed.version -eq 'V1.0') $p.data.activeCreed.version
Check '全景里程碑2条' ($p.data.milestones.Count -eq 2) $p.data.milestones.Count
Check '全景生效红线2条' ($p.data.redlines.Count -eq 2) $p.data.redlines.Count
Check '全景重大红线数1' ($p.data.redlineLevelCount.'重大' -eq 1) ($p.data.redlineLevelCount | ConvertTo-Json -Compress)
Check '全景一票否决数1' ($p.data.vetoCount -eq 1) $p.data.vetoCount
Check '全景生效制度2份' ($p.data.institutions.Count -eq 2) $p.data.institutions.Count
Check '全景释义2条' ($p.data.glossaries.Count -eq 2) $p.data.glossaries.Count

# 2. 纲领：字段校验拦截
$bad = @{ creedName = 'SMOKE'; mission = '短'; vision = '短'; valueGroups = '[]'; applyOrg = ''; effectDate = '2025-01-01' }
$r = Send-Api 'POST' '/api/culture/foundation/creed' 'C03' $bad
Check '纲领字段不足拦截E-CUL-01' ($r.code -eq 400 -and $r.message -like '*使命*') ($r | ConvertTo-Json -Compress)

# 3. 纲领三级审批全链路（独立测试组织，不影响种子母版）
$creed = @{
    creedName = 'SMOKE测试纲领'
    mission = '这是SMOKE测试用的企业使命文本，长度不少于十字用于通过字段校验，聚焦客户价值与长期奋斗。'
    vision = '这是SMOKE测试用的企业愿景文本，成为受客户尊重、员工自豪、行业领先的优秀组织，持续创造价值。'
    valueGroups = '[{"title":"测试价值观","clauses":["测试条款一","测试条款二"]}]'
    spirit = '测试精神'; slogan = '测试口号'
    applyOrg = 'SMOKE测试子公司'; effectDate = '2025-01-01'
}
$r = Send-Api 'POST' '/api/culture/foundation/creed' 'C03' $creed
$creedId = $r.data.id
Check '纲领草稿创建' ($r.code -eq 200 -and $creedId -gt 0 -and $r.data.status -eq '草稿') ($r | ConvertTo-Json -Compress)

$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/submit" 'C03' $null
Check '纲领提交待审' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/approve?pass=true" 'C04' $null
Check '纲领节点1角色错误403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/approve?pass=true" 'C05' $null
Check '纲领人力初审通过进节点2' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/approve?pass=true" 'C06' $null
Check '纲领法务审核通过进节点3' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/approve?pass=false" 'C07' $null
Check '纲领驳回无意见拦截E-CUL-04' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/creed/$creedId/approve?pass=true&opinion=同意发布" 'C07' $null
Check '纲领高管终审生效' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 驳回后单据已退回草稿？验证状态机：上一步驳回退回草稿，随后又走完 → 当前应为生效
$r = Send-Api 'GET' "/api/culture/foundation/creed/$creedId" 'C03' $null
Check '纲领最终状态生效且审批日志3节点' ($r.data.status -eq '生效' -and $r.data.approvalLog -like '*"stage":3*') $r.data.status

# 生效后再编辑应被拒
$creedEdit = @{
    creedName = 'SMOKE测试纲领'; mission = $creed.mission; vision = $creed.vision
    valueGroups = $creed.valueGroups; spirit = '尝试修改'; slogan = '测试口号'
    applyOrg = 'SMOKE测试子公司'; effectDate = '2025-01-01'
}
$r = Send-Api 'PUT' "/api/culture/foundation/creed/$creedId" 'C03' $creedEdit
Check '生效纲领只读E-CUL-06' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 4. 制度学习 + 合规覆盖率
$il = Send-Api 'GET' '/api/culture/foundation/institution/list?status=生效' 'C03' $null
$instId = ($il.data | Where-Object { $_.docName -eq '员工行为规范管理制度' })[0].id
$r = Send-Api 'POST' "/api/culture/foundation/institution/$instId/learn" 'C01' @{ userName = 'smoke_user'; orgName = 'SMOKE测试部' }
Check '制度学习确认' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/institution/$instId/learn" 'C01' @{ userName = 'smoke_user' }
Check '制度学习幂等' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/foundation/institution/$instId/compliance" 'C03' $null
Check '合规覆盖率60%' ([math]::Abs([double]$r.data.rate - 60.0) -lt 0.01) ($r | ConvertTo-Json -Compress)

# 制度修订复制新版本
$r = Send-Api 'POST' "/api/culture/foundation/institution/$instId/revise?reason=SMOKE测试修订原因不少于五个字" 'C03' $null
Check '制度修订生成新版本' ($r.code -eq 200 -and $r.data.version -eq 'V1.3' -and $r.data.status -eq '草稿') ($r | ConvertTo-Json -Compress)

# 制度同分类重名
$dup = @{ docName = '员工行为规范管理制度'; docType = '行为规范类'; summary = ('制度摘要' * 30); keyClause = '关键条款必须填写'; fileUrl = '/x.pdf'; effectDate = '2025-01-01' }
$r = Send-Api 'POST' '/api/culture/foundation/institution' 'C03' $dup
Check '制度重名拦截E-CUL-05' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 5. 红线：角色鉴权 + 编号自动 + 重大自动否决 + 终审 + 归档
$r = Send-Api 'POST' '/api/culture/foundation/redline' 'C01' @{ category = '廉洁诚信'; violationLevel = '重大'; description = '普通员工无权创建红线条款测试用例描述文本'; punishStd = '处罚标准不少于五个字' }
Check '红线创建角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

$rl = @{ category = '廉洁诚信'; violationLevel = '重大'; description = 'SMOKE测试重大红线违规情形描述，用于验证一票否决自动携带与锁定逻辑。'; punishStd = 'SMOKE测试处罚标准文本不少于五个字'; scopeText = '全员' }
$r = Send-Api 'POST' '/api/culture/foundation/redline' 'C03' $rl
$rlId = $r.data.id
Check '红线草稿创建并自动编号HX年份' ($r.code -eq 200 -and $r.data.code -like ('HX' + (Get-Date).Year + '*')) ($r | ConvertTo-Json -Compress)
Check '重大红线自动携带一票否决' ($r.data.vetoFlag -eq 1) $r.data.vetoFlag

Send-Api 'POST' "/api/culture/foundation/redline/$rlId/submit" 'C03' $null | Out-Null
$r = Send-Api 'POST' "/api/culture/foundation/redline/$rlId/approve?pass=true" 'C03' $null
Check '红线文化审核通过' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/redline/$rlId/approve?pass=true" 'C05' $null
Check '红线人力合规复核通过' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/redline/$rlId/approve?pass=true" 'C07' $null
Check '红线高管终审生效' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/foundation/redline/$rlId" 'C03' $null
Check '生效红线否决标记保持1' ($r.data.status -eq '生效' -and $r.data.vetoFlag -eq 1) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/redline/$rlId/archive?reason=SMOKE测试数据归档清理" 'C07' $null
Check '红线归档不物理删除' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 6. 释义：C02编制→C03审核→生效→归档
$g = @{ valueTag = 'SMOKE测试释义标签'; positiveText = 'SMOKE测试正向释义文本不少于十字用于通过校验逻辑。'; negativeText = 'SMOKE测试反面警示说明文本。'; applyOrg = 'SMOKE测试子公司' }
$r = Send-Api 'POST' '/api/culture/foundation/glossary' 'C02' $g
$gId = $r.data.id
Check '释义草稿创建' ($r.code -eq 200 -and $gId -gt 0) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/foundation/glossary/$gId/submit" 'C02' $null | Out-Null
$r = Send-Api 'POST' "/api/culture/foundation/glossary/$gId/approve?pass=false" 'C02' $null
Check '释义审核角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/glossary/$gId/approve?pass=true" 'C03' $null
Check '释义C03审核生效' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/foundation/glossary/$gId/archive" 'C03' $null | Out-Null

# 7. 里程碑：前台开关切换并恢复
$ml = Send-Api 'GET' '/api/culture/foundation/milestone/list?status=启用&front=1' 'C03' $null
$mId = $ml.data[0].id
$r = Send-Api 'POST' "/api/culture/foundation/milestone/$mId/toggle?action=frontOff" 'C03' $null
Check '里程碑关闭前台' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/milestone/$mId/toggle?action=frontOn" 'C03' $null
Check '里程碑恢复前台' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/foundation/milestone/$mId/toggle?action=frontOn" 'C01' $null
Check '里程碑维护角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 8. 全景复核：种子数据未被污染（生效红线仍为2）
$p2 = Send-Api 'GET' '/api/culture/foundation/panorama/overview' 'C01' $null
Check '复核全景生效红线仍2条' ($p2.data.redlines.Count -eq 2) $p2.data.redlines.Count
Check '复核全景母版仍V1.0' ($p2.data.activeCreed.version -eq 'V1.0') $p2.data.activeCreed.version

Write-Host ''
Write-Host ("==== SMOKE RESULT: PASS=" + $script:pass + " FAIL=" + $script:fail + " ====")
if ($script:fail -gt 0) { $script:failMsgs | ForEach-Object { Write-Host (" - " + $_) -ForegroundColor Yellow }; exit 1 }
