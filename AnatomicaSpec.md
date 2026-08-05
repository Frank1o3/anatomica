# Anatomica — Mod Design Specification

**Purpose of this document:** this is a from-scratch design spec for a new, independent
Fabric mod called **Anatomica**. It describes desired behavior and architecture only.
Do not port, copy, or reference code from any other mod's source — implement everything
below as new, original code. The only pre-existing code that should be reused verbatim
is explicitly called out in **Section 9 (Existing Files to Reuse)**; everything else is
built new from this spec.

---

## 0. Identity

| Field | Value |
| --- | --- |
| Mod name | Anatomica |
| Mod ID | `anatomica` |
| Root Java package | `com.frank1o3.anatomica` |
| License | Choose your own (MIT/LGPL/etc — this is original code, not a derivative) |
| Loader | Fabric |
| Minecraft version | 26.2 (adjust to whatever you're actually targeting) |
| Java version | 25 |
| Source layout | Loom `splitEnvironmentSourceSets()` — `main` (common) + `client` (client-only). Add a `server` source set only if/when server-only logic (e.g. admin commands) is needed. |

Package roots:

- `com.frank1o3.anatomica` — common code (`src/main`)
- `com.frank1o3.anatomica.client` — client-only code (`src/client`)

**Hard rule:** nothing in `com.frank1o3.anatomica` (common) may import anything from
`net.minecraft.client.*`, rendering classes, or GUI classes. If a class needs those,
it belongs under `com.frank1o3.anatomica.client`. This is the boundary that keeps a
dedicated server able to load the mod without a client physics/rendering stack.

---

## 1. Feature Summary

Anatomica lets players customize an avatar body-shape attachment (breast size/shape)
with:

1. **Server-authoritative config sync** — one player's settings are visible to other
   players on the same server, relayed through the server. No cloud service, no
   account gating, no external HTTP calls. Everything works fully offline / on a
   LAN world / on a vanilla-auth server.
2. **Pluggable physics engines** — registry of `IPhysicsEngine` implementations.
   Ship with exactly one: a **soft-body, node-based physics simulation** (mass-spring /
   PBD-style, your own design — see Section 4). No rigid "box translation" physics
   engine; softbody is the only and default engine.
3. **Pluggable deformable models** — registry of `IDeformableModel` implementations.
   Ship with a **box model** and a **subdivided organic mesh model**, both driven by
   the *same* generic node-skinning code (Section 5) — no per-model special-casing in
   the renderer.
4. **Per-player local config file**, synced to server on change, server relays to
   nearby clients.
5. **In-game GUI** built from your own existing widget kit (`ScaleSlider`,
   `ScaleButton`, `ScaleGuiUtils`, `BaseScaleScreen` — see Section 9), not from any
   other mod's GUI code.

---

## 2. Project Structure

```text
anatomica/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/frank1o3/anatomica/
│   │   │   ├── Anatomica.java                     # ModInitializer
│   │   │   ├── config/
│   │   │   │   ├── BodyConfig.java                # per-entity data (pure POJO)
│   │   │   │   ├── BodyConfigKey.java              # generic typed config key
│   │   │   │   ├── keys/
│   │   │   │   │   ├── FloatConfigKey.java
│   │   │   │   │   ├── BooleanConfigKey.java
│   │   │   │   │   ├── EnumConfigKey.java
│   │   │   │   │   └── IdentifierConfigKey.java
│   │   │   │   └── AnatomicaConfig.java            # registered key table + defaults
│   │   │   ├── data/
│   │   │   │   └── EntityBodyData.java             # cache: UUID -> BodyConfig, common-side
│   │   │   ├── physics/
│   │   │   │   ├── IPhysicsEngine.java             # interface, no impl
│   │   │   │   ├── PhysicsEngineFactory.java        # @FunctionalInterface for registry
│   │   │   │   └── PhysicsNode.java                # record: id, restPos, pos, fixed
│   │   │   ├── model/
│   │   │   │   ├── IDeformableModel.java           # interface, no impl
│   │   │   │   ├── ModelFactory.java                # @FunctionalInterface for registry
│   │   │   │   └── ModelVertex.java                 # record: pos, uv, nodeInfluences
│   │   │   ├── registry/
│   │   │   │   └── AnatomicaRegistries.java         # DefaultedRegistry<...> for physics+models
│   │   │   └── networking/
│   │   │       ├── BodySyncPacket.java              # C2S and S2C, one shared codec
│   │   │       └── AnatomicaNetworking.java         # register() common-side handlers
│   │   └── resources/
│   │       └── fabric.mod.json
│   └── client/
│       └── java/com/frank1o3/anatomica/client/
│           ├── AnatomicaClient.java                 # ClientModInitializer
│           ├── physics/
│           │   └── SoftbodyPhysicsEngine.java       # THE physics engine impl
│           ├── model/
│           │   ├── BoxDeformableModel.java
│           │   └── OrganicMeshDeformableModel.java
│           ├── render/
│           │   ├── BodyRenderLayer.java             # RenderLayer<...>
│           │   └── NodeSkinning.java                # shared vertex<-node blending math
│           ├── gui/                                  # <-- reuse your existing files here
│           │   ├── ScaleSlider.java                  # (yours, verbatim)
│           │   ├── ScaleButton.java                  # (yours, verbatim)
│           │   ├── ScaleGuiUtils.java                # (yours, verbatim)
│           │   ├── BaseScaleScreen.java              # (yours, verbatim — base class)
│           │   └── screen/
│           │       ├── BodyCustomizationScreen.java  # extends BaseScaleScreen
│           │       └── ModelSelectScreen.java         # extends BaseScaleScreen
│           └── networking/
│               └── AnatomicaClientNetworking.java
```

---

## 3. Common-Side Data Model

### 3.1 `BodyConfig`

Pure data holder, no Minecraft client imports. Fields (all with sane defaults):

- `float size` (0.0–1.0)
- `float offsetX, offsetY, offsetZ`
- `float spread` ("cleavage"/outward angle equivalent), small range e.g. 0–0.1
- `boolean independentSides` (renamed, clearer version of "uniboob" — `true` = each side
  simulates independently, `false` = mirrored)
- `boolean physicsEnabled`
- `float bounceStrength` (0–1, drives engine intensity)
- `float softness` (0–1, drives engine compliance/damping)
- `Identifier physicsEngineId` (registry key, default `anatomica:softbody`)
- `Identifier modelId` (registry key, default `anatomica:box`)
- `boolean showInArmor`

Provide:

- `toNbt()` / `fromNbt()` (for save-to-disk) — use `CompoundTag` directly, don't build a
  bespoke JSON layer unless you specifically want human-editable config files (if you
  want that, use Gson the same way but keep it in a dedicated `io/` subpackage).
- `copy()`

### 3.2 `BodyConfigKey<T>`

Generic typed key + validator + default, modeled loosely like a `GameRule`. Each key
knows how to read/write itself to/from `CompoundTag` and how to clamp/validate a value.
Concrete subclasses: `FloatConfigKey` (min/max), `BooleanConfigKey`, `EnumConfigKey<E>`,
`IdentifierConfigKey` (for physics engine / model selection, validated against the
relevant registry at read-time — invalid/missing IDs silently fall back to the
registry's default entry).

### 3.3 `AnatomicaConfig`

Static table of all `BodyConfigKey` instances plus a `List<RegisteredKey<?>>` that maps
each key to a getter/setter pair on `BodyConfig`, used for generic load/save/dump —
avoids writing repetitive per-field NBT code.

### 3.4 `EntityBodyData`

`LoadingCache<UUID, BodyConfig>` (Guava), common-side. Server populates/holds this
authoritatively; client uses the same class to cache configs received via sync packets
for *other* players, plus its own local player's config loaded from disk.

No `SyncStatus` enum needed unless you want one — keep it simple: `UNSET` (never
received/loaded) vs `SET`.

---

## 4. Physics — Softbody Engine (Client-Only)

### 4.1 Interface (common-side, `IPhysicsEngine`)

```java
public interface IPhysicsEngine {
    void tick(float deltaTime, LivingEntityLike entity, BodyConfig config);
    void reset();
    int nodeCount();
    Vec3 nodeRestPosition(int index);
    Vec3 nodePosition(int index);       // current, post-tick
    Vec3 nodeVelocity(int index);
    boolean isNodeFixed(int index);
    void interpolate(float partialTick); // writes an interpolated snapshot for rendering
    Vec3 interpolatedNodePosition(int index);
    void applyImpulse(Vec3 localPoint, Vec3 force);
}
```

Keep this interface **free of legacy scalar getters** (no `getPositionX()`,
`getBounceRotation()`, etc). Every consumer (models, debug HUD) reads node data only.
This is the #1 structural improvement over ad-hoc "legacy compat" getters bolted onto
physics engines — don't reintroduce that pattern.

`LivingEntityLike` — a tiny common-side wrapper interface exposing only what physics
needs (position delta, pose, swing state, vehicle, random) so the interface technically
doesn't have to live client-side even though the only implementation will. (Optional:
if you decide physics genuinely never needs to run outside the client, it's fine to put
`IPhysicsEngine` itself under `client` instead — pick one and be consistent. Recommended:
keep the **interface** in `main` so a hypothetical dedicated-server debug/validation
tool could reference the type without needing client classes, but only ship the real
implementation client-side.)

### 4.2 `SoftbodyPhysicsEngine` (client-only, the actual simulation)

Design as an independent-implementation soft-body system — do not copy any specific
mass-spring/PBD code you've seen elsewhere; use this as a functional spec instead:

- **Grid**: configurable `cols × rows × layers` (default 3×3×3 = 27 nodes), stored as
  flat `float[]` arrays (`posX/Y/Z`, `restX/Y/Z`, `velX/Y/Z`) — avoid `Vec3[]` for the
  hot per-tick arrays, wrap with `Vec3` only at the interface boundary for callers.
- **Fixed layer**: back layer (`z == 0`, chest-anchor) nodes are pinned and never
  integrated.
- **Constraints**: build distance constraints between 6-connected neighbors once in the
  constructor from rest positions; solve with a small number of Gauss-Seidel-style
  relaxation iterations per tick (Position-Based Dynamics style: predict, correct,
  derive velocity from position delta). 4–6 iterations is a good default; make it a
  constant, not a config option, unless you want to expose "physics quality."
- **External forces**: derive a per-tick impulse from entity motion delta, walk-cycle
  oscillation, pose transitions (crouch/sleep), vehicle state, and arm swing — same
  *categories* of input as any believable body-jiggle sim, but write your own weighting
  curves; don't reuse specific magic-number tuning from elsewhere. Treat this as a todo
  you'll hand-tune by feel in-game.
- **Config coupling**: `bounceStrength` scales impulse magnitude; `softness` scales
  damping/compliance (higher `softness` → looser springs, more visible jiggle, slower
  settle).
- **Output**: `getDeformationVertices()`-equivalent is *not* part of the interface —
  models pull node positions directly via `nodePosition(i)` / `interpolatedNodePosition(i)`
  and do their own skinning (see Section 5). This removes the need for the engine to
  know anything about vertex layout.
- **Interpolation**: store `prevPos*` snapshot each tick; `interpolate(alpha)` lerps
  into a separate interpolated-output array read by the renderer. Never let the
  renderer read raw simulation state mid-tick.

### 4.3 Registry

```java
public final class AnatomicaRegistries {
    public static final DefaultedRegistry<PhysicsEngineFactory> PHYSICS_ENGINES = ...;
    public static final DefaultedRegistry<ModelFactory> MODELS = ...;
}
```

Register the built-in engine/model client-side in `AnatomicaClient#onInitializeClient()`:

```java
Registry.register(AnatomicaRegistries.PHYSICS_ENGINES, Anatomica.id("softbody"), SoftbodyPhysicsEngine::new);
Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("box"), BoxDeformableModel::new);
Registry.register(AnatomicaRegistries.MODELS, Anatomica.id("organic"), OrganicMeshDeformableModel::new);
```

Default entries (`anatomica:softbody`, `anatomica:box`) are what `IdentifierConfigKey`
falls back to when a stored/synced ID doesn't resolve (unknown engine from a mod that
isn't installed, corrupted config, etc).

---

## 5. Models — Unified Node-Skinning (Client-Only)

This is the key structural simplification versus "legacy scalar fallback + separate
per-vertex softbody path": **every model skins itself from physics nodes, always**,
including the box model. There is no legacy/fallback branch anywhere in the renderer.

### 5.1 Interface

```java
public interface IDeformableModel {
    ModelVertex[] baseVertices();     // rest-pose vertices, each pre-tagged with node influences+weights
    int[] indices();                  // triangle list
    float[] baseUVs();                // flat [u0,v0,u1,v1,...] matching baseVertices order
    Identifier id();
    Component displayName();

    default Vec3[] deform(IPhysicsEngine engine) {
        Vec3[] base = baseVertices();
        Vec3[] out = new Vec3[base.length];
        for (int i = 0; i < base.length; i++) {
            out[i] = NodeSkinning.skin(base[i], engine);
        }
        return out;
    }
}
```

`ModelVertex` carries its own `nodeInfluences: int[]` + `nodeWeights: float[]` computed
once at model-construction time (e.g. via inverse-distance weighting to the 2–4 nearest
rest-space physics nodes). `NodeSkinning.skin()` is a single shared static method:

```java
static Vec3 skin(ModelVertex v, IPhysicsEngine engine) {
    Vec3 accum = Vec3.ZERO;
    float totalWeight = 0;
    for (int k = 0; k < v.nodeInfluences().length; k++) {
        int node = v.nodeInfluences()[k];
        float w = v.nodeWeights()[k];
        Vec3 delta = engine.interpolatedNodePosition(node).subtract(engine.nodeRestPosition(node));
        accum = accum.add(delta.scale(w));
        totalWeight += w;
    }
    return v.restPosition().add(totalWeight > 0 ? accum.scale(1f / totalWeight) : Vec3.ZERO);
}
```

Both `BoxDeformableModel` (8 vertices, weighted toward the corner-nearest of the 27
nodes — effectively degenerates to whichever handful of nodes are closest) and
`OrganicMeshDeformableModel` (subdivided grid mesh, many more vertices, smoother
weight falloff) go through the *exact same* skinning call. No `isCustomMesh()` flag,
no dual code paths in the render layer.

### 5.2 `OrganicMeshDeformableModel`

Generate a subdivided box mesh (front/back/top/bottom/left/right faces, N×N quads per
face — 6 or 8 subdivisions is plenty) purely proceduraly in a static initializer, same
general idea as any subdivided-cuboid mesh generator: build a vertex grid per face,
triangulate each cell with consistent CCW winding, compute UVs per-face in [0,1] and
remap to the model's assigned `UVQuad` at render time. Write this from scratch — it's
straightforward computational geometry, not something that needs source-level reuse.

---

## 6. Rendering (Client-Only)

`BodyRenderLayer extends RenderLayer<S, M>`:

1. On `submit()`, pull the entity's `BodyConfig` + physics engine instance (owned by a
   client-side per-entity physics holder, ticked every entity tick — see 6.1) + model
   instance (looked up in `AnatomicaRegistries.MODELS` by `config.modelId()`).
