package com.elementfx.tvp.ad.client.resources;

import com.elementfx.tvp.ad.util.ADResourceLocation;
import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class AwakenDreamsResourcePack implements IResourcePack
{
    public static final Set<String> DEFAULT_RESOURCE_DOMAINS = ImmutableSet.<String>of("awakendreams");
    private final ResourceIndex resourceIndex;

    public AwakenDreamsResourcePack(ResourceIndex resourceIndexIn)
    {
        this.resourceIndex = resourceIndexIn;
    }

    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        InputStream inputstream = this.getResourceStream(location);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            InputStream inputstream1 = this.getInputStreamAssets(location);

            if (inputstream1 != null)
            {
                return inputstream1;
            }
            else
            {
                throw new FileNotFoundException(location.getResourcePath());
            }
        }
    }

    @Nullable
    public InputStream getInputStreamAssets(ResourceLocation location) throws IOException, FileNotFoundException
    {
        File file1 = this.resourceIndex.getFile(location);
        return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
    }

    private InputStream getResourceStream(ResourceLocation location)
    {
        return AwakenDreamsResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
    }

    public boolean resourceExists(ResourceLocation location)
    {
        return this.getResourceStream(location) != null || this.resourceIndex.isFileExisting(location);
    }

    public Set<String> getResourceDomains()
    {
        return DEFAULT_RESOURCE_DOMAINS;
    }

    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException
    {
        try
        {
//        	ResourceLocation location = new ResourceLocation("awakendreams:pack.mcmeta");
            //InputStream inputstream = new FileInputStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
        		InputStream inputstream = this.getResourceStream(new ADResourceLocation("pack.mcmeta"));
            return AbstractResourcePack.readMetadata(metadataSerializer, inputstream, metadataSectionName);
        }
        catch (RuntimeException var4)
        {
            return (T)null;
        }
        /*catch (FileNotFoundException var5)
        {
            return (T)null;
        }*/
    }

    public BufferedImage getPackImage() throws IOException
    {
        return TextureUtil.readBufferedImage(this.getResourceStream(new ADResourceLocation("textures/misc/adpack.png")));
    }

    public String getPackName()
    {
        return "Awaken Dreams Default";
    }
}
