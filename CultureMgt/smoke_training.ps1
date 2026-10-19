# CCMS 子模块5 在线培训及考试 冒烟测试（UTF-8 BOM，可重入）
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
    if ($null -ne $bodyObj) {
        $json = $bodyObj | ConvertTo-Json -Depth 8 -Compress
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
        $req.ContentLength = $bytes.Length
        $s = $req.GetRequestStream(); $s.Write($bytes, 0, $bytes.Length); $s.Close()
    }
    try { $resp = $req.GetResponse() } catch [System.Net.WebException] { $resp = $_.Exception.Response }
    $rs = $resp.GetResponseStream()
    $sr = New-Object System.IO.StreamReader($rs, [System.Text.Encoding]::UTF8)
    $txt = $sr.ReadToEnd(); $sr.Close(); $resp.Close()
    if ($txt) { return $txt | ConvertFrom-Json } else { return $null }
}

function Check($name, $cond, $detail) {
    if ($cond) { $script:pass++; Write-Host "[PASS] $name" }
    else { $script:fail++; $script:failMsgs += "$name :: $detail"; Write-Host "[FAIL] $name :: $detail" -ForegroundColor Red }
}

# ========== 一、课程元数据 ==========
$r = Send-Api 'POST' '/api/culture/course' 'C01' @{ courseName='SMOKE课程A'; courseCategory='年度复训'; midCourseId='MID-SMOKE-001'; creditHours=1; orgScope='全集团'; requiredFlag=1 } 'smoke-c01'
Check 'C01建课程403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/course' 'C03' @{ courseName='SMOKE课程A'; courseCategory='年度复训'; midCourseId='MID-SMOKE-002'; creditHours=40.5; orgScope='全集团'; requiredFlag=1 } 'smoke-c03'
Check '学时超40拦截E-TR-02' ($r.code -eq 400 -and $r.message -like '*0-40*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/course' 'C03' @{ courseName='SMOKE课程A'; courseCategory='年度复训'; midCourseId='MID-C2026001'; creditHours=1; orgScope='全集团'; requiredFlag=1 } 'smoke-c03'
Check '中台课程ID重复E-TR-03' ($r.code -eq 400 -and $r.message -like '*已被*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/course' 'C03' @{ courseName='SMOKE课程A'; courseCategory='年度复训'; midCourseId='MID-SMOKE-001'; creditHours=1; orgScope='全集团'; requiredFlag=1 } 'smoke-c03'
Check 'C03建SMOKE课程成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$courseId = $r.data.id

# ========== 二、题库 ==========
$r = Send-Api 'POST' '/api/culture/question' 'C03' @{ questionType='判断'; stem='【SMOKE】判断题测试题干是否不少于五个字？'; answer='对'; valueTag='诚信合规' } 'smoke-c03'
Check '判断题答案非法E-TR-05' ($r.code -eq 400 -and $r.message -like '*正确/错误*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/question/import-batch' 'C03' @{ rows=@() } 'smoke-c03'
Check '空导入拦截E-TR-06' ($r.code -eq 400 -and $r.message -like '*导入*') ($r | ConvertTo-Json -Compress)
$badRows = @( @{ questionType='单选'; stem='【SMOKE】导入题缺答案?'; optionItems=(@('A选项','B选项') | ConvertTo-Json -Compress); answer=''; valueTag='诚信合规' } )
$r = Send-Api 'POST' '/api/culture/question/import-batch' 'C03' @{ rows=$badRows } 'smoke-c03'
Check '导入行缺答案E-TR-06带行号' ($r.code -eq 400 -and $r.message -like '*第1行*') ($r | ConvertTo-Json -Compress)
$goodRows = @()
1..5 | ForEach-Object {
    $goodRows += @{ questionType='单选'; stem="【SMOKE】批量导入测试题$_ 的题干内容是否足够长？"; optionItems=(@('甲选项','乙选项','丙选项') | ConvertTo-Json -Compress); answer='甲选项'; valueTag='诚信合规' }
}
$r = Send-Api 'POST' '/api/culture/question/import-batch' 'C03' @{ rows=$goodRows } 'smoke-c03'
Check '批量导入5题成功' ($r.code -eq 200 -and $r.data.imported -eq 5) ($r | ConvertTo-Json -Compress)
$qs = Send-Api 'GET' ('/api/culture/question/list?keyword=' + [uri]::EscapeDataString('【SMOKE】批量导入')) 'C03' $null
$smokeQids = @($qs.data | ForEach-Object { $_.id })
Check 'SMOKE题目可检索5条' ($smokeQids.Count -eq 5) ($qs.data.Count)

# ========== 三、学习任务 ==========
$r = Send-Api 'POST' '/api/culture/study-task' 'C03' @{ taskName='SMOKE学习任务A'; taskType='季度复训'; courseId=$courseId; deadline='2026-12-31'; remindDays=7 } 'smoke-c03'
Check '任务类型非法E-TR-04' ($r.code -eq 400 -and $r.message -like '*任务类型*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/study-task' 'C03' @{ taskName='SMOKE学习任务A'; taskType='部门专项'; courseId=$courseId; deadline='2026-12-31'; remindDays=7 } 'smoke-c03'
Check 'C03建SMOKE学习任务' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$taskId = $r.data.id
$users = @( @{userName='smoke-t1';userDept='SMOKE测试部'}, @{userName='smoke-t2';userDept='SMOKE测试部'} )
$r = Send-Api 'POST' "/api/culture/study-task/$taskId/assign" 'C03' @{ users=$users } 'smoke-c03'
Check '分配2人成功' ($r.code -eq 200 -and $r.data.created -eq 2) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/study-task/$taskId/sync" 'C03' @{ userName='smoke-t1'; progress=150 } 'smoke-c03'
Check '进度150拦截E-TR-15' ($r.code -eq 400 -and $r.message -like '*0-100*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/study-task/$taskId/sync" 'C03' @{ userName='smoke-t1'; progress=100 } 'smoke-c03'
Check '中台回写100完成' ($r.code -eq 200 -and $r.data.status -eq '已完成') ($r | ConvertTo-Json -Compress)
$recs = Send-Api 'GET' "/api/culture/study-task/$taskId/records" 'C03' $null
Check '任务完成率50%' ($recs.data.rate -eq 50) ($recs.data | ConvertTo-Json -Compress)
$rem = Send-Api 'GET' "/api/culture/study-task/$taskId/remind" 'C03' $null
Check '到期提醒未完成名单含smoke-t2' ($rem.data.undoneCount -eq 1 -and $rem.data.task.taskName) ($rem.data | ConvertTo-Json -Compress)
$my = Send-Api 'GET' '/api/culture/study-task/my-records' 'C01' $null 'smoke-t1'
Check '我的学习含已完成记录' (@($my.data | Where-Object { $_.record.userName -eq 'smoke-t1' }).Count -ge 1) ($my.data.Count)

# ========== 四、考试任务配置 ==========
$bankJson = '[' + ($smokeQids -join ',') + ']'
$r = Send-Api 'POST' '/api/culture/exam' 'C03' @{ examName='SMOKE考试A'; bankIds='[]'; durationMin=30; passLine=70; maxRetake=1; antiSwitch=1; startTime='2026-10-01T00:00:00'; endTime='2026-12-31T23:59:00' } 'smoke-c03'
Check '题量不足5拦截E-TR-07' ($r.code -eq 400 -and $r.message -like '*5题*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/exam' 'C03' @{ examName='SMOKE考试A'; bankIds=$bankJson; durationMin=30; passLine=101; maxRetake=1; antiSwitch=1; startTime='2026-10-01T00:00:00'; endTime='2026-12-31T23:59:00' } 'smoke-c03'
Check '及格线101拦截E-TR-12' ($r.code -eq 400 -and $r.message -like '*0-100*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/exam' 'C03' @{ examName='SMOKE考试A'; bankIds=$bankJson; durationMin=30; passLine=70; maxRetake=1; antiSwitch=1; startTime='2026-10-01T00:00:00'; endTime='2026-12-31T23:59:00' } 'smoke-c03'
Check 'C03建SMOKE考试A' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$examA = $r.data.id
$r = Send-Api 'POST' '/api/culture/exam' 'C03' @{ examName='SMOKE考试B未来'; bankIds=$bankJson; durationMin=30; passLine=70; maxRetake=0; antiSwitch=1; startTime='2027-01-01T00:00:00'; endTime='2027-12-31T23:59:00' } 'smoke-c03'
Check 'C03建SMOKE考试B(未来窗)' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$examB = $r.data.id
$r = Send-Api 'POST' '/api/culture/exam' 'C03' @{ examName='SMOKE考试C零补考'; bankIds=$bankJson; durationMin=30; passLine=70; maxRetake=0; antiSwitch=1; startTime='2026-10-01T00:00:00'; endTime='2026-12-31T23:59:00' } 'smoke-c03'
Check 'C03建SMOKE考试C(零补考)' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$examC = $r.data.id
$r = Send-Api 'POST' "/api/culture/exam/$examA/assign" 'C03' @{ users=@(@{userName='smoke-t1';userDept='SMOKE测试部'}, @{userName='smoke-t2';userDept='SMOKE测试部'}) } 'smoke-c03'
Check '考试A分配2人' ($r.code -eq 200 -and $r.data.created -eq 2) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/$examB/assign" 'C03' @{ users=@(@{userName='smoke-t3';userDept='SMOKE测试部'}) } 'smoke-c03'
Check '考试B分配1人' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/$examC/assign" 'C03' @{ users=@(@{userName='smoke-t4';userDept='SMOKE测试部'}) } 'smoke-c03'
Check '考试C分配1人' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/$examA/toggle" 'C01' $null 'smoke-c01'
Check 'C01操作考试403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)

# ========== 五、考试流程：时间窗/开考/切屏/交卷 ==========
$instB = Send-Api 'GET' "/api/culture/exam/$examB/instances" 'C03' $null
$iB = $instB.data.instances[0].id
$r = Send-Api 'POST' "/api/culture/exam/instance/$iB/start" 'C01' $null 'smoke-t3'
Check '时间窗外开考E-TR-08' ($r.code -eq 400 -and $r.message -like '*时间窗*') ($r | ConvertTo-Json -Compress)

$instA = Send-Api 'GET' "/api/culture/exam/$examA/instances" 'C03' $null
$iA1 = ($instA.data.instances | Where-Object { $_.userName -eq 'smoke-t1' })[0].id
$iA2 = ($instA.data.instances | Where-Object { $_.userName -eq 'smoke-t2' })[0].id
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA1/start" 'C01' $null 'smoke-t1'
Check '开考返回卷面(无答案)' ($r.code -eq 200 -and $r.data.paper.Count -eq 5 -and -not ($r.data.paper[0].PSObject.Properties.Name -contains 'answer')) ($r.data.paper.Count)
$p1 = $r.data
# smoke-t1 切屏2次后交卷（全部选"甲选项"→满分合格→自动证书）
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA1/switch" 'C01' @{ count=2 } 'smoke-t1'
Check '切屏上报累计2' ($r.code -eq 200 -and $r.data.switchCount -eq 2) ($r | ConvertTo-Json -Compress)
$ans1 = @{}
foreach ($q in $p1.paper) { $ans1[[string]$q.id] = '甲选项' }
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA1/submit" 'C01' @{ answers=$ans1 } 'smoke-t1'
Check '满分交卷合格' ($r.code -eq 200 -and $r.data.pass -eq $true -and $r.data.score -eq 100) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA1/submit" 'C01' @{ answers=$ans1 } 'smoke-t1'
Check '重复交卷E-TR-09' ($r.code -eq 400 -and $r.message -like '*已交卷*') ($r | ConvertTo-Json -Compress)
$crt = Send-Api 'GET' '/api/culture/archive/smoke-t1' 'C03' $null
$certAuto = @($crt.data.certs | Where-Object { $_.certType -eq '年度文化合格认证' -and $_.sourceExamId -eq $examA })
Check '合格自动颁发认证证书' ($certAuto.Count -eq 1) ($crt.data.certs.Count)

# smoke-t2 不及格→冻结→补考合格→解冻
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA2/start" 'C01' $null 'smoke-t2'
$p2 = $r.data
$ans2 = @{}
foreach ($q in $p2.paper) { $ans2[[string]$q.id] = '乙选项' }
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA2/submit" 'C01' @{ answers=$ans2 } 'smoke-t2'
Check '不及格判分' ($r.code -eq 200 -and $r.data.pass -eq $false) ($r | ConvertTo-Json -Compress)
$inst2 = Send-Api 'GET' "/api/culture/exam/$examA/instances" 'C03' $null
$i2after = ($inst2.data.instances | Where-Object { $_.id -eq $iA2 })[0]
Check '不及格自动冻结资格' ($i2after.frozen -eq 1 -and $i2after.status -eq '已完成不合格待补考') ($i2after | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA2/start" 'C01' $null 'smoke-t2'
Check '补考开考attempt=2' ($r.code -eq 200 -and $r.data.attempt -eq 2) ($r | ConvertTo-Json -Compress)
$p2r = $r.data
$ans2r = @{}
foreach ($q in $p2r.paper) { $ans2r[[string]$q.id] = '甲选项' }
$r = Send-Api 'POST' "/api/culture/exam/instance/$iA2/submit" 'C01' @{ answers=$ans2r } 'smoke-t2'
Check '补考合格' ($r.code -eq 200 -and $r.data.status -eq '补考完成合格') ($r | ConvertTo-Json -Compress)
$inst2b = Send-Api 'GET' "/api/culture/exam/$examA/instances" 'C03' $null
$i2final = ($inst2b.data.instances | Where-Object { $_.id -eq $iA2 })[0]
Check '补考合格自动解冻' ($i2final.frozen -eq 0 -and $i2final.unfrozenTime -ne $null) ($i2final | ConvertTo-Json -Compress)

# 零补考考试：不合格后再开考→E-TR-11
$instC = Send-Api 'GET' "/api/culture/exam/$examC/instances" 'C03' $null
$iC = $instC.data.instances[0].id
$r = Send-Api 'POST' "/api/culture/exam/instance/$iC/start" 'C01' $null 'smoke-t4'
$pC = $r.data
$ansC = @{}
foreach ($q in $pC.paper) { $ansC[[string]$q.id] = '乙选项' }
$r = Send-Api 'POST' "/api/culture/exam/instance/$iC/submit" 'C01' @{ answers=$ansC } 'smoke-t4'
Check '零补考考试不合格' ($r.code -eq 200 -and $r.data.pass -eq $false) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/instance/$iC/start" 'C01' $null 'smoke-t4'
Check '补考次数用尽E-TR-11' ($r.code -eq 400 -and $r.message -like '*补考次数*') ($r | ConvertTo-Json -Compress)

# ========== 六、种子冻结场景：zhaoliu 补考通过自动解冻 ==========
$zi = Send-Api 'GET' '/api/culture/exam/my-instances' 'C01' $null 'zhaoliu'
$zid = ($zi.data | Where-Object { $_.userName -eq 'zhaoliu' })[0].id
Check 'zhaoliu种子实例待补考+冻结' ($zid -and $zi.data[0].frozen -eq 1) ($zi.data | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/exam/instance/$zid/start" 'C01' $null 'zhaoliu'
Check 'zhaoliu补考开考' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$pz = $r.data
$ansZ = @{}
foreach ($q in $pz.paper) { $ansZ[[string]$q.id] = '客户需求' }  # 部分正确，构造不合格不触发；改用全对
$ansZ = @{}
$qlist = Send-Api 'GET' ('/api/culture/question/list?keyword=' + [uri]::EscapeDataString('【文化考试】')) 'C03' $null
foreach ($q in $pz.paper) {
    $hit = $qlist.data | Where-Object { $_.id -eq $q.id }
    $ansZ[[string]$q.id] = $hit.answer
}
$r = Send-Api 'POST' "/api/culture/exam/instance/$zid/submit" 'C01' @{ answers=$ansZ } 'zhaoliu'
Check 'zhaoliu补考合格' ($r.code -eq 200 -and $r.data.pass -eq $true) ($r | ConvertTo-Json -Compress)
$zafter = Send-Api 'GET' '/api/culture/exam/my-instances' 'C01' $null 'zhaoliu'
$zf = ($zafter.data | Where-Object { $_.id -eq $zid })[0]
Check 'zhaoliu冻结自动解除' ($zf.frozen -eq 0) ($zf | ConvertTo-Json -Compress)

# ========== 七、档案/证书/统计 ==========
$r = Send-Api 'POST' '/api/culture/archive/cert' 'C03' @{ userName='smoke-t1'; certType='优秀文化践行者认证'; reason='SMOKE颁发测试：季度践行案例入选案例库三篇。' } 'smoke-c03'
Check 'C03颁发践行者认证' ($r.code -eq 200 -and $r.data.certNo) ($r | ConvertTo-Json -Compress)
$certNo = $r.data.certNo
$r = Send-Api 'POST' '/api/culture/archive/cert' 'C03' @{ userName='smoke-t1'; certType='优秀文化践行者认证'; reason='SMOKE重复颁发测试理由不少于五个字。' } 'smoke-c03'
Check '重复颁发E-TR-13' ($r.code -eq 400 -and $r.message -like '*已持有*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'GET' '/api/culture/archive/smoke-t2' 'C01' $null 'other-user'
Check '非本人查档案403' ($r.code -eq 403 -or ($r.code -eq 400 -and $r.message -like '*仅可查看本人*')) ($r | ConvertTo-Json -Compress)
$arc = Send-Api 'GET' '/api/culture/archive/list' 'C03' $null
Check '档案聚合含smoke-t1' (@($arc.data | Where-Object { $_.userName -eq 'smoke-t1' }).Count -eq 1) ($arc.data.Count)
$arcF = Send-Api 'GET' '/api/culture/archive/list?frozen=1' 'C03' $null
Check '冻结名单不含已解冻' (@($arcF.data | Where-Object { $_.userName -eq 'smoke-t2' }).Count -eq 0) ($arcF.data.Count)
$st = Send-Api 'GET' "/api/culture/exam/stats?examId=$examA" 'C03' $null
$std = @($st.data | Where-Object { $_.dept -eq 'SMOKE测试部' })[0]
Check '合格率统计(2/2=100%)' ($std.total -eq 2 -and $std.rate -eq 100) ($st.data | ConvertTo-Json -Compress)

# ========== 八、删除保护 ==========
$r = Send-Api 'DELETE' "/api/culture/course/$courseId" 'C03' $null 'smoke-c03'
Check '课程被任务引用禁删E-TR-14' ($r.code -eq 400 -and $r.message -like '*禁止删除*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'DELETE' "/api/culture/question/$($smokeQids[0])" 'C03' $null 'smoke-c03'
Check '题目被考试引用禁删E-TR-14' ($r.code -eq 400 -and $r.message -like '*禁止删除*') ($r | ConvertTo-Json -Compress)

# ========== 清理（恢复种子可重入） ==========
$clean = @'
UPDATE culture_mgt.cul_exam_instance SET deleted=1 WHERE user_name LIKE 'smoke-%';
UPDATE culture_mgt.cul_study_record SET deleted=1 WHERE user_name LIKE 'smoke-%';
UPDATE culture_mgt.cul_cert SET deleted=1 WHERE user_name LIKE 'smoke-%';
UPDATE culture_mgt.cul_exam SET deleted=1 WHERE exam_name LIKE 'SMOKE%';
UPDATE culture_mgt.cul_study_task SET deleted=1 WHERE task_name LIKE 'SMOKE%';
UPDATE culture_mgt.cul_question SET deleted=1 WHERE stem LIKE '【SMOKE】%';
UPDATE culture_mgt.cul_course SET deleted=1 WHERE course_name LIKE 'SMOKE%';
UPDATE culture_mgt.cul_exam_instance SET status='已完成不合格待补考', score=50, attempt=1, frozen=1, frozen_time='2026-09-28 10:30:00', unfrozen_time=NULL, answers='{}', submit_time='2026-09-28 10:25:00', start_time='2026-09-28 09:40:00', switch_count=2 WHERE user_name='zhaoliu' AND deleted=0;
UPDATE culture_mgt.cul_cert SET deleted=1 WHERE user_name='zhaoliu' AND cert_type='年度文化合格认证';
DELETE FROM culture_mgt.cul_audit_log WHERE operator LIKE 'smoke-%' OR detail LIKE '%SMOKE%';
'@
$tmp = "$env:TEMP\tr_clean.sql"
[System.IO.File]::WriteAllText($tmp, $clean, (New-Object System.Text.UTF8Encoding($false)))
$m = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe'
Get-Content -Raw -Encoding UTF8 $tmp | & $m -uroot -p"Sun-0126King-0325" --default-character-set=utf8mb4 2>$null
Write-Host "`n清理完成"
Write-Host "==================== 冲烟结果 PASS=$pass FAIL=$fail ====================" -ForegroundColor $(if ($fail -eq 0) { 'Green' } else { 'Red' })
$failMsgs | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
