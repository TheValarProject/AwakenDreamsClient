package com.elementfx.tvp.ad.client.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.resources.ResourcePackListEntryServer;

public class ResourcePackListEntryAwakenDreams extends ResourcePackListEntryServer
{
    public ResourcePackListEntryAwakenDreams(GuiScreenResourcePacks resourcePacksGUIIn)
    {
        super(resourcePacksGUIIn, Minecraft.getMinecraft().getResourcePackRepository().rprAwakenDreamsResourcePack);
    }

    protected String getResourcePackName()
    {
        return "Awaken Dreams Default";
    }

    public boolean isServerPack()
    {
        return false;
    }
}
