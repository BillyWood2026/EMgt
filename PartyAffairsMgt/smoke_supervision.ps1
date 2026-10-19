# 第14章 监察监督与党风廉政建设 冒烟脚本（可重入）
# 验证 7 子域：日常监督/廉政档案/风险防控/廉政谈话/巡察整改/廉政考核/监察驾驶舱
# E-SV-01..21 错误码全覆盖

$ErrorActionPreference = 'Continue'
$base = "http://127.0.0.1:8150/api/party/supervision"
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

function MysqlSql { param([string]$Sql)
    $mysql = "C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe"
    & $mysql -uroot -p"Sun-0126King-0325" party_affairs_mgt -e $Sql 2>$null
}

Write-Host "===== 第14章 监察监督与党风廉政建设 冒烟 =====" -ForegroundColor Cyan

# 0. 清理（mysql）
Write-Host "`n--- 0. 清理测试数据 ---" -ForegroundColor Yellow
MysqlSql "DELETE FROM integrity_risk_check WHERE risk_id IN (SELECT id FROM (SELECT id FROM integrity_risk WHERE risk_point LIKE 'smoke%') t);
DELETE FROM integrity_risk WHERE risk_point LIKE 'smoke%';
DELETE FROM integrity_profile_log WHERE profile_id IN (SELECT id FROM (SELECT id FROM integrity_profile WHERE person_name LIKE 'smoke%') t);
DELETE FROM integrity_opinion_reply WHERE profile_id IN (SELECT id FROM (SELECT id FROM integrity_profile WHERE person_name LIKE 'smoke%') t);
DELETE FROM integrity_profile WHERE person_name LIKE 'smoke%';
DELETE FROM daily_supervision WHERE title LIKE 'smoke%';
DELETE FROM integrity_talk WHERE interviewee_name LIKE 'smoke%';
DELETE FROM inspection_issue WHERE inspection_id IN (SELECT id FROM (SELECT id FROM inspection_rectification WHERE inspected_org LIKE 'smoke%') t);
DELETE FROM inspection_rectification WHERE inspected_org LIKE 'smoke%';
DELETE FROM integrity_assess_indicator WHERE scheme_id IN (SELECT id FROM (SELECT id FROM integrity_assessment WHERE scheme_name LIKE 'smoke%') t);
DELETE FROM integrity_assessment WHERE scheme_name LIKE 'smoke%' OR org_name LIKE 'smoke%';
DELETE FROM stat_export_log WHERE report_key LIKE 'supervision-%' AND export_by='smoke';"
Write-Host "  cleaned"

# 1. 日常监督 Daily
Write-Host "`n--- 1. 日常监督 ---" -ForegroundColor Yellow

# E-SV-01 必填缺失
$r = Req -Method POST -Url "$base/daily" -Body @{title=""} -Role "R23"
Assert "E-SV-01 监督事项必填缺失" (HasCode $r "E-SV-01") $r.body

# R99 越权
$r = Req -Method POST -Url "$base/daily" -Body @{title="smoke越权";supervisionType="专项监督";superviseDate="2026-10-07"} -Role "R99"
Assert "R99 新建监督被拒（403）" (-not $r.ok) $r.body

# 正常新建
$r = Req -Method POST -Url "$base/daily" -Body @{
    title="smoke日常监督事项A"; supervisionType="专项监督"; superviseDate="2026-10-07";
    targetOrg="smoke被监督单位"; targetPerson="smoke张三"; superviseMethod="列席会议"
} -Role "R23"
$ok = $r.ok -and ($r.body -like "*JD2026*")
Assert "新建监督事项（R23）" $ok $r.body
$d1 = 0
if ($ok) { $d1 = (($r.body | ConvertFrom-Json).data).id }

