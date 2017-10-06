###### [Contributing Guidelines](../../CONTRIBUTING.md) > Basic Tutorials > Basic Items

# Basic Items

##### Prerequisite Tutorials: [Getting Started](Getting_Started.md)

Adding basic items is quite straightforward. In this tutorial we will guide you through each of the file that needs to be modified and what needs to be changed to add your item to the mod.

## Prerequisites

There are three things that are best to have before you start any coding: a texture, an item class, and a creative tab.

### Texture

An item texture is the icon you see in your hand and in your inventory. Almost all items just have that one texture.

The dimensions of a texture image are typically 32 pixels wide and 32 pixels high (32x32) for our project. Vanilla Minecraft item textures are all 16x16. All item textures must be square and should be one of the following dimensions:
- 16x16
- 32x32
- 64x64
- 256x256

There should be absolutely no translucent pixels. Transparency is allowed, but pixels that are not either fully opaque or fully transparent cause rendering issues.

### Class

Every item is constructed from the `Item` class, or from a subclass of `Item`. These classes store the properties and define the behaviors of the item, everything from how many items are allowed in a stack to what happens when you try to break a block with it. For basic items you will not need to create your own class, but more specialized items that perform functions (food, weapons, etc.) you will need to use custom classes. For now you only need to use the main Item class, but we encourage you to browse through the existing Item classes to see what is available. Vanilla item classes can be found in `/mcp/src/minecraft/net/minecraft/item/` and custom mod block classes can be found in `/mcp/src/minecraft/com/elementfx/tvp/ad/item/`.

### Creative Tab

What creative tab should the item show up in? Most basic items will go in Materials or Miscellaneous. Your options are: `BUILDING_BLOCKS`, `DECORATIONS`, `REDSTONE`, `TRANSPORTATION`, `MISC`, `FOOD`, `TOOLS`, `COMBAT`, `BREWING`, and `MATERIALS`.

## Source Code Changes

In order to add a basic item, 3 of the .java source files have to be modified. The files will be identified by their package, not by their path. This means you can find the files in Eclipse with the package explorer, or you can find them manually by going to `/mcp/src/` then following the package path.

### net.minecraft.item.Item.java

`Item.java` is the superclass for all Item classes (see Prerequisites > Class) and is responsible for constructing and registering all item objects. Near the bottom of the file you will see many lines beginning with `registerADItem`. Right after the last one of these function calls, you will want to make your own line for your new item. Looking at the previous lines can help you get an idea of how to create many different kinds of items. For a basic item you will want to use this template:
```java
registerADItem(<id>, "<mixed_unlocalized_name>", (new Item()).setCreativeTab(CreativeTabs.<creative_tab>));
```

`<id>` is an integer (positive whole number) that uniquely identifies the item and is used for saving the item as well as the /give in-game command. You should look at the IDs used by the mod in the previous lines, and choose the next available one.

`<unlocalized_name>` is a name for the item which is used to identify it in other parts of the code. This is not the display name, that comes later. To figure out what to use for the unlocalized name, take the English display name and do the following:
1. Make all letters lowercase
2. Remove all accents
3. Replace all spaces with underscores ('\_')
4. Remove all characters that are not letters or numbers (note: the first character should not be a number, if it is consider renaming the item or spelling out the number)

So for example if I had a item called "Royal Númenórean Blade 1", its unlocalized name would be "royal_numenorean_blade_1".

`<mixed_unlocalized_name>` is a special variant of `<unlocalized_name>` that is only used in `Item.java`. Instead of being completely lowercase, the first letter of every word (except for the first word) is capitalized. So continuing from our previous example, the mixed unlocalized name would be "royal_Numenorean_Blade_1".

`<creative_tab>` is simply the creative tab where you want the item to show up in. See Prerequisites > Creative Tab for options.

### net.minecraft.init.Items.java

The `Items.java` file (do not confuse it with `Item.java`) provides a static reference for every item for convenience when programming. When looking at the file, you will notice two large sections, one where all the lines start with public static final Item (this is were the static item variables are declared), and another where the lines contain either `getRegisteredItem` or `getRegisteredADItem`. At the end of the first section add a new line add use the following template:
```java
public static final Item <static_name>;
```
Replacing `<static_name>` with an uppercase version of the block's unlocalized name.

Now go to the second part, and after the last entry add a new line with this template:
```java
<static_name> = getRegisteredADItem("<unlocalized_name>");
```
And don't forget to replace the <> parts in the ways discussed before.

### net.minecraft.client.renderer.RenderItem.java

`RenderItem.java` registers all of the items so that they render properly in your hand and inventory. Find the last line that starts with `this.registerADItem` and add a new line under it and add the following template to it:
```java
this.registerADItem(Items.<static_name>, "<unlocalized_name>");
```
Replacing the parts in <> in the same way as before.

## Asset Changes

Assets are non-source files that are needed to run the game. For Minecraft, assets mostly consist of textures and JSON files. JSON is a human-readable data format commonly used in many applications today. You can find many guides for JSON online, but you can get the general idea of how it works just by looking at the templates we will provide.

Vanilla Minecraft assets can be found at `mcp/temp/src/minecraft/assets/`. All assets for the mod can be found in `mcp/src/minecraft/assets/awakendreams/` and all paths for this section will be relative to that directory.

### textures/items/

As we discussed in the Prerequisites, you need an image to be the texture for your item's icon. Simply place the texture you have into this folder and make sure that it is named with the format `<unlocalized_name>.png` where `<unlocalized_name>` is the same as the one from we came up with in `Item.java`.

### models/item/

Item models determine how an item will be rendered when held in your hand and in the GUI. In this case the model is for the ItemBlock. Almost all ItemBlocks the same format. Create a file called `<unlocalized_name>.json` with the following template:
```json
{
  "parent": "item/generated",
  "textures": {
    "layer0": "awakendreams:items/<unlocalized_name>"
  }
}
```
This basically tells Minecraft to generate a flat model based on the texture in layer0. You can also use item/generated to add multilayered item textures using layer1, layer2 etc., but you won't need that for a basic item.

### lang/

Finally we have the language files. These are not JSON files but instead a very simple custom format where each lines follows this format:
```
<full_compact_name>=<localized_name>
```
`<compact_name>` is `<mixed_unlocalized_name>` except with the underscores removed. Continuing on from the example we used for explaining the unlocalized name, the compact name would be "royalNumenoreanBlade1". The `<full_compact_name>` is the same as the `<compact_name>` except it has extra data in front of it to identify what category the string belongs to. Item names have `item.` prepended to them. The `<localized_name>` is the display name in-game for the language specified in the filename. `en_US.lang` is for English display names, `fr_FR.lang` for French, `da_DK.lang` for Danish, and `cs_CZ.lang` for Czech. The English file needs to be changed, and if you know any of the other languages, feel free to update those as well. For each language file you wish to add a display name for the block to, find the last line that starts with `item.` and add a new line under it that follows this template:
```
item.<compact_name>=<localized_name>
```
Where the `<localized_name>` is the display name in the proper language

## Conclusion

Now you should be able to start the client and see your item in the game! Test it out by finding it in the creative inventory and making sure that the display name is correct and that the icon renders properly. If everything is good, you are ready to submit your changes to be added into the mod (see the [Submitting Changes](Basic_Tutorials/Submitting_Changes.md) tutorial).

<h6 align="center">← <a href="Getting_Started.md">Getting Started</a> | <a href="Basic_Blocks.md">Basic Blocks</a> →</h6>
