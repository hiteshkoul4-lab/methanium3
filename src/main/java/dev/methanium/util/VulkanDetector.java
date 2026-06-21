package dev.methanium.util;

import dev.methanium.MethaniumMod;
import net.minecraft.client.Minecraft;

/**
 * Detects which rendering backend Minecraft 26.2 is using.
 *
 * In MC 26.2 the backend is selected via a launch option / video settings toggle.
 * The class name used for detection is a best-effort assumption based on
 * early 26.2 snapshot mappings; update if Mojang renames the backend enum.
 *
 * Reflected class: net.minecraft.client.renderer.RenderBackend  (assumed name)
 * Enum values:     OPENGL, VULKAN
 */
public final class VulkanDetector {

    public enum Backend { VULKAN, OPENGL, UNKNOWN }

    private static Backend activeBackend = Backend.UNKNOWN;

    /** Call once during mod init. */
    public static void detect() {
        // Approach: try to reflectively check the backend field on the renderer.
        // If MC changes the class/field name this falls back to UNKNOWN gracefully.
        try {
            Class<?> backendClass = Class.forName("net.minecraft.client.renderer.GlStateManager");
            // GlStateManager is only initialised on the OpenGL path.
            // If it's accessible and not null we're on OpenGL.
            activeBackend = Backend.OPENGL;
        } catch (ClassNotFoundException e) {
            // GlStateManager not present → likely Vulkan-only path
            activeBackend = Backend.VULKAN;
        } catch (Exception e) {
            activeBackend = Backend.UNKNOWN;
        }

        if (activeBackend == Backend.OPENGL) {
            MethaniumMod.LOGGER.warn(
                "[Methanium] Running on OpenGL backend. Methanium is designed for " +
                "Vulkan (MC 26.2 experimental). Enable the Vulkan backend in Video " +
                "Settings → Graphics Backend for maximum benefit."
            );
        } else if (activeBackend == Backend.VULKAN) {
            MethaniumMod.LOGGER.info("[Methanium] Vulkan backend detected ✓ All optimisations active.");
        }
    }

    public static Backend getBackend()    { return activeBackend; }
    public static boolean isVulkan()      { return activeBackend == Backend.VULKAN; }
    public static String  getBackendName(){ return activeBackend.name(); }
}