# 问题登记
if ($d1 -gt 0) {
    $r = Req -Method POST -Url "$base/daily/$d1/problem" -Body @{
        finding="smoke发现：津补贴发放不规范"; problemNature="smoke财经纪律"; rectifyOwner="smoke责任人甲"; rectifyMeasure="smoke立即清退"
    } -Role "R23"
    Assert "问题登记（默认30日整改期）" ($r.ok -and ($r.body -like "*整改中*")) $r.body

    # E-SV-05 屡查屡犯：同对象同性质再登记
    $r = Req -Method POST -Url "$base/daily" -Body @{
        title="smoke日常监督事项B"; supervisionType="日常监督"; superviseDate="2026-10-07"; targetOrg="smoke被监督单位"
    } -Role "R23"
    $d2 = 0
    if ($r.ok) { $d2 = (($r.body | ConvertFrom-Json).data).id }
    if ($d2 -gt 0) {
        $r = Req -Method POST -Url "$base/daily/$d2/problem" -Body @{
            finding="smoke再次发现同类问题"; problemNature="smoke财经纪律"; rectifyOwner="smoke责任人乙"
        } -Role "R23"
        Assert "E-SV-05 屡查屡犯标记 repeatFlag" ($r.ok -and ($r.body -like '*"repeatFlag":1*')) $r.body

        # E-SV-02 超期红色升级：mysql 将期限改到过去，list 触发升级
        MysqlSql "UPDATE daily_supervision SET rectify_deadline='2020-01-01' WHERE id=$d2;"
        $r = Req -Method GET -Url "$base/daily/list?keyword=smoke" -Role "R23"
        Assert "E-SV-02 整改超期自动升级" ($r.ok -and ($r.body -like "*超期*")) $r.body
    }

    # 整改反馈：缺整改情况 E-SV-01
    $r = Req -Method POST -Url "$base/daily/$d1/feedback" -Body @{rectifyFeedback=""} -Role "R07"
    Assert "E-SV-01 整改反馈必填" (HasCode $r "E-SV-01") $r.body

    # 整改反馈（R07 被监督单位书记，无佐证）
    $r = Req -Method POST -Url "$base/daily/$d1/feedback" -Body @{rectifyFeedback="smoke已清退整改"} -Role "R07"
    Assert "整改反馈（R07）" ($r.ok -and ($r.body -like "*复核中*")) $r.body

    # E-SV-03 复核分离
    $r = Req -Method POST -Url "$base/daily/$d1/close" -Body @{reviewer="smoke责任人甲";reviewOpinion="smoke复核"} -Role "R06"
    Assert "E-SV-03 复核人=整改责任人阻断" (HasCode $r "E-SV-03") $r.body

    # E-SV-04 缺佐证销号
    $r = Req -Method POST -Url "$base/daily/$d1/close" -Body @{reviewer="smoke复核人乙";reviewOpinion="smoke复核"} -Role "R06"
    Assert "E-SV-04 销号缺佐证阻断" (HasCode $r "E-SV-04") $r.body

    # 补佐证再销号
    $r = Req -Method POST -Url "$base/daily/$d1/feedback" -Body @{rectifyFeedback="smoke已清退整改";evidence="smoke退款凭证扫描件"} -Role "R07"
    $r = Req -Method POST -Url "$base/daily/$d1/close" -Body @{reviewer="smoke复核人乙";reviewOpinion="smoke整改到位同意销号"} -Role "R06"
    Assert "复核销号成功" ($r.ok -and ($r.body -like "*已销号*")) $r.body

    # 催办
    $r = Req -Method POST -Url "$base/daily/$d1/urge" -Role "R23"
    Assert "催办 urgeCount+1" ($r.ok -and ($r.body -notlike '*"urgeCount":0*')) $r.body
}
$r = Req -Method GET -Url "$base/daily/stats" -Role "R23"
Assert "日常监督统计" ($r.ok -and ($r.body -like "*closeRate*")) $r.body

# 2. 廉政档案 Profile
Write-Host "`n--- 2. 廉政档案 ---" -ForegroundColor Yellow

$r = Req -Method POST -Url "$base/profile" -Body @{personName=""} -Role "R23"
Assert "E-SV-01 档案必填缺失" (HasCode $r "E-SV-01") $r.body

