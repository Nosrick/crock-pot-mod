package com.github.nosrick.crockpot.mixin;

import com.github.nosrick.crockpot.client.tooltip.StewContentsTooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipComponent.class)
public interface TooltipComponentMixin extends TooltipComponent {

    @Inject(
            at = @At("HEAD"),
            method = "of(Lnet/minecraft/text/OrderedText;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;",
            cancellable = true
    )
    private static void ofReplacement(OrderedText text, CallbackInfoReturnable<TooltipComponent> info)
    {
        if (text instanceof StewContentsTooltip)
        {
            info.setReturnValue((StewContentsTooltip) text);
        }
    }
}
