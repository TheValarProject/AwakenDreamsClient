package net.minecraft.block;

import com.elementfx.tvp.ad.block.Block3D;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Block
{
    /** ResourceLocation for the Air block */
    private static final ResourceLocation AIR_ID = new ResourceLocation("air");
    public static final RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY = new RegistryNamespacedDefaultedByKey(AIR_ID);
    public static final ObjectIntIdentityMap<IBlockState> BLOCK_STATE_IDS = new ObjectIntIdentityMap();
    public static final AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    public static final AxisAlignedBB NULL_AABB = null;
    private CreativeTabs displayOnCreativeTab;
    protected boolean fullBlock;

    /** How much light is subtracted for going through this block */
    protected int lightOpacity;
    protected boolean translucent;

    /** Amount of light emitted */
    protected int lightValue;

    /**
     * Flag if block should use the brightest neighbor light value as its own
     */
    protected boolean useNeighborBrightness;

    /** Indicates how many hits it takes to break a block. */
    protected float blockHardness;

    /** Indicates how much this block can resist explosions */
    protected float blockResistance;
    protected boolean enableStats;

    /**
     * Flags whether or not this block is of a type that needs random ticking. Ref-counted by ExtendedBlockStorage in
     * order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    protected boolean needsRandomTick;

    /** true if the Block contains a Tile Entity */
    protected boolean isBlockContainer;

    /** Sound of stepping on the block */
    protected SoundType blockSoundType;
    public float blockParticleGravity;
    protected final Material blockMaterial;

    /** The Block's MapColor */
    protected final MapColor blockMapColor;

    /**
     * Determines how much velocity is maintained while moving on top of this block
     */
    public float slipperiness;
    protected final BlockStateContainer blockState;
    private IBlockState defaultBlockState;
    private String unlocalizedName;

    public static int getIdFromBlock(Block blockIn)
    {
        return REGISTRY.getIDForObject(blockIn);
    }

    /**
     * Get a unique ID for the given BlockState, containing both BlockID and metadata
     */
    public static int getStateId(IBlockState state)
    {
        Block block = state.getBlock();
        return getIdFromBlock(block) + (block.getMetaFromState(state) << 12);
    }

    public static Block getBlockById(int id)
    {
        return (Block)REGISTRY.getObjectById(id);
    }

    /**
     * Get a BlockState by it's ID (see getStateId)
     */
    public static IBlockState getStateById(int id)
    {
        int i = id & 4095;
        int j = id >> 12 & 15;
        return getBlockById(i).getStateFromMeta(j);
    }

    public static Block getBlockFromItem(Item itemIn)
    {
        return itemIn instanceof ItemBlock ? ((ItemBlock)itemIn).getBlock() : null;
    }

    @Nullable
    public static Block getBlockFromName(String name)
    {
        ResourceLocation resourcelocation = new ResourceLocation(name);

        if (REGISTRY.containsKey(resourcelocation))
        {
            return (Block)REGISTRY.getObject(resourcelocation);
        }
        else
        {
            try
            {
                return (Block)REGISTRY.getObjectById(Integer.parseInt(name));
            }
            catch (NumberFormatException var3)
            {
                return null;
            }
        }
    }

    @Deprecated

    /**
     * Checks if an IBlockState represents a block that is opaque and a full cube.
     */
    public boolean isFullyOpaque(IBlockState state)
    {
        return state.getMaterial().isOpaque() && state.isFullCube();
    }

    @Deprecated
    public boolean isFullBlock(IBlockState state)
    {
        return this.fullBlock;
    }

    @Deprecated
    public boolean func_189872_a(IBlockState p_189872_1_, Entity p_189872_2_)
    {
        return true;
    }

    @Deprecated
    public int getLightOpacity(IBlockState state)
    {
        return this.lightOpacity;
    }

    @Deprecated

    /**
     * Used in the renderer to apply ambient occlusion
     */
    public boolean isTranslucent(IBlockState state)
    {
        return this.translucent;
    }

    @Deprecated
    public int getLightValue(IBlockState state)
    {
        return this.lightValue;
    }

    @Deprecated

    /**
     * Should block use the brightest neighbor light value as its own
     */
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return this.useNeighborBrightness;
    }

    @Deprecated

    /**
     * Get a material of block
     */
    public Material getMaterial(IBlockState state)
    {
        return this.blockMaterial;
    }

    @Deprecated

    /**
     * Get the MapColor for this Block and the given BlockState
     */
    public MapColor getMapColor(IBlockState state)
    {
        return this.blockMapColor;
    }

    @Deprecated

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState();
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state)
    {
        if (state != null && !state.getPropertyNames().isEmpty())
        {
            throw new IllegalArgumentException("Don\'t know how to convert " + state + " back into data...");
        }
        else
        {
            return 0;
        }
    }

    @Deprecated

    /**
     * Get the actual Block state of this Block at the given position. This applies properties not visible in the
     * metadata, such as fence connections.
     */
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return state;
    }

    @Deprecated

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state;
    }

    @Deprecated

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state;
    }

    public Block(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        this.enableStats = true;
        this.blockSoundType = SoundType.STONE;
        this.blockParticleGravity = 1.0F;
        this.slipperiness = 0.6F;
        this.blockMaterial = blockMaterialIn;
        this.blockMapColor = blockMapColorIn;
        this.blockState = this.createBlockState();
        this.setDefaultState(this.blockState.getBaseState());
        this.fullBlock = this.getDefaultState().isOpaqueCube();
        this.lightOpacity = this.fullBlock ? 255 : 0;
        this.translucent = !blockMaterialIn.blocksLight();
    }

    protected Block(Material materialIn)
    {
        this(materialIn, materialIn.getMaterialMapColor());
    }

    /**
     * Sets the footstep sound for the block. Returns the object for convenience in constructing.
     */
    protected Block setSoundType(SoundType sound)
    {
        this.blockSoundType = sound;
        return this;
    }

    /**
     * Sets how much light is blocked going through this block. Returns the object for convenience in constructing.
     */
    protected Block setLightOpacity(int opacity)
    {
        this.lightOpacity = opacity;
        return this;
    }

    /**
     * Sets the light value that the block emits. Returns resulting block instance for constructing convenience.
     */
    protected Block setLightLevel(float value)
    {
        this.lightValue = (int)(15.0F * value);
        return this;
    }

    /**
     * Sets the the blocks resistance to explosions. Returns the object for convenience in constructing.
     */
    protected Block setResistance(float resistance)
    {
        this.blockResistance = resistance * 3.0F;
        return this;
    }

    @Deprecated

    /**
     * Indicate if a material is a normal solid opaque cube
     */
    public boolean isBlockNormalCube(IBlockState state)
    {
        return state.getMaterial().blocksMovement() && state.isFullCube();
    }

    @Deprecated

    /**
     * Used for nearly all game logic (non-rendering) purposes. Use Forge-provided isNormalCube(IBlockAccess, BlockPos)
     * instead.
     */
    public boolean isNormalCube(IBlockState state)
    {
        return state.getMaterial().isOpaque() && state.isFullCube() && !state.canProvidePower();
    }

    public boolean isVisuallyOpaque()
    {
        return this.blockMaterial.blocksMovement() && this.getDefaultState().isFullCube();
    }

    @Deprecated
    public boolean isFullCube(IBlockState state)
    {
        return true;
    }

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return !this.blockMaterial.blocksMovement();
    }

    @Deprecated

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    /**
     * Whether this Block can be replaced directly by other blocks (true for e.g. tall grass)
     */
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }

    /**
     * Sets how many hits it takes to break a block.
     */
    protected Block setHardness(float hardness)
    {
        this.blockHardness = hardness;

        if (this.blockResistance < hardness * 5.0F)
        {
            this.blockResistance = hardness * 5.0F;
        }

        return this;
    }

    protected Block setBlockUnbreakable()
    {
        this.setHardness(-1.0F);
        return this;
    }

    @Deprecated
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return this.blockHardness;
    }

    /**
     * Sets whether this block type will receive random update ticks
     */
    protected Block setTickRandomly(boolean shouldTick)
    {
        this.needsRandomTick = shouldTick;
        return this;
    }

    /**
     * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
     * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
     */
    public boolean getTickRandomly()
    {
        return this.needsRandomTick;
    }

    public boolean hasTileEntity()
    {
        return this.isBlockContainer;
    }

    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return FULL_BLOCK_AABB;
    }

    @Deprecated
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        int i = source.getCombinedLight(pos, state.getLightValue());

        if (i == 0 && state.getBlock() instanceof BlockSlab)
        {
            pos = pos.down();
            state = source.getBlockState(pos);
            return source.getCombinedLight(pos, state.getLightValue());
        }
        else
        {
            return i;
        }
    }

    @Deprecated
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        AxisAlignedBB axisalignedbb = blockState.getBoundingBox(blockAccess, pos);

        switch (side)
        {
            case DOWN:
                if (axisalignedbb.minY > 0.0D)
                {
                    return true;
                }

                break;

            case UP:
                if (axisalignedbb.maxY < 1.0D)
                {
                    return true;
                }

                break;

            case NORTH:
                if (axisalignedbb.minZ > 0.0D)
                {
                    return true;
                }

                break;

            case SOUTH:
                if (axisalignedbb.maxZ < 1.0D)
                {
                    return true;
                }

                break;

            case WEST:
                if (axisalignedbb.minX > 0.0D)
                {
                    return true;
                }

                break;

            case EAST:
                if (axisalignedbb.maxX < 1.0D)
                {
                    return true;
                }
        }

        return !blockAccess.getBlockState(pos.offset(side)).isOpaqueCube();
    }

    /**
     * Whether this Block is solid on the given Side
     */
    public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        return worldIn.getBlockState(pos).getMaterial().isSolid();
    }

    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return state.getBoundingBox(worldIn, pos).offset(pos);
    }

    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
    {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }

    protected static void addCollisionBoxToList(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable AxisAlignedBB blockBox)
    {
        if (blockBox != NULL_AABB)
        {
            AxisAlignedBB axisalignedbb = blockBox.offset(pos);

            if (entityBox.intersectsWith(axisalignedbb))
            {
                collidingBoxes.add(axisalignedbb);
            }
        }
    }

    @Deprecated
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return blockState.getBoundingBox(worldIn, pos);
    }

    @Deprecated

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return this.isCollidable();
    }

    /**
     * Returns if this block is collidable. Only used by fire, although stairs return that of the block that the stair
     * is made of (though nobody's going to make fire stairs, right?)
     */
    public boolean isCollidable()
    {
        return true;
    }

    /**
     * Called randomly when setTickRandomly is set to true (used by e.g. crops to grow, etc.)
     */
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
    {
        this.updateTick(worldIn, pos, state, random);
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    }

    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
    }

    /**
     * Called when a player destroys this Block
     */
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    @Deprecated
    public void func_189540_a(IBlockState p_189540_1_, World p_189540_2_, BlockPos p_189540_3_, Block p_189540_4_)
    {
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World worldIn)
    {
        return 10;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random random)
    {
        return 1;
    }

    @Nullable

    /**
     * Get the Item that this Block should drop when harvested.
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(this);
    }

    @Deprecated

    /**
     * Get the hardness of this Block relative to the ability of the given player
     */
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos)
    {
        float f = state.getBlockHardness(worldIn, pos);
        return f < 0.0F ? 0.0F : (!player.canHarvestBlock(state) ? player.getDigSpeed(state) / f / 100.0F : player.getDigSpeed(state) / f / 30.0F);
    }

    /**
     * Spawn this Block's drops into the World as EntityItems
     */
    public final void dropBlockAsItem(World worldIn, BlockPos pos, IBlockState state, int fortune)
    {
        this.dropBlockAsItemWithChance(worldIn, pos, state, 1.0F, fortune);
    }

    /**
     * Spawns this Block's drops into the World as EntityItems.
     */
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (!worldIn.isRemote)
        {
            int i = this.quantityDroppedWithBonus(fortune, worldIn.rand);

            for (int j = 0; j < i; ++j)
            {
                if (worldIn.rand.nextFloat() <= chance)
                {
                    Item item = this.getItemDropped(state, worldIn.rand, fortune);

                    if (item != null)
                    {
                        spawnAsEntity(worldIn, pos, new ItemStack(item, 1, this.damageDropped(state)));
                    }
                }
            }
        }
    }

    /**
     * Spawns the given ItemStack as an EntityItem into the World at the given position
     */
    public static void spawnAsEntity(World worldIn, BlockPos pos, ItemStack stack)
    {
        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops"))
        {
            float f = 0.5F;
            double d0 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double)(worldIn.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
            entityitem.setDefaultPickupDelay();
            worldIn.spawnEntityInWorld(entityitem);
        }
    }

    /**
     * Spawns the given amount of experience into the World as XP orb entities
     */
    protected void dropXpOnBlockBreak(World worldIn, BlockPos pos, int amount)
    {
        if (!worldIn.isRemote && worldIn.getGameRules().getBoolean("doTileDrops"))
        {
            while (amount > 0)
            {
                int i = EntityXPOrb.getXPSplit(amount);
                amount -= i;
                worldIn.spawnEntityInWorld(new EntityXPOrb(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, i));
            }
        }
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return 0;
    }

    /**
     * Returns how much this block can resist explosions from the passed in entity.
     */
    public float getExplosionResistance(Entity exploder)
    {
        return this.blockResistance / 5.0F;
    }

    @Deprecated
    @Nullable

    /**
     * Ray traces through the blocks collision from start vector to end vector returning a ray trace hit.
     */
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        return this.rayTrace(pos, start, end, blockState.getBoundingBox(worldIn, pos));
    }

    @Nullable
    protected RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB boundingBox)
    {
        Vec3d vec3d = start.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        Vec3d vec3d1 = end.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
        RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
        return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), raytraceresult.sideHit, pos);
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn)
    {
    }

    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    public boolean canReplace(World worldIn, BlockPos pos, EnumFacing side, @Nullable ItemStack stack)
    {
        return this.canPlaceBlockOnSide(worldIn, pos, side);
    }

    /**
     * Check whether this Block can be placed on the given side
     */
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return this.canPlaceBlockAt(worldIn, pos);
    }

    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().blockMaterial.isReplaceable();
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    /**
     * Triggered whenever an entity collides with this block (enters into the block)
     */
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
    }

    /**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getStateFromMeta(meta);
    }

    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
    }

    public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion)
    {
        return motion;
    }

    @Deprecated
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return 0;
    }

    @Deprecated

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower(IBlockState state)
    {
        return false;
    }

    /**
     * Called When an Entity Collided with the Block
     */
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
    }

    @Deprecated
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return 0;
    }

    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, @Nullable ItemStack stack)
    {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.025F);

        if (this.canSilkHarvest() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
        {
            ItemStack itemstack = this.createStackedBlock(state);

            if (itemstack != null)
            {
                spawnAsEntity(worldIn, pos, itemstack);
            }
        }
        else
        {
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            this.dropBlockAsItem(worldIn, pos, state, i);
        }
    }

    protected boolean canSilkHarvest()
    {
        return this.getDefaultState().isFullCube() && !this.isBlockContainer;
    }

    @Nullable
    protected ItemStack createStackedBlock(IBlockState state)
    {
        Item item = Item.getItemFromBlock(this);

        if (item == null)
        {
            return null;
        }
        else
        {
            int i = 0;

            if (item.getHasSubtypes())
            {
                i = this.getMetaFromState(state);
            }

            return new ItemStack(item, 1, i);
        }
    }

    /**
     * Get the quantity dropped based on the given fortune level
     */
    public int quantityDroppedWithBonus(int fortune, Random random)
    {
        return this.quantityDropped(random);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
    }

    /**
     * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
     */
    public boolean canSpawnInBlock()
    {
        return !this.blockMaterial.isSolid() && !this.blockMaterial.isLiquid();
    }

    public Block setUnlocalizedName(String name)
    {
        this.unlocalizedName = name;
        return this;
    }

    /**
     * Gets the localized name of this block. Used for the statistics page.
     */
    public String getLocalizedName()
    {
        return I18n.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    /**
     * Returns the unlocalized name of the block with "tile." appended to the front.
     */
    public String getUnlocalizedName()
    {
        return "tile." + this.unlocalizedName;
    }

    @Deprecated
    public boolean func_189539_a(IBlockState p_189539_1_, World p_189539_2_, BlockPos p_189539_3_, int p_189539_4_, int p_189539_5_)
    {
        return false;
    }

    /**
     * Return the state of blocks statistics flags - if the block is counted for mined and placed.
     */
    public boolean getEnableStats()
    {
        return this.enableStats;
    }

    protected Block disableStats()
    {
        this.enableStats = false;
        return this;
    }

    @Deprecated
    public EnumPushReaction getMobilityFlag(IBlockState state)
    {
        return this.blockMaterial.getMobilityFlag();
    }

    @Deprecated
    public float getAmbientOcclusionLightValue(IBlockState state)
    {
        return state.isBlockNormalCube() ? 0.2F : 1.0F;
    }

    /**
     * Block's chance to react to a living entity falling on it.
     */
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance)
    {
        entityIn.fall(fallDistance, 1.0F);
    }

    /**
     * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
     * on its own
     */
    public void onLanded(World worldIn, Entity entityIn)
    {
        entityIn.motionY = 0.0D;
    }

    @Nullable
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list)
    {
        list.add(new ItemStack(itemIn));
    }

    /**
     * Returns the CreativeTab to display the given block on.
     */
    public CreativeTabs getCreativeTabToDisplayOn()
    {
        return this.displayOnCreativeTab;
    }

    public Block setCreativeTab(CreativeTabs tab)
    {
        this.displayOnCreativeTab = tab;
        return this;
    }

    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
    }

    /**
     * Called similar to random ticks, but only when it is raining.
     */
    public void fillWithRain(World worldIn, BlockPos pos)
    {
    }

    public boolean requiresUpdates()
    {
        return true;
    }

    /**
     * Return whether this block can drop from an explosion.
     */
    public boolean canDropFromExplosion(Explosion explosionIn)
    {
        return true;
    }

    public boolean isAssociatedBlock(Block other)
    {
        return this == other;
    }

    public static boolean isEqualTo(Block blockIn, Block other)
    {
        return blockIn != null && other != null ? (blockIn == other ? true : blockIn.isAssociatedBlock(other)) : false;
    }

    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return false;
    }

    @Deprecated
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return 0;
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[0]);
    }

    public BlockStateContainer getBlockState()
    {
        return this.blockState;
    }

    protected final void setDefaultState(IBlockState state)
    {
        this.defaultBlockState = state;
    }

    public final IBlockState getDefaultState()
    {
        return this.defaultBlockState;
    }

    /**
     * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
     */
    public Block.EnumOffsetType getOffsetType()
    {
        return Block.EnumOffsetType.NONE;
    }

    public SoundType getSoundType()
    {
        return this.blockSoundType;
    }

    public String toString()
    {
        return "Block{" + REGISTRY.getNameForObject(this) + "}";
    }

    public static void registerBlocks()
    {
        registerBlock(0, AIR_ID, (new BlockAir()).setUnlocalizedName("air"));
        registerBlock(1, "stone", (new BlockStone()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stone"));
        registerBlock(2, "grass", (new BlockGrass()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("grass"));
        registerBlock(3, "dirt", (new BlockDirt()).setHardness(0.5F).setSoundType(SoundType.GROUND).setUnlocalizedName("dirt"));
        Block block = (new Block(Material.ROCK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stonebrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(4, "cobblestone", block);
        Block block1 = (new BlockPlanks()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("wood");
        registerBlock(5, "planks", block1);
        registerBlock(6, "sapling", (new BlockSapling()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("sapling"));
        registerBlock(7, "bedrock", (new BlockEmptyDrops(Material.ROCK)).setBlockUnbreakable().setResistance(6000000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("bedrock").disableStats().setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(8, "flowing_water", (new BlockDynamicLiquid(Material.WATER)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
        registerBlock(9, "water", (new BlockStaticLiquid(Material.WATER)).setHardness(100.0F).setLightOpacity(3).setUnlocalizedName("water").disableStats());
        registerBlock(10, "flowing_lava", (new BlockDynamicLiquid(Material.LAVA)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
        registerBlock(11, "lava", (new BlockStaticLiquid(Material.LAVA)).setHardness(100.0F).setLightLevel(1.0F).setUnlocalizedName("lava").disableStats());
        registerBlock(12, "sand", (new BlockSand()).setHardness(0.5F).setSoundType(SoundType.SAND).setUnlocalizedName("sand"));
        registerBlock(13, "gravel", (new BlockGravel()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("gravel"));
        registerBlock(14, "gold_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreGold"));
        registerBlock(15, "iron_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreIron"));
        registerBlock(16, "coal_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreCoal"));
        registerBlock(17, "log", (new BlockOldLog()).setUnlocalizedName("log"));
        registerBlock(18, "leaves", (new BlockOldLeaf()).setUnlocalizedName("leaves"));
        registerBlock(19, "sponge", (new BlockSponge()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("sponge"));
        registerBlock(20, "glass", (new BlockGlass(Material.GLASS, false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("glass"));
        registerBlock(21, "lapis_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreLapis"));
        registerBlock(22, "lapis_block", (new Block(Material.IRON, MapColor.LAPIS)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blockLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(23, "dispenser", (new BlockDispenser()).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("dispenser"));
        Block block2 = (new BlockSandStone()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("sandStone");
        registerBlock(24, "sandstone", block2);
        registerBlock(25, "noteblock", (new BlockNote()).setSoundType(SoundType.WOOD).setHardness(0.8F).setUnlocalizedName("musicBlock"));
        registerBlock(26, "bed", (new BlockBed()).setSoundType(SoundType.WOOD).setHardness(0.2F).setUnlocalizedName("bed").disableStats());
        registerBlock(27, "golden_rail", (new BlockRailPowered()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("goldenRail"));
        registerBlock(28, "detector_rail", (new BlockRailDetector()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("detectorRail"));
        registerBlock(29, "sticky_piston", (new BlockPistonBase(true)).setUnlocalizedName("pistonStickyBase"));
        registerBlock(30, "web", (new BlockWeb()).setLightOpacity(1).setHardness(4.0F).setUnlocalizedName("web"));
        registerBlock(31, "tallgrass", (new BlockTallGrass()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("tallgrass"));
        registerBlock(32, "deadbush", (new BlockDeadBush()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("deadbush"));
        registerBlock(33, "piston", (new BlockPistonBase(false)).setUnlocalizedName("pistonBase"));
        registerBlock(34, "piston_head", (new BlockPistonExtension()).setUnlocalizedName("pistonBase"));
        registerBlock(35, "wool", (new BlockColored(Material.CLOTH)).setHardness(0.8F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cloth"));
        registerBlock(36, "piston_extension", new BlockPistonMoving());
        registerBlock(37, "yellow_flower", (new BlockYellowFlower()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("flower1"));
        registerBlock(38, "red_flower", (new BlockRedFlower()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("flower2"));
        Block block3 = (new BlockMushroom()).setHardness(0.0F).setSoundType(SoundType.PLANT).setLightLevel(0.125F).setUnlocalizedName("mushroom");
        registerBlock(39, "brown_mushroom", block3);
        Block block4 = (new BlockMushroom()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("mushroom");
        registerBlock(40, "red_mushroom", block4);
        registerBlock(41, "gold_block", (new Block(Material.IRON, MapColor.GOLD)).setHardness(3.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockGold").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(42, "iron_block", (new Block(Material.IRON, MapColor.IRON)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockIron").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(43, "double_stone_slab", (new BlockDoubleStoneSlab()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab"));
        registerBlock(44, "stone_slab", (new BlockHalfStoneSlab()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab"));
        Block block5 = (new Block(Material.ROCK, MapColor.RED)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("brick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(45, "brick_block", block5);
        registerBlock(46, "tnt", (new BlockTNT()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("tnt"));
        registerBlock(47, "bookshelf", (new BlockBookshelf()).setHardness(1.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("bookshelf"));
        registerBlock(48, "mossy_cobblestone", (new Block(Material.ROCK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneMoss").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(49, "obsidian", (new BlockObsidian()).setHardness(50.0F).setResistance(2000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("obsidian"));
        registerBlock(50, "torch", (new BlockTorch()).setHardness(0.0F).setLightLevel(0.9375F).setSoundType(SoundType.WOOD).setUnlocalizedName("torch"));
        registerBlock(51, "fire", (new BlockFire()).setHardness(0.0F).setLightLevel(1.0F).setSoundType(SoundType.CLOTH).setUnlocalizedName("fire").disableStats());
        registerBlock(52, "mob_spawner", (new BlockMobSpawner()).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("mobSpawner").disableStats());
        registerBlock(53, "oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK))).setUnlocalizedName("stairsWood"));
        registerBlock(54, "chest", (new BlockChest(BlockChest.Type.BASIC)).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("chest"));
        registerBlock(55, "redstone_wire", (new BlockRedstoneWire()).setHardness(0.0F).setSoundType(SoundType.STONE).setUnlocalizedName("redstoneDust").disableStats());
        registerBlock(56, "diamond_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreDiamond"));
        registerBlock(57, "diamond_block", (new Block(Material.IRON, MapColor.DIAMOND)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockDiamond").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(58, "crafting_table", (new BlockWorkbench()).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("workbench"));
        registerBlock(59, "wheat", (new BlockCrops()).setUnlocalizedName("crops"));
        Block block6 = (new BlockFarmland()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("farmland");
        registerBlock(60, "farmland", block6);
        registerBlock(61, "furnace", (new BlockFurnace(false)).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("furnace").setCreativeTab(CreativeTabs.DECORATIONS));
        registerBlock(62, "lit_furnace", (new BlockFurnace(true)).setHardness(3.5F).setSoundType(SoundType.STONE).setLightLevel(0.875F).setUnlocalizedName("furnace"));
        registerBlock(63, "standing_sign", (new BlockStandingSign()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("sign").disableStats());
        registerBlock(64, "wooden_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorOak").disableStats());
        registerBlock(65, "ladder", (new BlockLadder()).setHardness(0.4F).setSoundType(SoundType.LADDER).setUnlocalizedName("ladder"));
        registerBlock(66, "rail", (new BlockRail()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("rail"));
        registerBlock(67, "stone_stairs", (new BlockStairs(block.getDefaultState())).setUnlocalizedName("stairsStone"));
        registerBlock(68, "wall_sign", (new BlockWallSign()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("sign").disableStats());
        registerBlock(69, "lever", (new BlockLever()).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("lever"));
        registerBlock(70, "stone_pressure_plate", (new BlockPressurePlate(Material.ROCK, BlockPressurePlate.Sensitivity.MOBS)).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("pressurePlateStone"));
        registerBlock(71, "iron_door", (new BlockDoor(Material.IRON)).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("doorIron").disableStats());
        registerBlock(72, "wooden_pressure_plate", (new BlockPressurePlate(Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("pressurePlateWood"));
        registerBlock(73, "redstone_ore", (new BlockRedstoneOre(false)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreRedstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).setLightLevel(0.625F).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreRedstone"));
        registerBlock(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("notGate"));
        registerBlock(76, "redstone_torch", (new BlockRedstoneTorch(true)).setHardness(0.0F).setLightLevel(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("notGate").setCreativeTab(CreativeTabs.REDSTONE));
        registerBlock(77, "stone_button", (new BlockButtonStone()).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("button"));
        registerBlock(78, "snow_layer", (new BlockSnow()).setHardness(0.1F).setSoundType(SoundType.SNOW).setUnlocalizedName("snow").setLightOpacity(0));
        registerBlock(79, "ice", (new BlockIce()).setHardness(0.5F).setLightOpacity(3).setSoundType(SoundType.GLASS).setUnlocalizedName("ice"));
        registerBlock(80, "snow", (new BlockSnowBlock()).setHardness(0.2F).setSoundType(SoundType.SNOW).setUnlocalizedName("snow"));
        registerBlock(81, "cactus", (new BlockCactus()).setHardness(0.4F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cactus"));
        registerBlock(82, "clay", (new BlockClay()).setHardness(0.6F).setSoundType(SoundType.GROUND).setUnlocalizedName("clay"));
        registerBlock(83, "reeds", (new BlockReed()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("reeds").disableStats());
        registerBlock(84, "jukebox", (new BlockJukebox()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("jukebox"));
        registerBlock(85, "fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.OAK.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("fence"));
        Block block7 = (new BlockPumpkin()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkin");
        registerBlock(86, "pumpkin", block7);
        registerBlock(87, "netherrack", (new BlockNetherrack()).setHardness(0.4F).setSoundType(SoundType.STONE).setUnlocalizedName("hellrock"));
        registerBlock(88, "soul_sand", (new BlockSoulSand()).setHardness(0.5F).setSoundType(SoundType.SAND).setUnlocalizedName("hellsand"));
        registerBlock(89, "glowstone", (new BlockGlowstone(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setLightLevel(1.0F).setUnlocalizedName("lightgem"));
        registerBlock(90, "portal", (new BlockPortal()).setHardness(-1.0F).setSoundType(SoundType.GLASS).setLightLevel(0.75F).setUnlocalizedName("portal"));
        registerBlock(91, "lit_pumpkin", (new BlockPumpkin()).setHardness(1.0F).setSoundType(SoundType.WOOD).setLightLevel(1.0F).setUnlocalizedName("litpumpkin"));
        registerBlock(92, "cake", (new BlockCake()).setHardness(0.5F).setSoundType(SoundType.CLOTH).setUnlocalizedName("cake").disableStats());
        registerBlock(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("diode").disableStats());
        registerBlock(94, "powered_repeater", (new BlockRedstoneRepeater(true)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("diode").disableStats());
        registerBlock(95, "stained_glass", (new BlockStainedGlass(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("stainedGlass"));
        registerBlock(96, "trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("trapdoor").disableStats());
        registerBlock(97, "monster_egg", (new BlockSilverfish()).setHardness(0.75F).setUnlocalizedName("monsterStoneEgg"));
        Block block8 = (new BlockStoneBrick()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stonebricksmooth");
        registerBlock(98, "stonebrick", block8);
        registerBlock(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.WOOD, MapColor.DIRT, block3)).setHardness(0.2F).setSoundType(SoundType.WOOD).setUnlocalizedName("mushroom"));
        registerBlock(100, "red_mushroom_block", (new BlockHugeMushroom(Material.WOOD, MapColor.RED, block4)).setHardness(0.2F).setSoundType(SoundType.WOOD).setUnlocalizedName("mushroom"));
        registerBlock(101, "iron_bars", (new BlockPane(Material.IRON, true)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("fenceIron"));
        registerBlock(102, "glass_pane", (new BlockPane(Material.GLASS, false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("thinGlass"));
        Block block9 = (new BlockMelon()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("melon");
        registerBlock(103, "melon_block", block9);
        registerBlock(104, "pumpkin_stem", (new BlockStem(block7)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkinStem"));
        registerBlock(105, "melon_stem", (new BlockStem(block9)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("pumpkinStem"));
        registerBlock(106, "vine", (new BlockVine()).setHardness(0.2F).setSoundType(SoundType.PLANT).setUnlocalizedName("vine"));
        registerBlock(107, "fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.OAK)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("fenceGate"));
        registerBlock(108, "brick_stairs", (new BlockStairs(block5.getDefaultState())).setUnlocalizedName("stairsBrick"));
        registerBlock(109, "stone_brick_stairs", (new BlockStairs(block8.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT))).setUnlocalizedName("stairsStoneBrickSmooth"));
        registerBlock(110, "mycelium", (new BlockMycelium()).setHardness(0.6F).setSoundType(SoundType.PLANT).setUnlocalizedName("mycel"));
        registerBlock(111, "waterlily", (new BlockLilyPad()).setHardness(0.0F).setSoundType(SoundType.PLANT).setUnlocalizedName("waterlily"));
        Block block10 = (new BlockNetherBrick()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherBrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        registerBlock(112, "nether_brick", block10);
        registerBlock(113, "nether_brick_fence", (new BlockFence(Material.ROCK, MapColor.NETHERRACK)).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherFence"));
        registerBlock(114, "nether_brick_stairs", (new BlockStairs(block10.getDefaultState())).setUnlocalizedName("stairsNetherBrick"));
        registerBlock(115, "nether_wart", (new BlockNetherWart()).setUnlocalizedName("netherStalk"));
        registerBlock(116, "enchanting_table", (new BlockEnchantmentTable()).setHardness(5.0F).setResistance(2000.0F).setUnlocalizedName("enchantmentTable"));
        registerBlock(117, "brewing_stand", (new BlockBrewingStand()).setHardness(0.5F).setLightLevel(0.125F).setUnlocalizedName("brewingStand"));
        registerBlock(118, "cauldron", (new BlockCauldron()).setHardness(2.0F).setUnlocalizedName("cauldron"));
        registerBlock(119, "end_portal", (new BlockEndPortal(Material.PORTAL)).setHardness(-1.0F).setResistance(6000000.0F));
        registerBlock(120, "end_portal_frame", (new BlockEndPortalFrame()).setSoundType(SoundType.GLASS).setLightLevel(0.125F).setHardness(-1.0F).setUnlocalizedName("endPortalFrame").setResistance(6000000.0F).setCreativeTab(CreativeTabs.DECORATIONS));
        registerBlock(121, "end_stone", (new Block(Material.ROCK, MapColor.SAND)).setHardness(3.0F).setResistance(15.0F).setSoundType(SoundType.STONE).setUnlocalizedName("whiteStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(122, "dragon_egg", (new BlockDragonEgg()).setHardness(3.0F).setResistance(15.0F).setSoundType(SoundType.STONE).setLightLevel(0.125F).setUnlocalizedName("dragonEgg"));
        registerBlock(123, "redstone_lamp", (new BlockRedstoneLight(false)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("redstoneLight").setCreativeTab(CreativeTabs.REDSTONE));
        registerBlock(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("redstoneLight"));
        registerBlock(125, "double_wooden_slab", (new BlockDoubleWoodSlab()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("woodSlab"));
        registerBlock(126, "wooden_slab", (new BlockHalfWoodSlab()).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("woodSlab"));
        registerBlock(127, "cocoa", (new BlockCocoa()).setHardness(0.2F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("cocoa"));
        registerBlock(128, "sandstone_stairs", (new BlockStairs(block2.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))).setUnlocalizedName("stairsSandStone"));
        registerBlock(129, "emerald_ore", (new BlockOre()).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreEmerald"));
        registerBlock(130, "ender_chest", (new BlockEnderChest()).setHardness(22.5F).setResistance(1000.0F).setSoundType(SoundType.STONE).setUnlocalizedName("enderChest").setLightLevel(0.5F));
        registerBlock(131, "tripwire_hook", (new BlockTripWireHook()).setUnlocalizedName("tripWireSource"));
        registerBlock(132, "tripwire", (new BlockTripWire()).setUnlocalizedName("tripWire"));
        registerBlock(133, "emerald_block", (new Block(Material.IRON, MapColor.EMERALD)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockEmerald").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(134, "spruce_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE))).setUnlocalizedName("stairsWoodSpruce"));
        registerBlock(135, "birch_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH))).setUnlocalizedName("stairsWoodBirch"));
        registerBlock(136, "jungle_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE))).setUnlocalizedName("stairsWoodJungle"));
        registerBlock(137, "command_block", (new BlockCommandBlock(MapColor.BROWN)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("commandBlock"));
        registerBlock(138, "beacon", (new BlockBeacon()).setUnlocalizedName("beacon").setLightLevel(1.0F));
        registerBlock(139, "cobblestone_wall", (new BlockWall(block)).setUnlocalizedName("cobbleWall"));
        registerBlock(140, "flower_pot", (new BlockFlowerPot()).setHardness(0.0F).setSoundType(SoundType.STONE).setUnlocalizedName("flowerPot"));
        registerBlock(141, "carrots", (new BlockCarrot()).setUnlocalizedName("carrots"));
        registerBlock(142, "potatoes", (new BlockPotato()).setUnlocalizedName("potatoes"));
        registerBlock(143, "wooden_button", (new BlockButtonWood()).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("button"));
        registerBlock(144, "skull", (new BlockSkull()).setHardness(1.0F).setSoundType(SoundType.STONE).setUnlocalizedName("skull"));
        registerBlock(145, "anvil", (new BlockAnvil()).setHardness(5.0F).setSoundType(SoundType.ANVIL).setResistance(2000.0F).setUnlocalizedName("anvil"));
        registerBlock(146, "trapped_chest", (new BlockChest(BlockChest.Type.TRAP)).setHardness(2.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("chestTrap"));
        registerBlock(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.IRON, 15, MapColor.GOLD)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("weightedPlate_light"));
        registerBlock(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted(Material.IRON, 150)).setHardness(0.5F).setSoundType(SoundType.WOOD).setUnlocalizedName("weightedPlate_heavy"));
        registerBlock(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).setHardness(0.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("comparator").disableStats());
        registerBlock(150, "powered_comparator", (new BlockRedstoneComparator(true)).setHardness(0.0F).setLightLevel(0.625F).setSoundType(SoundType.WOOD).setUnlocalizedName("comparator").disableStats());
        registerBlock(151, "daylight_detector", new BlockDaylightDetector(false));
        registerBlock(152, "redstone_block", (new BlockCompressedPowered(Material.IRON, MapColor.TNT)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL).setUnlocalizedName("blockRedstone").setCreativeTab(CreativeTabs.REDSTONE));
        registerBlock(153, "quartz_ore", (new BlockOre(MapColor.NETHERRACK)).setHardness(3.0F).setResistance(5.0F).setSoundType(SoundType.STONE).setUnlocalizedName("netherquartz"));
        registerBlock(154, "hopper", (new BlockHopper()).setHardness(3.0F).setResistance(8.0F).setSoundType(SoundType.METAL).setUnlocalizedName("hopper"));
        Block block11 = (new BlockQuartz()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("quartzBlock");
        registerBlock(155, "quartz_block", block11);
        registerBlock(156, "quartz_stairs", (new BlockStairs(block11.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.DEFAULT))).setUnlocalizedName("stairsQuartz"));
        registerBlock(157, "activator_rail", (new BlockRailPowered()).setHardness(0.7F).setSoundType(SoundType.METAL).setUnlocalizedName("activatorRail"));
        registerBlock(158, "dropper", (new BlockDropper()).setHardness(3.5F).setSoundType(SoundType.STONE).setUnlocalizedName("dropper"));
        registerBlock(159, "stained_hardened_clay", (new BlockColored(Material.ROCK)).setHardness(1.25F).setResistance(7.0F).setSoundType(SoundType.STONE).setUnlocalizedName("clayHardenedStained"));
        registerBlock(160, "stained_glass_pane", (new BlockStainedGlassPane()).setHardness(0.3F).setSoundType(SoundType.GLASS).setUnlocalizedName("thinStainedGlass"));
        registerBlock(161, "leaves2", (new BlockNewLeaf()).setUnlocalizedName("leaves"));
        registerBlock(162, "log2", (new BlockNewLog()).setUnlocalizedName("log"));
        registerBlock(163, "acacia_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA))).setUnlocalizedName("stairsWoodAcacia"));
        registerBlock(164, "dark_oak_stairs", (new BlockStairs(block1.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK))).setUnlocalizedName("stairsWoodDarkOak"));
        registerBlock(165, "slime", (new BlockSlime()).setUnlocalizedName("slime").setSoundType(SoundType.SLIME));
        registerBlock(166, "barrier", (new BlockBarrier()).setUnlocalizedName("barrier"));
        registerBlock(167, "iron_trapdoor", (new BlockTrapDoor(Material.IRON)).setHardness(5.0F).setSoundType(SoundType.METAL).setUnlocalizedName("ironTrapdoor").disableStats());
        registerBlock(168, "prismarine", (new BlockPrismarine()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("prismarine"));
        registerBlock(169, "sea_lantern", (new BlockSeaLantern(Material.GLASS)).setHardness(0.3F).setSoundType(SoundType.GLASS).setLightLevel(1.0F).setUnlocalizedName("seaLantern"));
        registerBlock(170, "hay_block", (new BlockHay()).setHardness(0.5F).setSoundType(SoundType.PLANT).setUnlocalizedName("hayBlock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(171, "carpet", (new BlockCarpet()).setHardness(0.1F).setSoundType(SoundType.CLOTH).setUnlocalizedName("woolCarpet").setLightOpacity(0));
        registerBlock(172, "hardened_clay", (new BlockHardenedClay()).setHardness(1.25F).setResistance(7.0F).setSoundType(SoundType.STONE).setUnlocalizedName("clayHardened"));
        registerBlock(173, "coal_block", (new Block(Material.ROCK, MapColor.BLACK)).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blockCoal").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(174, "packed_ice", (new BlockPackedIce()).setHardness(0.5F).setSoundType(SoundType.GLASS).setUnlocalizedName("icePacked"));
        registerBlock(175, "double_plant", new BlockDoublePlant());
        registerBlock(176, "standing_banner", (new BlockBanner.BlockBannerStanding()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("banner").disableStats());
        registerBlock(177, "wall_banner", (new BlockBanner.BlockBannerHanging()).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("banner").disableStats());
        registerBlock(178, "daylight_detector_inverted", new BlockDaylightDetector(true));
        Block block12 = (new BlockRedSandstone()).setSoundType(SoundType.STONE).setHardness(0.8F).setUnlocalizedName("redSandStone");
        registerBlock(179, "red_sandstone", block12);
        registerBlock(180, "red_sandstone_stairs", (new BlockStairs(block12.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.SMOOTH))).setUnlocalizedName("stairsRedSandStone"));
        registerBlock(181, "double_stone_slab2", (new BlockDoubleStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab2"));
        registerBlock(182, "stone_slab2", (new BlockHalfStoneSlabNew()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneSlab2"));
        registerBlock(183, "spruce_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.SPRUCE)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("spruceFenceGate"));
        registerBlock(184, "birch_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.BIRCH)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("birchFenceGate"));
        registerBlock(185, "jungle_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.JUNGLE)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("jungleFenceGate"));
        registerBlock(186, "dark_oak_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.DARK_OAK)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("darkOakFenceGate"));
        registerBlock(187, "acacia_fence_gate", (new BlockFenceGate(BlockPlanks.EnumType.ACACIA)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("acaciaFenceGate"));
        registerBlock(188, "spruce_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.SPRUCE.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("spruceFence"));
        registerBlock(189, "birch_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.BIRCH.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("birchFence"));
        registerBlock(190, "jungle_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.JUNGLE.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("jungleFence"));
        registerBlock(191, "dark_oak_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.DARK_OAK.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("darkOakFence"));
        registerBlock(192, "acacia_fence", (new BlockFence(Material.WOOD, BlockPlanks.EnumType.ACACIA.getMapColor())).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("acaciaFence"));
        registerBlock(193, "spruce_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorSpruce").disableStats());
        registerBlock(194, "birch_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorBirch").disableStats());
        registerBlock(195, "jungle_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorJungle").disableStats());
        registerBlock(196, "acacia_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorAcacia").disableStats());
        registerBlock(197, "dark_oak_door", (new BlockDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("doorDarkOak").disableStats());
        registerBlock(198, "end_rod", (new BlockEndRod()).setHardness(0.0F).setLightLevel(0.9375F).setSoundType(SoundType.WOOD).setUnlocalizedName("endRod"));
        registerBlock(199, "chorus_plant", (new BlockChorusPlant()).setHardness(0.4F).setSoundType(SoundType.WOOD).setUnlocalizedName("chorusPlant"));
        registerBlock(200, "chorus_flower", (new BlockChorusFlower()).setHardness(0.4F).setSoundType(SoundType.WOOD).setUnlocalizedName("chorusFlower"));
        Block block13 = (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("purpurBlock");
        registerBlock(201, "purpur_block", block13);
        registerBlock(202, "purpur_pillar", (new BlockRotatedPillar(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("purpurPillar"));
        registerBlock(203, "purpur_stairs", (new BlockStairs(block13.getDefaultState())).setUnlocalizedName("stairsPurpur"));
        registerBlock(204, "purpur_double_slab", (new BlockPurpurSlab.Double()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("purpurSlab"));
        registerBlock(205, "purpur_slab", (new BlockPurpurSlab.Half()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("purpurSlab"));
        registerBlock(206, "end_bricks", (new Block(Material.ROCK)).setSoundType(SoundType.STONE).setHardness(0.8F).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setUnlocalizedName("endBricks"));
        registerBlock(207, "beetroots", (new BlockBeetroot()).setUnlocalizedName("beetroots"));
        Block block14 = (new BlockGrassPath()).setHardness(0.65F).setSoundType(SoundType.PLANT).setUnlocalizedName("grassPath").disableStats();
        registerBlock(208, "grass_path", block14);
        registerBlock(209, "end_gateway", (new BlockEndGateway(Material.PORTAL)).setHardness(-1.0F).setResistance(6000000.0F));
        registerBlock(210, "repeating_command_block", (new BlockCommandBlock(MapColor.PURPLE)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("repeatingCommandBlock"));
        registerBlock(211, "chain_command_block", (new BlockCommandBlock(MapColor.GREEN)).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("chainCommandBlock"));
        registerBlock(212, "frosted_ice", (new BlockFrostedIce()).setHardness(0.5F).setLightOpacity(3).setSoundType(SoundType.GLASS).setUnlocalizedName("frostedIce"));
        registerBlock(213, "magma", (new BlockMagma()).setHardness(0.5F).setSoundType(SoundType.STONE).setUnlocalizedName("magma"));
        registerBlock(214, "nether_wart_block", (new Block(Material.GRASS, MapColor.RED)).setCreativeTab(CreativeTabs.BUILDING_BLOCKS).setHardness(1.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("netherWartBlock"));
        registerBlock(215, "red_nether_brick", (new BlockNetherBrick()).setHardness(2.0F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("redNetherBrick").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(216, "bone_block", (new BlockBone()).setUnlocalizedName("boneBlock"));
        registerBlock(217, "structure_void", (new BlockStructureVoid()).setUnlocalizedName("structureVoid"));
        registerBlock(255, "structure_block", (new BlockStructure()).setBlockUnbreakable().setResistance(6000000.0F).setUnlocalizedName("structureBlock"));
        // Being Awaken Dreams code
        registerBlock(500, "jade_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreJade").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(501, "amber_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreAmber").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(502, "tanzanite_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreTanzanite").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(503, "permanent_dirt", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("permanentDirt").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(504, "luindol", (new BlockValarFlower()).setPlantBounds(12, 19).setUnlocalizedName("luindol").setBlockTextureName("luindol"));
        //registerBlock(505, "hopper_mushroom", (new BlockValarCustomRender(Material.plants, 1)).setCreativeTab(CreativeTabs.tabDecorations).setHardness(0.0F).setSoundType(soundTypeGrass).setUnlocalizedName("hopperMushroom").setBlockTextureName("hopper_mushroom"));
        registerBlock(506, "amethyst_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreAmethyst").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(507, "ruby_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreRuby").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(508, "onyx_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreOnyx").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(509, "moonstone_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreMoonstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(511, "minas_morgul_glowstone", (new Block(Material.GLASS)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setUnlocalizedName("minasMorgulGlowstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(512, "argonath_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("argonathStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(513, "mithril_block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blockMithril").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(514, "blue_bed", (new BlockValarBed("blue_bed_item").setHardness(0.2F).setSoundType(soundTypeCloth).setUnlocalizedName("blueBed").setBlockTextureName("blue_bed")));
        //registerBlock(515, "brown_bed", (new BlockValarBed("brown_bed_item")).setHardness(0.2F).setSoundType(soundTypeCloth).setUnlocalizedName("brownBed").setBlockTextureName("brown_bed"));
        registerBlock(516, "crystal_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreCrystal").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(517, "desert_road_block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("desertRoadBlock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(518, "dirt_road_block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dirtRoadBlock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(519, "light_blue_glowstone", (new Block(Material.GLASS)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setUnlocalizedName("lightBlueGlowstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(520, "black_iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("blackIron").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(521, "moria_pillar_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("moriaPillarStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(522, "rusty_iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rustyIron").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(523, "elf_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elfFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(524, "gondorian_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("gondorianFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(525, "stone_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(526, "moria_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("moriaTrapdoor").disableStats().setBlockTextureName("moria_trapdoor"));
        //registerBlock(527, "bree_door", (new BlockValarDoor(Material.wood, "bree_door_item")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("breeDoor").setBlockTextureName("bree_door"));
        //registerBlock(529, "stone_inscription_post", (new BlockValarStoneInscription(TileEntityValarStoneInscription.class, true)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneInscriptionPost").setBlockTextureName("stone_inscription_post"));
        //registerBlock(530, "stone_inscription_wall", (new BlockValarStoneInscription(TileEntityValarStoneInscription.class, false)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("stoneInscriptionWall").setBlockTextureName("stone_inscription_wall"));
        //registerBlock(531, "palm_leaves", (new BlockValarLeaves()).setSaplingBlock("palm_sapling").setUnlocalizedName("palmLeaves").setBlockTextureName("palm_leaves"));
        registerBlock(532, "palm_log", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("palmLog").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(533, "palm_sapling", (new BlockValarSapling()).setPlantBounds(21, 31).setUnlocalizedName("palmSapling").setBlockTextureName("palm_sapling"));
        registerBlock(534, "silk_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("silkStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(535, "cracked_silk_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crackedSilkStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(536, "straw", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("straw").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(537, "cracked_earth", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crackedEarth").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(538, "bag_end_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("bagEndFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(539, "bag_end_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("bagEndWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(540, "brown_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("brownStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(541, "mordor_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mordorStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(542, "cliff_block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("cliffBlock").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(543, "spider_egg", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("spiderEgg").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(544, "bakruel", (new BlockValarCustomRender(Material.plants, 1)).setCreativeTab(CreativeTabs.tabDecorations).setHardness(0.0F).setSoundType(soundTypeGrass).setUnlocalizedName("bakruel").setBlockTextureName("bakruel"));
        //registerBlock(545, "hopperfoot", (new BlockValarCustomRender(Material.plants, 1)).setCreativeTab(CreativeTabs.tabDecorations).setHardness(0.0F).setSoundType(soundTypeGrass).setUnlocalizedName("hopperfoot").setBlockTextureName("hopperfoot"));
        //registerBlock(546, "madarch", (new BlockValarCustomRender(Material.plants, 1)).setCreativeTab(CreativeTabs.tabDecorations).setHardness(0.0F).setSoundType(soundTypeGrass).setUnlocalizedName("madarch").setBlockTextureName("madarch"));
        //registerBlock(547, "mehas", (new BlockValarCustomRender(Material.plants, 1)).setCreativeTab(CreativeTabs.tabDecorations).setHardness(0.0F).setSoundType(soundTypeGrass).setUnlocalizedName("mehas").setBlockTextureName("mehas"));
        //registerBlock(548, "arfandas", (new BlockValarFlower()).setPlantBounds(10, 14, 16).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("arfandas").setBlockTextureName("arfandas"));
        //registerBlock(549, "athelas", (new BlockValarFlower()).setPlantBounds(16, 8, 16).setRenderType(105).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("athelas").setBlockTextureName("athelas"));
        //registerBlock(550, "bellis", (new BlockValarFlower()).setPlantBounds(46, 61, 64).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("bellis").setBlockTextureName("bellis"));
        //registerBlock(551, "lamp", (new BlockValarLamp()).setLampBounds(20, 32).setUnlocalizedName("lamp").setBlockTextureName("lamp"));
        registerBlock(552, "gondorian_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("gondorianStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(553, "gondorian_brick_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("gondorianBrickStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(554, "cracked_gondorian_brick_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crackedGondorianBrickStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(555, "lorien_lamp", (new BlockValarLamp()).setLampBounds(26, 32).setUnlocalizedName("lorienLamp").setBlockTextureName("lorien_lamp"));
        registerBlock(556, "bucklebury_lamp", (new Block3D(Material.GLASS, Arrays.asList(new AxisAlignedBB(4.75 / 16, 0.0D, 4.75 / 16, 11.25 / 16, 1.0D, 11.25 / 16)))).setUnlocalizedName("buckleburyLamp").setCreativeTab(CreativeTabs.DECORATIONS).setLightLevel(1.0F));
        registerBlock(557, "salt_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreSalt").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(558, "simbelmyne", (new BlockValarFlower()).setPlantBounds(16, 6, 16).setRenderType(105).setUnlocalizedName("simbelmyne").setBlockTextureName("simbelmyne"));
        //registerBlock(559, "bree_glass", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("breeGlass").setBlockTextureName("bree_glass"));
        //registerBlock(560, "shire_flower", (new BlockValarFlower()).setPlantBounds(12, 10, 16).setUnlocalizedName("shireFlower").setBlockTextureName("shire_flower"));
        registerBlock(561, "dark_metal", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("darkMetal").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(562, "rivendell_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rivendellWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(563, "moss", (new Block(Material.PLANTS)).setHardness(0.3F).setSoundType(SoundType.PLANT).setUnlocalizedName("moss").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(564, "gondorian_roof", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("gondorianRoof").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(565, "rivendell_roof", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rivendellRoof").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(566, "mossy_gondorian_brick_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mossyGondorianBrickStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(567, "dwarf_door", (new BlockValarDoor(Material.wood, "dwarf_door_item")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfDoor").setBlockTextureName("dwarf_door"));
        //registerBlock(568, "window", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("window").setBlockTextureName("window"));
        registerBlock(569, "rohan_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rohanBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(570, "mordor_brick_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mordorBrickStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(571, "mithril_ore", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oreMithril").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(572, "bree_glass_pane", (new BlockValarPane("bree_glass", "bree_glass", Material.glass, false)).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("breeGlassPane").setBlockTextureName("bree_glass"));
        //registerBlock(573, "arlans_slipper", (new BlockValarFlower()).setUnlocalizedName("arlansSlipper").setBlockTextureName("arlans_slipper"));
        registerBlock(574, "numenorean", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("numenorean").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(575, "dwarf_inner_wall_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfInnerWallDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(576, "dale_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("daleStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(577, "dwarf_inner_wall_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfInnerWallStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(578, "red_lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("redLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(579, "purple_lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("purpleLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(580, "light_blue_lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("lightBlueLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(581, "green_lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("greenLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(582, "dwarf_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(583, "brown_lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("brownLapis").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(584, "dwarf_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(585, "dwarven_hall_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenHallFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(586, "dwarf_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(587, "dwarven_torch", (new BlockValarTorch()).setLightLevel(0.9375F).setUnlocalizedName("dwarvenTorch").setBlockTextureName("dwarven_torch"));
        registerBlock(588, "dwarven_gold", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenGold").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(589, "rohan_iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rohanIron").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(590, "dark_dwarf_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("darkDwarfStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(591, "medium_dark_dwarf_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mediumDarkDwarfStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(592, "mud_block", (new BlockValarMud()).setUnlocalizedName("mudBlock").setBlockTextureName("mud_block"));
        //registerBlock(593, "gondorian_brick_stone_stairs", (new BlockValarDeprecated(1303)));
        registerBlock(594, "light_brown_wood", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.WOOD).setUnlocalizedName("lightBrownWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(595, "old_tree", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("oldTree").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(596, "bree_bookshelf", (new BlockValarBookshelf(Material.wood)).setHardness(1.5F).setSoundType(soundTypeWood).setUnlocalizedName("breeBookshelf").setBlockTextureName("bree_bookshelf"));
        registerBlock(597, "cross_hay", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crossHay").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(598, "light_grey_circle_stone", (new Block3D(Material.GLASS)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("lightGreyCircleStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(599, "hobbit_lamp_1", (new BlockValarLamp()).setLampBounds(16, 32).setUnlocalizedName("hobbitLamp1").setBlockTextureName("hobbit_lamp_1"));
        //registerBlock(600, "hobbit_lamp_2", (new BlockValarLamp()).setLampBounds(16, 32).setUnlocalizedName("hobbitLamp2").setBlockTextureName("hobbit_lamp_2"));
        registerBlock(601, "lossarnarch_decoration_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("lossarnarchDecorationStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //float[][] breeLampBumpMap = { /* Frame */ { 0, 0, 0, 32, 4, 4 }, { 0, 0, 28, 32, 4, 32 }, { 0, 0, 4, 4, 4, 28 }, { 28, 0, 4, 32, 4, 28 }, { 0, 28, 0, 32, 32, 4 }, { 0, 28, 28, 32, 32, 32 }, { 0, 28, 4, 4, 32, 28 }, { 28, 28, 4, 32, 32, 28 }, { 0, 4, 0, 4, 28, 4 }, { 28, 4, 0, 32, 28, 4 }, { 0, 4, 28, 4, 28, 32 }, { 28, 4, 28, 32, 28, 32 }, { 4, 4, 4, 28, 28, 28 }, /* Bottom */ { 13, 2, 4, 19, 4, 5 }, { 12, 2, 5, 14, 4, 6 }, { 18, 2, 5, 20, 4, 6 }, { 11, 2, 6, 13, 4, 7 }, { 19, 2, 6, 21, 4, 7 }, { 10, 2, 7, 12, 4, 8 }, { 20, 2, 7, 22, 4, 8 }, { 8, 2, 8, 11, 4, 10 }, { 21, 2, 8, 25, 4, 9 }, { 7, 2, 7, 9, 4, 8 }, { 24, 2, 7, 26, 4, 8 }, { 6, 2, 6, 8, 4, 7 }, { 25, 2, 6, 27, 4, 7 }, { 4, 2, 5, 7, 4, 6 }, { 26, 2, 5, 28, 4, 6 }, { 4, 2, 4, 5, 4, 5 }, { 27, 2, 4, 28, 4, 5 }, { 22, 2, 9, 24, 4, 11 }, { 10, 2, 10, 12, 4, 11 }, { 21, 2, 10, 22, 4, 12 }, { 11, 2, 11, 13,  4, 12 }, { 20, 2, 11, 21, 4, 13 }, { 12, 2, 12, 14, 4, 13 }, { 19, 2, 12, 20, 4, 14 }, { 13, 2, 13, 15, 4, 14 }, { 18, 2, 13, 19, 4, 15 }, { 14, 2, 14, 16, 4, 15 }, { 17, 2, 14, 18, 4, 16 }, { 15, 2, 15, 17, 4, 16 }, { 7, 2, 10, 9, 4, 11 }, { 24, 2, 10, 25, 4, 12 }, { 6, 2, 11, 8, 4, 12 }, { 25, 2, 11, 26, 4, 13 }, { 5, 2, 12, 7, 4, 13 }, { 26, 2, 12, 27, 4, 14 }, { 4, 2, 13, 6, 4, 14 }, { 27, 2, 13, 28, 4, 17 }, { 4, 2, 14, 5, 4, 17 }, { 15, 2, 16, 18, 4, 17 }, { 14, 2, 17, 16, 4, 18 }, { 17, 2, 17, 19, 4, 18 }, { 13, 2, 18, 15, 4, 19 }, { 18, 2, 18, 20, 4, 19 }, { 12, 2, 19, 14, 4, 20 }, { 19, 2, 19, 21, 4, 20 }, { 11, 2, 20, 13, 4, 21 }, { 20, 2, 20, 22, 4, 21 }, { 8, 2, 21, 12, 4, 22 }, { 21, 2, 21, 24, 4, 23 }, { 4, 2, 17, 6, 4, 18 }, { 26, 2, 17, 28, 4, 18 }, { 5, 2, 18, 7, 4, 19 }, { 25, 2, 18, 27, 4, 19 }, { 6, 2, 19, 8, 4, 20 }, { 24, 2, 19, 26, 4, 20 }, { 7, 2, 20, 9, 4, 21 }, { 23, 2, 20, 25, 4, 21 }, { 9, 2, 22, 11, 4, 23 }, { 8, 2, 23, 12, 4, 24 }, { 20, 2, 23, 22, 4, 24 }, { 11, 2, 24, 13, 4, 25 }, { 19, 2, 24, 21, 4, 25 }, { 12, 2, 25, 14, 4, 26 }, { 18, 2, 25, 20, 4, 26 }, { 13, 2, 26, 15, 4, 27 }, { 17, 2, 26, 19, 4, 27 }, { 14, 2, 27, 18, 4, 28 }, { 23, 2, 23, 25, 4, 24 }, { 7, 2, 24, 9, 4, 25 }, { 24, 2, 24, 26, 4, 25 }, { 6, 2, 25, 8, 4, 26 }, { 25, 2, 25, 27, 4, 26 }, { 5, 2, 26, 7, 4, 27 }, { 26, 2, 26, 28, 4, 27 }, { 4, 2, 26, 5, 4, 28 }, { 27, 2, 27, 28, 4, 28 }, /* Top * { 13, 28, 4, 19, 30, 5 }, { 12, 28, 5, 14, 30, 6 }, { 18, 28, 5, 20, 30, 6 }, { 11, 28, 6, 13, 30, 7 }, { 19, 28, 6, 21, 30, 7 }, { 10, 28, 7, 12, 30, 8 }, { 20, 28, 7, 22, 30, 8 }, { 8, 28, 8, 11, 30, 10 }, { 21, 28, 8, 25, 30, 9 }, { 7, 28, 7, 9, 30, 8 }, { 24, 28, 7, 26, 30, 8 }, { 6, 28, 6, 8, 30, 7 }, { 25, 28, 6, 27, 30, 7 }, { 4, 28, 5, 7, 30, 6 }, { 26, 28, 5, 28, 30, 6 }, { 4, 28, 4, 5, 30, 5 }, { 27, 28, 4, 28, 30, 5 }, { 22, 28, 9, 24, 30, 11 }, { 10, 28, 10, 12, 30, 11 }, { 21, 28, 10, 22, 30, 12 }, { 11, 28, 11, 13, 30, 12 }, { 20, 28, 11, 21, 30, 13 }, { 12, 28, 12, 14, 30, 13 }, { 19, 28, 12, 20, 30, 14 }, { 13, 28, 13, 15, 30, 14 }, { 18, 28, 13, 19, 30, 15 }, { 14, 28, 14, 16, 30, 15 }, { 17, 28, 14, 18, 30, 16 }, { 15, 28, 15, 17, 30, 16 }, { 7, 28, 10, 9, 30, 11 }, { 24, 28, 10, 25, 30, 12 }, { 6, 28, 11, 8, 30, 12 }, { 25, 28, 11, 26, 30, 13 }, { 5, 28, 12, 7, 30, 13 }, { 26, 28, 12, 27, 30, 14 }, { 4, 28, 13, 6, 30, 14 }, { 27, 28, 13, 28, 30, 17 }, { 4, 28, 14, 5, 30, 17 }, { 15, 28, 16, 18, 30, 17 }, { 14, 28, 17, 16, 30, 18 }, { 17, 28, 17, 19, 30, 18 }, { 13, 28, 18, 15, 30, 19 }, { 18, 28, 18, 20, 30, 19 }, { 12, 28, 19, 14, 30, 20 }, { 19, 28, 19, 21, 30, 20 }, { 11, 28, 20, 13, 30, 21 }, { 20, 28, 20, 22, 30, 21 }, { 8, 28, 21, 12, 30, 22 }, { 21, 28, 21, 24, 30, 23 }, { 4, 28, 17, 6, 30, 18 }, { 26, 28, 17, 28, 30, 18 }, { 5, 28, 18, 7, 30, 19 }, { 25, 28, 18, 27, 30, 19 }, { 6, 28, 19, 8, 30, 20 }, { 24, 28, 19, 26, 30, 20 }, { 7, 28, 20, 9, 30, 21 }, { 23, 28, 20, 25, 30, 21 }, { 9, 28, 22, 11, 30, 23 }, { 8, 28, 23, 12, 30, 24 }, { 20, 28, 23, 22, 30, 24 }, { 11, 28, 24, 13, 30, 25 }, { 19, 28, 24, 21, 30, 25 }, { 12, 28, 25, 14, 30, 26 }, { 18, 28, 25, 20, 30, 26 }, { 13, 28, 26, 15, 30, 27 }, { 17, 28, 26, 19, 30, 27 }, { 14, 28, 27, 18, 30, 28 }, { 23, 28, 23, 25, 30, 24 }, { 7, 28, 24, 9, 30, 25 }, { 24, 28, 24, 26, 30, 25 }, { 6, 28, 25, 8, 30, 26 }, { 25, 28, 25, 27, 30, 26 }, { 5, 28, 26, 7, 30, 27 }, { 26, 28, 26, 28, 30, 27 }, { 4, 28, 26, 5, 30, 28 }, { 27, 28, 27, 28, 30, 28 }, /* Side 1 */ };
        //registerBlock(602, "bree_lamp", (new BlockValarBumpMapped(Material.glass)).setBumpMapping(breeLampBumpMap).setRenderItemAs3D(true).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setLightLevel(1.0F).setUnlocalizedName("breeLamp").setBlockTextureName("bree_lamp"));
        //registerBlock(603, "archet_lamp", (new BlockValarArchetLamp(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("archetLamp").setBlockTextureName("archet_lamp"));
        //registerBlock(604, "gondorian_brick_stone_slab", (new BlockValarDeprecated(2053)));
        //registerBlock(605, "gondorian_brick_stone_double_slab", (new BlockValarDeprecated(553)));
        //registerBlock(606, "town_marker", (new BlockValarMultitextured(Material.ROCK, "marker_top", "town_marker")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("townMarker"));
        //registerBlock(607, "village_marker", (new BlockValarMultitextured(Material.ROCK, "marker_top", "village_marker")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("villageMarker"));
        //registerBlock(608, "ruin_marker", (new BlockValarMultitextured(Material.ROCK, "marker_top", "ruin_marker")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("ruinMarker"));
        registerBlock(609, "elven_stone_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenStoneFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(610, "ancient_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("ancientStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(611, "bree_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("breeStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(612, "cracked_bree_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crackedBreeStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(613, "mossy_bree_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mossyBreeStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(614, "shire_hay", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("shireHay").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(615, "bree_oak_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("breeOakPlanks").setBlockTextureName("bree_oak_planks"));
        //registerBlock(616, "bree_spruce_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("breeSprucePlanks").setBlockTextureName("bree_spruce_planks"));
        //registerBlock(617, "bree_birch_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("breeBirchPlanks").setBlockTextureName("bree_birch_planks"));
        //registerBlock(618, "bree_jungle_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("breeJunglePlanks").setBlockTextureName("bree_jungle_planks"));
        registerBlock(619, "shire_path", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("shirePath").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(620, "bree_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("breeFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(621, "arnor_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("arnorFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(622, "cardolan_brick_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("cardolanBrickStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(623, "elven_sandstone_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenSandstoneFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(624, "dead_lava", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("deadLava").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(625, "chiseled_gondorian_stone", (new Block3D(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("chiseledGondorianStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(626, "needles", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("needles").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(627, "rhun_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rhunFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(628, "khand_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("khandFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(629, "hobbit_window", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("hobbitWindow").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(630, "old_toby_crop", (new BlockValarDeprecated(774)).setUnlocalizedName("oldTobyCrop").setBlockTextureName("old_toby_crop"));
        //registerBlock(631, "city_marker", (new BlockValarMultitextured(Material.ROCK, "marker_top", "city_marker")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("cityMarker"));
        registerBlock(632, "rivendell_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rivendellFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(633, "column", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("column").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(634, "column_top", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("columnTop").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(635, "dwarf_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(636, "dwarf_floor2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(637, "dwarf_floor3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor3").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(638, "dwarf_floor4", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor4").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(639, "dwarf_floor5", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor5").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(640, "dwarf_floor6", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor6").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(641, "dwarf_floor7", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor7").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(642, "dwarf_floor8", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor8").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(643, "dwarf_floor9", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfFloor9").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(644, "dwarf_king_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarfKingStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(645, "dwarven_king_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenKingFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(646, "dwarven_king_floor2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenKingFloor2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(647, "dwarven_pillar_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenPillarDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(648, "dwarven_steel", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dwarvenSteel").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(649, "erebor_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("ereborFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(650, "erebor_floor2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("ereborFloor2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(651, "ered_luin_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("eredLuinStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(652, "iron_hills_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("ironHillsFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(653, "smooth_gold", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("smoothGold").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(654, "rivendell_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rivendellWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(655, "sindar_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(656, "sindar_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(657, "sindar_floor2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarFloor2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(658, "sindar_decoration_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarDecorationStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(659, "noldor_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("noldorFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(660, "noldor_sandstone_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("noldorSandstoneFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(661, "mallorn_woodplanks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mallornWoodplanks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(662, "lindon_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("lindonWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(663, "lindon_wall_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("lindonWallDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(664, "high_elf_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("highElfWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(665, "harlindon_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("harlindonWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(666, "forlond_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("forlondFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(667, "forlindon_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("forlindonWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(668, "forlindon_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("forlindonWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(669, "elven_stone_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenStoneWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(670, "elven_noble_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenNobleWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(671, "rivendell_statue_bottom", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rivendellStatueBottom").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(672, "elven_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(673, "elven_decoration_light", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenDecorationLight").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(674, "elven_decoration2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenDecoration2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(675, "elven_decoration3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenDecoration3").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(676, "hobbit_floor", (new Block3D(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("hobbitFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(677, "hobbit_floor2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("hobbitFloor2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(678, "dark_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("darkBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(679, "rohirrim_crossbeam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rohirrimCrossbeam").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(680, "crossbeam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crossbeam").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(681, "crossbeam2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crossbeam2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(682, "crossbeam3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("crossbeam3").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(683, "standard_crossbeam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("standardCrossbeam").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(684, "standard_crossbeam2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("standardCrossbeam2").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(685, "standard_crossbeam3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("standardCrossbeam3").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(686, "vertical_beam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("verticalBeam").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(687, "vertical_horizontal_beam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("verticalHorizontalBeam").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(689, "dale_sandstone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("daleSandstone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(690, "dale_tiles", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("daleTiles").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(691, "dale_wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("daleWall").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(692, "harad_sandstone_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("haradSandstoneFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(693, "harad_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("haradStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(694, "sandfloor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sandfloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(695, "umbar_haven_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("umbarHavenFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(696, "umbar_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("umbarStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(697, "umbar_wall_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("umbarWallDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(698, "khand_stone_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("khandStoneBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(699, "brown_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("brownBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(700, "edoras_cobblestone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("edorasCobblestone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(701, "reinforced_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("reinforcedWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(702, "meduseld_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("meduseldWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(703, "rohirrim_wall_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("rohirrimWallDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(704, "statue_head", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("statueHead").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(705, "rohirrim_oak_wood_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("rohirrimOakWoodPlanks").setBlockTextureName("rohirrim_oak_wood_planks"));
        //registerBlock(706, "rohirrim_spruce_wood_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("rohirrimSpruceWoodPlanks").setBlockTextureName("rohirrim_spruce_wood_planks"));
        //registerBlock(707, "rohirrim_birch_wood_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("rohirrimBirchWoodPlanks").setBlockTextureName("rohirrim_birch_wood_planks"));
        //registerBlock(708, "rohirrim_jungle_wood_planks", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setResistance(5.0F).setSoundType(soundTypeWood).setUnlocalizedName("rohirrimJungleWoodPlanks").setBlockTextureName("rohirrim_jungle_wood_planks"));
        registerBlock(709, "angmar_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("angmarFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(710, "angmar_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("angmarBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(711, "beorning_wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("beorningWood").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(712, "annuminas_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("annuminasDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(713, "arnor_decoration_jewel", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("arnorDecorationJewel").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(714, "arnorian_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("arnorianBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(715, "minas_tirith_floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("minasTirithFloor").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(716, "pelargir_stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("pelargirStone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(717, "white_cobblestone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("whiteCobblestone").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(718, "dunland_totem", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dunlandTotem").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(719, "dunland_wall_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dunlandWallDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(720, "mirkwood_web", (new BlockValarWeb()).setLightOpacity(1).setHardness(4.0F).setUnlocalizedName("mirkwoodWeb").setBlockTextureName("mirkwood_web"));
        //registerBlock(721, "dale_window", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("daleWindow").setBlockTextureName("dale_window"));
        //registerBlock(722, "dale_window_2", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setUnlocalizedName("daleWindow2").setBlockTextureName("dale_window_2"));
        //registerBlock(723, "harad_window", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("haradWindow").setBlockTextureName("harad_window"));
        registerBlock(724, "mordor_lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setUnlocalizedName("mordorLamp").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(725, "harad_light", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("haradLight").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(726, "elven_lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenLamp").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(727, "carn_dum_lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("carnDumLamp").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(728, "elven_door", (new BlockValarDoor(Material.wood, "elven_door_item")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("elvenDoor").setBlockTextureName("elven_door"));
        //registerBlock(729, "human_door", (new BlockValarDoor(Material.wood, "human_door_item")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("humanDoor").setBlockTextureName("human_door"));
        //registerBlock(730, "green_bed", (new BlockValarBed("green_bed_item")).setHardness(0.2F).setSoundType(soundTypeCloth).setUnlocalizedName("greenBed").setBlockTextureName("green_bed"));
        registerBlock(731, "bree_tile", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("breeTile").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(732, "bree_tile_stairs", (new BlockValarDeprecated(1481)));
        //registerBlock(733, "bree_tile_slab", (new BlockValarDeprecated(2231)));
        //registerBlock(734, "numenorean_stairs", (new BlockValarDeprecated(1324)));
        //registerBlock(735, "numenorean_slab", (new BlockValarDeprecated(2074)));
        //registerBlock(736, "human_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("humanTrapdoor").disableStats().setBlockTextureName("human_trapdoor"));
        //registerBlock(737, "mordor_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("mordorTrapdoor").disableStats().setBlockTextureName("mordor_trapdoor"));
        //registerBlock(738, "khandish_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("khandishTrapdoor").disableStats().setBlockTextureName("khandish_trapdoor"));
        //registerBlock(739, "sindar_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("sindarTrapdoor").disableStats().setBlockTextureName("sindar_trapdoor"));
        //registerBlock(740, "riverfolk_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("riverfolkTrapdoor").disableStats().setBlockTextureName("riverfolk_trapdoor"));
        //registerBlock(741, "fornost_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("fornostTrapdoor").disableStats().setBlockTextureName("fornost_trapdoor"));
        //registerBlock(742, "prison_trapdoor", (new BlockValarTrapDoor(Material.wood, true)).setHardness(3.0F).setSoundType(soundTypeWood).setUnlocalizedName("prisonTrapdoor").disableStats().setBlockTextureName("prison_trapdoor"));
        //float[][] candleBumpMap = {{ 13, 0, 13, 19, 2, 19 }, { 11, 2, 13, 21, 3, 19 }, { 12, 2, 12, 20, 3, 13 }, { 12, 2, 19, 20, 3, 20 }, { 13, 2, 11, 19, 3, 12 }, { 13, 2, 20, 19, 3, 21 }, { 10, 3, 12, 22, 4, 20 }, { 11, 3, 11, 21, 4, 12 }, { 11, 3, 20, 21, 4, 21 }, { 12, 3, 10, 20, 4, 11 }, { 12, 3, 21, 20, 4, 22 }, { 9, 4, 11, 23, 5, 21 }, { 10, 4, 10, 22, 5, 11 }, { 10, 4, 21, 22, 5, 22 }, { 11, 4, 9, 21, 5, 10 }, { 11, 4, 22, 21, 5, 23 }, { 8, 5, 10, 24, 6, 22 }, { 9, 5, 9, 23, 6, 10 }, { 9, 5, 22, 23, 6, 23 }, { 10, 5, 8, 22, 6, 9 }, { 10, 5, 23, 22, 6, 24 }, { 13, 6, 15, 19, 24, 17 }, { 14, 6, 14, 18, 24, 15 }, { 14, 6, 17, 18, 24, 18 }, { 15, 6, 13, 17, 24, 14 }, { 15, 6, 18, 17, 24, 19 }, { 15, 24, 15, 17, 28, 17 }};
        //registerBlock(743, "candle", (new BlockValarCandle(Material.ROCK)).setBumpMapping(candleBumpMap).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setLightLevel(0.5F).setUnlocalizedName("candle").setBlockTextureName("candle"));
        //registerBlock(744, "human_ladder", (new BlockValarLadder()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeLadder).setUnlocalizedName("humanLadder").setBlockTextureName("human_ladder"));
        //registerBlock(745, "rohirrim_ladder", (new BlockValarLadder()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeLadder).setUnlocalizedName("rohirrimLadder").setBlockTextureName("rohirrim_ladder"));
        //registerBlock(746, "dunland_ladder", (new BlockValarLadder()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeLadder).setUnlocalizedName("dunlandLadder").setBlockTextureName("dunland_ladder"));
        //registerBlock(747, "sindar_chest", (new BlockValarChest(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarChest").setBlockTextureName("sindar_chest"));
        //registerBlock(748, "castle_door", (new BlockValarDoor(Material.wood, "castle_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("castleDoor").setBlockTextureName("castle_door"));
        //registerBlock(749, "dol_guldur_prison_door", (new BlockValarDoor(Material.ROCK, "dol_guldur_prison_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("dolGuldurPrisonDoor").setBlockTextureName("dol_guldur_prison_door"));
        //registerBlock(750, "hillmen_door", (new BlockValarDoor(Material.wood, "hillmen_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("hillmenDoor").setBlockTextureName("hillmen_door"));
        //registerBlock(751, "mordor_door", (new BlockValarDoor(Material.wood, "mordor_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("mordorDoor").setBlockTextureName("mordor_door"));
        //registerBlock(752, "prison_door", (new BlockValarDoor(Material.wood, "prison_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("prisonDoor").setBlockTextureName("prison_door"));
        //registerBlock(753, "sindar_door", (new BlockValarDoor(Material.wood, "sindar_door")).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("sindarDoor").setBlockTextureName("sindar_door"));
        //registerBlock(754, "yellow_leaves", (new BlockValarLeaves().setSaplingBlock("mallorn_sapling").setUnlocalizedName("yellowLeaves").setBlockTextureName("yellow_leaves"))); // TODO Change Mallorn leaf color based on time of year
        //registerBlock(755, "mallorn_sapling", (new BlockValarSapling()).setPlantBounds(232, 251, 256).setUnlocalizedName("mallornSapling").setBlockTextureName("mallorn_sapling"));
        //registerBlock(756, "brown_leaves", (new BlockValarLeaves().setUnlocalizedName("brownLeaves").setBlockTextureName("brown_leaves")));
        //registerBlock(757, "red_leaves", (new BlockValarLeaves().setUnlocalizedName("redLeaves").setBlockTextureName("red_leaves")));
        //registerBlock(758, "old_tree_sapling", (new BlockValarSapling()).setPlantBounds(56, 51, 64).setUnlocalizedName("oldTreeSapling").setBlockTextureName("old_tree_sapling"));
        //registerBlock(759, "light_brown_tree_sapling", (new BlockValarSapling()).setPlantBounds(22, 32).setUnlocalizedName("lightBrownTreeSapling").setBlockTextureName("light_brown_tree_sapling"));
        //registerBlock(760, "dead_tree_sapling", (new BlockValarSapling()).setPlantBounds(58, 45, 64).setUnlocalizedName("deadTreeSapling").setBlockTextureName("dead_tree_sapling"));
        //registerBlock(761, "bush", (new BlockValarFlower()).setPlantBounds(31, 27).setUnlocalizedName("bush").setBlockTextureName("bush"));
        //registerBlock(762, "dead_bush", (new BlockValarFlower()).setPlantBounds(30, 25).setUnlocalizedName("deadBush").setBlockTextureName("dead_bush"));
        //registerBlock(763, "cursed_plant", (new BlockValarFlower()).setPlantBounds(32, 26).setUnlocalizedName("cursedPlant").setBlockTextureName("cursed_plant"));
        //registerBlock(764, "haradwaith_fern", (new BlockValarFlower()).setPlantBounds(28, 31).setUnlocalizedName("haradwaithFern").setBlockTextureName("haradwaith_fern"));
        //registerBlock(765, "lorilendel", (new BlockValarFlower()).setPlantBounds(30, 22).setRenderType(106).setUnlocalizedName("lorilendel").setBlockTextureName("lorilendel"));
        //registerBlock(766, "stakes", (new BlockValarCustomRender(Material.plants, CreativeTabs.tabDecorations, 106).setUnlocalizedName("stakes").setBlockTextureName("stakes")));
        //registerBlock(767, "southlinch", (new BlockValarCrops(5)).setRenderType(1).setItemSeed("southlinch_seed").setItemYield("south_linch").setUnlocalizedName("southlinch").setBlockTextureName("southlinch"));
        //registerBlock(768, "green_grape", (new BlockValarCrops(5)).setRenderType(106).setItemSeed("green_grape_seed").setItemYield("green_grapes").setRequiresStakes(true).setUnlocalizedName("greenGrape").setBlockTextureName("green_grape"));
        //registerBlock(769, "purple_grape", (new BlockValarCrops(5)).setRenderType(106).setItemSeed("purple_grape_seed").setItemYield("purple_grapes").setRequiresStakes(true).setUnlocalizedName("purpleGrape").setBlockTextureName("purple_grape"));
        //registerBlock(770, "pipeweed_plant", (new BlockValarCrops(5)).setRenderType(1).setItemSeed("pipeweed_seed").setUnlocalizedName("pipeweedPlant").setBlockTextureName("pipeweed_plant"));
        //registerBlock(771, "pea_plant", (new BlockValarCrops(5)).setRenderType(1).setItemSeed("pea_seed").setItemYield("peas").setUnlocalizedName("peaPlant").setBlockTextureName("pea_plant"));
        //registerBlock(772, "leek_plant", (new BlockValarCrops(5)).setRenderType(1).setItemSeed("leek_seed").setItemYield("leek").setUnlocalizedName("leekPlant").setBlockTextureName("leek_plant"));
        //registerBlock(773, "onion_plant", (new BlockValarCrops(5)).setRenderType(1).setItemSeed("onion_seed").setItemYield("onion").setUnlocalizedName("onionPlant").setBlockTextureName("onion_plant"));
        //registerBlock(774, "old_toby_plant", (new BlockValarCrops(3)).setRenderType(1).setItemSeed("old_toby_seed").setItemYield("old_toby").setUnlocalizedName("oldTobyPlant").setBlockTextureName("old_toby_plant"));
        //registerBlock(775, "southern_star_plant", (new BlockValarCrops(3)).setRenderType(1).setItemSeed("southern_star_seed").setItemYield("southern_star").setUnlocalizedName("southernStarPlant").setBlockTextureName("southern_star_plant"));
        //registerBlock(776, "strawberry_bush", (new BlockValarCrops(4)).setRenderType(1).setItemSeed("strawberry_seed").setItemYield("strawberry").setUnlocalizedName("strawberryBush").setBlockTextureName("strawberry_bush"));
        //registerBlock(777, "shire_garden_flower", (new BlockValarFlower()).setPlantBounds(12, 10, 16).setUnlocalizedName("shireGardenFlower").setBlockTextureName("shire_garden_flower"));
        registerBlock(778, "pile_of_coins", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("pileOfCoins").setCreativeTab(CreativeTabs.BUILDING_BLOCKS)); // TODO custom step sound
        registerBlock(779, "grey_column", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("greyColumn").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(780, "grey_column_top", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("greyColumnTop").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(781, "column_top_decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("columnTopDecoration").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(782, "meduseld_pillar", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("meduseldPillar").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerBlock(783, "diagonal_bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setUnlocalizedName("diagonalBricks").setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerBlock(784, "dark_brown_wood", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setSoundType(soundTypeWood).setUnlocalizedName("darkBrownWood").setBlockTextureName("dark_brown_wood"));
        // End Awaken Dreams code
        REGISTRY.validateKey();

        for (Block block15 : REGISTRY)
        {
            if (block15.blockMaterial == Material.AIR)
            {
                block15.useNeighborBrightness = false;
            }
            else
            {
                boolean flag = false;
                boolean flag1 = block15 instanceof BlockStairs;
                boolean flag2 = block15 instanceof BlockSlab;
                boolean flag3 = block15 == block6 || block15 == block14;
                boolean flag4 = block15.translucent;
                boolean flag5 = block15.lightOpacity == 0;

                if (flag1 || flag2 || flag3 || flag4 || flag5)
                {
                    flag = true;
                }

                block15.useNeighborBrightness = flag;
            }
        }

        Set<Block> set = Sets.newHashSet(new Block[] {(Block)REGISTRY.getObject(new ResourceLocation("tripwire"))});

        for (Block block16 : REGISTRY)
        {
            if (set.contains(block16))
            {
                for (int i = 0; i < 15; ++i)
                {
                    int j = REGISTRY.getIDForObject(block16) << 4 | i;
                    BLOCK_STATE_IDS.put(block16.getStateFromMeta(i), j);
                }
            }
            else
            {
                for (IBlockState iblockstate : block16.getBlockState().getValidStates())
                {
                    int k = REGISTRY.getIDForObject(block16) << 4 | block16.getMetaFromState(iblockstate);
                    BLOCK_STATE_IDS.put(iblockstate, k);
                }
            }
        }
    }

    private static void registerBlock(int id, ResourceLocation textualID, Block block_)
    {
        REGISTRY.register(id, textualID, block_);
    }

    private static void registerBlock(int id, String textualID, Block block_)
    {
        registerBlock(id, new ResourceLocation(textualID), block_);
    }

    public static enum EnumOffsetType
    {
        NONE,
        XZ,
        XYZ;
    }
}