$r = Req -Method POST -Url "$base/profile" -Body @{personName="smoke档案甲"; position="smoke部门经理"; orgName="smoke单位"} -Role "R23"
$ok = $r.ok -and ($r.body -like "*DA2026*")
Assert "廉政档案建档（待补材料）" $ok $r.body
$p1 = 0
if ($ok) { $p1 = (($r.body | ConvertFrom-Json).data).id }

if ($p1 -gt 0) {
    # E-SV-08 只增不改：更新走留痕
    $r = Req -Method POST -Url "$base/profile/$p1/material" -Body @{
        action="新增材料"; materialName="经济责任审计结果"; content="smoke审计无异常"; operator="R23"
    } -Role "R23"
    Assert "档案材料追加留痕（E-SV-08）" $r.ok $r.body
    $r = Req -Method GET -Url "$base/profile/$p1/logs" -Role "R23"
    Assert "档案变更日志可查" ($r.ok -and ($r.body -like "*建档*" -and $r.body -like "*经济责任审计*")) $r.body

    # E-SV-09 关键材料缺失阻断任用
    $r = Req -Method GET -Url "$base/profile/$p1/appointment-check" -Role "R02"
    Assert "E-SV-09 个人事项报告缺失阻断任用" (HasCode $r "E-SV-09") $r.body

    # 发起意见回复
    $r = Req -Method POST -Url "$base/profile/$p1/reply" -Body @{requestOrg="smoke组织部"; matter="smoke提拔前征求廉政意见"} -Role "R23"
    $rep1 = 0
    if ($r.ok) { $rep1 = (($r.body | ConvertFrom-Json).data).id }
    Assert "发起廉政意见回复（7自然日时限）" ($rep1 -gt 0) $r.body

    # E-SV-01 回复意见非法值
    $r = Req -Method POST -Url "$base/profile/reply/$rep1/issue" -Body @{replyOpinion="错误值";approveBy="R06"} -Role "R06"
    Assert "E-SV-01 回复意见取值非法" (HasCode $r "E-SV-01") $r.body

    # E-SV-07 超期回复：mysql 改 deadline 到过去
    MysqlSql "UPDATE integrity_opinion_reply SET deadline='2020-01-01' WHERE id=$rep1;"
    $r = Req -Method POST -Url "$base/profile/reply/$rep1/issue" -Body @{replyOpinion="无影响";recheckResult="smoke核查无问题";approveBy="R06"} -Role "R06"
    Assert "E-SV-07 意见回复超期标记" ($r.ok -and ($r.body -like "*超期*")) $r.body

    # 出具须 R06：R23 无权
    $r = Req -Method POST -Url "$base/profile/$p1/reply" -Body @{requestOrg="smoke组织部2";matter="smoke再次征求"} -Role "R23"
    $rep2 = 0
    if ($r.ok) { $rep2 = (($r.body | ConvertFrom-Json).data).id }
    $r = Req -Method POST -Url "$base/profile/reply/$rep2/issue" -Body @{replyOpinion="无影响"} -Role "R23"
    Assert "意见出具须R06审批（R23被拒）" (-not $r.ok) $r.body
    $r = Req -Method POST -Url "$base/profile/reply/$rep2/issue" -Body @{replyOpinion="无影响";approveBy="R06"} -Role "R06"
    Assert "意见出具（R06）已办结" ($r.ok -and ($r.body -like "*已办结*")) $r.body
}
$r = Req -Method GET -Url "$base/profile/stats" -Role "R23"
Assert "E-SV-06 覆盖率<95%橙色告警" ($r.ok -and ($r.body -like '*"coverageAlert":true*')) $r.body

# 3. 风险防控 Risk
Write-Host "`n--- 3. 廉政风险防控 ---" -ForegroundColor Yellow

$r = Req -Method POST -Url "$base/risk" -Body @{riskPoint=""} -Role "R23"
Assert "E-SV-01 风险点必填缺失" (HasCode $r "E-SV-01") $r.body

