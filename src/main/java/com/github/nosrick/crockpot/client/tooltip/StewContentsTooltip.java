package com.github.nosrick.crockpot.client.tooltip;

import com.github.nosrick.crockpot.item.StewItem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;
import net.minecraft.util.Language;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class StewContentsTooltip implements Text, OrderedText, TooltipComponent {

    protected List<Item> contents;
    protected ItemStack stewStack;
    protected MutableText contentsString;
    protected Language language;
    protected OrderedText ordered;

    StewContentsTooltip(ItemStack stack) {
        this.stewStack = stack;
        this.contents = StewItem.getContents(stack);
        this.contentsString = Text.translatable("tooltip.crockpot.contents");
    }

    public static StewContentsTooltip of(ItemStack stack) {
        return new StewContentsTooltip(stack);
    }

    @Override
    public Style getStyle()
    {
        return Style.EMPTY;
    }

    @Override
    public TextContent getContent()
    {
        return TextContent.EMPTY;
    }

    static List<Text> emptySiblings = new ArrayList<>();

    @Override
    public List<Text> getSiblings()
    {
        return emptySiblings;
    }

    @Override
    public OrderedText asOrderedText()
    {
        Language language = Language.getInstance();
        if (this.language != language) {
            this.ordered = language.reorder(this);
            this.language = language;
        }

        return this;
    }

    @Override
    public int getHeight() {
        return 19;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return this.contents.size() * 8;
    }

    @Override
    public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, VertexConsumerProvider.Immediate vertexConsumers) {
        textRenderer.draw(
                this.contentsString,
                (float)x,
                (float)y,
                -1,
                true,
                matrix,
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
        TooltipComponent.super.drawItems(textRenderer, x, y, matrices, itemRenderer, z);
        for(int i = 0; i < contents.size(); i++) {
            itemRenderer.renderGuiItemIcon(
                    matrices,
                    this.contents.get(i).getDefaultStack(),
                    x + textRenderer.getWidth(this.contentsString) + (i * 8),
                    y);
        }
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return TextVisitFactory.visitFormatted(this, getStyle(), visitor);
    }
}
