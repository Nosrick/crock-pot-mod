package com.github.nosrick.crockpot.compat.wthit;

import com.mojang.blaze3d.systems.RenderSystem;
import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ItemRenderTooltipComponent implements ITooltipComponent {

    protected static ResourceManager RESOURCE_MANAGER;

    protected List<Item> contents;

    public ItemRenderTooltipComponent(List<Item> contents) {
        this.contents = contents;
    }

    @Override
    public int getWidth() {
        return this.contents.size() * 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float delta) {
        if(RESOURCE_MANAGER == null) {
            RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
        }

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        for(int i = 0; i < contents.size(); i++) {
            itemRenderer.renderGuiItemIcon(new ItemStack(this.contents.get(i)), x + (i * 8), y);
        }
    }
}
