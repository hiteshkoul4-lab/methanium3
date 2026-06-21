package dev.methanium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.methanium.MethaniumMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All user-facing settings for Methanium.
 *
 * Config file location: .minecraft/config/methanium.json
 * Defaults are chosen to give good FPS gains on low-end hardware with
 * zero visual regression on mid-range hardware.
 */
public final class MethaniumConfig {

    // ── Fields ──────────────────────────────────────────────────────────────

    /** Target frame rate for adaptive systems. Default 60 FPS. */
    private int targetFps = 60;

    /**
     * Reduces render distance automatically when FPS drops below targetFps.
     * Restores distance when FPS recovers. Great for low-end devices.
     */
    private boolean adaptiveRenderDistance = true;

    /**
     * Enhanced frustum + cave culling.
     * Skips chunks/entities that are provably not visible to the camera.
     * Significant win in dense/underground scenes.
     */
    private boolean smartCullingEnabled = true;

    /**
     * Maximum entity render distance in blocks (per-axis, i.e. a cube, not sphere).
     * Entities further than this are skipped entirely.
     * Vanilla behaviour: entities skip rendering only at >64 blocks (hardcoded).
     * Methanium makes this configurable and tighter. Default 48 blocks.
     */
    private int entityCullDistanceBlocks = 48;

    /**
     * Cap on simultaneously rendered particles.
     * Vanilla can spawn hundreds; on Vulkan this creates many small draw calls.
     * Default 512. Set higher for powerful GPUs, lower for potato PCs.
     */
    private int maxParticles = 512;

    /**
     * Skip rendering the sky dome when the player is underground (y < 0 or
     * surrounded by opaque blocks). Saves a draw call + descriptor set bind.
     */
    private boolean skipUndergroundSky = true;

    /**
     * Reduce work (frame rate cap, particle cull) when the game window is
     * not focused / minimised. Saves battery and avoids background GPU thrash.
     */
    private boolean backgroundThrottle = true;

    /** Frame rate cap when backgroundThrottle kicks in. Default 10 FPS. */
    private int backgroundFpsCap = 10;

    /**
     * Try to sort opaque chunk draw calls front-to-back.
     * On Vulkan, early-Z rejection works best with F2B ordering, reducing
     * fragment shader invocations on overdraw-heavy scenes (caves, dense builds).
     */
    private boolean frontToBackChunkSorting = true;

    /**
     * Reuse compiled chunk geometry for N frames if the section hasn't changed.
     * Cuts CPU rebuild overhead significantly in mostly-static worlds.
     */
    private boolean chunkGeometryCache = true;

    // ── Serialisation ────────────────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "methanium.json";

    public static MethaniumConfig loadOrCreate() {
        Path cfgPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(cfgPath)) {
            try (Reader reader = Files.newBufferedReader(cfgPath)) {
                MethaniumConfig loaded = GSON.fromJson(reader, MethaniumConfig.class);
                MethaniumMod.LOGGER.info("[Methanium] Config loaded from {}", cfgPath);
                return loaded;
            } catch (IOException e) {
                MethaniumMod.LOGGER.warn("[Methanium] Failed to read config, using defaults.", e);
            }
        }
        // First run — create default config
        MethaniumConfig defaults = new MethaniumConfig();
        defaults.save(cfgPath);
        MethaniumMod.LOGGER.info("[Methanium] Default config written to {}", cfgPath);
        return defaults;
    }

    public void save(Path cfgPath) {
        try (Writer writer = Files.newBufferedWriter(cfgPath)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            MethaniumMod.LOGGER.error("[Methanium] Failed to save config.", e);
        }
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public int  getTargetFps()                    { return targetFps; }
    public boolean isAdaptiveRenderDistanceEnabled() { return adaptiveRenderDistance; }
    public boolean isSmartCullingEnabled()           { return smartCullingEnabled; }
    public int  getEntityCullDistanceBlocks()      { return entityCullDistanceBlocks; }
    public int  getMaxParticles()                  { return maxParticles; }
    public boolean isSkipUndergroundSky()           { return skipUndergroundSky; }
    public boolean isBackgroundThrottle()           { return backgroundThrottle; }
    public int  getBackgroundFpsCap()              { return backgroundFpsCap; }
    public boolean isFrontToBackChunkSorting()      { return frontToBackChunkSorting; }
    public boolean isChunkGeometryCache()           { return chunkGeometryCache; }
}
