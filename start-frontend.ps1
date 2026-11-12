$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot\frontend"
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) { Write-Host "Node.js 20+ (npm) is required." -ForegroundColor Red; exit 1 }
if (-not (Test-Path "node_modules")) { npm install }
npm run dev
