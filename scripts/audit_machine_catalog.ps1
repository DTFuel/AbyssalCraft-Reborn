param(
    [switch]$WriteCatalog
)

$ErrorActionPreference = 'Stop'

function Find-RepositoryRoot {
    $directory = (Get-Location).Path
    while ($directory) {
        if (Test-Path (Join-Path $directory 'settings.gradle.kts')) { return $directory }
        $parent = Split-Path $directory -Parent
        if ($parent -eq $directory) { break }
        $directory = $parent
    }
    throw 'Cannot locate the AbyssalCraft repository root'
}

$root = Find-RepositoryRoot
$legacyPath = Join-Path $root 'docs/AbyssalCraft-1.12.2/src/main/java/com/shinoow/abyssalcraft/common/AbyssalCrafting.java'
$resourcePath = Join-Path $root 'src/main/resources/data/abyssalcraft/catalog/legacy_machine_calls.txt'
$auditResourcePath = Join-Path $root 'src/main/resources/data/abyssalcraft/catalog/legacy_machine_catalog.json'
$sourceJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/data/gen/LegacyMachineRecipeSource.java'
$catalogJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/data/gen/LegacyMachineRecipeCatalog.java'
$resolverJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/data/gen/MachineOutputResolutionCatalog.java'
$recipeDataJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/data/gen/MachineRecipeData.java'
$materialItemsJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/content/item/material/MaterialItems.java'
$clusterBlocksJava = Join-Path $root 'src/main/java/com/shinoow/abyssalcraft/content/block/material/CrystalClusterBlocks.java'

$callPattern = '^\s*AbyssalCraftAPI\.(addSingleCrystallization|addCrystallization|addTransmutation|addMaterialization)\((.*)\);\s*$'
$legacyLines = Get-Content $legacyPath
$catalogLines = foreach ($index in 0..($legacyLines.Count - 1)) {
    if ($legacyLines[$index] -match $callPattern) {
        [pscustomobject]@{ Ordinal = 0; Line = $index + 1; Method = $Matches[1]; Arguments = $Matches[2]; Call = $legacyLines[$index].Trim() }
    }
}
for ($index = 0; $index -lt $catalogLines.Count; $index++) { $catalogLines[$index].Ordinal = $index + 1 }

if ($catalogLines.Count -ne 223) { throw "Legacy machine source count changed: $($catalogLines.Count)" }
$kindCounts = @{
    crystallization = @($catalogLines | Where-Object Method -Match 'Crystallization').Count
    transmutation = @($catalogLines | Where-Object Method -EQ 'addTransmutation').Count
    materialization = @($catalogLines | Where-Object Method -EQ 'addMaterialization').Count
}
if ($kindCounts.crystallization -ne 109 -or $kindCounts.transmutation -ne 46 -or $kindCounts.materialization -ne 68) {
    throw "Legacy machine kind counts changed: $($kindCounts | ConvertTo-Json -Compress)"
}

$resourceLines = New-Object 'System.Collections.Generic.List[string]'
for ($index = 0; $index -lt $legacyLines.Count; $index++) {
    if ($legacyLines[$index] -match $callPattern) { $resourceLines.Add($legacyLines[$index].Trim()) } else { $resourceLines.Add('') }
}
$expectedResource = ($resourceLines -join "`n") + "`n"
if ($WriteCatalog) {
    $resourceDirectory = Split-Path $resourcePath -Parent
    New-Item -ItemType Directory -Force $resourceDirectory | Out-Null
    [IO.File]::WriteAllText($resourcePath, $expectedResource, [Text.UTF8Encoding]::new($false))
}
if (!(Test-Path $resourcePath)) { throw "Missing packaged source-derived catalog: $resourcePath" }
$actualResource = [IO.File]::ReadAllText($resourcePath).Replace("`r`n", "`n")
if ($actualResource -ne $expectedResource) { throw 'Packaged legacy machine call catalog is stale; run with -WriteCatalog' }

$resolverSource = Get-Content -Raw $resolverJava
$resolutions = [regex]::Matches($resolverSource, 'put\(result,\s*"([^"]+)",\s*"([^"]+)",\s*"([^"]+)"\)')
if ($resolutions.Count -ne 25) { throw "Machine output resolver count changed: $($resolutions.Count)" }
$resolutionTags = New-Object 'System.Collections.Generic.HashSet[string]'
$resolutionItems = @{}
foreach ($match in $resolutions) {
    $tag = $match.Groups[1].Value
    if (!$resolutionTags.Add($tag)) { throw "Duplicate machine output resolver: $tag" }
    $resolutionItems[$tag] = $match.Groups[2].Value
}

