$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$sourceDirectory = Join-Path $root 'src\main\resources\assets\abyssalcraft\textures\model\ghoul'
$targetDirectory = Join-Path $root 'src\main\resources\assets\abyssalcraft\textures\block\ghoul_head'
$textures = @(
    'depths_ghoul',
    'depths_ghoul_pete',
    'depths_ghoul_wilson',
    'depths_ghoul_orange'
)

New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
foreach ($texture in $textures) {
    $sourcePath = Join-Path $sourceDirectory "$texture.png"
    $targetPath = Join-Path $targetDirectory "$texture.png"
    $source = [System.Drawing.Bitmap]::FromFile($sourcePath)
    try {
        if ($source.Width -ne 128 -or $source.Height -ne 64) {
            throw "Unexpected Ghoul texture dimensions: $sourcePath ($($source.Width)x$($source.Height))"
        }
        $target = New-Object System.Drawing.Bitmap -ArgumentList 128, 128
        try {
            for ($y = 0; $y -lt 64; $y++) {
                for ($x = 0; $x -lt 128; $x++) {
                    $target.SetPixel($x, $y, $source.GetPixel($x, $y))
                }
            }
            $target.Save($targetPath, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $target.Dispose()
        }
    } finally {
        $source.Dispose()
    }
}

Write-Output "RR_GHOUL_HEAD_TEXTURES_OK textures=$($textures.Count) size=128x128 sourceRows=64"
