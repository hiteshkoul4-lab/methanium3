package dev.methanium.mixin;

import dev.methanium.MethaniumMod;
import dev.methanium.util.VulkanDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into the Minecraft client startup to:
 *   • Enforce sensible defaults for low-end devices on first install.
 *   • Log combined system/render info for troubleshooting.
 *   • Warn if the user has vsync on (can mask FPS gains on Vulkan).
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Shadow @Final private Options options;

    @Inject(
        method = "<init>",
        at = @At(value = "RETURN")
    )
    private void methanium$onClientInit(CallbackInfo ci) {
        if (!VulkanDetector.isVulkan()) return; // Only apply Vulkan-specific hints

        // If maxFPS is unbounded (0) and framerateLimit is set to "Unlimited",
        // suggest a cap to avoid Vulkan swapchain recreation storms.
        int fpsLimit = options.framerateLimit().get();
        if (fpsLimit <= 0 || fpsLimit == 260 /* Minecraft's "Unlimited" sentinel */) {
            MethaniumMod.LOGGER.warn(
                "[Methanium] Unlimited FPS detected on Vulkan. " +
                "This can cause swapchain pressure and GPU throttling on low-end devices. " +
                "Consider setting a frame cap in Options → Video Settings."
            );
        }

        // If VSync is off: Vulkan will run at maximum rate, which can cause tearing
        // and thermal throttling on integrated GPUs.
        // (We never change settings without user consent; this is advisory only.)
        boolean vsync = options.enableVsync().get();
        if (!vsync) {
            MethaniumMod.LOGGER.info(
                "[Methanium] VSync is OFF. On integrated/low-end GPUs this may " +
                "cause thermal throttling. If you see stuttering, try enabling VSync."
            );
        }

        MethaniumMod.LOGGER.info("[Methanium] Vulkan startup checks complete.");
    }
}
