package com.github.nosrick.crockpot.compat.wthit;

import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;

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
    public void render(DrawContext ctx, int x, int y, float delta) {
        if(RESOURCE_MANAGER == null) {
            RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
        }

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        for(int i = 0; i < contents.size(); i++) {
            ctx.drawItem(new ItemStack(this.contents.get(i)), x + (i * 8), y);
        }
    }
}
