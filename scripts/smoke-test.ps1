$ErrorActionPreference = 'Stop'
$healthUrl = if ($env:LEDGERLINE_API_URL) { "$($env:LEDGERLINE_API_URL.TrimEnd('/'))/actuator/health" } else { 'http://localhost:8080/actuator/health' }
try {
    $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 10
    if ($response.status -ne 'UP') { throw "Health endpoint did not report UP: $($response | ConvertTo-Json -Compress)" }
    Write-Host "LedgerLine API is healthy: $healthUrl" -ForegroundColor Green
} catch {
    Write-Error "LedgerLine health check failed at $healthUrl. Start PostgreSQL and the backend first. $($_.Exception.Message)"
    exit 1
}
