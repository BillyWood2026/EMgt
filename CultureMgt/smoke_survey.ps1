# CCMS 子模块6 文化调研与氛围评估 冒烟测试（UTF-8 BOM，可重入）
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

# 标准7题问卷（4维度量表+单选+多选+文本）JSON
$qs7 = @(
  @{ qid='q1'; type='量表'; stem='SMOKE精神层量表'; options=@(); dimension='精神层认同'; required=1; maxScale=5 },
  @{ qid='q2'; type='量表'; stem='SMOKE制度层量表'; options=@(); dimension='制度层认可'; required=1; maxScale=5 },
  @{ qid='q3'; type='量表'; stem='SMOKE行为层量表'; options=@(); dimension='行为层践行'; required=1; maxScale=5 },
  @{ qid='q4'; type='量表'; stem='SMOKE物质层量表'; options=@(); dimension='物质层感知'; required=1; maxScale=5 },
  @{ qid='q5'; type='单选'; stem='SMOKE单选题'; options=@('甲选项','乙选项','丙选项'); dimension=''; required=1; maxScale=5 },
  @{ qid='q6'; type='多选'; stem='SMOKE多选题'; options=@('子选项','丑选项','寅选项'); dimension=''; required=0; maxScale=5 },
  @{ qid='q7'; type='文本'; stem='SMOKE开放题'; options=@(); dimension=''; required=0; maxScale=5 }
) | ConvertTo-Json -Depth 6 -Compress

