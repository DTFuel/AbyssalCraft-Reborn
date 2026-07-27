# Complex block model fidelity (T9.2b / PK-2b)

## Contract

`assets/abyssalcraft/complex_block_model_fidelity.json` is the permanent one-entry-per-registered-block catalog for complex client models. Every entry is `ACTIVE` and records the legacy model or OBJ owner, its SHA-256, modern blockstate and model files, texture files, state mapping, and the geometry replacement rationale.

The catalog owns 42 blocks:

- 13 layered ores: a host-stone cube plus the legacy ore overlay on an expanded second element.
- 3 machines: distinct front, side, and top faces; crystallizer and transmutator also have distinct idle and active models/textures.
- 2 multiface ground blocks: dreadlands grass and fused abyssal sand retain distinct face materials.
- 15 registered statue blocks backed by 7 deity geometries.
- 9 material-specific tombstones backed by the legacy eight-element Blockbench geometry.

## Deterministic conversion

Run `node scripts/audit_complex_block_models.js --write` from the repository root. The generator reads only the checked-in 1.12.2 source tree. Each legacy statue OBJ object group becomes one bounded vanilla model element, preserving the multipart silhouette without a Forge/NeoForge-specific OBJ loader. The seven MTL-selected textures are copied byte-for-byte. Tombstones retain the legacy Blockbench elements. The generated JSON uses stable ordering and LF line endings.

The OBJ conversion deliberately approximates each named object group by its axis-aligned bounds. This preserves the deity-specific multipart proportions and is materially more faithful than a shared cube, while remaining loader-neutral; arbitrary non-axis-aligned OBJ faces are the only known geometric limitation.

## Audit gate

Run `node scripts/audit_complex_block_models.js --check`. Success prints:

```text
RR_COMPLEX_BLOCK_MODEL_AUDIT_OK ... blocked=0
```

The gate rejects missing or duplicate catalog owners, stale legacy hashes, invalid JSON, missing textures, shared idle/active machine models, incomplete facing rotations, non-layered ores, statues with fewer than five elements, tombstones that differ from the legacy eight-element form, and multiface blocks with fewer than three texture bindings. Gradle `check`, the release audit, and the datagen asset provider all consume the permanent contract.