# Anatomica

Anatomica is a client-focused Fabric mod for Minecraft that adds configurable, physics-driven body attachments to player avatars. It is a hobby project inspired by Female Gender Mod, but it is not intended to replace it. Anatomica has its own architecture and is being developed as an experiment in modular body rendering, physics, and resource-pack customization.

## Current features

- Per-player body customization: enablement, size, offsets, spread, cleavage, model selection, and physics settings.
- A node-based soft-body physics engine with walking, jumping, falling, turning, and pose inputs.
- Built-in box, organic, and rounded attachment models.
- Per-face skin UV selection and an in-game UV editor.
- Client-side rendering separated from common/server-safe configuration and synchronization code.
- Integration with FranklyLib for reusable UI and rendering utilities.

## Direction

The long-term goal is to make custom body models data-driven: resource packs should eventually be able to define models with JSON instead of requiring Java code. The built-in physics engines will remain code-driven, because they contain the simulation logic; the current soft-body engine is the first of these. A lightweight rigid-body engine and an armor rendering layer are planned, but are not implemented yet.

This mod is not a finished replacement for Female Gender Mod. It is a learning-oriented hobby project and may contain rendering issues, compatibility gaps, unfinished features, and breaking changes while its systems are refined.

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
