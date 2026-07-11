package com.nnpg.glazed.modules.esp;

import com.nnpg.glazed.GlazedAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * LightESP — phát hiện nguồn sáng bao gồm tầng âm (Y < 0)
 *
 * Vấn đề với API cũ:
 *   mc.world.getLightLevel(LightType.BLOCK, pos) gọi qua WorldView
 *   → nội bộ dùng section index = (y - bottomY) >> 4
 *   → nếu world chưa load đủ section hoặc LightingProvider bị giới hạn
 *     bởi server-side plugin thì trả về 0 dù thực ra có ánh sáng.
 *
 * Giải pháp:
 *   1. Query trực tiếp qua LightingProvider của client world.
 *   2. Đọc luminance từ BlockState (block tự phát sáng không cần light query).
 *   3. Dùng mutable BlockPos.Mutable để tránh tạo triệu object mỗi scan.
 *   4. Scan chạy mỗi N ticks thay vì mỗi frame.
 */
public class LightESP extends Module {

    // ── Settings groups ──────────────────────────────────────────────────────

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilter  = settings.createGroup("Filters");
    private final SettingGroup sgRender  = settings.createGroup("Render");

    // General
    private final Setting<Integer> chunkRadius = sgGeneral.add(new IntSetting.Builder()
        .name("chunk-radius")
        .description("Bán kính chunk để quét quanh người chơi.")
        .defaultValue(4).min(1).max(16).sliderMax(16)
        .build());

    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("min-y")
        .description("Y tối thiểu để quét (âm để thấy tầng dưới 0).")
        .defaultValue(-64).min(-64).max(319).sliderMin(-64).sliderMax(319)
        .build());

    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("max-y")
        .description("Y tối đa để quét.")
        .defaultValue(100).min(-64).max(319).sliderMin(-64).sliderMax(319)
        .build());

    private final Setting<Integer> minLuminance = sgGeneral.add(new IntSetting.Builder()
        .name("min-luminance")
        .description("Luminance tối thiểu của block để hiển thị (0-15).")
        .defaultValue(5).min(0).max(15).sliderMax(15)
        .build());

    private final Setting<Integer> scanTickInterval = sgGeneral.add(new IntSetting.Builder()
        .name("scan-interval-ticks")
        .description("Số tick giữa mỗi lần quét. Cao hơn = ít lag hơn.")
        .defaultValue(10).min(1).max(60).sliderMax(40)
        .build());

    private final Setting<Boolean> distanceLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("distance-limit")
        .description("Giới hạn khoảng cách render.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> maxDistance = sgGeneral.add(new IntSetting.Builder()
        .name("max-distance")
        .description("Khoảng cách tối đa để render (blocks).")
        .defaultValue(128).min(16).max(512).sliderMax(256)
        .visible(distanceLimit::get)
        .build());

    // Filters
    private final Setting<Boolean> filterLava = sgFilter.add(new BoolSetting.Builder()
        .name("filter-lava")
        .description("Ẩn lava và magma (nguồn sáng tự nhiên).")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showTorches = sgFilter.add(new BoolSetting.Builder()
        .name("show-torches")
        .description("Hiện đuốc, đèn lồng.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showGlowstone = sgFilter.add(new BoolSetting.Builder()
        .name("show-glowstone")
        .description("Hiện glowstone, sea lantern, shroomlight.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showRedstone = sgFilter.add(new BoolSetting.Builder()
        .name("show-redstone")
        .description("Hiện redstone lamp, redstone torch.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showBeacons = sgFilter.add(new BoolSetting.Builder()
        .name("show-beacons")
        .description("Hiện beacon, conduit.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> showAmethyst = sgFilter.add(new BoolSetting.Builder()
        .name("show-amethyst")
        .description("Hiện amethyst cluster (luminance 5 — hữu ích tìm geode).")
        .defaultValue(true)
        .build());

    // Render
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build());

    private final Setting<Boolean> thermalColors = sgRender.add(new BoolSetting.Builder()
        .name("thermal-colors")
        .description("Màu theo luminance: xanh (yếu) → vàng → đỏ (mạnh).")
        .defaultValue(true)
        .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .defaultValue(new SettingColor(255, 255, 0, 60))
        .visible(() -> !thermalColors.get())
        .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .defaultValue(new SettingColor(255, 255, 0, 255))
        .visible(() -> !thermalColors.get())
        .build());

    // ── State ────────────────────────────────────────────────────────────────

    // key = packed BlockPos long, value = luminance
    private final Map<Long, Integer> lightCache = new ConcurrentHashMap<>();
    private int ticksSinceScan = 0;

    public LightESP() {
        super(GlazedAddon.esp, "light-esp", "Phát hiện nguồn sáng kể cả tầng âm Y<0");
    }

    @Override
    public void onActivate() {
        lightCache.clear();
        ticksSinceScan = 0;
    }

    @Override
    public void onDeactivate() {
        lightCache.clear();
    }

    // ── Tick: scan định kỳ ───────────────────────────────────────────────────

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.world == null || mc.player == null) return;
        ticksSinceScan++;
        if (ticksSinceScan >= scanTickInterval.get()) {
            ticksSinceScan = 0;
            scanForLights();
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null || lightCache.isEmpty()) return;

        for (Map.Entry<Long, Integer> entry : lightCache.entrySet()) {
            BlockPos pos = BlockPos.fromLong(entry.getKey());
            int lum = entry.getValue();

            SettingColor sc, lc;
            if (thermalColors.get()) {
                float[] t = thermalColor(lum);
                sc = new SettingColor((int)(t[0]*255), (int)(t[1]*255), (int)(t[2]*255), (int)(t[3]*255));
                lc = new SettingColor((int)(t[0]*255), (int)(t[1]*255), (int)(t[2]*255), 255);
            } else {
                sc = sideColor.get();
                lc = lineColor.get();
            }
            event.renderer.box(pos, sc, lc, shapeMode.get(), 0);
        }
    }

    // ── Scan logic ────────────────────────────────────────────────────────────

    private void scanForLights() {
        if (mc.world == null || mc.player == null) return;

        ChunkPos center = mc.player.getChunkPos();
        int radius = chunkRadius.get();
        int yMin = minY.get();
        int yMax = maxY.get();
        double maxDistSq = distanceLimit.get()
            ? (double) maxDistance.get() * maxDistance.get()
            : Double.MAX_VALUE;

        // LightingProvider — đây là cách đúng để query light ở tầng âm
        LightingProvider lightProvider = mc.world.getLightingProvider();

        Map<Long, Integer> newCache = new HashMap<>();

        // Mutable pos để tránh tạo triệu object BlockPos
        BlockPos.Mutable mPos = new BlockPos.Mutable();

        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {

                Chunk chunk = mc.world.getChunk(cx, cz);
                if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.FULL)) continue;

                int startX = cx << 4; // cx * 16
                int startZ = cz << 4;

                // Duyệt từng section trong phạm vi Y
                // Section = khối 16x16x16, section index tính từ world bottom
                int bottomY       = mc.world.getBottomY();       // thường -64
                int sectionBottom = Math.floorDiv(yMin, 16);
                int sectionTop    = Math.floorDiv(yMax, 16);

                ChunkSection[] sections = chunk.getSectionArray();

                for (int si = sectionBottom; si <= sectionTop; si++) {
                    // Chuyển section coord sang array index
                    int arrIdx = si - Math.floorDiv(bottomY, 16);
                    if (arrIdx < 0 || arrIdx >= sections.length) continue;

                    ChunkSection section = sections[arrIdx];
                    // Bỏ qua section rỗng (không có block nào phát sáng)
                    if (section == null || section.isEmpty()) continue;

                    int baseY = si << 4; // Y của block đầu tiên trong section

                    for (int lx = 0; lx < 16; lx++) {
                        for (int lz = 0; lz < 16; lz++) {
                            for (int ly = 0; ly < 16; ly++) {
                                int worldY = baseY + ly;
                                if (worldY < yMin || worldY > yMax) continue;

                                int worldX = startX + lx;
                                int worldZ = startZ + lz;

                                mPos.set(worldX, worldY, worldZ);

                                // Distance check
                                if (distanceLimit.get()) {
                                    double dx = worldX - mc.player.getX();
                                    double dy = worldY - mc.player.getY();
                                    double dz = worldZ - mc.player.getZ();
                                    if (dx*dx + dy*dy + dz*dz > maxDistSq) continue;
                                }

                                // Lấy block state từ section (không cần world lookup)
                                BlockState state = section.getBlockState(lx, ly, lz);
                                int luminance = state.getLuminance();

                                if (luminance < minLuminance.get()) continue;

                                Block block = state.getBlock();
                                if (!passesFilter(block)) continue;

                                // Xác nhận lại qua LightingProvider cho tầng âm
                                // (một số server plugin can thiệp vào WorldView
                                //  nhưng không can thiệp được LightingProvider client)
                                int blockLight = getBlockLight(lightProvider, mPos);

                                // Dùng max của luminance và blockLight
                                // vì khi Y âm đôi khi một trong hai bị block
                                int effectiveLight = Math.max(luminance, blockLight);
                                if (effectiveLight < minLuminance.get()) continue;

                                newCache.put(mPos.asLong(), effectiveLight);
                            }
                        }
                    }
                }
            }
        }

        lightCache.clear();
        lightCache.putAll(newCache);
    }

    /**
     * Query block light trực tiếp từ LightingProvider.
     * Đây là cách bypass giới hạn của WorldView ở tầng âm.
     */
    private int getBlockLight(LightingProvider provider, BlockPos pos) {
        try {
            // method_15562 = get(LightType), method_15544 = getChunkLightProvider
            // field_9282 = BLOCK
            return provider.get(LightType.BLOCK).getLightLevel(pos);
        } catch (Exception e) {
            // Fallback nếu provider không support vị trí này
            return 0;
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    private boolean passesFilter(Block block) {
        // Lava / magma — nguồn sáng tự nhiên
        if (filterLava.get()) {
            if (block == Blocks.LAVA
             || block == Blocks.MAGMA_BLOCK
             || block == Blocks.FIRE
             || block == Blocks.SOUL_FIRE) return false;
        }

        // Đuốc và đèn lồng
        if (!showTorches.get()) {
            if (block == Blocks.TORCH
             || block == Blocks.WALL_TORCH
             || block == Blocks.SOUL_TORCH
             || block == Blocks.SOUL_WALL_TORCH
             || block == Blocks.LANTERN
             || block == Blocks.SOUL_LANTERN) return false;
        }

        // Glowstone, sea lantern, shroomlight
        if (!showGlowstone.get()) {
            if (block == Blocks.GLOWSTONE
             || block == Blocks.SEA_LANTERN
             || block == Blocks.SHROOMLIGHT) return false;
        }

        // Redstone lamp và torch
        if (!showRedstone.get()) {
            if (block == Blocks.REDSTONE_LAMP
             || block == Blocks.REDSTONE_TORCH
             || block == Blocks.REDSTONE_WALL_TORCH) return false;
        }

        // Beacon, conduit
        if (!showBeacons.get()) {
            if (block == Blocks.BEACON
             || block == Blocks.CONDUIT) return false;
        }

        // Amethyst cluster (luminance 5 — geode finder)
        if (!showAmethyst.get()) {
            if (block == Blocks.AMETHYST_CLUSTER
             || block == Blocks.LARGE_AMETHYST_BUD
             || block == Blocks.MEDIUM_AMETHYST_BUD
             || block == Blocks.SMALL_AMETHYST_BUD) return false;
        }

        return true;
    }

    // ── Thermal color ─────────────────────────────────────────────────────────

    /**
     * Trả về [r, g, b, a] trong khoảng 0.0-1.0
     * Gradient: xanh dương (dim) → vàng (mid) → đỏ/trắng (sáng nhất)
     */
    private float[] thermalColor(int lum) {
        float[] c = new float[4];
        float n = lum / 15.0f;
        c[3] = 0.25f + n * 0.75f;

        if (lum <= 5) {
            float t = lum / 5.0f;
            c[0] = t * 0.2f;
            c[1] = t * 0.3f;
            c[2] = 0.5f + t * 0.5f;
        } else if (lum <= 10) {
            float t = (lum - 5) / 5.0f;
            c[0] = 0.2f + t * 0.8f;
            c[1] = 0.3f + t * 0.7f;
            c[2] = 1.0f - t * 0.8f;
        } else if (lum <= 13) {
            float t = (lum - 10) / 3.0f;
            c[0] = 1.0f;
            c[1] = 1.0f - t * 0.5f;
            c[2] = 0.2f - t * 0.2f;
        } else {
            float t = (lum - 13) / 2.0f;
            c[0] = 1.0f;
            c[1] = 0.5f + t * 0.5f;
            c[2] = t;
            c[3] = 0.8f + t * 0.2f;
        }
        return c;
    }
}
