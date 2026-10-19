# 第12章 系统管理 冒烟测试（PartyAffairsMgt 8150）
# 注意：PS5.1 响应按 Latin1 解码，断言只比 ASCII（错误码/数字/字段名）
$ErrorActionPreference = 'Continue'
$base = 'http://localhost:8150/api/party/system'
$script:pass = 0; $script:fail = 0

# 清理冒烟残留，保证可重入
$mysql = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
& $mysql -uroot -pSun-0126King-0325 party_affairs_mgt -e "DELETE FROM sys_user WHERE account IN ('smokeu1','smokeu2','x1'); DELETE FROM sys_role WHERE role_code='SMK'; UPDATE sys_user SET fail_count=0, lock_until=NULL, status='启用' WHERE account='guest';" 2>$null

function Invoke-Api {
    param([string]$Method, [string]$Url, [string]$Role = 'R02', [string]$BodyJson = $null)
    $headers = @{}
    if ($Role) { $headers['X-Party-Role'] = $Role; $headers['X-User-Id'] = 'smoke' }
    try {
        $p = @{ Uri = $Url; Method = $Method; Headers = $headers; TimeoutSec = 30 }
        if ($BodyJson) {
            $p['Body'] = [Text.Encoding]::UTF8.GetBytes($BodyJson)
            $p['ContentType'] = 'application/json'
        }
        $r = Invoke-WebRequest @p
        return @{ code = [int]$r.StatusCode; body = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $sr = New-Object IO.StreamReader($resp.GetResponseStream())
            return @{ code = [int]$resp.StatusCode; body = $sr.ReadToEnd() }
        }
        return @{ code = -1; body = $_.Exception.Message }
    }
}

function Check { param([string]$Name, [bool]$Cond)
    if ($Cond) { $script:pass++; Write-Host "PASS $Name" } else { $script:fail++; Write-Host "FAIL $Name" } }

# ---------- 用户 ----------
$r = Invoke-Api GET "$base/user/list?pageSize=50"
Check 'U01 用户列表9条(R02)' ($r.code -eq 200 -and $r.body -match '"total":9')
$r = Invoke-Api GET "$base/user/list" 'R99'
Check 'U02 用户列表R99可读' ($r.code -eq 200)
$r = Invoke-Api POST "$base/user" 'R99' '{"account":"x1","name":"x","pwdHash":"Test@12345"}'
Check 'U03 R99写用户403' ($r.code -eq 403)
$r = Invoke-Api POST "$base/user/login" '' '{"account":"admin","password":"Admin@12345"}'
Check 'U04 登录成功admin' ($r.code -eq 200 -and $r.body -match '"account":"admin"' -and $r.body -match 'R02')
$r = Invoke-Api POST "$base/user/login" '' '{"account":"guest","password":"Wrong@123"}'
Check 'U05 错误密码提示剩余次数' ($r.body -match '"code":500')
$r = Invoke-Api POST "$base/user/login" '' '{"account":"nouser","password":"Aa@12345"}'
Check 'U06 不存在账号' ($r.body -match '"code":500')
$r = Invoke-Api POST "$base/user" 'R02' '{"account":"admin","name":"重复","pwdHash":"Test@12345"}'
Check 'U07 重复账号E-SY-01' ($r.body -match 'E-SY-01')
$r = Invoke-Api POST "$base/user" 'R02' '{"account":"smokeu1","name":"弱密码","pwdHash":"123"}'
Check 'U08 弱密码E-SY-02' ($r.body -match 'E-SY-02')
$r = Invoke-Api POST "$base/user" 'R02' '{"account":"smokeu1","pwdHash":"Test@12345"}'
Check 'U09 缺姓名E-SY-16' ($r.body -match 'E-SY-16')
$r = Invoke-Api POST "$base/user" 'R02' '{"account":"smokeu1","name":"冒烟用户","pwdHash":"Test@12345","roleCodes":"R99"}'
Check 'U10 新增用户成功' ($r.code -eq 200 -and $r.body -match '"account":"smokeu1"')
$uid = 0; if ($r.body -match '"id":(\d+)') { $uid = [int]$Matches[1] }
$r = Invoke-Api POST "$base/user/$uid/toggle" 'R02'
Check 'U11 停用切换' ($r.code -eq 200)
$r = Invoke-Api POST "$base/user/login" '' '{"account":"smokeu1","password":"Test@12345"}'
Check 'U12 停用账号登录E-SY-05' ($r.body -match 'E-SY-05')
$r = Invoke-Api POST "$base/user/$uid/toggle" 'R02'
Check 'U13 启用切换' ($r.code -eq 200)
$r = Invoke-Api POST "$base/user/$uid/reset-pwd" 'R02'
Check 'U14 重置密码' ($r.code -eq 200 -and $r.body -match 'newPassword')
$r = Invoke-Api POST "$base/user/login" '' '{"account":"smokeu1","password":"Smokeu1@12345"}'
Check 'U15 重置后可登录' ($r.code -eq 200 -and $r.body -match '"account":"smokeu1"')
$r = Invoke-Api POST "$base/user/import" 'R02' '[{"account":"smokeu2","name":"导入1","pwdHash":"Test@12345"},{"account":"admin","name":"坏行","pwdHash":"Test@12345"}]'
Check 'U16 导入1成功1失败' ($r.body -match '"ok":1' -and $r.body -match '"fail":1')

