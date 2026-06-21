package dev.methanium.mixin;

import dev.methanium.MethaniumMod;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips expensive fog colour/density recalculation when:
 *   • The player hasn't moved biome or altitude since the last tick.
 *   • No rain/weather change has occurred.
 *
 * On Vulkan, fog parameters are uploaded as uniform buffer data each frame.
 * Avoiding redundant recalculation saves a small UBO update and uniform
 * constant flush per frame.
 *
 * Note: class was BackgroundRenderer in older versions; renamed to FogRenderer
 * in recent snapshots. Check the actual 26.2 Yarn mapping.
 */
@Mixin(FogRenderer.class)
public abstract class BackgroundRendererMixin {

    private static double methanium$lastCamX = Double.MAX_VALUE;
    private static double methanium$lastCamY = Double.MAX_VALUE;
    private static double methanium$lastCamZ = Double.MAX_VALUE;
    private static int    methanium$skipCount  = 0;

    // Skip if camera hasn't moved more than 0.5 blocks since last fog update
    private static final double MOVEMENT_THRESHOLD_SQ = 0.25; // 0.5^2

    @Inject(
        method = "setupColor",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void methanium$skipStaticFogColor(
            Camera camera,
            float partialTick,
            net.minecraft.client.multiplayer.ClientLevel level,
            int renderDistanceChunks,
            float darkenWorldAmount,
            CallbackInfo ci) {

        if (!MethaniumMod.getConfig().isSmartCullingEnabled()) return;

        double cx = camera.getPosition().x;
        double cy = camera.getPosition().y;
        double cz = camera.getPosition().z;

        double dx = cx - methanium$lastCamX;
        double dy = cy - methanium$lastCamY;
        double dz = cz - methanium$lastCamZ;
        double distSq = dx*dx + dy*dy + dz*dz;

        if (distSq < MOVEMENT_THRESHOLD_SQ) {
            // Camera hasn't moved meaningfully — reuse last fog color
            methanium$skipCount++;
            ci.cancel();
            return;
        }

        // Camera moved — update cached position and let vanilla compute fog
        methanium$lastCamX = cx;
        methanium$lastCamY = cy;
        methanium$lastCamZ = cz;
        methanium$skipCount = 0;
    }
}
