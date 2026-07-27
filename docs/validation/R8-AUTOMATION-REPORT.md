# R8 Automatic Verification Report

Date: 2026-07-27

## Server matrix

- Runner: `scripts/run_rr_server_matrix.ps1`
- Nodes: Forge 1.20.1 and NeoForge 1.21.1
- Seed: `1251393890`
- Lifecycle: new world followed by a persisted restart on each node
- Result: `RR_SERVER_MATRIX_RUNNER_OK nodes=2 phases=4`
- Runtime closure: 4 AC dimensions loaded, 37 legacy structures covered, 11 vanilla natural-spawn scenarios invoked, 97 entity loot tables resolved, 219 machine recipes matched exactly, and resurrection state completed after restart.
- World gate: the unchanged 19-coordinate FULL chunk routes passed p50 <= 100 ms and p95 <= 500 ms for Abyssal Wasteland and Dreadlands.

## Client smoke

- Runner: `scripts/run_rr_client_smoke.ps1`
- Each loader uses a clean, isolated `run/rr-client-smoke-<loader>` game directory which is removed after the run.
- The opt-in client hook requires `TitleScreen` for two consecutive client ticks, emits exactly one `RR_CLIENT_TITLE_SMOKE_OK screen=title ticks=2`, and calls the normal Minecraft stop path.
- The runner rejects non-zero Gradle exits, missing or duplicate success markers, crashes, lifecycle/mod-loading failures, and AbyssalCraft model or resource errors.
- Result: `RR_CLIENT_SMOKE_RUNNER_OK nodes=2`.
- Scope boundary: title-screen smoke proves client initialization and resource/model baking; it does not replace any visual, audio, gameplay, or interaction item in `U-*`.

## Data and artifacts

- Crafting audit: 339 MIGRATED, 61 REPLACED, 1 BLOCKED, 0 RETIRED. The sole BLOCKED entry is the legacy generic spawn egg, which has no semantically equivalent modern item without an explicit entity-selection design.
- Smelting audit: 52 MIGRATED, 1 REPLACED, 0 BLOCKED, 0 RETIRED.
- Tags: 181 logical tags and 351 physical files.
- Asset audit: 533 textures and 1819 ledger entries; missing=0. The Powerstone Tracker item-atlas copy is byte-identical to its legacy renderer texture.
- Forge JAR: `1a3f577dd5abb304cdfc9d3cfed972241ef8517213641b69c8eb31a0462b2fc2`
- NeoForge JAR: `0c59ebc25856c7e3877213a14c40db549be515f07c8829a81cc9b03eceb3d400`
- Release JAR audit: 11457 combined entries and 74 structures across the two rebuilt JARs.
- Resource index audit: Forge 3246, NeoForge 3259, matched 3245, semantically justified allowlist 15.
- Validation residue audit: 5286 files scanned, 72 permanent validators, 4 fixtures, 0 temporary residues.
- Release audit result: both node tasks reported `R8_RELEASE_AUDIT_OK audits=9 failures=0` against these exact rebuilt JARs.

## Gate boundary

`T11.2` / `RR-SERVER` is complete. `T11.1`, every `U-*`, `U-GATE`, `T11.3`, `T11.4`, `R8-Gate`, and M11 remain incomplete; this automatic report does not change their status.