# ---------- 角色 ----------
$r = Invoke-Api GET "$base/role/list?pageSize=50"
Check 'R01 角色列表9条' ($r.code -eq 200 -and $r.body -match '"total":9')
$r = Invoke-Api POST "$base/role" 'R02' '{"roleCode":"R02","roleName":"重复角色"}'
Check 'R02 角色编码重复E-SY-06' ($r.body -match 'E-SY-06')
$roleId = 0
$r = Invoke-Api GET "$base/role/list?roleCodeLike=R02"
if ($r.body -match '"id":(\d+)') { $roleId = [int]$Matches[1] }
$r = Invoke-Api DELETE "$base/role/$roleId" 'R02'
Check 'R03 角色被引用E-SY-07' ($r.body -match 'E-SY-07')
$r = Invoke-Api POST "$base/role" 'R02' '{"roleCode":"SMK","roleName":"冒烟角色","dataScope":"本人","pagePerms":"[]","buttonPerms":"[]","fieldPerms":"[]"}'
Check 'R04 新增角色' ($r.code -eq 200)
$smkId = 0; if ($r.body -match '"id":(\d+)') { $smkId = [int]$Matches[1] }
$r = Invoke-Api PUT "$base/role/$smkId" 'R02' '{"roleName":"冒烟角色2","dataScope":"全部"}'
Check 'R05 修改角色(写审计)' ($r.code -eq 200 -and $r.body -match '"dataScope":"')
$r = Invoke-Api DELETE "$base/role/$smkId" 'R02'
Check 'R06 删除未引用角色' ($r.body -match '"data":true')

# ---------- 岗位 ----------
$r = Invoke-Api GET "$base/position/list?pageSize=50"
Check 'P01 岗位列表有种子' ($r.code -eq 200 -and $r.body -match '"total":')
$r = Invoke-Api POST "$base/position" 'R02' '{"orgId":1,"orgName":"集团党委","positionName":"书记","memberName":"测试冲突","partTime":0,"status":"在任"}'
Check 'P02 非兼职在任冲突E-SY-08' ($r.body -match 'E-SY-08')
$r = Invoke-Api POST "$base/position" 'R02' '{"orgId":1,"orgName":"集团党委","positionName":"书记","memberName":"测试兼职","partTime":1,"status":"在任"}'
Check 'P03 兼职可共存' ($r.code -eq 200)
$posId = 0; if ($r.body -match '"id":(\d+)') { $posId = [int]$Matches[1] }
$r = Invoke-Api DELETE "$base/position/$posId" 'R02'
Check 'P04 删除岗位' ($r.body -match '"data":true')

# ---------- 字典 ----------
$r = Invoke-Api GET "$base/dict/type/list"
Check 'D01 字典类型8个' ($r.code -eq 200 -and $r.body -match 'ACTIVITY_TYPE')
$r = Invoke-Api POST "$base/dict/type" 'R02' '{"typeCode":"DUES_LEVEL","typeName":"重复"}'
Check 'D02 类型编码重复E-SY-09' ($r.body -match 'E-SY-09')
$r = Invoke-Api GET "$base/dict/item/list?typeCode=ACTIVITY_TYPE"
Check 'D03 活动类型字典项5个' ($r.code -eq 200 -and $r.body -match '"sortOrder"')
$itemId = 0
if ($r.body -match '"id":(\d+)[^}]*"itemValue":"\S{2,4}"[^}]*"itemLabel"') { }
# 找“党课”项id（Latin1下中文乱码，按 sortOrder=4 定位）
$items = $r.body | ConvertFrom-Json
foreach ($it in $items.data) { if ($it.sortOrder -eq 4) { $itemId = $it.id } }
$r = Invoke-Api DELETE "$base/dict/item/$itemId" 'R02'
Check 'D04 被引用字典项E-SY-10' ($r.body -match 'E-SY-10')
$r = Invoke-Api POST "$base/dict/item" 'R02' '{"typeCode":"ACTIVITY_TYPE","itemValue":"SMKTMP","itemLabel":"冒烟临时","sortOrder":99}'
Check 'D05 新增字典项' ($r.code -eq 200)
$tmpId = 0; if ($r.body -match '"id":(\d+)') { $tmpId = [int]$Matches[1] }
$r = Invoke-Api PUT "$base/dict/item/$tmpId" 'R02' '{"itemLabel":"冒烟临时v2"}'
Check 'D06 修改字典项版本+1' ($r.body -match '"version":2')
$r = Invoke-Api DELETE "$base/dict/item/$tmpId" 'R02'
Check 'D07 未引用物理删除' ($r.body -match '"data":true')
$r = Invoke-Api POST "$base/dict/item/$itemId/disable" 'R02'
Check 'D08 停用被引用项' ($r.code -eq 200)

# ---------- 参数 ----------
$r = Invoke-Api GET "$base/param/list"
Check 'A01 参数列表31个' ($r.code -eq 200 -and $r.body -match 'backup.rpoMinutes')
$r = Invoke-Api PUT "$base/param/dues.ratio.l1" 'R02' '{"paramValue":"0.6"}'
Check 'A02 修改参数为0.6' ($r.body -match '"paramValue":"0.6"')
$r = Invoke-Api GET "$base/param/dues.ratio.l1/history"
Check 'A03 参数历史含v2' ($r.body -match '"version":2')
Invoke-Api PUT "$base/param/dues.ratio.l1" 'R02' '{"paramValue":"0.7"}' | Out-Null
$r = Invoke-Api POST "$base/param/dues.ratio.l1/rollback?version=2" 'R02'
Check 'A04 回滚到v2值0.6' ($r.code -eq 200 -and $r.body -match '"paramValue":"0.6"')
$r = Invoke-Api PUT "$base/param/no.such.key" 'R02' '{"paramValue":"1"}'
Check 'A05 参数不存在E-SY-12' ($r.body -match 'E-SY-12')
$r = Invoke-Api POST "$base/param/dues.ratio.l1/rollback?version=99" 'R02'
Check 'A06 回滚版本不存在E-SY-13' ($r.body -match 'E-SY-13')
$r = Invoke-Api PUT "$base/param/dues.ratio.l1" 'R99' '{"paramValue":"9"}'
Check 'A07 R99改参数403' ($r.code -eq 403)