$sourceText = Get-Content -Raw $sourceJava
$tagMappings = @{}
foreach ($match in [regex]::Matches($sourceText, 'result\.put\("([^"]+)",\s*"([^"]+)"\)')) {
    $tagMappings[$match.Groups[1].Value] = $match.Groups[2].Value
}
foreach ($material in 'Tin','Copper','Aluminum','Aluminium','Zinc','Magnesium','Calcium','Bronze','Brass','Iron','Gold') {
    $path = $material.ToLower().Replace('aluminum', 'aluminium')
    $tagMappings["ingot$material"] = "c:ingots/$path"
    $tagMappings["ore$material"] = "c:ores/$path"
    $tagMappings["nugget$material"] = "c:nuggets/$path"
    $tagMappings["dust$material"] = "c:dusts/$path"
    $tagMappings["block$material"] = "c:storage_blocks/$path"
}

function Split-Arguments([string]$value) {
    $result = New-Object 'System.Collections.Generic.List[string]'
    $depth = 0; $quoted = $false; $start = 0
    for ($index = 0; $index -lt $value.Length; $index++) {
        $character = $value[$index]
        if ($character -eq '"') { $quoted = !$quoted }
        if ($quoted) { continue }
        if ($character -eq '(') { $depth++ }
        elseif ($character -eq ')') { $depth-- }
        elseif ($character -eq ',' -and $depth -eq 0) {
            $result.Add($value.Substring($start, $index - $start).Trim())
            $start = $index + 1
        }
    }
    $result.Add($value.Substring($start).Trim())
    return $result.ToArray()
}

function Stack-Count([string[]]$arguments) {
    $count = 0
    for ($index = 0; $index -lt $arguments.Count;) {
        if ($arguments[$index].StartsWith('"') -and $index + 1 -lt $arguments.Count -and $arguments[$index + 1] -match '^\d+$') { $index += 2 } else { $index++ }
        $count++
    }
    return $count
}

$resolvedOutputs = 0; $dualOutputs = 0; $multiInputs = 0
$migrated = 0; $replaced = 0; $retired = 0; $blocked = 0
$recipeIds = New-Object 'System.Collections.Generic.HashSet[string]'
$auditEntries = New-Object 'System.Collections.Generic.List[object]'
foreach ($entry in $catalogLines) {
    $arguments = @(Split-Arguments $entry.Arguments)
    if ($entry.Method -ne 'addMaterialization') { $arguments = $arguments[0..($arguments.Count - 2)] }
    $stackCount = Stack-Count $arguments
    $inputCount = if ($entry.Method -eq 'addMaterialization') { $stackCount - 1 } else { 1 }
    $outputCount = if ($entry.Method -eq 'addMaterialization') { 1 } else { $stackCount - 1 }
    if ($inputCount -lt 1 -or $outputCount -lt 1) {
        $blocked++
        $auditEntries.Add([ordered]@{ ordinal=$entry.Ordinal; source_line=$entry.Line; kind=''; status='BLOCKED'; classification_key=''; legacy_call=$entry.Call })
        continue
    }
    if ($inputCount -gt 1) { $multiInputs++ }
    if ($outputCount -eq 2) { $dualOutputs++ }
    if ($outputCount -gt 2) { throw "Unsupported output count at ordinal $($entry.Ordinal): $outputCount" }

    $isRetired = $entry.Call.Contains('"ingotAluminium"') -or $entry.Call.Contains('"nuggetAluminium"')
    $kind = if ($entry.Method -match 'Crystallization') {'CRYSTALLIZATION'} elseif ($entry.Method -eq 'addTransmutation') {'TRANSMUTATION'} else {'MATERIALIZATION'}
    if ($isRetired) {
        $retired++
        $auditEntries.Add([ordered]@{ ordinal=$entry.Ordinal; source_line=$entry.Line; kind=$kind; status='RETIRED'; classification_key=''; legacy_call=$entry.Call })
        continue
    }
    $isReplaced = $entry.Call.Contains('OreDictionary') -or $entry.Call.Contains('PotionUtils') -or $entry.Call -match 'new ItemStack\([^,]+,\s*1,\s*[1-9]'
    foreach ($quoted in [regex]::Matches($entry.Arguments, '"([A-Za-z][A-Za-z0-9]+)"')) {
        $legacyTag = $quoted.Groups[1].Value
        if (!$tagMappings.ContainsKey($legacyTag)) { continue }
        $isReplaced = $true
    }
    $status = if ($isReplaced) { $replaced++; 'REPLACED' } else { $migrated++; 'MIGRATED' }
    $recipeId = '{0}_{1:d3}' -f $kind.ToLower(), $entry.Ordinal
    if (!$recipeIds.Add($recipeId)) { throw "Duplicate executable recipe id: $recipeId" }
    $auditEntries.Add([ordered]@{ ordinal=$entry.Ordinal; source_line=$entry.Line; kind=$kind; status=$status; classification_key=$recipeId; legacy_call=$entry.Call })
}

