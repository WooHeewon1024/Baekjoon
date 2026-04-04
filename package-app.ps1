$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$binDir = Join-Path $root 'bin'
$distDir = Join-Path $root 'dist'
$jarDir = Join-Path $distDir 'package'
$jarPath = Join-Path $jarDir 'SunDirectionApp.jar'
$appImageDir = Join-Path $distDir 'SunDirectionApp'
$iconScript = Join-Path $root 'generate-assets.ps1'
$iconIco = Join-Path $root 'assets\sun-direction-icon.ico'
$dotnetInstallerScript = Join-Path $root 'build-dotnet-installer.ps1'
$sourceFiles = Get-ChildItem -Path (Join-Path $root 'src') -Filter '*.java' | ForEach-Object { $_.FullName }

if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Path $binDir | Out-Null
}

if (Test-Path $distDir) {
    Remove-Item $distDir -Recurse -Force
}

New-Item -ItemType Directory -Path $jarDir | Out-Null

& powershell -ExecutionPolicy Bypass -File $iconScript

& javac -d $binDir $sourceFiles
& jar --create --file $jarPath --main-class App -C $binDir .

& jpackage `
    --type app-image `
    --input $jarDir `
    --main-jar 'SunDirectionApp.jar' `
    --name 'SunDirectionApp' `
    --dest $distDir `
    --vendor 'GitHub Copilot' `
    --app-version '1.0.0' `
    --icon $iconIco

$zipPath = Join-Path $distDir 'SunDirectionApp.zip'
Compress-Archive -Path $appImageDir -DestinationPath $zipPath -Force

& powershell -ExecutionPolicy Bypass -File $dotnetInstallerScript

Write-Host "Packaged app created: $appImageDir"
Write-Host "Zip created: $zipPath"
Write-Host "Installer created: $(Join-Path $distDir 'SunDirectionApp-Installer.exe')"