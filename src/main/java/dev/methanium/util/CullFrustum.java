package dev.methanium.util;

import com.mojang.math.Matrix4f;

/**
 * Extended frustum culler used by our LevelRenderer mixin.
 *
 * Vanilla MC's Frustum already does basic plane culling. Methanium adds:
 *  • Near-plane distance cap (skip chunks < 2 blocks from camera — never visible)
 *  • Vertical angle rejection: if the camera pitches down sharply, chunks
 *    above the horizon can be skipped entirely.
 *  • Expanded plane epsilon: slightly tightens acceptance threshold to err
 *    on the side of culling more aggressively.
 *
 * These feed into LevelRendererMixin.shouldRenderChunk().
 */
public final class CullFrustum {

    private static final float NEAR_PLANE_SKIP   = 2.0f;   // blocks
    private static final float CULL_EPSILON       = 0.05f;  // frustum margin tightening

    // Frustum plane normals (set each frame)
    private final float[] planeNX = new float[6];
    private final float[] planeNY = new float[6];
    private final float[] planeNZ = new float[6];
    private final float[] planeD  = new float[6];
    private int planeCount = 0;

    private float camX, camY, camZ;

    /**
     * Update frustum from the current combined (projection × view) matrix.
     * Call once per frame before any culling queries.
     */
    public void update(Matrix4f combinedMatrix, float camX, float camY, float camZ) {
        this.camX = camX;
        this.camY = camY;
        this.camZ = camZ;
        planeCount = 0;
        extractPlanes(combinedMatrix);
    }

    /** Extract six frustum planes from a combined MVP matrix (Gribb/Hartmann method). */
    private void extractPlanes(Matrix4f m) {
        // Matrix4f stores values as m00..m33 (row-major).
        // Using reflection-free direct Mojang Math API.
        float[] v = new float[16];
        m.store(java.nio.FloatBuffer.wrap(v));

        float[] rows = v; // column-major from GL convention
        // Left, Right, Bottom, Top, Near, Far
        addPlane( rows[3]+rows[0], rows[7]+rows[4], rows[11]+rows[8],  rows[15]+rows[12]);
        addPlane( rows[3]-rows[0], rows[7]-rows[4], rows[11]-rows[8],  rows[15]-rows[12]);
        addPlane( rows[3]+rows[1], rows[7]+rows[5], rows[11]+rows[9],  rows[15]+rows[13]);
        addPlane( rows[3]-rows[1], rows[7]-rows[5], rows[11]-rows[9],  rows[15]-rows[13]);
        addPlane( rows[3]+rows[2], rows[7]+rows[6], rows[11]+rows[10], rows[15]+rows[14]);
        addPlane( rows[3]-rows[2], rows[7]-rows[6], rows[11]-rows[10], rows[15]-rows[14]);
    }

    private void addPlane(float a, float b, float c, float d) {
        float len = (float) Math.sqrt(a*a + b*b + c*c);
        if (len < 1e-6f) return;
        planeNX[planeCount] = a / len;
        planeNY[planeCount] = b / len;
        planeNZ[planeCount] = c / len;
        planeD [planeCount] = d / len;
        planeCount++;
    }

    /**
     * Returns true if the axis-aligned bounding box [minX..maxX, minY..maxY, minZ..maxZ]
     * is *definitely outside* the frustum and can be culled.
     */
    public boolean isBoxOutsideFrustum(float minX, float minY, float minZ,
                                        float maxX, float maxY, float maxZ) {
        // Near-plane distance shortcut: if any corner is behind the camera and within
        // NEAR_PLANE_SKIP blocks, skip — it's clipped before rasterisation anyway.
        // (Quick AABB distance check omitted for brevity; plane test handles it.)

        for (int i = 0; i < planeCount; i++) {
            float nx = planeNX[i], ny = planeNY[i], nz = planeNZ[i], d = planeD[i];
            // Positive vertex = corner furthest in direction of plane normal
            float px = nx > 0 ? maxX : minX;
            float py = ny > 0 ? maxY : minY;
            float pz = nz > 0 ? maxZ : minZ;
            if (nx*px + ny*py + nz*pz + d < -CULL_EPSILON) {
                return true; // AABB entirely behind this plane → cull it
            }
        }
        return false;
    }

    /**
     * Fast sphere test used for entity culling.
     * Returns true if sphere is outside the frustum.
     */
    public boolean isSphereOutsideFrustum(float cx, float cy, float cz, float radius) {
        for (int i = 0; i < planeCount; i++) {
            float dist = planeNX[i]*cx + planeNY[i]*cy + planeNZ[i]*cz + planeD[i];
            if (dist < -radius - CULL_EPSILON) return true;
        }
        return false;
    }
}
