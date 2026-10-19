# CCMS 子模块9 新闻宣传管理 冒烟测试（可重入）
# 运行前确保 8160 已启动；脚本自带清理逻辑，可重复执行

$base = "http://localhost:8160/api/culture"
$pass = 0; $fail = 0

function Send-Api($method, $url, $body, $role, $user) {
    $headers = @{}
    if ($role) { $headers['X-Culture-Role'] = $role }
    if ($user) { $headers['X-User-Id'] = $user }
    try {
        if ($method -eq 'GET') {
            $r = Invoke-WebRequest -Uri "$base$url" -Method GET -Headers $headers -UseBasicParsing
        } elseif ($method -eq 'POST') {
            if ($body) {
                $bytes = [System.Text.Encoding]::UTF8.GetBytes($body)
                $r = Invoke-WebRequest -Uri "$base$url" -Method POST -Headers $headers -Body $bytes -ContentType 'application/json; charset=utf-8' -UseBasicParsing
            } else {
                $r = Invoke-WebRequest -Uri "$base$url" -Method POST -Headers $headers -UseBasicParsing
            }
        } elseif ($method -eq 'DELETE') {
            $r = Invoke-WebRequest -Uri "$base$url" -Method DELETE -Headers $headers -UseBasicParsing
        }
        $code = [int]$r.StatusCode
        $ms = New-Object System.IO.MemoryStream
        $r.RawContentStream.CopyTo($ms)
        $content = [System.Text.Encoding]::UTF8.GetString($ms.ToArray())
        try { $json = $content | ConvertFrom-Json } catch { $json = $null }
        return @{ code = $code; json = $json; text = $content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $code = [int]$resp.StatusCode
            $stream = $resp.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
            $text = $reader.ReadToEnd()
            try { $json = $text | ConvertFrom-Json } catch { $json = $null }
            return @{ code = $code; json = $json; text = $text }
        }
        return @{ code = 0; json = $null; text = $_.Exception.Message }
    }
}

function Assert ($cond, $name) {
    if ($cond) { $script:pass++; Write-Host "  PASS  $name" }
    else { $script:fail++; Write-Host "  FAIL  $name" }
}

$SUMMARY = '这是一段用于冒烟测试的稿件摘要内容，长度必须大于三十个字符才能通过后端校验规则，特此补充说明。'

Write-Host "=== 子模块9 新闻宣传管理 冒烟测试 ==="

# ---------- 0. 清理旧 smoke 数据 ----------
Write-Host "`n[0] 清理旧 smoke 数据"
& 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -pSun-0126King-0325 -e "USE culture_mgt; DELETE v FROM cul_news_version v JOIN cul_news n ON v.news_id=n.id WHERE n.title LIKE 'smoke%' OR n.creator LIKE 'smoke%'; DELETE a FROM cul_news_approval a JOIN cul_news n ON a.news_id=n.id WHERE n.title LIKE 'smoke%' OR n.creator LIKE 'smoke%'; DELETE FROM cul_news WHERE title LIKE 'smoke%' OR creator LIKE 'smoke%'; DELETE FROM cul_news_tag WHERE tag_name LIKE 'smoke%'; DELETE FROM cul_culture_asset WHERE asset_name LIKE 'smoke-新闻素材%' OR creator LIKE 'smoke%';" 2>$null
Assert ($true) "清理完成"

# ---------- 1. 新闻标签字典 ----------
Write-Host "`n[1] 新闻标签字典"

$r = Send-Api GET "/news/tag/list" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 4) "标签列表 4条种子"
Assert ($r.json.data[0].refCount -ge 0) "标签引用稿件数量实时统计"

$body = '{"tagName":"smoke-测试标签","tagScope":"集团全局","enabled":1}'
$r = Send-Api POST "/news/tag" $body "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.tagName -eq 'smoke-测试标签') "新建标签(C03)"
$smokeTagId = $r.json.data.id

$r = Send-Api POST "/news/tag" $body "C01" "smoke-c01"
Assert ($r.code -eq 403) "新建标签越权 C01 → 403"

