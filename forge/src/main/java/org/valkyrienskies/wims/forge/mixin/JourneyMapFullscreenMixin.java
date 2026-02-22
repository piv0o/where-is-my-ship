package org.valkyrienskies.wims.forge.mixin;

//import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.Mixin;
//import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.injection.At;
//import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.injection.Inject;
//import dev.architectury.patchedmixin.staticmixin.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import journeymap.client.properties.FullMapProperties;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.wims.WIMSMod;
import org.valkyrienskies.wims.forge.client.plugin.WIMSJourneyMapPlugin;

import java.awt.geom.Point2D;

@Mixin(value = Fullscreen.class)
public abstract class JourneyMapFullscreenMixin {
    @Final
    @Shadow(remap = false)
    private static GridRenderer gridRenderer;

    @Shadow(remap = false)
    private Boolean isScrolling;

    @Shadow(remap = false)
    protected abstract Point2D.Double getMouseDrag();

    @Shadow(remap = false)
    private FullMapProperties fullMapProperties;

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    target = "Ljourneymap/client/ui/fullscreen/Fullscreen;drawMap(Lnet/minecraft/client/gui/GuiGraphics;II)V",
                    value = "INVOKE",
                    shift = Shift.AFTER
            ),
            remap = false
    )
    public void JourneyMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float pt, CallbackInfo ci) {
        boolean dragging = isScrolling;
        Point2D.Double mouseDrag = getMouseDrag();
        double x = gridRenderer.getCenterBlockX() - (dragging ? mouseDrag.x : 0);
        double z = gridRenderer.getCenterBlockZ() - (dragging ? mouseDrag.y : 0);
        WIMSJourneyMapPlugin.OnRender(graphics, (Fullscreen) (Object) this, x, z, mouseX, mouseY, pt, fullMapProperties);
    }
}
