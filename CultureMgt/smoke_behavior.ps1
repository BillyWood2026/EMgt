# CCMS 子模块7 文化行为与价值观履职 冒烟测试（UTF-8 BOM，可重入）
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

# ========== 清理 smoke 历史数据（可重入） ==========
mysql --default-character-set=utf8mb4 -e "DELETE FROM culture_mgt.cul_behavior_evidence WHERE contributor LIKE 'smoke-%'; DELETE FROM culture_mgt.cul_behavior_audit WHERE target_user LIKE 'smoke-%'; DELETE FROM culture_mgt.cul_behavior_duty WHERE officer LIKE 'smoke-%'; DELETE FROM culture_mgt.cul_behavior_review_record WHERE evaluator LIKE 'smoke-%'; DELETE FROM culture_mgt.cul_behavior_review_config WHERE period_tag='SMOKE-Q'; DELETE FROM culture_mgt.cul_behavior_score_txn WHERE user_name LIKE 'smoke-%'; DELETE FROM culture_mgt.cul_behavior_score_account WHERE user_name LIKE 'smoke-%';" 2>$null

# ========== 一、个人行为举证 ==========
# 1. 越权创建（C01无权限，实际举证任何登录用户可建，故用非本人编辑测试）
$r = Send-Api 'POST' '/api/culture/behavior/evidence' 'C01' @{ valueDimension='客户导向'; behaviorType='正向'; occurTime='2026-10-05 10:00:00'; scene='SMOKE场景'; deedDesc='这是一个超过三十字的冒烟测试事迹描述，用于验证举证创建流程是否正常。'; attachments='[{"name":"t.jpg","url":"/f/t.jpg"}]'; selfScore=8 } 'smoke-c01'
Check 'C01创建举证成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$eviId = $r.data.id

# 2. 非本人编辑拦截
$r = Send-Api 'PUT' "/api/culture/behavior/evidence/$eviId" 'C01' @{ valueDimension='客户导向'; behaviorType='正向'; occurTime='2026-10-05 10:00:00'; scene='SMOKE场景2'; deedDesc='这是一个超过三十字的冒烟测试事迹描述，用于验证举证编辑流程是否正常。'; attachments='[{"name":"t.jpg","url":"/f/t.jpg"}]'; selfScore=9 } 'smoke-c02'
Check '非本人编辑拦截E-BH-04' ($r.code -eq 400 -and $r.message -like '*本人*') ($r | ConvertTo-Json -Compress)

# 3. 未来时间拦截
$r = Send-Api 'POST' '/api/culture/behavior/evidence' 'C01' @{ valueDimension='客户导向'; behaviorType='正向'; occurTime='2027-01-01 10:00:00'; scene='SMOKE场景'; deedDesc='这是一个超过三十字的冒烟测试事迹描述，用于验证未来时间拦截。'; attachments='[{"name":"t.jpg","url":"/f/t.jpg"}]'; selfScore=8 } 'smoke-c01'
Check '未来时间拦截E-BH-07' ($r.code -eq 400 -and $r.message -like '*未来*') ($r | ConvertTo-Json -Compress)

# 4. 事迹描述不足30字
$r = Send-Api 'POST' '/api/culture/behavior/evidence' 'C01' @{ valueDimension='客户导向'; behaviorType='正向'; occurTime='2026-10-05 10:00:00'; scene='SMOKE场景'; deedDesc='太短'; attachments='[{"name":"t.jpg","url":"/f/t.jpg"}]'; selfScore=8 } 'smoke-c01'
Check '描述不足30字拦截E-BH-05' ($r.code -eq 400 -and $r.message -like '*30*') ($r | ConvertTo-Json -Compress)

# 5. 提交举证
$r = Send-Api 'POST' "/api/culture/behavior/evidence/$eviId/submit" 'C01' $null 'smoke-c01'
Check '提交举证成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 6. 重复提交拦截（状态已变）
$r = Send-Api 'POST' "/api/culture/behavior/evidence/$eviId/submit" 'C01' $null 'smoke-c01'
Check '重复提交拦截E-BH-02' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 7. C04 初审通过（stage=1）
$r = Send-Api 'POST' "/api/culture/behavior/evidence/$eviId/approve?pass=true&opinion=初审通过" 'C04' $null 'smoke-c04'
Check 'C04初审通过' ($r.code -eq 200 -and $r.data.status -eq '待复核') ($r | ConvertTo-Json -Compress)

