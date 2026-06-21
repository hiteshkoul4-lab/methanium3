package dev.methanium;

import dev.methanium.config.MethaniumConfig;
import dev.methanium.util.FrameCounter;
import dev.methanium.util.VulkanDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Methanium — Vulkan Renderer Optimizer for Minecraft 26.2
 *
 * Philosophy: Sodium optimised OpenGL. Methanium targets Minecraft's new
 * experimental Vulkan backend, reducing per-frame CPU overhead and culling
 * work that never reaches the screen.
 *
 * Architecture:
 *  • Config  — user-tunable knobs (target FPS, culling aggressiveness, etc.)
 *  • Mixins  — hooks into MC's render pipeline at the Java boundary
 *  • Util    — frame counter, Vulkan detector, adaptive distance manager
 */
@Environment(EnvType.CLIENT)
public class MethaniumMod implements ClientModInitializer {

    public static final String MOD_ID = "methanium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Singleton instances shared across mixins
    private static MethaniumConfig config;
    private static FrameCounter   frameCounter;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Methanium] Initialising Vulkan optimiser for MC 26.2...");

        // Load or create config file from .minecraft/config/methanium.json
        config       = MethaniumConfig.loadOrCreate();
        frameCounter = new FrameCounter();

        // Detect which renderer backend is active and warn if on OpenGL
        VulkanDetector.detect();

        // Register per-tick logic: adaptive render distance, FPS tracking, etc.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            frameCounter.tick();

            if (config.isAdaptiveRenderDistanceEnabled()) {
                AdaptiveRenderDistanceManager.tick(client, frameCounter, config);
            }
        });

        LOGGER.info("[Methanium] Ready. Backend: {}  |  Target FPS: {}  |  Culling: {}",
                VulkanDetector.getBackendName(),
                config.getTargetFps(),
                config.isSmartCullingEnabled() ? "ENABLED" : "DISABLED");
    }

    // ── Static accessors used by Mixins ─────────────────────────────────────

    public static MethaniumConfig getConfig() {
        return config;
    }

    public static FrameCounter getFrameCounter() {
        return frameCounter;
    }
}
