$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectPath = Join-Path $root 'installer\InstallerApp.csproj'
$publishDir = Join-Path $root 'dist\installer-publish'
$finalInstaller = Join-Path $root 'dist\SunDirectionApp-Installer.exe'

if (-not (Test-Path (Join-Path $root 'dist\SunDirectionApp.zip'))) {
    throw 'SunDirectionApp.zip not found. Build the app package first.'
}

if (Test-Path $publishDir) {
    Remove-Item $publishDir -Recurse -Force
}

& dotnet publish $projectPath -c Release -r win-x64 -p:PublishSingleFile=true -p:SelfContained=true -o $publishDir

$publishedExe = Join-Path $publishDir 'SunDirectionAppInstaller.exe'
if (-not (Test-Path $publishedExe)) {
    throw 'Installer publish output not found.'
}

Copy-Item $publishedExe $finalInstaller -Force
Write-Host "Installer created: $finalInstaller"