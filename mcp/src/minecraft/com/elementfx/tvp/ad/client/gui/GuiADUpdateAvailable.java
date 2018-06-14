package com.elementfx.tvp.ad.client.gui;

import java.io.IOException;

import com.elementfx.tvp.ad.util.ADDefinitions;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiADUpdateAvailable extends GuiScreen
{
    private final String newVersion;

    public GuiADUpdateAvailable(String newVersion)
    {
        this.newVersion = newVersion;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        super.initGui();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 140, I18n.format("gui.toTitle", new Object[0])));
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.updateAvailable.title", new Object[0]), this.width / 2, 90, 16777215);
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.updateAvailable.message1", new Object[0]), this.width / 2, 110, 16777215);
        this.drawCenteredString(this.fontRendererObj, I18n.format("gui.updateAvailable.message2", ADDefinitions.version, newVersion), this.width / 2, 120, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        this.mc.displayGuiScreen((GuiScreen)null);
    }
}
