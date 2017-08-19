package com.elementfx.tvp.ad.util;

import org.apache.commons.lang3.Validate;

import net.minecraft.util.ResourceLocation;

public class ADResourceLocation extends ResourceLocation
{
    public ADResourceLocation(String resourceName)
    {
        super(0, new String[] {"awakendreams", resourceName});
    }
}
