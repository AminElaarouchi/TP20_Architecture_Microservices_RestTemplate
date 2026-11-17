$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$logs = Join-Path $root 'target/smoke-test'
New-Item -ItemType Directory -Force -Path $logs | Out-Null
$started = @()

function Start-ServiceJar([string]$module, [string]$profile = '') {
    $jar = Join-Path $root "$module/target/$module-1.0-SNAPSHOT.jar"
    if (-not (Test-Path $jar)) { throw "JAR absent: $jar. Lancez mvn package." }
    $arguments = @('-jar', $jar)
    if ($profile) { $arguments += "--spring.profiles.active=$profile" }
    $process = Start-Process -FilePath 'java' -ArgumentList $arguments -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logs "$module.log") `
        -RedirectStandardError (Join-Path $logs "$module.err.log")
    $script:started += $process
}

function Wait-Endpoint([string]$url, [int]$seconds = 120) {
    $deadline = (Get-Date).AddSeconds($seconds)
    do {
        try { return Invoke-RestMethod -Uri $url -TimeoutSec 3 }
        catch { Start-Sleep -Seconds 2 }
    } while ((Get-Date) -lt $deadline)
    throw "Délai dépassé pour $url. Voir $logs"
}

try {
    Start-ServiceJar 'eureka-server'
    Wait-Endpoint 'http://localhost:8761/' | Out-Null
    Start-ServiceJar 'service-client' 'demo'
    Wait-Endpoint 'http://localhost:8081/api/client' | Out-Null
    Start-ServiceJar 'service-car' 'demo'
    Wait-Endpoint 'http://localhost:8082/api/car' | Out-Null
    Start-ServiceJar 'api-gateway'
    Wait-Endpoint 'http://localhost:8888/actuator/health' | Out-Null

    $client = Invoke-RestMethod -Method Post -Uri 'http://localhost:8081/api/client' `
        -ContentType 'application/json' -Body '{"nom":"Ahmed","age":30}'
    $body = @{ brand = 'Toyota'; model = 'Corolla'; matricule = 'A-123'; clientId = $client.id } | ConvertTo-Json -Compress
    $car = Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/api/car' `
        -ContentType 'application/json' -Body $body
    $result = Wait-Endpoint "http://localhost:8888/SERVICE-CAR/api/car/$($car.id)"
    if ($result.client.nom -ne 'Ahmed' -or $result.brand -ne 'Toyota') {
        throw "Réponse inattendue: $($result | ConvertTo-Json -Compress)"
    }
    Write-Output ($result | ConvertTo-Json -Depth 5 -Compress)
    Write-Output 'TP20 end-to-end: OK (Eureka, Gateway, RestTemplate, H2)'
}
finally {
    foreach ($process in $started) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
}