# E-SV-10 高风险无措施
$r = Req -Method POST -Url "$base/risk" -Body @{riskPoint="smoke高风险点"; riskLevel="高"; domain="smoke工程建设"} -Role "R23"
Assert "E-SV-10 高风险无防控措施阻断" (HasCode $r "E-SV-10") $r.body

# 正常：中风险
$r = Req -Method POST -Url "$base/risk" -Body @{
    riskPoint="smoke采购风险点"; riskLevel="中"; domain="smoke采购"; checkFreq="季度";
    lastCheckDate="2026-07-01"; dept="smoke采购部"; preventionMeasure="smoke三人会签"
} -Role "R23"
$ok = $r.ok -and ($r.body -like "*FX2026*")
Assert "风险点登记（季度检查）" $ok $r.body
$rk1 = 0
if ($ok) { $rk1 = (($r.body | ConvertFrom-Json).data).id }

if ($rk1 -gt 0) {
    # 定级须 R06
    $r = Req -Method POST -Url "$base/risk/$rk1/confirm-level" -Body @{riskLevel="高";measure="smoke轮岗+全程留痕"} -Role "R23"
    Assert "定级须R06集体评估（R23被拒）" (-not $r.ok) $r.body
    $r = Req -Method POST -Url "$base/risk/$rk1/confirm-level" -Body @{riskLevel="高";measure="smoke轮岗+全程留痕"} -Role "R06"
    Assert "纪委集体评估定级（R06）" ($r.ok -and ($r.body -like '*"levelConfirmed":1*')) $r.body

    # 检查发现问题 → 触发重新评估
    $r = Req -Method POST -Url "$base/risk/$rk1/check" -Body @{finding="smoke发现拆单规避招标";measureAdjust="smoke增加金额校验";checker="R23"} -Role "R23"
    Assert "检查发现问题触发30日重新评估" ($r.ok -and ($r.body -like "*发生问题*" -or $r.ok)) $r.body
    $r = Req -Method GET -Url "$base/risk/$rk1" -Role "R23"
    Assert "风险点状态=发生问题且有reevalDeadline" ($r.body -like "*发生问题*" -and $r.body -like "*reevalDeadline*") $r.body

    # E-SV-12 超期重新评估
    MysqlSql "UPDATE integrity_risk SET reeval_deadline='2020-01-01' WHERE id=$rk1;"
    $r = Req -Method POST -Url "$base/risk/$rk1/reevaluate" -Body @{newLevel="中";newMeasure="smoke强化审核"} -Role "R06"
    Assert "E-SV-12 重新评估超30日阻断" (HasCode $r "E-SV-12") $r.body
}

# 另一条：正常检查 + E-SV-11 到期未检
$r = Req -Method POST -Url "$base/risk" -Body @{
    riskPoint="smoke用人风险点"; riskLevel="低"; domain="smoke人事"; checkFreq="半年"; lastCheckDate="2026-01-01"
} -Role "R23"
$rk2 = 0
if ($r.ok) { $rk2 = (($r.body | ConvertFrom-Json).data).id }
if ($rk2 -gt 0) {
    $r = Req -Method POST -Url "$base/risk/$rk2/check" -Body @{finding="";checker="R23"} -Role "R23"
    Assert "定期检查（无问题）" $r.ok $r.body
    MysqlSql "UPDATE integrity_risk SET next_check_date='2020-01-01' WHERE id=$rk2;"
    $r = Req -Method GET -Url "$base/risk/list?keyword=smoke" -Role "R23"
    Assert "E-SV-11 到期未检自动告警" ($r.ok -and ($r.body -like "*到期未检*")) $r.body

    # 期限内重新评估正常（先清掉到期状态再走另一条新点）
    $r = Req -Method POST -Url "$base/risk" -Body @{
        riskPoint="smoke财务风险点"; riskLevel="中"; domain="smoke财务"; checkFreq="年度"
    } -Role "R23"
    $rk3 = 0
    if ($r.ok) { $rk3 = (($r.body | ConvertFrom-Json).data).id }
    if ($rk3 -gt 0) {
        $r = Req -Method POST -Url "$base/risk/$rk3/check" -Body @{finding="smoke发现白条入账"} -Role "R23"
        $r = Req -Method POST -Url "$base/risk/$rk3/reevaluate" -Body @{newLevel="高";newMeasure="smoke取消现金支付"} -Role "R06"
        Assert "期限内重新评估通过" ($r.ok -and ($r.body -like "*正常*")) $r.body
    }
}
$r = Req -Method GET -Url "$base/risk/stats" -Role "R23"
Assert "风险防控统计" ($r.ok -and ($r.body -like "*dueUnchecked*")) $r.body

