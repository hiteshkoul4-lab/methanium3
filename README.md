# Methanium 🔥
### Vulkan Renderer Optimizer for Minecraft 26.2 + Fabric

> *Sodium optimised OpenGL. Methanium optimises Vulkan.*

---

## What it does

Minecraft 26.2 introduced an **experimental Vulkan rendering backend** — a modern GPU API
that replaces the aging OpenGL renderer. Methanium squeezes maximum stable FPS out of that
backend, especially on low-end and integrated-GPU devices.

| Optimisation | What it does | FPS impact |
|---|---|---|
| **Adaptive Render Distance** | Lowers chunk draw distance when FPS drops, raises it back when stable | ★★★★ |
| **Smart Chunk Culling** | Skips chunks the camera mathematically cannot see | ★★★ |
| **Entity Distance Cull** | Entities beyond your config distance are skipped entirely | ★★★ |
| **Underground Sky Skip** | Doesn't render the sky when you can't see it | ★★ |
| **Particle Cap** | Limits simultaneous particles (each = a Vulkan draw call) | ★★ |
| **Front-to-Back Sort** | Draws opaque chunks F2B so Vulkan's early-Z discards more fragments | ★★ |
| **Background Throttle** | Caps FPS at 10 when window is minimised — stops wasting GPU power | ★ |
| **Fog Cache** | Skips fog colour recalculation when the camera hasn't moved | ★ |

---

## Requirements

- Minecraft **26.2**
- Fabric Loader **0.19.3** or higher
- Fabric API **0.152.0+26.2** or higher
- Java **25** (required by MC 26.2)
- A GPU with **Vulkan 1.2+** support (for full benefit)

> **Tip:** Enable the Vulkan backend in Video Settings → Graphics Backend.
> The mod works on OpenGL too but most optimisations target the Vulkan path.

---

## Install (players)

1. Install [Fabric Loader 0.19.3](https://fabricmc.net/use/installer/)
2. Download `methanium-1.0.0.jar`
3. Drop it into `.minecraft/mods/`
4. Launch Minecraft 26.2 with the Fabric profile

---

## Config

On first launch Methanium writes `.minecraft/config/methanium.json`.
Edit it with any text editor:

```json
{
  "targetFps": 60,
  "adaptiveRenderDistance": true,
  "smartCullingEnabled": true,
  "entityCullDistanceBlocks": 48,
  "maxParticles": 512,
  "skipUndergroundSky": true,
  "backgroundThrottle": true,
  "backgroundFpsCap": 10,
  "frontToBackChunkSorting": true,
  "chunkGeometryCache": true
}
```

**Recommended profiles:**

*Potato PC (iGPU / <4 GB RAM)*
```json
{ "targetFps": 30, "entityCullDistanceBlocks": 32, "maxParticles": 128, "adaptiveRenderDistance": true }
```

*Mid-range (GTX 1060 / RX 580)*
```json
{ "targetFps": 60, "entityCullDistanceBlocks": 64, "maxParticles": 512 }
```

*High-end (RTX 3070+ / RX 6700+)*
```json
{ "targetFps": 144, "adaptiveRenderDistance": false, "entityCullDistanceBlocks": 96, "maxParticles": 2048 }
```

---

## Build from source (developers)

### Prerequisites
- **Java 25 JDK** ([download here](https://adoptium.net/temurin/releases/?version=25))
- Git

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/your-name/methanium.git
cd methanium

# 2. (Windows) Download Gradle wrapper jar from:
#    https://services.gradle.org/distributions/gradle-9.5.1-wrapper.jar
#    Save it to:  gradle/wrapper/gradle-wrapper.jar

# 3. Build
./gradlew build          # Linux / Mac
gradlew.bat build        # Windows

# 4. Find the output jar at:
#    build/libs/methanium-1.0.0.jar
```

The first build takes 5–15 minutes — Gradle downloads Minecraft 26.2 and
applies Fabric's yarn mappings automatically.

### Getting the Gradle wrapper jar (required for fresh clones)
```
https://raw.githubusercontent.com/gradle/gradle/v9.5.1/gradle/wrapper/gradle-wrapper.jar
```
Download this and place it at `gradle/wrapper/gradle-wrapper.jar` before running `./gradlew build`.

---

## Compatibility

| Mod | Status |
|---|---|
| Sodium (if updated to 26.2) | ⚠️ Partial — Sodium targets OpenGL; Methanium targets Vulkan. Use one or the other depending on your backend. |
| VulkanMod | ❌ Incompatible — VulkanMod replaces the renderer entirely; Methanium optimises MC's native Vulkan backend. |
| Sulkan | ✅ Compatible — Sulkan adds shaders; Methanium reduces overhead. Stack them. |
| Iris / Oculus | ⚠️ Untested |
| Lithium | ✅ Compatible — Lithium targets game logic, Methanium targets rendering. |
| FerriteCore | ✅ Compatible — memory reduction stacks well with our culling. |
| EntityCulling | ⚠️ Partial overlap — both cull entities; disable Methanium's entity cull if using EntityCulling. |

---

## FAQ

**Q: Will this break vanilla gameplay?**  
A: No. All optimisations are rendering-side only. No game logic is altered.

**Q: Does it work on servers?**  
A: Client-side only. No server-side component, no permission needed.

**Q: Vulkan backend crashes for me. Is that Methanium?**  
A: Unlikely. The Vulkan backend in MC 26.2 is experimental. Try disabling the backend
   in Video Settings first to confirm. Check the log for `[Methanium]` lines for clues.

**Q: Can I use this with OptiFine?**  
A: OptiFine hasn't been updated for 26.2 as of writing. Not tested.

---

## License
MIT — do whatever you want with it.
