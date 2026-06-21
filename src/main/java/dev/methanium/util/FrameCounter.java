package dev.methanium.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Rolling-window FPS tracker.
 *
 * Records the timestamp of each tick and computes a smoothed FPS over
 * the last WINDOW_SIZE ticks. Called from the client tick event (~20/sec)
 * and from the render loop for sub-tick precision.
 */
public final class FrameCounter {

    private static final int WINDOW_SIZE = 60; // ~3 seconds of ticks

    private final Deque<Long> timestamps = new ArrayDeque<>(WINDOW_SIZE + 1);
    private float smoothedFps = 60f;

    /**
     * Record a tick timestamp and recompute smoothed FPS.
     * Should be called every client tick.
     */
    public void tick() {
        long now = System.nanoTime();
        timestamps.addLast(now);

        if (timestamps.size() > WINDOW_SIZE) {
            timestamps.removeFirst();
        }

        if (timestamps.size() >= 2) {
            long oldest  = timestamps.peekFirst();
            long elapsed = now - oldest;                     // nanoseconds
            float seconds = elapsed / 1_000_000_000f;
            float rawFps  = (timestamps.size() - 1) / seconds;
            // Exponential moving average for stability
            smoothedFps = smoothedFps * 0.85f + rawFps * 0.15f;
        }
    }

    /** Returns EMA-smoothed FPS (updates ~20x/sec). */
    public float getSmoothedFps() {
        return smoothedFps;
    }

    /** Raw integer FPS, good for display. */
    public int getFpsInt() {
        return Math.round(smoothedFps);
    }
}
