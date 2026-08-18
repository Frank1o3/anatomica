# Anatomica

Anatomica is a Fabric mod that adds configurable, physics-driven body attachments to player avatars. It's a hobby project inspired by Female Gender Mod, though it's a separate mod with its own architecture and isn't intended as a drop-in replacement.

## Status

Anatomica is in **stable beta**. Its core customization, rendering, physics, networking, and extension systems are implemented and usable in normal play. The remaining work is mostly polish and compatibility — especially a few texture-rotation edge cases — before a full release. Compatibility testing and feedback are especially valuable at this stage.

## Features

- Per-player body customization: enable/disable, size, offsets, spread, cleavage, model selection, and physics settings.
- Physics that respond naturally to walking, jumping, falling, turning, and other poses.
- Several built-in attachment models to choose from.
- Per-face skin UV selection with an in-game editor.
- Renders correctly alongside armor and stays synced across multiplayer.
- Other mods can add their own models and physics engines through code.
- Built on [FranklyLib](https://github.com/Frank1o3/franklylib) for UI and rendering.

## Installation

1. Install **Fabric Loader** for the Minecraft version supported by the mod.
2. Install **Fabric API**.
3. Install [**FranklyLib**](https://github.com/Frank1o3/franklylib) — required. See its page for current install notes (it's a manual `.jar` download until its Modrinth listing is approved).
4. Download the Anatomica `.jar` file and place it in your `mods` folder alongside the others.

## Requirements

- Minecraft `26.2`
- Fabric Loader
- Fabric API
- Java 25 or newer
- [FranklyLib](https://github.com/Frank1o3/franklylib)

## Extending Anatomica

Other mods can register their own body models and physics engines through Anatomica's registries — this is done through mod code rather than resource-pack JSON. See the [source repository](https://github.com/Frank1o3/anatomica) for details.

## FranklyLib

Anatomica uses FranklyLib for its UI and rendering support. Maintaining that library benefits Anatomica and other projects, so bug reports, testing feedback, and contributions to FranklyLib are welcome too.

## License

Anatomica is licensed under the [BSD 3-Clause License](https://github.com/Frank1o3/anatomica/blob/main/LICENSE), the same license used by FranklyLib.

## Links

- [Source code and issue tracker](https://github.com/Frank1o3/anatomica)
- [FranklyLib](https://github.com/Frank1o3/franklylib)
