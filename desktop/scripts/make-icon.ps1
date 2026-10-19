param()

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$buildDir = Join-Path (Join-Path $PSScriptRoot '..') 'build'
New-Item -ItemType Directory -Path $buildDir -Force | Out-Null

function New-IconBitmap([int]$size) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $g.Clear([System.Drawing.Color]::Transparent)

    $rect = New-Object System.Drawing.RectangleF(0, 0, $size, $size)
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $radius = [int]($size * 0.16)
    if ($radius -lt 4) { $radius = 4 }
    $d = $radius * 2
    $path.AddArc($rect.X, $rect.Y, $d, $d, 180, 90)
    $path.AddArc($rect.Right - $d, $rect.Y, $d, $d, 270, 90)
    $path.AddArc($rect.Right - $d, $rect.Bottom - $d, $d, $d, 0, 90)
    $path.AddArc($rect.X, $rect.Bottom - $d, $d, $d, 90, 90)
    $path.CloseFigure()

    $bgColor = [System.Drawing.ColorTranslator]::FromHtml('#1f4e8c')
    $bgBrush = New-Object System.Drawing.SolidBrush($bgColor)
    $g.FillPath($bgBrush, $path)
    $bgBrush.Dispose()

    $fontSize = [float]($size * 0.42)
    $font = New-Object System.Drawing.Font('Arial', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $textBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $fmt = New-Object System.Drawing.StringFormat
    $fmt.Alignment = [System.Drawing.StringAlignment]::Center
    $fmt.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString('HR', $font, $textBrush, $rect, $fmt)
    $fmt.Dispose()
    $textBrush.Dispose()
    $font.Dispose()
    $path.Dispose()
    $g.Dispose()
    return $bmp
}

function Get-PngBytes([System.Drawing.Bitmap]$bmp) {
    $ms = New-Object System.IO.MemoryStream
    $bmp.Save($ms, [System.Drawing.Imaging.ImageFormat]::Png)
    $bytes = $ms.ToArray()
    $ms.Dispose()
    return ,$bytes
}

function Write-BigEndianUInt32([System.IO.BinaryWriter]$writer, [uint32]$value) {
    $bytes = [BitConverter]::GetBytes([uint32]$value)
    if ([BitConverter]::IsLittleEndian) { [Array]::Reverse($bytes) }
    $writer.Write($bytes)
}

# 1) 256x256 master PNG
$bmp256 = New-IconBitmap 256
$png256Path = Join-Path $buildDir 'icon.png'
$bmp256.Save($png256Path, [System.Drawing.Imaging.ImageFormat]::Png)
$png256Bytes = Get-PngBytes $bmp256
Write-Host "Wrote $png256Path ($($png256Bytes.Length) bytes)"

# 512x512 PNG bytes (needed for icns ic09 entry)
$bmp512 = New-IconBitmap 512
$png512Bytes = Get-PngBytes $bmp512
$bmp512.Dispose()

# 2) ICO container with a single embedded 256x256 PNG (Vista+ compatible)
$icoPath = Join-Path $buildDir 'icon.ico'
$fs = New-Object System.IO.FileStream($icoPath, [System.IO.FileMode]::Create)
$bw = New-Object System.IO.BinaryWriter($fs)
# ICONDIR
$bw.Write([uint16]0)   # reserved
$bw.Write([uint16]1)   # type: icon
$bw.Write([uint16]1)   # image count
# ICONDIRENTRY
$bw.Write([byte]0)     # width  (0 means 256)
$bw.Write([byte]0)     # height (0 means 256)
$bw.Write([byte]0)     # color count
$bw.Write([byte]0)     # reserved
$bw.Write([uint16]1)   # color planes
$bw.Write([uint16]32)  # bits per pixel
$bw.Write([uint32]$png256Bytes.Length) # data size
$bw.Write([uint32]22)  # data offset (6 + 16)
$bw.Write($png256Bytes)
$bw.Flush()
$bw.Dispose()
$fs.Dispose()
Write-Host "Wrote $icoPath"

# 3) Linux icon: plain PNG copy
$linuxPngPath = Join-Path $buildDir 'icon-linux.png'
[System.IO.File]::WriteAllBytes($linuxPngPath, $png256Bytes)
Write-Host "Wrote $linuxPngPath"

# 4) macOS icns container: ic08 (256x256 PNG) + ic09 (512x512 PNG)
$icnsPath = Join-Path $buildDir 'icon.icns'
$totalSize = 8 + (8 + $png256Bytes.Length) + (8 + $png512Bytes.Length)
$fs2 = New-Object System.IO.FileStream($icnsPath, [System.IO.FileMode]::Create)
$bw2 = New-Object System.IO.BinaryWriter($fs2)
$bw2.Write([System.Text.Encoding]::ASCII.GetBytes('icns'))
Write-BigEndianUInt32 $bw2 ([uint32]$totalSize)
$bw2.Write([System.Text.Encoding]::ASCII.GetBytes('ic08'))
Write-BigEndianUInt32 $bw2 ([uint32](8 + $png256Bytes.Length))
$bw2.Write($png256Bytes)
$bw2.Write([System.Text.Encoding]::ASCII.GetBytes('ic09'))
Write-BigEndianUInt32 $bw2 ([uint32](8 + $png512Bytes.Length))
$bw2.Write($png512Bytes)
$bw2.Flush()
$bw2.Dispose()
$fs2.Dispose()
Write-Host "Wrote $icnsPath"

$bmp256.Dispose()
Write-Host "Icon generation complete."
