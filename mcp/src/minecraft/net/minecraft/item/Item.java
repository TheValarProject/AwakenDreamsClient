package net.minecraft.item;

import com.elementfx.tvp.ad.block.BlockCustomBed;
import com.elementfx.tvp.ad.block.BlockCustomCrops;
import com.elementfx.tvp.ad.item.ItemCustomBed;
import com.elementfx.tvp.ad.item.ItemCustomFood;
import com.elementfx.tvp.ad.item.ItemWeapon;
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
        registerItemBlock(Blocks.HOPPER_MUSHROOM);
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
        registerItemBlock(Blocks.BAKRUEL);
        registerItemBlock(Blocks.HOPPERFOOT);
        registerItemBlock(Blocks.MADARCH);
        registerItemBlock(Blocks.MEHAS);
        registerItemBlock(Blocks.ARFANDAS);
        registerItemBlock(Blocks.ATHELAS);
        registerItemBlock(Blocks.BELLIS);
        registerItemBlock(Blocks.LAMP);
        registerItemBlock(Blocks.GONDORIAN_STONE);
        registerItemBlock(Blocks.GONDORIAN_BRICK_STONE);
        registerItemBlock(Blocks.CRACKED_GONDORIAN_BRICK_STONE);
        registerItemBlock(Blocks.LORIEN_LAMP);
        registerItemBlock(Blocks.BUCKLEBURY_LAMP);
        registerItemBlock(Blocks.SALT_ORE);
        registerItemBlock(Blocks.SIMBELMYNE);
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
        registerItemBlock(Blocks.ROHIRRIM_OAK_PLANKS);
        registerItemBlock(Blocks.ROHIRRIM_SPRUCE_PLANKS);
        registerItemBlock(Blocks.ROHIRRIM_BIRCH_PLANKS);
        registerItemBlock(Blocks.ROHIRRIM_JUNGLE_PLANKS);
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
        registerItemBlock(Blocks.STAKES);
        registerItemBlock(Blocks.SHIRE_GARDEN_FLOWER);
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
        registerItem(6000, "lembas", (new ItemCustomFood(8, 0.8F, false)).setUnlocalizedName("lembas"));
        registerItem(6001, "mithril_ingot", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("mithrilIngot"));
        registerItem(6002, "bronze_ingot", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("bronzeIngot"));
        //registerItem(6003, "pipe", (new Item()).setCreativeTab().setUnlocalizedName("pipe"));
        //registerItem(6004, "tobacco", (new Item()).setCreativeTab().setUnlocalizedName("tobacco"));
        //registerItem(6005, "flaming_arrow", (new Item()).setCreativeTab().setUnlocalizedName("flamingArrow"));
        registerItem(6008, "jade", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("jade"));
        registerItem(6009, "amber", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("amber"));
        //registerItem(6010, "parchment", (new Item()).setCreativeTab().setUnlocalizedName("parchment"));
        registerItem(6014, "crystal", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("crystal"));
        registerItem(6015, "amethyst", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("amethyst"));
        registerItem(6016, "onyx", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("onyx"));
        registerItem(6017, "moonstone", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("moonstone"));
        registerItem(6019, "tanzanite", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("tanzanite"));
        registerItem(6020, "uruk_sword", (new ItemWeapon(510, 7)).setUnlocalizedName("urukSword"));
        //registerItem(6021, "phial_of_galadriel", (new ItemValarBase().setUnlocalizedName("phialOfGaladriel"));
        registerItem(6022, "tomato", (new ItemCustomFood(1, 0.1F, false)).setUnlocalizedName("tomato"));
        registerItem(6023, "blue_feather", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("blueFeather"));
        registerItem(6026, "spider_poison", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("spiderPoison"));
        registerItem(6028, "herugrim", (new ItemWeapon(830, 7)).setUnlocalizedName("herugrim"));
        registerItem(6029, "nuts", (new ItemCustomFood(1, 0.2F, false)).setUnlocalizedName("nuts"));
        registerItem(6032, "corn_cob", (new ItemCustomFood(2, 0.3F, false)).setUnlocalizedName("cornCob"));
        registerItem(6035, "staff_base", (new ItemWeapon(350, 2)).setUnlocalizedName("staffBase"));
        registerItem(6036, "knife", (new ItemWeapon(35, 4)).setUnlocalizedName("knife"));
        registerItem(6038, "berries", (new ItemCustomFood(2, 0.3F, false)).setUnlocalizedName("berries"));
        //registerItem(6039, "gondorian_horn", (new ItemValarInstrument("").setUnlocalizedName("gondorianHorn").setTextureName("gondorian_horn")));
        registerItem(6040, "bear_claw", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("bearClaw"));
        registerItem(6041, "beast_skin", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("beastSkin"));
        registerItem(6042, "dead_root", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("deadRoot"));
        registerItem(6043, "eagle_feather", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("eagleFeather"));
        registerItem(6044, "goblin_eye", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("goblinEye"));
        registerItem(6045, "helegrog_deamon_heart", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("helegrogDeamonHeart"));
        registerItem(6046, "orc_blood", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("orcBlood"));
        registerItem(6047, "owl_feather", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("owlFeather"));
        registerItem(6048, "scorpion_tail", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("scorpionTail"));
        registerItem(6049, "shark_tooth", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("sharkTooth"));
        registerItem(6050, "troll_skin", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("trollSkin"));
        registerItem(6051, "warg_fang", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("wargFang"));
        registerItem(6052, "blue_bed", (new ItemCustomBed((BlockCustomBed)Blocks.BLUE_BED)).setUnlocalizedName("blueBed"));
        registerItem(6053, "brown_bed", (new ItemCustomBed((BlockCustomBed)Blocks.BROWN_BED)).setUnlocalizedName("brownBed"));
        //registerItem(6054, "brown_hobbit_hat", (new ItemValarArmor(0, 1, 55, 0).setUnlocalizedName("brownHobbitHat").setTextureName("brown_hobbit_hat")));
        //registerItem(6055, "orc_helmet", (new ItemValarArmor(0, 1, 200, 0).setUnlocalizedName("orcHelmet").setTextureName("orc_helmet")));
        registerItem(6056, "bree_door", (new ItemDoor(Blocks.BREE_DOOR)).setUnlocalizedName("breeDoor"));
        //registerItem(6057, "rucksack", (new ItemValarBase().setUnlocalizedName("rucksack").setTextureName("rucksack")));
        registerItem(6058, "cram", (new ItemCustomFood(2, 0.8F, false)).setUnlocalizedName("cram"));
        registerItem(6059, "cabbage", (new ItemCustomFood(3, 0.6F, false)).setUnlocalizedName("cabbage"));
        registerItem(6060, "coconut", (new ItemCustomFood(1, 0.3F, false)).setUnlocalizedName("coconut"));
        registerItem(6061, "raw_deer_meat", (new ItemCustomFood(3, 0.4F, false)).setUnlocalizedName("rawDeerMeat"));
        registerItem(6062, "cooked_deer_meat", (new ItemCustomFood(5, 0.6F, false)).setUnlocalizedName("cookedDeerMeat"));
        registerItem(6063, "muffin", (new ItemCustomFood(1, 0.4F, false)).setUnlocalizedName("muffin"));
        registerItem(6064, "strawberry", (new ItemCustomFood(2, 0.3F, false)).setUnlocalizedName("strawberry").setCreativeTab(CreativeTabs.FOOD));
        registerItem(6065, "strawberry_muffin", (new ItemCustomFood(3, 0.6F, false)).setUnlocalizedName("strawberryMuffin"));
        registerItem(6066, "orange", (new ItemCustomFood(2, 0.6F, false)).setUnlocalizedName("orange"));
        registerItem(6067, "orange_muffin", (new ItemCustomFood(3, 0.6F, false)).setUnlocalizedName("orangeMuffin"));
        registerItem(6068, "pear", (new ItemCustomFood(2, 0.6F, false)).setUnlocalizedName("pear"));
        registerItem(6069, "raw_meat", (new ItemCustomFood(2, 0.2F, false)).setUnlocalizedName("rawMeat"));
        registerItem(6070, "cooked_meat", (new ItemCustomFood(5, 0.7F, false)).setUnlocalizedName("cookedMeat"));
        //registerItem(6071, "rohan_helm_1", (new ItemValarBase().setUnlocalizedName("rohanHelm1").setTextureName("rohan_helm_1")));
        //registerItem(6072, "rohan_helm_2", (new ItemValarBase().setUnlocalizedName("rohanHelm2").setTextureName("rohan_helm_2")));
        //registerItem(6073, "rohan_helm_3", (new ItemValarBase().setUnlocalizedName("rohanHelm3").setTextureName("rohan_helm_3")));
        //registerItem(6074, "rohan_helm_4", (new ItemValarBase().setUnlocalizedName("rohanHelm4").setTextureName("rohan_helm_4")));
        //registerItem(6075, "rohan_helm_5", (new ItemValarBase().setUnlocalizedName("rohanHelm5").setTextureName("rohan_helm_5")));
        //registerItem(6076, "rohan_helm_6", (new ItemValarBase().setUnlocalizedName("rohanHelm6").setTextureName("rohan_helm_6")));
        //registerItem(6077, "rohan_chestplate", (new ItemValarBase().setUnlocalizedName("rohanChestplate").setTextureName("rohan_chestplate")));
        //registerItem(6080, "telescope", (new ItemValarTelescope().setUnlocalizedName("telescope").setTextureName("telescope")));
        //registerItem(6081, "stone_inscription", (new ItemValarStoneInscription()).setUnlocalizedName("stoneInscription").setTextureName("stone_inscription"));
        //registerItem(6082, "sting", (new ItemValarSting().setUnlocalizedName("sting").setTextureName("sting")));
        registerItem(6085, "human_dagger", (new ItemWeapon(50, 4)).setUnlocalizedName("humanDagger"));
        registerItem(6086, "hobbit_dagger", (new ItemWeapon(50, 4)).setUnlocalizedName("hobbitDagger"));
        registerItem(6087, "elf_dagger", (new ItemWeapon(80, 5)).setUnlocalizedName("elfDagger"));
        registerItem(6088, "gondorian_sword", (new ItemWeapon(350, 6)).setUnlocalizedName("gondorianSword"));
        //registerItem(6089, "broken_narsil", (new ItemValarWeapon(2000, 10).setUnlocalizedName("brokenNarsil").setTextureName("broken_narsil")));
        //registerItem(6090, "poison_potion", (new ItemValarBase().setUnlocalizedName("poisonPotion").setTextureName("poison_potion")));
        //registerItem(6091, "healing_potion", (new ItemValarBase().setUnlocalizedName("healingPotion").setTextureName("healing_potion")));
        //registerItem(6092, "light_blue_potion", (new ItemValarBase().setUnlocalizedName("lightBluePotion").setTextureName("light_blue_potion")));
        //registerItem(6093, "miruvor", (new ItemValarBase().setUnlocalizedName("miruvor").setTextureName("miruvor")));
        //registerItem(6094, "orange_potion", (new ItemValarBase().setUnlocalizedName("orangePotion").setTextureName("orange_potion")));
        //registerItem(6095, "red_potion", (new ItemValarBase().setUnlocalizedName("redPotion").setTextureName("red_potion")));
        //registerItem(6096, "yellow_potion", (new ItemValarBase().setUnlocalizedName("yellowPotion").setTextureName("yellow_potion")));*/
        registerItem(6097, "southern_star", (new Item()).setUnlocalizedName("southernStar").setCreativeTab(CreativeTabs.MATERIALS));
        registerItem(6098, "southlinch", (new Item()).setUnlocalizedName("southlinch").setCreativeTab(CreativeTabs.MATERIALS));
        //registerItem(6099, "gondorian_tobacco", (new ItemValarBase().setUnlocalizedName("gondorianTobacco").setTextureName("gondorian_tobacco")));
        //registerItem(6100, "longbottom_leaf", (new ItemValarBase().setUnlocalizedName("longbottomLeaf").setTextureName("longbottom_leaf")));
        registerItem(6101, "old_toby", (new Item()).setUnlocalizedName("oldToby").setCreativeTab(CreativeTabs.MATERIALS));
        //registerItem(6102, "brown_pipe", (new ItemValarBase()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("brownPipe"));
        registerItem(6103, "stone_of_darkness", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfDarkness"));
        registerItem(6104, "stone_of_earth", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfEarth"));
        registerItem(6105, "stone_of_fire", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfFire"));
        registerItem(6106, "stone_of_greed", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfGreed"));
        registerItem(6107, "stone_of_hope", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfHope"));
        registerItem(6108, "stone_of_nature", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfNature"));
        registerItem(6109, "stone_of_sunlight", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfSunlight"));
        registerItem(6110, "stone_of_sea", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfSea"));
        registerItem(6111, "stone_of_sky", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfSky"));
        registerItem(6112, "stone_of_wealth", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfWealth"));
        registerItem(6113, "stone_of_wind", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("stoneOfWind"));
        registerItem(6114, "hammer", (new Item()).setCreativeTab(CreativeTabs.TOOLS).setUnlocalizedName("hammer"));
        registerItem(6115, "gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRing"));
        registerItem(6116, "onyx_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("onyxGoldRing"));
        registerItem(6117, "tanzanite_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("tanzaniteGoldRing"));
        registerItem(6118, "jade_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("jadeGoldRing"));
        registerItem(6119, "moonstone_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("moonstoneGoldRing"));
        registerItem(6120, "amethyst_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amethystGoldRing"));
        registerItem(6121, "ruby_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("rubyGoldRing"));
        registerItem(6122, "quartz_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("quartzGoldRing"));
        registerItem(6123, "amber_gold_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amberGoldRing"));
        registerItem(6124, "silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRing"));
        registerItem(6125, "onyx_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("onyxSilverRing"));
        registerItem(6126, "tanzanite_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("tanzaniteSilverRing"));
        registerItem(6127, "jade_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("jadeSilverRing"));
        registerItem(6128, "moonstone_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("moonstoneSilverRing"));
        registerItem(6129, "amethyst_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amethystSilverRing"));
        registerItem(6130, "ruby_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("rubySilverRing"));
        registerItem(6131, "quartz_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("quartzSilverRing"));
        registerItem(6132, "amber_silver_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amberSilverRing"));
        registerItem(6133, "bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRing"));
        registerItem(6134, "onyx_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("onyxBronzeRing"));
        registerItem(6135, "tanzanite_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("tanzaniteBronzeRing"));
        registerItem(6136, "jade_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("jadeBronzeRing"));
        registerItem(6137, "moonstone_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("moonstoneBronzeRing"));
        registerItem(6138, "amethyst_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amethystBronzeRing"));
        registerItem(6139, "ruby_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("rubyBronzeRing"));
        registerItem(6140, "quartz_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("quartzBronzeRing"));
        registerItem(6141, "amber_bronze_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amberBronzeRing"));
        registerItem(6142, "mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRing"));
        registerItem(6143, "onyx_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("onyxMithrilRing"));
        registerItem(6144, "tanzanite_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("tanzaniteMithrilRing"));
        registerItem(6145, "jade_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("jadeMithrilRing"));
        registerItem(6146, "moonstone_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("moonstoneMithrilRing"));
        registerItem(6147, "amethyst_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amethystMithrilRing"));
        registerItem(6148, "ruby_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("rubyMithrilRing"));
        registerItem(6149, "quartz_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("quartzMithrilRing"));
        registerItem(6150, "amber_mithril_ring", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("amberMithrilRing"));
        registerItem(6151, "salt", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("salt"));
        //registerItem(6152, "noldor_chestplate", (new ItemValarBase().setUnlocalizedName("noldorChestplate").setTextureName("noldor_chestplate")));
        registerItem(6153, "silver_ingot", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("silverIngot"));
        registerItem(6154, "dwarf_door", (new ItemDoor(Blocks.DWARF_DOOR)).setUnlocalizedName("dwarfDoor"));
        registerItem(6155, "boromirs_sword", (new ItemWeapon(900, 9)).setUnlocalizedName("boromirsSword"));
        //registerItem(6156, "grey_potion", (new ItemValarBase().setUnlocalizedName("greyPotion").setTextureName("grey_potion")));
        //registerItem(6157, "blue_potion", (new ItemValarBase().setUnlocalizedName("bluePotion").setTextureName("blue_potion")));
        //registerItem(6158, "brown_potion", (new ItemValarBase().setUnlocalizedName("brownPotion").setTextureName("brown_potion")));
        //registerItem(6159, "dark_green_potion", (new ItemValarBase().setUnlocalizedName("darkGreenPotion").setTextureName("dark_green_potion")));
        //registerItem(6160, "elanor_potion", (new ItemValarBase().setUnlocalizedName("elanorPotion").setTextureName("elanor_potion")));
        //registerItem(6161, "light_purple_potion", (new ItemValarBase().setUnlocalizedName("lightPurplePotion").setTextureName("light_purple_potion")));
        //registerItem(6162, "orc_potion", (new ItemValarBase().setUnlocalizedName("orcPotion").setTextureName("orc_potion")));
        registerItem(6163, "orc_sword_1", (new ItemWeapon(230, 5)).setUnlocalizedName("orcSword1"));
        registerItem(6164, "orc_sword_2", (new ItemWeapon(230, 5)).setUnlocalizedName("orcSword2"));
        //registerItem(6165, "rivendell_sword", (new ItemValarBase().setUnlocalizedName("rivendellSword").setTextureName("rivendell_sword")));
        registerItem(6166, "goblin_sword", (new ItemWeapon(200, 5)).setUnlocalizedName("goblinSword"));
        registerItem(6167, "hobbit_axe", (new ItemWeapon(235, 5)).setUnlocalizedName("hobbitAxe"));
        registerItem(6168, "sharkus_shortsword", (new ItemWeapon(467, 8)).setUnlocalizedName("sharkusShortsword"));
        registerItem(6169, "battle_pickaxe", (new ItemWeapon(300, 5)).setUnlocalizedName("battlePickaxe"));
        registerItem(6170, "shirriff_club", (new ItemWeapon(131, 6)).setUnlocalizedName("shirriffClub"));
        registerItem(6171, "pike_club", (new ItemWeapon(350, 6)).setUnlocalizedName("pikeClub"));
        registerItem(6172, "hobbit_sword", (new ItemWeapon(230, 5)).setUnlocalizedName("hobbitSword"));
        registerItem(6173, "hobbit_hammer", (new ItemWeapon(235, 5)).setUnlocalizedName("hobbitHammer"));
        //registerItem(6174, "dwarven_axe_moria", (new ItemValarBase().setUnlocalizedName("dwarvenAxeMoria").setTextureName("dwarvenaxemoria")));
        /*registerItem(6175, "green_arrow", (new ItemValarBase().setUnlocalizedName("greenArrow").setTextureName("green_arrow")));
        registerItem(6176, "black_arrow", (new ItemValarBase().setUnlocalizedName("blackArrow").setTextureName("black_arrow")));
        registerItem(6177, "goblin_arrow", (new ItemValarBase().setUnlocalizedName("goblinArrow").setTextureName("goblin_arrow")));
        registerItem(6179, "red_arrow", (new ItemValarBase().setUnlocalizedName("redArrow").setTextureName("red_arrow")));
        registerItem(6179, "yellow_arrow", (new ItemValarBase().setUnlocalizedName("yellowArrow").setTextureName("yellow_arrow")));
        //registerItem(6180, "throwing_rock", (new ItemValarBase().setUnlocalizedName("throwingRock").setTextureName("throwing_rock")));*/
        registerItem(6181, "boar_horns", (new Item()).setCreativeTab(CreativeTabs.MATERIALS).setUnlocalizedName("boarHorns"));
        registerItem(6182, "gold_ring_of_darkness", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfDarkness"));
        registerItem(6183, "gold_ring_of_earth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfEarth"));
        registerItem(6184, "gold_ring_of_fire", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfFire"));
        registerItem(6185, "gold_ring_of_greed", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfGreed"));
        registerItem(6186, "gold_ring_of_hope", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfHope"));
        registerItem(6187, "gold_ring_of_nature", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfNature"));
        registerItem(6188, "gold_ring_of_sea", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfSea"));
        registerItem(6189, "gold_ring_of_sky", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfSky"));
        registerItem(6190, "gold_ring_of_sunlight", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfSunlight"));
        registerItem(6191, "gold_ring_of_wealth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfWealth"));
        registerItem(6192, "gold_ring_of_wind", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("goldRingOfWind"));
        registerItem(6193, "silver_ring_of_darkness", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfDarkness"));
        registerItem(6194, "silver_ring_of_earth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfEarth"));
        registerItem(6195, "silver_ring_of_fire", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfFire"));
        registerItem(6196, "silver_ring_of_greed", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfGreed"));
        registerItem(6197, "silver_ring_of_hope", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfHope"));
        registerItem(6198, "silver_ring_of_nature", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfNature"));
        registerItem(6199, "silver_ring_of_sea", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfSea"));
        registerItem(6200, "silver_ring_of_sky", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfSky"));
        registerItem(6201, "silver_ring_of_sunlight", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfSunlight"));
        registerItem(6202, "silver_ring_of_wealth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfWealth"));
        registerItem(6203, "silver_ring_of_wind", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("silverRingOfWind"));
        registerItem(6204, "bronze_ring_of_darkness", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfDarkness"));
        registerItem(6205, "bronze_ring_of_earth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfEarth"));
        registerItem(6206, "bronze_ring_of_fire", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfFire"));
        registerItem(6207, "bronze_ring_of_greed", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfGreed"));
        registerItem(6208, "bronze_ring_of_hope", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfHope"));
        registerItem(6209, "bronze_ring_of_nature", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfNature"));
        registerItem(6210, "bronze_ring_of_sea", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfSea"));
        registerItem(6211, "bronze_ring_of_sky", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfSky"));
        registerItem(6212, "bronze_ring_of_sunlight", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfSunlight"));
        registerItem(6213, "bronze_ring_of_wealth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfWealth"));
        registerItem(6214, "bronze_ring_of_wind", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("bronzeRingOfWind"));
        registerItem(6215, "mithril_ring_of_darkness", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfDarkness"));
        registerItem(6216, "mithril_ring_of_earth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfEarth"));
        registerItem(6217, "mithril_ring_of_fire", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfFire"));
        registerItem(6218, "mithril_ring_of_greed", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfGreed"));
        registerItem(6219, "mithril_ring_of_hope", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfHope"));
        registerItem(6220, "mithril_ring_of_nature", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfNature"));
        registerItem(6221, "mithril_ring_of_sea", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfSea"));
        registerItem(6222, "mithril_ring_of_sky", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfSky"));
        registerItem(6223, "mithril_ring_of_sunlight", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfSunlight"));
        registerItem(6224, "mithril_ring_of_wealth", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfWealth"));
        registerItem(6225, "mithril_ring_of_wind", (new Item()).setCreativeTab(CreativeTabs.MISC).setUnlocalizedName("mithrilRingOfWind"));
        registerItem(6226, "anduril", (new ItemWeapon(3000, 11)).setUnlocalizedName("anduril"));
        registerItem(6227, "morgul_sword", (new ItemWeapon(2500, 9)).setUnlocalizedName("morgulSword"));
        registerItem(6228, "elven_long_sword", (new ItemWeapon(1800, 7)).setUnlocalizedName("elvenLongSword"));
        registerItem(6229, "berserker_sword", (new ItemWeapon(1400, 8)).setUnlocalizedName("berserkerSword"));
        registerItem(6230, "two_handed_sword", (new ItemWeapon(500, 7)).setUnlocalizedName("twohandedSword"));
        //registerItem(6231, "green_flag", (new ItemValarBase().setUnlocalizedName("greenFlag"));
        //registerItem(6232, "hobbit_bow", (new ItemValarBase().setUnlocalizedName("hobbitBow"));
        //registerItem(6233, "rohan_sheild_6", (new ItemValarBase().setUnlocalizedName("rohanShield6"));
        //registerItem(6234, "throwing_stone", (new ItemValarBase().setUnlocalizedName("throwingStone"));
        registerItem(6235, "rohirrim_axe", (new ItemWeapon(267, 6)).setUnlocalizedName("rohirrimAxe"));
        //registerItem(6236, "ecthelions_boots", (new ItemValarArmor(3, 500, 3, 0).setUnlocalizedName("ecthelionsBoots"));
        registerItem(6239, "elven_door", (new ItemDoor(Blocks.ELVEN_DOOR)).setUnlocalizedName("elvenDoor"));
        registerItem(6240, "human_door", (new ItemDoor(Blocks.HUMAN_DOOR)).setUnlocalizedName("humanDoor"));
        registerItem(6241, "green_bed", (new ItemCustomBed((BlockCustomBed)Blocks.GREEN_BED)).setUnlocalizedName("greenBed"));
        //registerItem(6242, "one_ring", (new ItemValarBase().setUnlocalizedName("oneRing"));
        registerItem(6243, "aicanar", (new ItemWeapon(6000, 10)).setUnlocalizedName("aicanar"));
        registerItem(6244, "balins_sword", (new ItemWeapon(1600, 7)).setUnlocalizedName("balinsSword"));
        registerItem(6245, "bifurs_spear", (new ItemWeapon(1102, 6)).setUnlocalizedName("bifursSpear"));
        //registerItem(6246, "bofurs_spear", (new ItemWeapon(1182, 6)).setUnlocalizedName("bofursSpear"));
        registerItem(6247, "bomburs_spoon", (new ItemWeapon(321, 4)).setUnlocalizedName("bombursSpoon"));
        registerItem(6248, "doris_sword", (new ItemWeapon(1500, 5)).setUnlocalizedName("dorisSword"));
        registerItem(6249, "dwalins_axe", (new ItemWeapon(1700, 7)).setUnlocalizedName("dwalinsAxe"));
        registerItem(6250, "filis_knife", (new ItemWeapon(112, 4)).setUnlocalizedName("filisKnife"));
        registerItem(6251, "filis_sword", (new ItemWeapon(1111, 6)).setUnlocalizedName("filisSword"));
        registerItem(6252, "kilis_sword", (new ItemWeapon(1111, 6)).setUnlocalizedName("kilisSword"));
        registerItem(6253, "noris_club", (new ItemWeapon(861, 5)).setUnlocalizedName("norisClub"));
        registerItem(6254, "oins_staff", (new ItemWeapon(922, 6)).setUnlocalizedName("oinsStaff"));
        registerItem(6255, "orcrist", (new ItemWeapon(8001, 12)).setUnlocalizedName("orcrist"));
        registerItem(6256, "sword_of_westernesse", (new ItemWeapon(3200, 8)).setUnlocalizedName("swordOfWesternesse"));
        registerItem(6257, "thorins_sword", (new ItemWeapon(1200, 6)).setUnlocalizedName("thorinsSword"));
        registerItem(6258, "thrains_warhammer", (new ItemWeapon(1288, 6)).setUnlocalizedName("thrainsWarhammer"));
        registerItem(6259, "thrors_warhammer", (new ItemWeapon(1428, 7)).setUnlocalizedName("throrsWarhammer"));
        registerItem(6260, "aragorns_elf_knife", (new ItemWeapon(1300, 5)).setUnlocalizedName("aragornsElfKnife"));
        registerItem(6261, "eowyns_sword", (new ItemWeapon(1261, 6)).setUnlocalizedName("eowynsSword"));
        registerItem(6262, "gandalf_the_greys_staff", (new ItemWeapon(3000, 9)).setUnlocalizedName("gandalfTheGreysStaff"));
        registerItem(6263, "gandalf_the_whites_staff", (new ItemWeapon(5000, 11)).setUnlocalizedName("gandalfTheWhitesStaff"));
        registerItem(6264, "gimlis_two_headed_axe", (new ItemWeapon(2000, 9)).setUnlocalizedName("gimlisTwoHeadedAxe"));
        registerItem(6265, "gimlis_longaxe", (new ItemWeapon(1700, 7)).setUnlocalizedName("gimlisLongaxe"));
        registerItem(6266, "glamdring", (new ItemWeapon(3200, 11)).setUnlocalizedName("glamdring"));
        registerItem(6267, "gloins_axe", (new ItemWeapon(1700, 7)).setUnlocalizedName("gloinsAxe"));
        registerItem(6268, "glorfindels_sword", (new ItemWeapon(3600, 9)).setUnlocalizedName("glorfindelsSword"));
        registerItem(6269, "guthwine", (new ItemWeapon(1732, 8)).setUnlocalizedName("guthwine"));
        registerItem(6270, "hadhafang", (new ItemWeapon(3400, 10)).setUnlocalizedName("hadhafang"));
        //registerItem(6271, "herugrim", (new ItemValarBase().setUnlocalizedName("herugrim"));
        registerItem(6272, "legolas_sword", (new ItemWeapon(2900, 9)).setUnlocalizedName("legolasSword"));
        registerItem(6273, "radagasts_staff", (new ItemWeapon(3100, 8)).setUnlocalizedName("radagastsStaff"));
        registerItem(6274, "lurtz_sword", (new ItemWeapon(1870, 8)).setUnlocalizedName("lurtzSword"));
        registerItem(6275, "saurons_mace", (new ItemWeapon(7500, 13)).setUnlocalizedName("sauronsMace"));
        registerItem(6276, "sarumans_staff", (new ItemWeapon(4500, 10)).setUnlocalizedName("sarumansStaff"));
        registerItem(6277, "yaznegs_axe", (new ItemWeapon(830, 6)).setUnlocalizedName("yaznegsAxe"));
        registerItem(6278, "dwarven_lord_knife", (new ItemWeapon(1200, 4)).setUnlocalizedName("dwarvenLordKnife"));
        registerItem(6279, "gondorian_noble_sword", (new ItemWeapon(1400, 7)).setUnlocalizedName("gondorianNobleSword"));
        registerItem(6280, "mace_of_glory", (new ItemWeapon(2100, 8)).setUnlocalizedName("maceOfGlory"));
        registerItem(6281, "golden_war_hammer_of_erebor", (new ItemWeapon(2800, 7)).setUnlocalizedName("goldenWarHammerOfErebor"));
        registerItem(6282, "gondorian_spear", (new ItemWeapon(1000, 6)).setUnlocalizedName("gondorianSpear"));
        registerItem(6283, "rohirrim_spear", (new ItemWeapon(1040, 6)).setUnlocalizedName("rohirrimSpear"));
        registerItem(6284, "bull_head_mace", (new ItemWeapon(1091, 6)).setUnlocalizedName("bullHeadMace"));
        registerItem(6285, "corsair_eket", (new ItemWeapon(1133, 7)).setUnlocalizedName("corsairEket"));
        registerItem(6286, "dol_guldur_spiked_mace", (new ItemWeapon(981, 6)).setUnlocalizedName("dolGuldurSpikedMace"));
        registerItem(6287, "fell_wargrider_sword", (new ItemWeapon(991, 6)).setUnlocalizedName("fellWargriderSword"));
        registerItem(6288, "haradrim_snake_dagger", (new ItemWeapon(777, 5)).setUnlocalizedName("haradrimSnakeDagger"));
        registerItem(6289, "north_goblin_mace", (new ItemWeapon(531, 5)).setUnlocalizedName("northGoblinMace"));
        registerItem(6290, "north_goblin_sword", (new ItemWeapon(772, 5)).setUnlocalizedName("northGoblinSword"));
        registerItem(6291, "orc_captain_sword", (new ItemWeapon(1130, 7)).setUnlocalizedName("orcCaptainSword"));
        registerItem(6292, "orc_dagger", (new ItemWeapon(103, 4)).setUnlocalizedName("orcDagger"));
        registerItem(6293, "orc_halbard", (new ItemWeapon(605, 7)).setUnlocalizedName("orcHalbard"));
        registerItem(6294, "uruk_siege_trooper_dagger", (new ItemWeapon(1041, 5)).setUnlocalizedName("urukSiegeTrooperDagger"));
        //registerItem(6295, "orc_spawner", (new ItemValarBase().setUnlocalizedName("orcSpawner"));
        registerItem(6296, "iron_mace", (new ItemWeapon(387, 7)).setUnlocalizedName("ironMace"));
        registerItem(6297, "orc_sword_3", (new ItemWeapon(837, 6)).setUnlocalizedName("orcSword3"));
        //registerItem(6298, "morannon_orc_shield", (new ItemValarBase().setUnlocalizedName("morannonOrcShield"));
        registerItem(6299, "morannon_dagger", (new ItemWeapon(99, 3)).setUnlocalizedName("morannonDagger"));
        registerItem(6300, "orc_spear", (new ItemWeapon(123, 6)).setUnlocalizedName("orcSpear"));
        //registerItem(6301, "net", (new ItemValarBase().setUnlocalizedName("net").setTextureName("net")));
        //registerItem(6302, "erus_staff", (new ItemValarErusStaff()).setUnlocalizedName("erusStaff").setTextureName("erus_staff"));
        registerItem(6303, "castle_door", (new ItemDoor(Blocks.CASTLE_DOOR)).setUnlocalizedName("castleDoor"));
        registerItem(6304, "dol_guldur_prison_door", (new ItemDoor(Blocks.DOL_GULDUR_PRISON_DOOR)).setUnlocalizedName("dolGuldurPrisonDoor"));
        registerItem(6305, "hillmen_door", (new ItemDoor(Blocks.HILLMEN_DOOR)).setUnlocalizedName("hillmenDoor"));
        registerItem(6306, "mordor_door", (new ItemDoor(Blocks.MORDOR_DOOR)).setUnlocalizedName("mordorDoor"));
        registerItem(6307, "prison_door", (new ItemDoor(Blocks.PRISON_DOOR)).setUnlocalizedName("prisonDoor"));
        registerItem(6308, "sindar_door", (new ItemDoor(Blocks.SINDAR_DOOR)).setUnlocalizedName("sindarDoor"));
        registerItem(6309, "southlinch_seed", (new ItemSeeds(Blocks.SOUTHLINCH, Blocks.FARMLAND)).setUnlocalizedName("southlinchSeed"));
        /*registerItem(6310, "green_grape_seed", (new ItemValarCrops((BlockValarCrops)Blocks.greenGrape)).setUnlocalizedName("greenGrapeSeed").setTextureName("green_grape_seed"));*/
        registerItem(6311, "green_grapes", (new ItemCustomFood(2, 0.3F, false)).setUnlocalizedName("greenGrapes"));
        /*registerItem(6312, "purple_grape_seed", (new ItemValarCrops((BlockValarCrops)Blocks.purpleGrape)).setUnlocalizedName("purpleGrapeSeed").setTextureName("purple_grape_seed"));*/
        registerItem(6313, "purple_grapes", (new ItemCustomFood(2, 0.3F, false)).setUnlocalizedName("purpleGrapes"));
        registerItem(6314, "pipeweed_seed", (new ItemSeeds(Blocks.PIPEWEED_PLANT, Blocks.FARMLAND)).setUnlocalizedName("pipeweedSeed"));
        registerItem(6315, "pea_seed", (new ItemSeeds(Blocks.PEA_PLANT, Blocks.FARMLAND)).setUnlocalizedName("peaSeed"));
        registerItem(6316, "peas", (new ItemCustomFood(1, 0.6F, false)).setUnlocalizedName("peas").setCreativeTab(CreativeTabs.FOOD));
        registerItem(6317, "leek", (new ItemCustomFood(3, 0.6F, false)).setUnlocalizedName("leek").setCreativeTab(CreativeTabs.FOOD));
        registerItem(6318, "leek_seed", (new ItemSeeds(Blocks.LEEK_PLANT, Blocks.FARMLAND)).setUnlocalizedName("leekSeed"));
        registerItem(6319, "onion_seed", (new ItemSeeds(Blocks.ONION_PLANT, Blocks.FARMLAND)).setUnlocalizedName("onionSeed"));
        registerItem(6320, "onion", (new ItemCustomFood(3, 0.6F, false)).setUnlocalizedName("onion").setCreativeTab(CreativeTabs.FOOD));
        registerItem(6321, "old_toby_seed", (new ItemSeeds(Blocks.OLD_TOBY_PLANT, Blocks.FARMLAND)).setUnlocalizedName("oldTobySeed"));
        registerItem(6322, "southern_star_seed", (new ItemSeeds(Blocks.SOUTHERN_STAR_PLANT, Blocks.FARMLAND)).setUnlocalizedName("southernStarSeed"));
        registerItem(6323, "strawberry_seed", (new ItemSeeds(Blocks.STRAWBERRY_BUSH, Blocks.FARMLAND)).setUnlocalizedName("strawberrySeed"));
        /*registerItem(6324, "azogs_mace", (new ItemValarWeapon(3500, 7, 0, true, false)).setUnlocalizedName("azogsMace").setTextureName("azogs_mace"));
        // Temporary Id, change to proper id when adding items
        //registerItem(10002, "rucksack", (new ItemValarRucksack()).setUnlocalizedName("rucksack").setTextureName("rucksack"));
        //registerItem(10003, "frodos_finger", (new ItemValarFood(2, 0.1F, true).setUnlocalizedName("frodosFinger").setTextureName("frodos_finger")));
        //registerItem(10004, "two_handed_sword", (new ItemValarWeapon(1, 20, 0, true).setUnlocalizedName("twoHandedSword").setTextureName("two_handed_sword")));
        //registerItem(10005, "easterling_helmet", (new ItemValarArmor(0, 1000, 2, 0).setUnlocalizedName("easterlingHelmet").setTextureName("easterling_helmet")));
        //registerItem(10006, "miruvoir", (new ItemValarDrinkable(Items.glass_bottle).setUnlocalizedName("miruvoir").setTextureName("miruvoir")));*/
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
