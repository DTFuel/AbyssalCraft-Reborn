param(
    [ValidateSet('forge', 'neoforge', 'all')]
    [string]$Node = 'all'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$run = Join-Path $root 'run'
$nodes = if ($Node -eq 'all') { @('forge', 'neoforge') } else { @($Node) }
$oldJavaToolOptions = $env:JAVA_TOOL_OPTIONS
$localMavenInit = Join-Path $root 'build\local-maven.init.gradle'

function Invoke-ClientSmoke([string]$project, [string]$log, [string]$runDir) {
    $arguments = @($project, '--console=plain', "-Pabyssalcraft.runDir=$runDir")
    if (Test-Path $localMavenInit) {
        $arguments += @('--init-script', $localMavenInit)
    }
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & (Join-Path $root 'gradlew.bat') @arguments 2>&1 |
            Tee-Object -FilePath $log
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    if ($exitCode -ne 0) {
        throw "RR client smoke Gradle phase failed: $project"
    }
}

try {
    New-Item -ItemType Directory -Force -Path $run | Out-Null
    $env:JAVA_TOOL_OPTIONS = (($oldJavaToolOptions, '-Dabyssalcraft.rrClientSmoke=true') |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join ' '

    foreach ($loader in $nodes) {
        $version = if ($loader -eq 'forge') { '1.20.1-forge' } else { '1.21.1-neoforge' }
        $log = Join-Path $run "rr-client-smoke-$loader.log"
        $smokeRun = Join-Path $run "rr-client-smoke-$loader"
        Remove-Item $log, $smokeRun -Recurse -Force -ErrorAction SilentlyContinue
        Invoke-ClientSmoke ":${version}:runClient" $log "../../run/rr-client-smoke-$loader"
        if ((Select-String -Path $log -SimpleMatch 'RR_CLIENT_TITLE_SMOKE_OK screen=title ticks=2').Count -ne 1) {
            throw "RR client smoke did not reach the title screen exactly once: $loader"
        }
        $failurePatterns = @(
            'Crash report'
            'Encountered an unexpected exception'
            'BUILD FAILED'
            'Failed to complete lifecycle event'
            'ModLoadingException'
            'Missing textures in model abyssalcraft:'
            'Unable to load model.*abyssalcraft:'
            'Failed to load.*abyssalcraft:'
            '\[[^]]+/ERROR\] \[co\.sh\.ab\.'
        )
        if (Select-String -Quiet -Path $log -Pattern $failurePatterns) {
            throw "RR client smoke log contains a failure marker: $loader"
        }
        Remove-Item $log, $smokeRun -Recurse -Force
    }

    Write-Output "RR_CLIENT_SMOKE_RUNNER_OK nodes=$($nodes.Count)"
} finally {
    $env:JAVA_TOOL_OPTIONS = $oldJavaToolOptions
    foreach ($loader in $nodes) {
        Remove-Item (Join-Path $run "rr-client-smoke-$loader.log") -Force -ErrorAction SilentlyContinue
        Remove-Item (Join-Path $run "rr-client-smoke-$loader") -Recurse -Force -ErrorAction SilentlyContinue
    }
}