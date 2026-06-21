package dev.methanium;

import dev.methanium.config.MethaniumConfig;
import dev.methanium.util.FrameCounter;
import net.minecraft.client.Minecraft;

/**
 * Dynamically raises or lowers render distance every N ticks to keep
 * the smoothed FPS near the user's configured target.
 *
 * Algorithm:
 *   • If FPS is consistently > targetFPS + HYSTERESIS  → try increasing distance (up to vanilla max)
 *   • If FPS is consistently < targetFPS - HYSTERESIS  → reduce distance by 1 chunk
 *   • Changes are gated by MIN_CHANGE_INTERVAL ticks to avoid thrashing
 */
public final class AdaptiveRenderDistanceManager {

    private static final int HYSTERESIS          = 5;   // FPS deadband around target
    private static final int MIN_CHANGE_INTERVAL = 100; // ticks (~5 sec) between changes
    private static final int MIN_RENDER_DIST     = 3;
    private static final int MAX_RENDER_DIST     = 32;

    private static int ticksSinceLastChange = 0;
    private static int originalRenderDist   = -1;       // saved on first run

    /** Called every client tick. */
    public static void tick(Minecraft mc, FrameCounter counter, MethaniumConfig config) {
        ticksSinceLastChange++;
        if (ticksSinceLastChange < MIN_CHANGE_INTERVAL) return;

        // Save the user's intended distance on first run
        if (originalRenderDist < 0) {
            originalRenderDist = mc.options.renderDistance().get();
        }

        float smoothFps = counter.getSmoothedFps();
        int   target    = config.getTargetFps();
        int   current   = mc.options.renderDistance().get();

        if (smoothFps < target - HYSTERESIS && current > MIN_RENDER_DIST) {
            // Performance pressure — reduce distance
            setRenderDistance(mc, current - 1);
            MethaniumMod.LOGGER.debug("[Methanium] FPS={} below target={}, reducing rd → {}",
                    (int) smoothFps, target, current - 1);
            ticksSinceLastChange = 0;

        } else if (smoothFps > target + HYSTERESIS && current < Math.min(originalRenderDist, MAX_RENDER_DIST)) {
            // GPU headroom — reclaim distance
            setRenderDistance(mc, current + 1);
            MethaniumMod.LOGGER.debug("[Methanium] FPS={} above target={}, increasing rd → {}",
                    (int) smoothFps, target, current + 1);
            ticksSinceLastChange = 0;
        }
    }

    private static void setRenderDistance(Minecraft mc, int dist) {
        mc.options.renderDistance().set(dist);
        mc.levelRenderer.allChanged(); // triggers chunk rebuild for new distance
    }
}
