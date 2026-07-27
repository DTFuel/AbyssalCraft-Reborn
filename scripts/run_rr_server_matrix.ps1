param(
    [ValidateSet('forge', 'neoforge', 'all')]
    [string]$Node = 'all'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$run = Join-Path $root 'run'
$eula = Join-Path $run 'eula.txt'
$eulaBackup = Join-Path $run 'eula.txt.rr-server-matrix-backup'
$properties = Join-Path $run 'server.properties'
$backup = Join-Path $run 'server.properties.rr-server-matrix-backup'
$nodes = if ($Node -eq 'all') { @('forge', 'neoforge') } else { @($Node) }
$oldJavaToolOptions = $env:JAVA_TOOL_OPTIONS

function Invoke-MatrixPhase([string]$project, [string]$log) {
    & (Join-Path $root 'gradlew.bat') $project '--console=plain' 2>&1 |
        Tee-Object -FilePath $log
    if ($LASTEXITCODE -ne 0) {
        throw "RR server matrix Gradle phase failed: $project"
    }
}

try {
    New-Item -ItemType Directory -Force -Path $run | Out-Null
    if (Test-Path $properties) { Copy-Item $properties $backup -Force }
    if (Test-Path $eula) { Copy-Item $eula $eulaBackup -Force }
    Set-Content -Path $eula -Value 'eula=true' -Encoding ascii
    $env:JAVA_TOOL_OPTIONS = (($oldJavaToolOptions, '-Dabyssalcraft.rrServerMatrix=true') |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join ' '

    foreach ($loader in $nodes) {
        $version = if ($loader -eq 'forge') { '1.20.1-forge' } else { '1.21.1-neoforge' }
        $worldName = "rr-server-matrix-$loader"
        $world = Join-Path $run $worldName
        $log1 = Join-Path $run "$worldName-phase1.log"
        $log2 = Join-Path $run "$worldName-phase2.log"
        Remove-Item $world, $log1, $log2 -Recurse -Force -ErrorAction SilentlyContinue
        @(
            "level-name=$worldName"
            'server-port=0'
            'online-mode=false'
            'enable-query=false'
            'enable-rcon=false'
        ) | Set-Content -Path $properties -Encoding ascii

        Invoke-MatrixPhase ":${version}:runServer" $log1
        if (-not (Select-String -Quiet -Path $log1 -SimpleMatch 'RR_SERVER_MATRIX_PENDING phase=new_world restart=automatic')) {
            throw "RR server matrix phase one did not request restart: $loader"
        }
        if (Select-String -Quiet -Path $log1 -SimpleMatch 'RR_SERVER_MATRIX_OK') {
            throw "RR server matrix completed without a persisted restart: $loader"
        }

        Invoke-MatrixPhase ":${version}:runServer" $log2
        if (-not (Select-String -Quiet -Path $log2 -SimpleMatch 'RR_SERVER_MATRIX_OK')) {
            throw "RR server matrix phase two did not complete: $loader"
        }
        Remove-Item $world, $log1, $log2 -Recurse -Force
    }

    Write-Output "RR_SERVER_MATRIX_RUNNER_OK nodes=$($nodes.Count) phases=$($nodes.Count * 2)"
} finally {
    $env:JAVA_TOOL_OPTIONS = $oldJavaToolOptions
    if (Test-Path $backup) {
        Move-Item $backup $properties -Force
    } else {
        Remove-Item $properties -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $eulaBackup) {
        Move-Item $eulaBackup $eula -Force
    } else {
        Remove-Item $eula -Force -ErrorAction SilentlyContinue
    }
    foreach ($loader in $nodes) {
        Remove-Item (Join-Path $run "rr-server-matrix-$loader") -Recurse -Force -ErrorAction SilentlyContinue
        Remove-Item (Join-Path $run "rr-server-matrix-$loader-phase1.log") -Force -ErrorAction SilentlyContinue
        Remove-Item (Join-Path $run "rr-server-matrix-$loader-phase2.log") -Force -ErrorAction SilentlyContinue
    }
}