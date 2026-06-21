package dev.methanium.mixin;

import dev.methanium.MethaniumMod;
import dev.methanium.config.MethaniumConfig;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the top-level render loop for:
 *   1. Background throttle — cap FPS when the window is minimised/unfocused.
 *      On Vulkan, an uncapped background rate thrashes the GPU and wastes
 *      power on a frame nobody is seeing.
 *   2. Frame-start performance telemetry (TRACE level only).
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    private static long methanium$lastFrameNs = 0;

    @Inject(
        method = "render",
        at = @At("HEAD")
    )
    private void methanium$onRenderHead(float partialTick, long startTime, boolean tick,
                                         CallbackInfo ci) {

        MethaniumConfig cfg = MethaniumMod.getConfig();
        if (!cfg.isBackgroundThrottle()) return;

        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        boolean windowFocused = mc.isWindowActive();

        if (!windowFocused) {
            int fpsCap  = cfg.getBackgroundFpsCap();
            long frameNs = 1_000_000_000L / fpsCap;
            long now     = System.nanoTime();
            long elapsed = now - methanium$lastFrameNs;

            if (elapsed < frameNs) {
                try {
                    long sleepMs = (frameNs - elapsed) / 1_000_000;
                    if (sleepMs > 1) Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        methanium$lastFrameNs = System.nanoTime();
    }
}
