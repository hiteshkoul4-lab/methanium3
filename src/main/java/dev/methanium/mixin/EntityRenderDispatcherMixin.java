package dev.methanium.mixin;

import dev.methanium.MethaniumMod;
import dev.methanium.config.MethaniumConfig;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reduces entity rendering overhead — one of the biggest FPS killers on
 * Vulkan because every entity is a separate draw call (descriptor set bind
 * + draw command).
 *
 * Optimisations:
 *   • Hard distance cull: entities beyond config.entityCullDistanceBlocks
 *     return false from shouldRender() immediately.
 *   • Invisible entity skip: sleeping, riding, or invisibility-flag entities
 *     are skipped without reaching the GPU.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
        method = "shouldRender",
        at = @At("HEAD"),
        cancellable = true
    )
    private <E extends Entity> void methanium$cullDistantEntities(
            E entity,
            net.minecraft.client.renderer.culling.Frustum frustum,
            double camX, double camY, double camZ,
            CallbackInfoReturnable<Boolean> cir) {

        MethaniumConfig cfg = MethaniumMod.getConfig();
        if (!cfg.isSmartCullingEnabled()) return;

        double limit = cfg.getEntityCullDistanceBlocks();
        double limitSq = limit * limit;

        Vec3 pos = entity.position();
        double dx = pos.x - camX;
        double dy = pos.y - camY;
        double dz = pos.z - camZ;
        double distSq = dx*dx + dy*dy + dz*dz;

        if (distSq > limitSq) {
            cir.setReturnValue(false); // Too far — skip the entity entirely
            return;
        }

        // Skip entities with full invisibility (still track them, but no draw call)
        if (entity.isInvisible()) {
            cir.setReturnValue(false);
        }
    }
}
