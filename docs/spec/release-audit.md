# R8 / PV-3 Release Audit

## Commands

Run the complete read-only gate against already-built production JARs:

```powershell
./gradlew releaseAudit
```

The task is an `Exec` wrapper around `node scripts/run_release_audit.js`. It has no dependency on
`build`, `jar`, `remapJar`, or datagen and never creates a replacement JAR. The Node entry point can
also be run directly when Gradle is unavailable:

```powershell
node scripts/run_release_audit.js
```

Refresh the maintained documentation index intentionally, then return to read-only checking:

```powershell
node scripts/generate_docs_index.js --write
node scripts/generate_docs_index.js --check
```

Release automation invokes `node scripts/replace_restricted_assets.js --check`. The audit freezes the
license-safe replacement scope, original PNG dimensions, required GeckoLib bone names, empty sound
event catalog, removed production OGG files, and placeholder Java model boundary. Run the script with
`--write` only when intentionally replacing newly introduced restricted assets.

## Fail-closed criteria

`audit_release_jars.js` requires exactly one non-sources production JAR under each node's
`build/libs` directory. Missing, unreadable, ambiguous, or older-than-source JARs fail. It verifies:

- Forge contains only `META-INF/mods.toml`; NeoForge contains only `META-INF/neoforge.mods.toml`.
- Metadata contains no `${...}` placeholder and exactly matches the repository TOML template after
  expansion from `gradle.properties`, including dependency IDs and version ranges.
- The artifact filename identifies the declared mod and Minecraft versions.
- Only the node-correct `structure(s)` directory contains NBT templates.
- Development files such as source, scripts, build/cache content, and editor metadata are absent.
- Restricted legacy textures, models, and sounds remain replaced according to
  `RR-RESTRICTED-ASSET-PLACEHOLDERS.json` before artifacts are built.
- Each inspected artifact reports a SHA-256 digest, including when another criterion fails.
- `mod.license` must exist and match metadata. A repository `LICENSE`, `LICENCE`, or `COPYING` file is
  also mandatory; a declaration without license text fails explicitly. The audit never invents terms.

`audit_resource_index.js` chooses the datapack directory spelling consumed by each node, normalizes it
to one logical path, canonicalizes JSON object key order and recipe result ID spelling, and compares
the resulting SHA-256 indexes. One-sided resources pass only when their exact logical path and owner
are declared in the script. A paired resource with different content always fails.

`audit_validation_residue.js` distinguishes permanent validation code from temporary probes. Permanent
`SelfTest`, `Audit`, `Invariant`, validator/orchestrator classes and `validation/**` are pattern-owned;
the four production `*Fixture.java` files are exact allowlist entries. A new fixture, probe-like file,
temporary marker, or stale allowlist entry fails.

`generate_docs_index.js` indexes maintained Markdown under `docs`. It excludes build/runtime output,
the legacy 1.12.2 source mirror, validation evidence, U-lane evidence, porting task plans 01-03, and its
own generated output. `--check` never writes and fails on a missing or stale index.

The aggregate then runs compatibility, rename, and datapack singular/plural audits in the same serial
process. Every audit runs even after an earlier failure, and the aggregate exits nonzero with a compact
summary after collecting all failing script names.

## Current artifact expectation

Existing JARs are not accepted merely because they are present. After source or resource changes they
must fail as stale until both production artifacts are rebuilt. At the time this gate was introduced,
the repository also declared `All Rights Reserved` in `gradle.properties` without a root license file;
that condition is intentionally reported as a release-blocking missing-license-text reason.