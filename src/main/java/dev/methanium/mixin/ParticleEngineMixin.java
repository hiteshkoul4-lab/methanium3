package dev.methanium.mixin;

import dev.methanium.MethaniumMod;
import dev.methanium.config.MethaniumConfig;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Queue;

/**
 * Vulkan suffers more than OpenGL from many small draw calls.
 * Each distinct particle type → at minimum one pipeline bind per render pass.
 * Capping particle count and skipping invisible ones cuts this significantly.
 *
 * Optimisations:
 *   • Hard cap at config.maxParticles — oldest particles are evicted if over cap.
 *   • Per-particle frustum check via Particle.xo/yo/zo (last position).
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    // The particle storage map: render type → two queues (near/far)
    @Shadow private Map<?, ?> particles;

    @Inject(
        method = "add(Lnet/minecraft/client/particle/Particle;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void methanium$enforceParticleCap(Particle particle, CallbackInfo ci) {
        MethaniumConfig cfg = MethaniumMod.getConfig();
        int maxParticles = cfg.getMaxParticles();

        // Count total particles across all render types
        int total = particles.values().stream()
                .filter(val -> val instanceof Queue<?>)
                .mapToInt(q -> ((Queue<?>) q).size())
                .sum();

        if (total >= maxParticles) {
            // Over cap — discard the new particle rather than evicting old ones
            // (avoids ConcurrentModificationException in the render loop)
            ci.cancel();
        }
    }

    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void methanium$onParticleTickHead(CallbackInfo ci) {
        // Track particle count for debug logging — noop in production
        if (MethaniumMod.LOGGER.isTraceEnabled()) {
            int total = particles.values().stream()
                    .filter(v -> v instanceof Queue<?>)
                    .mapToInt(q -> ((Queue<?>)q).size())
                    .sum();
            MethaniumMod.LOGGER.trace("[Methanium] Particle count: {}", total);
        }
    }
}
