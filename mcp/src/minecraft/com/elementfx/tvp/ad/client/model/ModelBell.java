package com.elementfx.tvp.ad.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBell extends ModelBase
{
    public ModelRenderer crown;
    public ModelRenderer shoulder0;
    public ModelRenderer shoulder45;
    public ModelRenderer shoulder90;
    public ModelRenderer shoulder135;
    public ModelRenderer shoulder180;
    public ModelRenderer shoulder225;
    public ModelRenderer shoulder270;
    public ModelRenderer shoulder315;
    public ModelRenderer rightHeadstock;
    public ModelRenderer leftHeadstock;
    public ModelRenderer waist0;
    public ModelRenderer waist45;
    public ModelRenderer waist90;
    public ModelRenderer waist135;
    public ModelRenderer waist180;
    public ModelRenderer waist225;
    public ModelRenderer waist270;
    public ModelRenderer waist315;
    public ModelRenderer soundBow0;
    public ModelRenderer soundBow45;
    public ModelRenderer soundBow90;
    public ModelRenderer shoundBow135;
    public ModelRenderer soundBow180;
    public ModelRenderer soundBow225;
    public ModelRenderer soundBow270;
    public ModelRenderer soundBow315;
    public ModelRenderer clapperRod;
    public ModelRenderer clapperBall;
    public ModelRenderer topHeadstock;

    public ModelBell()
    {
        this.textureWidth = 32;
        this.textureHeight = 32;
        this.waist315 = new ModelRenderer(this, 0, 0);
        this.waist315.setRotationPoint(-1.5F, 3.1F, 1.5F);
        this.waist315.addBox(-1.5F, 0.0F, 0.0F, 3, 1, 8, 0.0F);
        this.setRotateAngle(waist315, -1.3089969389957472F, -((float)Math.PI / 4F), 0.0F);
        this.shoulder270 = new ModelRenderer(this, 0, 0);
        this.shoulder270.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shoulder270.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder270, -0.5235987755982988F, -((float)Math.PI / 2F), 0.0F);
        this.shoulder180 = new ModelRenderer(this, 0, 0);
        this.shoulder180.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shoulder180.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder180, -0.5235987755982988F, (float)Math.PI, 0.0F);
        this.shoulder0 = new ModelRenderer(this, 0, 0);
        this.shoulder0.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shoulder0.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder0, -0.5235987755982988F, 0.0F, 0.0F);
        this.shoulder45 = new ModelRenderer(this, 0, 0);
        this.shoulder45.setRotationPoint(0.0F, 1.8F, 0.0F);
        this.shoulder45.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder45, -0.5235987755982988F, ((float)Math.PI / 4F), 0.0F);
        this.waist0 = new ModelRenderer(this, 0, 0);
        this.waist0.setRotationPoint(0.0F, 3.0F, 2.6F);
        this.waist0.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 8, 0.0F);
        this.setRotateAngle(waist0, -1.3089969389957472F, 0.0F, 0.0F);
        this.waist270 = new ModelRenderer(this, 0, 0);
        this.waist270.setRotationPoint(-2.6F, 3.0F, 0.0F);
        this.waist270.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 8, 0.0F);
        this.setRotateAngle(waist270, -1.3089969389957472F, -((float)Math.PI / 2F), 0.0F);
        this.clapperRod = new ModelRenderer(this, 0, 0);
        this.clapperRod.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.clapperRod.addBox(-0.5F, 2.0F, -0.5F, 1, 9, 1, 0.0F);
        this.setRotateAngle(clapperRod, 0.0F, ((float)Math.PI / 2F), 0.0F);
        this.waist135 = new ModelRenderer(this, 0, 0);
        this.waist135.setRotationPoint(1.5F, 3.1F, -1.5F);
        this.waist135.addBox(-1.5F, 0.0F, 0.0F, 3, 1, 8, 0.0F);
        this.setRotateAngle(waist135, -1.3089969389957472F, 2.356194490192345F, 0.0F);
        this.clapperBall = new ModelRenderer(this, 0, 0);
        this.clapperBall.setRotationPoint(0.0F, 9.5F, 0.0F);
        this.clapperBall.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
        this.setRotateAngle(clapperBall, 0.0F, ((float)Math.PI / 2F), 0.0F);
        this.shoulder135 = new ModelRenderer(this, 0, 0);
        this.shoulder135.setRotationPoint(0.0F, 1.8F, 0.0F);
        this.shoulder135.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder135, -0.5235987755982988F, 2.356194490192345F, 0.0F);
        this.soundBow315 = new ModelRenderer(this, 0, 0);
        this.soundBow315.setRotationPoint(-3.0F, 9.7F, 3.0F);
        this.soundBow315.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow315, -1.3089969389957472F, -((float)Math.PI / 4F), 0.0F);
        this.shoulder225 = new ModelRenderer(this, 0, 0);
        this.shoulder225.setRotationPoint(0.0F, 1.8F, 0.0F);
        this.shoulder225.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder225, -0.5235987755982988F, -2.356194490192345F, 0.0F);
        this.topHeadstock = new ModelRenderer(this, 0, 0);
        this.topHeadstock.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.topHeadstock.addBox(-2.0F, -0.5F, -1.0F, 4, 1, 2, 0.01F);
        this.shoulder90 = new ModelRenderer(this, 0, 0);
        this.shoulder90.setRotationPoint(0.0F, 1.5F, 0.0F);
        this.shoulder90.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder90, -0.5235987755982988F, ((float)Math.PI / 2F), 0.0F);
        this.shoulder315 = new ModelRenderer(this, 0, 0);
        this.shoulder315.setRotationPoint(0.0F, 1.8F, 0.0F);
        this.shoulder315.addBox(-1.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.setRotateAngle(shoulder315, -0.5235987755982988F, -((float)Math.PI / 4F), 0.0F);
        this.shoundBow135 = new ModelRenderer(this, 0, 0);
        this.shoundBow135.setRotationPoint(3.0F, 9.7F, -3.0F);
        this.shoundBow135.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(shoundBow135, -1.3089969389957472F, 2.356194490192345F, 0.0F);
        this.waist45 = new ModelRenderer(this, 0, 0);
        this.waist45.setRotationPoint(1.5F, 3.1F, 1.5F);
        this.waist45.addBox(-1.5F, 0.0F, 0.0F, 3, 1, 8, 0.0F);
        this.setRotateAngle(waist45, -1.3089969389957472F, ((float)Math.PI / 4F), 0.0F);
        this.crown = new ModelRenderer(this, 0, 0);
        this.crown.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.crown.addBox(-1.0F, 1.5F, -1.0F, 2, 1, 2, 0.01F);
        this.waist225 = new ModelRenderer(this, 0, 0);
        this.waist225.setRotationPoint(-1.5F, 3.1F, -1.5F);
        this.waist225.addBox(-1.5F, 0.0F, 0.0F, 3, 1, 8, 0.0F);
        this.setRotateAngle(waist225, -1.3089969389957472F, -2.356194490192345F, 0.0F);
        this.waist180 = new ModelRenderer(this, 0, 0);
        this.waist180.setRotationPoint(0.0F, 3.0F, -2.6F);
        this.waist180.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 8, 0.0F);
        this.setRotateAngle(waist180, -1.3089969389957472F, (float)Math.PI, 0.0F);
        this.soundBow270 = new ModelRenderer(this, 0, 0);
        this.soundBow270.setRotationPoint(-4.4F, 9.7F, 0.0F);
        this.soundBow270.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow270, -1.0471975511965976F, -((float)Math.PI / 2F), 0.0F);
        this.soundBow180 = new ModelRenderer(this, 0, 0);
        this.soundBow180.setRotationPoint(0.0F, 9.7F, -4.4F);
        this.soundBow180.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow180, -1.0471975511965976F, (float)Math.PI, 0.0F);
        this.waist90 = new ModelRenderer(this, 0, 0);
        this.waist90.setRotationPoint(2.6F, 3.0F, 0.0F);
        this.waist90.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 8, 0.0F);
        this.setRotateAngle(waist90, -1.3089969389957472F, ((float)Math.PI / 2F), 0.0F);
        this.soundBow90 = new ModelRenderer(this, 0, 0);
        this.soundBow90.setRotationPoint(4.4F, 9.7F, 0.0F);
        this.soundBow90.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow90, -1.0471975511965976F, ((float)Math.PI / 2F), 0.0F);
        this.rightHeadstock = new ModelRenderer(this, 0, 0);
        this.rightHeadstock.setRotationPoint(0.0F, 1.2F, 0.0F);
        this.rightHeadstock.addBox(-1.5F, -0.5F, -1.0F, 2, 1, 2, 0.0F);
        this.setRotateAngle(rightHeadstock, 0.0F, 0.0F, 1.9198621771937625F);
        this.leftHeadstock = new ModelRenderer(this, 0, 0);
        this.leftHeadstock.setRotationPoint(0.0F, 1.2F, 0.0F);
        this.leftHeadstock.addBox(-1.5F, -0.5F, -1.0F, 2, 1, 2, 0.0F);
        this.setRotateAngle(leftHeadstock, 0.0F, 0.0F, 1.2217304763960306F);
        this.soundBow0 = new ModelRenderer(this, 0, 0);
        this.soundBow0.setRotationPoint(0.0F, 9.7F, 4.4F);
        this.soundBow0.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow0, -1.0471975511965976F, 0.0F, 0.0F);
        this.soundBow45 = new ModelRenderer(this, 0, 0);
        this.soundBow45.setRotationPoint(3.0F, 9.7F, 3.0F);
        this.soundBow45.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow45, -1.3089969389957472F, ((float)Math.PI / 4F), 0.0F);
        this.soundBow225 = new ModelRenderer(this, 0, 0);
        this.soundBow225.setRotationPoint(-3.0F, 9.7F, -3.0F);
        this.soundBow225.addBox(-2.0F, 0.0F, 0.0F, 4, 1, 2, 0.0F);
        this.setRotateAngle(soundBow225, -1.3089969389957472F, -2.356194490192345F, 0.0F);
        this.crown.addChild(this.waist315);
        this.crown.addChild(this.shoulder270);
        this.crown.addChild(this.shoulder180);
        this.crown.addChild(this.shoulder0);
        this.crown.addChild(this.shoulder45);
        this.crown.addChild(this.waist0);
        this.crown.addChild(this.waist270);
        this.crown.addChild(this.waist135);
        this.clapperRod.addChild(this.clapperBall);
        this.crown.addChild(this.shoulder135);
        this.crown.addChild(this.soundBow315);
        this.crown.addChild(this.shoulder225);
        this.crown.addChild(this.shoulder90);
        this.crown.addChild(this.shoulder315);
        this.crown.addChild(this.shoundBow135);
        this.crown.addChild(this.waist45);
        this.crown.addChild(this.waist225);
        this.crown.addChild(this.waist180);
        this.crown.addChild(this.soundBow270);
        this.crown.addChild(this.soundBow180);
        this.crown.addChild(this.waist90);
        this.crown.addChild(this.soundBow90);
        this.crown.addChild(this.rightHeadstock);
        this.crown.addChild(this.leftHeadstock);
        this.crown.addChild(this.soundBow0);
        this.crown.addChild(this.soundBow45);
        this.crown.addChild(this.soundBow225);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        this.clapperRod.render(f5);
        this.topHeadstock.render(f5);
        this.crown.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