# 4. 廉政谈话 Talk
Write-Host "`n--- 4. 廉政谈话 ---" -ForegroundColor Yellow

$r = Req -Method POST -Url "$base/talk" -Body @{talkType="错误类型"} -Role "R23"
Assert "E-SV-01 谈话必填/类型非法" (HasCode $r "E-SV-01") $r.body

$r = Req -Method POST -Url "$base/talk" -Body @{
    talkType="任前廉政谈话"; intervieweeName="smoke谈话甲"; talker="R06书记"; planDate="2026-10-10"; orgName="smoke单位"
} -Role "R23"
$ok = $r.ok -and ($r.body -like "*TH2026*")
Assert "谈话计划（待审批）" $ok $r.body
$tk1 = 0
if ($ok) { $tk1 = (($r.body | ConvertFrom-Json).data).id }

if ($tk1 -gt 0) {
    # E-SV-13 任前谈话未完成阻断任用
    $r = Req -Method GET -Url ("$base/talk/appointment-gate?personName=" + [uri]::EscapeDataString("smoke谈话甲")) -Role "R02"
    Assert "E-SV-13 任前谈话未完成阻断任用" (HasCode $r "E-SV-13") $r.body

    # 审批须 R06
    $r = Req -Method POST -Url "$base/talk/$tk1/approve?pass=true" -Role "R23"
    Assert "谈话审批须R06（R23被拒）" (-not $r.ok) $r.body
    $r = Req -Method POST -Url "$base/talk/$tk1/approve?pass=true" -Role "R06"
    Assert "谈话审批通过（R06）" ($r.ok -and ($r.body -like "*已计划*")) $r.body

    # 实施（需整改回访）
    $r = Req -Method POST -Url "$base/talk/$tk1/complete" -Body @{
        keypoints="smoke任前廉洁要求"; attitude="smoke表态接受"; recorder="R23"; needRectify=1
    } -Role "R23"
    Assert "谈话实施（回访期限+60日）" ($r.ok -and ($r.body -like "*回访中*") -and ($r.body -like "*visitDeadline*")) $r.body

    # E-SV-14 回访超期
    MysqlSql "UPDATE integrity_talk SET visit_deadline='2020-01-01' WHERE id=$tk1;"
    $r = Req -Method POST -Url "$base/talk/$tk1/visit" -Body @{visitNote="smoke整改已到位"} -Role "R23"
    Assert "E-SV-14 回访超60日告警" ($r.ok -and ($r.body -like "*E-SV-14*")) $r.body
}
$r = Req -Method GET -Url "$base/talk/stats" -Role "R23"
Assert "廉政谈话统计" ($r.ok -and ($r.body -like "*byType*")) $r.body

# 5. 巡察整改 Inspection
Write-Host "`n--- 5. 巡察整改 ---" -ForegroundColor Yellow

$r = Req -Method POST -Url "$base/inspection" -Body @{inspectedOrg=""} -Role "R24"
Assert "E-SV-01 巡察必填缺失" (HasCode $r "E-SV-01") $r.body