foreach ($entry in $catalogLines) {
    $arguments = @(Split-Arguments $entry.Arguments)
    if ($entry.Method -ne 'addMaterialization') { $arguments = $arguments[0..($arguments.Count - 2)] }
    $stackCount = Stack-Count $arguments
    $outputStart = if ($entry.Method -eq 'addMaterialization') { 0 } else { 1 }
    $outputEnd = if ($entry.Method -eq 'addMaterialization') { 0 } else { $stackCount - 1 }
    $stackIndex = 0
    for ($index = 0; $index -lt $arguments.Count;) {
        $value = $arguments[$index]
        $legacyTag = $null
        if ($value.StartsWith('"')) { $legacyTag = $value.Trim('"') }
        if ($value.StartsWith('"') -and $index + 1 -lt $arguments.Count -and $arguments[$index + 1] -match '^\d+$') { $index += 2 } else { $index++ }
        if ($stackIndex -ge $outputStart -and $stackIndex -le $outputEnd -and $legacyTag -and $tagMappings.ContainsKey($legacyTag)) {
            $tag = $tagMappings[$legacyTag]
            if (!$resolutionTags.Contains($tag)) { throw "Missing output resolver for $legacyTag -> $tag at ordinal $($entry.Ordinal)" }
            $resolvedOutputs++
        }
        $stackIndex++
    }
}
if ($resolvedOutputs -ne 25) { throw "Resolved legacy output count changed: $resolvedOutputs" }
if ($blocked -ne 0) { throw "Machine catalog contains blocked entries: $blocked" }
if ($migrated + $replaced + $retired + $blocked -ne 223) { throw 'Machine status closure does not total 223' }
if ($recipeIds.Count -ne $migrated + $replaced) { throw 'Executable recipe IDs are not one-for-one' }
if ($dualOutputs -lt 1 -or $multiInputs -lt 1) { throw "Machine schema coverage regressed: dual=$dualOutputs multi=$multiInputs" }

$materialSource = Get-Content -Raw $materialItemsJava
$clusterSource = Get-Content -Raw $clusterBlocksJava
if ($materialSource -notmatch 'MACHINE_COMPAT_ELEMENTS = \{"copper", "tin"\}' -or $clusterSource -notmatch 'MACHINE_COMPAT_ELEMENTS') {
    throw 'Copper/tin crystal and cluster registry aliases are not wired'
}
$expectedCompat = @{
    'c:ingots/copper' = 'abyssalcraft:crystal_copper'
    'c:nuggets/copper' = 'abyssalcraft:crystal_shard_copper'
    'c:ingots/tin' = 'abyssalcraft:crystal_tin'
    'c:nuggets/tin' = 'abyssalcraft:crystal_shard_tin'
}
foreach ($pair in $expectedCompat.GetEnumerator()) {
    if ($resolutionItems[$pair.Key] -ne $pair.Value) { throw "Incorrect copper/tin output mapping: $($pair.Key) -> $($resolutionItems[$pair.Key])" }
}
$registeredResolverItems = New-Object 'System.Collections.Generic.HashSet[string]'
foreach ($element in [regex]::Match($materialSource, 'CRYSTAL_ELEMENTS\s*=\s*\{([^}]+)\}', 'Singleline').Groups[1].Value.Split(',')) {
    $name = $element.Trim().Trim('"')
    if ($name) {
        [void]$registeredResolverItems.Add("abyssalcraft:crystal_$name")
        [void]$registeredResolverItems.Add("abyssalcraft:crystal_shard_$name")
    }
}
foreach ($element in 'copper','tin') {
    [void]$registeredResolverItems.Add("abyssalcraft:crystal_$element")
    [void]$registeredResolverItems.Add("abyssalcraft:crystal_shard_$element")
}
$vanillaResolverItems = @('minecraft:beef','minecraft:iron_nugget','minecraft:coal_ore','minecraft:diamond_ore',
    'minecraft:gold_ore','minecraft:iron_ore','minecraft:lapis_ore','minecraft:redstone_ore','minecraft:oak_leaves',
    'minecraft:oak_log','minecraft:oak_planks','minecraft:oak_sapling','minecraft:vine')
