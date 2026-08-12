# Anatomica

Anatomica is a client-focused Fabric mod for Minecraft that adds configurable, physics-driven body attachments to player avatars. It is a hobby project inspired by Female Gender Mod, but it is not intended to replace it. Anatomica has its own architecture and is being developed as an experiment in modular body rendering, physics, and resource-pack customization.

## Status

Anatomica is in **stable beta**. Its core customization, rendering, physics,
networking, and extension systems are implemented and usable in normal play.
The remaining work is focused on polish and compatibility—especially a few
texture-rotation edge cases—before the mod can be considered ready for a full
release.

## Features

- Per-player body customization: enablement, size, offsets, spread, cleavage, model selection, and physics settings.
- A node-based soft-body physics engine with walking, jumping, falling, turning, and pose inputs.
- Built-in wedge, organic, and rounded attachment models.
- Per-face skin UV selection and an in-game UV editor.
- Client-side rendering, armor rendering, and server-safe configuration synchronization.
- Pluggable registries for deformable models and physics engines, with safe defaults when a saved entry is unavailable.
- Integration with FranklyLib for reusable UI and rendering utilities.

## Extensibility

Anatomica's model and physics selection is data-driven through mod registries.
Additional models and physics engines are supplied as mod code rather than
resource-pack JSON. This keeps stateful rendering and simulation behavior in a
controlled code path, avoiding race conditions associated with runtime resource
loading while still allowing other mods to extend the available content.

The beta still has minor texture-rotation fixes and general polish remaining.
Compatibility testing and feedback are especially valuable at this stage.

Anatomica is inspired by Female Gender Mod, but is a separate project with its
own architecture and goals; it is not intended as a drop-in replacement.

## FranklyLib

Anatomica uses [FranklyLib](https://github.com/Frank1o3/franklylib) for its UI and rendering support. Maintaining that library benefits Anatomica and other projects, including planned work to bring the Proportionality mod to older Minecraft versions. Bug reports, testing feedback, documentation improvements, and contributions to FranklyLib are especially welcome.

## Development

The project targets Minecraft 26.2, Fabric Loader, Java 25, Fabric API, and FranklyLib. To start a development client:

```bash
./gradlew runClient
```

The development client may use a generated profile/skin. For accurate UV testing, use a stable skin whose torso pixels are easy to identify.

## License

Anatomica is licensed under the [BSD 3-Clause License](LICENSE), the same license used by FranklyLib.

## Links

- [Source code and issue tracker](https://github.com/Frank1o3/anatomica)
- [FranklyLib](https://github.com/Frank1o3/franklylib)
