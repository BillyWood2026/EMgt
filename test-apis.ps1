$base = 'http://localhost'
$apis = @(
    @{Port=8086; Path='/api/recruitment/requirement/list'; Name='Recruitment-Req'},
    @{Port=8086; Path='/api/recruitment/rec02/list'; Name='Recruitment-Candidate'},
    @{Port=8083; Path='/api/hr-archive/employee'; Name='Employee'},
    @{Port=8085; Path='/api/perf/template/list'; Name='Perf-Kpi'},
    @{Port=8084; Path='/api/attendance/clock-in'; Name='Attendance-Daily'},
    @{Port=8090; Path='/api/standard/list'; Name='Qual-Standard'},
    @{Port=8087; Path='/api/training/course'; Name='Training-Course'},
    @{Port=8094; Path='/api/evaluation/employee/search'; Name='Talent-Eval'},
    @{Port=8093; Path='/api/skill-cert/standard/list'; Name='SkillCert-Std'},
    @{Port=8092; Path='/api/salary/matrix-package/list'; Name='Salary-Matrix'},
    @{Port=8095; Path='/api/hr-planning/proposal/page'; Name='HRPlan-Proposal'},
    @{Port=9000; Path='/api/module-config/enabled'; Name='ModuleConfig'}
)

foreach ($api in $apis) {
    $url = "$base`:$($api.Port)$($api.Path)"
    try {
        $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        $body = $resp.Content
        if ($body.Length -gt 200) { $body = $body.Substring(0, 200) + '...' }
        Write-Host "$($api.Name) ($($api.Port)): $($resp.StatusCode) - $body"
    } catch {
        $code = $_.Exception.Response.StatusCode.value__
        Write-Host "$($api.Name) ($($api.Port)): ERROR $code - $($_.Exception.Message)"
    }
}
