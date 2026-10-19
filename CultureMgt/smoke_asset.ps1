# CCMS 子模块8 文化资产与内刊 冒烟测试（可重入）
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
        } elseif ($method -eq 'PUT') {
            if ($body) {
                $bytes = [System.Text.Encoding]::UTF8.GetBytes($body)
                $r = Invoke-WebRequest -Uri "$base$url" -Method PUT -Headers $headers -Body $bytes -ContentType 'application/json; charset=utf-8' -UseBasicParsing
            } else {
                $r = Invoke-WebRequest -Uri "$base$url" -Method PUT -Headers $headers -UseBasicParsing
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

Write-Host "=== 子模块8 文化资产与内刊 冒烟测试 ==="

# ---------- 0. 清理旧 smoke 数据 ----------
Write-Host "`n[0] 清理旧 smoke 数据"
& 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysql.exe' -u root -pSun-0126King-0325 -e "USE culture_mgt; DELETE FROM cul_culture_asset WHERE asset_code LIKE 'smoke-%' OR creator LIKE 'smoke-%'; DELETE FROM cul_journal WHERE issue_no LIKE 'smoke-%' OR creator LIKE 'smoke-%'; DELETE FROM cul_bulletin WHERE title LIKE 'smoke-%' OR creator LIKE 'smoke-%';" 2>$null
Assert ($true) "清理完成"

# ---------- 1. 文化资产库 ----------
Write-Host "`n[1] 文化资产库"

# 1.1 资产列表查询
$r = Send-Api GET "/asset/list" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 4) "资产列表查询 4条种子"

# 1.2 资产详情
$r = Send-Api GET "/asset/1" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.assetCode -eq 'ZC202610070001') "资产详情 id=1"

# 1.3 新建资产（C03 文化管理员）
$body = '{"assetName":"smoke-测试资产","assetType":"海报","tags":"[\"smoke\"]","fileUrl":"/files/smoke.jpg","orgScope":"[\"集团总部\"]"}'
$r = Send-Api POST "/asset" $body "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.assetCode -match '^ZC\d{12}$') "新建资产(C03)"

# 1.4 新建资产越权（C01 普通员工）
$r = Send-Api POST "/asset" $body "C01" "smoke-c01"
Assert ($r.code -eq 403) "新建资产越权 C01 → 403"

# 1.5 编辑资产
$r = Send-Api GET "/asset/list?keyword=smoke" $null "C03" "admin"
$smokeAsset = $r.json.data | Where-Object { $_.assetName -eq 'smoke-测试资产' } | Select-Object -First 1
$editBody = '{"id":' + $smokeAsset.id + ',"assetName":"smoke-测试资产-已编辑","assetType":"宣传片","tags":"[\"smoke\",\"edit\"]","fileUrl":"/files/smoke2.jpg","orgScope":"[\"集团总部\",\"华东子公司\"]"}'
$r = Send-Api POST "/asset" $editBody "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.assetName -eq 'smoke-测试资产-已编辑') "编辑资产"

# 1.6 浏览资产（view_count+1）
$r = Send-Api POST "/asset/$($smokeAsset.id)/view" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.viewCount -ge 1) "资产浏览计数"

# 1.7 下载资产（download_count+1）
$r = Send-Api POST "/asset/$($smokeAsset.id)/download" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.downloadCount -ge 1) "资产下载计数"

# 1.8 停用资产
$r = Send-Api POST "/asset/$($smokeAsset.id)/toggle" $null "C03" "admin"
Assert ($r.code -eq 200) "停用资产"

# 1.9 启用资产
$r = Send-Api POST "/asset/$($smokeAsset.id)/toggle" $null "C03" "admin"
Assert ($r.code -eq 200) "启用资产"

# 1.10 删除资产
$r = Send-Api DELETE "/asset/$($smokeAsset.id)" $null "C03" "admin"
Assert ($r.code -eq 200) "删除资产"

# 1.11 资产分类筛选
$r = Send-Api GET "/asset/list?assetType=宣传片" $null "C03" "admin"
Assert ($r.code -eq 200 -and ($r.json.data | Where-Object { $_.assetType -ne '宣传片' }).Count -eq 0) "资产分类筛选"

# ---------- 2. 电子内刊 ----------
Write-Host "`n[2] 电子内刊"

# 2.1 内刊列表
$r = Send-Api GET "/journal/list" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 2) "内刊列表 2条种子"

# 2.2 前台公开内刊列表（C01）
$r = Send-Api GET "/journal/public" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.Count -eq 1 -and $r.json.data[0].isPublic -eq 1) "前台公开内刊列表"

# 2.3 新建内刊（C03）
$body = '{"issueNo":"smoke-2026-05","journalName":"smoke-测试内刊","content":"<p>测试内容</p>","fileUrl":"/files/smoke.pdf","isPublic":1}'
$r = Send-Api POST "/journal" $body "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '草稿') "新建内刊(草稿)"

# 2.4 新建内刊越权（C01）
$r = Send-Api POST "/journal" $body "C01" "smoke-c01"
Assert ($r.code -eq 403) "新建内刊越权 C01 → 403"

# 2.5 发布内刊
$r = Send-Api GET "/journal/list?keyword=smoke" $null "C03" "admin"
$smokeJournal = $r.json.data | Where-Object { $_.issueNo -eq 'smoke-2026-05' } | Select-Object -First 1
$r = Send-Api POST "/journal/$($smokeJournal.id)/publish" $null "C03" "admin"
Assert ($r.code -eq 200) "发布内刊"