# ========== 一、维度配置 ==========
$r = Send-Api 'POST' '/api/culture/survey-dimension' 'C01' @{ dimensionName='SMOKE维度C01'; weight=0 } 'smoke-c01'
Check 'C01建维度403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-dimension' 'C03' @{ dimensionName='精神层认同'; weight=10 } 'smoke-c03'
Check '维度名重复E-SR-18' ($r.code -eq 400 -and $r.message -like '*已存在*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-dimension' 'C03' @{ dimensionName='SMOKE维度越界'; weight=101 } 'smoke-c03'
Check '权重越界E-SR-17' ($r.code -eq 400 -and $r.message -like '*权重*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-dimension' 'C03' @{ dimensionName='SMOKE维度X'; weight=0 } 'smoke-c03'
Check 'C03建0权重维度成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
$dimX = $r.data.id
$r = Send-Api 'PUT' "/api/culture/survey-dimension/$dimX" 'C03' @{ dimensionName='SMOKE维度X'; weight=5; status='启用' } 'smoke-c03'
Check '改权重致合计≠100拦截E-SR-17' ($r.code -eq 400 -and $r.message -like '*合计*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-dimension/$dimX/toggle" 'C03' $null 'smoke-c03'
Check '0权重维度可停用' ($r.code -eq 200 -and $r.data.status -eq '停用') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-dimension/$dimX/toggle" 'C03' $null 'smoke-c03'
Check '0权重维度可启用' ($r.code -eq 200 -and $r.data.status -eq '启用') ($r | ConvertTo-Json -Compress)
# 停用语义维度（权重30，剩余70）须阻断且回滚
$spirit = (Send-Api 'GET' '/api/culture/survey-dimension/list?status=%E5%90%AF%E7%94%A8' 'C03' $null).data | Where-Object { $_.dimensionName -eq '精神层认同' }
$r = Send-Api 'POST' "/api/culture/survey-dimension/$($spirit.id)/toggle" 'C03' $null 'smoke-c03'
Check '停用30权重维度阻断E-SR-17' ($r.code -eq 400 -and $r.message -like '*合计*') ($r | ConvertTo-Json -Compress)
$after = (Send-Api 'GET' '/api/culture/survey-dimension/list' 'C03' $null).data | Where-Object { $_.id -eq $spirit.id }
Check '阻断后维度仍启用(事务回滚)' ($after.status -eq '启用') ($after.status)

# ========== 二、问卷草稿校验 ==========
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C01' @{ title='SMOKE-C01问卷'; periodType='季度'; questions=$qs7 } 'smoke-c01'
Check 'C01建问卷403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='短'; periodType='季度'; questions=$qs7 } 'smoke-c03'
Check '问卷名称长度E-SR-02' ($r.code -eq 400 -and $r.message -like '*问卷名称*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE周期非法'; periodType='月度'; questions=$qs7 } 'smoke-c03'
Check '周期枚举E-SR-03' ($r.code -eq 400 -and $r.message -like '*季度/年度/专项*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE空题目'; periodType='季度'; questions='[]' } 'smoke-c03'
Check '空题目E-SR-04' ($r.code -eq 400 -and $r.message -like '*题目*') ($r | ConvertTo-Json -Compress)
$badType = '[{"qid":"q1","type":"判断题","stem":"SMOKE非法题型","options":[],"dimension":"","required":1,"maxScale":5}]'
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE题型非法'; periodType='季度'; questions=$badType } 'smoke-c03'
Check '题型非法E-SR-04' ($r.code -eq 400 -and $r.message -like '*题型*') ($r | ConvertTo-Json -Compress)
$noDim = '[{"qid":"q1","type":"量表","stem":"SMOKE无维度量表","options":[],"dimension":"","required":1,"maxScale":5}]'
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE量表无维度'; periodType='季度'; questions=$noDim } 'smoke-c03'
Check '量表缺维度E-SR-04' ($r.code -eq 400 -and $r.message -like '*维度*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE合法草稿'; periodType='季度'; anonymousFlag=1; targetOrg='SMOKE测试范围'; questions=$qs7 } 'smoke-c03'
Check 'C03合法草稿成功' ($r.code -eq 200 -and $r.data.status -eq '草稿') ($r | ConvertTo-Json -Compress)

# ========== 三、模板复制与发布校验 ==========
$tpl = (Send-Api 'GET' '/api/culture/survey-questionnaire/list?templateOnly=true' 'C03' $null).data | Select-Object -First 1
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$($tpl.id)/publish" 'C03' @{ targetCount=10; startTime='2026-01-01 09:00:00'; endTime='2027-12-31 18:00:00' } 'smoke-c03'
Check '模板不可直接发布E-SR-07' ($r.code -eq 400 -and $r.message -like '*模板*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/template/$($tpl.id)/copy" 'C03' $null 'smoke-c03'
Check '模板复制草稿成功' ($r.code -eq 200 -and $r.data.status -eq '草稿' -and $r.data.templateFlag -eq 0) ($r | ConvertTo-Json -Compress)

# 正式问卷A（当前窗口匿名）
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE-A当前窗口问卷'; periodType='季度'; anonymousFlag=1; targetOrg='全员'; questions=$qs7 } 'smoke-c03'
$qA = $r.data.id
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$qA/publish" 'C03' @{ targetCount=0; startTime='2026-01-01 09:00:00'; endTime='2027-12-31 18:00:00' } 'smoke-c03'
Check '发布缺目标人数E-SR-12' ($r.code -eq 400 -and $r.message -like '*目标*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$qA/publish" 'C03' @{ targetCount=2; startTime='2027-01-01 09:00:00'; endTime='2026-01-01 18:00:00' } 'smoke-c03'
Check '发布止早于起E-SR-06' ($r.code -eq 400 -and $r.message -like '*有效期*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$qA/publish" 'C03' @{ targetCount=2; startTime='2026-01-01 09:00:00'; endTime='2027-12-31 18:00:00'; targetOrg='SMOKE全员' } 'smoke-c03'
Check '问卷A发布成功' ($r.code -eq 200 -and $r.data.status -eq '已发布') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'PUT' "/api/culture/survey-questionnaire/$qA" 'C03' @{ title='SMOKE-A当前窗口问卷'; periodType='季度'; questions=$qs7 } 'smoke-c03'
Check '已发布不可编辑E-SR-07' ($r.code -eq 400 -and $r.message -like '*草稿*') ($r | ConvertTo-Json -Compress)

# 问卷B（已发布但时间窗在过去→09）与问卷C（实名→重复10）
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE-B过期窗口问卷'; periodType='专项'; anonymousFlag=1; questions=$qs7 } 'smoke-c03'
$qB = $r.data.id
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$qB/publish" 'C03' @{ targetCount=5; startTime='2026-01-01 09:00:00'; endTime='2026-01-10 18:00:00' } 'smoke-c03'
Check '问卷B发布(过期窗口)' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-questionnaire' 'C03' @{ title='SMOKE-C实名问卷'; periodType='年度'; anonymousFlag=0; questions=$qs7 } 'smoke-c03'
$qC = $r.data.id
$r = Send-Api 'POST' "/api/culture/survey-questionnaire/$qC/publish" 'C03' @{ targetCount=5; startTime='2026-01-01 09:00:00'; endTime='2027-12-31 18:00:00' } 'smoke-c03'
Check '问卷C实名发布' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)

# ========== 四、作答校验 ==========
$r = Send-Api 'POST' "/api/culture/survey-mine/$qB/submit" 'C01' @{ userDept='SMOKE测试部'; answers=@{} } 'smoke-window'
Check '时间窗外作答E-SR-09' ($r.code -eq 400 -and $r.message -like '*时间窗*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE薄弱部'; answers=@{ q1=5; q2=5; q3=5; q5='甲选项' } } 'smoke-dept1-a'
Check '必答题缺失E-SR-11' ($r.code -eq 400 -and $r.message -like '*必答*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE薄弱部'; answers=@{ q1=9; q2=1; q3=1; q4=1; q5='甲选项' } } 'smoke-dept1-a'
Check '量表越界E-SR-05' ($r.code -eq 400 -and $r.message -like '*量表*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE薄弱部'; answers=@{ q1=1; q2=1; q3=1; q4=1; q5='丁选项' } } 'smoke-dept1-a'
Check '单选答案超范围E-SR-11' ($r.code -eq 400 -and $r.message -like '*选项*') ($r | ConvertTo-Json -Compress)
# dept1 全1分（薄弱）
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE薄弱部'; answers=@{ q1=1; q2=1; q3=1; q4=1; q5='甲选项'; q7='SMOKE客户闭环整改氛围待加强' } } 'smoke-dept1-a'
Check 'dept1全1分提交成功' ($r.code -eq 200 -and $r.data.id) ($r | ConvertTo-Json -Compress)
# dept2 全5分（优秀）
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE优秀部'; answers=@{ q1=5; q2=5; q3=5; q4=5; q5='乙选项'; q6='子选项,丑选项'; q7='SMOKE文化氛围好客户满意奋斗者故事多' } } 'smoke-dept2-a'
Check 'dept2全5分提交成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
# 匿名问卷允许重复提交
$r = Send-Api 'POST' "/api/culture/survey-mine/$qA/submit" 'C01' @{ userDept='SMOKE优秀部'; answers=@{ q1=5; q2=5; q3=5; q4=5; q5='乙选项' } } 'smoke-dept2-b'
Check '匿名问卷可重复提交' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
# 实名问卷重复拦截
$realAns = @{ q1=5; q2=5; q3=5; q4=5; q5='甲选项' }
$r = Send-Api 'POST' "/api/culture/survey-mine/$qC/submit" 'C01' @{ userDept='SMOKE实名部'; answers=$realAns } 'smoke-real1'
Check '实名首次提交成功' ($r.code -eq 200) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-mine/$qC/submit" 'C01' @{ userDept='SMOKE实名部'; answers=$realAns } 'smoke-real1'
Check '实名重复提交E-SR-10' ($r.code -eq 400 -and $r.message -like '*重复*') ($r | ConvertTo-Json -Compress)
# 我的调研列表含已发布问卷
$ml = Send-Api 'GET' '/api/culture/survey-mine/list' 'C01' $null 'smoke-real1'
Check '我的列表含SMOKE-C已答标记' (@($ml.data | Where-Object { $_.id -eq $qC -and $_.submitted -eq $true }).Count -eq 1) ($ml.data.Count)

# 回收进度（target=2，已交3份，率截断100%）
$pg = Send-Api 'GET' "/api/culture/survey-questionnaire/$qA/progress" 'C03' $null
Check '回收进度3份参与率100%截断' ($pg.data.submitted -eq 3 -and $pg.data.participationRate -eq 100) ($pg.data | ConvertTo-Json -Compress)
$rl = Send-Api 'GET' "/api/culture/survey-questionnaire/$qA/responses" 'C03' $null
Check '答卷明细3份' (@($rl.data).Count -eq 3) (@($rl.data).Count)

# ========== 五、健康度（算法61） ==========
$ov = Send-Api 'GET' "/api/culture/survey-health/overview?questionnaireId=$qA" 'C03' $null
Check '总览含两部门+整体' (@($ov.data.depts).Count -eq 2 -and $ov.data.overall) ($ov.data | ConvertTo-Json -Compress)
$good = @($ov.data.depts | Where-Object { $_.dept -eq 'SMOKE优秀部' })[0]
Check '优秀部指数100优秀低风险' ($good.index -eq 100 -and $good.grade -eq '优秀' -and $good.riskLevel -eq '低风险') ($good | ConvertTo-Json -Compress)
$bad = @($ov.data.depts | Where-Object { $_.dept -eq 'SMOKE薄弱部' })[0]
Check '薄弱部指数0薄弱高风险四短板' ($bad.index -eq 0 -and $bad.grade -eq '薄弱' -and $bad.riskLevel -eq '高风险' -and @($bad.shortBoardTags).Count -eq 4) ($bad | ConvertTo-Json -Compress)
$det = Send-Api 'GET' "/api/culture/survey-health/org?dept=$([uri]::EscapeDataString('SMOKE优秀部'))&questionnaireId=$qA" 'C03' $null
Check '部门诊断含4维度行' (@($det.data.dimensions).Count -eq 4) (@($det.data.dimensions).Count)
$wc = @($det.data.wordCloud | Where-Object { $_.word -like '*客户*' -or $_.word -like '*奋斗*' })
Check '词云含业务词' (@($wc).Count -ge 1) ($det.data.wordCloud | ConvertTo-Json -Compress)
$os = @($det.data.optionStats)
Check '题项分布含单选+多选' (@($os).Count -eq 2) (@($os).Count)
Check '诊断含整改台账数组' ($det.data.PSObject.Properties.Name -contains 'rectifies') ($det.data | ConvertTo-Json -Compress)
$tr = Send-Api 'GET' '/api/culture/survey-health/trend' 'C03' $null
Check '趋势含2026Q1/Q2/Q3' (@($tr.data | Where-Object { $_.period -in @('2026Q1','2026Q2','2026Q3') }).Count -ge 3) (@($tr.data).Count)

# ========== 六、删除保护 ==========
$r = Send-Api 'DELETE' "/api/culture/survey-questionnaire/$qA" 'C03' $null 'smoke-c03'
Check '有答卷问卷禁删E-SR-08' ($r.code -eq 400 -and $r.message -like '*禁止删除*') ($r | ConvertTo-Json -Compress)

# ========== 七、整改BPM全流程 ==========
$r = Send-Api 'POST' '/api/culture/survey-rectify' 'C01' @{ title='SMOKE整改C01'; dept='SMOKE部'; owner='smoke-owner' } 'smoke-c01'
Check 'C01建整改403' ($r.code -eq 403) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-rectify' 'C03' @{ title='SMOKE整改缺责任人'; dept='SMOKE部' } 'smoke-c03'
Check '整改缺责任人E-SR-01' ($r.code -eq 400 -and $r.message -like '*责任*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' '/api/culture/survey-rectify' 'C03' @{ questionnaireId=$qA; sourceTag='精神层认同,制度层认可'; title='SMOKE整改全流程'; dept='SMOKE整改部'; owner='smoke-owner'; deadline='2026-11-30' } 'smoke-c03'
Check 'C03建整改成功待方案' ($r.code -eq 200 -and $r.data.status -eq '待方案提交') ($r | ConvertTo-Json -Compress)
$rid = $r.data.id
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan" 'C01' @{ plan='我不是责任人也要提交方案测试权限拦截' } 'smoke-other'
Check '非责任人提交方案拦截E-SR-15' ($r.code -eq 403 -or ($r.code -eq 400 -and $r.message -like '*责任人*')) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan" 'C01' @{ plan='方案太短' } 'smoke-owner'
Check '方案不足20字E-SR-01' ($r.code -eq 400 -and $r.message -like '*20字*') ($r | ConvertTo-Json -Compress)
$plan1 = 'SMOKE整改方案：一、开展月度文化复盘会；二、建立短板问题台账周跟踪；三、责任人分工明确月底见效。'
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan" 'C01' @{ plan=$plan1 } 'smoke-owner'
Check '责任人提交方案待审核' ($r.code -eq 200 -and $r.data.status -eq '方案待审核') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan-review?pass=false&opinion=短" 'C03' $null 'smoke-c03'
Check '审核意见不足E-SR-14' ($r.code -eq 400 -and $r.message -like '*5字*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan-review?pass=false&opinion=SMOKE方案驳回测试：措施不够具体。" 'C03' $null 'smoke-c03'
Check '方案驳回回待方案提交' ($r.code -eq 200 -and $r.data.status -eq '待方案提交') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan" 'C01' @{ plan=$plan1 + '补充：增加文化培训与考核挂钩。' } 'smoke-owner'
Check '重新提交方案' ($r.code -eq 200 -and $r.data.status -eq '方案待审核') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan-review?pass=true&opinion=SMOKE方案可行，同意实施。" 'C03' $null 'smoke-c03'
Check '方案通过进整改实施中' ($r.code -eq 200 -and $r.data.status -eq '整改实施中') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/evidence" 'C01' @{ implementNote='说明太短'; evidence='[{"name":"a.pdf"}]' } 'smoke-owner'
Check '实施说明不足10字E-SR-01' ($r.code -eq 400 -and $r.message -like '*实施*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/evidence" 'C01' @{ implementNote='SMOKE已完成全部整改措施并附上相关材料。'; evidence='[]' } 'smoke-owner'
Check '验收材料缺失E-SR-19' ($r.code -eq 400 -and $r.message -like '*验收材料*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/evidence" 'C01' @{ implementNote='SMOKE已完成全部整改措施并附上相关材料。'; evidence='[{"name":"rectify-report.pdf","size":"1MB"}]' } 'smoke-owner'
Check '提交验收材料进待验收' ($r.code -eq 200 -and $r.data.status -eq '待验收') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/accept?pass=false&opinion=短" 'C03' $null 'smoke-c03'
Check '验收意见不足E-SR-14' ($r.code -eq 400 -and $r.message -like '*5字*') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/accept?pass=false&opinion=SMOKE验收驳回：佐证不充分需补充。" 'C03' $null 'smoke-c03'
Check '验收驳回重整改' ($r.code -eq 200 -and $r.data.status -eq '验收驳回重整改') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/evidence" 'C01' @{ implementNote='SMOKE补充整改后再次提交验收材料说明。'; evidence='[{"name":"rectify-report-v2.pdf","size":"2MB"}]' } 'smoke-owner'
Check '驳回后补材料回待验收' ($r.code -eq 200 -and $r.data.status -eq '待验收') ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/accept?pass=true&opinion=SMOKE验收通过，同意闭环。" 'C03' $null 'smoke-c03'
Check '验收通过闭环' ($r.code -eq 200 -and $r.data.status -eq '验收闭环' -and $r.data.closeTime) ($r | ConvertTo-Json -Compress)
$r = Send-Api 'POST' "/api/culture/survey-rectify/$rid/plan" 'C01' @{ plan=$plan1 } 'smoke-owner'
Check '闭环后状态非法E-SR-13' ($r.code -eq 400 -and $r.message -like '*不可提交方案*') ($r | ConvertTo-Json -Compress)
# 逾期整改
$r = Send-Api 'POST' '/api/culture/survey-rectify' 'C03' @{ title='SMOKE逾期整改任务'; dept='SMOKE逾期部'; owner='smoke-late'; deadline='2026-01-01' } 'smoke-c03'
$lateId = $r.data.id
Check '建逾期整改成功' ($r.code -eq 200 -and $lateId) ($r | ConvertTo-Json -Compress)
$od = Send-Api 'GET' '/api/culture/survey-rectify/list?overdueOnly=true' 'C03' $null
Check '逾期名单含SMOKE逾期整改' (@($od.data | Where-Object { $_.id -eq $lateId }).Count -eq 1) (@($od.data).Count)

# ========== 清理（恢复种子可重入） ==========
$clean = @'
UPDATE culture_mgt.cul_survey_rectify SET deleted=1 WHERE title LIKE 'SMOKE%';
UPDATE culture_mgt.cul_survey_response r
JOIN culture_mgt.cul_survey_questionnaire q ON r.questionnaire_id=q.id
SET r.deleted=1 WHERE q.title LIKE 'SMOKE%';
UPDATE culture_mgt.cul_survey_questionnaire SET deleted=1 WHERE title LIKE 'SMOKE%';
UPDATE culture_mgt.cul_survey_questionnaire SET deleted=1 WHERE creator LIKE 'smoke-%' AND template_flag=0;
UPDATE culture_mgt.cul_survey_dimension SET deleted=1 WHERE dimension_name LIKE 'SMOKE%';
DELETE FROM culture_mgt.cul_audit_log WHERE target_type LIKE 'cul_survey_%' AND
  (detail LIKE '%SMOKE%' OR operator LIKE 'smoke-%');
'@
$clean | & 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -uroot -p"Sun-0126King-0325" --default-character-set=utf8mb4 2>$null

Write-Host ""
Write-Host "==================== 冲烟结果 PASS=$($script:pass) FAIL=$($script:fail) ===================="
if ($script:failMsgs.Count -gt 0) { $script:failMsgs | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow } }
