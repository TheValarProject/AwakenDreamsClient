package net.minecraft.block;

import com.elementfx.tvp.ad.block.Block3D;
import com.elementfx.tvp.ad.block.Block3DDirectional;
import com.elementfx.tvp.ad.block.BlockArchetLamp;
import com.elementfx.tvp.ad.block.BlockBell;
import com.elementfx.tvp.ad.block.BlockCandle;
import com.elementfx.tvp.ad.block.BlockCustomBed;
import com.elementfx.tvp.ad.block.BlockCustomCrops;
import com.elementfx.tvp.ad.block.BlockCustomDoor;
import com.elementfx.tvp.ad.block.BlockCustomFlower;
import com.elementfx.tvp.ad.block.BlockCustomOre;
import com.elementfx.tvp.ad.block.BlockCustomWorkbench;
import com.elementfx.tvp.ad.block.BlockDwarvenTorch;
import com.elementfx.tvp.ad.block.BlockLamp;
import com.elementfx.tvp.ad.block.BlockStakePlant;
import com.elementfx.tvp.ad.block.BlockStakes;
import com.elementfx.tvp.ad.block.BlockWaterWheel;
import com.elementfx.tvp.ad.util.ADResourceLocation;
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
import net.minecraft.init.Items;
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
        registerADBlock(500, "jade_Ore", new BlockCustomOre());
        registerADBlock(501, "amber_Ore", new BlockCustomOre());
        registerADBlock(502, "tanzanite_Ore", new BlockCustomOre());
        registerADBlock(503, "permanent_Dirt", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE)); // Deprecated in favor of coarse dirt
        registerADBlock(504, "luindol", new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(12, 19)));
        registerADBlock(505, "hopper_Mushroom", new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(4, 11, 16)));
        registerADBlock(506, "amethyst_Ore", new BlockCustomOre());
        registerADBlock(507, "ruby_Ore", new BlockCustomOre());
        registerADBlock(508, "onyx_Ore", new BlockCustomOre());
        registerADBlock(509, "moonstone_Ore", new BlockCustomOre());
        registerADBlock(511, "minas_Morgul_Glowstone", (new Block(Material.GLASS)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(512, "argonath_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(513, "mithril_Block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(514, "blue_Bed", new BlockCustomBed());
        registerADBlock(515, "brown_Bed", new BlockCustomBed());
        registerADBlock(516, "crystal_Ore", new BlockCustomOre(2, 5));
        registerADBlock(517, "desert_Road_Block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(518, "dirt_Road_Block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(519, "light_Blue_Glowstone", (new Block(Material.GLASS)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(520, "black_Iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(521, "moria_Pillar_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(522, "rusty_Iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(523, "elf_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(524, "gondorian_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(525, "stone_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(526, "moria_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(527, "bree_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        //registerADBlock(529, "stone_Inscription_Post", (new BlockValarStoneInscription(TileEntityValarStoneInscription.class, true)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setBlockTextureName("stone_inscription_post"));
        //registerADBlock(530, "stone_Inscription_Wall", (new BlockValarStoneInscription(TileEntityValarStoneInscription.class, false)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setBlockTextureName("stone_inscription_wall"));
        //registerADBlock(531, "palm_Leaves", (new BlockValarLeaves()).setSaplingBlock("palm_sapling").setBlockTextureName("palm_leaves"));
        registerADBlock(532, "palm_Log", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerADBlock(533, "palm_sapling", (new BlockValarSapling()).setPlantBounds(21, 31).setBlockTextureName("palm_sapling"));
        registerADBlock(534, "silk_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(535, "cracked_Silk_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(536, "straw", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(537, "cracked_Earth", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(538, "bag_End_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(539, "bag_End_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(540, "brown_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(541, "mordor_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(542, "cliff_Block", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(543, "spider_Egg", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(544, "bakruel", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(28, 25, 64, -1))));
        registerADBlock(545, "hopperfoot", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(12, 15, 16))));
        registerADBlock(546, "madarch", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(28, 18, 32, 4))));
        registerADBlock(547, "mehas", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(17, 21, 32, 1))));
        registerADBlock(548, "arfandas", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(10, 14, 16))));
        registerADBlock(549, "athelas", (new BlockCustomFlower(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 8.0 / 16, 1.0D))));
        registerADBlock(550, "bellis", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(46, 61, 64, -1))));
        registerADBlock(551, "lamp", (new BlockLamp(Material.GLASS, Arrays.asList(new AxisAlignedBB(3.0 / 16, 0.0D, 3.0 / 16, 13.0 / 16, 1.0D, 13.0 / 16)))));
        registerADBlock(552, "gondorian_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(553, "gondorian_Brick_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(554, "cracked_Gondorian_Brick_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(555, "lorien_Lamp", (new BlockLamp(Material.GLASS, Arrays.asList(new AxisAlignedBB(1.5 / 16, 0.0D, 1.5 / 16, 14.5 / 16, 1.0D, 14.5 / 16)))));
        registerADBlock(556, "bucklebury_Lamp", (new Block3D(Material.GLASS, Arrays.asList(new AxisAlignedBB(4.75 / 16, 0.0D, 4.75 / 16, 11.25 / 16, 1.0D, 11.25 / 16)))).setCreativeTab(CreativeTabs.DECORATIONS).setLightLevel(1.0F));
        registerADBlock(557, "salt_Ore", new BlockCustomOre(0, 2).setQuantityDropped(5));
        registerADBlock(558, "simbelmyne", (new BlockCustomFlower(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 6.0 / 16, 1.0D))));
        //registerADBlock(559, "bree_Glass", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setBlockTextureName("bree_glass"));
        registerADBlock(560, "shire_Flower", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(12, 10, 16))));
        registerADBlock(561, "dark_Metal", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(562, "rivendell_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(563, "moss", (new Block(Material.PLANTS)).setHardness(0.3F).setSoundType(SoundType.PLANT).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(564, "gondorian_Roof", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(565, "rivendell_Roof", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(566, "mossy_Gondorian_Brick_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(567, "dwarf_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(568, "window", (new BlockGlass(Material.GLASS, false)).setHardness(0.5F).setSoundType(SoundType.GLASS));
        registerADBlock(569, "rohan_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(570, "mordor_Brick_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(571, "mithril_Ore", new BlockCustomOre());
        //registerADBlock(572, "bree_Glass_Pane", (new BlockValarPane("bree_glass", "bree_glass", Material.glass, false)).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setBlockTextureName("bree_glass"));
        registerADBlock(573, "arlans_Slipper", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(28, 32))));
        registerADBlock(574, "numenorean", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(575, "dwarf_Inner_Wall_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(576, "dale_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(577, "dwarf_Inner_Wall_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(578, "red_Lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(579, "purple_Lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(580, "light_Blue_Lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(581, "green_Lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(582, "dwarf_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(583, "brown_Lapis", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(584, "dwarf_Floor_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(585, "dwarven_Hall_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(586, "dwarf_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(587, "dwarven_Torch", (new BlockDwarvenTorch()).setLightLevel(1.0F).setCreativeTab(CreativeTabs.DECORATIONS));
        registerADBlock(588, "dwarven_Gold", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(589, "rohan_Iron", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(590, "dark_Dwarf_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(591, "medium_Dark_Dwarf_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerADBlock(592, "mud_Block", (new BlockValarMud()).setBlockTextureName("mud_block"));
        registerADBlock(594, "light_Brown_Wood", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(595, "old_Tree", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(596, "bree_Bookshelf", (new Block3DDirectional(Material.WOOD)).setHardness(1.5F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(597, "cross_Hay", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(598, "light_Grey_Circle_Stone", (new Block3D(Material.GLASS)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(599, "hobbit_Lamp_1", (new BlockLamp(Material.GLASS, Arrays.asList(new AxisAlignedBB(4.0 / 16, 0.0D, 4.0 / 16, 12.0 / 16, 1.0D, 12.0 / 16)))));
        registerADBlock(600, "hobbit_Lamp_2", (new BlockLamp(Material.GLASS, Arrays.asList(new AxisAlignedBB(4.0 / 16, 0.0D, 4.0 / 16, 12.0 / 16, 1.0D, 12.0 / 16)))));
        registerADBlock(601, "lossarnach_Decoration_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(602, "bree_Lamp", (new Block3D(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.WOOD).setLightLevel(1.0F).setCreativeTab(CreativeTabs.DECORATIONS));
        registerADBlock(603, "archet_Lamp", new BlockArchetLamp());
        registerADBlock(606, "town_Marker", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE)); // Deprecated
        registerADBlock(607, "village_Marker", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE)); // Deprecated
        registerADBlock(608, "ruin_Marker", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE)); // Deprecated
        registerADBlock(609, "elven_Stone_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(610, "ancient_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(611, "bree_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(612, "cracked_Bree_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(613, "mossy_Bree_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(614, "shire_Hay", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(615, "bree_Oak_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(616, "bree_Spruce_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(617, "bree_Birch_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(618, "bree_Jungle_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(619, "shire_Path", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(620, "bree_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(621, "arnor_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(622, "cardolan_Brick_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(623, "elven_Sandstone_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(624, "dead_Lava", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(625, "chiseled_Gondorian_Stone", (new Block3D(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(626, "needles", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(627, "rhun_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(628, "khand_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerADBlock(629, "hobbit_Window", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(631, "city_Marker", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE)); // Deprecated
        registerADBlock(632, "rivendell_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(633, "column", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(634, "column_Top", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(635, "dwarf_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(636, "dwarf_Floor_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(637, "dwarf_Floor_3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(638, "dwarf_Floor_4", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(639, "dwarf_Floor_5", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(640, "dwarf_Floor_6", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(641, "dwarf_Floor_7", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(642, "dwarf_Floor_8", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(643, "dwarf_Floor_9", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(644, "dwarf_King_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(645, "dwarven_King_Floor_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(646, "dwarven_King_Floor_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(647, "dwarven_Pillar_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(648, "dwarven_Steel", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(649, "erebor_Floor_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(650, "erebor_Floor_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(651, "ered_Luin_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(652, "iron_Hills_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(653, "smooth_Gold", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(654, "rivendell_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(655, "sindar_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(656, "sindar_Floor_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(657, "sindar_Floor_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(658, "sindar_Decoration_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(659, "noldor_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(660, "noldor_Sandstone_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(661, "mallorn_Woodplanks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(662, "lindon_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(663, "lindon_Wall_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(664, "high_Elf_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(665, "harlindon_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(666, "forlond_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(667, "forlindon_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(668, "forlindon_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(669, "elven_Stone_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(670, "elven_Noble_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(671, "rivendell_Statue_Bottom", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(672, "elven_Decoration_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(673, "elven_Decoration_light", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(674, "elven_Decoration_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(675, "elven_Decoration_3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(676, "hobbit_Floor_1", (new Block3D(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(677, "hobbit_Floor_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(678, "dark_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(679, "rohirrim_Crossbeam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(680, "crossbeam_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(681, "crossbeam_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(682, "crossbeam_3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(683, "standard_Crossbeam_1", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(684, "standard_Crossbeam_2", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(685, "standard_Crossbeam_3", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(686, "vertical_Beam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(687, "vertical_Horizontal_Beam", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(689, "dale_Sandstone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(690, "dale_Tiles", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(691, "dale_Wall", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(692, "harad_Sandstone_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(693, "harad_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(694, "sandfloor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(695, "umbar_Haven_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(696, "umbar_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(697, "umbar_Wall_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(698, "khand_Stone_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(699, "brown_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(700, "edoras_Cobblestone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(701, "reinforced_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(702, "meduseld_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(703, "rohirrim_Wall_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(704, "statue_Head", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(705, "rohirrim_Oak_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(706, "rohirrim_Spruce_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(707, "rohirrim_Birch_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(708, "rohirrim_Jungle_Planks", (new Block3DDirectional(Material.WOOD)).setHardness(2.0F).setResistance(5.0F).setSoundType(SoundType.WOOD).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(709, "angmar_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(710, "angmar_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(711, "beorning_Wood", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(712, "annuminas_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(713, "arnor_Decoration_Jewel", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(714, "arnorian_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(715, "minas_Tirith_Floor", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(716, "pelargir_Stone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(717, "white_Cobblestone", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(718, "dunland_Totem", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(719, "dunland_Wall_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerADBlock(720, "mirkwood_Web", (new BlockValarWeb()).setLightOpacity(1).setHardness(4.0F).setBlockTextureName("mirkwood_web"));
        //registerADBlock(721, "dale_Window", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setBlockTextureName("dale_window"));
        //registerADBlock(722, "dale_Window_2", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(soundTypeGlass).setBlockTextureName("dale_window_2"));
        //registerADBlock(723, "harad_Window", (new BlockValarGlass()).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setBlockTextureName("harad_window"));
        registerADBlock(724, "mordor_Lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.GLASS).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(725, "harad_Light", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(726, "elven_Lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(727, "carn_Dum_Lamp", (new Block(Material.ROCK)).setLightLevel(1.0F).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(728, "elven_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(729, "human_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(730, "green_Bed", new BlockCustomBed());
        registerADBlock(731, "bree_Tile", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(736, "human_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(737, "mordor_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(738, "khandish_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(739, "sindar_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(740, "riverfolk_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(741, "fornost_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(742, "prison_Trapdoor", (new BlockTrapDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(743, "candle", new BlockCandle());
        registerADBlock(744, "human_Ladder", (new BlockLadder()).setHardness(0.4F).setSoundType(SoundType.LADDER));
        registerADBlock(745, "rohirrim_Ladder", (new BlockLadder()).setHardness(0.4F).setSoundType(SoundType.LADDER));
        registerADBlock(746, "dunland_Ladder", (new BlockLadder()).setHardness(0.4F).setSoundType(SoundType.LADDER));
        //registerADBlock(747, "sindar_Chest", (new BlockValarChest(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setBlockTextureName("sindar_chest"));
        registerADBlock(748, "castle_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(749, "dol_Guldur_Prison_Door", (new BlockCustomDoor(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).disableStats());
        registerADBlock(750, "hillmen_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(751, "mordor_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(752, "prison_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        registerADBlock(753, "sindar_Door", (new BlockCustomDoor(Material.WOOD)).setHardness(3.0F).setSoundType(SoundType.WOOD).disableStats());
        //registerADBlock(754, "yellow_Leaves", (new BlockValarLeaves().setSaplingBlock("mallorn_sapling").setBlockTextureName("yellow_leaves"))); // TODO Change Mallorn leaf color based on time of year
        //registerADBlock(755, "mallorn_Sapling", (new BlockValarSapling()).setPlantBounds(232, 251, 256).setBlockTextureName("mallorn_sapling"));
        //registerADBlock(756, "brown_Leaves", (new BlockValarLeaves().setBlockTextureName("brown_leaves")));
        //registerADBlock(757, "red_Leaves", (new BlockValarLeaves().setBlockTextureName("red_leaves")));
        //registerADBlock(758, "old_Tree_Sapling", (new BlockValarSapling()).setPlantBounds(56, 51, 64).setBlockTextureName("old_tree_sapling"));
        //registerADBlock(759, "light_Brown_Tree_Sapling", (new BlockValarSapling()).setPlantBounds(22, 32).setBlockTextureName("light_brown_tree_sapling"));
        //registerADBlock(760, "dead_Tree_Sapling", (new BlockValarSapling()).setPlantBounds(58, 45, 64).setBlockTextureName("dead_tree_sapling"));
        registerADBlock(761, "bush", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(31, 26, 32, -1))));
        registerADBlock(762, "alt_Dead_Bush", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(30, 25)))); // Deprecated
        registerADBlock(763, "cursed_Plant", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(32, 27))));
        registerADBlock(764, "haradwaith_Fern", (new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(28, 31))));
        registerADBlock(765, "lorilendel", (new BlockCustomFlower()));
        registerADBlock(766, "stakes", (new BlockStakes()).setCreativeTab(CreativeTabs.DECORATIONS));
        registerADBlock(767, "southlinch", new BlockCustomCrops(5));
        registerADBlock(768, "green_Grape", new BlockStakePlant(5));
        registerADBlock(769, "purple_Grape", new BlockStakePlant(5));
        registerADBlock(770, "pipeweed_Plant", new BlockCustomCrops(5));
        registerADBlock(771, "pea_Plant", new BlockCustomCrops(5));
        registerADBlock(772, "leek_Plant", new BlockCustomCrops(5));
        registerADBlock(773, "onion_Plant", new BlockCustomCrops(5));
        registerADBlock(774, "old_Toby_Plant", new BlockCustomCrops(3));
        registerADBlock(775, "southern_Star_Plant", new BlockCustomCrops(3));
        registerADBlock(776, "strawberry_Bush", new BlockCustomCrops(4));
        registerADBlock(777, "shire_Garden_Flower", new BlockCustomFlower(BlockCustomFlower.generateCrossBoundingBox(12, 10, 16)));
        registerADBlock(778, "pile_Of_Coins", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS)); // TODO custom step sound
        registerADBlock(779, "grey_Column", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(780, "grey_Column_Top", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(781, "column_Top_Decoration", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(782, "meduseld_Pillar", (new Block(Material.WOOD)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        registerADBlock(783, "diagonal_Bricks", (new Block(Material.ROCK)).setHardness(1.5F).setResistance(10.0F).setSoundType(SoundType.STONE).setCreativeTab(CreativeTabs.BUILDING_BLOCKS));
        //registerADBlock(784, "dark_Brown_Wood", (new BlockValarDirectional(Material.wood)).setHardness(2.0F).setSoundType(soundTypeWood).setBlockTextureName("dark_brown_wood"));
        registerADBlock(785, "bell", new BlockBell());
        registerADBlock(786, "custom_Crafting_Table", (new BlockCustomWorkbench()).setSoundType(SoundType.WOOD).setHardness(0.8F));
        registerADBlock(787, "water_Wheel", new BlockWaterWheel());
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
    
    // Begin Awaken Dreams code
    /**
     * @param mixedId Requires format of _ instead of spaces and camel caps. Ex: block_Name
     */
    private static void registerADBlock(int id, String mixedId, Block block_)
    {
    		registerADBlock(id, mixedId.toLowerCase(), mixedId.replace("_", ""), block_);
    }
    
    private static void registerADBlock(int id, String textualID, String unlocalizedName, Block block_)
    {
    		if(unlocalizedName != null)
    		{
    			block_.setUnlocalizedName(unlocalizedName);
    		}
    		registerBlock(id, new ADResourceLocation(textualID), block_);
    }
    // End Awaken Dreams code

    public static enum EnumOffsetType
    {
        NONE,
        XZ,
        XYZ;
    }
}