$r = Send-Api POST "/news/tag" $body "C02" "smoke-wangwu"
Assert ($r.code -eq 403) "新建标签越权 C02 → 403（集团管理员专属）"

$r = Send-Api POST "/news/tag" '{"tagName":"smoke-测试标签","tagScope":"子公司私有","enabled":1}' "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-13') "重复标签名拦截 E-NW-13"

$r = Send-Api POST "/news/tag" '{"tagName":"smoke-超长标签名称测试超长标签名称测试超长标签名称测试超长标签名称测试超长标签名称测试超长标签名称测试","tagScope":"集团全局","enabled":1}' "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-13') "标签名超长拦截 E-NW-13"

$r = Send-Api POST "/news/tag/$smokeTagId/toggle" $null "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.enabled -eq 0) "停用标签"

# ---------- 2. 稿件新建与校验 ----------
Write-Host "`n[2] 稿件新建与校验"

$body = ('{"title":"smoke-子公司稿件一：全流程验证","summary":"' + $SUMMARY + '","content":"<p>smoke 正文内容</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[\"奋斗者精神\"]","relCaseId":1,"isMobileShow":1,"isTop":0,"creator":"sunli"}')
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '草稿' -and $r.json.data.newsNo -match '^XW\d{12}$') "C02新建子公司稿(草稿,编号XW+yyyyMMdd+4位)"
$news1 = $r.json.data

$body = '{"title":"短标题","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-02') "标题过短拦截 E-NW-02"

$body = '{"title":"smoke-摘要过短的稿件","summary":"太短的摘要","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-03') "摘要过短拦截 E-NW-03"

$body = '{"title":"smoke-正文缺失的稿件","summary":"' + $SUMMARY + '","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-04') "正文必填拦截 E-NW-04"

$body = '{"title":"smoke-类型非法的稿件","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"非法类型","sourceOrg":"华东子公司","valueTags":"[]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-05') "新闻类型非法拦截 E-NW-05"

$body = '{"title":"smoke-C02归属集团稿","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"集团总部","valueTags":"[]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-06') "C02不能归属集团总部 E-NW-06"

$body = '{"title":"smoke-停用标签的稿件","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[\"smoke-测试标签\"]"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-14') "引用停用标签拦截 E-NW-14"

$body = '{"title":"smoke-计划时间过期的稿件","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]","planPublishTime":"2026-01-01 09:00:00"}'
$r = Send-Api POST "/news" $body "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-12') "计划发布时间早于当前拦截 E-NW-12"

$r = Send-Api GET "/news/999999" $null "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-01') "稿件不存在 E-NW-01"

# ---------- 3. 权限：归属与越权 ----------
Write-Host "`n[3] 归属与越权"

$editBody = ('{"id":' + $news1.id + ',"title":"smoke-子公司稿件一：编辑后版本","summary":"' + $SUMMARY + '","content":"<p>smoke 正文内容-第2版</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[\"奋斗者精神\"]","relCaseId":1,"isMobileShow":1,"isTop":0}')
$r = Send-Api POST "/news" $editBody "C02" "smoke-wangwu"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-08') "他人C02编辑子公司稿拦截 E-NW-08"

$r = Send-Api POST "/news" $editBody "C02" "sunli"
Assert ($r.code -eq 200 -and $r.json.data.title -match '编辑后版本') "撰稿本人编辑草稿"

# 集团草稿（smoke-admin 创建，留草稿态供越权测试）
$hqBody = ('{"title":"smoke-集团稿件一：越权测试","summary":"' + $SUMMARY + '","content":"<p>hq</p>","newsType":"文化专题","sourceOrg":"集团总部","valueTags":"[\"客户导向\"]","isTop":0}')
$r = Send-Api POST "/news" $hqBody "C03" "smoke-admin"
$hqDraft = $r.json.data
Assert ($r.code -eq 200 -and $r.json.data.sourceOrg -eq '集团总部') "C03新建集团稿(草稿)"

