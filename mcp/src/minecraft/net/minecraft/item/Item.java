package net.minecraft.item;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class Item
{
    public static final RegistryNamespaced<ResourceLocation, Item> REGISTRY = new RegistryNamespaced();
    private static final Map<Block, Item> BLOCK_TO_ITEM = Maps.<Block, Item>newHashMap();
    private static final IItemPropertyGetter DAMAGED_GETTER = new IItemPropertyGetter()
    {
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
        {
            return stack.isItemDamaged() ? 1.0F : 0.0F;
        }
    };
    private static final IItemPropertyGetter DAMAGE_GETTER = new IItemPropertyGetter()
    {
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
        {
            return MathHelper.clamp_float((float)stack.getItemDamage() / (float)stack.getMaxDamage(), 0.0F, 1.0F);
        }
    };
    private static final IItemPropertyGetter LEFTHANDED_GETTER = new IItemPropertyGetter()
    {
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
        {
            return entityIn != null && entityIn.getPrimaryHand() != EnumHandSide.RIGHT ? 1.0F : 0.0F;
        }
    };
    private static final IItemPropertyGetter COOLDOWN_GETTER = new IItemPropertyGetter()
    {
        public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
        {
            return entityIn instanceof EntityPlayer ? ((EntityPlayer)entityIn).getCooldownTracker().getCooldown(stack.getItem(), 0.0F) : 0.0F;
        }
    };
    private final IRegistry<ResourceLocation, IItemPropertyGetter> properties = new RegistrySimple();
    protected static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private CreativeTabs tabToDisplayOn;

    /** The RNG used by the Item subclasses. */
    protected static Random itemRand = new Random();

    /** Maximum size of the stack. */
    protected int maxStackSize = 64;

    /** Maximum damage an item can handle. */
    private int maxDamage;

    /** If true, render the object in full 3D, like weapons and tools. */
    protected boolean bFull3D;

    /**
     * Some items (like dyes) have multiple subtypes on same item, this is field define this behavior
     */
    protected boolean hasSubtypes;
    private Item containerItem;

    /** The unlocalized name of this item. */
    private String unlocalizedName;

    public static int getIdFromItem(Item itemIn)
    {
        return itemIn == null ? 0 : REGISTRY.getIDForObject(itemIn);
    }

    public static Item getItemById(int id)
    {
        return (Item)REGISTRY.getObjectById(id);
    }

    @Nullable
    public static Item getItemFromBlock(Block blockIn)
    {
        return (Item)BLOCK_TO_ITEM.get(blockIn);
    }

    /**
     * Tries to get an Item by it's name (e.g. minecraft:apple) or a String representation of a numerical ID. If both
     * fail, null is returned.
     */
    public static Item getByNameOrId(String id)
    {
        Item item = (Item)REGISTRY.getObject(new ResourceLocation(id));

        if (item == null)
        {
            try
            {
                return getItemById(Integer.parseInt(id));
            }
            catch (NumberFormatException var3)
            {
                ;
            }
        }

        return item;
    }

    /**
     * Creates a new override param for item models. See usage in clock, compass, elytra, etc.
     */
    public final void addPropertyOverride(ResourceLocation key, IItemPropertyGetter getter)
    {
        this.properties.putObject(key, getter);
    }

    @Nullable
    public IItemPropertyGetter getPropertyGetter(ResourceLocation key)
    {
        return (IItemPropertyGetter)this.properties.getObject(key);
    }

    public boolean hasCustomProperties()
    {
        return !this.properties.getKeys().isEmpty();
    }

    /**
     * Called when an ItemStack with NBT data is read to potentially that ItemStack's NBT data
     */
    public boolean updateItemStackNBT(NBTTagCompound nbt)
    {
        return false;
    }

    public Item()
    {
        this.addPropertyOverride(new ResourceLocation("lefthanded"), LEFTHANDED_GETTER);
        this.addPropertyOverride(new ResourceLocation("cooldown"), COOLDOWN_GETTER);
    }

    public Item setMaxStackSize(int maxStackSize)
    {
        this.maxStackSize = maxStackSize;
        return this;
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return EnumActionResult.PASS;
    }

    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        return 1.0F;
    }

    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        return new ActionResult(EnumActionResult.PASS, itemStackIn);
    }

    @Nullable

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
    {
        return stack;
    }

    /**
     * Returns the maximum size of the stack for a specific item. *Isn't this more a Set than a Get?*
     */
    public int getItemStackLimit()
    {
        return this.maxStackSize;
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    public int getMetadata(int damage)
    {
        return 0;
    }

    public boolean getHasSubtypes()
    {
        return this.hasSubtypes;
    }

    protected Item setHasSubtypes(boolean hasSubtypes)
    {
        this.hasSubtypes = hasSubtypes;
        return this;
    }

    /**
     * Returns the maximum damage an item can take.
     */
    public int getMaxDamage()
    {
        return this.maxDamage;
    }

    /**
     * set max damage of an Item
     */
    protected Item setMaxDamage(int maxDamageIn)
    {
        this.maxDamage = maxDamageIn;

        if (maxDamageIn > 0)
        {
            this.addPropertyOverride(new ResourceLocation("damaged"), DAMAGED_GETTER);
            this.addPropertyOverride(new ResourceLocation("damage"), DAMAGE_GETTER);
        }

        return this;
    }

    public boolean isDamageable()
    {
        return this.maxDamage > 0 && (!this.hasSubtypes || this.maxStackSize == 1);
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        return false;
    }

    /**
     * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
     */
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        return false;
    }

    /**
     * Check whether this Item can harvest the given Block
     */
    public boolean canHarvestBlock(IBlockState blockIn)
    {
        return false;
    }

    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        return false;
    }

    /**
     * Sets bFull3D to True and return the object.
     */
    public Item setFull3D()
    {
        this.bFull3D = true;
        return this;
    }

    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    public boolean isFull3D()
    {
        return this.bFull3D;
    }

    /**
     * Returns true if this item should be rotated by 180 degrees around the Y axis when being held in an entities
     * hands.
     */
    public boolean shouldRotateAroundWhenRendering()
    {
        return false;
    }

    /**
     * Sets the unlocalized name of this item to the string passed as the parameter, prefixed by "item."
     */
    public Item setUnlocalizedName(String unlocalizedName)
    {
        this.unlocalizedName = unlocalizedName;
        return this;
    }

    /**
     * Translates the unlocalized name of this item, but without the .name suffix, so the translation fails and the
     * unlocalized name itself is returned.
     */
    public String getUnlocalizedNameInefficiently(ItemStack stack)
    {
        String s = this.getUnlocalizedName(stack);
        return s == null ? "" : I18n.translateToLocal(s);
    }

    /**
     * Returns the unlocalized name of this item.
     */
    public String getUnlocalizedName()
    {
        return "item." + this.unlocalizedName;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item." + this.unlocalizedName;
    }

    public Item setContainerItem(Item containerItem)
    {
        this.containerItem = containerItem;
        return this;
    }

    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean getShareTag()
    {
        return true;
    }

    public Item getContainerItem()
    {
        return this.containerItem;
    }

    /**
     * True if this Item has a container item (a.k.a. crafting result)
     */
    public boolean hasContainerItem()
    {
        return this.containerItem != null;
    }

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
    }

    /**
     * Called when item is crafted/smelted. Used only by maps so far.
     */
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn)
    {
    }

    /**
     * false for all Items except sub-classes of ItemMapBase
     */
    public boolean isMap()
    {
        return false;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.NONE;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 0;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
    {
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
    }

    public String getItemStackDisplayName(ItemStack stack)
    {
        return ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
    }

    public boolean hasEffect(ItemStack stack)
    {
        return stack.isItemEnchanted();
    }

    /**
     * Return an item rarity from EnumRarity
     */
    public EnumRarity getRarity(ItemStack stack)
    {
        return stack.isItemEnchanted() ? EnumRarity.RARE : EnumRarity.COMMON;
    }

    /**
     * Checks isDamagable and if it cannot be stacked
     */
    public boolean isItemTool(ItemStack stack)
    {
        return this.getItemStackLimit() == 1 && this.isDamageable();
    }

    protected RayTraceResult rayTrace(World worldIn, EntityPlayer playerIn, boolean useLiquids)
    {
        float f = playerIn.rotationPitch;
        float f1 = playerIn.rotationYaw;
        double d0 = playerIn.posX;
        double d1 = playerIn.posY + (double)playerIn.getEyeHeight();
        double d2 = playerIn.posZ;
        Vec3d vec3d = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 5.0D;
        Vec3d vec3d1 = vec3d.addVector((double)f6 * 5.0D, (double)f5 * 5.0D, (double)f7 * 5.0D);
        return worldIn.rayTraceBlocks(vec3d, vec3d1, useLiquids, !useLiquids, false);
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return 0;
    }

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        subItems.add(new ItemStack(itemIn));
    }

    /**
     * gets the CreativeTab this item is displayed on
     */
    public CreativeTabs getCreativeTab()
    {
        return this.tabToDisplayOn;
    }

    /**
     * returns this;
     */
    public Item setCreativeTab(CreativeTabs tab)
    {
        this.tabToDisplayOn = tab;
        return this;
    }

    /**
     * Returns true if players can use this item to affect the world (e.g. placing blocks, placing ender eyes in portal)
     * when not in creative
     */
    public boolean canItemEditBlocks()
    {
        return false;
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        return false;
    }

    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot)
    {
        return HashMultimap.<String, AttributeModifier>create();
    }

    public static void registerItems()
    {
        registerItemBlock(Blocks.STONE, (new ItemMultiTexture(Blocks.STONE, Blocks.STONE, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockStone.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("stone"));
        registerItemBlock(Blocks.GRASS, new ItemColored(Blocks.GRASS, false));
        registerItemBlock(Blocks.DIRT, (new ItemMultiTexture(Blocks.DIRT, Blocks.DIRT, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockDirt.DirtType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("dirt"));
        registerItemBlock(Blocks.COBBLESTONE);
        registerItemBlock(Blocks.PLANKS, (new ItemMultiTexture(Blocks.PLANKS, Blocks.PLANKS, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("wood"));
        registerItemBlock(Blocks.SAPLING, (new ItemMultiTexture(Blocks.SAPLING, Blocks.SAPLING, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("sapling"));
        registerItemBlock(Blocks.BEDROCK);
        registerItemBlock(Blocks.SAND, (new ItemMultiTexture(Blocks.SAND, Blocks.SAND, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockSand.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("sand"));
        registerItemBlock(Blocks.GRAVEL);
        registerItemBlock(Blocks.GOLD_ORE);
        registerItemBlock(Blocks.IRON_ORE);
        registerItemBlock(Blocks.COAL_ORE);
        registerItemBlock(Blocks.LOG, (new ItemMultiTexture(Blocks.LOG, Blocks.LOG, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("log"));
        registerItemBlock(Blocks.LOG2, (new ItemMultiTexture(Blocks.LOG2, Blocks.LOG2, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockPlanks.EnumType.byMetadata(p_apply_1_.getMetadata() + 4).getUnlocalizedName();
            }
        })).setUnlocalizedName("log"));
        registerItemBlock(Blocks.LEAVES, (new ItemLeaves(Blocks.LEAVES)).setUnlocalizedName("leaves"));
        registerItemBlock(Blocks.LEAVES2, (new ItemLeaves(Blocks.LEAVES2)).setUnlocalizedName("leaves"));
        registerItemBlock(Blocks.SPONGE, (new ItemMultiTexture(Blocks.SPONGE, Blocks.SPONGE, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return (p_apply_1_.getMetadata() & 1) == 1 ? "wet" : "dry";
            }
        })).setUnlocalizedName("sponge"));
        registerItemBlock(Blocks.GLASS);
        registerItemBlock(Blocks.LAPIS_ORE);
        registerItemBlock(Blocks.LAPIS_BLOCK);
        registerItemBlock(Blocks.DISPENSER);
        registerItemBlock(Blocks.SANDSTONE, (new ItemMultiTexture(Blocks.SANDSTONE, Blocks.SANDSTONE, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockSandStone.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("sandStone"));
        registerItemBlock(Blocks.NOTEBLOCK);
        registerItemBlock(Blocks.GOLDEN_RAIL);
        registerItemBlock(Blocks.DETECTOR_RAIL);
        registerItemBlock(Blocks.STICKY_PISTON, new ItemPiston(Blocks.STICKY_PISTON));
        registerItemBlock(Blocks.WEB);
        registerItemBlock(Blocks.TALLGRASS, (new ItemColored(Blocks.TALLGRASS, true)).setSubtypeNames(new String[] {"shrub", "grass", "fern"}));
        registerItemBlock(Blocks.DEADBUSH);
        registerItemBlock(Blocks.PISTON, new ItemPiston(Blocks.PISTON));
        registerItemBlock(Blocks.WOOL, (new ItemCloth(Blocks.WOOL)).setUnlocalizedName("cloth"));
        registerItemBlock(Blocks.YELLOW_FLOWER, (new ItemMultiTexture(Blocks.YELLOW_FLOWER, Blocks.YELLOW_FLOWER, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.YELLOW, p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("flower"));
        registerItemBlock(Blocks.RED_FLOWER, (new ItemMultiTexture(Blocks.RED_FLOWER, Blocks.RED_FLOWER, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockFlower.EnumFlowerType.getType(BlockFlower.EnumFlowerColor.RED, p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("rose"));
        registerItemBlock(Blocks.BROWN_MUSHROOM);
        registerItemBlock(Blocks.RED_MUSHROOM);
        registerItemBlock(Blocks.GOLD_BLOCK);
        registerItemBlock(Blocks.IRON_BLOCK);
        registerItemBlock(Blocks.STONE_SLAB, (new ItemSlab(Blocks.STONE_SLAB, Blocks.STONE_SLAB, Blocks.DOUBLE_STONE_SLAB)).setUnlocalizedName("stoneSlab"));
        registerItemBlock(Blocks.BRICK_BLOCK);
        registerItemBlock(Blocks.TNT);
        registerItemBlock(Blocks.BOOKSHELF);
        registerItemBlock(Blocks.MOSSY_COBBLESTONE);
        registerItemBlock(Blocks.OBSIDIAN);
        registerItemBlock(Blocks.TORCH);
        registerItemBlock(Blocks.END_ROD);
        registerItemBlock(Blocks.CHORUS_PLANT);
        registerItemBlock(Blocks.CHORUS_FLOWER);
        registerItemBlock(Blocks.PURPUR_BLOCK);
        registerItemBlock(Blocks.PURPUR_PILLAR);
        registerItemBlock(Blocks.PURPUR_STAIRS);
        registerItemBlock(Blocks.PURPUR_SLAB, (new ItemSlab(Blocks.PURPUR_SLAB, Blocks.PURPUR_SLAB, Blocks.PURPUR_DOUBLE_SLAB)).setUnlocalizedName("purpurSlab"));
        registerItemBlock(Blocks.MOB_SPAWNER);
        registerItemBlock(Blocks.OAK_STAIRS);
        registerItemBlock(Blocks.CHEST);
        registerItemBlock(Blocks.DIAMOND_ORE);
        registerItemBlock(Blocks.DIAMOND_BLOCK);
        registerItemBlock(Blocks.CRAFTING_TABLE);
        registerItemBlock(Blocks.FARMLAND);
        registerItemBlock(Blocks.FURNACE);
        registerItemBlock(Blocks.LADDER);
        registerItemBlock(Blocks.RAIL);
        registerItemBlock(Blocks.STONE_STAIRS);
        registerItemBlock(Blocks.LEVER);
        registerItemBlock(Blocks.STONE_PRESSURE_PLATE);
        registerItemBlock(Blocks.WOODEN_PRESSURE_PLATE);
        registerItemBlock(Blocks.REDSTONE_ORE);
        registerItemBlock(Blocks.REDSTONE_TORCH);
        registerItemBlock(Blocks.STONE_BUTTON);
        registerItemBlock(Blocks.SNOW_LAYER, new ItemSnow(Blocks.SNOW_LAYER));
        registerItemBlock(Blocks.ICE);
        registerItemBlock(Blocks.SNOW);
        registerItemBlock(Blocks.CACTUS);
        registerItemBlock(Blocks.CLAY);
        registerItemBlock(Blocks.JUKEBOX);
        registerItemBlock(Blocks.OAK_FENCE);
        registerItemBlock(Blocks.SPRUCE_FENCE);
        registerItemBlock(Blocks.BIRCH_FENCE);
        registerItemBlock(Blocks.JUNGLE_FENCE);
        registerItemBlock(Blocks.DARK_OAK_FENCE);
        registerItemBlock(Blocks.ACACIA_FENCE);
        registerItemBlock(Blocks.PUMPKIN);
        registerItemBlock(Blocks.NETHERRACK);
        registerItemBlock(Blocks.SOUL_SAND);
        registerItemBlock(Blocks.GLOWSTONE);
        registerItemBlock(Blocks.LIT_PUMPKIN);
        registerItemBlock(Blocks.TRAPDOOR);
        registerItemBlock(Blocks.MONSTER_EGG, (new ItemMultiTexture(Blocks.MONSTER_EGG, Blocks.MONSTER_EGG, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockSilverfish.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("monsterStoneEgg"));
        registerItemBlock(Blocks.STONEBRICK, (new ItemMultiTexture(Blocks.STONEBRICK, Blocks.STONEBRICK, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockStoneBrick.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("stonebricksmooth"));
        registerItemBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        registerItemBlock(Blocks.RED_MUSHROOM_BLOCK);
        registerItemBlock(Blocks.IRON_BARS);
        registerItemBlock(Blocks.GLASS_PANE);
        registerItemBlock(Blocks.MELON_BLOCK);
        registerItemBlock(Blocks.VINE, new ItemColored(Blocks.VINE, false));
        registerItemBlock(Blocks.OAK_FENCE_GATE);
        registerItemBlock(Blocks.SPRUCE_FENCE_GATE);
        registerItemBlock(Blocks.BIRCH_FENCE_GATE);
        registerItemBlock(Blocks.JUNGLE_FENCE_GATE);
        registerItemBlock(Blocks.DARK_OAK_FENCE_GATE);
        registerItemBlock(Blocks.ACACIA_FENCE_GATE);
        registerItemBlock(Blocks.BRICK_STAIRS);
        registerItemBlock(Blocks.STONE_BRICK_STAIRS);
        registerItemBlock(Blocks.MYCELIUM);
        registerItemBlock(Blocks.WATERLILY, new ItemLilyPad(Blocks.WATERLILY));
        registerItemBlock(Blocks.NETHER_BRICK);
        registerItemBlock(Blocks.NETHER_BRICK_FENCE);
        registerItemBlock(Blocks.NETHER_BRICK_STAIRS);
        registerItemBlock(Blocks.ENCHANTING_TABLE);
        registerItemBlock(Blocks.END_PORTAL_FRAME);
        registerItemBlock(Blocks.END_STONE);
        registerItemBlock(Blocks.END_BRICKS);
        registerItemBlock(Blocks.DRAGON_EGG);
        registerItemBlock(Blocks.REDSTONE_LAMP);
        registerItemBlock(Blocks.WOODEN_SLAB, (new ItemSlab(Blocks.WOODEN_SLAB, Blocks.WOODEN_SLAB, Blocks.DOUBLE_WOODEN_SLAB)).setUnlocalizedName("woodSlab"));
        registerItemBlock(Blocks.SANDSTONE_STAIRS);
        registerItemBlock(Blocks.EMERALD_ORE);
        registerItemBlock(Blocks.ENDER_CHEST);
        registerItemBlock(Blocks.TRIPWIRE_HOOK);
        registerItemBlock(Blocks.EMERALD_BLOCK);
        registerItemBlock(Blocks.SPRUCE_STAIRS);
        registerItemBlock(Blocks.BIRCH_STAIRS);
        registerItemBlock(Blocks.JUNGLE_STAIRS);
        registerItemBlock(Blocks.COMMAND_BLOCK);
        registerItemBlock(Blocks.BEACON);
        registerItemBlock(Blocks.COBBLESTONE_WALL, (new ItemMultiTexture(Blocks.COBBLESTONE_WALL, Blocks.COBBLESTONE_WALL, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockWall.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("cobbleWall"));
        registerItemBlock(Blocks.WOODEN_BUTTON);
        registerItemBlock(Blocks.ANVIL, (new ItemAnvilBlock(Blocks.ANVIL)).setUnlocalizedName("anvil"));
        registerItemBlock(Blocks.TRAPPED_CHEST);
        registerItemBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        registerItemBlock(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        registerItemBlock(Blocks.DAYLIGHT_DETECTOR);
        registerItemBlock(Blocks.REDSTONE_BLOCK);
        registerItemBlock(Blocks.QUARTZ_ORE);
        registerItemBlock(Blocks.HOPPER);
        registerItemBlock(Blocks.QUARTZ_BLOCK, (new ItemMultiTexture(Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BLOCK, new String[] {"default", "chiseled", "lines"})).setUnlocalizedName("quartzBlock"));
        registerItemBlock(Blocks.QUARTZ_STAIRS);
        registerItemBlock(Blocks.ACTIVATOR_RAIL);
        registerItemBlock(Blocks.DROPPER);
        registerItemBlock(Blocks.STAINED_HARDENED_CLAY, (new ItemCloth(Blocks.STAINED_HARDENED_CLAY)).setUnlocalizedName("clayHardenedStained"));
        registerItemBlock(Blocks.BARRIER);
        registerItemBlock(Blocks.IRON_TRAPDOOR);
        registerItemBlock(Blocks.HAY_BLOCK);
        registerItemBlock(Blocks.CARPET, (new ItemCloth(Blocks.CARPET)).setUnlocalizedName("woolCarpet"));
        registerItemBlock(Blocks.HARDENED_CLAY);
        registerItemBlock(Blocks.COAL_BLOCK);
        registerItemBlock(Blocks.PACKED_ICE);
        registerItemBlock(Blocks.ACACIA_STAIRS);
        registerItemBlock(Blocks.DARK_OAK_STAIRS);
        registerItemBlock(Blocks.SLIME_BLOCK);
        registerItemBlock(Blocks.GRASS_PATH);
        registerItemBlock(Blocks.DOUBLE_PLANT, (new ItemMultiTexture(Blocks.DOUBLE_PLANT, Blocks.DOUBLE_PLANT, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockDoublePlant.EnumPlantType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("doublePlant"));
        registerItemBlock(Blocks.STAINED_GLASS, (new ItemCloth(Blocks.STAINED_GLASS)).setUnlocalizedName("stainedGlass"));
        registerItemBlock(Blocks.STAINED_GLASS_PANE, (new ItemCloth(Blocks.STAINED_GLASS_PANE)).setUnlocalizedName("stainedGlassPane"));
        registerItemBlock(Blocks.PRISMARINE, (new ItemMultiTexture(Blocks.PRISMARINE, Blocks.PRISMARINE, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockPrismarine.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("prismarine"));
        registerItemBlock(Blocks.SEA_LANTERN);
        registerItemBlock(Blocks.RED_SANDSTONE, (new ItemMultiTexture(Blocks.RED_SANDSTONE, Blocks.RED_SANDSTONE, new Function<ItemStack, String>()
        {
            @Nullable
            public String apply(@Nullable ItemStack p_apply_1_)
            {
                return BlockRedSandstone.EnumType.byMetadata(p_apply_1_.getMetadata()).getUnlocalizedName();
            }
        })).setUnlocalizedName("redSandStone"));
        registerItemBlock(Blocks.RED_SANDSTONE_STAIRS);
        registerItemBlock(Blocks.STONE_SLAB2, (new ItemSlab(Blocks.STONE_SLAB2, Blocks.STONE_SLAB2, Blocks.DOUBLE_STONE_SLAB2)).setUnlocalizedName("stoneSlab2"));
        registerItemBlock(Blocks.REPEATING_COMMAND_BLOCK);
        registerItemBlock(Blocks.CHAIN_COMMAND_BLOCK);
        registerItemBlock(Blocks.field_189877_df);
        registerItemBlock(Blocks.field_189878_dg);
        registerItemBlock(Blocks.field_189879_dh);
        registerItemBlock(Blocks.field_189880_di);
        registerItemBlock(Blocks.field_189881_dj);
        registerItemBlock(Blocks.STRUCTURE_BLOCK);
        // Begin Awaken Dreams code
        registerItemBlock(Blocks.JADE_ORE);
        registerItemBlock(Blocks.AMBER_ORE);
        registerItemBlock(Blocks.TANZANITE_ORE);
        registerItemBlock(Blocks.PERMANENT_DIRT);
        registerItemBlock(Blocks.LUINDOL);
        registerItemBlock(Blocks.AMETHYST_ORE);
        registerItemBlock(Blocks.RUBY_ORE);
        registerItemBlock(Blocks.ONYX_ORE);
        registerItemBlock(Blocks.MOONSTONE_ORE);
        registerItemBlock(Blocks.MINAS_MORGUL_GLOWSTONE);
        registerItemBlock(Blocks.ARGONATH_STONE);
        registerItemBlock(Blocks.MITHRIL_BLOCK);
        registerItemBlock(Blocks.CRYSTAL_ORE);
        registerItemBlock(Blocks.DESERT_ROAD_BLOCK);
        registerItemBlock(Blocks.DIRT_ROAD_BLOCK);
        registerItemBlock(Blocks.LIGHT_BLUE_GLOWSTONE);
        registerItemBlock(Blocks.BLACK_IRON);
        registerItemBlock(Blocks.MORIA_PILLAR_STONE);
        registerItemBlock(Blocks.RUSTY_IRON);
        registerItemBlock(Blocks.ELF_FLOOR);
        registerItemBlock(Blocks.GONDORIAN_FLOOR);
        registerItemBlock(Blocks.STONE_FLOOR);
        registerItemBlock(Blocks.MORIA_TRAPDOOR);
        registerItemBlock(Blocks.PALM_LOG);
        registerItemBlock(Blocks.SILK_STONE);
        registerItemBlock(Blocks.CRACKED_SILK_STONE);
        registerItemBlock(Blocks.STRAW);
        registerItemBlock(Blocks.CRACKED_EARTH);
        registerItemBlock(Blocks.BAG_END_FLOOR);
        registerItemBlock(Blocks.BAG_END_WALL);
        registerItemBlock(Blocks.BROWN_STONE);
        registerItemBlock(Blocks.MORDOR_STONE);
        registerItemBlock(Blocks.CLIFF_BLOCK);
        registerItemBlock(Blocks.SPIDER_EGG);
        registerItemBlock(Blocks.LAMP);
        registerItemBlock(Blocks.GONDORIAN_STONE);
        registerItemBlock(Blocks.GONDORIAN_BRICK_STONE);
        registerItemBlock(Blocks.CRACKED_GONDORIAN_BRICK_STONE);
        registerItemBlock(Blocks.LORIEN_LAMP);
        registerItemBlock(Blocks.BUCKLEBURY_LAMP);
        registerItemBlock(Blocks.SALT_ORE);
        registerItemBlock(Blocks.SHIRE_FLOWER);
        registerItemBlock(Blocks.DARK_METAL);
        registerItemBlock(Blocks.RIVENDELL_WOOD);
        registerItemBlock(Blocks.MOSS);
        registerItemBlock(Blocks.GONDORIAN_ROOF);
        registerItemBlock(Blocks.RIVENDELL_ROOF);
        registerItemBlock(Blocks.MOSSY_GONDORIAN_BRICK_STONE);
        registerItemBlock(Blocks.ROHAN_BRICKS);
        registerItemBlock(Blocks.MORDOR_BRICK_STONE);
        registerItemBlock(Blocks.MITHRIL_ORE);
        registerItemBlock(Blocks.NUMENOREAN);
        registerItemBlock(Blocks.DWARF_INNER_WALL_DECORATION);
        registerItemBlock(Blocks.DALE_STONE);
        registerItemBlock(Blocks.DWARF_INNER_WALL_STONE);
        registerItemBlock(Blocks.RED_LAPIS);
        registerItemBlock(Blocks.PURPLE_LAPIS);
        registerItemBlock(Blocks.LIGHT_BLUE_LAPIS);
        registerItemBlock(Blocks.GREEN_LAPIS);
        registerItemBlock(Blocks.DWARF_WALL);
        registerItemBlock(Blocks.BROWN_LAPIS);
        registerItemBlock(Blocks.DWARF_FLOOR);
        registerItemBlock(Blocks.DWARVEN_HALL_FLOOR);
        registerItemBlock(Blocks.DWARF_STONE);
        registerItemBlock(Blocks.DWARVEN_TORCH);
        registerItemBlock(Blocks.DWARVEN_GOLD);
        registerItemBlock(Blocks.ROHAN_IRON);
        registerItemBlock(Blocks.DARK_DWARF_STONE);
        registerItemBlock(Blocks.MEDIUM_DARK_DWARF_STONE);
        registerItemBlock(Blocks.LIGHT_BROWN_WOOD);
        registerItemBlock(Blocks.OLD_TREE);
        registerItemBlock(Blocks.BREE_BOOKSHELF);
        registerItemBlock(Blocks.CROSS_HAY);
        registerItemBlock(Blocks.LIGHT_GREY_CIRCLE_STONE);
        registerItemBlock(Blocks.HOBBIT_LAMP_1);
        registerItemBlock(Blocks.HOBBIT_LAMP_2);
        registerItemBlock(Blocks.LOSSARNARCH_DECORATION_STONE);
        registerItemBlock(Blocks.BREE_LAMP);
        registerItemBlock(Blocks.ARCHET_LAMP);
        registerItemBlock(Blocks.TOWN_MARKER);
        registerItemBlock(Blocks.VILLAGE_MARKER);
        registerItemBlock(Blocks.RUIN_MARKER);
        registerItemBlock(Blocks.ELVEN_STONE_FLOOR);
        registerItemBlock(Blocks.ANCIENT_STONE);
        registerItemBlock(Blocks.BREE_STONE_BRICKS);
        registerItemBlock(Blocks.CRACKED_BREE_STONE_BRICKS);
        registerItemBlock(Blocks.MOSSY_BREE_STONE_BRICKS);
        registerItemBlock(Blocks.SHIRE_HAY);
        registerItemBlock(Blocks.BREE_OAK_PLANKS);
        registerItemBlock(Blocks.BREE_SPRUCE_PLANKS);
        registerItemBlock(Blocks.BREE_BIRCH_PLANKS);
        registerItemBlock(Blocks.BREE_JUNGLE_PLANKS);
        registerItemBlock(Blocks.SHIRE_PATH);
        registerItemBlock(Blocks.BREE_FLOOR);
        registerItemBlock(Blocks.ARNOR_FLOOR);
        registerItemBlock(Blocks.CARDOLAN_BRICK_STONE);
        registerItemBlock(Blocks.ELVEN_SANDSTONE_FLOOR);
        registerItemBlock(Blocks.DEAD_LAVA);
        registerItemBlock(Blocks.CHISELED_GONDORIAN_STONE);
        registerItemBlock(Blocks.NEEDLES);
        registerItemBlock(Blocks.RHUN_FLOOR);
        registerItemBlock(Blocks.KHAND_FLOOR);
        registerItemBlock(Blocks.CITY_MARKER);
        registerItemBlock(Blocks.RIVENDELL_FLOOR);
        registerItemBlock(Blocks.COLUMN);
        registerItemBlock(Blocks.COLUMN_TOP);
        registerItemBlock(Blocks.DWARF_BRICKS);
        registerItemBlock(Blocks.DWARF_FLOOR2);
        registerItemBlock(Blocks.DWARF_FLOOR3);
        registerItemBlock(Blocks.DWARF_FLOOR4);
        registerItemBlock(Blocks.DWARF_FLOOR5);
        registerItemBlock(Blocks.DWARF_FLOOR6);
        registerItemBlock(Blocks.DWARF_FLOOR7);
        registerItemBlock(Blocks.DWARF_FLOOR8);
        registerItemBlock(Blocks.DWARF_FLOOR9);
        registerItemBlock(Blocks.DWARF_KING_STONE);
        registerItemBlock(Blocks.DWARVEN_KING_FLOOR);
        registerItemBlock(Blocks.DWARVEN_KING_FLOOR2);
        registerItemBlock(Blocks.DWARVEN_PILLAR_DECORATION);
        registerItemBlock(Blocks.DWARVEN_STEEL);
        registerItemBlock(Blocks.EREBOR_FLOOR);
        registerItemBlock(Blocks.EREBOR_FLOOR2);
        registerItemBlock(Blocks.ERED_LUIN_STONE);
        registerItemBlock(Blocks.IRON_HILLS_FLOOR);
        registerItemBlock(Blocks.SMOOTH_GOLD);
        registerItemBlock(Blocks.RIVENDELL_WALL);
        registerItemBlock(Blocks.SINDAR_STONE);
        registerItemBlock(Blocks.SINDAR_FLOOR);
        registerItemBlock(Blocks.SINDAR_FLOOR2);
        registerItemBlock(Blocks.SINDAR_DECORATION_STONE);
        registerItemBlock(Blocks.NOLDOR_FLOOR);
        registerItemBlock(Blocks.NOLDOR_SANDSTONE_FLOOR);
        registerItemBlock(Blocks.MALLORN_WOODPLANKS);
        registerItemBlock(Blocks.LINDON_WOOD);
        registerItemBlock(Blocks.LINDON_WALL_DECORATION);
        registerItemBlock(Blocks.HIGH_ELF_WALL);
        registerItemBlock(Blocks.HARLINDON_WOOD);
        registerItemBlock(Blocks.FORLOND_FLOOR);
        registerItemBlock(Blocks.FORLINDON_WOOD);
        registerItemBlock(Blocks.FORLINDON_WALL);
        registerItemBlock(Blocks.ELVEN_STONE_WALL);
        registerItemBlock(Blocks.ELVEN_NOBLE_WALL);
        registerItemBlock(Blocks.RIVENDELL_STATUE_BOTTOM);
        registerItemBlock(Blocks.ELVEN_DECORATION);
        registerItemBlock(Blocks.ELVEN_DECORATION_LIGHT);
        registerItemBlock(Blocks.ELVEN_DECORATION2);
        registerItemBlock(Blocks.ELVEN_DECORATION3);
        registerItemBlock(Blocks.HOBBIT_FLOOR);
        registerItemBlock(Blocks.HOBBIT_FLOOR2);
        registerItemBlock(Blocks.DARK_BRICKS);
        registerItemBlock(Blocks.ROHIRRIM_CROSSBEAM);
        registerItemBlock(Blocks.CROSSBEAM);
        registerItemBlock(Blocks.CROSSBEAM2);
        registerItemBlock(Blocks.CROSSBEAM3);
        registerItemBlock(Blocks.STANDARD_CROSSBEAM);
        registerItemBlock(Blocks.STANDARD_CROSSBEAM2);
        registerItemBlock(Blocks.STANDARD_CROSSBEAM3);
        registerItemBlock(Blocks.VERTICAL_BEAM);
        registerItemBlock(Blocks.VERTICAL_HORIZONTAL_BEAM);
        registerItemBlock(Blocks.DALE_SANDSTONE);
        registerItemBlock(Blocks.DALE_TILES);
        registerItemBlock(Blocks.DALE_WALL);
        registerItemBlock(Blocks.HARAD_SANDSTONE_FLOOR);
        registerItemBlock(Blocks.HARAD_STONE_BRICKS);
        registerItemBlock(Blocks.SANDFLOOR);
        registerItemBlock(Blocks.UMBAR_HAVEN_FLOOR);
        registerItemBlock(Blocks.UMBAR_STONE_BRICKS);
        registerItemBlock(Blocks.UMBAR_WALL_DECORATION);
        registerItemBlock(Blocks.KHAND_STONE_BRICKS);
        registerItemBlock(Blocks.BROWN_BRICKS);
        registerItemBlock(Blocks.EDORAS_COBBLESTONE);
        registerItemBlock(Blocks.REINFORCED_WOOD);
        registerItemBlock(Blocks.MEDUSELD_WOOD);
        registerItemBlock(Blocks.ROHIRRIM_WALL_DECORATION);
        registerItemBlock(Blocks.STATUE_HEAD);
        registerItemBlock(Blocks.ANGMAR_FLOOR);
        registerItemBlock(Blocks.ANGMAR_BRICKS);
        registerItemBlock(Blocks.BEORNING_WOOD);
        registerItemBlock(Blocks.ANNUMINAS_DECORATION);
        registerItemBlock(Blocks.ARNOR_DECORATION_JEWEL);
        registerItemBlock(Blocks.ARNORIAN_BRICKS);
        registerItemBlock(Blocks.MINAS_TIRITH_FLOOR);
        registerItemBlock(Blocks.PELARGIR_STONE);
        registerItemBlock(Blocks.WHITE_COBBLESTONE);
        registerItemBlock(Blocks.DUNLAND_TOTEM);
        registerItemBlock(Blocks.DUNLAND_WALL_DECORATION);
        registerItemBlock(Blocks.MORDOR_LAMP);
        registerItemBlock(Blocks.HARAD_LIGHT);
        registerItemBlock(Blocks.ELVEN_LAMP);
        registerItemBlock(Blocks.CARN_DUM_LAMP);
        registerItemBlock(Blocks.BREE_TILE);
        registerItemBlock(Blocks.HUMAN_TRAPDOOR);
        registerItemBlock(Blocks.MORDOR_TRAPDOOR);
        registerItemBlock(Blocks.KHANDISH_TRAPDOOR);
        registerItemBlock(Blocks.SINDAR_TRAPDOOR);
        registerItemBlock(Blocks.RIVERFOLK_TRAPDOOR);
        registerItemBlock(Blocks.FORNOST_TRAPDOOR);
        registerItemBlock(Blocks.PRISON_TRAPDOOR);
        registerItemBlock(Blocks.CANDLE);
        registerItemBlock(Blocks.HUMAN_LADDER);
        registerItemBlock(Blocks.ROHIRRIM_LADDER);
        registerItemBlock(Blocks.DUNLAND_LADDER);
        registerItemBlock(Blocks.LORILENDEL);
        registerItemBlock(Blocks.PILE_OF_COINS);
        registerItemBlock(Blocks.GREY_COLUMN);
        registerItemBlock(Blocks.GREY_COLUMN_TOP);
        registerItemBlock(Blocks.COLUMN_TOP_DECORATION);
        registerItemBlock(Blocks.MEDUSELD_PILLAR);
        registerItemBlock(Blocks.DIAGONAL_BRICKS);
        // End Awaken Dreams code
        registerItem(256, "iron_shovel", (new ItemSpade(Item.ToolMaterial.IRON)).setUnlocalizedName("shovelIron"));
        registerItem(257, "iron_pickaxe", (new ItemPickaxe(Item.ToolMaterial.IRON)).setUnlocalizedName("pickaxeIron"));
        registerItem(258, "iron_axe", (new ItemAxe(Item.ToolMaterial.IRON)).setUnlocalizedName("hatchetIron"));
        registerItem(259, "flint_and_steel", (new ItemFlintAndSteel()).setUnlocalizedName("flintAndSteel"));
        registerItem(260, "apple", (new ItemFood(4, 0.3F, false)).setUnlocalizedName("apple"));
        registerItem(261, "bow", (new ItemBow()).setUnlocalizedName("bow"));
        registerItem(262, "arrow", (new ItemArrow()).setUnlocalizedName("arrow"));
        registerItem(263, "coal", (new ItemCoal()).setUnlocalizedName("coal"));
        registerItem(264, "diamond", (new Item()).setUnlocalizedName("diamond").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(265, "iron_ingot", (new Item()).setUnlocalizedName("ingotIron").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(266, "gold_ingot", (new Item()).setUnlocalizedName("ingotGold").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(267, "iron_sword", (new ItemSword(Item.ToolMaterial.IRON)).setUnlocalizedName("swordIron"));
        registerItem(268, "wooden_sword", (new ItemSword(Item.ToolMaterial.WOOD)).setUnlocalizedName("swordWood"));
        registerItem(269, "wooden_shovel", (new ItemSpade(Item.ToolMaterial.WOOD)).setUnlocalizedName("shovelWood"));
        registerItem(270, "wooden_pickaxe", (new ItemPickaxe(Item.ToolMaterial.WOOD)).setUnlocalizedName("pickaxeWood"));
        registerItem(271, "wooden_axe", (new ItemAxe(Item.ToolMaterial.WOOD)).setUnlocalizedName("hatchetWood"));
        registerItem(272, "stone_sword", (new ItemSword(Item.ToolMaterial.STONE)).setUnlocalizedName("swordStone"));
        registerItem(273, "stone_shovel", (new ItemSpade(Item.ToolMaterial.STONE)).setUnlocalizedName("shovelStone"));
        registerItem(274, "stone_pickaxe", (new ItemPickaxe(Item.ToolMaterial.STONE)).setUnlocalizedName("pickaxeStone"));
        registerItem(275, "stone_axe", (new ItemAxe(Item.ToolMaterial.STONE)).setUnlocalizedName("hatchetStone"));
        registerItem(276, "diamond_sword", (new ItemSword(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("swordDiamond"));
        registerItem(277, "diamond_shovel", (new ItemSpade(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("shovelDiamond"));
        registerItem(278, "diamond_pickaxe", (new ItemPickaxe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("pickaxeDiamond"));
        registerItem(279, "diamond_axe", (new ItemAxe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("hatchetDiamond"));
        registerItem(280, "stick", (new Item()).setFull3D().setUnlocalizedName("stick").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(281, "bowl", (new Item()).setUnlocalizedName("bowl").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(282, "mushroom_stew", (new ItemSoup(6)).setUnlocalizedName("mushroomStew"));
        registerItem(283, "golden_sword", (new ItemSword(Item.ToolMaterial.GOLD)).setUnlocalizedName("swordGold"));
        registerItem(284, "golden_shovel", (new ItemSpade(Item.ToolMaterial.GOLD)).setUnlocalizedName("shovelGold"));
        registerItem(285, "golden_pickaxe", (new ItemPickaxe(Item.ToolMaterial.GOLD)).setUnlocalizedName("pickaxeGold"));
        registerItem(286, "golden_axe", (new ItemAxe(Item.ToolMaterial.GOLD)).setUnlocalizedName("hatchetGold"));
        registerItem(287, "string", (new ItemBlockSpecial(Blocks.TRIPWIRE)).setUnlocalizedName("string").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(288, "feather", (new Item()).setUnlocalizedName("feather").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(289, "gunpowder", (new Item()).setUnlocalizedName("sulphur").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(290, "wooden_hoe", (new ItemHoe(Item.ToolMaterial.WOOD)).setUnlocalizedName("hoeWood"));
        registerItem(291, "stone_hoe", (new ItemHoe(Item.ToolMaterial.STONE)).setUnlocalizedName("hoeStone"));
        registerItem(292, "iron_hoe", (new ItemHoe(Item.ToolMaterial.IRON)).setUnlocalizedName("hoeIron"));
        registerItem(293, "diamond_hoe", (new ItemHoe(Item.ToolMaterial.DIAMOND)).setUnlocalizedName("hoeDiamond"));
        registerItem(294, "golden_hoe", (new ItemHoe(Item.ToolMaterial.GOLD)).setUnlocalizedName("hoeGold"));
        registerItem(295, "wheat_seeds", (new ItemSeeds(Blocks.WHEAT, Blocks.FARMLAND)).setUnlocalizedName("seeds"));
        registerItem(296, "wheat", (new Item()).setUnlocalizedName("wheat").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(297, "bread", (new ItemFood(5, 0.6F, false)).setUnlocalizedName("bread"));
        registerItem(298, "leather_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetCloth"));
        registerItem(299, "leather_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateCloth"));
        registerItem(300, "leather_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsCloth"));
        registerItem(301, "leather_boots", (new ItemArmor(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsCloth"));
        registerItem(302, "chainmail_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetChain"));
        registerItem(303, "chainmail_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateChain"));
        registerItem(304, "chainmail_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsChain"));
        registerItem(305, "chainmail_boots", (new ItemArmor(ItemArmor.ArmorMaterial.CHAIN, 1, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsChain"));
        registerItem(306, "iron_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetIron"));
        registerItem(307, "iron_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateIron"));
        registerItem(308, "iron_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsIron"));
        registerItem(309, "iron_boots", (new ItemArmor(ItemArmor.ArmorMaterial.IRON, 2, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsIron"));
        registerItem(310, "diamond_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetDiamond"));
        registerItem(311, "diamond_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateDiamond"));
        registerItem(312, "diamond_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsDiamond"));
        registerItem(313, "diamond_boots", (new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 3, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsDiamond"));
        registerItem(314, "golden_helmet", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.HEAD)).setUnlocalizedName("helmetGold"));
        registerItem(315, "golden_chestplate", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.CHEST)).setUnlocalizedName("chestplateGold"));
        registerItem(316, "golden_leggings", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.LEGS)).setUnlocalizedName("leggingsGold"));
        registerItem(317, "golden_boots", (new ItemArmor(ItemArmor.ArmorMaterial.GOLD, 4, EntityEquipmentSlot.FEET)).setUnlocalizedName("bootsGold"));
        registerItem(318, "flint", (new Item()).setUnlocalizedName("flint").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(319, "porkchop", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("porkchopRaw"));
        registerItem(320, "cooked_porkchop", (new ItemFood(8, 0.8F, true)).setUnlocalizedName("porkchopCooked"));
        registerItem(321, "painting", (new ItemHangingEntity(EntityPainting.class)).setUnlocalizedName("painting"));
        registerItem(322, "golden_apple", (new ItemAppleGold(4, 1.2F, false)).setAlwaysEdible().setUnlocalizedName("appleGold"));
        registerItem(323, "sign", (new ItemSign()).setUnlocalizedName("sign"));
        registerItem(324, "wooden_door", (new ItemDoor(Blocks.OAK_DOOR)).setUnlocalizedName("doorOak"));
        Item item = (new ItemBucket(Blocks.AIR)).setUnlocalizedName("bucket").setMaxStackSize(16);
        registerItem(325, "bucket", item);
        registerItem(326, "water_bucket", (new ItemBucket(Blocks.FLOWING_WATER)).setUnlocalizedName("bucketWater").setContainerItem(item));
        registerItem(327, "lava_bucket", (new ItemBucket(Blocks.FLOWING_LAVA)).setUnlocalizedName("bucketLava").setContainerItem(item));
        registerItem(328, "minecart", (new ItemMinecart(EntityMinecart.Type.RIDEABLE)).setUnlocalizedName("minecart"));
        registerItem(329, "saddle", (new ItemSaddle()).setUnlocalizedName("saddle"));
        registerItem(330, "iron_door", (new ItemDoor(Blocks.IRON_DOOR)).setUnlocalizedName("doorIron"));
        registerItem(331, "redstone", (new ItemRedstone()).setUnlocalizedName("redstone"));
        registerItem(332, "snowball", (new ItemSnowball()).setUnlocalizedName("snowball"));
        registerItem(333, "boat", new ItemBoat(EntityBoat.Type.OAK));
        registerItem(334, "leather", (new Item()).setUnlocalizedName("leather").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(335, "milk_bucket", (new ItemBucketMilk()).setUnlocalizedName("milk").setContainerItem(item));
        registerItem(336, "brick", (new Item()).setUnlocalizedName("brick").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(337, "clay_ball", (new Item()).setUnlocalizedName("clay").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(338, "reeds", (new ItemBlockSpecial(Blocks.REEDS)).setUnlocalizedName("reeds").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(339, "paper", (new Item()).setUnlocalizedName("paper").setCreativeTab(CreativeTabs.MISC));
        registerItem(340, "book", (new ItemBook()).setUnlocalizedName("book").setCreativeTab(CreativeTabs.MISC));
        registerItem(341, "slime_ball", (new Item()).setUnlocalizedName("slimeball").setCreativeTab(CreativeTabs.MISC));
        registerItem(342, "chest_minecart", (new ItemMinecart(EntityMinecart.Type.CHEST)).setUnlocalizedName("minecartChest"));
        registerItem(343, "furnace_minecart", (new ItemMinecart(EntityMinecart.Type.FURNACE)).setUnlocalizedName("minecartFurnace"));
        registerItem(344, "egg", (new ItemEgg()).setUnlocalizedName("egg"));
        registerItem(345, "compass", (new ItemCompass()).setUnlocalizedName("compass").setCreativeTab(CreativeTabs.TOOLS));
        registerItem(346, "fishing_rod", (new ItemFishingRod()).setUnlocalizedName("fishingRod"));
        registerItem(347, "clock", (new ItemClock()).setUnlocalizedName("clock").setCreativeTab(CreativeTabs.TOOLS));
        registerItem(348, "glowstone_dust", (new Item()).setUnlocalizedName("yellowDust").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(349, "fish", (new ItemFishFood(false)).setUnlocalizedName("fish").setHasSubtypes(true));
        registerItem(350, "cooked_fish", (new ItemFishFood(true)).setUnlocalizedName("fish").setHasSubtypes(true));
        registerItem(351, "dye", (new ItemDye()).setUnlocalizedName("dyePowder"));
        registerItem(352, "bone", (new Item()).setUnlocalizedName("bone").setFull3D().setCreativeTab(CreativeTabs.MISC));
        registerItem(353, "sugar", (new Item()).setUnlocalizedName("sugar").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(354, "cake", (new ItemBlockSpecial(Blocks.CAKE)).setMaxStackSize(1).setUnlocalizedName("cake").setCreativeTab(CreativeTabs.FOOD));
        registerItem(355, "bed", (new ItemBed()).setMaxStackSize(1).setUnlocalizedName("bed"));
        registerItem(356, "repeater", (new ItemBlockSpecial(Blocks.UNPOWERED_REPEATER)).setUnlocalizedName("diode").setCreativeTab(CreativeTabs.REDSTONE));
        registerItem(357, "cookie", (new ItemFood(2, 0.1F, false)).setUnlocalizedName("cookie"));
        registerItem(358, "filled_map", (new ItemMap()).setUnlocalizedName("map"));
        registerItem(359, "shears", (new ItemShears()).setUnlocalizedName("shears"));
        registerItem(360, "melon", (new ItemFood(2, 0.3F, false)).setUnlocalizedName("melon"));
        registerItem(361, "pumpkin_seeds", (new ItemSeeds(Blocks.PUMPKIN_STEM, Blocks.FARMLAND)).setUnlocalizedName("seeds_pumpkin"));
        registerItem(362, "melon_seeds", (new ItemSeeds(Blocks.MELON_STEM, Blocks.FARMLAND)).setUnlocalizedName("seeds_melon"));
        registerItem(363, "beef", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("beefRaw"));
        registerItem(364, "cooked_beef", (new ItemFood(8, 0.8F, true)).setUnlocalizedName("beefCooked"));
        registerItem(365, "chicken", (new ItemFood(2, 0.3F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.3F).setUnlocalizedName("chickenRaw"));
        registerItem(366, "cooked_chicken", (new ItemFood(6, 0.6F, true)).setUnlocalizedName("chickenCooked"));
        registerItem(367, "rotten_flesh", (new ItemFood(4, 0.1F, true)).setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F).setUnlocalizedName("rottenFlesh"));
        registerItem(368, "ender_pearl", (new ItemEnderPearl()).setUnlocalizedName("enderPearl"));
        registerItem(369, "blaze_rod", (new Item()).setUnlocalizedName("blazeRod").setCreativeTab(CreativeTabs.MATERIALS).setFull3D());
        registerItem(370, "ghast_tear", (new Item()).setUnlocalizedName("ghastTear").setCreativeTab(CreativeTabs.BREWING));
        registerItem(371, "gold_nugget", (new Item()).setUnlocalizedName("goldNugget").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(372, "nether_wart", (new ItemSeeds(Blocks.NETHER_WART, Blocks.SOUL_SAND)).setUnlocalizedName("netherStalkSeeds"));
        registerItem(373, "potion", (new ItemPotion()).setUnlocalizedName("potion"));
        Item item1 = (new ItemGlassBottle()).setUnlocalizedName("glassBottle");
        registerItem(374, "glass_bottle", item1);
        registerItem(375, "spider_eye", (new ItemFood(2, 0.8F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 1.0F).setUnlocalizedName("spiderEye"));
        registerItem(376, "fermented_spider_eye", (new Item()).setUnlocalizedName("fermentedSpiderEye").setCreativeTab(CreativeTabs.BREWING));
        registerItem(377, "blaze_powder", (new Item()).setUnlocalizedName("blazePowder").setCreativeTab(CreativeTabs.BREWING));
        registerItem(378, "magma_cream", (new Item()).setUnlocalizedName("magmaCream").setCreativeTab(CreativeTabs.BREWING));
        registerItem(379, "brewing_stand", (new ItemBlockSpecial(Blocks.BREWING_STAND)).setUnlocalizedName("brewingStand").setCreativeTab(CreativeTabs.BREWING));
        registerItem(380, "cauldron", (new ItemBlockSpecial(Blocks.CAULDRON)).setUnlocalizedName("cauldron").setCreativeTab(CreativeTabs.BREWING));
        registerItem(381, "ender_eye", (new ItemEnderEye()).setUnlocalizedName("eyeOfEnder"));
        registerItem(382, "speckled_melon", (new Item()).setUnlocalizedName("speckledMelon").setCreativeTab(CreativeTabs.BREWING));
        registerItem(383, "spawn_egg", (new ItemMonsterPlacer()).setUnlocalizedName("monsterPlacer"));
        registerItem(384, "experience_bottle", (new ItemExpBottle()).setUnlocalizedName("expBottle"));
        registerItem(385, "fire_charge", (new ItemFireball()).setUnlocalizedName("fireball"));
        registerItem(386, "writable_book", (new ItemWritableBook()).setUnlocalizedName("writingBook").setCreativeTab(CreativeTabs.MISC));
        registerItem(387, "written_book", (new ItemWrittenBook()).setUnlocalizedName("writtenBook").setMaxStackSize(16));
        registerItem(388, "emerald", (new Item()).setUnlocalizedName("emerald").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(389, "item_frame", (new ItemHangingEntity(EntityItemFrame.class)).setUnlocalizedName("frame"));
        registerItem(390, "flower_pot", (new ItemBlockSpecial(Blocks.FLOWER_POT)).setUnlocalizedName("flowerPot").setCreativeTab(CreativeTabs.DECORATIONS));
        registerItem(391, "carrot", (new ItemSeedFood(3, 0.6F, Blocks.CARROTS, Blocks.FARMLAND)).setUnlocalizedName("carrots"));
        registerItem(392, "potato", (new ItemSeedFood(1, 0.3F, Blocks.POTATOES, Blocks.FARMLAND)).setUnlocalizedName("potato"));
        registerItem(393, "baked_potato", (new ItemFood(5, 0.6F, false)).setUnlocalizedName("potatoBaked"));
        registerItem(394, "poisonous_potato", (new ItemFood(2, 0.3F, false)).setPotionEffect(new PotionEffect(MobEffects.POISON, 100, 0), 0.6F).setUnlocalizedName("potatoPoisonous"));
        registerItem(395, "map", (new ItemEmptyMap()).setUnlocalizedName("emptyMap"));
        registerItem(396, "golden_carrot", (new ItemFood(6, 1.2F, false)).setUnlocalizedName("carrotGolden").setCreativeTab(CreativeTabs.BREWING));
        registerItem(397, "skull", (new ItemSkull()).setUnlocalizedName("skull"));
        registerItem(398, "carrot_on_a_stick", (new ItemCarrotOnAStick()).setUnlocalizedName("carrotOnAStick"));
        registerItem(399, "nether_star", (new ItemSimpleFoiled()).setUnlocalizedName("netherStar").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(400, "pumpkin_pie", (new ItemFood(8, 0.3F, false)).setUnlocalizedName("pumpkinPie").setCreativeTab(CreativeTabs.FOOD));
        registerItem(401, "fireworks", (new ItemFirework()).setUnlocalizedName("fireworks"));
        registerItem(402, "firework_charge", (new ItemFireworkCharge()).setUnlocalizedName("fireworksCharge").setCreativeTab(CreativeTabs.MISC));
        registerItem(403, "enchanted_book", (new ItemEnchantedBook()).setMaxStackSize(1).setUnlocalizedName("enchantedBook"));
        registerItem(404, "comparator", (new ItemBlockSpecial(Blocks.UNPOWERED_COMPARATOR)).setUnlocalizedName("comparator").setCreativeTab(CreativeTabs.REDSTONE));
        registerItem(405, "netherbrick", (new Item()).setUnlocalizedName("netherbrick").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(406, "quartz", (new Item()).setUnlocalizedName("netherquartz").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(407, "tnt_minecart", (new ItemMinecart(EntityMinecart.Type.TNT)).setUnlocalizedName("minecartTnt"));
        registerItem(408, "hopper_minecart", (new ItemMinecart(EntityMinecart.Type.HOPPER)).setUnlocalizedName("minecartHopper"));
        registerItem(409, "prismarine_shard", (new Item()).setUnlocalizedName("prismarineShard").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(410, "prismarine_crystals", (new Item()).setUnlocalizedName("prismarineCrystals").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(411, "rabbit", (new ItemFood(3, 0.3F, true)).setUnlocalizedName("rabbitRaw"));
        registerItem(412, "cooked_rabbit", (new ItemFood(5, 0.6F, true)).setUnlocalizedName("rabbitCooked"));
        registerItem(413, "rabbit_stew", (new ItemSoup(10)).setUnlocalizedName("rabbitStew"));
        registerItem(414, "rabbit_foot", (new Item()).setUnlocalizedName("rabbitFoot").setCreativeTab(CreativeTabs.BREWING));
        registerItem(415, "rabbit_hide", (new Item()).setUnlocalizedName("rabbitHide").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(416, "armor_stand", (new ItemArmorStand()).setUnlocalizedName("armorStand").setMaxStackSize(16));
        registerItem(417, "iron_horse_armor", (new Item()).setUnlocalizedName("horsearmormetal").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
        registerItem(418, "golden_horse_armor", (new Item()).setUnlocalizedName("horsearmorgold").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
        registerItem(419, "diamond_horse_armor", (new Item()).setUnlocalizedName("horsearmordiamond").setMaxStackSize(1).setCreativeTab(CreativeTabs.MISC));
        registerItem(420, "lead", (new ItemLead()).setUnlocalizedName("leash"));
        registerItem(421, "name_tag", (new ItemNameTag()).setUnlocalizedName("nameTag"));
        registerItem(422, "command_block_minecart", (new ItemMinecart(EntityMinecart.Type.COMMAND_BLOCK)).setUnlocalizedName("minecartCommandBlock").setCreativeTab((CreativeTabs)null));
        registerItem(423, "mutton", (new ItemFood(2, 0.3F, true)).setUnlocalizedName("muttonRaw"));
        registerItem(424, "cooked_mutton", (new ItemFood(6, 0.8F, true)).setUnlocalizedName("muttonCooked"));
        registerItem(425, "banner", (new ItemBanner()).setUnlocalizedName("banner"));
        registerItem(426, "end_crystal", new ItemEndCrystal());
        registerItem(427, "spruce_door", (new ItemDoor(Blocks.SPRUCE_DOOR)).setUnlocalizedName("doorSpruce"));
        registerItem(428, "birch_door", (new ItemDoor(Blocks.BIRCH_DOOR)).setUnlocalizedName("doorBirch"));
        registerItem(429, "jungle_door", (new ItemDoor(Blocks.JUNGLE_DOOR)).setUnlocalizedName("doorJungle"));
        registerItem(430, "acacia_door", (new ItemDoor(Blocks.ACACIA_DOOR)).setUnlocalizedName("doorAcacia"));
        registerItem(431, "dark_oak_door", (new ItemDoor(Blocks.DARK_OAK_DOOR)).setUnlocalizedName("doorDarkOak"));
        registerItem(432, "chorus_fruit", (new ItemChorusFruit(4, 0.3F)).setAlwaysEdible().setUnlocalizedName("chorusFruit").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(433, "chorus_fruit_popped", (new Item()).setUnlocalizedName("chorusFruitPopped").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(434, "beetroot", (new ItemFood(1, 0.6F, false)).setUnlocalizedName("beetroot"));
        registerItem(435, "beetroot_seeds", (new ItemSeeds(Blocks.BEETROOTS, Blocks.FARMLAND)).setUnlocalizedName("beetroot_seeds"));
        registerItem(436, "beetroot_soup", (new ItemSoup(6)).setUnlocalizedName("beetroot_soup"));
        registerItem(437, "dragon_breath", (new Item()).setCreativeTab(CreativeTabs.BREWING).setUnlocalizedName("dragon_breath").setContainerItem(item1));
        registerItem(438, "splash_potion", (new ItemSplashPotion()).setUnlocalizedName("splash_potion"));
        registerItem(439, "spectral_arrow", (new ItemSpectralArrow()).setUnlocalizedName("spectral_arrow"));
        registerItem(440, "tipped_arrow", (new ItemTippedArrow()).setUnlocalizedName("tipped_arrow"));
        registerItem(441, "lingering_potion", (new ItemLingeringPotion()).setUnlocalizedName("lingering_potion"));
        registerItem(442, "shield", (new ItemShield()).setUnlocalizedName("shield"));
        registerItem(443, "elytra", (new ItemElytra()).setUnlocalizedName("elytra"));
        registerItem(444, "spruce_boat", new ItemBoat(EntityBoat.Type.SPRUCE));
        registerItem(445, "birch_boat", new ItemBoat(EntityBoat.Type.BIRCH));
        registerItem(446, "jungle_boat", new ItemBoat(EntityBoat.Type.JUNGLE));
        registerItem(447, "acacia_boat", new ItemBoat(EntityBoat.Type.ACACIA));
        registerItem(448, "dark_oak_boat", new ItemBoat(EntityBoat.Type.DARK_OAK));
        registerItem(2256, "record_13", (new ItemRecord("13", SoundEvents.RECORD_13)).setUnlocalizedName("record"));
        registerItem(2257, "record_cat", (new ItemRecord("cat", SoundEvents.RECORD_CAT)).setUnlocalizedName("record"));
        registerItem(2258, "record_blocks", (new ItemRecord("blocks", SoundEvents.RECORD_BLOCKS)).setUnlocalizedName("record"));
        registerItem(2259, "record_chirp", (new ItemRecord("chirp", SoundEvents.RECORD_CHIRP)).setUnlocalizedName("record"));
        registerItem(2260, "record_far", (new ItemRecord("far", SoundEvents.RECORD_FAR)).setUnlocalizedName("record"));
        registerItem(2261, "record_mall", (new ItemRecord("mall", SoundEvents.RECORD_MALL)).setUnlocalizedName("record"));
        registerItem(2262, "record_mellohi", (new ItemRecord("mellohi", SoundEvents.RECORD_MELLOHI)).setUnlocalizedName("record"));
        registerItem(2263, "record_stal", (new ItemRecord("stal", SoundEvents.RECORD_STAL)).setUnlocalizedName("record"));
        registerItem(2264, "record_strad", (new ItemRecord("strad", SoundEvents.RECORD_STRAD)).setUnlocalizedName("record"));
        registerItem(2265, "record_ward", (new ItemRecord("ward", SoundEvents.RECORD_WARD)).setUnlocalizedName("record"));
        registerItem(2266, "record_11", (new ItemRecord("11", SoundEvents.RECORD_11)).setUnlocalizedName("record"));
        registerItem(2267, "record_wait", (new ItemRecord("wait", SoundEvents.RECORD_WAIT)).setUnlocalizedName("record"));
        // Begin Awaken Dreams code
        /*itemRegistry.addObject(6000, "lembas", (new ItemValarFood(8, 0.8F, false).setUnlocalizedName("lembas").setTextureName("lembas")));
        itemRegistry.addObject(6001, "mithril_ingot", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("mithrilIngot").setTextureName("mithril_ingot")));
        itemRegistry.addObject(6002, "bronze_ingot", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("bronzeIngot").setTextureName("bronze_ingot")));
        itemRegistry.addObject(6003, "pipe", (new ItemValarBase().setUnlocalizedName("pipe").setTextureName("pipe")));
        itemRegistry.addObject(6004, "tobacco", (new ItemValarBase().setUnlocalizedName("tobacco").setTextureName("tobacco")));
        itemRegistry.addObject(6005, "flaming_arrow", (new ItemValarBase().setUnlocalizedName("flamingArrow").setTextureName("flaming_arrow")));
        //itemRegistry.addObject(6006, "requiem_for_a_dream", (new ItemValarBase().setUnlocalizedName("requiemForADream").setTextureName("requiem_for_a_dream")));
        //itemRegistry.addObject(6007, "ruby", (new ItemValarBase().setUnlocalizedName("ruby").setTextureName("ruby")));
        itemRegistry.addObject(6008, "jade", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("jade").setTextureName("jade")));
        itemRegistry.addObject(6009, "amber", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("amber").setTextureName("amber")));
        itemRegistry.addObject(6010, "parchment", (new ItemValarBase().setUnlocalizedName("parchment").setTextureName("parchment")));
        //itemRegistry.addObject(6011, "evenstar_record", (new ItemValarBase().setUnlocalizedName("evenstar_record").setTextureName("evenstar_record")));
        //itemRegistry.addObject(6012, "bridge_of_khazad_dum", (new ItemValarBase().setUnlocalizedName("bridgeOfKhazadDum").setTextureName("bridge_of_khazad_dum")));
        //itemRegistry.addObject(6013, "isengard_unleashed", (new ItemValarBase().setUnlocalizedName("isengardUnleashed").setTextureName("isengard_unleashed")));
        itemRegistry.addObject(6014, "crystal", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("crystal").setTextureName("crystal")));
        itemRegistry.addObject(6015, "amethyst", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("amethyst").setTextureName("amethyst")));
        itemRegistry.addObject(6016, "onyx", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("onyx").setTextureName("onyx")));
        itemRegistry.addObject(6017, "moonstone", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("moonstone").setTextureName("moonstone")));
        //itemRegistry.addObject(6018, "quartz", (new ItemValarBase().setUnlocalizedName("quartz").setTextureName("quartz")));
        itemRegistry.addObject(6019, "tanzanite", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("tanzanite").setTextureName("tanzanite")));
        itemRegistry.addObject(6020, "uruk_sword", (new ItemValarWeapon(510, 7, 0, true, false).setUnlocalizedName("urukSword").setTextureName("uruk_sword")));
        itemRegistry.addObject(6021, "phial_of_galadriel", (new ItemValarBase().setUnlocalizedName("phialOfGaladriel").setTextureName("phial_of_galadriel")));
        itemRegistry.addObject(6022, "tomato", (new ItemValarFood(1, 0.1F, false).setUnlocalizedName("tomato").setTextureName("tomato")));
        itemRegistry.addObject(6023, "blue_feather", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("blueFeather").setTextureName("blue_feather")));
        //itemRegistry.addObject(6024, "rain_stick", (new ItemValarBase().setUnlocalizedName("rainStick").setTextureName("rain_stick")));
        //itemRegistry.addObject(6025, "sun_staff", (new ItemValarBase().setUnlocalizedName("sunStaff").setTextureName("sun_staff")));
        itemRegistry.addObject(6026, "spider_poison", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("spiderPoison").setTextureName("spider_poison")));
        //itemRegistry.addObject(6027, "life_staff", (new ItemValarBase().setUnlocalizedName("lifeStaff").setTextureName("life_staff")));
        itemRegistry.addObject(6028, "herugrim", (new ItemValarWeapon(830, 7, 0, true, false).setUnlocalizedName("herugrim").setTextureName("herugrim")));
        itemRegistry.addObject(6029, "nuts", (new ItemValarFood(1, 0.2F, false).setUnlocalizedName("nuts").setTextureName("nuts")));
        // SPOTS 6030-6031 FREE
        itemRegistry.addObject(6032, "corn_cob", (new ItemValarFood(2, 0.3F, false).setUnlocalizedName("cornCob").setTextureName("corn_cob")));
        //itemRegistry.addObject(6033, "healing_staff", (new ItemValarBase().setUnlocalizedName("healingStaff").setTextureName("healing_staff")));
        //itemRegistry.addObject(6034, "lightning_rod", (new ItemValarBase().setUnlocalizedName("lightningRod").setTextureName("lightning_rod")));
        itemRegistry.addObject(6035, "staff_base", (new ItemValarBase().setUnlocalizedName("staffBase").setTextureName("staff_base")));
        itemRegistry.addObject(6036, "knife", (new ItemValarWeapon(35, 4, 0, true, false).setUnlocalizedName("knife").setTextureName("knife")));
        //itemRegistry.addObject(6037, "mana_bottle", (new ItemValarBase().setUnlocalizedName("manaBottle").setTextureName("mana_bottle")));
        itemRegistry.addObject(6038, "berries", (new ItemValarFood(2, 0.3F, false).setUnlocalizedName("berries").setTextureName("berries")));
        itemRegistry.addObject(6039, "gondorian_horn", (new ItemValarInstrument("").setUnlocalizedName("gondorianHorn").setTextureName("gondorian_horn")));
        itemRegistry.addObject(6040, "bear_claw", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("bearClaw").setTextureName("bear_claw")));
        itemRegistry.addObject(6041, "beast_skin", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("beastSkin").setTextureName("beast_skin")));
        itemRegistry.addObject(6042, "dead_root", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("deadRoot").setTextureName("dead_root")));
        itemRegistry.addObject(6043, "eagle_feather", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("eagleFeather").setTextureName("eagle_feather")));
        itemRegistry.addObject(6044, "goblin_eye", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("goblinEye").setTextureName("goblin_eye")));
        itemRegistry.addObject(6045, "helegrog_deamon_heart", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("helegrogDeamonHeart").setTextureName("helegrog_deamon_heart")));
        itemRegistry.addObject(6046, "orc_blood", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("orcBlood").setTextureName("orc_blood")));
        itemRegistry.addObject(6047, "owl_feather", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("owlFeather").setTextureName("owl_feather")));
        itemRegistry.addObject(6048, "scorpion_tail", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("scorpionTail").setTextureName("scorpion_tail")));
        itemRegistry.addObject(6049, "shark_tooth", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("sharkTooth").setTextureName("shark_tooth")));
        itemRegistry.addObject(6050, "troll_skin", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("trollSkin").setTextureName("troll_skin")));
        itemRegistry.addObject(6051, "warg_fang", (new ItemValarBase(CreativeTabs.tabMaterials).setUnlocalizedName("wargFang").setTextureName("warg_fang")));
        itemRegistry.addObject(6052, "blue_bed_item", (new ItemValarBed((BlockValarBed)Blocks.blueBed).setUnlocalizedName("blueBedItem").setTextureName("blue_bed_item")));
        itemRegistry.addObject(6053, "brown_bed_item", (new ItemValarBed((BlockValarBed)Blocks.brownBed).setUnlocalizedName("brownBedItem").setTextureName("brown_bed_item")));
        itemRegistry.addObject(6054, "brown_hobbit_hat", (new ItemValarArmor(0, 1, 55, 0).setUnlocalizedName("brownHobbitHat").setTextureName("brown_hobbit_hat")));
        itemRegistry.addObject(6055, "orc_helmet", (new ItemValarArmor(0, 1, 200, 0).setUnlocalizedName("orcHelmet").setTextureName("orc_helmet")));*/
        registerItem(6056, "bree_door", (new ItemDoor(Blocks.BREE_DOOR)).setUnlocalizedName("breeDoor"));
      //itemRegistry.addObject(6057, "rucksack", (new ItemValarBase().setUnlocalizedName("rucksack").setTextureName("rucksack")));
        /*itemRegistry.addObject(6058, "cram", (new ItemValarFood(2, 0.8F, false).setUnlocalizedName("cram").setTextureName("cram")));
        itemRegistry.addObject(6059, "cabbage", (new ItemValarFood(3, 0.6F, false).setUnlocalizedName("cabbage").setTextureName("cabbage")));
        itemRegistry.addObject(6060, "coconut", (new ItemValarFood(1, 0.3F, false).setUnlocalizedName("coconut").setTextureName("coconut")));
        itemRegistry.addObject(6061, "raw_deer_meat", (new ItemValarFood(3, 0.4F, false).setUnlocalizedName("rawDeerMeat").setTextureName("raw_deer_meat")));
        itemRegistry.addObject(6062, "cooked_deer_meat", (new ItemValarFood(5, 0.6F, false).setUnlocalizedName("cookedDeerMeat").setTextureName("cooked_deer_meat")));
        itemRegistry.addObject(6063, "muffin", (new ItemValarFood(1, 0.4F, false).setUnlocalizedName("muffin").setTextureName("muffin")));
        itemRegistry.addObject(6064, "strawberry", (new ItemValarFood(2, 0.3F, false).setUnlocalizedName("strawberry").setTextureName("strawberry")));
        itemRegistry.addObject(6065, "strawberry_muffin", (new ItemValarFood(3, 0.6F, false).setUnlocalizedName("strawberryMuffin").setTextureName("strawberry_muffin")));
        itemRegistry.addObject(6066, "orange", (new ItemValarFood(2, 0.6F, false).setUnlocalizedName("orange").setTextureName("orange")));
        itemRegistry.addObject(6067, "orange_muffin", (new ItemValarFood(3, 0.6F, false).setUnlocalizedName("orangeMuffin").setTextureName("orange_muffin")));
        itemRegistry.addObject(6068, "pear", (new ItemValarFood(2, 0.6F, false).setUnlocalizedName("pear").setTextureName("pear")));
        itemRegistry.addObject(6069, "raw_meat", (new ItemValarFood(2, 0.2F, false).setUnlocalizedName("rawMeat").setTextureName("raw_meat")));
        itemRegistry.addObject(6070, "cooked_meat", (new ItemValarFood(5, 0.7F, false).setUnlocalizedName("cookedMeat").setTextureName("cooked_meat")));
        itemRegistry.addObject(6071, "rohan_helm_1", (new ItemValarBase().setUnlocalizedName("rohanHelm1").setTextureName("rohan_helm_1")));
        itemRegistry.addObject(6072, "rohan_helm_2", (new ItemValarBase().setUnlocalizedName("rohanHelm2").setTextureName("rohan_helm_2")));
        itemRegistry.addObject(6073, "rohan_helm_3", (new ItemValarBase().setUnlocalizedName("rohanHelm3").setTextureName("rohan_helm_3")));
        itemRegistry.addObject(6074, "rohan_helm_4", (new ItemValarBase().setUnlocalizedName("rohanHelm4").setTextureName("rohan_helm_4")));
        itemRegistry.addObject(6075, "rohan_helm_5", (new ItemValarBase().setUnlocalizedName("rohanHelm5").setTextureName("rohan_helm_5")));
        itemRegistry.addObject(6076, "rohan_helm_6", (new ItemValarBase().setUnlocalizedName("rohanHelm6").setTextureName("rohan_helm_6")));
        itemRegistry.addObject(6077, "rohan_chestplate", (new ItemValarBase().setUnlocalizedName("rohanChestplate").setTextureName("rohan_chestplate")));
        //itemRegistry.addObject(6078, "crossbow", (new ItemValarBase().setUnlocalizedName("crossbow").setTextureName("crossbow")));
        //itemRegistry.addObject(6079, "crossbow_bolt", (new ItemValarBase().setUnlocalizedName("crossbowBolt").setTextureName("crossbow_bolt")));
        itemRegistry.addObject(6080, "telescope", (new ItemValarTelescope().setUnlocalizedName("telescope").setTextureName("telescope")));
        itemRegistry.addObject(6081, "stone_inscription", (new ItemValarStoneInscription()).setUnlocalizedName("stoneInscription").setTextureName("stone_inscription"));
        itemRegistry.addObject(6082, "sting", (new ItemValarSting().setUnlocalizedName("sting").setTextureName("sting")));
        //itemRegistry.addObject(6083, "into_the_west", (new ItemValarBase().setUnlocalizedName("intotheWest").setTextureName("into_the_west")));
        //itemRegistry.addObject(6084, "may_it_be", (new ItemValarBase().setUnlocalizedName("mayItBe").setTextureName("may_it_be")));
        itemRegistry.addObject(6085, "human_dagger", (new ItemValarWeapon(50, 4).setUnlocalizedName("humanDagger").setTextureName("human_dagger")));
        itemRegistry.addObject(6086, "hobbit_dagger", (new ItemValarWeapon(50, 4).setUnlocalizedName("hobbitDagger").setTextureName("hobbit_dagger")));
        itemRegistry.addObject(6087, "elf_dagger", (new ItemValarWeapon(80, 5).setUnlocalizedName("elfDagger").setTextureName("elf_dagger")));
        itemRegistry.addObject(6088, "gondorian_sword", (new ItemValarWeapon(350, 6).setUnlocalizedName("gondorianSword").setTextureName("gondorian_sword")));
        itemRegistry.addObject(6089, "broken_narsil", (new ItemValarWeapon(2000, 10).setUnlocalizedName("brokenNarsil").setTextureName("broken_narsil")));
        //itemRegistry.addObject(6090, "poison_potion", (new ItemValarBase().setUnlocalizedName("poisonPotion").setTextureName("poison_potion")));
        //itemRegistry.addObject(6091, "healing_potion", (new ItemValarBase().setUnlocalizedName("healingPotion").setTextureName("healing_potion")));
        //itemRegistry.addObject(6092, "light_blue_potion", (new ItemValarBase().setUnlocalizedName("lightBluePotion").setTextureName("light_blue_potion")));
        //itemRegistry.addObject(6093, "miruvor", (new ItemValarBase().setUnlocalizedName("miruvor").setTextureName("miruvor")));
        //itemRegistry.addObject(6094, "orange_potion", (new ItemValarBase().setUnlocalizedName("orangePotion").setTextureName("orange_potion")));
        //itemRegistry.addObject(6095, "red_potion", (new ItemValarBase().setUnlocalizedName("redPotion").setTextureName("red_potion")));
        //itemRegistry.addObject(6096, "yellow_potion", (new ItemValarBase().setUnlocalizedName("yellowPotion").setTextureName("yellow_potion")));
        itemRegistry.addObject(6097, "southern_star", (new ItemValarBase().setUnlocalizedName("southernStar").setTextureName("southern_star")));
        itemRegistry.addObject(6098, "south_linch", (new ItemValarBase().setUnlocalizedName("southLinch").setTextureName("south_linch")));
        itemRegistry.addObject(6099, "gondorian_tobacco", (new ItemValarBase().setUnlocalizedName("gondorianTobacco").setTextureName("gondorian_tobacco")));
        itemRegistry.addObject(6100, "longbottom_leaf", (new ItemValarBase().setUnlocalizedName("longbottomLeaf").setTextureName("longbottom_leaf")));
        // Temporary break (remove this comment if you do not know what it is)
        itemRegistry.addObject(6101, "old_toby", (new ItemValarBase().setUnlocalizedName("oldToby").setTextureName("old_toby")));
        itemRegistry.addObject(6102, "brown_pipe", (new ItemValarBase().setUnlocalizedName("brownPipe").setTextureName("brown_pipe")));
        itemRegistry.addObject(6103, "stone_of_darkness", (new ItemValarBase().setUnlocalizedName("stoneOfDarkness").setTextureName("stone_of_darkness")));
        itemRegistry.addObject(6104, "stone_of_earth", (new ItemValarBase().setUnlocalizedName("stoneOfEarth").setTextureName("stone_of_earth")));
        itemRegistry.addObject(6105, "stone_of_fire", (new ItemValarBase().setUnlocalizedName("stoneOfFire").setTextureName("stone_of_fire")));
        itemRegistry.addObject(6106, "stone_of_greed", (new ItemValarBase().setUnlocalizedName("stoneOfGreed").setTextureName("stone_of_greed")));
        itemRegistry.addObject(6107, "stone_of_hope", (new ItemValarBase().setUnlocalizedName("stoneOfHope").setTextureName("stone_of_hope")));
        itemRegistry.addObject(6108, "stone_of_nature", (new ItemValarBase().setUnlocalizedName("stoneOfNature").setTextureName("stone_of_nature")));
        itemRegistry.addObject(6109, "stone_of_sunlight", (new ItemValarBase().setUnlocalizedName("stoneOfSunlight").setTextureName("stone_of_sunlight")));
        itemRegistry.addObject(6110, "stone_of_sea", (new ItemValarBase().setUnlocalizedName("stoneOfSea").setTextureName("stone_of_sea")));
        itemRegistry.addObject(6111, "stone_of_sky", (new ItemValarBase().setUnlocalizedName("stoneOfSky").setTextureName("stone_of_sky")));
        itemRegistry.addObject(6112, "stone_of_wealth", (new ItemValarBase().setUnlocalizedName("stoneOfWealth").setTextureName("stone_of_wealth")));
        itemRegistry.addObject(6113, "stone_of_wind", (new ItemValarBase().setUnlocalizedName("stoneOfWind").setTextureName("stone_of_wind")));
        itemRegistry.addObject(6114, "hammer", (new ItemValarBase().setUnlocalizedName("hammer").setTextureName("hammer")));
        itemRegistry.addObject(6115, "gold_ring", (new ItemValarBase().setUnlocalizedName("goldRing").setTextureName("gold_ring")));
        itemRegistry.addObject(6116, "onyx_gold_ring", (new ItemValarBase().setUnlocalizedName("onyxGoldRing").setTextureName("onyx_gold_ring")));
        itemRegistry.addObject(6117, "tanzanite_gold_ring", (new ItemValarBase().setUnlocalizedName("tanzaniteGoldRing").setTextureName("tanzanite_gold_ring")));
        itemRegistry.addObject(6118, "jade_gold_ring", (new ItemValarBase().setUnlocalizedName("jadeGoldRing").setTextureName("jade_gold_ring")));
        itemRegistry.addObject(6119, "moonstone_gold_ring", (new ItemValarBase().setUnlocalizedName("moonstoneGoldRing").setTextureName("moonstone_gold_ring")));
        itemRegistry.addObject(6120, "amethyst_gold_ring", (new ItemValarBase().setUnlocalizedName("amethystGoldRing").setTextureName("amethyst_gold_ring")));
        itemRegistry.addObject(6121, "ruby_gold_ring", (new ItemValarBase().setUnlocalizedName("rubyGoldRing").setTextureName("ruby_gold_ring")));
        itemRegistry.addObject(6122, "quartz_gold_ring", (new ItemValarBase().setUnlocalizedName("quartzGoldRing").setTextureName("quartz_gold_ring")));
        itemRegistry.addObject(6123, "amber_gold_ring", (new ItemValarBase().setUnlocalizedName("amberGoldRing").setTextureName("amber_gold_ring")));
        itemRegistry.addObject(6124, "silver_ring", (new ItemValarBase().setUnlocalizedName("silverRing").setTextureName("silver_ring")));
        itemRegistry.addObject(6125, "onyx_silver_ring", (new ItemValarBase().setUnlocalizedName("onyxSilverRing").setTextureName("onyx_silver_ring")));
        itemRegistry.addObject(6126, "tanzanite_silver_ring", (new ItemValarBase().setUnlocalizedName("tanzaniteSilverRing").setTextureName("tanzanite_silver_ring")));
        itemRegistry.addObject(6127, "jade_silver_ring", (new ItemValarBase().setUnlocalizedName("jadeSilverRing").setTextureName("jade_silver_ring")));
        itemRegistry.addObject(6128, "moonstone_silver_ring", (new ItemValarBase().setUnlocalizedName("moonstoneSilverRing").setTextureName("moonstone_silver_ring")));
        itemRegistry.addObject(6129, "amethyst_silver_ring", (new ItemValarBase().setUnlocalizedName("amethystSilverRing").setTextureName("amethyst_silver_ring")));
        itemRegistry.addObject(6130, "ruby_silver_ring", (new ItemValarBase().setUnlocalizedName("rubySilverRing").setTextureName("ruby_silver_ring")));
        itemRegistry.addObject(6131, "quartz_silver_ring", (new ItemValarBase().setUnlocalizedName("quartzSilverRing").setTextureName("quartz_silver_ring")));
        itemRegistry.addObject(6132, "amber_silver_ring", (new ItemValarBase().setUnlocalizedName("amberSilverRing").setTextureName("amber_silver_ring")));
        itemRegistry.addObject(6133, "bronze_ring", (new ItemValarBase().setUnlocalizedName("bronzeRing").setTextureName("bronze_ring")));
        itemRegistry.addObject(6134, "onyx_bronze_ring", (new ItemValarBase().setUnlocalizedName("onyxBronzeRing").setTextureName("onyx_bronze_ring")));
        itemRegistry.addObject(6135, "tanzanite_bronze_ring", (new ItemValarBase().setUnlocalizedName("tanzaniteBronzeRing").setTextureName("tanzanite_bronze_ring")));
        itemRegistry.addObject(6136, "jade_bronze_ring", (new ItemValarBase().setUnlocalizedName("jadeBronzeRing").setTextureName("jade_bronze_ring")));
        itemRegistry.addObject(6137, "moonstone_bronze_ring", (new ItemValarBase().setUnlocalizedName("moonstoneBronzeRing").setTextureName("moonstone_bronze_ring")));
        itemRegistry.addObject(6138, "amethyst_bronze_ring", (new ItemValarBase().setUnlocalizedName("amethystBronzeRing").setTextureName("amethyst_bronze_ring")));
        itemRegistry.addObject(6139, "ruby_bronze_ring", (new ItemValarBase().setUnlocalizedName("rubyBronzeRing").setTextureName("ruby_bronze_ring")));
        itemRegistry.addObject(6140, "quartz_bronze_ring", (new ItemValarBase().setUnlocalizedName("quartzBronzeRing").setTextureName("quartz_bronze_ring")));
        itemRegistry.addObject(6141, "amber_bronze_ring", (new ItemValarBase().setUnlocalizedName("amberBronzeRing").setTextureName("amber_bronze_ring")));
        itemRegistry.addObject(6142, "mithril_ring", (new ItemValarBase().setUnlocalizedName("mithrilRing").setTextureName("mithril_ring")));
        itemRegistry.addObject(6143, "onyx_mithril_ring", (new ItemValarBase().setUnlocalizedName("onyxMithrilRing").setTextureName("onyx_mithril_ring")));
        itemRegistry.addObject(6144, "tanzanite_mithril_ring", (new ItemValarBase().setUnlocalizedName("tanzaniteMithrilRing").setTextureName("tanzanite_mithril_ring")));
        itemRegistry.addObject(6145, "jade_mithril_ring", (new ItemValarBase().setUnlocalizedName("jadeMithrilRing").setTextureName("jade_mithril_ring")));
        itemRegistry.addObject(6146, "moonstone_mithril_ring", (new ItemValarBase().setUnlocalizedName("moonstoneMithrilRing").setTextureName("moonstone_mithril_ring")));
        itemRegistry.addObject(6147, "amethyst_mithril_ring", (new ItemValarBase().setUnlocalizedName("amethystMithrilRing").setTextureName("amethyst_mithril_ring")));
        itemRegistry.addObject(6148, "ruby_mithril_ring", (new ItemValarBase().setUnlocalizedName("rubyMithrilRing").setTextureName("ruby_mithril_ring")));
        itemRegistry.addObject(6149, "quartz_mithril_ring", (new ItemValarBase().setUnlocalizedName("quartzMithrilRing").setTextureName("quartz_mithril_ring")));
        itemRegistry.addObject(6150, "amber_mithril_ring", (new ItemValarBase().setUnlocalizedName("amberMithrilRing").setTextureName("amber_mithril_ring")));
        itemRegistry.addObject(6151, "salt", (new ItemValarBase().setUnlocalizedName("salt").setTextureName("salt")));
        itemRegistry.addObject(6152, "noldor_chestplate", (new ItemValarBase().setUnlocalizedName("noldorChestplate").setTextureName("noldor_chestplate")));
        itemRegistry.addObject(6153, "silver_ingot", (new ItemValarBase().setUnlocalizedName("silverIngot").setTextureName("silver_ingot")));*/
        registerItem(6154, "dwarf_door", (new ItemDoor(Blocks.DWARF_DOOR)).setUnlocalizedName("dwarfDoor"));
        /*itemRegistry.addObject(6155, "boromirs_sword", (new ItemValarWeapon(900, 9, 0, true, false).setUnlocalizedName("boromirsSword").setTextureName("boromirs_sword")));
        //itemRegistry.addObject(6156, "grey_potion", (new ItemValarBase().setUnlocalizedName("greyPotion").setTextureName("grey_potion")));
        //itemRegistry.addObject(6157, "blue_potion", (new ItemValarBase().setUnlocalizedName("bluePotion").setTextureName("blue_potion")));
        //itemRegistry.addObject(6158, "brown_potion", (new ItemValarBase().setUnlocalizedName("brownPotion").setTextureName("brown_potion")));
        //itemRegistry.addObject(6159, "dark_green_potion", (new ItemValarBase().setUnlocalizedName("darkGreenPotion").setTextureName("dark_green_potion")));
        //itemRegistry.addObject(6160, "elanor_potion", (new ItemValarBase().setUnlocalizedName("elanorPotion").setTextureName("elanor_potion")));
        //itemRegistry.addObject(6161, "light_purple_potion", (new ItemValarBase().setUnlocalizedName("lightPurplePotion").setTextureName("light_purple_potion")));
        //itemRegistry.addObject(6162, "orc_potion", (new ItemValarBase().setUnlocalizedName("orcPotion").setTextureName("orc_potion")));
        itemRegistry.addObject(6163, "orc_sword_1", (new ItemValarWeapon(230, 5, 0, true, true).setUnlocalizedName("orcSword1").setTextureName("orc_sword_1")));
        itemRegistry.addObject(6164, "orc_sword_2", (new ItemValarWeapon(230, 5).setUnlocalizedName("orcSword2").setTextureName("orc_sword_2")));
        //itemRegistry.addObject(6165, "rivendell_sword", (new ItemValarBase().setUnlocalizedName("rivendellSword").setTextureName("rivendell_sword")));
        itemRegistry.addObject(6166, "goblin_sword", (new ItemValarWeapon(200, 5).setUnlocalizedName("goblinSword").setTextureName("goblin_sword")));
        itemRegistry.addObject(6167, "hobbit_axe", (new ItemValarWeapon(235, 5, 0, true, true).setUnlocalizedName("hobbitAxe").setTextureName("hobbit_axe")));
        itemRegistry.addObject(6168, "sharkus_shortsword", (new ItemValarWeapon(467, 8).setUnlocalizedName("sharkusShortsword").setTextureName("sharkus_shortsword")));
        itemRegistry.addObject(6169, "battle_pickaxe", (new ItemValarWeapon(300, 5).setUnlocalizedName("battlePickaxe").setTextureName("battle_pickaxe")));
        itemRegistry.addObject(6170, "shirriff_club", (new ItemValarWeapon(131, 6).setUnlocalizedName("shirriffClub").setTextureName("shirriff_club")));
        itemRegistry.addObject(6171, "pike_club", (new ItemValarWeapon(350, 6).setUnlocalizedName("pikeClub").setTextureName("pike_club")));
        itemRegistry.addObject(6172, "hobbit_sword", (new ItemValarWeapon(230, 5).setUnlocalizedName("hobbitSword").setTextureName("hobbit_sword")));
        itemRegistry.addObject(6173, "hobbit_hammer", (new ItemValarWeapon(235, 5).setUnlocalizedName("hobbitHammer").setTextureName("hobbit_hammer")));
        //itemRegistry.addObject(6174, "dwarven_axe_moria", (new ItemValarBase().setUnlocalizedName("dwarvenAxeMoria").setTextureName("dwarvenaxemoria")));
        itemRegistry.addObject(6175, "green_arrow", (new ItemValarBase().setUnlocalizedName("greenArrow").setTextureName("green_arrow")));
        itemRegistry.addObject(6176, "black_arrow", (new ItemValarBase().setUnlocalizedName("blackArrow").setTextureName("black_arrow")));
        itemRegistry.addObject(6177, "goblin_arrow", (new ItemValarBase().setUnlocalizedName("goblinArrow").setTextureName("goblin_arrow")));
        itemRegistry.addObject(6179, "red_arrow", (new ItemValarBase().setUnlocalizedName("redArrow").setTextureName("red_arrow")));
        itemRegistry.addObject(6179, "yellow_arrow", (new ItemValarBase().setUnlocalizedName("yellowArrow").setTextureName("yellow_arrow")));
        //itemRegistry.addObject(6180, "throwing_rock", (new ItemValarBase().setUnlocalizedName("throwingRock").setTextureName("throwing_rock")));
        itemRegistry.addObject(6181, "boar_horns", (new ItemValarBase().setUnlocalizedName("boarHorns").setTextureName("boar_horns")));
        itemRegistry.addObject(6182, "gold_ring_of_darkness", (new ItemValarBase().setUnlocalizedName("goldRingofDarkness").setTextureName("gold_ring_of_darkness")));
        itemRegistry.addObject(6183, "gold_ring_of_earth", (new ItemValarBase().setUnlocalizedName("goldRingofEarth").setTextureName("gold_ring_of_earth")));
        itemRegistry.addObject(6184, "gold_ring_of_fire", (new ItemValarBase().setUnlocalizedName("goldRingofFire").setTextureName("gold_ring_of_fire")));
        itemRegistry.addObject(6185, "gold_ring_of_greed", (new ItemValarBase().setUnlocalizedName("goldRingofGreed").setTextureName("gold_ring_of_greed")));
        itemRegistry.addObject(6186, "gold_ring_of_hope", (new ItemValarBase().setUnlocalizedName("goldRingofHope").setTextureName("gold_ring_of_hope")));
        itemRegistry.addObject(6187, "gold_ring_of_nature", (new ItemValarBase().setUnlocalizedName("goldRingofNature").setTextureName("gold_ring_of_nature")));
        itemRegistry.addObject(6188, "gold_ring_of_sea", (new ItemValarBase().setUnlocalizedName("goldRingofSea").setTextureName("gold_ring_of_sea")));
        itemRegistry.addObject(6189, "gold_ring_of_sky", (new ItemValarBase().setUnlocalizedName("goldRingofSky").setTextureName("gold_ring_of_sky")));
        itemRegistry.addObject(6190, "gold_ring_of_sunlight", (new ItemValarBase().setUnlocalizedName("goldRingofSunlight").setTextureName("gold_ring_of_sunlight")));
        itemRegistry.addObject(6191, "gold_ring_of_wealth", (new ItemValarBase().setUnlocalizedName("goldRingofWealth").setTextureName("gold_ring_of_wealth")));
        itemRegistry.addObject(6192, "gold_ring_of_wind", (new ItemValarBase().setUnlocalizedName("goldRingofWind").setTextureName("gold_ring_of_wind")));
        itemRegistry.addObject(6193, "silver_ring_of_darkness", (new ItemValarBase().setUnlocalizedName("silverRingofDarkness").setTextureName("silver_ring_of_darkness")));
        itemRegistry.addObject(6194, "silver_ring_of_earth", (new ItemValarBase().setUnlocalizedName("silverRingofEarth").setTextureName("silver_ring_of_earth")));
        itemRegistry.addObject(6195, "silver_ring_of_fire", (new ItemValarBase().setUnlocalizedName("silverRingofFire").setTextureName("silver_ring_of_fire")));
        itemRegistry.addObject(6196, "silver_ring_of_greed", (new ItemValarBase().setUnlocalizedName("silverRingofGreed").setTextureName("silver_ring_of_greed")));
        itemRegistry.addObject(6197, "silver_ring_of_hope", (new ItemValarBase().setUnlocalizedName("silverRingofHope").setTextureName("silver_ring_of_hope")));
        itemRegistry.addObject(6198, "silver_ring_of_nature", (new ItemValarBase().setUnlocalizedName("silverRingofNature").setTextureName("silver_ring_of_nature")));
        itemRegistry.addObject(6199, "silver_ring_of_sea", (new ItemValarBase().setUnlocalizedName("silverRingofSea").setTextureName("silver_ring_of_sea")));
        itemRegistry.addObject(6200, "silver_ring_of_sky", (new ItemValarBase().setUnlocalizedName("silverRingofSky").setTextureName("silver_ring_of_sky")));
        itemRegistry.addObject(6201, "silver_ring_of_sunlight", (new ItemValarBase().setUnlocalizedName("silverRingofSunlight").setTextureName("silver_ring_of_sunlight")));
        itemRegistry.addObject(6202, "silver_ring_of_wealth", (new ItemValarBase().setUnlocalizedName("silverRingofWealth").setTextureName("silver_ring_of_wealth")));
        itemRegistry.addObject(6203, "silver_ring_of_wind", (new ItemValarBase().setUnlocalizedName("silverRingofWind").setTextureName("silver_ring_of_wind")));
        itemRegistry.addObject(6204, "bronze_ring_of_darkness", (new ItemValarBase().setUnlocalizedName("bronzeRingofDarkness").setTextureName("bronze_ring_of_darkness")));
        itemRegistry.addObject(6205, "bronze_ring_of_earth", (new ItemValarBase().setUnlocalizedName("bronzeRingofEarth").setTextureName("bronze_ring_of_earth")));
        itemRegistry.addObject(6206, "bronze_ring_of_fire", (new ItemValarBase().setUnlocalizedName("bronzeRingofFire").setTextureName("bronze_ring_of_fire")));
        itemRegistry.addObject(6207, "bronze_ring_of_greed", (new ItemValarBase().setUnlocalizedName("bronzeRingofGreed").setTextureName("bronze_ring_of_greed")));
        itemRegistry.addObject(6208, "bronze_ring_of_hope", (new ItemValarBase().setUnlocalizedName("bronzeRingofHope").setTextureName("bronze_ring_of_hope")));
        itemRegistry.addObject(6209, "bronze_ring_of_nature", (new ItemValarBase().setUnlocalizedName("bronzeRingofNature").setTextureName("bronze_ring_of_nature")));
        itemRegistry.addObject(6210, "bronze_ring_of_sea", (new ItemValarBase().setUnlocalizedName("bronzeRingofSea").setTextureName("bronze_ring_of_sea")));
        itemRegistry.addObject(6211, "bronze_ring_of_sky", (new ItemValarBase().setUnlocalizedName("bronzeRingofSky").setTextureName("bronze_ring_of_sky")));
        itemRegistry.addObject(6212, "bronze_ring_of_sunlight", (new ItemValarBase().setUnlocalizedName("bronzeRingofSunlight").setTextureName("bronze_ring_of_sunlight")));
        itemRegistry.addObject(6213, "bronze_ring_of_wealth", (new ItemValarBase().setUnlocalizedName("bronzeRingofWealth").setTextureName("bronze_ring_of_wealth")));
        itemRegistry.addObject(6214, "bronze_ring_of_wind", (new ItemValarBase().setUnlocalizedName("bronzeRingofWind").setTextureName("bronze_ring_of_wind")));
        itemRegistry.addObject(6215, "mithril_ring_of_darkness", (new ItemValarBase().setUnlocalizedName("mithrilRingofDarkness").setTextureName("mithril_ring_of_darkness")));
        itemRegistry.addObject(6216, "mithril_ring_of_earth", (new ItemValarBase().setUnlocalizedName("mithrilRingofEarth").setTextureName("mithril_ring_of_earth")));
        itemRegistry.addObject(6217, "mithril_ring_of_fire", (new ItemValarBase().setUnlocalizedName("mithrilRingofFire").setTextureName("mithril_ring_of_fire")));
        itemRegistry.addObject(6218, "mithril_ring_of_greed", (new ItemValarBase().setUnlocalizedName("mithrilRingofGreed").setTextureName("mithril_ring_of_greed")));
        itemRegistry.addObject(6219, "mithril_ring_of_hope", (new ItemValarBase().setUnlocalizedName("mithrilRingofHope").setTextureName("mithril_ring_of_hope")));
        itemRegistry.addObject(6220, "mithril_ring_of_nature", (new ItemValarBase().setUnlocalizedName("mithrilRingofNature").setTextureName("mithril_ring_of_nature")));
        itemRegistry.addObject(6221, "mithril_ring_of_sea", (new ItemValarBase().setUnlocalizedName("mithrilRingofSea").setTextureName("mithril_ring_of_sea")));
        itemRegistry.addObject(6222, "mithril_ring_of_sky", (new ItemValarBase().setUnlocalizedName("mithrilRingofSky").setTextureName("mithril_ring_of_sky")));
        itemRegistry.addObject(6223, "mithril_ring_of_sunlight", (new ItemValarBase().setUnlocalizedName("mithrilRingofSunlight").setTextureName("mithril_ring_of_sunlight")));
        itemRegistry.addObject(6224, "mithril_ring_of_wealth", (new ItemValarBase().setUnlocalizedName("mithrilRingofWealth").setTextureName("mithril_ring_of_wealth")));
        itemRegistry.addObject(6225, "mithril_ring_of_wind", (new ItemValarBase().setUnlocalizedName("mithrilRingofWind").setTextureName("mithril_ring_of_wind")));
        itemRegistry.addObject(6226, "anduril", (new ItemValarWeapon(3000, 11, 0, true, false).setUnlocalizedName("anduril").setTextureName("anduril")));
        itemRegistry.addObject(6227, "morgul_sword", (new ItemValarWeapon(2500, 9).setUnlocalizedName("morgulSword").setTextureName("morgul_sword")));
        itemRegistry.addObject(6228, "elven_long_sword", (new ItemValarWeapon(1800, 7).setUnlocalizedName("elvenLongSword").setTextureName("elven_long_sword")));
        itemRegistry.addObject(6229, "berserker_sword", (new ItemValarWeapon(1400, 8).setUnlocalizedName("berserkerSword").setTextureName("berserker_sword")));
        itemRegistry.addObject(6230, "two_handed_sword", (new ItemValarWeapon(500, 7).setUnlocalizedName("twohandedSword").setTextureName("two_handed_sword")));
        //itemRegistry.addObject(6231, "green_flag", (new ItemValarBase().setUnlocalizedName("greenFlag").setTextureName("green_flag")));
        //itemRegistry.addObject(6232, "hobbit_bow", (new ItemValarBase().setUnlocalizedName("hobbitBow").setTextureName("hobbit_bow")));
        itemRegistry.addObject(6233, "rohan_sheild_6", (new ItemValarBase().setUnlocalizedName("rohanShield6").setTextureName("rohan_shield_6")));
        //itemRegistry.addObject(6234, "throwing_stone", (new ItemValarBase().setUnlocalizedName("throwingStone").setTextureName("throwing_stone")));
        itemRegistry.addObject(6235, "rohirrim_axe", (new ItemValarWeapon(267, 6, 0, true, true).setUnlocalizedName("rohirrimAxe").setTextureName("rohirrim_axe")));
        itemRegistry.addObject(6236, "ecthelions_boots", (new ItemValarArmor(3, 500, 3, 0).setUnlocalizedName("ecthelionsBoots").setTextureName("ecthelions_boots")));
        //itemRegistry.addObject(3237, "gondorian_brick_stone_slab_item", (new ItemValarBase().setUnlocalizedName("gondorianBrickStoneSlabItem").setTextureName("gondorian_brick_stone_slab_item")));
        //itemRegistry.addObject(6238, "a_knife_in_the_dark", (new ItemValarBase().setUnlocalizedName("aKnifeintheDark").setTextureName("a_knife_in_the_dark")));*/
        registerItem(6239, "elven_door", (new ItemDoor(Blocks.ELVEN_DOOR)).setUnlocalizedName("elvenDoor"));
        registerItem(6240, "human_door", (new ItemDoor(Blocks.HUMAN_DOOR)).setUnlocalizedName("humanDoor"));
        /*itemRegistry.addObject(6241, "green_bed_item", (new ItemValarBed((BlockValarBed)Blocks.greenBed).setUnlocalizedName("greenBedItem").setTextureName("green_bed_item")));
        //itemRegistry.addObject(6242, "one_ring", (new ItemValarBase().setUnlocalizedName("oneRing").setTextureName("one_ring")));
        itemRegistry.addObject(6243, "aicanar", (new ItemValarWeapon(6000, 10).setUnlocalizedName("aicanar").setTextureName("aicanar")));
        itemRegistry.addObject(6244, "balins_sword", (new ItemValarWeapon(1600, 7).setUnlocalizedName("balinsSword").setTextureName("balins_sword")));
        itemRegistry.addObject(6245, "bifurs_spear", (new ItemValarWeapon(1102, 6, 0, true, true).setUnlocalizedName("bifursSpear").setTextureName("bifurs_spear")));
        //itemRegistry.addObject(6246, "bofurs_spear", (new ItemValarWeapon(1182, 6).setUnlocalizedName("bofursSpear").setTextureName("bofurs_spear")));
        itemRegistry.addObject(6247, "bomburs_spoon", (new ItemValarWeapon(321, 4).setUnlocalizedName("bombursSpoon").setTextureName("bomburs_spoon")));
        itemRegistry.addObject(6248, "doris_sword", (new ItemValarWeapon(1500, 5).setUnlocalizedName("dorisSword").setTextureName("doris_sword")));
        itemRegistry.addObject(6249, "dwalins_axe", (new ItemValarWeapon(1700, 7).setUnlocalizedName("dwalinsAxe").setTextureName("dwalins_axe")));
        itemRegistry.addObject(6250, "filis_knife", (new ItemValarWeapon(112, 4).setUnlocalizedName("filisKnife").setTextureName("filis_knife")));
        itemRegistry.addObject(6251, "filis_sword", (new ItemValarWeapon(1111, 6).setUnlocalizedName("filisSword").setTextureName("filis_sword")));
        itemRegistry.addObject(6252, "kilis_sword", (new ItemValarWeapon(1111, 6).setUnlocalizedName("kilisSword").setTextureName("kilis_sword")));
        itemRegistry.addObject(6253, "noris_club", (new ItemValarWeapon(861, 5).setUnlocalizedName("norisClub").setTextureName("noris_club")));
        itemRegistry.addObject(6254, "oins_staff", (new ItemValarWeapon(922, 6).setUnlocalizedName("oinsStaff").setTextureName("oins_staff")));
        itemRegistry.addObject(6255, "orcrist", (new ItemValarWeapon(8001, 12).setUnlocalizedName("orcrist").setTextureName("orcrist")));
        itemRegistry.addObject(6256, "sword_of_westernesse", (new ItemValarWeapon(3200, 8).setUnlocalizedName("swordOfWesternesse").setTextureName("sword_of_westernesse")));
        itemRegistry.addObject(6257, "thorins_sword", (new ItemValarWeapon(1200, 6).setUnlocalizedName("thorinsSword").setTextureName("thorins_sword")));
        itemRegistry.addObject(6258, "thrains_warhammer", (new ItemValarWeapon(1288, 6).setUnlocalizedName("thrainsWarhammer").setTextureName("thrains_warhammer")));
        itemRegistry.addObject(6259, "thrors_warhammer", (new ItemValarWeapon(1428, 7).setUnlocalizedName("throrsWarhammer").setTextureName("thrors_warhammer")));
        itemRegistry.addObject(6260, "aragorns_elf_knife", (new ItemValarWeapon(1300, 5, 0, true, true).setUnlocalizedName("aragornsElfKnife").setTextureName("aragorns_elf_knife")));
        itemRegistry.addObject(6261, "eowyns_sword", (new ItemValarWeapon(1261, 6).setUnlocalizedName("eowynsSword").setTextureName("eowyns_sword")));
        itemRegistry.addObject(6262, "gandalf_the_greys_staff", (new ItemValarWeapon(3000, 9).setUnlocalizedName("gandalftheGreysStaff").setTextureName("gandalf_the_greys_staff")));
        itemRegistry.addObject(6263, "gandalf_the_whites_staff", (new ItemValarWeapon(5000, 11).setUnlocalizedName("gandalftheWhitesStaff").setTextureName("gandalf_the_whites_staff")));
        itemRegistry.addObject(6264, "gimlis_two-headed_axe", (new ItemValarWeapon(2000, 9).setUnlocalizedName("gimlisTwo-HeadedAxe").setTextureName("gimlis_two-headed_axe")));
        itemRegistry.addObject(6265, "gimlis_longaxe", (new ItemValarWeapon(1700, 7).setUnlocalizedName("gimlisLongaxe").setTextureName("gimlis_longaxe")));
        itemRegistry.addObject(6266, "glamdring", (new ItemValarWeapon(3200, 11).setUnlocalizedName("glamdring").setTextureName("glamdring")));
        itemRegistry.addObject(6267, "gloins_axe", (new ItemValarWeapon(1700, 7).setUnlocalizedName("gloinsAxe").setTextureName("gloins_axe")));
        itemRegistry.addObject(6268, "glorfindels_sword", (new ItemValarWeapon(3600, 9).setUnlocalizedName("glorfindelsSword").setTextureName("glorfindels_sword")));
        itemRegistry.addObject(6269, "guthwine", (new ItemValarWeapon(1732, 8).setUnlocalizedName("guthwine").setTextureName("guthwine")));
        itemRegistry.addObject(6270, "hadhafang", (new ItemValarWeapon(3400, 10).setUnlocalizedName("hadhafang").setTextureName("hadhafang")));
        //itemRegistry.addObject(6271, "herugrim", (new ItemValarBase().setUnlocalizedName("herugrim").setTextureName("herugrim")));
        itemRegistry.addObject(6272, "legolas_sword", (new ItemValarWeapon(2900, 9, 0, true, true).setUnlocalizedName("legolasSword").setTextureName("legolas_sword")));
        itemRegistry.addObject(6273, "radagasts_staff", (new ItemValarWeapon(3100, 8, 0, true, true).setUnlocalizedName("radagast'sStaff").setTextureName("radagasts_staff")));
        itemRegistry.addObject(6274, "lurtz_sword", (new ItemValarWeapon(1870, 8).setUnlocalizedName("lurtzSword").setTextureName("lurtz_sword")));
        itemRegistry.addObject(6275, "saurons_mace", (new ItemValarWeapon(7500, 13).setUnlocalizedName("sauronsMace").setTextureName("saurons_mace")));
        itemRegistry.addObject(6276, "sarumans_staff", (new ItemValarWeapon(4500, 10).setUnlocalizedName("sarumansStaff").setTextureName("sarumans_staff")));
        itemRegistry.addObject(6277, "yaznegs_axe", (new ItemValarWeapon(830, 6, 0, true, true).setUnlocalizedName("yaznegsAxe").setTextureName("yaznegs_axe")));
        itemRegistry.addObject(6278, "dwarven_lord_knife", (new ItemValarWeapon(1200, 4).setUnlocalizedName("dwarvenLordKnife").setTextureName("dwarven_lord_knife")));
        itemRegistry.addObject(6279, "gondorian_noble_sword", (new ItemValarWeapon(1400, 7).setUnlocalizedName("gondorianNobleSword").setTextureName("gondorian_noble_sword")));
        itemRegistry.addObject(6280, "mace_of_glory", (new ItemValarWeapon(2100, 8).setUnlocalizedName("maceOfGlory").setTextureName("mace_of_glory")));
        itemRegistry.addObject(6281, "golden_war_hammer_of_erebor", (new ItemValarWeapon(2800, 7).setUnlocalizedName("goldenWarHammerofErebor").setTextureName("golden_war_hammer_of_erebor")));
        itemRegistry.addObject(6282, "gondorian_spear", (new ItemValarWeapon(1000, 6).setUnlocalizedName("gondorianSpear").setTextureName("gondorian_spear")));
        itemRegistry.addObject(6283, "rohirrim_spear", (new ItemValarWeapon(1040, 6).setUnlocalizedName("rohirrimSpear").setTextureName("rohirrim_spear")));
        itemRegistry.addObject(6284, "bull-head_mace", (new ItemValarWeapon(1091, 6).setUnlocalizedName("bull-HeadMace").setTextureName("bull-head_mace")));
        itemRegistry.addObject(6285, "corsair_eket", (new ItemValarWeapon(1133, 7).setUnlocalizedName("corsairEket").setTextureName("corsair_eket")));
        itemRegistry.addObject(6286, "dol_guldur_spiked_mace", (new ItemValarWeapon(981, 6).setUnlocalizedName("dolGuldurSpikedMace").setTextureName("dol_guldur_spiked_mace")));
        itemRegistry.addObject(6287, "fell-wargrider_sword", (new ItemValarWeapon(991, 6, 0, true, true).setUnlocalizedName("fell-WargriderSword").setTextureName("fell-wargrider_sword")));
        itemRegistry.addObject(6288, "haradrim_snake_dagger", (new ItemValarWeapon(777, 5).setUnlocalizedName("haradrimSnakeDagger").setTextureName("haradrim_snake_dagger")));
        itemRegistry.addObject(6289, "north-goblin_mace", (new ItemValarWeapon(531, 5).setUnlocalizedName("north-GoblinMace").setTextureName("north-goblin_mace")));
        itemRegistry.addObject(6290, "north-goblin_sword", (new ItemValarWeapon(772, 5).setUnlocalizedName("north-GoblinSword").setTextureName("north-goblin_sword")));
        itemRegistry.addObject(6291, "orc_captain_sword", (new ItemValarWeapon(1130, 7).setUnlocalizedName("orcCaptainSword").setTextureName("orc_captain_sword")));
        itemRegistry.addObject(6292, "orc_dagger", (new ItemValarWeapon(103, 4).setUnlocalizedName("orcDagger").setTextureName("orc_dagger")));
        itemRegistry.addObject(6293, "orc_halbard", (new ItemValarWeapon(605, 7, 0, true, true).setUnlocalizedName("orcHalbard").setTextureName("orc_halbard")));
        itemRegistry.addObject(6294, "uruk_siege-trooper_dagger", (new ItemValarWeapon(1041, 5).setUnlocalizedName("urukSiege-TrooperDagger").setTextureName("uruk_siege-trooper_dagger")));
        //itemRegistry.addObject(6295, "orc_spawner", (new ItemValarBase().setUnlocalizedName("orcSpawner").setTextureName("orc_spawner")));
        itemRegistry.addObject(6296, "iron_mace", (new ItemValarWeapon(387, 7).setUnlocalizedName("ironMace").setTextureName("iron_mace")));
        itemRegistry.addObject(6297, "orc_sword_3", (new ItemValarWeapon(837, 6).setUnlocalizedName("orcSword3").setTextureName("orc_sword_3")));
        itemRegistry.addObject(6298, "morannon_orc_shield", (new ItemValarBase().setUnlocalizedName("morannonOrcShield").setTextureName("morannon_orc_shield")));
        itemRegistry.addObject(6299, "morannon_dagger", (new ItemValarWeapon(99, 3).setUnlocalizedName("morannonDagger").setTextureName("morannon_dagger")));
        itemRegistry.addObject(6300, "orc_spear", (new ItemValarWeapon(123, 6).setUnlocalizedName("orcSpear").setTextureName("orc_spear")));
        itemRegistry.addObject(6301, "net", (new ItemValarBase().setUnlocalizedName("net").setTextureName("net")));
        //itemRegistry.addObject(6302, "erus_staff", (new ItemValarErusStaff()).setUnlocalizedName("erusStaff").setTextureName("erus_staff"));*/
        registerItem(6303, "castle_door", (new ItemDoor(Blocks.CASTLE_DOOR)).setUnlocalizedName("castleDoor"));
        registerItem(6304, "dol_guldur_prison_door", (new ItemDoor(Blocks.DOL_GULDUR_PRISON_DOOR)).setUnlocalizedName("dolGuldurPrisonDoor"));
        registerItem(6305, "hillmen_door", (new ItemDoor(Blocks.HILLMEN_DOOR)).setUnlocalizedName("hillmenDoor"));
        registerItem(6306, "mordor_door", (new ItemDoor(Blocks.MORDOR_DOOR)).setUnlocalizedName("mordorDoor"));
        registerItem(6307, "prison_door", (new ItemDoor(Blocks.PRISON_DOOR)).setUnlocalizedName("prisonDoor"));
        registerItem(6308, "sindar_door", (new ItemDoor(Blocks.SINDAR_DOOR)).setUnlocalizedName("sindarDoor"));
        /*itemRegistry.addObject(6309, "southlinch_seed", (new ItemValarCrops((BlockValarCrops)Blocks.southlinch)).setUnlocalizedName("southlinchSeed").setTextureName("southlinch_seed"));
        itemRegistry.addObject(6310, "green_grape_seed", (new ItemValarCrops((BlockValarCrops)Blocks.greenGrape)).setUnlocalizedName("greenGrapeSeed").setTextureName("green_grape_seed"));
        itemRegistry.addObject(6311, "green_grapes", (new ItemValarFood(2, 0.3F, false)).setUnlocalizedName("greenGrapes").setTextureName("green_grapes"));
        itemRegistry.addObject(6312, "purple_grape_seed", (new ItemValarCrops((BlockValarCrops)Blocks.purpleGrape)).setUnlocalizedName("purpleGrapeSeed").setTextureName("purple_grape_seed"));
        itemRegistry.addObject(6313, "purple_grapes", (new ItemValarFood(2, 0.3F, false)).setUnlocalizedName("purpleGrapes").setTextureName("purple_grapes"));
        itemRegistry.addObject(6314, "pipeweed_seed", (new ItemValarCrops((BlockValarCrops)Blocks.pipeweedPlant)).setUnlocalizedName("pipeweedSeed").setTextureName("pipeweed_seed"));
        itemRegistry.addObject(6315, "pea_seed", (new ItemValarCrops((BlockValarCrops)Blocks.peaPlant)).setUnlocalizedName("peaSeed").setTextureName("pea_seed"));
        itemRegistry.addObject(6316, "peas", (new ItemValarFood(1, 0.6F, false)).setUnlocalizedName("peas").setTextureName("peas"));
        itemRegistry.addObject(6317, "leek", (new ItemValarFood(3, 0.6F, false)).setUnlocalizedName("leek").setTextureName("leek"));
        itemRegistry.addObject(6318, "leek_seed", (new ItemValarCrops((BlockValarCrops)Blocks.leekPlant)).setUnlocalizedName("leekSeed").setTextureName("leek_seed"));
        itemRegistry.addObject(6319, "onion_seed", (new ItemValarCrops((BlockValarCrops)Blocks.onionPlant)).setUnlocalizedName("onionSeed").setTextureName("onion_seed"));
        itemRegistry.addObject(6320, "onion", (new ItemValarFood(3, 0.6F, false)).setUnlocalizedName("onion").setTextureName("onion"));
        itemRegistry.addObject(6321, "old_toby_seed", (new ItemValarCrops((BlockValarCrops)Blocks.oldTobyPlant)).setUnlocalizedName("oldTobySeed").setTextureName("old_toby_seed"));
        itemRegistry.addObject(6322, "southern_star_seed", (new ItemValarCrops((BlockValarCrops)Blocks.southernStarPlant)).setUnlocalizedName("southernStarSeed").setTextureName("southern_star_seed"));
        itemRegistry.addObject(6323, "strawberry_seed", (new ItemValarCrops((BlockValarCrops)Blocks.strawberryBush)).setUnlocalizedName("strawberrySeed").setTextureName("strawberry_seed"));
        itemRegistry.addObject(6324, "azogs_mace", (new ItemValarWeapon(3500, 7, 0, true, false)).setUnlocalizedName("azogsMace").setTextureName("azogs_mace"));
        // Temporary Id, change to proper id when adding items
        //itemRegistry.addObject(10002, "rucksack", (new ItemValarRucksack()).setUnlocalizedName("rucksack").setTextureName("rucksack"));
        //itemRegistry.addObject(10003, "frodos_finger", (new ItemValarFood(2, 0.1F, true).setUnlocalizedName("frodosFinger").setTextureName("frodos_finger")));
        //itemRegistry.addObject(10004, "two_handed_sword", (new ItemValarWeapon(1, 20, 0, true).setUnlocalizedName("twoHandedSword").setTextureName("two_handed_sword")));
        //itemRegistry.addObject(10005, "easterling_helmet", (new ItemValarArmor(0, 1000, 2, 0).setUnlocalizedName("easterlingHelmet").setTextureName("easterling_helmet")));
        //itemRegistry.addObject(10006, "miruvoir", (new ItemValarDrinkable(Items.glass_bottle).setUnlocalizedName("miruvoir").setTextureName("miruvoir")));*/
        // End Awaken Dreams code
    }

    /**
     * Register a default ItemBlock for the given Block.
     */
    private static void registerItemBlock(Block blockIn)
    {
        registerItemBlock(blockIn, new ItemBlock(blockIn));
    }

    /**
     * Register the given Item as the ItemBlock for the given Block.
     */
    protected static void registerItemBlock(Block blockIn, Item itemIn)
    {
        registerItem(Block.getIdFromBlock(blockIn), (ResourceLocation)Block.REGISTRY.getNameForObject(blockIn), itemIn);
        BLOCK_TO_ITEM.put(blockIn, itemIn);
    }

    private static void registerItem(int id, String textualID, Item itemIn)
    {
        registerItem(id, new ResourceLocation(textualID), itemIn);
    }

    private static void registerItem(int id, ResourceLocation textualID, Item itemIn)
    {
        REGISTRY.register(id, textualID, itemIn);
    }

    public static enum ToolMaterial
    {
        WOOD(0, 59, 2.0F, 0.0F, 15),
        STONE(1, 131, 4.0F, 1.0F, 5),
        IRON(2, 250, 6.0F, 2.0F, 14),
        DIAMOND(3, 1561, 8.0F, 3.0F, 10),
        GOLD(0, 32, 12.0F, 0.0F, 22);

        private final int harvestLevel;
        private final int maxUses;
        private final float efficiencyOnProperMaterial;
        private final float damageVsEntity;
        private final int enchantability;

        private ToolMaterial(int harvestLevel, int maxUses, float efficiency, float damageVsEntity, int enchantability)
        {
            this.harvestLevel = harvestLevel;
            this.maxUses = maxUses;
            this.efficiencyOnProperMaterial = efficiency;
            this.damageVsEntity = damageVsEntity;
            this.enchantability = enchantability;
        }

        public int getMaxUses()
        {
            return this.maxUses;
        }

        public float getEfficiencyOnProperMaterial()
        {
            return this.efficiencyOnProperMaterial;
        }

        public float getDamageVsEntity()
        {
            return this.damageVsEntity;
        }

        public int getHarvestLevel()
        {
            return this.harvestLevel;
        }

        public int getEnchantability()
        {
            return this.enchantability;
        }

        public Item getRepairItem()
        {
            return this == WOOD ? Item.getItemFromBlock(Blocks.PLANKS) : (this == STONE ? Item.getItemFromBlock(Blocks.COBBLESTONE) : (this == GOLD ? Items.GOLD_INGOT : (this == IRON ? Items.IRON_INGOT : (this == DIAMOND ? Items.DIAMOND : null))));
        }
    }
}
