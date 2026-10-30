$ErrorActionPreference = "Stop"
$projectRoot = $PSScriptRoot
$mysqlBin = Join-Path $projectRoot ".tools\mysql\mysql-8.4.11-winx64\bin"
$mysqlConfig = Join-Path $projectRoot ".tools\mysql\my.ini"
$maven = Join-Path $projectRoot ".tools\apache-maven-3.9.16\bin\mvn.cmd"
$mysqlExe = Join-Path $mysqlBin "mysql.exe"
$serverExe = Join-Path $mysqlBin "mysqld.exe"

foreach ($path in @($mysqlExe, $serverExe, $mysqlConfig, $maven)) {
    if (-not (Test-Path $path)) { throw "Required project tool not found: $path" }
}

try {
    $response = Invoke-WebRequest "http://127.0.0.1:8081/" -UseBasicParsing -TimeoutSec 2
    if ($response.StatusCode -eq 200) {
        Write-Host "TaskFlow is already running at http://localhost:8081"
        return
    }
} catch { }

if (-not (Get-Process -Name mysqld -ErrorAction SilentlyContinue)) {
    $configArg = "--defaults-file=`"$mysqlConfig`""
    Start-Process -FilePath $serverExe -ArgumentList $configArg -WindowStyle Hidden
}

$databaseReady = $false
$prevEAP = $ErrorActionPreference
$prevPSNative = if (Test-Path Variable:PSNativeCommandUseErrorActionPreference) { $PSNativeCommandUseErrorActionPreference } else { $null }
$ErrorActionPreference = "SilentlyContinue"
if ($null -ne $prevPSNative) { $PSNativeCommandUseErrorActionPreference = $false }

try {
    for ($attempt = 0; $attempt -lt 45; $attempt++) {
        try {
            & $mysqlExe --host=127.0.0.1 --port=3306 --user=root --execute="SELECT 1" 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { $databaseReady = $true; break }
        } catch { }
        Start-Sleep -Seconds 1
    }
} finally {
    $ErrorActionPreference = $prevEAP
    if ($null -ne $prevPSNative) { $PSNativeCommandUseErrorActionPreference = $prevPSNative }
}
if (-not $databaseReady) { throw "Local MySQL did not start. Check .tools\mysql\stderr.log" }

$env:JAVA_HOME = Join-Path $env:USERPROFILE ".antigravity-ide\extensions\oracle.oracle-java-27.0.0-universal\out\webviews\jdkDownloader\jdk_downloads\jdk-27"
if (-not (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) { throw "Java 17+ was not found at $env:JAVA_HOME" }
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:PORT = "8081"
Write-Host "Starting TaskFlow at http://localhost:8081 (Ctrl+C to stop the app)."
Push-Location $projectRoot
try { & $maven spring-boot:run } finally { Pop-Location }
