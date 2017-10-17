###### [Contributing Guidelines](../../CONTRIBUTING.md) > Basic Tutorials > Basic Blocks

# Basic Blocks

##### Prerequisite Tutorials: [Getting Started](Getting_Started.md)

Adding basic blocks to the mod is a simple affair, but requires modifying many files. In this guide we will walk you through the various changes you need to make to add a block to the mod.

## Prerequisites

There are three things that are best to have before you start any coding: a texture, class, and properties.

### Texture

Textures are the images that show up on the sides of a block. For the most basic of blocks, you will need one texture, which will be used for all sides of the block. Blocks with multiple textures will be covered in another tutorial.

The dimensions of a texture image are typically 32 pixels wide and 32 pixels high (32x32) for our project. Vanilla Minecraft blocks are all 16x16. All block textures must be square and should be one of the following dimensions:
- 16x16
- 32x32
- 64x64
- 256x256

For basic blocks, there should be absolutely no transparent or translucent pixels.

### Class

Every block is constructed from the `Block` class, or from a subclass of `Block`. These classes store the properties and define the behaviors of a block, everything from names to what happens when you right click on the block is specified in the class. For basic blocks you will not need to create your own class, but we will describe how to do that in the intermediate tutorial [Functional Blocks](Functional_Blocks.md). For now you will probably only need to use the main Block class, but we encourage you to browse through the existing Block classes to see what is available. Vanilla block classes can be found in `/mcp/src/minecraft/net/minecraft/block/` and custom mod block classes can be found in `/mcp/src/minecraft/com/elementfx/tvp/ad/block/`.

### Properties

There is a set of properties that all blocks have, at it is a good idea to think of what you want the values of these properties to be:

#### Creative Tab

What creative tab should the block show up in? Most basic blocks will go in either Building Blocks or Decorations. Your options are: `BUILDING_BLOCKS`, `DECORATIONS`, `REDSTONE`, `TRANSPORTATION`, `MISC`, `FOOD`, `TOOLS`, `COMBAT`, `BREWING`, and `MATERIALS`.

#### Hardness