2. Call `model.deform(engine)` to get world-local deformed vertices.
3. Submit as custom geometry (triangle list from `model.indices()`), mapping UVs through
   the player's configured UV layout the same general way as any dynamic-mesh render
   command — build this fresh, don't copy an existing `BreastRenderCommand`-style class
   verbatim; the *shape* of "custom geometry render command carrying a model+engine
   reference" is fine to reinvent, the specific code should be new.
4. Handle left/right mirroring by flipping X and the triangle winding for one side, same
   as any bilateral cosmetic attachment.

### 6.1 Per-entity physics holder (client-only)

A small `ClientBodyPhysics` class per tracked entity (owned by whatever your client-side
entity-data cache is), holding one or two `IPhysicsEngine` instances (one if
`independentSides == false`, two if `true`), created via
`AnatomicaRegistries.PHYSICS_ENGINES.get(config.physicsEngineId())`, re-created whenever
`physicsEngineId` changes. Ticked once per client entity tick from an event hook.

---

## 7. Networking

One packet type, sent both directions (client→server on local-player change, server→
client relay to nearby players), same general shape as any "sync this player's cosmetic
config" packet:

```java
public record BodySyncPacket(UUID uuid, CompoundTag data) implements CustomPacketPayload { ... }
```

Simplest correct approach: serialize the whole `BodyConfig` via NBT using the
`AnatomicaConfig` registered-key table (reuse the same dump/load logic as the local
config file, so there's exactly one serialization path for both disk and network) rather
than hand-writing a parallel `StreamCodec` field list. If you'd rather have a tighter
wire format, define an explicit `StreamCodec<ByteBuf, BodyConfig>` composed from each
key's own codec — pick whichever you prefer, but **do not** maintain two independent
serialization formats (one for disk, one for network) long-term, since keeping them in
sync is exactly the kind of maintenance burden you're trying to get away from.

Server relay behavior: on receiving a client's packet, validate UUID matches the sender,
store into the server-side `EntityBodyData` cache, then re-broadcast to all players
currently tracking that entity (Fabric's `EntityTrackingEvents`/`PlayerLookup.tracking`).
On a new player entering tracking range of an already-known player, immediately send
that player's current config once (no periodic re-broadcast needed beyond change +
initial sync).

No hello/version-handshake packet is required unless you want one for future wire-format
migrations — if you do want it, keep it to a single integer version number sent once on
join, same idea as any protocol-version handshake.

---

## 8. GUI

Screens are built entirely from the widget kit in Section 9 (your own files). Two
screens to start:

- **`BodyCustomizationScreen`** (extends `BaseScaleScreen`): sliders for size, offsets,
  spread, bounce strength, softness; toggle buttons for physics enabled / independent
  sides / show-in-armor.
- **`ModelSelectScreen`** (extends `BaseScaleScreen`): one `ScaleButton` per entry in
  `AnatomicaRegistries.MODELS`/`PHYSICS_ENGINES`, highlighting the currently-selected
  entry, same interaction pattern as any "pick one of N registered options" list screen.

Both screens read the current `BodyConfig` for the local player, mutate a working copy
as sliders move, and push a `BodySyncPacket` to the server on release/close (mirroring
whatever your slider's `.save()` callback convention already is per `ScaleSlider`).

---

## 9. Existing Files to Reuse (verbatim, as your own prior work)

These four files were supplied separately and should be copied in as-is under
`com.frank1o3.anatomica.client.gui` (with only the package declaration, since they're
already in `frank1o3.statscale.client.gui` — decide whether to keep them under a shared
personal "widget kit" package across your mods, or move them into
`com.frank1o3.anatomica.client.gui` directly):

- `ScaleSlider.java`
- `ScaleButton.java`
- `ScaleGuiUtils.java`
- `BaseScaleScreen.java`

`ScaleGuiUtils` currently references `frank1o3.statscale.client.mixin.accessors.InventoryScreenAccessor`
— Anatomica needs its own copy of that accessor mixin (same one-method
`@Invoker`/`@Accessor` interface pattern, just declared in
`com.frank1o3.anatomica.client.mixin.accessors`), since mixins aren't shared across mod
jars. Everything else in these four files can be used unmodified.

Nothing else should be treated as "existing code to port" — all other classes described
in this spec are new.

---

## 10. Build Setup Notes

- `fabric.mod.json`: `"id": "anatomica"`, `"name": "Anatomica"`, entrypoints
  `main -> com.frank1o3.anatomica.Anatomica`, `client -> com.frank1o3.anatomica.client.AnatomicaClient`.
- `build.gradle`: `group = 'com.frank1o3'`, same `splitEnvironmentSourceSets()` /
  `loom { mods { "anatomica" { sourceSet sourceSets.main; sourceSet sourceSets.client } } }`
  pattern as any split-source Fabric mod.
- No cloud/HTTP dependencies at all — no `java.net.http` usage anywhere in this mod.
- Suggested Fabric API modules: `fabric-networking-api-v1`, `fabric-lifecycle-events-v1`,
  `fabric-key-mapping-api-v1`, `fabric-rendering-v1`. Add `fabric-registry-sync-v0` if the
  physics-engine/model registries should be synced/validated against the server's set
  (recommended, so a client with extra registered engines/models doesn't desync from a
  server that doesn't have them).

---

## 11. Suggested Build Order (for an agent implementing this spec)

1. Scaffold project (`build.gradle`, `settings.gradle`, `fabric.mod.json`, empty
   `Anatomica` / `AnatomicaClient` entrypoints that just log "Anatomica loaded").
2. Common config layer: `BodyConfigKey` + subclasses, `BodyConfig`, `AnatomicaConfig`,
   `EntityBodyData`.
3. Registries: `AnatomicaRegistries`, empty at first.
4. Physics interface (`IPhysicsEngine`, `PhysicsNode`) — no implementation yet.
5. Model interface (`IDeformableModel`, `ModelVertex`) — no implementation yet.
6. Networking: `BodySyncPacket` + server relay logic, wired to `EntityBodyData`.
7. Client: `SoftbodyPhysicsEngine` implementation + register it.
8. Client: `BoxDeformableModel` implementation + register it (get something rendering
   before tackling the subdivided mesh).
9. Client: `BodyRenderLayer` + `NodeSkinning`, wired to a real `RenderLayer` registration
   on the player renderer, using the box model first.
10. Client: `OrganicMeshDeformableModel`, verify it renders through the same
    `BodyRenderLayer` with zero renderer-side special-casing.
11. Client GUI: copy in the four reused files, build `BodyCustomizationScreen` and
    `ModelSelectScreen`, wire to a keybind.
12. Polish pass: config file save/load to disk, join-sync behavior, debug HUD entry
    (optional).

Each step should be independently buildable/testable before moving to the next —
particularly steps 7–10, since that's where the actual visible payoff (soft-body
jiggle with a clean model abstraction) lives.
