package org.valkyrienskies.wims.forge.mixin;

import journeymap.client.model.MapState;
import journeymap.client.properties.MiniMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.minimap.MiniMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.wims.forge.client.plugin.WIMSJourneyMapPlugin;

@Mixin(value = MiniMap.class)
public abstract class JourneyMapMiniMapMixin {

    @Final
    @Shadow(remap = false)
    private static GridRenderer gridRenderer;

    @Shadow(remap=false)
    private MiniMapProperties miniMapProperties;

    @Final
    @Shadow(remap=false)
    private static MapState state;

    @Inject(
            method = "drawMap(Lnet/minecraft/client/gui/GuiGraphics;Z)V",
            at = @At(
                    target = "Ljourneymap/client/ui/minimap/MiniMap;drawOnMapEntities(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource;D)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    public void drawMap(GuiGraphics graphics, boolean preview, CallbackInfo ci) {
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            var centerPoint = gridRenderer.getPixel(mc.player.getX(), mc.player.getZ());
            double x = centerPoint.x;
            double z = centerPoint.y;
            WIMSJourneyMapPlugin.onMinimapRender(graphics, (MiniMap) (Object) this, x, z, gridRenderer, miniMapProperties, state);
        }
    }
}
