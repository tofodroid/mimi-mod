# MIMI - Musical Instrument Minecraft Interface

## Overview
MIMI is a Forge mod for Minecraft Java Edition that allows you to play music from MIDI files or a MIDI Input Device using a variety of instruments either solo or with a group. The implementation is similar to the way that Rust instruments work and makes for a really easy to use yet powerful system.

| Minecraft Version |        Mod Support Level |
| -------------     |                    -----:|
| < 1.16            |                     None |
| 1.16.x            |         Features & Fixes |
| 1.17              |  None (until Forge 1.17) |

## Questions
Have a question about how something in the mod works? First check the in-game documentation book to see if it has an answer for you. If not open an issue in the Issues tab above using the `Question` template. Note that issues asking about a port to another mod loader or a different version of Minecraft will be removed.

## Reporting Bugs and Issues
I don't have tests setup for MIMI at this time and as such bugs are fairly likely. If you encounter a bug or an issue please report it using the Issues tab above using the `Bug` template. Note that issues requesting a port to another mod loader or a different version of Minecraft will be removed.

## Suggestions
I have a fairly lengthy list of potential new features and improvements to add to MIMI in the future (see below) but am also always open to suggestions. Please open an issue in the Issues tab above using the `Improvement Request` or `Feature Request` template. Note that issues requesting a port to another mod loader or a different version of Minecraft will be removed.

## Downloads
**Official JAR builds of the mod will be posted on CurseForge and only on CurseForge. If you find a JAR download on any site other than CurseForge it is not an official build and you may be putting your system at risk if you use it.**

## Redistributing
This mod may be included in modpacks. If you choose to redistribute the mod JAR itself outside of CurseForge note the conditions of the [MIT License](https://opensource.org/licenses/MIT) and understand that from my point of view your build will be considered unofficial as noted in the section above.

## Minecraft Versions
MIMI is a mod that I am developing in my spare time and as such it will only be supporting a limited subset of Minecraft and Forge versions. When a new version is released it may take some time for the mod to be updated. Fabric is not supported and will not be supported by me, but if someone would like to port the mod to Fabric that would be fine! The current versions that this mod officially supports are outlined in the overview above.

## Forking and Contribtuing
Feel free to fork the mod (note the conditions of the [MIT License](https://opensource.org/licenses/MIT)) and/or provide pull requests back to this repository! I do not spend a great deal of time on working on this mod or monitoring the GitHub page so it is possible that pull requests may sit for some time before I see them but I will do my best to review them as soon as possible!

## Future Feature Considerations
This is a list of features and improvements that I am currently considering adding to MIMI in the future. Note that these are not in priority order or garauntees.

- Add music recording using MIDI Sequencers

- Add controllable instrument volume

- Add redstone "listener" that can output redstone signals based on nearby played notes and configured settings

- Add redstone "receiver" that can output redstone signals based on nearby transmitted notes and configured settings

- Add instrument variants (piano woods, drum colors via dye, etc.)

- Add uncraftable instruments that can only be found as loot or from villagers

- Add music villagers that sell instruments and possibly songs

- Add multiple voices to existing instruments (I.E: microphone currently uses "dos" but could have a toggle for "ahs")

- Add the ability to apply a banner pattern to the bass drum

- Add note particles when instruments are playing

- Add basic animations for playing instruments

- Add mechanical musician that can play instruments like a player

- Add additional midi settings from config file to in-game GUI