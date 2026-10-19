# CCMS 子模块3 文化活动与阵地 冒烟测试（UTF-8 BOM）
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

$u = 'smoke-act-user'
$ps = (Get-Date).AddDays(10).ToString('yyyy-MM-ddTHH:mm:ss')
$pe = (Get-Date).AddDays(11).ToString('yyyy-MM-ddTHH:mm:ss')
$sd = (Get-Date).AddDays(8).ToString('yyyy-MM-ddTHH:mm:ss')
$actBody = @{
    actName = 'SMOKE测试活动：价值观读书会全流程验证用例'
    actType = '价值观读书会'
    organizerDept = 'SMOKE测试部'
    planStart = $ps; planEnd = $pe; signDeadline = $sd
    maxSign = 50; shouldCount = 30
    intro = '冒烟测试活动简介，用于全流程验证。'
}

# 1. 广场
$sq = Send-Api 'GET' '/api/culture/activity/square' 'C01' $null
Check '广场返回已发布+进行中2场' ($sq.data.Count -eq 2) $sq.data.Count
$t = [uri]::EscapeDataString('价值观读书会')
$sq2 = Send-Api 'GET' "/api/culture/activity/square?type=$t" 'C01' $null
Check '广场类型筛选读书会1场' ($sq2.data.Count -eq 1) $sq2.data.Count

# 2. 字段校验
$b = $actBody.Clone(); $b.actName = '短名'
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $b $u
Check '活动名称字数拦截E-AC-01' ($r.code -eq 400 -and $r.message -like '*10-100*') ($r | ConvertTo-Json -Compress)
$b = $actBody.Clone(); $b.actType = '聚餐'
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $b $u
Check '活动类型枚举拦截' ($r.code -eq 400 -and $r.message -like '*类型*') ($r | ConvertTo-Json -Compress)
$b = $actBody.Clone(); $b.planStart = $pe; $b.planEnd = $ps
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $b $u
Check '结束早于开始拦截' ($r.code -eq 400 -and $r.message -like '*结束*') ($r | ConvertTo-Json -Compress)
$b = $actBody.Clone(); $b.signDeadline = $pe
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $b $u
Check '报名截止晚于开始拦截' ($r.code -eq 400 -and $r.message -like '*截止*') ($r | ConvertTo-Json -Compress)

# 3. 全流程
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $actBody $u
$aid = $r.data.id
Check '活动草稿创建(经办人)' ($r.code -eq 200 -and $r.data.status -eq '草稿') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'PUT' "/api/culture/activity/$aid" 'C01' $actBody 'other-user'
Check '非经办人编辑拦截E-AC-04' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/activity/$aid/submit" 'C01' $null $u | Out-Null
$r = Send-Api 'POST' "/api/culture/activity/$aid/approve?pass=true" 'C02' $null $u
Check '节点1角色错误403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/activity/$aid/approve?pass=true" 'C04' $null $u | Out-Null
$r = Send-Api 'POST' "/api/culture/activity/$aid/approve?pass=false" 'C02' $null $u
Check '驳回无意见拦截E-AC-03' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/activity/$aid/approve?pass=true&opinion=%E5%90%8C%E6%84%8F" 'C02' $null $u
Check '二级审批通过已发布' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '活动状态已发布' ($r.data.status -eq '已发布') $r.data.status

# 4. 报名签到
$r = Send-Api 'POST' "/api/culture/activity-sign/$aid/sign" 'C01' @{ userDept = 'SMOKE测试部' } $u
Check '员工报名成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/activity-sign/$aid/sign" 'C01' @{ userDept = 'SMOKE测试部' } $u
Check '重复报名拦截' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
$ro = Send-Api 'GET' "/api/culture/activity-sign/roster?activityId=$aid" 'C01' $null $u
Check '花名册1行已报名' ($ro.data.Count -eq 1 -and $ro.data[0].signStatus -eq '已报名') ($ro | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '已报名人数回写1' ($r.data.signedCount -eq 1) $r.data.signedCount
Send-Api 'POST' "/api/culture/activity-sign/$aid/cancel" 'C01' $null $u | Out-Null
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '取消报名后回写0' ($r.data.signedCount -eq 0) $r.data.signedCount
Send-Api 'POST' "/api/culture/activity-sign/$aid/sign" 'C01' @{ userDept = 'SMOKE测试部' } $u | Out-Null

$r = Send-Api 'POST' "/api/culture/activity-sign/$aid/check?userName=$u&checkStatus=%E7%99%BE%E5%88%86" 'C04' $null $u
Check '签到状态枚举拦截' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/activity-sign/$aid/check?userName=$u&checkStatus=%E5%B7%B2%E7%AD%BE%E5%88%B0" 'C01' $null $u
Check '签到登记角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$cs = [uri]::EscapeDataString('已签到')
Send-Api 'POST' "/api/culture/activity-sign/$aid/check?userName=$u&checkStatus=$cs" 'C04' $null $u | Out-Null
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '已签到人数回写1' ($r.data.checkedCount -eq 1) $r.data.checkedCount

# 5. 进行中→待复盘→闭环
$r = Send-Api 'POST' "/api/culture/activity/$aid/start" 'C01' $null $u
Check '开始活动角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/activity/$aid/start" 'C04' $null $u | Out-Null
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '活动进行中' ($r.data.status -eq '进行中') $r.data.status
Send-Api 'POST' "/api/culture/activity/$aid/finish" 'C02' $null $u | Out-Null
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '活动待复盘' ($r.data.status -eq '待复盘') $r.data.status
$closeBody = @{ summary = '冒烟复盘总结：流程顺畅参与积极。'; resultEval = '参与率100%'; archiveFiles = '[]' }
$r = Send-Api 'POST' "/api/culture/activity/$aid/close" 'C02' $closeBody $u
Check '无素材闭环拦截E-AC-01' ($r.code -eq 400 -and $r.message -like '*素材*') ($r | ConvertTo-Json -Compress)
$closeBody.archiveFiles = '["/files/smoke/p1.jpg","/files/smoke/v1.mp4"]'
$r = Send-Api 'POST' "/api/culture/activity/$aid/close" 'C02' $closeBody $u
Check '复盘闭环成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/activity/$aid" 'C01' $null $u
Check '闭环状态与素材2个' ($r.data.status -eq '已闭环' -and $r.data.summary -like '*流程顺畅*') $r.data.status
$sq3 = Send-Api 'GET' '/api/culture/activity/square' 'C01' $null
Check '闭环活动不在广场' ($sq3.data.Count -eq 2) $sq3.data.Count

# 6. 取消流程
$r = Send-Api 'POST' '/api/culture/activity' 'C01' $actBody $u
$aid2 = $r.data.id
Send-Api 'POST' "/api/culture/activity/$aid2/submit" 'C01' $null $u | Out-Null
Send-Api 'POST' "/api/culture/activity/$aid2/approve?pass=true" 'C04' $null $u | Out-Null
Send-Api 'POST' "/api/culture/activity/$aid2/approve?pass=true&opinion=%E5%90%8C%E6%84%8F" 'C02' $null $u | Out-Null
$r = Send-Api 'POST' "/api/culture/activity/$aid2/cancel?reason=%E7%9F%AD" 'C02' $null $u
Check '取消原因过短拦截' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)
$rs = [uri]::EscapeDataString('场地冲突取消')
$r = Send-Api 'POST' "/api/culture/activity/$aid2/cancel?reason=$rs" 'C01' $null $u
Check '取消活动角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/activity/$aid2/cancel?reason=$rs" 'C02' $null $u
$r = Send-Api 'GET' "/api/culture/activity/$aid2" 'C01' $null $u
Check '活动已取消' ($r.data.status -eq '已取消') $r.data.status

