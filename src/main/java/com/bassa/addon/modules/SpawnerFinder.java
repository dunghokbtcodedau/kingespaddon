package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2791;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5250;
import net.minecraft.class_638;

/* JADX INFO: loaded from: king.jar:com/bassa/addon/modules/SpawnerFinder.class */
public class SpawnerFinder extends KingModule {
    private static final int EXCLUDE_CAPS = 6;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNotify;
    private final SettingGroup sgRender;
    private final SettingGroup sgRainbow;
    private final Setting<Integer> chunkRange;
    private final Setting<Integer> scanInterval;
    private final Setting<Integer> chunksPerBatch;
    private final Setting<Integer> maxScanTimeMs;
    private final Setting<Integer> priorityRadius;
    private final Setting<Integer> yMin;
    private final Setting<Integer> yMax;
    private final Setting<Boolean> chatNotify;
    private final Setting<Boolean> toastNotify;
    private final Setting<Integer> toastDuration;
    private final Setting<Boolean> soundEnabled;
    private final Setting<Double> soundVolume;
    private final Setting<Double> soundPitch;
    private final Setting<Integer> notifyCooldown;
    private final Setting<Boolean> renderChunks;
    private final Setting<Integer> renderBottom;
    private final Setting<Integer> renderTop;
    private final Setting<Integer> maxRenderChunkRadius;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Integer> sideColorAlpha;
    private final Setting<Integer> lineColorAlpha;
    private final Setting<Boolean> rainbow;
    private final Setting<Integer> rainbowSpeed;
    private final Setting<Boolean> chunkOffsetRainbow;
    private final Setting<Double> rainbowSaturation;
    private final Setting<Double> rainbowBrightness;
    private final LongSet markedChunks;
    private final Long2ObjectMap<List<class_2338>> spawnerPositions;
    private final Long2IntMap spawnerCounts;
    private final Long2IntMap lastNotifiedCount;
    private final Long2IntMap lastNotifyTime;
    private final Long2IntMap missedScanCounts;
    private final int CHUNK_HOLD_TICKS = 40;
    private final List<class_1923> scanQueue;
    private int tickCounter;
    private int queueIndex;
    private int totalSpawners;
    private final Color renderSide;
    private final Color renderLine;

