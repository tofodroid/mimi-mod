# MIMI - Musical Instrument Minecraft Interface: Custom Instruments

## Overview
This example adds two instruments to MIMI - a Viola (hand-held instrument) and an Electronic Piano (a block instrument). There are two major components of this example: A ResourcePack/DataPack and a MIMI config file (`custom.json`).

## The custom.json Config File
When MIMI starts for the first time a configuration file called `custom.json` is created in the `/config/mimi` folder of your Minecraft instance (or your server) and this is where you should define your custom instruments. If you are playing multiplayer it is required that every player _and_ the server have the exact same `custom.json` file in their `/config/mimi` folder.

The `custom.json` file should contain a JSON array of objects using the following structure:

```json
{
    "registryName": "epiano",
    "midiBankNumber": 0,
    "midiPatchNumber": 4,
    "isBlock": true,
    "collisionShapes": [
        "0,0,0,16,12,10",
        "0,0,0,16,6,16"
    ]
}
```

**Property Definitions**
* _registryName:_ The identifier that should be associated with this instrument's Item ID (I.E: `mincraft:iron_ingot` in Vanilla). Should be all lower case with only letters and underscores.
* _midiBankNumber:_ The bank number that contains the MIDI Patch that you want associated with this instrument. This is almost always 0 unless you want to venture into drum kits or the extended MIDI spec.
* _midiPatchNumber:_ The patch number for the MIDI sound that you want associated with this instrument. A list of MIDI 1.0 patch numbers (all in bank 0) can be found [here](https://pjb.com.au/muscript/gm.html#patch).
* _isBlock:_ Whether or not this instrument is a block instrument (`true`) or a hand-held instrument (`false`).
* _collisionShapes:_ Required only if `isBlock` is set to `true`. A list of cuboid definitions to form the collision shape for this instrument. The coordinates of the cuboids are defined via: `"x1,y1,z1,x2,y2,z2"` strings and all of the cuboids are merged together to form the final collision shape. The collision shape you define here should be for the block when it is facing North. MIMI will automatically rotate your collision shape when the block is not facing North.

## The ResourcePack/DataPack
Once you have your instruments added to the custom.json file they will appear in-game and be completely usable, but they will lack any textures, models, recipes, or names (other than the registry name). In order to provide all of this information for your custom instruments you will need to create (and include in both the server and clients in multiplayer) a ResourcePack/DataPack.

### Adding a Hand-Held Instrument
Adding data for a hand-held instrument is a bit simpler than for a block instrument because there are fewer required components. The following are all of the files that are needed to add a hand-held instrument:

1. **Localization Text:** Adding a nice name for your instrument in-game.
    * **File:** `assets/mimi/lang/<language.json>`
    * **Content:** In the JSON file create an entry for `item.mimi.<registryname>` using the registry name of your instrument from `custom.json`.
    * **Example:**
        ```json
        {
            "item.mimi.viola": "Viola"
        }
        ```

2. **Item Model:** Adding the model shown when holding your instrument.
    * **File:** `assets/mimi/models/item/<registryname>.json`
    * **Content:** Item models are very simple and essentially just point to the texture that should be used.
    * **Example:**
        ```json
        {
            "parent": "item/handheld",
            "textures": {
                "layer0": "mimi:item/viola"
            }
        }
        ```

3. **Item Texture:** Adding the texture for your instrument.
    * **File:** `assets/mimi/textures/item/<registryname>.png`
    * **Content:** The texture to use for your Item. The default size is 16x16.
    * **Example:** 
        ```json
        See the example resourcepack in this folder
        ```

4. **Item Recipe:** Adding the crafting recipe for your instrument.
    * **File:** `data/mimi/recipes/<registryname>.json`
    * **Content:** A crafting recipe definition following the spec [here](https://minecraft.fandom.com/wiki/Recipe#JSON_format)
    * **Example:**
        ```json
        {
            "type": "minecraft:crafting_shaped",
            "pattern": [
                " SP",
                "SPT",
                "N S"
            ],
            "key": {
                "N": {
                    "item": "minecraft:note_block"
                },
                "S": {
                    "item": "minecraft:string"
                },
                "T": {
                    "item": "minecraft:stick"
                },
                "P": {
                    "tag": "minecraft:planks"
                }
            },
            "result": {
                "item": "mimi:viola"
            }
        }
        ```

4. **Recipe Advancement (Optional):** Adding the trigger to "unlock" this recipe in the in-game crafting book.
    * **File:** `data/mimi/advancements/recipes/<registryname>.json`
    * **Content:** The advancement definition to unlock the recipe following the spec [here](https://minecraft.fandom.com/wiki/Advancement/JSON_format)
    * **Example:**
        ```json
        {
            "parent": "minecraft:recipes/root",
            "rewards": {
                "recipes": [
                    "mimi:viola"
                ]
            },
            "criteria": {
                "has_note": {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {
                        "items": [
                            {
                                "item": "minecraft:note_block"
                            }
                        ]
                    }
                },
                "has_the_recipe": {
                    "trigger": "minecraft:recipe_unlocked",
                    "conditions": {
                        "recipe": "mimi:viola"
                    }
                }
            },
            "requirements": [
                [
                    "has_note",
                    "has_the_recipe"
                ]
            ]
        }
        ```

### Adding a Block Instrument
The process for adding a new block instrument is similar to the process for hand-held instruments but comes with a number of extra steps to make the block fully functional in-game.

1. **Localization Text:** Adding a nice name for your instrument in-game.
    * **File:** `assets/mimi/lang/<language.json>`
    * **Content:** In the JSON file create an entry for `block.mimi.<registryname>` using the registry name of your instrument from `custom.json`.
    * **Example:**
        ```json
        {
            "block.mimi.epiano": "Electric Piano"
        }
        ```

2. **Block Model:** Adding model for your block instrument.
    * **File:** `assets/mimi/models/block/<registryname>.png`
    * **Content:** The JSON model definition for your instrument. There are many tools that can be used to create JSON models for Minecraft. I personally use [BlockBench](https://www.blockbench.net/). Note that you will likely need to manually tweak the texture paths in your Block model JSON to match the paths in the resourcepack.
    * **Example:** 
        ```json
        Too big to show here. See the example resourcepack in this folder.
        ```

3. **Item Model:** Adding the model shown when holding your instrument.
    * **File:** `assets/mimi/models/item/<registryname>.json`
    * **Content:** Item models for blocks are even simpler than for Items because they just point to the block model.
    * **Example:**
        ```json
        {
            "parent": "mimi:block/epiano"
        }
        ```

4. **Block Texture(s):** Adding the texture(s) for your instrument model. Note that you may need to manually tweak the texture paths in your Block model JSON to match the paths in the resourcepack.
    * **File:** `assets/mimi/textures/block/<registryname>.png`
    * **Content:** The texture(s) to use for your Block. The default size is 16x16.
    * **Example:** 
        ```json
        See the example resourcepack in this folder
        ```

5. **Block State Definition:** Letting Minecraft know what model to use for your Block depending on the situation.
    * **File:** `assets/mimi/blockstates/<registryname>.json`
    * **Content:** The block state definitions for your block. MIMI instruments support rotating the model based on the block orientation the collision shape you define in the `custom.json` file will be rotated to match.
    * **Example:** 
        ```json
        {
            "variants": {
                "facing=south": { "model": "mimi:block/epiano" },
                "facing=west": { "model": "mimi:block/epiano", "y": 90 },
                "facing=north": { "model": "mimi:block/epiano", "y": 180 },
                "facing=east": { "model": "mimi:block/epiano", "y": 270 }
            }
        }
        ```

6. **Block Loot Table:** Letting Minecraft know to give you the correct item when you break your block in-game.
    * **File:** `data/mimi/loot_tables/blocks/<registryname>.json`
    * **Content:** The loot table definition for your block following the spec [here](https://minecraft.fandom.com/wiki/Loot_table).
    * **Example:** 
        ```json
        {
            "type": "minecraft:block",
            "pools": [
                {
                    "name": "epiano",
                    "rolls": 1,
                    "entries": [
                        {
                            "type": "minecraft:item",
                            "name": "mimi:epiano"
                        }
                    ],
                    "conditions": [
                        {
                            "condition": "minecraft:survives_explosion"
                        }
                    ]
                }
            ]
        }
        ```

7. **Block Recipe:** Adding the crafting recipe for your instrument.
    * **File:** `data/mimi/recipes/<registryname>.json`
    * **Content:** A crafting recipe definition following the spec [here](https://minecraft.fandom.com/wiki/Recipe#JSON_format)
    * **Example:**
        ```json
        {
            "type": "minecraft:crafting_shaped",
            "pattern": [
                "GGG",
                "GNG",
                "BBB"
            ],
            "key": {
                "G": {
                    "tag": "minecraft:glowstone"
                },
                "N": {
                    "item": "minecraft:note_block"
                },
                "B": {
                    "item": "minecraft:stone_button"
                }
            },
            "result": {
                "item": "mimi:epiano"
            }
        }
        ```

8. **Recipe Advancement (Optional):** Adding the trigger to "unlock" this recipe in the in-game crafting book.
    * **File:** `data/mimi/advancements/recipes/<registryname>.json`
    * **Content:** The advancement definition to unlock the recipe following the spec [here](https://minecraft.fandom.com/wiki/Advancement/JSON_format)
    * **Example:**
        ```json
        {
            "parent": "minecraft:recipes/root",
            "rewards": {
                "recipes": [
                    "mimi:epiano"
                ]
            },
            "criteria": {
                "has_note": {
                    "trigger": "minecraft:inventory_changed",
                    "conditions": {
                        "items": [
                            {
                                "item": "minecraft:note_block"
                            }
                        ]
                    }
                },
                "has_the_recipe": {
                    "trigger": "minecraft:recipe_unlocked",
                    "conditions": {
                        "recipe": "mimi:epiano"
                    }
                }
            },
            "requirements": [
                [
                    "has_note",
                    "has_the_recipe"
                ]
            ]
        }
        ```