$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "Starting mobile web app server at http://localhost:8080/mobile-web/"
python -m http.server 8080