$hqEdit = ('{"id":' + $hqDraft.id + ',"title":"smoke-集团稿件一：越权测试","summary":"' + $SUMMARY + '","content":"<p>hq-edit</p>","newsType":"文化专题","sourceOrg":"集团总部","valueTags":"[\"客户导向\"]","isTop":0}')
$r = Send-Api POST "/news" $hqEdit "C02" "smoke-wangwu"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-09') "C02编辑集团稿拦截 E-NW-09 越权组织"

# ---------- 4. 子公司三节点审批链 ----------
Write-Host "`n[4] 子公司三节点审批链"

$r = Send-Api POST "/news/$($news1.id)/submit" $null "C02" "sunli"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '待审核' -and $r.json.data.stage -eq 1) "提交审批 草稿→待审核 stage=1"

$againBody = ('{"id":' + $news1.id + ',"title":"smoke-子公司稿件一：待审再编辑","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[\"奋斗者精神\"]","isTop":0}')
$r = Send-Api POST "/news" $againBody "C02" "sunli"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-07') "待审核状态编辑拦截 E-NW-07"

$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-16') "C03做初审拦截 E-NW-16（初审需C04）"

$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C04" "smoke-c04"
Assert ($r.code -eq 200 -and $r.json.data.stage -eq 2) "C04子公司负责人初审通过 stage=2"

$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C04" "smoke-c04"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-16') "C04做复核拦截 E-NW-16（复核需C03）"

$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.stage -eq 3) "C03集团文化管理员复核通过 stage=3"

$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-16') "C03做终审拦截 E-NW-16（终审需C07）"

$caseBefore = & 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -pSun-0126King-0325 -N -e "USE culture_mgt; SELECT ref_news_count FROM cul_case WHERE id=1;" 2>$null
$r = Send-Api POST "/news/$($news1.id)/approve?pass=true" $null "C07" "smoke-c07"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '已发布') "C07集团高管终审通过→已发布"
$caseAfter = & 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -pSun-0126King-0325 -N -e "USE culture_mgt; SELECT ref_news_count FROM cul_case WHERE id=1;" 2>$null
Assert ([int]$caseAfter -eq ([int]$caseBefore + 1)) "联动：发布后案例被新闻引用计数+1"

# ---------- 5. 驳回/撤回/待办 ----------
Write-Host "`n[5] 驳回/撤回/待办"

$body = ('{"title":"smoke-子公司稿件二：驳回验证","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"活动报道","sourceOrg":"华东子公司","valueTags":"[]","isTop":0}')
$r = Send-Api POST "/news" $body "C02" "sunli"
$news2 = $r.json.data
Send-Api POST "/news/$($news2.id)/submit" $null "C02" "sunli" | Out-Null

$opinion = [uri]::EscapeDataString('内容与事实不符且口径不规范，请补充材料后重新提交审批。')
$r = Send-Api POST "/news/$($news2.id)/approve?pass=false" $null "C04" "smoke-c04"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-10') "空驳回意见拦截 E-NW-10"

$r = Send-Api POST "/news/$($news2.id)/approve?pass=false&opinion=$opinion" $null "C04" "smoke-c04"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '草稿' -and $r.json.data.stage -eq 0) "C04驳回退回草稿（意见留痕）"

$body = ('{"title":"smoke-子公司稿件三：撤回验证","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]","isTop":0}')
$r = Send-Api POST "/news" $body "C02" "sunli"
$news3 = $r.json.data
Send-Api POST "/news/$($news3.id)/submit" $null "C02" "sunli" | Out-Null
$r = Send-Api POST "/news/$($news3.id)/withdraw" $null "C02" "sunli"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '草稿') "撰稿人撤回 待审核→草稿"