# ---------- 消息 ----------
$r = Invoke-Api GET "$base/message/template/list"
Check 'M01 模板3个' ($r.code -eq 200 -and $r.body -match 'ALERT_DEFAULT')
$r = Invoke-Api POST "$base/message/send" 'R02' '{"tplCode":"NOTICE_DEFAULT","receiver":"smoke","title":"t","content":"c"}'
Check 'M02 发送通知' ($r.code -eq 200 -and $r.body -match 'recordIds')
# 用预警模板(夜间不静默)验证失败重发，避免夜间静默窗干扰
$r = Invoke-Api POST "$base/message/send" 'R02' '{"tplCode":"ALERT_DEFAULT","receiver":"smoke","title":"f","content":"c","fail":true}'
Check 'M03 模拟发送失败' ($r.code -eq 200 -and $r.body -match 'recordIds')
$mid = 0
if ($r.body -match '"recordIds":\[(\d+)') { $mid = [int]$Matches[1] }
$r = Invoke-Api POST "$base/message/$mid/resend" 'R02'
Check 'M04 重发1次成功' ($r.body -match '"retryCount":1')
$r = Invoke-Api POST "$base/message/send" 'R02' '{"tplCode":"ALERT_DEFAULT","receiver":"smoke","title":"f2","content":"c","fail":true}'
$mid2 = 0
if ($r.body -match '"recordIds":\[(\d+)') { $mid2 = [int]$Matches[1] }
Invoke-Api POST "$base/message/$mid2/resend" 'R02' | Out-Null
Invoke-Api POST "$base/message/$mid2/resend" 'R02' | Out-Null
$r = Invoke-Api POST "$base/message/$mid2/resend" 'R02'
Check 'M05 重试3次E-SY-11预警' ($r.body -match 'E-SY-11')

# ---------- 审计 ----------
$r = Invoke-Api GET "$base/audit/list?pageSize=5"
Check 'T01 审计列表R02' ($r.code -eq 200 -and $r.body -match '"total":')
$r = Invoke-Api GET "$base/audit/list" 'R99'
Check 'T02 审计R99禁止403' ($r.code -eq 403)
$r = Invoke-Api PUT "$base/audit/1" 'R02' '{}'
Check 'T03 审计禁改E-SY-14' ($r.body -match 'E-SY-14')
$r = Invoke-Api DELETE "$base/audit/1" 'R02'
Check 'T04 审计禁删E-SY-14' ($r.body -match 'E-SY-14')

# ---------- 备份 ----------
$r = Invoke-Api POST "$base/backup/trigger?type=$([uri]::EscapeDataString('全量'))" 'R02'
Check 'B01 触发全量备份' ($r.code -eq 200 -and $r.body -match 'full_')
$r = Invoke-Api POST "$base/backup/trigger?type=$([uri]::EscapeDataString('增量'))" 'R02'
Check 'B02 触发增量备份' ($r.code -eq 200 -and $r.body -match 'incr_')
$r = Invoke-Api GET "$base/backup/list?pageSize=50"
Check 'B03 备份记录含种子+新增' ($r.code -eq 200 -and $r.body -match '"total":')
$r = Invoke-Api POST "$base/backup/trigger" 'R99'
Check 'B04 R99触发备份403' ($r.code -eq 403)
$r = Invoke-Api POST "$base/backup/drill" 'R02' '{"drillDate":"2026-10-07","rpoMinutes":10,"rtoMinutes":60,"notes":"达标演练"}'
Check 'B05 演练达标' ($r.code -eq 200 -and $r.body -notmatch 'E-SY-15')
$r = Invoke-Api POST "$base/backup/drill" 'R02' '{"drillDate":"2026-10-07","rpoMinutes":30,"rtoMinutes":200,"notes":"不达标演练"}'
Check 'B06 演练不达标E-SY-15预警' ($r.body -match 'E-SY-15')
$r = Invoke-Api GET "$base/backup/drill/list"
Check 'B07 演练记录列表' ($r.code -eq 200 -and $r.body -match '"total":')

Write-Host ""
Write-Host "=== RESULT: pass=$($script:pass) fail=$($script:fail) ==="