# R07 不能创建（只看本单位）
$r = Req -Method POST -Url "$base/inspection" -Body @{inspectedOrg="smoke单位";roundNo="smoke轮次"} -Role "R07"
Assert "R07 不可新建巡察（403）" (-not $r.ok) $r.body
# R07 可看
$r = Req -Method GET -Url "$base/inspection/list" -Role "R07"
Assert "R07 可查看巡察列表" $r.ok $r.body

$r = Req -Method POST -Url "$base/inspection" -Body @{
    inspectedOrg="smoke被巡察单位"; roundNo="smoke2026第一轮"; team="smoke巡察一组"; inspectionName="smoke常规巡察"
} -Role "R24"
$ok = $r.ok -and ($r.body -like "*XC2026*")
Assert "巡察计划（R24）" $ok $r.body
$in1 = 0
if ($ok) { $in1 = (($r.body | ConvertFrom-Json).data).id }

if ($in1 -gt 0) {
    $r = Req -Method POST -Url "$base/inspection/$in1/issue" -Body @{issueDesc="smoke问题一：三重一大执行不严";category="smoke决策";owner="smoke责任人A"} -Role "R24"
    $is1 = 0
    if ($r.ok) { $is1 = (($r.body | ConvertFrom-Json).data).id }
    $r = Req -Method POST -Url "$base/inspection/$in1/issue" -Body @{issueDesc="smoke问题二：公车管理混乱";category="smoke作风";owner="smoke责任人B"} -Role "R24"
    $is2 = 0
    if ($r.ok) { $is2 = (($r.body | ConvertFrom-Json).data).id }
    Assert "巡察问题登记2条" ($is1 -gt 0 -and $is2 -gt 0) "is1=$is1 is2=$is2"

    # E-SV-16 未锁定禁止销号
    $r = Req -Method POST -Url "$base/inspection/issue/$is1/close" -Body @{evidence="smoke凭证";reviewer="R24"} -Role "R24"
    Assert "E-SV-16 清单未锁定禁止销号" (HasCode $r "E-SV-16") $r.body

    # 锁定+反馈（3个月）
    $r = Req -Method POST -Url "$base/inspection/$in1/lock-feedback" -Body @{feedbackIssue="smoke反馈意见：限期整改";deadlineMonths=3} -Role "R24"
    Assert "问题清单锁定+反馈（3月期限）" ($r.ok -and ($r.body -like '*"issueLocked":1*')) $r.body

    # 锁定后不可加问题
    $r = Req -Method POST -Url "$base/inspection/$in1/issue" -Body @{issueDesc="smoke问题三"} -Role "R24"
    Assert "E-SV-16 锁定后禁止新增问题" (HasCode $r "E-SV-16") $r.body

    # E-SV-15 销号缺佐证
    $r = Req -Method POST -Url "$base/inspection/issue/$is1/close" -Body @{reviewer="R24"} -Role "R24"
    Assert "E-SV-15 销号缺佐证阻断" (HasCode $r "E-SV-15") $r.body
    # E-SV-15 销号缺复核人
    $r = Req -Method POST -Url "$base/inspection/issue/$is1/close" -Body @{evidence="smoke整改凭证"} -Role "R24"
    Assert "E-SV-15 销号缺复核人阻断" (HasCode $r "E-SV-15") $r.body

    # 正常销号问题一
    $r = Req -Method POST -Url "$base/inspection/issue/$is1/close" -Body @{evidence="smoke会议纪要凭证";reviewer="smoke巡察复核人"} -Role "R24"
    Assert "问题一销号（完成率50%）" ($r.ok -and ($r.body -like "*已销号*")) $r.body
    $r = Req -Method GET -Url "$base/inspection/$in1" -Role "R24"
    Assert "完成率自动计算50%" ($r.body -like '*"finishRate":50*') $r.body

    # 线索移交
    $r = Req -Method POST -Url "$base/inspection/$in1/transfer-clue" -Body @{count=2} -Role "R24"
    Assert "问题线索移交（转13章流程）" ($r.ok -and ($r.body -like '*"clueTransferCount":2*')) $r.body

    # 整体销号：仍有未销号
    $r = Req -Method POST -Url "$base/inspection/$in1/close" -Body @{reviewer="R06"} -Role "R06"
    Assert "E-SV-15 未全部销号禁止整体销号" (HasCode $r "E-SV-15") $r.body

    # 销号问题二 → 整体销号
    $r = Req -Method POST -Url "$base/inspection/issue/$is2/close" -Body @{evidence="smoke公车台账";reviewer="smoke巡察复核人"} -Role "R24"
    $r = Req -Method POST -Url "$base/inspection/$in1/close" -Body @{reviewer="R06书记"} -Role "R06"
    Assert "巡察整体销号（R06）" ($r.ok -and ($r.body -like "*已销号*")) $r.body
}
$r = Req -Method GET -Url "$base/inspection/stats" -Role "R24"
Assert "巡察整改统计" ($r.ok -and ($r.body -like "*finishRate*")) $r.body