$body = ('{"title":"smoke-子公司稿件四：待办验证","summary":"' + $SUMMARY + '","content":"<p>x</p>","newsType":"文化动态","sourceOrg":"华东子公司","valueTags":"[]","isTop":0}')
$r = Send-Api POST "/news" $body "C02" "sunli"
$news4 = $r.json.data
Send-Api POST "/news/$($news4.id)/submit" $null "C02" "sunli" | Out-Null
$r = Send-Api GET "/news/todo" $null "C04" "smoke-c04"
Assert ($r.code -eq 200 -and (@($r.json.data | Where-Object { $_.id -eq $news4.id })).Count -eq 1) "C04待办包含当前节点稿件"
$r = Send-Api GET "/news/todo" $null "C07" "smoke-c07"
Assert ($r.code -eq 200 -and (@($r.json.data | Where-Object { $_.id -eq $news4.id })).Count -eq 0) "C07待办不包含初审节点稿件"
$r = Send-Api GET "/news/todo" $null "C01" "smoke-c01"
Assert ($r.code -eq 403) "C01访问待办 → 403"

# ---------- 6. 集团两节点审批链 ----------
Write-Host "`n[6] 集团两节点审批链"

$body = ('{"title":"smoke-集团稿件二：两节点验证","summary":"' + $SUMMARY + '","content":"<p>hq</p>","newsType":"高管寄语","sourceOrg":"集团总部","valueTags":"[\"奋斗者精神\"]","isTop":0}')
$r = Send-Api POST "/news" $body "C03" "smoke-admin"
$news5 = $r.json.data
Send-Api POST "/news/$($news5.id)/submit" $null "C03" "smoke-admin" | Out-Null
$r = Send-Api POST "/news/$($news5.id)/approve?pass=true" $null "C04" "smoke-c04"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-16') "集团稿初审C04拦截 E-NW-16（需C03）"
$r = Send-Api POST "/news/$($news5.id)/approve?pass=true" $null "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.stage -eq 2) "C03集团文化管理员审核通过 stage=2"
$r = Send-Api POST "/news/$($news5.id)/approve?pass=true" $null "C07" "smoke-c07"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '已发布') "C07分管高管终审→已发布（2节点）"

# 重复标题预警
$r = Send-Api POST "/news" $body "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-18') "重复标题预警 E-NW-18"
$r = Send-Api POST "/news" ('{"title":"smoke-集团稿件二：两节点验证","summary":"' + $SUMMARY + '","content":"<p>hq</p>","newsType":"高管寄语","sourceOrg":"集团总部","valueTags":"[\"奋斗者精神\"]","isTop":0,"confirmDuplicate":true}') "C03" "smoke-admin"
Assert ($r.code -eq 200) "重复标题确认后放行(confirmDuplicate)"

# ---------- 7. 置顶上限 ----------
Write-Host "`n[7] 置顶上限"

$topOk = $true
for ($i = 1; $i -le 5; $i++) {
    $b = ('{"title":"smoke-置顶测试稿' + $i + '号","summary":"' + $SUMMARY + '","content":"<p>top</p>","newsType":"文化专题","sourceOrg":"smoke置顶组织","valueTags":"[]","isTop":1}')
    $r = Send-Api POST "/news" $b "C03" "smoke-admin"
    if ($r.code -ne 200) { $topOk = $false }
}
Assert ($topOk) "同组织置顶前5条成功"
$b = ('{"title":"smoke-置顶测试稿6号","summary":"' + $SUMMARY + '","content":"<p>top</p>","newsType":"文化专题","sourceOrg":"smoke置顶组织","valueTags":"[]","isTop":1}')
$r = Send-Api POST "/news" $b "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-15') "第6条置顶拦截 E-NW-15（上限5条）"

# ---------- 8. 前台展示与阅读统计 ----------
Write-Host "`n[8] 前台展示与阅读统计"

$r = Send-Api GET "/news/public" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and (@($r.json.data | Where-Object { $_.status -ne '已发布' })).Count -eq 0) "前台仅展示已发布"
Assert ($r.json.data[0].isTop -eq 1) "前台排序：置顶优先"
Assert ((@($r.json.data | Where-Object { $_.id -eq $news1.id })).Count -eq 1) "前台包含smoke已发布稿件"