Each block has a decimal number which indicates how hard a block is. The hardness then determines how long it takes to break the block by hand and with tools. See [this table](https://minecraft.gamepedia.com/Breaking#Blocks_by_hardness) for the hardness of vanilla Minecraft blocks. Most mod blocks have a hardness of 1.5, just because we haven't had the chance to figure out the best hardness for each block yet (see [Issue #13](https://github.com/TheValarProject/AwakenDreamsClient/issues/13)), but it would be best if you asked yourself what blocks it should have a similar hardness to, and then set the hardness accordingly.

#### Resistance

The resistance property is a decimal number which determines how resistant the block is to explosions (TNT, creepers, etc.). See [here](https://minecraft.gamepedia.com/Explosion#Blast_resistance) for a list of the resistance values of vanilla Minecraft blocks. Similar to the hardness property, most of the mod blocks have a default value, this time of 10.0, but you are encouraged to compare the block to those with well-thought-out values and select an appropriate resistance value.

#### Sound Type

Sound types determine the sound that a block makes when it is placed, broken, and hit and stepped on. While custom sounds can be created, for now you should stick to the predefined sounds, which are the following: `WOOD`, `GROUND`, `PLANT`, `STONE`, `METAL`, `GLASS`, `CLOTH`, `SAND`, `SNOW`, `LADDER`, `ANVIL`, and `SLIME`.

#### Material

The material of a block defines various properties of the block, such as what tools are effective against it, if it is flammable, and what color it is on a map. It is possible to create custom materials, but for now you should choose one of the existing materials that best matches the block. There are over 30 material types, which can be found at the top of the `mcp/src/net/minecraft/block/material/Material.java` file. The following materials are the ones best suited for basic blocks: `GRASS`, `GROUND`, `WOOD`, `ROCK`, `IRON`, `CLOTH`, and `SAND`. Don't worry about getting a material that exactly matches, for now the closest match is fine.

## Source Code Changes

Now we get to the actually programming required to add blocks. There are 4 .java files which you need to edit for the block to work properly, which we go over in the following sections. We will be identifying these files by their package, not by their path. This means you can find the files in Eclipse with the package explorer, or you can find them manually by going to `/mcp/src/` then following the package path.

### net.minecraft.block.Block.java

`Block.java` has two functions: it defines the default block class (see Prerequisites > Class), and it constructs and registers all of the block objects. Near the bottom of the file you will see hundreds of lines starting with `registerADBlock`. Right after the last one of these function calls, you will want to make your own line for your new block. Often when you are creating blocks, both basic and complex, you can refer to the other block registrations to get an idea of how to add different types of blocks. For a basic block you will want to use this template:
```java
registerADBlock(<id>, "<mixed_unlocalized_name>", (new Block(Material.<material>)).setHardness(<hardness>F).setResistance(<resistance>F).setSoundType(SoundType.<sound_type>).setCreativeTab(CreativeTabs.<creative_tab>));
```

Replace all of the parts in <> with your desired value.

`<id>` is an integer (positive whole number) that uniquely identifies the block and is used for saving the block as well as the /give in-game command. You should look at the IDs previously used by the mod, and choose the next available one.

The `<unlocalized_name>` is a name for the block which is used to identify it in other parts of the code. This is not the display name, that comes later. To figure out what to use for the unlocalized name, take the English display name and do the following:
1. Make all letters lowercase
2. Remove all accents
3. Replace all spaces with underscores ('\_')
4. Remove all characters that are not letters or numbers (note: the first character should not be a number, if it is consider renaming the block or spelling out the number)

So for example if I had a block called "Kíli's 1st Gilded Footstool", its unlocalized name would be "kilis_1st_gilded_footstool".

`<mixed_unlocalized_name>` is a special variant of `<unlocalized_name>` that is only used in `Block.java`. Instead of being completely lowercase, the first letter of every word (except for the first word) is capitalized. So continuing from our previous example, the mixed unlocalized name would be "kilis_1st_Gilded_Footstool".

For the options of the rest of the parts see Prerequisites > Properties.

### net.minecraft.init.Blocks.java

The `Blocks.java` file (do not confuse it with `Block.java`) provides a static reference for every block for convenience when programming. When looking at the file, you will notice two large sections, one where all the lines start with public static final Block (this is were the static block variables are declared), and another where the lines contain either `getRegisteredBlock` or `getRegisteredADBlock`. At the end of the first section add a new line add use the following template:
```java
public static final Block <static_name>;
```
Replacing `<static_name>` with an uppercase version of the block's unlocalized name.

Now go to the second part, and after the last entry add a new line with this template:
```java
<static_name> = getRegisteredADBlock("<unlocalized_name>");
```
And don't forget to replace the <> parts in the ways discussed before.

### net.minecraft.item.Item.java

`Item.java` is, as you might have guessed, the equivalent of `Block.java` for items. What you might not have guessed is that we also need to modify this for blocks. See, each block actually has a corresponding ItemBlock, which is a special type of Item that automatically gets its name from the block it represents, and places that block whenever a right click action occurs with it in hand. Registering an ItemBlock is super easy, just look for the last line that starts with `registerItemBlock` and add the following template in a new line right after it:
```java
registerItemBlock(Blocks.<static_name>);
```
Where `<static_name>` is the same as the one we used in `Blocks.java`. Now we are starting to see where the static variables in `Blocks.java` come in handy!

### net.minecraft.client.renderer.RenderItem.java

Our final change to the source code is in `RenderItem.java`. As the name suggests, this is the class helps render items, whether in hand, on the hotbar or in a container. We must modify this class so that it properly renders the ItemBlock that we added to `Item.java`. To do that, we look for the last line that starts with `this.registerADBlock` and then create a new line after it with this template:
```java
this.registerADBlock(Blocks.<static_name>, "<unlocalized_name>");
```

## Asset Changes

Assets are non-source files that are needed to run the game. For Minecraft, assets mostly consist of textures and JSON files. JSON is a human-readable data format commonly used in many applications today. You can find many guides for JSON online, but you can get the general idea of how it works just by looking at the templates we will provide.

Vanilla Minecraft assets can be found at `mcp/temp/src/minecraft/assets/`. All assets for the mod can be found in `mcp/src/minecraft/assets/awakendreams/` and all paths for this section will be relative to that directory.

### textures/blocks/

As we discussed in the Prerequisites, you need an image to be the texture for your block in-game. Simply place the texture you have into the folder and make sure that it is named with the format `<unlocalized_name>.png` where `<unlocalized_name>` is the same as the one from we came up with in `Block.java`.

### models/block/

Block models determine how a block will be rendered when placed in the world. It specifies the texture that will be used and the shape of the block. Some blocks like torches have custom shapes, these are achieved by creating custom block models. However there is a separate intermediate tutorial for [Custom Block Models](Intermediate_Tutorials/Custom_Block_Models.md). For our basic block, we just have to create a `<unlocalized_name>.json` file with the following template:
```json
{
  "parent": "block/cube_all",
  "textures": {
    "all": "awakendreams:blocks/<unlocalized_name>"
  }
}
```
This is basically saying that we should use the regular cube model, and that all sides of it should have the texture at `mcp/src/minecraft/assets/awakendreams/textures/blocks/<unlocalized_name>.png`.

### blockstates/

Blockstates allow you to specify different block models for different block variations (like wool, which varies in color). Block variations is beyond the scope of this tutorial, so the only thing you need to do is create a file called `<unlocalized_name>.json` using this template for the contents:
```json
{
  "variants": {
    "normal": {
      "model": "awakendreams:<unlocalized_name>"
    }
  }
}
```

### models/item/

Item models determine how an item will be rendered when held in your hand and in the GUI. In this case the model is for the ItemBlock. Almost all ItemBlocks the same format. Create a file called `<unlocalized_name>.json` with the following template:
```json
{
  "parent": "awakendreams:block/<unlocalized_name>"
}
```
This basically tells the item to render in the same way that the block model does, displaying a little isometric view of the block as the icon, and displaying a small 3d cube when held in your hand.

### lang/

Finally we have the language files. These are not JSON files but instead a very simple custom format where each lines follows this format:
```
<full_compact_name>=<localized_name>
```
`<compact_name>` is `<mixed_unlocalized_name>` except with the underscores removed. Continuing on from the example we used for explaining the unlocalized name, the compact name would be "kilis1stGildedFootstool". The `<full_compact_name>` is the same as the `<compact_name>` except it has extra data in front of it to identify what category the string belongs to. Block names have `tile.` prepended to them. The `<localized_name>` is the display name in-game for the language specified in the filename. `en_US.lang` is for English display names, `fr_FR.lang` for French, `da_DK.lang` for Danish, and `cs_CZ.lang` for Czech. The English file needs to be changed, and if you know any of the other languages, feel free to update those as well. For each language file you wish to add a display name for the block to, find the last line that starts with `tile.` and add a new line under it that follows this template:
```
tile.<compact_name>=<localized_name>
```
Where the `<localized_name>` is the display name in the proper language

## Conclusion

Now you should be able to start the client and see your block in the game! Test it out by finding it in the creative inventory and placing it in the world. Make sure it has the right texture on all sides, the icon renders properly, the proper name is displayed when hovered over, and test to see if you are satisfied with the properties you have assigned it (hardness and step sound mostly). Then pat yourself on the back and submit your changes to be added into the mod (see the [Submitting Changes](Basic_Tutorials/Submitting_Changes.md) tutorial).

<h6 align="center">← <a href="Basic_Items.md">Basic Items</a> | <a href="Submitting_Changes.md">Submitting Changes</a> →</h6>