# 6. 廉政考核 Assessment
Write-Host "`n--- 6. 党风廉政建设考核 ---" -ForegroundColor Yellow

# E-SV-19 权重≠100
$r = Req -Method POST -Url "$base/assessment/scheme" -Body @{
    schemeName="smoke2026党风廉政考核"; year=2026; orgNames=@("smoke组织A","smoke组织B");
    indicators=@(@{indicatorName="两个责任落实";weight=60},@{indicatorName="廉洁风险防控";weight=30})
} -Role "R06"
Assert "E-SV-19 指标权重合计≠100阻断" (HasCode $r "E-SV-19") $r.body

# 权重预检
$r = Req -Method POST -Url "$base/assessment/weight-check" -Body @{
    indicators=@(@{indicatorName="两个责任落实";weight=60},@{indicatorName="廉洁风险防控";weight=40})
} -Role "R06"
Assert "权重预检=100通过" ($r.ok -and ($r.body -like '*"valid":true*')) $r.body

# 正常建方案
$r = Req -Method POST -Url "$base/assessment/scheme" -Body @{
    schemeName="smoke2026党风廉政考核"; year=2026; orgNames=@("smoke组织A","smoke组织B");
    indicators=@(@{indicatorName="两个责任落实";weight=60;standard="smoke标准"},@{indicatorName="廉洁风险防控";weight=40;standard="smoke标准"})
} -Role "R06"
$ok = $r.ok
Assert "考核方案创建（草稿+2组织+2指标）" $ok $r.body
$sc1 = 0
if ($ok) { $sc1 = (($r.body | ConvertFrom-Json).data).id }