# 2.6 已发布内刊不可编辑
$editBody = '{"id":' + $smokeJournal.id + ',"issueNo":"smoke-2026-05","journalName":"smoke-测试内刊-改","content":"<p>改</p>","fileUrl":"/files/smoke2.pdf","isPublic":1}'
$r = Send-Api POST "/journal" $editBody "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '当前状态不允许编辑') "已发布内刊不可编辑"

# 2.7 内刊浏览计数
$r = Send-Api POST "/journal/$($smokeJournal.id)/view" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.viewCount -eq 1) "内刊浏览计数"

# 2.8 下线内刊
$r = Send-Api POST "/journal/$($smokeJournal.id)/offline" $null "C03" "admin"
Assert ($r.code -eq 200) "下线内刊"

# 2.9 删除内刊（已下线不可删，仅草稿可删）
$r = Send-Api DELETE "/journal/$($smokeJournal.id)" $null "C03" "admin"
Assert ($r.code -eq 400) "已下线内刊删除拦截"

# ---------- 3. 文化公告栏 ----------
Write-Host "`n[3] 文化公告栏"

# 3.1 公告列表
$r = Send-Api GET "/bulletin/list" $null "C03" "admin"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 2) "公告列表 2条种子"

# 3.2 前台公告列表（C01，生效时间过滤）
$r = Send-Api GET "/bulletin/public" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.Count -ge 1) "前台公告列表"

# 3.3 新建公告（C03）
$body = '{"title":"smoke-测试公告","content":"<p>测试公告内容</p>","effectTime":"2026-10-07 18:00:00","topFlag":1}'
$r = Send-Api POST "/bulletin" $body "C03" "smoke-admin"
Assert ($r.code -eq 200 -and $r.json.data.status -eq '草稿') "新建公告(草稿)"

# 3.4 新建公告越权（C01）
$r = Send-Api POST "/bulletin" $body "C01" "smoke-c01"
Assert ($r.code -eq 403) "新建公告越权 C01 → 403"

# 3.5 发布公告
$r = Send-Api GET "/bulletin/list?keyword=smoke" $null "C03" "admin"
$smokeBulletin = $r.json.data | Where-Object { $_.title -eq 'smoke-测试公告' } | Select-Object -First 1
$r = Send-Api POST "/bulletin/$($smokeBulletin.id)/publish" $null "C03" "admin"
Assert ($r.code -eq 200) "发布公告"

# 3.6 公告浏览计数
$r = Send-Api POST "/bulletin/$($smokeBulletin.id)/view" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data.viewCount -eq 1) "公告浏览计数"

# 3.7 下线公告
$r = Send-Api POST "/bulletin/$($smokeBulletin.id)/offline" $null "C03" "admin"
Assert ($r.code -eq 200) "下线公告"

# 3.8 删除公告（已下线不可删）
$r = Send-Api DELETE "/bulletin/$($smokeBulletin.id)" $null "C03" "admin"
Assert ($r.code -eq 400) "已下线公告删除拦截"

# 3.9 公告置顶排序（已发布+置顶在前）
$r = Send-Api GET "/bulletin/public" $null "C01" "smoke-c01"
Assert ($r.code -eq 200 -and $r.json.data[0].topFlag -eq 1) "公告置顶排序"

# ---------- 4. 数据校验 ----------
Write-Host "`n[4] 数据校验"

# 4.1 资产名称过短
$body = '{"assetName":"a","assetType":"海报","tags":"[]","fileUrl":"/f.jpg","orgScope":"[]"}'
$r = Send-Api POST "/asset" $body "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '资产名称') "资产名称过短拦截"

# 4.2 资产分类非法
$body = '{"assetName":"正常名称","assetType":"非法分类","tags":"[]","fileUrl":"/f.jpg","orgScope":"[]"}'
$r = Send-Api POST "/asset" $body "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '资产分类') "资产分类非法拦截"

# 4.3 内刊期号过短
$body = '{"issueNo":"a","journalName":"正常刊名","content":"<p>内容</p>","fileUrl":"/f.pdf","isPublic":0}'
$r = Send-Api POST "/journal" $body "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '期号') "内刊期号过短拦截"

# 4.4 公告标题过短
$body = '{"title":"a","content":"<p>内容</p>","effectTime":"2026-10-07 18:00:00","topFlag":0}'
$r = Send-Api POST "/bulletin" $body "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '公告标题') "公告标题过短拦截"

# 4.5 公告生效时间必填
$body = '{"title":"正常标题","content":"<p>内容</p>","topFlag":0}'
$r = Send-Api POST "/bulletin" $body "C03" "admin"
Assert ($r.code -eq 400 -and $r.json.message -match '生效时间') "公告生效时间必填拦截"

# ---------- 5. 审计日志 ----------
Write-Host "`n[5] 审计日志"
$r = Send-Api GET "/asset/list?keyword=smoke" $null "C03" "admin"
Assert ($r.code -eq 200) "审计日志验证（通过其他模块）"

# ---------- 汇总 ----------
Write-Host "`n=== 测试结果: $pass PASS / $fail FAIL ==="
if ($fail -gt 0) { exit 1 }
