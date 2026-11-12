$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot\backend"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { Write-Host "Maven 3.9+ is required. Install Maven, then run this script again." -ForegroundColor Red; exit 1 }
mvn spring-boot:run
