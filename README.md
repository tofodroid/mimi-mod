# MIMI - Musical Instrument Minecraft Interface

## Overview
MIMI is a Forge mod for Minecraft Java Edition that allows you to play music from MIDI files or a MIDI Input Device using a variety of instruments either solo or with a group. The implementation is similar to the way that Rust instruments work and makes for a really easy to use yet powerful system. Mod releases are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mimi-mod/) and now on [Modrinth](https://modrinth.com/mod/mimi/).

| Minecraft Version |        Mod Support Level |
| -------------     |                    -----:|
| < 1.18.x          |                     None |
| 1.18.x            |           Bug Fixes Only |
| 1.19.x            |         Features & Fixes |
| > 1.19.x          |                     None |

## <span style="color:darkred;font-weight:bold;">Known Incompatibilities</span>
1. [Magnesium](https://www.curseforge.com/minecraft/mc-mods/sodium-reforged) / Radium / Rubidium (1.18.x/1.19.x) - Magnesium is a Forge port of the Fabric mod Sodium. It can result in some really fantastic performance increases but at the penalty of possibly causing some graphical issues with other mods. In MIMI this manifests as block colors not rendering properly for block instruments (they will appear mostly white and grey). There is currently a workaround which is to add the following lines to the end of the `sodium-mixins.properties` config file generated by Magnesium:

    ```
    mixin.features.chunk_rendering=false
    ```
    
    I'm trying to find a fix for this issue within MIMI itself but for now the above config file changes are the best way to use MIMI and Halogen together.

2. [Halogen](https://www.curseforge.com/minecraft/mc-mods/halogen) (1.16.x) - Halogen is a Forge port of the Fabric mods Sodium, Phosphor, and Lithium. It can result in some really fantastic performance increases but at the penalty of possibly causing some graphical issues with other mods. In MIMI this manifests as block colors not rendering properly for block instruments (they will appear mostly white and grey). There is currently a workaround which is to add the following lines to the end of the `sodium-mixins.properties` config file generated by Halogen:

    ```
    mixin.features.chunk_rendering=false
    mixin.features.entity=false
    mixin.features.particle=false
    mixin.features.debug=false
    ```
    
    I'm trying to find a fix for this issue within MIMI itself but for now the above config file changes are the best way to use MIMI and Halogen together.
    
## Adding Custom Instruments
MIMI instruments are data-driven which means that it is possible for modpacks to add custom instruments to the mod. Instruments in MIMI are registered in Minecraft as distinct Items and Blocks (where applicable) which means that adding custom instruments requires a combination of both a ResourcePack/DataPack and changes to the `custom.json` file in the `/config/mimi/` directory (created automatically after launching the mod for the first time or can be created manually).

See the [README](example/README.md) in the `/example` directory of this repo for more information and an example `custom.json` and ResourcePack/DataPack.

## Questions
Have a question about how something in the mod works? First check the in-game documentation book to see if it has an answer for you. If not open an issue in the Issues tab above using the `Question` template. Note that issues asking about a port to another mod loader or a different version of Minecraft will be removed.

## Reporting Bugs and Issues
I don't have tests setup for MIMI at this time and as such bugs are fairly likely. If you encounter a bug or an issue please report it using the Issues tab above using the `Bug` template. Note that issues requesting a port to another mod loader or a different version of Minecraft will be removed.

## Suggestions
I have a fairly lengthy list of potential new features and improvements to add to MIMI in the future (see below) but am also always open to suggestions. Please open an issue in the Issues tab above using the `Improvement Request` or `Feature Request` template. Note that issues requesting a port to another mod loader or a different version of Minecraft will be removed.

## Downloads
**Official JAR builds of the mod will be posted on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mimi-mod/) and [Modrinth](https://modrinth.com/mod/mimi/). If you find a JAR download on any site other than CurseForge or Modrinth it is not an official build and you may be putting your system at risk if you use it.**

## Redistributing
This mod may be included in modpacks. If you choose to redistribute the mod JAR itself outside of the official [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mimi-mod/) or [Modrinth](https://modrinth.com/mod/mimi/) page note the conditions of the [MIT License](https://opensource.org/licenses/MIT) and understand that from my point of view your build will be considered unofficial as noted in the section above.

## Minecraft Versions
MIMI is a mod that I am developing in my spare time and as such it will only be supporting a limited subset of Minecraft and Forge versions. When a new version is released it may take some time for the mod to be updated. Fabric is not supported and will not be supported by me, but if someone would like to port the mod to Fabric that would be fine! The current versions that this mod officially supports are outlined in the overview above.

## Forking and Contribtuing
Feel free to fork the mod (note the conditions of the [MIT License](https://opensource.org/licenses/MIT)) and/or provide pull requests back to this repository! I do not spend a great deal of time on working on this mod or monitoring the GitHub page so it is possible that pull requests may sit for some time before I see them but I will do my best to review them as soon as possible!

## Credits

### **MIMI Development**
- TofoDroid

### **Utilized Mods and Libraries**
- Forge by Forge Team
- Patchouli by Vazkii
