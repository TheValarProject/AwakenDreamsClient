package net.minecraft.client.renderer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import com.elementfx.tvp.ad.block.BlockCustomWorkbench;
import com.elementfx.tvp.ad.item.ItemRing;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class RenderItem implements IResourceManagerReloadListener
{
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    /** False when the renderer is rendering the item's effects into a GUI */
    private boolean notRenderingEffectsInGUI = true;

    /** Defines the zLevel of rendering of item on GUI. */
    public float zLevel;
    private final ItemModelMesher itemModelMesher;
    private final TextureManager textureManager;
    private final ItemColors itemColors;

    public RenderItem(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_)
    {
        this.textureManager = p_i46552_1_;
        this.itemModelMesher = new ItemModelMesher(p_i46552_2_);
        this.registerItems();
        this.itemColors = p_i46552_3_;
    }

    /**
     * False when the renderer is rendering the item's effects into a GUI
     */
    public void isNotRenderingEffectsInGUI(boolean isNot)
    {
        this.notRenderingEffectsInGUI = isNot;
    }

    public ItemModelMesher getItemModelMesher()
    {
        return this.itemModelMesher;
    }

    protected void registerItem(Item itm, int subType, String identifier)
    {
        this.itemModelMesher.register(itm, subType, new ModelResourceLocation(identifier, "inventory"));
    }

    protected void registerBlock(Block blk, int subType, String identifier)
    {
        this.registerItem(Item.getItemFromBlock(blk), subType, identifier);
    }

    private void registerBlock(Block blk, String identifier)
    {
        this.registerBlock(blk, 0, identifier);
    }

    private void registerItem(Item itm, String identifier)
    {
        this.registerItem(itm, 0, identifier);
    }

    private void renderModel(IBakedModel model, ItemStack stack)
    {
        this.renderModel(model, -1, stack);
    }

    private void renderModel(IBakedModel model, int color)
    {
        this.renderModel(model, color, (ItemStack)null);
    }

    private void renderModel(IBakedModel model, int color, @Nullable ItemStack stack)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderQuads(vertexbuffer, model.getQuads((IBlockState)null, enumfacing, 0L), color, stack);
        }

        this.renderQuads(vertexbuffer, model.getQuads((IBlockState)null, (EnumFacing)null, 0L), color, stack);
        tessellator.draw();
    }

    public void renderItem(ItemStack stack, IBakedModel model)
    {
        if (stack != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);

            if (model.isBuiltInRenderer())
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            }
            else
            {
                this.renderModel(model, stack);

                if (stack.hasEffect())
                {
                    this.renderEffect(model);
                }
            }

            GlStateManager.popMatrix();
        }
    }

    private void renderEffect(IBakedModel model)
    {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        this.textureManager.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    private void putQuadNormal(VertexBuffer renderer, BakedQuad quad)
    {
        Vec3i vec3i = quad.getFace().getDirectionVec();
        renderer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    }

    private void renderQuad(VertexBuffer renderer, BakedQuad quad, int color)
    {
        renderer.addVertexData(quad.getVertexData());
        renderer.putColor4(color);
        this.putQuadNormal(renderer, quad);
    }

    private void renderQuads(VertexBuffer renderer, List<BakedQuad> quads, int color, @Nullable ItemStack stack)
    {
        boolean flag = color == -1 && stack != null;
        int i = 0;

        for (int j = quads.size(); i < j; ++i)
        {
            BakedQuad bakedquad = (BakedQuad)quads.get(i);
            int k = color;

            if (flag && bakedquad.hasTintIndex())
            {
                k = this.itemColors.getColorFromItemstack(stack, bakedquad.getTintIndex());

                if (EntityRenderer.anaglyphEnable)
                {
                    k = TextureUtil.anaglyphColor(k);
                }

                k = k | -16777216;
            }

            this.renderQuad(renderer, bakedquad, k);
        }
    }

    public boolean shouldRenderItemIn3D(ItemStack stack)
    {
        IBakedModel ibakedmodel = this.itemModelMesher.getItemModel(stack);
        return ibakedmodel == null ? false : ibakedmodel.isGui3d();
    }

    public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType)
    {
        if (stack != null)
        {
            IBakedModel ibakedmodel = this.getItemModelWithOverrides(stack, (World)null, (EntityLivingBase)null);
            this.renderItemModel(stack, ibakedmodel, cameraTransformType, false);
        }
    }

    public IBakedModel getItemModelWithOverrides(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entitylivingbaseIn)
    {
        IBakedModel ibakedmodel = this.itemModelMesher.getItemModel(stack);
        Item item = stack.getItem();

        if (item != null && item.hasCustomProperties())
        {
            ResourceLocation resourcelocation = ibakedmodel.getOverrides().applyOverride(stack, worldIn, entitylivingbaseIn);
            return resourcelocation == null ? ibakedmodel : this.itemModelMesher.getModelManager().getModel(new ModelResourceLocation(resourcelocation, "inventory"));
        }
        else
        {
            return ibakedmodel;
        }
    }

    public void renderItem(ItemStack stack, EntityLivingBase entitylivingbaseIn, ItemCameraTransforms.TransformType transform, boolean leftHanded)
    {
        if (stack != null && entitylivingbaseIn != null && stack.getItem() != null)
        {
            IBakedModel ibakedmodel = this.getItemModelWithOverrides(stack, entitylivingbaseIn.worldObj, entitylivingbaseIn);
            this.renderItemModel(stack, ibakedmodel, transform, leftHanded);
        }
    }

    protected void renderItemModel(ItemStack stack, IBakedModel bakedmodel, ItemCameraTransforms.TransformType transform, boolean leftHanded)
    {
        if (stack.getItem() != null)
        {
            this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            ItemCameraTransforms itemcameratransforms = bakedmodel.getItemCameraTransforms();
            ItemCameraTransforms.applyTransformSide(itemcameratransforms.getTransform(transform), leftHanded);

            if (this.isThereOneNegativeScale(itemcameratransforms.getTransform(transform)))
            {
                GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            }

            this.renderItem(stack, bakedmodel);
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
    }

    /**
     * Return true if only one scale is negative
     */
    private boolean isThereOneNegativeScale(ItemTransformVec3f itemTranformVec)
    {
        return itemTranformVec.scale.x < 0.0F ^ itemTranformVec.scale.y < 0.0F ^ itemTranformVec.scale.z < 0.0F;
    }

    public void renderItemIntoGUI(ItemStack stack, int x, int y)
    {
        this.renderItemModelIntoGUI(stack, x, y, this.getItemModelWithOverrides(stack, (World)null, (EntityLivingBase)null));
    }

    protected void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel)
    {
        GlStateManager.pushMatrix();
        this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        this.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.setupGuiTransform(x, y, bakedmodel.isGui3d());
        bakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        this.renderItem(stack, bakedmodel);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        this.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
    }

    private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d)
    {
        GlStateManager.translate((float)xPosition, (float)yPosition, 100.0F + this.zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);

        if (isGui3d)
        {
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.disableLighting();
        }
    }

    public void renderItemAndEffectIntoGUI(ItemStack stack, int xPosition, int yPosition)
    {
        this.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().thePlayer, stack, xPosition, yPosition);
    }

    public void renderItemAndEffectIntoGUI(@Nullable EntityLivingBase p_184391_1_, final ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_)
    {
        if (p_184391_2_ != null && p_184391_2_.getItem() != null)
        {
            this.zLevel += 50.0F;

            try
            {
                this.renderItemModelIntoGUI(p_184391_2_, p_184391_3_, p_184391_4_, this.getItemModelWithOverrides(p_184391_2_, (World)null, p_184391_1_));
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                crashreportcategory.func_189529_a("Item Type", new ICrashReportDetail<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf((Object)p_184391_2_.getItem());
                    }
                });
                crashreportcategory.func_189529_a("Item Aux", new ICrashReportDetail<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf(p_184391_2_.getMetadata());
                    }
                });
                crashreportcategory.func_189529_a("Item NBT", new ICrashReportDetail<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf((Object)p_184391_2_.getTagCompound());
                    }
                });
                crashreportcategory.func_189529_a("Item Foil", new ICrashReportDetail<String>()
                {
                    public String call() throws Exception
                    {
                        return String.valueOf(p_184391_2_.hasEffect());
                    }
                });
                throw new ReportedException(crashreport);
            }

            this.zLevel -= 50.0F;
        }
    }

    public void renderItemOverlays(FontRenderer fr, ItemStack stack, int xPosition, int yPosition)
    {
        this.renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, (String)null);
    }

    /**
     * Renders the stack size and/or damage bar for the given ItemStack.
     */
    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text)
    {
        if (stack != null)
        {
            if (stack.stackSize != 1 || text != null)
            {
                String s = text == null ? String.valueOf(stack.stackSize) : text;

                if (text == null && stack.stackSize < 1)
                {
                    s = TextFormatting.RED + String.valueOf(stack.stackSize);
                }

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            if (stack.isItemDamaged())
            {
                int j = (int)Math.round(13.0D - (double)stack.getItemDamage() * 13.0D / (double)stack.getMaxDamage());
                int i = (int)Math.round(255.0D - (double)stack.getItemDamage() * 255.0D / (double)stack.getMaxDamage());
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer vertexbuffer = tessellator.getBuffer();
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;
            float f = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

            if (f > 0.0F)
            {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                Tessellator tessellator1 = Tessellator.getInstance();
                VertexBuffer vertexbuffer1 = tessellator1.getBuffer();
                this.draw(vertexbuffer1, xPosition, yPosition + MathHelper.floor_float(16.0F * (1.0F - f)), 16, MathHelper.ceiling_float_int(16.0F * f), 255, 255, 255, 127);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    /**
     * Draw with the WorldRenderer
     */
    private void draw(VertexBuffer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    private void registerItems()
    {
        this.registerBlock(Blocks.ANVIL, "anvil_intact");
        this.registerBlock(Blocks.ANVIL, 1, "anvil_slightly_damaged");
        this.registerBlock(Blocks.ANVIL, 2, "anvil_very_damaged");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.BLACK.getMetadata(), "black_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.BLUE.getMetadata(), "blue_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.BROWN.getMetadata(), "brown_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.CYAN.getMetadata(), "cyan_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.GRAY.getMetadata(), "gray_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.GREEN.getMetadata(), "green_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.LIME.getMetadata(), "lime_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.MAGENTA.getMetadata(), "magenta_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.ORANGE.getMetadata(), "orange_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.PINK.getMetadata(), "pink_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.PURPLE.getMetadata(), "purple_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.RED.getMetadata(), "red_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.SILVER.getMetadata(), "silver_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.WHITE.getMetadata(), "white_carpet");
        this.registerBlock(Blocks.CARPET, EnumDyeColor.YELLOW.getMetadata(), "yellow_carpet");
        this.registerBlock(Blocks.COBBLESTONE_WALL, BlockWall.EnumType.MOSSY.getMetadata(), "mossy_cobblestone_wall");
        this.registerBlock(Blocks.COBBLESTONE_WALL, BlockWall.EnumType.NORMAL.getMetadata(), "cobblestone_wall");
        this.registerBlock(Blocks.DIRT, BlockDirt.DirtType.COARSE_DIRT.getMetadata(), "coarse_dirt");
        this.registerBlock(Blocks.DIRT, BlockDirt.DirtType.DIRT.getMetadata(), "dirt");
        this.registerBlock(Blocks.DIRT, BlockDirt.DirtType.PODZOL.getMetadata(), "podzol");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.FERN.getMeta(), "double_fern");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.GRASS.getMeta(), "double_grass");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta(), "paeonia");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.ROSE.getMeta(), "double_rose");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta(), "sunflower");
        this.registerBlock(Blocks.DOUBLE_PLANT, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta(), "syringa");
        this.registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_leaves");
        this.registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_leaves");
        this.registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.OAK.getMetadata(), "oak_leaves");
        this.registerBlock(Blocks.LEAVES, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_leaves");
        this.registerBlock(Blocks.LEAVES2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_leaves");
        this.registerBlock(Blocks.LEAVES2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_leaves");
        this.registerBlock(Blocks.LOG, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_log");
        this.registerBlock(Blocks.LOG, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_log");
        this.registerBlock(Blocks.LOG, BlockPlanks.EnumType.OAK.getMetadata(), "oak_log");
        this.registerBlock(Blocks.LOG, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_log");
        this.registerBlock(Blocks.LOG2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_log");
        this.registerBlock(Blocks.LOG2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_log");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.CHISELED_STONEBRICK.getMetadata(), "chiseled_brick_monster_egg");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.COBBLESTONE.getMetadata(), "cobblestone_monster_egg");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.CRACKED_STONEBRICK.getMetadata(), "cracked_brick_monster_egg");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.MOSSY_STONEBRICK.getMetadata(), "mossy_brick_monster_egg");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.STONE.getMetadata(), "stone_monster_egg");
        this.registerBlock(Blocks.MONSTER_EGG, BlockSilverfish.EnumType.STONEBRICK.getMetadata(), "stone_brick_monster_egg");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_planks");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_planks");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_planks");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_planks");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.OAK.getMetadata(), "oak_planks");
        this.registerBlock(Blocks.PLANKS, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_planks");
        this.registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.BRICKS.getMetadata(), "prismarine_bricks");
        this.registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.DARK.getMetadata(), "dark_prismarine");
        this.registerBlock(Blocks.PRISMARINE, BlockPrismarine.EnumType.ROUGH.getMetadata(), "prismarine");
        this.registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.CHISELED.getMetadata(), "chiseled_quartz_block");
        this.registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.DEFAULT.getMetadata(), "quartz_block");
        this.registerBlock(Blocks.QUARTZ_BLOCK, BlockQuartz.EnumType.LINES_Y.getMetadata(), "quartz_column");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.ALLIUM.getMeta(), "allium");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta(), "blue_orchid");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta(), "houstonia");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta(), "orange_tulip");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta(), "oxeye_daisy");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta(), "pink_tulip");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.POPPY.getMeta(), "poppy");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.RED_TULIP.getMeta(), "red_tulip");
        this.registerBlock(Blocks.RED_FLOWER, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta(), "white_tulip");
        this.registerBlock(Blocks.SAND, BlockSand.EnumType.RED_SAND.getMetadata(), "red_sand");
        this.registerBlock(Blocks.SAND, BlockSand.EnumType.SAND.getMetadata(), "sand");
        this.registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.CHISELED.getMetadata(), "chiseled_sandstone");
        this.registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.DEFAULT.getMetadata(), "sandstone");
        this.registerBlock(Blocks.SANDSTONE, BlockSandStone.EnumType.SMOOTH.getMetadata(), "smooth_sandstone");
        this.registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.CHISELED.getMetadata(), "chiseled_red_sandstone");
        this.registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.DEFAULT.getMetadata(), "red_sandstone");
        this.registerBlock(Blocks.RED_SANDSTONE, BlockRedSandstone.EnumType.SMOOTH.getMetadata(), "smooth_red_sandstone");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_sapling");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_sapling");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_sapling");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_sapling");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.OAK.getMetadata(), "oak_sapling");
        this.registerBlock(Blocks.SAPLING, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_sapling");
        this.registerBlock(Blocks.SPONGE, 0, "sponge");
        this.registerBlock(Blocks.SPONGE, 1, "sponge_wet");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.RED.getMetadata(), "red_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.RED.getMetadata(), "red_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_GLASS_PANE, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass_pane");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BLACK.getMetadata(), "black_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BLUE.getMetadata(), "blue_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.BROWN.getMetadata(), "brown_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.GRAY.getMetadata(), "gray_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.GREEN.getMetadata(), "green_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.LIME.getMetadata(), "lime_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.PINK.getMetadata(), "pink_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.RED.getMetadata(), "red_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.SILVER.getMetadata(), "silver_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.WHITE.getMetadata(), "white_stained_hardened_clay");
        this.registerBlock(Blocks.STAINED_HARDENED_CLAY, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_hardened_clay");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.ANDESITE.getMetadata(), "andesite");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata(), "andesite_smooth");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.DIORITE.getMetadata(), "diorite");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata(), "diorite_smooth");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.GRANITE.getMetadata(), "granite");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata(), "granite_smooth");
        this.registerBlock(Blocks.STONE, BlockStone.EnumType.STONE.getMetadata(), "stone");
        this.registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.CRACKED.getMetadata(), "cracked_stonebrick");
        this.registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.DEFAULT.getMetadata(), "stonebrick");
        this.registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.CHISELED.getMetadata(), "chiseled_stonebrick");
        this.registerBlock(Blocks.STONEBRICK, BlockStoneBrick.EnumType.MOSSY.getMetadata(), "mossy_stonebrick");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.BRICK.getMetadata(), "brick_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata(), "cobblestone_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.WOOD.getMetadata(), "old_wood_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.NETHERBRICK.getMetadata(), "nether_brick_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.QUARTZ.getMetadata(), "quartz_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.SAND.getMetadata(), "sandstone_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata(), "stone_brick_slab");
        this.registerBlock(Blocks.STONE_SLAB, BlockStoneSlab.EnumType.STONE.getMetadata(), "stone_slab");
        this.registerBlock(Blocks.STONE_SLAB2, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata(), "red_sandstone_slab");
        this.registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.DEAD_BUSH.getMeta(), "dead_bush");
        this.registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.FERN.getMeta(), "fern");
        this.registerBlock(Blocks.TALLGRASS, BlockTallGrass.EnumType.GRASS.getMeta(), "tall_grass");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_slab");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_slab");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_slab");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_slab");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.OAK.getMetadata(), "oak_slab");
        this.registerBlock(Blocks.WOODEN_SLAB, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_slab");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.BLACK.getMetadata(), "black_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.BLUE.getMetadata(), "blue_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.BROWN.getMetadata(), "brown_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.CYAN.getMetadata(), "cyan_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.GRAY.getMetadata(), "gray_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.GREEN.getMetadata(), "green_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.LIME.getMetadata(), "lime_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.MAGENTA.getMetadata(), "magenta_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.ORANGE.getMetadata(), "orange_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.PINK.getMetadata(), "pink_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.PURPLE.getMetadata(), "purple_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.RED.getMetadata(), "red_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.SILVER.getMetadata(), "silver_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.WHITE.getMetadata(), "white_wool");
        this.registerBlock(Blocks.WOOL, EnumDyeColor.YELLOW.getMetadata(), "yellow_wool");
        this.registerBlock(Blocks.FARMLAND, "farmland");
        this.registerBlock(Blocks.ACACIA_STAIRS, "acacia_stairs");
        this.registerBlock(Blocks.ACTIVATOR_RAIL, "activator_rail");
        this.registerBlock(Blocks.BEACON, "beacon");
        this.registerBlock(Blocks.BEDROCK, "bedrock");
        this.registerBlock(Blocks.BIRCH_STAIRS, "birch_stairs");
        this.registerBlock(Blocks.BOOKSHELF, "bookshelf");
        this.registerBlock(Blocks.BRICK_BLOCK, "brick_block");
        this.registerBlock(Blocks.BRICK_BLOCK, "brick_block");
        this.registerBlock(Blocks.BRICK_STAIRS, "brick_stairs");
        this.registerBlock(Blocks.BROWN_MUSHROOM, "brown_mushroom");
        this.registerBlock(Blocks.CACTUS, "cactus");
        this.registerBlock(Blocks.CLAY, "clay");
        this.registerBlock(Blocks.COAL_BLOCK, "coal_block");
        this.registerBlock(Blocks.COAL_ORE, "coal_ore");
        this.registerBlock(Blocks.COBBLESTONE, "cobblestone");
        this.registerBlock(Blocks.CRAFTING_TABLE, "crafting_table");
        this.registerBlock(Blocks.DARK_OAK_STAIRS, "dark_oak_stairs");
        this.registerBlock(Blocks.DAYLIGHT_DETECTOR, "daylight_detector");
        this.registerBlock(Blocks.DEADBUSH, "dead_bush");
        this.registerBlock(Blocks.DETECTOR_RAIL, "detector_rail");
        this.registerBlock(Blocks.DIAMOND_BLOCK, "diamond_block");
        this.registerBlock(Blocks.DIAMOND_ORE, "diamond_ore");
        this.registerBlock(Blocks.DISPENSER, "dispenser");
        this.registerBlock(Blocks.DROPPER, "dropper");
        this.registerBlock(Blocks.EMERALD_BLOCK, "emerald_block");
        this.registerBlock(Blocks.EMERALD_ORE, "emerald_ore");
        this.registerBlock(Blocks.ENCHANTING_TABLE, "enchanting_table");
        this.registerBlock(Blocks.END_PORTAL_FRAME, "end_portal_frame");
        this.registerBlock(Blocks.END_STONE, "end_stone");
        this.registerBlock(Blocks.OAK_FENCE, "oak_fence");
        this.registerBlock(Blocks.SPRUCE_FENCE, "spruce_fence");
        this.registerBlock(Blocks.BIRCH_FENCE, "birch_fence");
        this.registerBlock(Blocks.JUNGLE_FENCE, "jungle_fence");
        this.registerBlock(Blocks.DARK_OAK_FENCE, "dark_oak_fence");
        this.registerBlock(Blocks.ACACIA_FENCE, "acacia_fence");
        this.registerBlock(Blocks.OAK_FENCE_GATE, "oak_fence_gate");
        this.registerBlock(Blocks.SPRUCE_FENCE_GATE, "spruce_fence_gate");
        this.registerBlock(Blocks.BIRCH_FENCE_GATE, "birch_fence_gate");
        this.registerBlock(Blocks.JUNGLE_FENCE_GATE, "jungle_fence_gate");
        this.registerBlock(Blocks.DARK_OAK_FENCE_GATE, "dark_oak_fence_gate");
        this.registerBlock(Blocks.ACACIA_FENCE_GATE, "acacia_fence_gate");
        this.registerBlock(Blocks.FURNACE, "furnace");
        this.registerBlock(Blocks.GLASS, "glass");
        this.registerBlock(Blocks.GLASS_PANE, "glass_pane");
        this.registerBlock(Blocks.GLOWSTONE, "glowstone");
        this.registerBlock(Blocks.GOLDEN_RAIL, "golden_rail");
        this.registerBlock(Blocks.GOLD_BLOCK, "gold_block");
        this.registerBlock(Blocks.GOLD_ORE, "gold_ore");
        this.registerBlock(Blocks.GRASS, "grass");
        this.registerBlock(Blocks.GRASS_PATH, "grass_path");
        this.registerBlock(Blocks.GRAVEL, "gravel");
        this.registerBlock(Blocks.HARDENED_CLAY, "hardened_clay");
        this.registerBlock(Blocks.HAY_BLOCK, "hay_block");
        this.registerBlock(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, "heavy_weighted_pressure_plate");
        this.registerBlock(Blocks.HOPPER, "hopper");
        this.registerBlock(Blocks.ICE, "ice");
        this.registerBlock(Blocks.IRON_BARS, "iron_bars");
        this.registerBlock(Blocks.IRON_BLOCK, "iron_block");
        this.registerBlock(Blocks.IRON_ORE, "iron_ore");
        this.registerBlock(Blocks.IRON_TRAPDOOR, "iron_trapdoor");
        this.registerBlock(Blocks.JUKEBOX, "jukebox");
        this.registerBlock(Blocks.JUNGLE_STAIRS, "jungle_stairs");
        this.registerBlock(Blocks.LADDER, "ladder");
        this.registerBlock(Blocks.LAPIS_BLOCK, "lapis_block");
        this.registerBlock(Blocks.LAPIS_ORE, "lapis_ore");
        this.registerBlock(Blocks.LEVER, "lever");
        this.registerBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, "light_weighted_pressure_plate");
        this.registerBlock(Blocks.LIT_PUMPKIN, "lit_pumpkin");
        this.registerBlock(Blocks.MELON_BLOCK, "melon_block");
        this.registerBlock(Blocks.MOSSY_COBBLESTONE, "mossy_cobblestone");
        this.registerBlock(Blocks.MYCELIUM, "mycelium");
        this.registerBlock(Blocks.NETHERRACK, "netherrack");
        this.registerBlock(Blocks.NETHER_BRICK, "nether_brick");
        this.registerBlock(Blocks.NETHER_BRICK_FENCE, "nether_brick_fence");
        this.registerBlock(Blocks.NETHER_BRICK_STAIRS, "nether_brick_stairs");
        this.registerBlock(Blocks.NOTEBLOCK, "noteblock");
        this.registerBlock(Blocks.OAK_STAIRS, "oak_stairs");
        this.registerBlock(Blocks.OBSIDIAN, "obsidian");
        this.registerBlock(Blocks.PACKED_ICE, "packed_ice");
        this.registerBlock(Blocks.PISTON, "piston");
        this.registerBlock(Blocks.PUMPKIN, "pumpkin");
        this.registerBlock(Blocks.QUARTZ_ORE, "quartz_ore");
        this.registerBlock(Blocks.QUARTZ_STAIRS, "quartz_stairs");
        this.registerBlock(Blocks.RAIL, "rail");
        this.registerBlock(Blocks.REDSTONE_BLOCK, "redstone_block");
        this.registerBlock(Blocks.REDSTONE_LAMP, "redstone_lamp");
        this.registerBlock(Blocks.REDSTONE_ORE, "redstone_ore");
        this.registerBlock(Blocks.REDSTONE_TORCH, "redstone_torch");
        this.registerBlock(Blocks.RED_MUSHROOM, "red_mushroom");
        this.registerBlock(Blocks.SANDSTONE_STAIRS, "sandstone_stairs");
        this.registerBlock(Blocks.RED_SANDSTONE_STAIRS, "red_sandstone_stairs");
        this.registerBlock(Blocks.SEA_LANTERN, "sea_lantern");
        this.registerBlock(Blocks.SLIME_BLOCK, "slime");
        this.registerBlock(Blocks.SNOW, "snow");
        this.registerBlock(Blocks.SNOW_LAYER, "snow_layer");
        this.registerBlock(Blocks.SOUL_SAND, "soul_sand");
        this.registerBlock(Blocks.SPRUCE_STAIRS, "spruce_stairs");
        this.registerBlock(Blocks.STICKY_PISTON, "sticky_piston");
        this.registerBlock(Blocks.STONE_BRICK_STAIRS, "stone_brick_stairs");
        this.registerBlock(Blocks.STONE_BUTTON, "stone_button");
        this.registerBlock(Blocks.STONE_PRESSURE_PLATE, "stone_pressure_plate");
        this.registerBlock(Blocks.STONE_STAIRS, "stone_stairs");
        this.registerBlock(Blocks.TNT, "tnt");
        this.registerBlock(Blocks.TORCH, "torch");
        this.registerBlock(Blocks.TRAPDOOR, "trapdoor");
        this.registerBlock(Blocks.TRIPWIRE_HOOK, "tripwire_hook");
        this.registerBlock(Blocks.VINE, "vine");
        this.registerBlock(Blocks.WATERLILY, "waterlily");
        this.registerBlock(Blocks.WEB, "web");
        this.registerBlock(Blocks.WOODEN_BUTTON, "wooden_button");
        this.registerBlock(Blocks.WOODEN_PRESSURE_PLATE, "wooden_pressure_plate");
        this.registerBlock(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION.getMeta(), "dandelion");
        this.registerBlock(Blocks.END_ROD, "end_rod");
        this.registerBlock(Blocks.CHORUS_PLANT, "chorus_plant");
        this.registerBlock(Blocks.CHORUS_FLOWER, "chorus_flower");
        this.registerBlock(Blocks.PURPUR_BLOCK, "purpur_block");
        this.registerBlock(Blocks.PURPUR_PILLAR, "purpur_pillar");
        this.registerBlock(Blocks.PURPUR_STAIRS, "purpur_stairs");
        this.registerBlock(Blocks.PURPUR_SLAB, "purpur_slab");
        this.registerBlock(Blocks.PURPUR_DOUBLE_SLAB, "purpur_double_slab");
        this.registerBlock(Blocks.END_BRICKS, "end_bricks");
        this.registerBlock(Blocks.field_189877_df, "magma");
        this.registerBlock(Blocks.field_189878_dg, "nether_wart_block");
        this.registerBlock(Blocks.field_189879_dh, "red_nether_brick");
        this.registerBlock(Blocks.field_189880_di, "bone_block");
        this.registerBlock(Blocks.field_189881_dj, "structure_void");
        this.registerBlock(Blocks.CHEST, "chest");
        this.registerBlock(Blocks.TRAPPED_CHEST, "trapped_chest");
        this.registerBlock(Blocks.ENDER_CHEST, "ender_chest");
        // Begin Awaken Dreams code
        this.registerBlock(Blocks.JADE_ORE, "jade_ore");
        this.registerBlock(Blocks.AMBER_ORE, "amber_ore");
        this.registerBlock(Blocks.TANZANITE_ORE, "tanzanite_ore");
        this.registerBlock(Blocks.PERMANENT_DIRT, "permanent_dirt");
        this.registerBlock(Blocks.LUINDOL, "luindol");
        this.registerBlock(Blocks.HOPPER_MUSHROOM, "hopper_mushroom");
        this.registerBlock(Blocks.AMETHYST_ORE, "amethyst_ore");
        this.registerBlock(Blocks.RUBY_ORE, "ruby_ore");
        this.registerBlock(Blocks.ONYX_ORE, "onyx_ore");
        this.registerBlock(Blocks.MOONSTONE_ORE, "moonstone_ore");
        this.registerBlock(Blocks.MINAS_MORGUL_GLOWSTONE, "minas_morgul_glowstone");
        this.registerBlock(Blocks.ARGONATH_STONE, "argonath_stone");
        this.registerBlock(Blocks.MITHRIL_BLOCK, "mithril_block");
        this.registerBlock(Blocks.CRYSTAL_ORE, "crystal_ore");
        this.registerBlock(Blocks.DESERT_ROAD_BLOCK, "desert_road_block");
        this.registerBlock(Blocks.DIRT_ROAD_BLOCK, "dirt_road_block");
        this.registerBlock(Blocks.LIGHT_BLUE_GLOWSTONE, "light_blue_glowstone");
        this.registerBlock(Blocks.BLACK_IRON, "black_iron");
        this.registerBlock(Blocks.MORIA_PILLAR_STONE, "moria_pillar_stone");
        this.registerBlock(Blocks.RUSTY_IRON, "rusty_iron");
        this.registerBlock(Blocks.ELF_FLOOR, "elf_floor");
        this.registerBlock(Blocks.GONDORIAN_FLOOR, "gondorian_floor");
        this.registerBlock(Blocks.STONE_FLOOR, "stone_floor");
        this.registerBlock(Blocks.MORIA_TRAPDOOR, "moria_trapdoor");
        this.registerBlock(Blocks.PALM_LOG, "palm_log");
        this.registerBlock(Blocks.SILK_STONE, "silk_stone");
        this.registerBlock(Blocks.CRACKED_SILK_STONE, "cracked_silk_stone");
        this.registerBlock(Blocks.STRAW, "straw");
        this.registerBlock(Blocks.CRACKED_EARTH, "cracked_earth");
        this.registerBlock(Blocks.BAG_END_FLOOR, "bag_end_floor");
        this.registerBlock(Blocks.BAG_END_WALL, "bag_end_wall");
        this.registerBlock(Blocks.BROWN_STONE, "brown_stone");
        this.registerBlock(Blocks.MORDOR_STONE, "mordor_stone");
        this.registerBlock(Blocks.CLIFF_BLOCK, "cliff_block");
        this.registerBlock(Blocks.SPIDER_EGG, "spider_egg");
        this.registerBlock(Blocks.BAKRUEL, "bakruel");
        this.registerBlock(Blocks.HOPPERFOOT, "hopperfoot");
        this.registerBlock(Blocks.MADARCH, "madarch");
        this.registerBlock(Blocks.MEHAS, "mehas");
        this.registerBlock(Blocks.ARFANDAS,  "arfandas");
        this.registerBlock(Blocks.ATHELAS, "athelas");
        this.registerBlock(Blocks.BELLIS, "bellis");
        this.registerBlock(Blocks.LAMP, "lamp");
        this.registerBlock(Blocks.GONDORIAN_STONE, "gondorian_stone");
        this.registerBlock(Blocks.GONDORIAN_BRICK_STONE, "gondorian_brick_stone");
        this.registerBlock(Blocks.CRACKED_GONDORIAN_BRICK_STONE, "cracked_gondorian_brick_stone");
        this.registerBlock(Blocks.LORIEN_LAMP, "lorien_lamp");
        this.registerBlock(Blocks.BUCKLEBURY_LAMP, "bucklebury_lamp");
        this.registerBlock(Blocks.SALT_ORE, "salt_ore");
        this.registerBlock(Blocks.SIMBELMYNE, "simbelmyne");
        this.registerBlock(Blocks.SHIRE_FLOWER, "shire_flower");
        this.registerBlock(Blocks.DARK_METAL, "dark_metal");
        this.registerBlock(Blocks.RIVENDELL_WOOD, "rivendell_wood");
        this.registerBlock(Blocks.MOSS, "moss");
        this.registerBlock(Blocks.GONDORIAN_ROOF, "gondorian_roof");
        this.registerBlock(Blocks.RIVENDELL_ROOF, "rivendell_roof");
        this.registerBlock(Blocks.MOSSY_GONDORIAN_BRICK_STONE, "mossy_gondorian_brick_stone");
        this.registerBlock(Blocks.WINDOW, "window");
        this.registerBlock(Blocks.ROHAN_BRICKS, "rohan_bricks");
        this.registerBlock(Blocks.MORDOR_BRICK_STONE, "mordor_brick_stone");
        this.registerBlock(Blocks.MITHRIL_ORE, "mithril_ore");
        this.registerBlock(Blocks.ARLANS_SLIPPER, "arlans_slipper");
        this.registerBlock(Blocks.NUMENOREAN, "numenorean");
        this.registerBlock(Blocks.DWARF_INNER_WALL_DECORATION, "dwarf_inner_wall_decoration");
        this.registerBlock(Blocks.DALE_STONE, "dale_stone");
        this.registerBlock(Blocks.DWARF_INNER_WALL_STONE, "dwarf_inner_wall_stone");
        this.registerBlock(Blocks.RED_LAPIS, "red_lapis");
        this.registerBlock(Blocks.PURPLE_LAPIS, "purple_lapis");
        this.registerBlock(Blocks.LIGHT_BLUE_LAPIS, "light_blue_lapis");
        this.registerBlock(Blocks.GREEN_LAPIS, "green_lapis");
        this.registerBlock(Blocks.DWARF_WALL, "dwarf_wall");
        this.registerBlock(Blocks.BROWN_LAPIS, "brown_lapis");
        this.registerBlock(Blocks.DWARF_FLOOR_1, "dwarf_floor_1");
        this.registerBlock(Blocks.DWARVEN_HALL_FLOOR, "dwarven_hall_floor");
        this.registerBlock(Blocks.DWARF_STONE, "dwarf_stone");
        this.registerBlock(Blocks.DWARVEN_TORCH, "dwarven_torch");
        this.registerBlock(Blocks.DWARVEN_GOLD, "dwarven_gold");
        this.registerBlock(Blocks.ROHAN_IRON, "rohan_iron");
        this.registerBlock(Blocks.DARK_DWARF_STONE, "dark_dwarf_stone");
        this.registerBlock(Blocks.MEDIUM_DARK_DWARF_STONE, "medium_dark_dwarf_stone");
        this.registerBlock(Blocks.LIGHT_BROWN_WOOD, "light_brown_wood");
        this.registerBlock(Blocks.OLD_TREE, "old_tree");
        this.registerBlock(Blocks.BREE_BOOKSHELF, "bree_bookshelf");
        this.registerBlock(Blocks.CROSS_HAY, "cross_hay");
        this.registerBlock(Blocks.LIGHT_GREY_CIRCLE_STONE, "light_grey_circle_stone");
        this.registerBlock(Blocks.HOBBIT_LAMP_1, "hobbit_lamp_1");
        this.registerBlock(Blocks.HOBBIT_LAMP_2, "hobbit_lamp_2");
        this.registerBlock(Blocks.LOSSARNACH_DECORATION_STONE, "lossarnach_decoration_stone");
        this.registerBlock(Blocks.BREE_LAMP, "bree_lamp");
        this.registerBlock(Blocks.ARCHET_LAMP, "archet_lamp");
        this.registerBlock(Blocks.TOWN_MARKER, "town_marker");
        this.registerBlock(Blocks.VILLAGE_MARKER, "village_marker");
        this.registerBlock(Blocks.RUIN_MARKER, "ruin_marker");
        this.registerBlock(Blocks.ELVEN_STONE_FLOOR, "elven_stone_floor");
        this.registerBlock(Blocks.ANCIENT_STONE, "ancient_stone");
        this.registerBlock(Blocks.BREE_STONE_BRICKS, "bree_stone_bricks");
        this.registerBlock(Blocks.CRACKED_BREE_STONE_BRICKS, "cracked_bree_stone_bricks");
        this.registerBlock(Blocks.MOSSY_BREE_STONE_BRICKS, "mossy_bree_stone_bricks");
        this.registerBlock(Blocks.SHIRE_HAY, "shire_hay");
        this.registerBlock(Blocks.BREE_OAK_PLANKS, "bree_oak_planks");
        this.registerBlock(Blocks.BREE_SPRUCE_PLANKS, "bree_spruce_planks");
        this.registerBlock(Blocks.BREE_BIRCH_PLANKS, "bree_birch_planks");
        this.registerBlock(Blocks.BREE_JUNGLE_PLANKS, "bree_jungle_planks");
        this.registerBlock(Blocks.SHIRE_PATH, "shire_path");
        this.registerBlock(Blocks.BREE_FLOOR, "bree_floor");
        this.registerBlock(Blocks.ARNOR_FLOOR, "arnor_floor");
        this.registerBlock(Blocks.CARDOLAN_BRICK_STONE, "cardolan_brick_stone");
        this.registerBlock(Blocks.ELVEN_SANDSTONE_FLOOR, "elven_sandstone_floor");
        this.registerBlock(Blocks.DEAD_LAVA, "dead_lava");
        this.registerBlock(Blocks.CHISELED_GONDORIAN_STONE, "chiseled_gondorian_stone");
        this.registerBlock(Blocks.NEEDLES, "needles");
        this.registerBlock(Blocks.RHUN_FLOOR, "rhun_floor");
        this.registerBlock(Blocks.KHAND_FLOOR, "khand_floor");
        this.registerBlock(Blocks.CITY_MARKER, "city_marker");
        this.registerBlock(Blocks.RIVENDELL_FLOOR, "rivendell_floor");
        this.registerBlock(Blocks.COLUMN, "column");
        this.registerBlock(Blocks.COLUMN_TOP, "column_top");
        this.registerBlock(Blocks.DWARF_BRICKS, "dwarf_bricks");
        this.registerBlock(Blocks.DWARF_FLOOR_2, "dwarf_floor_2");
        this.registerBlock(Blocks.DWARF_FLOOR_3, "dwarf_floor_3");
        this.registerBlock(Blocks.DWARF_FLOOR_4, "dwarf_floor_4");
        this.registerBlock(Blocks.DWARF_FLOOR_5, "dwarf_floor_5");
        this.registerBlock(Blocks.DWARF_FLOOR_6, "dwarf_floor_6");
        this.registerBlock(Blocks.DWARF_FLOOR_7, "dwarf_floor_7");
        this.registerBlock(Blocks.DWARF_FLOOR_8, "dwarf_floor_8");
        this.registerBlock(Blocks.DWARF_FLOOR_9, "dwarf_floor_9");
        this.registerBlock(Blocks.DWARF_KING_STONE, "dwarf_king_stone");
        this.registerBlock(Blocks.DWARVEN_KING_FLOOR_1, "dwarven_king_floor_1");
        this.registerBlock(Blocks.DWARVEN_KING_FLOOR_2, "dwarven_king_floor_2");
        this.registerBlock(Blocks.DWARVEN_PILLAR_DECORATION, "dwarven_pillar_decoration");
        this.registerBlock(Blocks.DWARVEN_STEEL, "dwarven_steel");
        this.registerBlock(Blocks.EREBOR_FLOOR_1, "erebor_floor_1");
        this.registerBlock(Blocks.EREBOR_FLOOR_2, "erebor_floor_2");
        this.registerBlock(Blocks.ERED_LUIN_STONE, "ered_luin_stone");
        this.registerBlock(Blocks.IRON_HILLS_FLOOR, "iron_hills_floor");
        this.registerBlock(Blocks.SMOOTH_GOLD, "smooth_gold");
        this.registerBlock(Blocks.RIVENDELL_WALL, "rivendell_wall");
        this.registerBlock(Blocks.SINDAR_STONE, "sindar_stone");
        this.registerBlock(Blocks.SINDAR_FLOOR_1, "sindar_floor_1");
        this.registerBlock(Blocks.SINDAR_FLOOR_2, "sindar_floor_2");
        this.registerBlock(Blocks.SINDAR_DECORATION_STONE, "sindar_decoration_stone");
        this.registerBlock(Blocks.NOLDOR_FLOOR, "noldor_floor");
        this.registerBlock(Blocks.NOLDOR_SANDSTONE_FLOOR, "noldor_sandstone_floor");
        this.registerBlock(Blocks.MALLORN_WOODPLANKS, "mallorn_woodplanks");
        this.registerBlock(Blocks.LINDON_WOOD, "lindon_wood");
        this.registerBlock(Blocks.LINDON_WALL_DECORATION, "lindon_wall_decoration");
        this.registerBlock(Blocks.HIGH_ELF_WALL, "high_elf_wall");
        this.registerBlock(Blocks.HARLINDON_WOOD, "harlindon_wood");
        this.registerBlock(Blocks.FORLOND_FLOOR, "forlond_floor");
        this.registerBlock(Blocks.FORLINDON_WOOD, "forlindon_wood");
        this.registerBlock(Blocks.FORLINDON_WALL, "forlindon_wall");
        this.registerBlock(Blocks.ELVEN_STONE_WALL, "elven_stone_wall");
        this.registerBlock(Blocks.ELVEN_NOBLE_WALL, "elven_noble_wall");
        this.registerBlock(Blocks.RIVENDELL_STATUE_BOTTOM, "rivendell_statue_bottom");
        this.registerBlock(Blocks.ELVEN_DECORATION_1, "elven_decoration_1");
        this.registerBlock(Blocks.ELVEN_DECORATION_LIGHT, "elven_decoration_light");
        this.registerBlock(Blocks.ELVEN_DECORATION_2, "elven_decoration_2");
        this.registerBlock(Blocks.ELVEN_DECORATION_3, "elven_decoration_3");
        this.registerBlock(Blocks.HOBBIT_FLOOR_1, "hobbit_floor_1");
        this.registerBlock(Blocks.HOBBIT_FLOOR_2, "hobbit_floor_2");
        this.registerBlock(Blocks.DARK_BRICKS, "dark_bricks");
        this.registerBlock(Blocks.ROHIRRIM_CROSSBEAM, "rohirrim_crossbeam");
        this.registerBlock(Blocks.CROSSBEAM_1, "crossbeam_1");
        this.registerBlock(Blocks.CROSSBEAM_2, "crossbeam_2");
        this.registerBlock(Blocks.CROSSBEAM_3, "crossbeam_3");
        this.registerBlock(Blocks.STANDARD_CROSSBEAM_1, "standard_crossbeam_1");
        this.registerBlock(Blocks.STANDARD_CROSSBEAM_2, "standard_crossbeam_2");
        this.registerBlock(Blocks.STANDARD_CROSSBEAM_3, "standard_crossbeam_3");
        this.registerBlock(Blocks.VERTICAL_BEAM, "vertical_beam");
        this.registerBlock(Blocks.VERTICAL_HORIZONTAL_BEAM, "vertical_horizontal_beam");
        this.registerBlock(Blocks.DALE_SANDSTONE, "dale_sandstone");
        this.registerBlock(Blocks.DALE_TILES, "dale_tiles");
        this.registerBlock(Blocks.DALE_WALL, "dale_wall");
        this.registerBlock(Blocks.HARAD_SANDSTONE_FLOOR, "harad_sandstone_floor");
        this.registerBlock(Blocks.HARAD_STONE_BRICKS, "harad_stone_bricks");
        this.registerBlock(Blocks.SANDFLOOR, "sandfloor");
        this.registerBlock(Blocks.UMBAR_HAVEN_FLOOR, "umbar_haven_floor");
        this.registerBlock(Blocks.UMBAR_STONE_BRICKS, "umbar_stone_bricks");
        this.registerBlock(Blocks.UMBAR_WALL_DECORATION, "umbar_wall_decoration");
        this.registerBlock(Blocks.KHAND_STONE_BRICKS, "khand_stone_bricks");
        this.registerBlock(Blocks.BROWN_BRICKS, "brown_bricks");
        this.registerBlock(Blocks.EDORAS_COBBLESTONE, "edoras_cobblestone");
        this.registerBlock(Blocks.REINFORCED_WOOD, "reinforced_wood");
        this.registerBlock(Blocks.MEDUSELD_WOOD, "meduseld_wood");
        this.registerBlock(Blocks.ROHIRRIM_WALL_DECORATION, "rohirrim_wall_decoration");
        this.registerBlock(Blocks.STATUE_HEAD, "statue_head");
        this.registerBlock(Blocks.ROHIRRIM_OAK_PLANKS, "rohirrim_oak_planks");
        this.registerBlock(Blocks.ROHIRRIM_SPRUCE_PLANKS, "rohirrim_spruce_planks");
        this.registerBlock(Blocks.ROHIRRIM_BIRCH_PLANKS, "rohirrim_birch_planks");
        this.registerBlock(Blocks.ROHIRRIM_JUNGLE_PLANKS, "rohirrim_jungle_planks");
        this.registerBlock(Blocks.ANGMAR_FLOOR, "angmar_floor");
        this.registerBlock(Blocks.ANGMAR_BRICKS, "angmar_bricks");
        this.registerBlock(Blocks.BEORNING_WOOD, "beorning_wood");
        this.registerBlock(Blocks.ANNUMINAS_DECORATION, "annuminas_decoration");
        this.registerBlock(Blocks.ARNOR_DECORATION_JEWEL, "arnor_decoration_jewel");
        this.registerBlock(Blocks.ARNORIAN_BRICKS, "arnorian_bricks");
        this.registerBlock(Blocks.MINAS_TIRITH_FLOOR, "minas_tirith_floor");
        this.registerBlock(Blocks.PELARGIR_STONE, "pelargir_stone");
        this.registerBlock(Blocks.WHITE_COBBLESTONE, "white_cobblestone");
        this.registerBlock(Blocks.DUNLAND_TOTEM, "dunland_totem");
        this.registerBlock(Blocks.DUNLAND_WALL_DECORATION, "dunland_wall_decoration");
        this.registerBlock(Blocks.MORDOR_LAMP, "mordor_lamp");
        this.registerBlock(Blocks.HARAD_LIGHT, "harad_light");
        this.registerBlock(Blocks.ELVEN_LAMP, "elven_lamp");
        this.registerBlock(Blocks.CARN_DUM_LAMP, "carn_dum_lamp");
        this.registerBlock(Blocks.BREE_TILE, "bree_tile");
        this.registerBlock(Blocks.CITY_MARKER, "city_marker");
        this.registerBlock(Blocks.HUMAN_TRAPDOOR, "human_trapdoor");
        this.registerBlock(Blocks.MORDOR_TRAPDOOR, "mordor_trapdoor");
        this.registerBlock(Blocks.KHANDISH_TRAPDOOR, "khandish_trapdoor");
        this.registerBlock(Blocks.SINDAR_TRAPDOOR, "sindar_trapdoor");
        this.registerBlock(Blocks.RIVERFOLK_TRAPDOOR, "riverfolk_trapdoor");
        this.registerBlock(Blocks.FORNOST_TRAPDOOR, "fornost_trapdoor");
        this.registerBlock(Blocks.PRISON_TRAPDOOR, "prison_trapdoor");
        this.registerBlock(Blocks.CANDLE, "candle");
        this.registerBlock(Blocks.HUMAN_LADDER, "human_ladder");
        this.registerBlock(Blocks.ROHIRRIM_LADDER, "rohirrim_ladder");
        this.registerBlock(Blocks.DUNLAND_LADDER, "dunland_ladder");
        this.registerBlock(Blocks.BUSH, "bush");
        this.registerBlock(Blocks.ALT_DEAD_BUSH, "alt_dead_bush");
        this.registerBlock(Blocks.CURSED_PLANT, "cursed_plant");
        this.registerBlock(Blocks.HARADWAITH_FERN, "haradwaith_fern");
        this.registerBlock(Blocks.LORILENDEL, "lorilendel");
        this.registerBlock(Blocks.STAKES, "stakes");
        this.registerBlock(Blocks.SHIRE_GARDEN_FLOWER,  "shire_garden_flower");
        this.registerBlock(Blocks.PILE_OF_COINS, "pile_of_coins");
        this.registerBlock(Blocks.GREY_COLUMN, "grey_column");
        this.registerBlock(Blocks.GREY_COLUMN_TOP, "grey_column_top");
        this.registerBlock(Blocks.COLUMN_TOP_DECORATION, "column_top_decoration");
        this.registerBlock(Blocks.MEDUSELD_PILLAR, "meduseld_pillar");
        this.registerBlock(Blocks.DIAGONAL_BRICKS, "diagonal_bricks");
        this.registerBlock(Blocks.BELL, "bell");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.ELVEN.getMetadata(), "elven_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.HUMAN.getMetadata(), "human_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.GONDORIAN.getMetadata(), "gondorian_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.ROHIRRIM.getMetadata(), "rohirrim_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.HOBBIT.getMetadata(), "hobbit_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.MORDOR.getMetadata(), "mordor_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.ISENGARD.getMetadata(), "isengard_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.GOBLIN.getMetadata(), "goblin_crafting_table");
        this.registerBlock(Blocks.CUSTOM_CRAFTING_TABLE, BlockCustomWorkbench.EnumType.DWARVEN.getMetadata(), "dwarven_crafting_table");
        this.registerBlock(Blocks.WATER_WHEEL, "water_wheel");
        // End Awaken Dreams code
        this.registerItem(Items.IRON_SHOVEL, "iron_shovel");
        this.registerItem(Items.IRON_PICKAXE, "iron_pickaxe");
        this.registerItem(Items.IRON_AXE, "iron_axe");
        this.registerItem(Items.FLINT_AND_STEEL, "flint_and_steel");
        this.registerItem(Items.APPLE, "apple");
        this.registerItem(Items.BOW, "bow");
        this.registerItem(Items.ARROW, "arrow");
        this.registerItem(Items.SPECTRAL_ARROW, "spectral_arrow");
        this.registerItem(Items.TIPPED_ARROW, "tipped_arrow");
        this.registerItem(Items.COAL, 0, "coal");
        this.registerItem(Items.COAL, 1, "charcoal");
        this.registerItem(Items.DIAMOND, "diamond");
        this.registerItem(Items.IRON_INGOT, "iron_ingot");
        this.registerItem(Items.GOLD_INGOT, "gold_ingot");
        this.registerItem(Items.IRON_SWORD, "iron_sword");
        this.registerItem(Items.WOODEN_SWORD, "wooden_sword");
        this.registerItem(Items.WOODEN_SHOVEL, "wooden_shovel");
        this.registerItem(Items.WOODEN_PICKAXE, "wooden_pickaxe");
        this.registerItem(Items.WOODEN_AXE, "wooden_axe");
        this.registerItem(Items.STONE_SWORD, "stone_sword");
        this.registerItem(Items.STONE_SHOVEL, "stone_shovel");
        this.registerItem(Items.STONE_PICKAXE, "stone_pickaxe");
        this.registerItem(Items.STONE_AXE, "stone_axe");
        this.registerItem(Items.DIAMOND_SWORD, "diamond_sword");
        this.registerItem(Items.DIAMOND_SHOVEL, "diamond_shovel");
        this.registerItem(Items.DIAMOND_PICKAXE, "diamond_pickaxe");
        this.registerItem(Items.DIAMOND_AXE, "diamond_axe");
        this.registerItem(Items.STICK, "stick");
        this.registerItem(Items.BOWL, "bowl");
        this.registerItem(Items.MUSHROOM_STEW, "mushroom_stew");
        this.registerItem(Items.GOLDEN_SWORD, "golden_sword");
        this.registerItem(Items.GOLDEN_SHOVEL, "golden_shovel");
        this.registerItem(Items.GOLDEN_PICKAXE, "golden_pickaxe");
        this.registerItem(Items.GOLDEN_AXE, "golden_axe");
        this.registerItem(Items.STRING, "string");
        this.registerItem(Items.FEATHER, "feather");
        this.registerItem(Items.GUNPOWDER, "gunpowder");
        this.registerItem(Items.WOODEN_HOE, "wooden_hoe");
        this.registerItem(Items.STONE_HOE, "stone_hoe");
        this.registerItem(Items.IRON_HOE, "iron_hoe");
        this.registerItem(Items.DIAMOND_HOE, "diamond_hoe");
        this.registerItem(Items.GOLDEN_HOE, "golden_hoe");
        this.registerItem(Items.WHEAT_SEEDS, "wheat_seeds");
        this.registerItem(Items.WHEAT, "wheat");
        this.registerItem(Items.BREAD, "bread");
        this.registerItem(Items.LEATHER_HELMET, "leather_helmet");
        this.registerItem(Items.LEATHER_CHESTPLATE, "leather_chestplate");
        this.registerItem(Items.LEATHER_LEGGINGS, "leather_leggings");
        this.registerItem(Items.LEATHER_BOOTS, "leather_boots");
        this.registerItem(Items.CHAINMAIL_HELMET, "chainmail_helmet");
        this.registerItem(Items.CHAINMAIL_CHESTPLATE, "chainmail_chestplate");
        this.registerItem(Items.CHAINMAIL_LEGGINGS, "chainmail_leggings");
        this.registerItem(Items.CHAINMAIL_BOOTS, "chainmail_boots");
        this.registerItem(Items.IRON_HELMET, "iron_helmet");
        this.registerItem(Items.IRON_CHESTPLATE, "iron_chestplate");
        this.registerItem(Items.IRON_LEGGINGS, "iron_leggings");
        this.registerItem(Items.IRON_BOOTS, "iron_boots");
        this.registerItem(Items.DIAMOND_HELMET, "diamond_helmet");
        this.registerItem(Items.DIAMOND_CHESTPLATE, "diamond_chestplate");
        this.registerItem(Items.DIAMOND_LEGGINGS, "diamond_leggings");
        this.registerItem(Items.DIAMOND_BOOTS, "diamond_boots");
        this.registerItem(Items.GOLDEN_HELMET, "golden_helmet");
        this.registerItem(Items.GOLDEN_CHESTPLATE, "golden_chestplate");
        this.registerItem(Items.GOLDEN_LEGGINGS, "golden_leggings");
        this.registerItem(Items.GOLDEN_BOOTS, "golden_boots");
        this.registerItem(Items.FLINT, "flint");
        this.registerItem(Items.PORKCHOP, "porkchop");
        this.registerItem(Items.COOKED_PORKCHOP, "cooked_porkchop");
        this.registerItem(Items.PAINTING, "painting");
        this.registerItem(Items.GOLDEN_APPLE, "golden_apple");
        this.registerItem(Items.GOLDEN_APPLE, 1, "golden_apple");
        this.registerItem(Items.SIGN, "sign");
        this.registerItem(Items.OAK_DOOR, "oak_door");
        this.registerItem(Items.SPRUCE_DOOR, "spruce_door");
        this.registerItem(Items.BIRCH_DOOR, "birch_door");
        this.registerItem(Items.JUNGLE_DOOR, "jungle_door");
        this.registerItem(Items.ACACIA_DOOR, "acacia_door");
        this.registerItem(Items.DARK_OAK_DOOR, "dark_oak_door");
        this.registerItem(Items.BUCKET, "bucket");
        this.registerItem(Items.WATER_BUCKET, "water_bucket");
        this.registerItem(Items.LAVA_BUCKET, "lava_bucket");
        this.registerItem(Items.MINECART, "minecart");
        this.registerItem(Items.SADDLE, "saddle");
        this.registerItem(Items.IRON_DOOR, "iron_door");
        this.registerItem(Items.REDSTONE, "redstone");
        this.registerItem(Items.SNOWBALL, "snowball");
        this.registerItem(Items.BOAT, "oak_boat");
        this.registerItem(Items.SPRUCE_BOAT, "spruce_boat");
        this.registerItem(Items.BIRCH_BOAT, "birch_boat");
        this.registerItem(Items.JUNGLE_BOAT, "jungle_boat");
        this.registerItem(Items.ACACIA_BOAT, "acacia_boat");
        this.registerItem(Items.DARK_OAK_BOAT, "dark_oak_boat");
        this.registerItem(Items.LEATHER, "leather");
        this.registerItem(Items.MILK_BUCKET, "milk_bucket");
        this.registerItem(Items.BRICK, "brick");
        this.registerItem(Items.CLAY_BALL, "clay_ball");
        this.registerItem(Items.REEDS, "reeds");
        this.registerItem(Items.PAPER, "paper");
        this.registerItem(Items.BOOK, "book");
        this.registerItem(Items.SLIME_BALL, "slime_ball");
        this.registerItem(Items.CHEST_MINECART, "chest_minecart");
        this.registerItem(Items.FURNACE_MINECART, "furnace_minecart");
        this.registerItem(Items.EGG, "egg");
        this.registerItem(Items.COMPASS, "compass");
        this.registerItem(Items.FISHING_ROD, "fishing_rod");
        this.registerItem(Items.CLOCK, "clock");
        this.registerItem(Items.GLOWSTONE_DUST, "glowstone_dust");
        this.registerItem(Items.FISH, ItemFishFood.FishType.COD.getMetadata(), "cod");
        this.registerItem(Items.FISH, ItemFishFood.FishType.SALMON.getMetadata(), "salmon");
        this.registerItem(Items.FISH, ItemFishFood.FishType.CLOWNFISH.getMetadata(), "clownfish");
        this.registerItem(Items.FISH, ItemFishFood.FishType.PUFFERFISH.getMetadata(), "pufferfish");
        this.registerItem(Items.COOKED_FISH, ItemFishFood.FishType.COD.getMetadata(), "cooked_cod");
        this.registerItem(Items.COOKED_FISH, ItemFishFood.FishType.SALMON.getMetadata(), "cooked_salmon");
        this.registerItem(Items.DYE, EnumDyeColor.BLACK.getDyeDamage(), "dye_black");
        this.registerItem(Items.DYE, EnumDyeColor.RED.getDyeDamage(), "dye_red");
        this.registerItem(Items.DYE, EnumDyeColor.GREEN.getDyeDamage(), "dye_green");
        this.registerItem(Items.DYE, EnumDyeColor.BROWN.getDyeDamage(), "dye_brown");
        this.registerItem(Items.DYE, EnumDyeColor.BLUE.getDyeDamage(), "dye_blue");
        this.registerItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage(), "dye_purple");
        this.registerItem(Items.DYE, EnumDyeColor.CYAN.getDyeDamage(), "dye_cyan");
        this.registerItem(Items.DYE, EnumDyeColor.SILVER.getDyeDamage(), "dye_silver");
        this.registerItem(Items.DYE, EnumDyeColor.GRAY.getDyeDamage(), "dye_gray");
        this.registerItem(Items.DYE, EnumDyeColor.PINK.getDyeDamage(), "dye_pink");
        this.registerItem(Items.DYE, EnumDyeColor.LIME.getDyeDamage(), "dye_lime");
        this.registerItem(Items.DYE, EnumDyeColor.YELLOW.getDyeDamage(), "dye_yellow");
        this.registerItem(Items.DYE, EnumDyeColor.LIGHT_BLUE.getDyeDamage(), "dye_light_blue");
        this.registerItem(Items.DYE, EnumDyeColor.MAGENTA.getDyeDamage(), "dye_magenta");
        this.registerItem(Items.DYE, EnumDyeColor.ORANGE.getDyeDamage(), "dye_orange");
        this.registerItem(Items.DYE, EnumDyeColor.WHITE.getDyeDamage(), "dye_white");
        this.registerItem(Items.BONE, "bone");
        this.registerItem(Items.SUGAR, "sugar");
        this.registerItem(Items.CAKE, "cake");
        this.registerItem(Items.BED, "bed");
        this.registerItem(Items.REPEATER, "repeater");
        this.registerItem(Items.COOKIE, "cookie");
        this.registerItem(Items.SHEARS, "shears");
        this.registerItem(Items.MELON, "melon");
        this.registerItem(Items.PUMPKIN_SEEDS, "pumpkin_seeds");
        this.registerItem(Items.MELON_SEEDS, "melon_seeds");
        this.registerItem(Items.BEEF, "beef");
        this.registerItem(Items.COOKED_BEEF, "cooked_beef");
        this.registerItem(Items.CHICKEN, "chicken");
        this.registerItem(Items.COOKED_CHICKEN, "cooked_chicken");
        this.registerItem(Items.RABBIT, "rabbit");
        this.registerItem(Items.COOKED_RABBIT, "cooked_rabbit");
        this.registerItem(Items.MUTTON, "mutton");
        this.registerItem(Items.COOKED_MUTTON, "cooked_mutton");
        this.registerItem(Items.RABBIT_FOOT, "rabbit_foot");
        this.registerItem(Items.RABBIT_HIDE, "rabbit_hide");
        this.registerItem(Items.RABBIT_STEW, "rabbit_stew");
        this.registerItem(Items.ROTTEN_FLESH, "rotten_flesh");
        this.registerItem(Items.ENDER_PEARL, "ender_pearl");
        this.registerItem(Items.BLAZE_ROD, "blaze_rod");
        this.registerItem(Items.GHAST_TEAR, "ghast_tear");
        this.registerItem(Items.GOLD_NUGGET, "gold_nugget");
        this.registerItem(Items.NETHER_WART, "nether_wart");
        this.registerItem(Items.BEETROOT, "beetroot");
        this.registerItem(Items.BEETROOT_SEEDS, "beetroot_seeds");
        this.registerItem(Items.BEETROOT_SOUP, "beetroot_soup");
        this.registerItem(Items.POTIONITEM, "bottle_drinkable");
        this.registerItem(Items.SPLASH_POTION, "bottle_splash");
        this.registerItem(Items.LINGERING_POTION, "bottle_lingering");
        this.registerItem(Items.GLASS_BOTTLE, "glass_bottle");
        this.registerItem(Items.DRAGON_BREATH, "dragon_breath");
        this.registerItem(Items.SPIDER_EYE, "spider_eye");
        this.registerItem(Items.FERMENTED_SPIDER_EYE, "fermented_spider_eye");
        this.registerItem(Items.BLAZE_POWDER, "blaze_powder");
        this.registerItem(Items.MAGMA_CREAM, "magma_cream");
        this.registerItem(Items.BREWING_STAND, "brewing_stand");
        this.registerItem(Items.CAULDRON, "cauldron");
        this.registerItem(Items.ENDER_EYE, "ender_eye");
        this.registerItem(Items.SPECKLED_MELON, "speckled_melon");
        this.itemModelMesher.register(Items.SPAWN_EGG, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("spawn_egg", "inventory");
            }
        });
        this.registerItem(Items.EXPERIENCE_BOTTLE, "experience_bottle");
        this.registerItem(Items.FIRE_CHARGE, "fire_charge");
        this.registerItem(Items.WRITABLE_BOOK, "writable_book");
        this.registerItem(Items.EMERALD, "emerald");
        this.registerItem(Items.ITEM_FRAME, "item_frame");
        this.registerItem(Items.FLOWER_POT, "flower_pot");
        this.registerItem(Items.CARROT, "carrot");
        this.registerItem(Items.POTATO, "potato");
        this.registerItem(Items.BAKED_POTATO, "baked_potato");
        this.registerItem(Items.POISONOUS_POTATO, "poisonous_potato");
        this.registerItem(Items.MAP, "map");
        this.registerItem(Items.GOLDEN_CARROT, "golden_carrot");
        this.registerItem(Items.SKULL, 0, "skull_skeleton");
        this.registerItem(Items.SKULL, 1, "skull_wither");
        this.registerItem(Items.SKULL, 2, "skull_zombie");
        this.registerItem(Items.SKULL, 3, "skull_char");
        this.registerItem(Items.SKULL, 4, "skull_creeper");
        this.registerItem(Items.SKULL, 5, "skull_dragon");
        this.registerItem(Items.CARROT_ON_A_STICK, "carrot_on_a_stick");
        this.registerItem(Items.NETHER_STAR, "nether_star");
        this.registerItem(Items.END_CRYSTAL, "end_crystal");
        this.registerItem(Items.PUMPKIN_PIE, "pumpkin_pie");
        this.registerItem(Items.FIREWORK_CHARGE, "firework_charge");
        this.registerItem(Items.COMPARATOR, "comparator");
        this.registerItem(Items.NETHERBRICK, "netherbrick");
        this.registerItem(Items.QUARTZ, "quartz");
        this.registerItem(Items.TNT_MINECART, "tnt_minecart");
        this.registerItem(Items.HOPPER_MINECART, "hopper_minecart");
        this.registerItem(Items.ARMOR_STAND, "armor_stand");
        this.registerItem(Items.IRON_HORSE_ARMOR, "iron_horse_armor");
        this.registerItem(Items.GOLDEN_HORSE_ARMOR, "golden_horse_armor");
        this.registerItem(Items.DIAMOND_HORSE_ARMOR, "diamond_horse_armor");
        this.registerItem(Items.LEAD, "lead");
        this.registerItem(Items.NAME_TAG, "name_tag");
        this.itemModelMesher.register(Items.BANNER, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("banner", "inventory");
            }
        });
        this.itemModelMesher.register(Items.SHIELD, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("shield", "inventory");
            }
        });
        this.registerItem(Items.ELYTRA, "elytra");
        this.registerItem(Items.CHORUS_FRUIT, "chorus_fruit");
        this.registerItem(Items.CHORUS_FRUIT_POPPED, "chorus_fruit_popped");
        this.registerItem(Items.RECORD_13, "record_13");
        this.registerItem(Items.RECORD_CAT, "record_cat");
        this.registerItem(Items.RECORD_BLOCKS, "record_blocks");
        this.registerItem(Items.RECORD_CHIRP, "record_chirp");
        this.registerItem(Items.RECORD_FAR, "record_far");
        this.registerItem(Items.RECORD_MALL, "record_mall");
        this.registerItem(Items.RECORD_MELLOHI, "record_mellohi");
        this.registerItem(Items.RECORD_STAL, "record_stal");
        this.registerItem(Items.RECORD_STRAD, "record_strad");
        this.registerItem(Items.RECORD_WARD, "record_ward");
        this.registerItem(Items.RECORD_11, "record_11");
        this.registerItem(Items.RECORD_WAIT, "record_wait");
        this.registerItem(Items.PRISMARINE_SHARD, "prismarine_shard");
        this.registerItem(Items.PRISMARINE_CRYSTALS, "prismarine_crystals");
        this.itemModelMesher.register(Items.ENCHANTED_BOOK, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("enchanted_book", "inventory");
            }
        });
        this.itemModelMesher.register(Items.FILLED_MAP, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("filled_map", "inventory");
            }
        });
        this.registerBlock(Blocks.COMMAND_BLOCK, "command_block");
        this.registerItem(Items.FIREWORKS, "fireworks");
        this.registerItem(Items.COMMAND_BLOCK_MINECART, "command_block_minecart");
        this.registerBlock(Blocks.BARRIER, "barrier");
        this.registerBlock(Blocks.MOB_SPAWNER, "mob_spawner");
        this.registerItem(Items.WRITTEN_BOOK, "written_book");
        this.registerBlock(Blocks.BROWN_MUSHROOM_BLOCK, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "brown_mushroom_block");
        this.registerBlock(Blocks.RED_MUSHROOM_BLOCK, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "red_mushroom_block");
        this.registerBlock(Blocks.DRAGON_EGG, "dragon_egg");
        this.registerBlock(Blocks.REPEATING_COMMAND_BLOCK, "repeating_command_block");
        this.registerBlock(Blocks.CHAIN_COMMAND_BLOCK, "chain_command_block");
        this.registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.SAVE.getModeId(), "structure_block");
        this.registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.LOAD.getModeId(), "structure_block");
        this.registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.CORNER.getModeId(), "structure_block");
        this.registerBlock(Blocks.STRUCTURE_BLOCK, TileEntityStructure.Mode.DATA.getModeId(), "structure_block");
        // Begin Awaken Dreams code
        this.registerItem(Items.LEMBAS, "awakendreams:lembas");
        this.registerItem(Items.MITHRIL_INGOT, "mithril_ingot");
        this.registerItem(Items.BRONZE_INGOT, "bronze_ingot");
        this.itemModelMesher.register(Items.PIPE, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
            	ModelResourceLocation m = new ModelResourceLocation(Items.PIPE.getModelName(stack), "inventory");
            	//System.out.println("modelName=" + m.getResourceDomain() + " " + m.getResourcePath());
            	return m;
            }
        });
        this.registerItem(Items.TOBACCO, "tobacco");
        this.registerItem(Items.JADE, "jade");
        this.registerItem(Items.AMBER, "amber");
        this.registerItem(Items.CRYSTAL, "crystal");
        this.registerItem(Items.AMETHYST, "amethyst");
        this.registerItem(Items.ONYX, "onyx");
        this.registerItem(Items.MOONSTONE, "moonstone");
        this.registerItem(Items.TANZANITE, "tanzanite");
        this.registerItem(Items.URUK_SWORD, "uruk_sword");
        this.registerItem(Items.TOMATO, "tomato");
        this.registerItem(Items.BLUE_FEATHER, "blue_feather");
        this.registerItem(Items.SPIDER_POISON, "spider_poison");
        this.registerItem(Items.HERUGRIM, "herugrim");
        this.registerItem(Items.NUTS, "nuts");
        this.registerItem(Items.CORN_COB, "corn_cob");
        this.registerItem(Items.STAFF_BASE, "staff_base");
        this.registerItem(Items.KNIFE, "knife");
        this.registerItem(Items.BERRIES, "berries");
        this.registerItem(Items.BEAR_CLAW, "bear_claw");
        this.registerItem(Items.BEAST_SKIN, "beast_skin");
        this.registerItem(Items.DEAD_ROOT, "dead_root");
        this.registerItem(Items.EAGLE_FEATHER, "eagle_feather");
        this.registerItem(Items.GOBLIN_EYE, "goblin_eye");
        this.registerItem(Items.HELEGROG_DEAMON_HEART, "helegrog_deamon_heart");
        this.registerItem(Items.ORC_BLOOD, "orc_blood");
        this.registerItem(Items.OWL_FEATHER, "owl_feather");
        this.registerItem(Items.SCORPION_TAIL, "scorpion_tail");
        this.registerItem(Items.SHARK_TOOTH, "shark_tooth");
        this.registerItem(Items.TROLL_SKIN, "troll_skin");
        this.registerItem(Items.WARG_FANG, "warg_fang");
        this.registerItem(Items.BLUE_BED, "blue_bed");
        this.registerItem(Items.BROWN_BED, "brown_bed");
        this.registerItem(Items.BREE_DOOR, "bree_door");
        this.registerItem(Items.RUCKSACK, "rucksack");
        this.registerItem(Items.CRAM, "cram");
        this.registerItem(Items.CABBAGE, "cabbage");
        this.registerItem(Items.COCONUT, "coconut");
        this.registerItem(Items.RAW_DEER_MEAT, "raw_deer_meat");
        this.registerItem(Items.COOKED_DEER_MEAT, "cooked_deer_meat");
        this.registerItem(Items.MUFFIN, "muffin");
        this.registerItem(Items.STRAWBERRY, "strawberry");
        this.registerItem(Items.STRAWBERRY_MUFFIN, "strawberry_muffin");
        this.registerItem(Items.ORANGE, "orange");
        this.registerItem(Items.ORANGE_MUFFIN, "orange_muffin");
        this.registerItem(Items.PEAR, "pear");
        this.registerItem(Items.RAW_MEAT, "raw_meat");
        this.registerItem(Items.COOKED_MEAT, "cooked_meat");
        this.registerItem(Items.ROHAN_HELMET_1, "rohan_helmet_1");
        this.registerItem(Items.ROHAN_HELMET_2, "rohan_helmet_2");
        this.registerItem(Items.ROHAN_HELMET_3, "rohan_helmet_3");
        this.registerItem(Items.ROHAN_HELMET_4, "rohan_helmet_4");
        this.registerItem(Items.ROHAN_HELMET_5, "rohan_helmet_5");
        this.registerItem(Items.ROHAN_HELMET_6, "rohan_helmet_6");
        this.registerItem(Items.ROHAN_CHESTPLATE, "rohan_chestplate");
        this.registerItem(Items.TELESCOPE, "telescope");
        this.registerItem(Items.HUMAN_DAGGER, "human_dagger");
        this.registerItem(Items.HOBBIT_DAGGER, "hobbit_dagger");
        this.registerItem(Items.ELF_DAGGER, "elf_dagger");
        this.registerItem(Items.GONDORIAN_SWORD, "gondorian_sword");
        this.registerItem(Items.SOUTHERN_STAR, "southern_star");
        this.registerItem(Items.SOUTHLINCH, "southlinch");
        this.registerItem(Items.OLD_TOBY, "old_toby");
        this.registerItem(Items.STONE_OF_DARKNESS, "stone_of_darkness");
        this.registerItem(Items.STONE_OF_EARTH, "stone_of_earth");
        this.registerItem(Items.STONE_OF_FIRE, "stone_of_fire");
        this.registerItem(Items.STONE_OF_GREED, "stone_of_greed");
        this.registerItem(Items.STONE_OF_HOPE, "stone_of_hope");
        this.registerItem(Items.STONE_OF_NATURE, "stone_of_nature");
        this.registerItem(Items.STONE_OF_SUNLIGHT, "stone_of_sunlight");
        this.registerItem(Items.STONE_OF_SEA, "stone_of_sea");
        this.registerItem(Items.STONE_OF_SKY, "stone_of_sky");
        this.registerItem(Items.STONE_OF_WEALTH, "stone_of_wealth");
        this.registerItem(Items.STONE_OF_WIND, "stone_of_wind");
        this.registerItem(Items.HAMMER, "hammer");
        this.registerItem(Items.GOLD_RING, "gold_ring");
        this.registerItem(Items.ONYX_GOLD_RING, "onyx_gold_ring");
        this.registerItem(Items.TANZANITE_GOLD_RING, "tanzanite_gold_ring");
        this.registerItem(Items.JADE_GOLD_RING, "jade_gold_ring");
        this.registerItem(Items.MOONSTONE_GOLD_RING, "moonstone_gold_ring");
        this.registerItem(Items.AMETHYST_GOLD_RING, "amethyst_gold_ring");
        this.registerItem(Items.RUBY_GOLD_RING, "ruby_gold_ring");
        this.registerItem(Items.QUARTZ_GOLD_RING, "quartz_gold_ring");
        this.registerItem(Items.AMBER_GOLD_RING, "amber_gold_ring");
        this.registerItem(Items.SILVER_RING, "silver_ring");
        this.registerItem(Items.ONYX_SILVER_RING, "onyx_silver_ring");
        this.registerItem(Items.TANZANITE_SILVER_RING, "tanzanite_silver_ring");
        this.registerItem(Items.JADE_SILVER_RING, "jade_silver_ring");
        this.registerItem(Items.MOONSTONE_SILVER_RING, "moonstone_silver_ring");
        this.registerItem(Items.AMETHYST_SILVER_RING, "amethyst_silver_ring");
        this.registerItem(Items.RUBY_SILVER_RING, "ruby_silver_ring");
        this.registerItem(Items.QUARTZ_SILVER_RING, "quartz_silver_ring");
        this.registerItem(Items.AMBER_SILVER_RING, "amber_silver_ring");
        this.registerItem(Items.BRONZE_RING, "bronze_ring");
        this.registerItem(Items.ONYX_BRONZE_RING, "onyx_bronze_ring");
        this.registerItem(Items.TANZANITE_BRONZE_RING, "tanzanite_bronze_ring");
        this.registerItem(Items.JADE_BRONZE_RING, "jade_bronze_ring");
        this.registerItem(Items.MOONSTONE_BRONZE_RING, "moonstone_bronze_ring");
        this.registerItem(Items.AMETHYST_BRONZE_RING, "amethyst_bronze_ring");
        this.registerItem(Items.RUBY_BRONZE_RING, "ruby_bronze_ring");
        this.registerItem(Items.QUARTZ_BRONZE_RING, "quartz_bronze_ring");
        this.registerItem(Items.AMBER_BRONZE_RING, "amber_bronze_ring");
        this.registerItem(Items.MITHRIL_RING, "mithril_ring");
        this.registerItem(Items.ONYX_MITHRIL_RING, "onyx_mithril_ring");
        this.registerItem(Items.TANZANITE_MITHRIL_RING, "tanzanite_mithril_ring");
        this.registerItem(Items.JADE_MITHRIL_RING, "jade_mithril_ring");
        this.registerItem(Items.MOONSTONE_MITHRIL_RING, "moonstone_mithril_ring");
        this.registerItem(Items.AMETHYST_MITHRIL_RING, "amethyst_mithril_ring");
        this.registerItem(Items.RUBY_MITHRIL_RING, "ruby_mithril_ring");
        this.registerItem(Items.QUARTZ_MITHRIL_RING, "quartz_mithril_ring");
        this.registerItem(Items.AMBER_MITHRIL_RING, "amber_mithril_ring");
        this.registerItem(Items.SALT, "salt");
        this.registerItem(Items.NOLDOR_CHESTPLATE, "noldor_chestplate");
        this.registerItem(Items.SILVER_INGOT, "silver_ingot");
        this.registerItem(Items.DWARF_DOOR, "dwarf_door");
        this.registerItem(Items.BOROMIRS_SWORD, "boromirs_sword");
        this.registerItem(Items.ORC_SWORD_1, "orc_sword_1");
        this.registerItem(Items.ORC_SWORD_2, "orc_sword_2");
        this.registerItem(Items.GOBLIN_SWORD, "goblin_sword");
        this.registerItem(Items.HOBBIT_AXE, "hobbit_axe");
        this.registerItem(Items.SHARKUS_SHORTSWORD, "sharkus_shortsword");
        this.registerItem(Items.BATTLE_PICKAXE, "battle_pickaxe");
        this.registerItem(Items.SHIRRIFF_CLUB, "shirriff_club");
        this.registerItem(Items.PIKE_CLUB, "pike_club");
        this.registerItem(Items.HOBBIT_SWORD, "hobbit_sword");
        this.registerItem(Items.HOBBIT_HAMMER, "hobbit_hammer");
        this.registerItem(Items.BOAR_HORNS, "boar_horns");
        this.registerItem(Items.GOLD_RING_OF_DARKNESS, "gold_ring_of_darkness");
        this.registerItem(Items.GOLD_RING_OF_EARTH, "gold_ring_of_earth");
        this.registerItem(Items.GOLD_RING_OF_FIRE, "gold_ring_of_fire");
        this.registerItem(Items.GOLD_RING_OF_GREED, "gold_ring_of_greed");
        this.registerItem(Items.GOLD_RING_OF_HOPE, "gold_ring_of_hope");
        this.registerItem(Items.GOLD_RING_OF_NATURE, "gold_ring_of_nature");
        this.registerItem(Items.GOLD_RING_OF_SEA, "gold_ring_of_sea");
        this.registerItem(Items.GOLD_RING_OF_SKY, "gold_ring_of_sky");
        this.registerItem(Items.GOLD_RING_OF_SUNLIGHT, "gold_ring_of_sunlight");
        this.registerItem(Items.GOLD_RING_OF_WEALTH, "gold_ring_of_wealth");
        this.registerItem(Items.GOLD_RING_OF_WIND, "gold_ring_of_wind");
        this.registerItem(Items.SILVER_RING_OF_DARKNESS, "silver_ring_of_darkness");
        this.registerItem(Items.SILVER_RING_OF_EARTH, "silver_ring_of_earth");
        this.registerItem(Items.SILVER_RING_OF_FIRE, "silver_ring_of_fire");
        this.registerItem(Items.SILVER_RING_OF_GREED, "silver_ring_of_greed");
        this.registerItem(Items.SILVER_RING_OF_HOPE, "silver_ring_of_hope");
        this.registerItem(Items.SILVER_RING_OF_NATURE, "silver_ring_of_nature");
        this.registerItem(Items.SILVER_RING_OF_SEA, "silver_ring_of_sea");
        this.registerItem(Items.SILVER_RING_OF_SKY, "silver_ring_of_sky");
        this.registerItem(Items.SILVER_RING_OF_SUNLIGHT, "silver_ring_of_sunlight");
        this.registerItem(Items.SILVER_RING_OF_WEALTH, "silver_ring_of_wealth");
        this.registerItem(Items.SILVER_RING_OF_WIND, "silver_ring_of_wind");
        this.registerItem(Items.BRONZE_RING_OF_DARKNESS, "bronze_ring_of_darkness");
        this.registerItem(Items.BRONZE_RING_OF_EARTH, "bronze_ring_of_earth");
        this.registerItem(Items.BRONZE_RING_OF_FIRE, "bronze_ring_of_fire");
        this.registerItem(Items.BRONZE_RING_OF_GREED, "bronze_ring_of_greed");
        this.registerItem(Items.BRONZE_RING_OF_HOPE, "bronze_ring_of_hope");
        this.registerItem(Items.BRONZE_RING_OF_NATURE, "bronze_ring_of_nature");
        this.registerItem(Items.BRONZE_RING_OF_SEA, "bronze_ring_of_sea");
        this.registerItem(Items.BRONZE_RING_OF_SKY, "bronze_ring_of_sky");
        this.registerItem(Items.BRONZE_RING_OF_SUNLIGHT, "bronze_ring_of_sunlight");
        this.registerItem(Items.BRONZE_RING_OF_WEALTH, "bronze_ring_of_wealth");
        this.registerItem(Items.BRONZE_RING_OF_WIND, "bronze_ring_of_wind");
        this.registerItem(Items.MITHRIL_RING_OF_DARKNESS, "mithril_ring_of_darkness");
        this.registerItem(Items.MITHRIL_RING_OF_EARTH, "mithril_ring_of_earth");
        this.registerItem(Items.MITHRIL_RING_OF_FIRE, "mithril_ring_of_fire");
        this.registerItem(Items.MITHRIL_RING_OF_GREED, "mithril_ring_of_greed");
        this.registerItem(Items.MITHRIL_RING_OF_HOPE, "mithril_ring_of_hope");
        this.registerItem(Items.MITHRIL_RING_OF_NATURE, "mithril_ring_of_nature");
        this.registerItem(Items.MITHRIL_RING_OF_SEA, "mithril_ring_of_sea");
        this.registerItem(Items.MITHRIL_RING_OF_SKY, "mithril_ring_of_sky");
        this.registerItem(Items.MITHRIL_RING_OF_SUNLIGHT, "mithril_ring_of_sunlight");
        this.registerItem(Items.MITHRIL_RING_OF_WEALTH, "mithril_ring_of_wealth");
        this.registerItem(Items.MITHRIL_RING_OF_WIND, "mithril_ring_of_wind");
        this.registerItem(Items.ANDURIL, "anduril");
        this.registerItem(Items.MORGUL_SWORD, "morgul_sword");
        this.registerItem(Items.ELVEN_LONG_SWORD, "elven_long_sword");
        this.registerItem(Items.BERSERKER_SWORD, "berserker_sword");
        this.registerItem(Items.TWO_HANDED_SWORD, "two_handed_sword");
        this.registerItem(Items.THROWING_STONE, "throwing_stone");
        this.registerItem(Items.ROHIRRIM_AXE, "rohirrim_axe");
        this.registerItem(Items.ELVEN_DOOR, "elven_door");
        this.registerItem(Items.HUMAN_DOOR, "human_door");
        this.registerItem(Items.GREEN_BED, "green_bed");
        this.registerItem(Items.AICANAR, "aicanar");
        this.registerItem(Items.BALINS_SWORD, "balins_sword");
        this.registerItem(Items.BIFURS_SPEAR, "bifurs_spear");
        this.registerItem(Items.BOMBURS_SPOON, "bomburs_spoon");
        this.registerItem(Items.DORIS_SWORD, "doris_sword");
        this.registerItem(Items.DWALINS_AXE, "dwalins_axe");
        this.registerItem(Items.FILIS_KNIFE, "filis_knife");
        this.registerItem(Items.FILIS_SWORD, "filis_sword");
        this.registerItem(Items.KILIS_SWORD, "kilis_sword");
        this.registerItem(Items.NORIS_CLUB, "noris_club");
        this.registerItem(Items.OINS_STAFF, "oins_staff");
        this.registerItem(Items.ORCRIST, "orcrist");
        this.registerItem(Items.SWORD_OF_WESTERNESSE, "sword_of_westernesse");
        this.registerItem(Items.THORINS_SWORD, "thorins_sword");
        this.registerItem(Items.THRAINS_WAR_HAMMER, "thrains_war_hammer");
        this.registerItem(Items.THRORS_WAR_HAMMER, "thrors_war_hammer");
        this.registerItem(Items.ARAGORNS_ELF_KNIFE, "aragorns_elf_knife");
        this.registerItem(Items.EOWYNS_SWORD, "eowyns_sword");
        this.registerItem(Items.GANDALF_THE_GREYS_STAFF, "gandalf_the_greys_staff");
        this.registerItem(Items.GANDALF_THE_WHITES_STAFF, "gandalf_the_whites_staff");
        this.registerItem(Items.GIMLIS_TWO_HEADED_AXE, "gimlis_two_headed_axe");
        this.registerItem(Items.GIMLIS_LONGAXE, "gimlis_longaxe");
        this.registerItem(Items.GLAMDRING, "glamdring");
        this.registerItem(Items.GLOINS_AXE, "gloins_axe");
        this.registerItem(Items.GLORFINDELS_SWORD, "glorfindels_sword");
        this.registerItem(Items.GUTHWINE, "guthwine");
        this.registerItem(Items.HADHAFANG, "hadhafang");
        this.registerItem(Items.LEGOLAS_SWORD, "legolas_sword");
        this.registerItem(Items.RADAGASTS_STAFF, "radagasts_staff");
        this.registerItem(Items.LURTZ_SWORD, "lurtz_sword");
        this.registerItem(Items.SAURONS_MACE, "saurons_mace");
        this.registerItem(Items.SARUMANS_STAFF, "sarumans_staff");
        this.registerItem(Items.YAZNEGS_AXE, "yaznegs_axe");
        this.registerItem(Items.DWARVEN_LORD_KNIFE, "dwarven_lord_knife");
        this.registerItem(Items.GONDORIAN_NOBLE_SWORD, "gondorian_noble_sword");
        this.registerItem(Items.MACE_OF_GLORY, "mace_of_glory");
        this.registerItem(Items.GOLDEN_WAR_HAMMER_OF_EREBOR, "golden_war_hammer_of_erebor");
        this.registerItem(Items.GONDORIAN_SPEAR, "gondorian_spear");
        this.registerItem(Items.ROHIRRIM_SPEAR, "rohirrim_spear");
        this.registerItem(Items.BULL_HEAD_MACE, "bull_head_mace");
        this.registerItem(Items.CORSAIR_EKET, "corsair_eket");
        this.registerItem(Items.DOL_GULDUR_SPIKED_MACE, "dol_guldur_spiked_mace");
        this.registerItem(Items.FELL_WARGRIDER_SWORD, "fell_wargrider_sword");
        this.registerItem(Items.HARADRIM_SNAKE_DAGGER, "haradrim_snake_dagger");
        this.registerItem(Items.NORTH_GOBLIN_MACE, "north_goblin_mace");
        this.registerItem(Items.NORTH_GOBLIN_SWORD, "north_goblin_sword");
        this.registerItem(Items.ORC_CAPTAIN_SWORD, "orc_captain_sword");
        this.registerItem(Items.ORC_DAGGER, "orc_dagger");
        this.registerItem(Items.ORC_HALBARD, "orc_halbard");
        this.registerItem(Items.URUK_SIEGE_TROOPER_DAGGER, "uruk_siege_trooper_dagger");
        this.registerItem(Items.IRON_MACE, "iron_mace");
        this.registerItem(Items.ORC_SWORD_3, "orc_sword_3");
        this.registerItem(Items.MORANNON_DAGGER, "morannon_dagger");
        this.registerItem(Items.ORC_SPEAR, "orc_spear");
        this.registerItem(Items.CASTLE_DOOR, "castle_door");
        this.registerItem(Items.DOL_GULDUR_PRISON_DOOR, "dol_guldur_prison_door");
        this.registerItem(Items.HILLMEN_DOOR, "hillmen_door");
        this.registerItem(Items.MORDOR_DOOR, "mordor_door");
        this.registerItem(Items.PRISON_DOOR, "prison_door");
        this.registerItem(Items.SINDAR_DOOR, "sindar_door");
        this.registerItem(Items.SOUTHLINCH_SEED, "southlinch_seed");
        this.registerItem(Items.GREEN_GRAPE_SEED, "green_grape_seed");
        this.registerItem(Items.GREEN_GRAPES, "green_grapes");
        this.registerItem(Items.PURPLE_GRAPE_SEED, "purple_grape_seed");
        this.registerItem(Items.PURPLE_GRAPES, "purple_grapes");
        this.registerItem(Items.PIPEWEED_SEED, "pipeweed_seed");
        this.registerItem(Items.PEAS, "peas");
        this.registerItem(Items.PEA_POD, "pea_pod");
        this.registerItem(Items.LEEK, "leek");
        this.registerItem(Items.LEEK_SEED, "leek_seed");
        this.registerItem(Items.ONION_SEED, "onion_seed");
        this.registerItem(Items.ONION, "onion");
        this.registerItem(Items.OLD_TOBY_SEED, "old_toby_seed");
        this.registerItem(Items.SOUTHERN_STAR_SEED, "southern_star_seed");
        this.registerItem(Items.STRAWBERRY_SEED, "strawberry_seed");
        this.registerItem(Items.ROHAN_BOOTS, "rohan_boots");
        this.registerItem(Items.ROHAN_LEGGINGS, "rohan_leggings");
        this.itemModelMesher.register(Items.RING, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
            	return new ModelResourceLocation(stack.getUnlocalizedName().substring(5).replace('.', '_'), "inventory");
            }
        });
        this.registerItem(Items.ELVEN_STEEL_INGOT, "elven_steel_ingot");
        // End Awaken Dreams code
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.itemModelMesher.rebuildCache();
    }
}