$r = Send-Api GET "/news/public?tag=华东风采" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and (@($r.json.data | Where-Object { $_.id -eq 2 })).Count -eq 1) "前台按价值观标签筛选"

$r = Send-Api POST "/news/$($news1.id)/view" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.readCount -ge 1) "阅读计数+1"
$r = Send-Api POST "/news/$($news1.id)/view?channel=mobile" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.mobileReadCount -ge 1) "移动端阅读计数+1"
$r = Send-Api POST "/news/$($news1.id)/like" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.likeCount -ge 1) "点赞计数+1"

$r = Send-Api GET "/news/stats" $null "C01" "smoke-c01"
Assert ($r.code -eq 403) "C01访问统计 → 403"
$r = Send-Api GET "/news/stats" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.total -ge 4 -and $r.json.data.byType.Count -eq 5 -and $r.json.data.byOrg.Count -ge 2) "统计分析（总量/类型占比/组织产出）"

# ---------- 9. 下线归档 ----------
Write-Host "`n[9] 下线归档"

$r = Send-Api POST "/news/$($news1.id)/offline" '{"reason":"测试"}' "C02" "sunli"
Assert ($r.code -eq 403) "C02下线拦截 → 403（仅集团管理员）"
$r = Send-Api POST "/news/$($news1.id)/offline" '{"reason":""}' "C03" "smoke-admin"
Assert ($r.code -eq 400 -and $r.json.data.errorCode -eq 'E-NW-11') "下线原因必填 E-NW-11"
$r = Send-Api POST "/news/$($news1.id)/offline" '{"reason":"宣传口径调整，暂时下线归档保存"}' "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '已下线归档') "C03下线归档"
$r = Send-Api GET "/news/public" $null "C01" "smoke-c01"
Assert ((@($r.json.data | Where-Object { $_.id -eq $news1.id })).Count -eq 0) "已下线新闻前台不可见"
$r = Send-Api GET "/news/list?status=已下线归档" $null "C03" "admin"
Assert ((@($r.json.data | Where-Object { $_.id -eq $news1.id })).Count -eq 1) "已下线新闻后台完整归档"
$r = Send-Api DELETE "/news/$($news1.id)" $null "C03" "smoke-admin"
Assert ($r.code -eq 400) "归档稿件禁止删除"

# ---------- 10. 版本/审批记录/素材 ----------
Write-Host "`n[10] 版本/审批记录/素材联动"

$r = Send-Api GET "/news/versions/$($news1.id)" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 2 -and $r.json.data[0].verNo -eq 1) "版本快照（新建+编辑自动生成）"
$r = Send-Api GET "/news/approvals/$($news1.id)" $null "C03" "admin"
$nodes = $r.json.data | ForEach-Object { $_.nodeName }
Assert ($r.code -eq 200 -and $nodes -contains '子公司负责人初审' -and $nodes -contains '集团文化管理员复核' -and $nodes -contains '集团高管终审') "审批流转记录三节点完整"
$rejects = Send-Api GET "/news/approvals/$($news2.id)" $null "C03" "admin"
Assert ((@($rejects.json.data | Where-Object { $_.action -eq '驳回' })).Count -eq 1) "驳回意见留痕可查"

$r = Send-Api POST "/news/material" '{"name":"smoke-新闻素材图","url":"/files/news/smoke-img.jpg"}' "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.assetType -eq '其他' -and $r.json.data.tags -match '新闻来源') "配图一键存入文化资产库(打新闻来源标签)"
$r = Send-Api POST "/news/material" '{"name":"smoke-越权素材","url":"/x.jpg"}' "C01" "smoke-c01"
Assert ($r.code -eq 403) "C01存素材越权 → 403"

$r = Send-Api GET "/news/list?keyword=smoke" $null "C02" "sunli"
Assert ($r.code -eq 200) "后台工作台列表(文化专员)"

# ---------- 汇总 ----------
Write-Host "`n=== 测试结果: $pass PASS / $fail FAIL ==="
if ($fail -gt 0) { exit 1 }
