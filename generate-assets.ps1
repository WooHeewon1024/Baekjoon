$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$assetsDir = Join-Path $root 'assets'
$iconPng = Join-Path $assetsDir 'sun-direction-icon.png'
$iconIco = Join-Path $assetsDir 'sun-direction-icon.ico'

if (-not (Test-Path $assetsDir)) {
    New-Item -ItemType Directory -Path $assetsDir | Out-Null
}

$size = 256
$bitmap = New-Object System.Drawing.Bitmap $size, $size
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.Clear([System.Drawing.Color]::FromArgb(22, 22, 42))

$rect = New-Object System.Drawing.Rectangle 0, 0, $size, $size
$bgBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    $rect,
    [System.Drawing.Color]::FromArgb(24, 56, 109),
    [System.Drawing.Color]::FromArgb(113, 173, 232),
    315.0
)
$graphics.FillEllipse($bgBrush, 12, 12, 232, 232)

$ringPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(235, 235, 245)), 10
$graphics.DrawEllipse($ringPen, 20, 20, 216, 216)

$guidePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(130, 220, 235, 245)), 3
$guidePen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
$graphics.DrawEllipse($guidePen, 62, 62, 132, 132)

$northPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 105, 105)), 8
$graphics.DrawLine($northPen, 128, 28, 128, 54)

$arrowPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 224, 95)), 8
$arrowPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$arrowPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($arrowPen, 128, 128, 181, 91)

$arrowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 224, 95))
$arrowPoints = @(
    (New-Object System.Drawing.Point 181, 91),
    (New-Object System.Drawing.Point 159, 90),
    (New-Object System.Drawing.Point 171, 107)
)
$graphics.FillPolygon($arrowBrush, $arrowPoints)

$sunGlowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(90, 255, 200, 0))
$graphics.FillEllipse($sunGlowBrush, 160, 64, 42, 42)

$sunBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 230, 64))
$graphics.FillEllipse($sunBrush, 166, 70, 30, 30)

$sunPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 180, 0)), 3
$graphics.DrawEllipse($sunPen, 166, 70, 30, 30)

$rayPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 236, 120)), 3
$graphics.DrawLine($rayPen, 181, 58, 181, 66)
$graphics.DrawLine($rayPen, 181, 104, 181, 112)
$graphics.DrawLine($rayPen, 154, 85, 162, 85)
$graphics.DrawLine($rayPen, 200, 85, 208, 85)
$graphics.DrawLine($rayPen, 162, 66, 168, 72)
$graphics.DrawLine($rayPen, 194, 98, 200, 104)
$graphics.DrawLine($rayPen, 162, 104, 168, 98)
$graphics.DrawLine($rayPen, 194, 72, 200, 66)

$fontFamily = New-Object System.Drawing.FontFamily 'Segoe UI'
$nFont = New-Object System.Drawing.Font $fontFamily, 30, ([System.Drawing.FontStyle]::Bold)
$labelBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(245, 245, 250))
$graphics.DrawString('N', $nFont, $labelBrush, 113, 176)

$bitmap.Save($iconPng, [System.Drawing.Imaging.ImageFormat]::Png)

$iconHandle = $bitmap.GetHicon()
$icon = [System.Drawing.Icon]::FromHandle($iconHandle)
$fileStream = [System.IO.File]::Open($iconIco, [System.IO.FileMode]::Create)
$icon.Save($fileStream)
$fileStream.Close()

$graphics.Dispose()
$bgBrush.Dispose()
$ringPen.Dispose()
$guidePen.Dispose()
$northPen.Dispose()
$arrowPen.Dispose()
$arrowBrush.Dispose()
$sunGlowBrush.Dispose()
$sunBrush.Dispose()
$sunPen.Dispose()
$rayPen.Dispose()
$labelBrush.Dispose()
$nFont.Dispose()
$icon.Dispose()
$bitmap.Dispose()

Write-Host "Generated icon assets: $iconPng"
Write-Host "Generated icon assets: $iconIco"