# 7. 阵地
$r = Send-Api 'POST' '/api/culture/position' 'C01' @{ posName = 'SMOKE测试阵地'; updateCycleDays = 30 } $u
Check '阵地创建角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/position' 'C02' @{ posName = 'SMOKE测试阵地'; posType = '线上阵地'; updateCycleDays = 30; ownerDept = 'SMOKE部' } $u
$posId = $r.data.id
Check '阵地创建成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
Send-Api 'POST' "/api/culture/position/$posId/touch" 'C02' $null $u | Out-Null
$r = Send-Api 'GET' "/api/culture/position/list" 'C01' $null
$myPos = $r.data | Where-Object { $_.id -eq $posId }
$today = (Get-Date).ToString('yyyy-MM-dd')
Check '阵地更新登记刷为今天' ($myPos.lastUpdateDate -like "$today*") $myPos.lastUpdateDate
Send-Api 'POST' "/api/culture/position/$posId/toggle" 'C02' $null $u | Out-Null
$r = Send-Api 'GET' '/api/culture/position/list' 'C01' $null
Check '阵地停用生效' ((($r.data | Where-Object { $_.id -eq $posId }).status) -eq '停用') 'not 停用'

# 8. 物料
$r = Send-Api 'POST' '/api/culture/material' 'C01' @{ matName = 'SMOKE测试物料'; unit = '件'; stock = 10; safetyStock = 5 } $u
Check '物料创建角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/material' 'C02' @{ matName = 'SMOKE测试物料'; matType = '文创'; unit = '件'; stock = 10; safetyStock = 5; keeper = 'smoke' } $u
$mid = $r.data.id
Check '物料创建成功' ($r.code -eq 200 -and $r.data.stock -eq 10) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/material/$mid/flow" 'C01' @{ flowType = '入库'; qty = 5 } $u
Check '出入库角色拦截403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/material/$mid/flow" 'C02' @{ flowType = '入库'; qty = 5; reason = '采购补货' } $u
Check '入库+5' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/material/$mid/flow" 'C02' @{ flowType = '出库'; qty = 100; reason = '大批领用' } $u
Check '出库超库存拦截E-AC-02' ($r.code -eq 400 -and $r.message -like '*库存不足*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/material/$mid/flow" 'C02' @{ flowType = '出库'; qty = 3; reason = '读书会领用' } $u
Check '出库-3' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' "/api/culture/material/list" 'C01' $null
Check '库存结余12' ((($r.data | Where-Object { $_.id -eq $mid }).stock) -eq 12) 'stock!=12'
$fl = Send-Api 'GET' "/api/culture/material/flows?materialId=$mid" 'C01' $null
Check '台账2笔含结余' ($fl.data.Count -eq 2 -and $fl.data[0].balanceAfter -eq 12) $fl.data.Count
Send-Api 'POST' "/api/culture/material/$mid/toggle" 'C02' $null $u | Out-Null
$r = Send-Api 'GET' '/api/culture/material/list' 'C01' $null
Check '物料停用生效' ((($r.data | Where-Object { $_.id -eq $mid }).status) -eq '停用') 'not 停用'

Write-Host ''
Write-Host "==== SMOKE RESULT: PASS=$script:pass FAIL=$script:fail ===="
if ($script:fail -gt 0) { $script:failMsgs | ForEach-Object { Write-Host " - $_" -ForegroundColor Yellow }; exit 1 }