foreach ($item in $resolutionItems.Values) {
    if (!$registeredResolverItems.Contains($item) -and $item -notin $vanillaResolverItems) {
        throw "Output resolver target is not proven by the modern registry sources: $item"
    }
}
if ($sourceText -notmatch 'dread_plagued_gateway_key"\s*->\s*"abyssalcraft:gatewaykeydl"' -or
    $sourceText -notmatch 'omothol_forged_gateway_key"\s*->\s*"abyssalcraft:gatewaykeyjzh"') {
    throw 'Gateway aliases are not mapped to the modern registry IDs'
}

$catalogSource = Get-Content -Raw $catalogJava
$recipeDataSource = Get-Content -Raw $recipeDataJava
$expectedCounts = [regex]::Matches($catalogSource, 'Status\.(MIGRATED|REPLACED|RETIRED|BLOCKED),\s*(\d+)')
$expectedCountMap = @{}
foreach ($match in $expectedCounts) { $expectedCountMap[$match.Groups[1].Value] = [int]$match.Groups[2].Value }
if ($expectedCountMap.MIGRATED -ne 142 -or $expectedCountMap.REPLACED -ne 77 -or
    $expectedCountMap.RETIRED -ne 4 -or $expectedCountMap.BLOCKED -ne 0) {
    throw "Java machine classification closure is not frozen at 142/77/4/0"
}
$closedOrdinalMatch = [regex]::Match($catalogSource, 'closedOrdinals\s*=\s*Set\.of\(([^)]+)\)', 'Singleline')
$closedOrdinals = @([regex]::Matches($closedOrdinalMatch.Groups[1].Value, '\d+') | ForEach-Object { [int]$_.Value })
if ($closedOrdinals.Count -ne 18 -or 120 -notin $closedOrdinals) { throw 'Registry/alias ordinal closure is incomplete' }
if ($catalogSource -notmatch 'self_test_missing_output.*self_test_missing_input' -or
    $recipeDataSource -notmatch 'secondary_result' -or $recipeDataSource -notmatch 'json\.add\("inputs", inputs\)') {
    throw 'Multi-dependency, dual-output, or multi-input contracts are not permanently asserted'
}

$auditDocument = [ordered]@{
    source = 223
    migrated = $migrated
    replaced = $replaced
    retired = $retired
    blocked = $blocked
    kinds = [ordered]@{ crystallization=109; transmutation=46; materialization=68 }
    output_resolver_count = $resolutions.Count
    resolved_output_count = $resolvedOutputs
    multi_input = $multiInputs
    dual_output = $dualOutputs
    executable_classification_keys = $recipeIds.Count
    source_resource = 'data/abyssalcraft/catalog/legacy_machine_calls.txt'
    output_resolutions = @($resolutions | ForEach-Object { [ordered]@{ tag=$_.Groups[1].Value; item=$_.Groups[2].Value; reason=$_.Groups[3].Value } })
    entries = $auditEntries
}
$expectedAudit = ($auditDocument | ConvertTo-Json -Depth 6) + "`n"
if ($WriteCatalog) { [IO.File]::WriteAllText($auditResourcePath, $expectedAudit, [Text.UTF8Encoding]::new($false)) }
if (!(Test-Path $auditResourcePath)) { throw "Missing packaged machine audit catalog: $auditResourcePath" }
$actualAudit = [IO.File]::ReadAllText($auditResourcePath).Replace("`r`n", "`n")
if ($actualAudit -ne $expectedAudit.Replace("`r`n", "`n")) { throw 'Packaged machine audit catalog is stale; run with -WriteCatalog' }

Write-Output ("RR_MACHINE_CATALOG_AUDIT source=223 migrated={0} replaced={1} retired={2} blocked=0 crystallization=109 transmutation=46 materialization=68 resolvers=25 resolvedOutputs=25 multiInput={3} dualOutput={4} ids={5} resource=packaged" -f $migrated, $replaced, $retired, $multiInputs, $dualOutputs, $recipeIds.Count)