    public SpawnerFinder() {
        super(KingAddon.CATEGORY, "spawner-finder", "Detects mob spawners with sound, toast, and rainbow chunk highlights.");
        //this.CHUNK_HOLD_TICKS = 40;
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNotify = this.settings.createGroup("Notifications");
        this.sgRender = this.settings.createGroup("Render");
        this.sgRainbow = this.settings.createGroup("Rainbow");
        this.rainbow = this.sgRainbow.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("rainbow")).description("Cycle chunk highlights through rainbow colors.")).defaultValue(true)).build());
        ColorSetting.Builder var18 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).description("Fill color of the chunk highlight when rainbow is disabled.")).defaultValue(new SettingColor(255, 85, 85, 80));
        this.sideColor = this.sgRender.add(((ColorSetting.Builder) var18.visible(() -> {
            return !((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        ColorSetting.Builder var182 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).description("Outline color of the chunk highlight when rainbow is disabled.")).defaultValue(new SettingColor(255, 85, 85, 255));
        this.lineColor = this.sgRender.add(((ColorSetting.Builder) var182.visible(() -> {
            return !((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        IntSetting.Builder var19 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("side-color-alpha")).description("Transparency of the filled chunk sides when rainbow is enabled. 0 = invisible, 255 = opaque.")).defaultValue(20)).range(0, 255).sliderRange(0, 255);
        this.sideColorAlpha = this.sgRender.add(((IntSetting.Builder) var19.visible(() -> {
            return ((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        IntSetting.Builder var192 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("line-color-alpha")).description("Transparency of the chunk outline when rainbow is enabled. 0 = invisible, 255 = opaque.")).defaultValue(200)).range(0, 255).sliderRange(0, 255);
        this.lineColorAlpha = this.sgRender.add(((IntSetting.Builder) var192.visible(() -> {
            return ((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        this.chunkRange = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("chunk-range")).description("Chunk radius to scan around you. Lower = less lag on DonutSMP.")).defaultValue(4)).range(1, 16).sliderRange(1, 16).build());
        this.scanInterval = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("scan-interval")).description("Ticks between background chunk scan batches. New chunks are always scanned instantly on load.")).defaultValue(1)).range(1, 40).sliderRange(1, 40).build());
        this.chunksPerBatch = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("chunks-per-batch")).description("Chunks scanned per background batch. Higher = faster full-area sweep.")).defaultValue(8)).range(1, 32).sliderRange(1, 32).build());
        this.maxScanTimeMs = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-scan-time-ms")).description("Max milliseconds spent scanning per tick. Lower = less lag on weak PCs.")).defaultValue(5)).range(1, 15).sliderRange(1, 15).build());
        this.priorityRadius = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("priority-radius")).description("Chunk radius around you rescanned every tick for instant spawner detection.")).defaultValue(2)).range(0, 4).sliderRange(0, 4).build());
        this.yMin = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("y-min")).description("Minimum Y level to scan. Spawners are underground, -64 catches everything.")).defaultValue(-64)).range(-64, 320).sliderRange(-64, 64).build());
        this.yMax = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("y-max")).description("Maximum Y level to scan. -58 is deepslate layer, ideal for dungeon spawners.")).defaultValue(-58)).range(-64, 320).sliderRange(-64, 320).build());
        this.chatNotify = this.sgNotify.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chat-notify")).description("Show a styled chat message with spawner coordinates.")).defaultValue(true)).build());
        this.toastNotify = this.sgNotify.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("toast-notify")).description("Show a Minecraft toast notification with a skeleton skull icon.")).defaultValue(true)).build());
        SettingGroup var10001 = this.sgNotify;
        IntSetting.Builder var10002 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("toast-duration")).description("How long the toast stays on screen (milliseconds).")).defaultValue(5000)).range(1000, 15000).sliderRange(1000, 10000);
        Setting<Boolean> var10003 = this.toastNotify;
        Objects.requireNonNull(var10003);
        Objects.requireNonNull(var10003);
        this.toastDuration = var10001.add(((IntSetting.Builder) var10002.visible(var10003::get)).build());
        this.soundEnabled = this.sgNotify.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound")).description("Play a notification sound when a spawner is found.")).defaultValue(true)).build());
        SettingGroup var100012 = this.sgNotify;
        DoubleSetting.Builder var10 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-volume")).description("Notification sound volume.")).defaultValue(1.0d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d);
        Setting<Boolean> var100032 = this.soundEnabled;
        Objects.requireNonNull(var100032);
        Objects.requireNonNull(var100032);
        this.soundVolume = var100012.add(((DoubleSetting.Builder) var10.visible(var100032::get)).build());
        SettingGroup var100013 = this.sgNotify;
        DoubleSetting.Builder var102 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-pitch")).description("Notification sound pitch. Higher = more urgent feel.")).defaultValue(1.5d).range(0.5d, 2.0d).sliderRange(0.5d, 2.0d);
        Setting<Boolean> var100033 = this.soundEnabled;
        Objects.requireNonNull(var100033);
        Objects.requireNonNull(var100033);
        this.soundPitch = var100013.add(((DoubleSetting.Builder) var102.visible(var100033::get)).build());
        this.notifyCooldown = this.sgNotify.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("notify-cooldown")).description("Seconds before the same chunk can trigger a notification again.")).defaultValue(60)).range(5, 300).sliderRange(5, 120).visible(() -> {
            return ((Boolean) this.chatNotify.get()).booleanValue() || ((Boolean) this.toastNotify.get()).booleanValue();
        })).build());
        this.renderChunks = this.sgRender.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("render-chunks")).description("Render chunk highlights where spawners are found.")).defaultValue(true)).build());
        SettingGroup var100014 = this.sgRender;
        IntSetting.Builder var12 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("render-bottom-y")).description("Bottom Y of the chunk column outline.")).defaultValue(-64)).range(-64, 320).sliderRange(-64, 320);
        Setting<Boolean> var100034 = this.renderChunks;
        Objects.requireNonNull(var100034);
        Objects.requireNonNull(var100034);
        this.renderBottom = var100014.add(((IntSetting.Builder) var12.visible(var100034::get)).build());
        SettingGroup var100015 = this.sgRender;
        IntSetting.Builder var122 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("render-top-y")).description("Top Y of the chunk column outline.")).defaultValue(128)).range(-64, 320).sliderRange(-64, 320);
        Setting<Boolean> var100035 = this.renderChunks;
        Objects.requireNonNull(var100035);
        Objects.requireNonNull(var100035);
        this.renderTop = var100015.add(((IntSetting.Builder) var122.visible(var100035::get)).build());
        SettingGroup var100016 = this.sgRender;
        IntSetting.Builder varRenderRadius = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-render-chunk-radius")).description("Chunk highlights auto-remove when farther than this many chunks from you. 0 = keep forever.")).defaultValue(16)).range(0, 64).sliderRange(0, 32);
        Setting<Boolean> var100036 = this.renderChunks;
        Objects.requireNonNull(var100036);
        Objects.requireNonNull(var100036);
        this.maxRenderChunkRadius = var100016.add(((IntSetting.Builder) varRenderRadius.visible(var100036::get)).build());
        SettingGroup var100017 = this.sgRender;
        EnumSetting.Builder var14 = (EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).description("Lines = fastest, Both = best visibility.")).defaultValue(ShapeMode.Both);
        Setting<Boolean> var100037 = this.renderChunks;
        Objects.requireNonNull(var100037);
        Objects.requireNonNull(var100037);
        this.shapeMode = var100017.add(((EnumSetting.Builder) var14.visible(var100037::get)).build());
        SettingGroup var100018 = this.sgRainbow;
        IntSetting.Builder var15 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("rainbow-speed")).description("Milliseconds for one full rainbow cycle. Lower = faster.")).defaultValue(3000)).range(500, 15000).sliderRange(500, 10000);
        Setting<Boolean> var100038 = this.rainbow;
        Objects.requireNonNull(var100038);
        Objects.requireNonNull(var100038);
        this.rainbowSpeed = var100018.add(((IntSetting.Builder) var15.visible(var100038::get)).build());
        SettingGroup var100019 = this.sgRainbow;
        BoolSetting.Builder var16 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chunk-offset")).description("Offset the rainbow hue per-chunk for a wave effect across multiple spawners.")).defaultValue(true);
        Setting<Boolean> var100039 = this.rainbow;
        Objects.requireNonNull(var100039);
        Objects.requireNonNull(var100039);
        this.chunkOffsetRainbow = var100019.add(((BoolSetting.Builder) var16.visible(var100039::get)).build());
        SettingGroup var1000110 = this.sgRainbow;
        DoubleSetting.Builder var17 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("saturation")).description("Color saturation. 1.0 = vivid, 0.0 = gray.")).defaultValue(1.0d).range(0.0d, 1.0d).sliderRange(0.0d, 1.0d);
        Setting<Boolean> var1000310 = this.rainbow;
        Objects.requireNonNull(var1000310);
        Objects.requireNonNull(var1000310);
        this.rainbowSaturation = var1000110.add(((DoubleSetting.Builder) var17.visible(var1000310::get)).build());
        SettingGroup var1000111 = this.sgRainbow;
        DoubleSetting.Builder var172 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("brightness")).description("Color brightness. 1.0 = full, 0.0 = black.")).defaultValue(1.0d).range(0.0d, 1.0d).sliderRange(0.0d, 1.0d);
        Setting<Boolean> var1000311 = this.rainbow;
        Objects.requireNonNull(var1000311);
        Objects.requireNonNull(var1000311);
        this.rainbowBrightness = var1000111.add(((DoubleSetting.Builder) var172.visible(var1000311::get)).build());
        this.markedChunks = new LongOpenHashSet();
        this.spawnerPositions = new Long2ObjectOpenHashMap();
        this.spawnerCounts = new Long2IntOpenHashMap();
        this.lastNotifiedCount = new Long2IntOpenHashMap();
        this.lastNotifyTime = new Long2IntOpenHashMap();
        this.scanQueue = new ArrayList();
        this.renderSide = new Color();
        this.renderLine = new Color();
        this.spawnerCounts.defaultReturnValue(0);
        this.lastNotifiedCount.defaultReturnValue(-1);
        this.missedScanCounts = new Long2IntOpenHashMap();
        this.missedScanCounts.defaultReturnValue(0);
    }

    public void onActivate() {
        this.markedChunks.clear();
        this.spawnerPositions.clear();
        this.spawnerCounts.clear();
        this.lastNotifiedCount.clear();
        this.lastNotifyTime.clear();
        this.missedScanCounts.clear();
        this.scanQueue.clear();
        this.tickCounter = 0;
        this.queueIndex = 0;
        this.totalSpawners = 0;
        rebuildScanQueue();
    }

    public void onDeactivate() {
        this.markedChunks.clear();
        this.spawnerPositions.clear();
        this.spawnerCounts.clear();
        this.missedScanCounts.clear();
        this.scanQueue.clear();
        this.totalSpawners = 0;
    }

    public String getInfoString() {
        int var10000 = this.totalSpawners;
        return var10000 + (this.totalSpawners == 1 ? " spawner" : " spawners") + " / " + this.markedChunks.size() + "c";
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            int now = (int) (System.currentTimeMillis() / 1000);
            class_2818 wc = event.chunk();
            processChunk(new class_1923(wc.method_12004().field_9181, wc.method_12004().field_9180), this.mc.field_1687, now);
            recomputeTotal();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            class_638 class_638Var = this.mc.field_1687;
            int now = (int) (System.currentTimeMillis() / 1000);
            scanPriorityChunks(class_638Var, now);
            int i = this.tickCounter;
            this.tickCounter = i + 1;
            if (i < ((Integer) this.scanInterval.get()).intValue()) {
                recomputeTotal();
                return;
            }
            this.tickCounter = 0;
            if (this.scanQueue.isEmpty()) {
                rebuildScanQueue();
            }
            if (this.scanQueue.isEmpty()) {
                recomputeTotal();
                return;
            }
            long deadline = System.nanoTime() + (((long) ((Integer) this.maxScanTimeMs.get()).intValue()) * 1000000);
            int batch = ((Integer) this.chunksPerBatch.get()).intValue();
            int i2 = 0;
            while (true) {
                if (i2 >= batch || this.queueIndex >= this.scanQueue.size()) {
                    break;
                }
                processChunk(this.scanQueue.get(this.queueIndex), class_638Var, now);
                if (System.nanoTime() > deadline) {
                    this.queueIndex++;
                    break;
                } else {
                    i2++;
                    this.queueIndex++;
                }
            }
            if (this.queueIndex >= this.scanQueue.size()) {
                this.queueIndex = 0;
                rebuildScanQueue();
            }
            recomputeTotal();
        }
    }

    private void scanPriorityChunks(class_1937 world, int now) {
        class_1923 center = this.mc.field_1724.method_31476();
        int radius = ((Integer) this.priorityRadius.get()).intValue();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                processChunk(new class_1923(center.field_9181 + dx, center.field_9180 + dz), world, now);
            }
        }
    }

    private void processChunk(class_1923 cp, class_1937 world, int now) {
        if (world.method_8393(cp.field_9181, cp.field_9180)) {
            class_2818 class_2818VarMethod_8497 = world.method_8497(cp.field_9181, cp.field_9180);
            if (class_2818VarMethod_8497 instanceof class_2818) {
                long key = class_1923.method_8331(cp.field_9181, cp.field_9180);
                List<class_2338> found = findSpawnersInChunk(class_2818VarMethod_8497);
                int count = found.size();
                if (count > 0) {
                    this.markedChunks.add(key);
                    this.spawnerPositions.put(key, found);
                    int previous = this.lastNotifiedCount.get(key);
                    int lastTime = this.lastNotifyTime.get(key);
                    boolean countChanged = previous != count;
                    boolean cooledDown = now - lastTime >= ((Integer) this.notifyCooldown.get()).intValue();
                    this.spawnerCounts.put(key, count);
                    this.missedScanCounts.put(key, 0);
                    if (countChanged && cooledDown) {
                        this.lastNotifiedCount.put(key, count);
                        this.lastNotifyTime.put(key, now);
                        fireNotification(cp, found);
                        return;
                    }
                    return;
                }
                int misses = this.missedScanCounts.get(key) + 1;
                this.missedScanCounts.put(key, misses);
                if (misses >= 40) {
                    this.markedChunks.remove(key);
                    this.spawnerPositions.remove(key);
                    this.spawnerCounts.remove(key);
                    this.lastNotifiedCount.remove(key);
                    this.missedScanCounts.remove(key);
                }
            }
        }
    }

    private void recomputeTotal() {
        this.totalSpawners = 0;
        IntIterator var1 = this.spawnerCounts.values().iterator();
        while (var1.hasNext()) {
            int c = var1.next().intValue();
            this.totalSpawners += c;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (((Boolean) this.renderChunks.get()).booleanValue() && !this.markedChunks.isEmpty()) {
            long time = System.currentTimeMillis();
            ShapeMode mode = (ShapeMode) this.shapeMode.get();
            double y1 = Math.min(((Integer) this.renderBottom.get()).intValue(), ((Integer) this.renderTop.get()).intValue());
            double y2 = Math.max(((Integer) this.renderBottom.get()).intValue(), ((Integer) this.renderTop.get()).intValue());
            int maxRadius = ((Integer) this.maxRenderChunkRadius.get()).intValue();
            class_1923 playerChunk = this.mc.field_1724 != null ? this.mc.field_1724.method_31476() : null;
            int pcx = playerChunk != null ? playerChunk.field_9181 : 0;
            int pcz = playerChunk != null ? playerChunk.field_9180 : 0;
            int maxRadiusSq = maxRadius * maxRadius;
            long[] snapshot = this.markedChunks.toLongArray();
            LongOpenHashSet longOpenHashSet = null;
            for (long key : snapshot) {
                int cx = class_1923.method_8325(key);
                int cz = class_1923.method_8332(key);
                int dx = cx - pcx;
                int dz = cz - pcz;
                if (maxRadius > 0 && (dx * dx) + (dz * dz) > maxRadiusSq) {
                    if (longOpenHashSet == null) {
                        longOpenHashSet = new LongOpenHashSet();
                    }
                    longOpenHashSet.add(key);
                } else {
                    int minX = cx << 4;
                    int minZ = cz << 4;
                    class_238 box = new class_238(minX, y1, minZ, minX + 16, y2, minZ + 16);
                    if (((Boolean) this.rainbow.get()).booleanValue()) {
                        float hue = computeHue(time, cx, cz);
                        float sat = ((Double) this.rainbowSaturation.get()).floatValue();
                        float bri = ((Double) this.rainbowBrightness.get()).floatValue();
                        hsbToColor(this.renderSide, hue, sat, bri, ((Integer) this.sideColorAlpha.get()).intValue());
                        hsbToColor(this.renderLine, hue, sat, bri, ((Integer) this.lineColorAlpha.get()).intValue());
                        event.renderer.box(box, this.renderSide, this.renderLine, mode, EXCLUDE_CAPS);
                    } else {
                        event.renderer.box(box, (Color) this.sideColor.get(), (Color) this.lineColor.get(), mode, EXCLUDE_CAPS);
                    }
                }
            }
            if (longOpenHashSet != null) {
                LongIterator it = longOpenHashSet.iterator();
                while (it.hasNext()) {
                    long key2 = ((Long) it.next()).longValue();
                    this.markedChunks.remove(key2);
                    this.spawnerPositions.remove(key2);
                    this.spawnerCounts.remove(key2);
                    this.lastNotifiedCount.remove(key2);
                    this.missedScanCounts.remove(key2);
                    this.lastNotifyTime.remove(key2);
                }
            }
        }
    }

    private void rebuildScanQueue() {
        this.scanQueue.clear();
        if (this.mc.field_1724 != null) {
            class_1923 center = this.mc.field_1724.method_31476();
            int range = ((Integer) this.chunkRange.get()).intValue();
            for (int ring = 0; ring <= range; ring++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                            this.scanQueue.add(new class_1923(center.field_9181 + dx, center.field_9180 + dz));
                        }
                    }
                }
            }
        }
    }

    private List<class_2338> findSpawnersInChunk(class_2791 chunk) {
        List<class_2338> positions = new ArrayList<>();
        class_1923 cp = chunk.method_12004();
        int startX = cp.method_8326();
        int startZ = cp.method_8328();
        int minY = ((Integer) this.yMin.get()).intValue();
        int maxY = ((Integer) this.yMax.get()).intValue();
        int bottomCoord = chunk.method_32891();
        int topCoordExcl = chunk.method_31597();
        for (int sectionCoord = bottomCoord; sectionCoord < topCoordExcl; sectionCoord++) {
            int baseY = sectionCoord << 4;
            if (baseY + 15 >= minY && baseY <= maxY) {
                int sectionIdx = sectionCoord - bottomCoord;
                class_2826 sec = chunk.method_38259(sectionIdx);
                if (sec != null && !sec.method_38292() && sec.method_19523(state -> {
                    return state.method_27852(class_2246.field_10260);
                })) {
                    for (int y = 0; y < 16; y++) {
                        int worldY = baseY + y;
                        if (worldY >= minY && worldY <= maxY) {
                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    if (sec.method_12254(x, y, z).method_27852(class_2246.field_10260)) {
                                        positions.add(new class_2338(startX + x, worldY, startZ + z));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    private void fireNotification(class_1923 chunkPos, List<class_2338> spawners) {
        if (((Boolean) this.chatNotify.get()).booleanValue()) {
            sendChatNotification(chunkPos, spawners);
        }
        if (((Boolean) this.toastNotify.get()).booleanValue()) {
            sendToastNotification(spawners);
        }
        if (((Boolean) this.soundEnabled.get()).booleanValue()) {
            playNotificationSound();
        }
    }

    private void sendChatNotification(class_1923 chunkPos, List<class_2338> spawners) {
        for (class_2338 pos : spawners) {
            class_5250 line = class_2561.method_43473();
            line.method_10852(class_2561.method_43470("Bassa").method_27694(s -> {
                return s.method_10977(class_124.field_1078).method_10982(true);
            }));
            line.method_10852(class_2561.method_43470(" » ").method_27694(s2 -> {
                return s2.method_10977(class_124.field_1063);
            }));
            line.method_10852(class_2561.method_43470("spawner").method_27694(s3 -> {
                return s3.method_10977(class_124.field_1061);
            }));
            line.method_10852(class_2561.method_43470(" | ").method_27694(s4 -> {
                return s4.method_10977(class_124.field_1063);
            }));
            int var10001 = pos.method_10263();
            line.method_10852(class_2561.method_43470(var10001 + " " + pos.method_10264() + " " + pos.method_10260()).method_27694(s5 -> {
                return s5.method_10977(class_124.field_1068);
            }));
            line.method_10852(class_2561.method_43470(" · ").method_27694(s6 -> {
                return s6.method_10977(class_124.field_1063);
            }));
            line.method_10852(class_2561.method_43470("chunk " + chunkPos.field_9181 + "," + chunkPos.field_9180).method_27694(s7 -> {
                return s7.method_10977(class_124.field_1080);
            }));
            ChatUtils.sendMsg(line);
        }
    }

    private void sendToastNotification(List<class_2338> spawners) {
        if (this.mc.method_1566() != null) {
            class_2338 first = spawners.get(0);
            int var10000 = first.method_10263();
            String coordStr = var10000 + " " + first.method_10264() + " " + first.method_10260();
            this.mc.method_1566().method_1999(new SpawnerToast(class_2561.method_43470("Spawner located"), class_2561.method_43470(coordStr), ((Integer) this.toastDuration.get()).intValue()));
        }
    }

    private void playNotificationSound() {
        if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            this.mc.field_1687.method_8396(this.mc.field_1724, this.mc.field_1724.method_24515(), class_3417.field_14627, class_3419.field_15250, ((Double) this.soundVolume.get()).floatValue(), ((Double) this.soundPitch.get()).floatValue());
        }
    }

    private float computeHue(long timeMs, int cx, int cz) {
        float base = (timeMs % ((long) ((Integer) this.rainbowSpeed.get()).intValue())) / ((Integer) this.rainbowSpeed.get()).intValue();
        if (((Boolean) this.chunkOffsetRainbow.get()).booleanValue()) {
            float offset = (((cx * 7) + (cz * 13)) & 255) / 256.0f;
            base = (base + offset) % 1.0f;
        }
        return base;
    }

    private void hsbToColor(Color out, float hue, float sat, float bri, int alpha) {
        float r;
        float g;
        float b;
        float h = (hue - ((float) Math.floor(hue))) * 6.0f;
        float f = h - ((float) Math.floor(h));
        float p = bri * (1.0f - sat);
        float q = bri * (1.0f - (sat * f));
        float t = bri * (1.0f - (sat * (1.0f - f)));
        switch ((int) h) {
            case 0:
                r = bri;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = bri;
                b = p;
                break;
            case 2:
                r = p;
                g = bri;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = bri;
                break;
            case 4:
                r = t;
                g = p;
                b = bri;
                break;
            default:
                r = bri;
                g = p;
                b = q;
                break;
        }
        out.set((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), alpha);
    }

    /* JADX INFO: loaded from: king.jar:com/bassa/addon/modules/SpawnerFinder$SpawnerToast.class */
    private static class SpawnerToast implements class_368 {
        private static final class_1799 ICON = new class_1799(class_1802.field_8849);
        private final class_2561 title;
        private final class_2561 description;
        private final long displayDuration;
        private long elapsed;
        private class_368.class_369 visibility = class_368.class_369.field_2210;

        SpawnerToast(class_2561 title, class_2561 description, long displayDurationMs) {
            this.title = title;
            this.description = description;
            this.displayDuration = displayDurationMs;
        }

        public class_368.class_369 method_61988() {
            return this.visibility;
        }

        public void method_61989(class_374 manager, long time) {
            this.elapsed = time;
            if (this.elapsed >= this.displayDuration) {
                this.visibility = class_368.class_369.field_2209;
            }
        }

        public void method_1986(class_332 context, class_327 textRenderer, long time) {
            context.method_25294(0, 0, method_29049(), method_29050(), -267118560);
            context.method_25294(0, 0, 3, method_29050(), -14494738);
            context.method_51427(ICON, 9, 7);
            context.method_51439(textRenderer, this.title, 32, 8, -14494738, false);
            context.method_51439(textRenderer, this.description, 32, 19, -5195580, false);
        }

        public int method_29049() {
            return 168;
        }

        public int method_29050() {
            return 30;
        }
    }
}
