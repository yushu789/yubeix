param([string]$In, [string]$Out, [int]$X, [int]$Y, [int]$W, [int]$H, [int]$Scale = 2)
Add-Type -AssemblyName System.Drawing
$src = [System.Drawing.Image]::FromFile($In)
$bmp = New-Object System.Drawing.Bitmap($W * $Scale, $H * $Scale)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$destRect = New-Object System.Drawing.Rectangle(0, 0, $W * $Scale, $H * $Scale)
$srcRect = New-Object System.Drawing.Rectangle($X, $Y, $W, $H)
$g.DrawImage($src, $destRect, $srcRect, [System.Drawing.GraphicsUnit]::Pixel)
$bmp.Save($Out)
$g.Dispose()
$bmp.Dispose()
$src.Dispose()
