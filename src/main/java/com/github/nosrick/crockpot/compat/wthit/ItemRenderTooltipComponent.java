package com.github.nosrick.crockpot.compat.wthit;

import com.mojang.blaze3d.systems.RenderSystem;
import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class ItemRenderTooltipComponent implements ITooltipComponent {

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
        for(int i = 0; i < contents.size(); i++) {
            Identifier id = Registry.ITEM.getId(contents.get(i));
            String content = "textures/item/" + id.getPath();
            content = content.replace('.', '/').concat(".png");
            RenderSystem.setShaderTexture(0, new Identifier(id.getNamespace(), content));
            DrawableHelper.drawTexture(matrices, x + (i * 8), y, 0, 0, 16, 16, 16, 16);
        }
    }
}