# 8. C04 越权复核（stage=2 需文化岗）
$r = Send-Api 'POST' "/api/culture/behavior/evidence/$eviId/approve?pass=true&opinion=越权复核" 'C04' $null 'smoke-c04'
Check 'C04复核越权拦截' ($r.code -eq 400 -or $r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 9. C03 复核通过 → 已入账 + 积分流水
$r = Send-Api 'POST' "/api/culture/behavior/evidence/$eviId/approve?pass=true&opinion=复核通过" 'C03' $null 'smoke-c03'
Check 'C03复核通过并入账' ($r.code -eq 200 -and $r.data.status -eq '已入账' -and $r.data.pointTxnId) ($r | ConvertTo-Json -Compress)

# 10. 积分账户验证
$acct = (Send-Api 'GET' '/api/culture/behavior/score/smoke-c01' 'C03' $null 'smoke-c03').data
Check '积分账户已创建且加分' ($acct -and $acct.totalScore -gt 60) ($acct | ConvertTo-Json -Compress)

# ========== 二、部门行为稽核 ==========
# 11. C01 越权登记稽核
$r = Send-Api 'POST' '/api/culture/behavior/audit' 'C01' @{ targetUser='smoke-c02'; targetDept='SMOKE部'; violateDimension='客户导向'; violateScene='SMOKE违规'; violateDetail='冒烟测试违规详情超过十个字'; occurTime='2026-09-10 10:00:00'; deductScore=3; rectifyRequirement='整改要求超过五个字'; rectifyDeadline='2026-10-20'; attachments='[{"name":"a.png","url":"/f/a.png"}]' } 'smoke-c01'
Check 'C01登记稽核越权拦截' ($r.code -eq 400 -or $r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 12. C03 登记稽核
$r = Send-Api 'POST' '/api/culture/behavior/audit' 'C03' @{ targetUser='smoke-c02'; targetDept='SMOKE部'; violateDimension='客户导向'; violateScene='SMOKE违规'; violateDetail='冒烟测试违规详情超过十个字'; occurTime='2026-09-10 10:00:00'; deductScore=3; rectifyRequirement='整改要求超过五个字'; rectifyDeadline='2026-10-20'; attachments='[{"name":"a.png","url":"/f/a.png"}]' } 'smoke-c03'
Check 'C03登记稽核成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$auditId = $r.data.id

# 13. 当事人确认
$r = Send-Api 'POST' "/api/culture/behavior/audit/$auditId/confirm" 'C01' $null 'smoke-c02'
Check '当事人确认成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 14. 非当事人确认拦截
$r = Send-Api 'POST' "/api/culture/behavior/audit/$auditId/appeal" 'C01' @{ reason='非当事人申诉测试超过十个字' } 'smoke-c01'
Check '非当事人申诉拦截' ($r.code -eq 400 -or $r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 15. 当事人提交整改
$r = Send-Api 'POST' "/api/culture/behavior/audit/$auditId/rectify" 'C01' @{ note='整改说明内容超过十个字'; evidence='[{"name":"r.pdf","url":"/f/r.pdf"}]' } 'smoke-c02'
Check '提交整改成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 16. 验收通过 → 已生效 + 扣分入账
$opn = [uri]::EscapeDataString('验收通过确认合格')
$r = Send-Api 'POST' "/api/culture/behavior/audit/$auditId/accept?pass=true&opinion=$opn" 'C03' $null 'smoke-c03'
Check '验收通过并扣分入账' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# ========== 三、干部文化履职 ==========
# 17. C01 越权填报
$r = Send-Api 'POST' '/api/culture/behavior/duty' 'C01' @{ periodTag='2026Q3'; officer='smoke-c01'; dept='SMOKE部'; dutyItems='[{"key":"宣贯","done":1,"note":"ok"},{"key":"活动","done":1,"note":"ok"},{"key":"案例","done":1,"note":"ok"},{"key":"谈心","done":1,"note":"ok"},{"key":"整改","done":1,"note":"ok"}]' } 'smoke-c01'
Check 'C01填报履职越权拦截' ($r.code -eq 400 -or $r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 18. C04 填报履职
$r = Send-Api 'POST' '/api/culture/behavior/duty' 'C04' @{ periodTag='2026Q3'; officer='smoke-c04'; dept='SMOKE部'; dutyItems='[{"key":"宣贯","done":1,"note":"ok"},{"key":"活动","done":1,"note":"ok"},{"key":"案例","done":1,"note":"ok"},{"key":"谈心","done":1,"note":"ok"},{"key":"整改","done":0,"note":"待改进"}]'; weakness='短板说明' } 'smoke-c04'
Check 'C04填报履职成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$dutyId = $r.data.id

# 19. 重复填报拦截（同officer+periodTag在途）
$r = Send-Api 'POST' '/api/culture/behavior/duty' 'C04' @{ periodTag='2026Q3'; officer='smoke-c04'; dept='SMOKE部'; dutyItems='[{"key":"宣贯","done":1,"note":"ok"},{"key":"活动","done":1,"note":"ok"},{"key":"案例","done":1,"note":"ok"},{"key":"谈心","done":1,"note":"ok"},{"key":"整改","done":0,"note":"待改进"}]' } 'smoke-c04'
Check '重复填报拦截E-BH-06' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 20. 提交履职
$r = Send-Api 'POST' "/api/culture/behavior/duty/$dutyId/submit" 'C04' $null 'smoke-c04'
Check '提交履职成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 21. C03 审核通过
$r = Send-Api 'POST' "/api/culture/behavior/duty/$dutyId/review?pass=true&opinion=审核通过" 'C03' $null 'smoke-c03'
Check 'C03审核履职通过' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# ========== 四、价值观评议 ==========
# 22. C01 越权保存配置
$r = Send-Api 'POST' '/api/culture/behavior/review/config' 'C01' @{ periodTag='SMOKE-Q'; weightJson='{"客户导向":25,"奋斗担当":30,"艰苦奋斗":25,"自我批判":20}'; targetScope='全员'; allowPeer=1; startTime='2026-10-01 09:00:00'; endTime='2026-10-31 18:00:00' } 'smoke-c01'
Check 'C01保存配置越权403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

# 23. C03 保存配置（权重合计100）
$r = Send-Api 'POST' '/api/culture/behavior/review/config' 'C03' @{ periodTag='SMOKE-Q'; weightJson='{"客户导向":25,"奋斗担当":30,"艰苦奋斗":25,"自我批判":20}'; targetScope='全员'; allowPeer=1; startTime='2026-10-01 09:00:00'; endTime='2026-10-31 18:00:00' } 'smoke-c03'
Check 'C03保存配置成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$cfgId = $r.data.id

# 24. 权重合计≠100拦截
$r = Send-Api 'POST' '/api/culture/behavior/review/config' 'C03' @{ periodTag='SMOKE-Q2'; weightJson='{"客户导向":20,"奋斗担当":30,"艰苦奋斗":25,"自我批判":20}'; targetScope='全员'; allowPeer=1; startTime='2026-10-01 09:00:00'; endTime='2026-10-31 18:00:00' } 'smoke-c03'
Check '权重合计≠100拦截E-BH-10' ($r.code -eq 400 -and $r.message -like '*100*') ($r | ConvertTo-Json -Compress)

# 25. 发布配置
$r = Send-Api 'POST' "/api/culture/behavior/review/config/$cfgId/publish" 'C03' $null 'smoke-c03'
Check '发布配置成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 26. 已发布配置不可改
$r = Send-Api 'POST' '/api/culture/behavior/review/config' 'C03' @{ id=$cfgId; periodTag='SMOKE-Q'; weightJson='{"客户导向":25,"奋斗担当":30,"艰苦奋斗":25,"自我批判":20}'; targetScope='全员'; allowPeer=1; startTime='2026-10-01 09:00:00'; endTime='2026-10-31 18:00:00' } 'smoke-c03'
Check '已发布配置不可改E-BH-11' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 27. 提交评议（窗口内）
$r = Send-Api 'POST' '/api/culture/behavior/review/submit' 'C01' @{ configId=$cfgId; evaluatee='smoke-c02'; relation='互评'; scoreCustomer=80; scoreStrive=85; scoreHardship=75; scoreReflect=70; comment='冒烟测试评议意见超过十个字' } 'smoke-c01'
Check '提交评议成功' ($r.code -eq 200 -and $r.data.totalScore) ($r | ConvertTo-Json -Compress)

# 28. 重复评议拦截
$r = Send-Api 'POST' '/api/culture/behavior/review/submit' 'C01' @{ configId=$cfgId; evaluatee='smoke-c02'; relation='互评'; scoreCustomer=80; scoreStrive=85; scoreHardship=75; scoreReflect=70; comment='重复评议测试超过十个字' } 'smoke-c01'
Check '重复评议拦截E-BH-06' ($r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 29. 评议入账验证（被评人积分增加）
$acct2 = (Send-Api 'GET' '/api/culture/behavior/score/smoke-c02' 'C03' $null 'smoke-c03').data
Check '评议入账成功' ($acct2 -and $acct2.totalScore -gt 60) ($acct2 | ConvertTo-Json -Compress)

# ========== 五、积分台账与权限 ==========
# 30. 隐私隔离：C01 查他人积分（非本部门）
$r = Send-Api 'GET' '/api/culture/behavior/score/admin' 'C01' $null 'smoke-c01'
Check 'C01查非本部门积分403' ($r.code -eq 403 -or $r.code -eq 400) ($r | ConvertTo-Json -Compress)

# 31. C03 查全员积分
$r = Send-Api 'GET' '/api/culture/behavior/score/dept-list' 'C03' $null 'smoke-c03'
Check 'C03查部门积分列表' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# 32. 达标率
$r = Send-Api 'GET' '/api/culture/behavior/score/compliance' 'C03' $null 'smoke-c03'
Check '部门达标率返回' ($r.code -eq 200 -and $r.data.complianceRate -ne $null) ($r | ConvertTo-Json -Compress)

# 33. 排行榜
$r = Send-Api 'GET' '/api/culture/behavior/score/rank?scope=all' 'C03' $null 'smoke-c03'
Check '积分排行榜返回' ($r.code -eq 200 -and $r.data.Count -gt 0) ($r | ConvertTo-Json -Compress)

# 34. 流水查询
$r = Send-Api 'GET' '/api/culture/behavior/score/txn?userName=smoke-c01' 'C03' $null 'smoke-c03'
Check '积分流水查询' ($r.code -eq 200 -and $r.data.Count -gt 0) ($r | ConvertTo-Json -Compress)

# ========== 汇总 ==========
Write-Host "`n========== 冒烟结果: $($script:pass) PASS / $($script:fail) FAIL =========="
if ($script:fail -gt 0) { $script:failMsgs | ForEach-Object { Write-Host $_ -ForegroundColor Red } ; exit 1 }
exit 0