# 取组织记录 id
$orgA = 0; $orgB = 0
if ($sc1 -gt 0) {
    $r = Req -Method GET -Url "$base/assessment/list?year=2026" -Role "R06"
    if ($r.ok) {
        $rows = ($r.body | ConvertFrom-Json).data | Where-Object { $_.schemeName -eq "smoke2026党风廉政考核" }
        foreach ($row in $rows) {
            if ($row.orgName -eq "smoke组织A") { $script:orgA = $row.id }
            if ($row.orgName -eq "smoke组织B") { $script:orgB = $row.id }
        }
    }
    $orgA = $script:orgA; $orgB = $script:orgB

    # E-SV-18 未发布禁自评
    $r = Req -Method POST -Url "$base/assessment/$orgA/self" -Body @{scores=@{"两个责任落实"=90;"廉洁风险防控"=90};evidence="smoke自评材料"} -Role "R07"
    Assert "E-SV-18 方案未发布禁止自评" (HasCode $r "E-SV-18") $r.body

    # 发布
    $r = Req -Method POST -Url "$base/assessment/scheme/$sc1/publish" -Role "R06"
    Assert "考核方案发布（R06）" ($r.ok -and ($r.body -like "*已发布*")) $r.body

    # 组织A：自评 → 核查（命中否决）→ E-SV-20 → 确认后不合格
    $r = Req -Method POST -Url "$base/assessment/$orgA/self" -Body @{scores=@{"两个责任落实"=90;"廉洁风险防控"=90};evidence="smoke自评材料A"} -Role "R07"
    Assert "组织A自评提交（R07）" ($r.ok -and ($r.body -like "*待核查*")) $r.body
    $r = Req -Method POST -Url "$base/assessment/$orgA/check" -Body @{
        scores=@{"两个责任落实"=85;"廉洁风险防控"=90}; deductions="smoke发生违纪案件"; vetoFlag=1
    } -Role "R06"
    Assert "纪委核查评分（一票否决标记）" ($r.ok -and ($r.body -like "*待评定*")) $r.body
    $r = Req -Method POST -Url "$base/assessment/$orgA/finalize?vetoConfirmed=false" -Body @{resultApplication="smoke通报"} -Role "R06"
    Assert "E-SV-20 一票否决须二次确认" (HasCode $r "E-SV-20") $r.body
    $r = Req -Method POST -Url "$base/assessment/$orgA/finalize?vetoConfirmed=true" -Body @{resultApplication="smoke通报并降档"} -Role "R06"
    Assert "否决确认后等次=不合格" ($r.ok -and ($r.body -like "*不合格*" -and $r.body -like '*"vetoConfirmed":1*')) $r.body

    # 组织B：自评 → 核查 93.8 → 优秀
    $r = Req -Method POST -Url "$base/assessment/$orgB/self" -Body @{scores=@{"两个责任落实"=95;"廉洁风险防控"=95};evidence="smoke自评材料B"} -Role "R07"
    $r = Req -Method POST -Url "$base/assessment/$orgB/check" -Body @{scores=@{"两个责任落实"=95;"廉洁风险防控"=92}} -Role "R06"
    $r = Req -Method POST -Url "$base/assessment/$orgB/finalize?vetoConfirmed=false" -Body @{} -Role "R06"
    Assert "加权得分93.8等次=优秀" ($r.ok -and ($r.body -like "*优秀*")) $r.body
}
$r = Req -Method GET -Url "$base/assessment/stats?year=2026" -Role "R06"
Assert "考核等次分布统计" ($r.ok -and ($r.body -like "*byGrade*")) $r.body

# 7. 监察驾驶舱 Stats
Write-Host "`n--- 7. 监察驾驶舱 ---" -ForegroundColor Yellow

$r = Req -Method GET -Url "$base/stats/dashboard" -Role "R06"
Assert "驾驶舱七指标" ($r.ok -and ($r.body -like "*problemCloseRate*") -and ($r.body -like "*profileCoverage*") `
    -and ($r.body -like "*riskOnTimeRate*") -and ($r.body -like "*talkDoneRate*") -and ($r.body -like "*inspectionFinishRate*") `
    -and ($r.body -like "*gradeDistribution*")) $r.body

$r = Req -Method GET -Url "$base/stats/dashboard" -Role "R99"
Assert "R99 驾驶舱被拒" (-not $r.ok) ""

# E-SV-21 导出未确认
$r = Req -Method GET -Url "$base/stats/export?type=daily&confirm=false&exportBy=smoke" -Role "R06"
Assert "E-SV-21 导出未二次确认阻断" (HasCode $r "E-SV-21") $r.body

# 五类导出
foreach ($t in @("daily","profile","risk","inspection","assessment")) {
    $r = Req -Method GET -Url "$base/stats/export?type=$t&confirm=true&exportBy=smoke" -Role "R06"
    Assert "导出CSV（$t）" ($r.ok -and ($r.body -like "*smoke*")) ""
}

# 总结
Write-Host "`n===== 结果 =====" -ForegroundColor Cyan
Write-Host "PASS: $pass  FAIL: $fail" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Red" })
if ($fail -gt 0) {
    Write-Host "`n失败明细:" -ForegroundColor Red
    $results | Where-Object { $_ -like "[FAIL]*" } | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}
exit $fail
