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

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/SpawnerFinder.class */
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

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/SpawnerFinder$SpawnerToast.class */
    private static class SpawnerToast implements class_368 {
        private static final class_1799 ICON = new class_1799(class_1802.field_8849);
        private final class_2561 title;
        private final class_2561 description;
        private final long displayDuration;
        private long elapsed;
        private class_368.class_369 visibility = class_368.class_369.field_2210;

        SpawnerToast(class_2561 param1, class_2561 param2, long param3) {
            this.title = param1;
            this.description = param2;
            this.displayDuration = param3;
        }

        public class_368.class_369 method_61988() {
            return this.visibility;
        }

        public void method_61989(class_374 param1, long param2) {
            this.elapsed = param2;
            if (this.elapsed >= this.displayDuration) {
                this.visibility = class_368.class_369.field_2209;
            }
        }

        public void method_1986(class_332 param1, class_327 param2, long param3) {
            param1.method_25294(0, 0, method_29049(), method_29050(), -267118560);
            param1.method_25294(0, 0, 3, method_29050(), -14494738);
            param1.method_51427(ICON, 9, 7);
            param1.method_51439(param2, this.title, 32, 8, -14494738, false);
            param1.method_51439(param2, this.description, 32, 19, -5195580, false);
        }

        public int method_29049() {
            return 168;
        }

        public int method_29050() {
            return 30;
        }
    }

    public SpawnerFinder() {
        super(KingAddon.CATEGORY, "spawner-finder", "Detects mob spawners with sound, toast, and rainbow chunk highlights.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNotify = this.settings.createGroup("Notifications");
        this.sgRender = this.settings.createGroup("Render");
        this.sgRainbow = this.settings.createGroup("Rainbow");
        this.rainbow = this.sgRainbow.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("rainbow")).description("Cycle chunk highlights through rainbow colors.")).defaultValue(true)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder) ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).description("Fill color of the chunk highlight when rainbow is disabled.")).defaultValue(new SettingColor(255, 85, 85, 80)).visible(() -> {
            return !((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder) ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).description("Outline color of the chunk highlight when rainbow is disabled.")).defaultValue(new SettingColor(255, 85, 85, 255)).visible(() -> {
            return !((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        this.sideColorAlpha = this.sgRender.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("side-color-alpha")).description("Transparency of the filled chunk sides when rainbow is enabled. 0 = invisible, 255 = opaque.")).defaultValue(20)).range(0, 255).sliderRange(0, 255).visible(() -> {
            return ((Boolean) this.rainbow.get()).booleanValue();
        })).build());
        this.lineColorAlpha = this.sgRender.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("line-color-alpha")).description("Transparency of the chunk outline when rainbow is enabled. 0 = invisible, 255 = opaque.")).defaultValue(200)).range(0, 255).sliderRange(0, 255).visible(() -> {
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
        SettingGroup settingGroup = this.sgNotify;
        IntSetting.Builder builderSliderRange = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("toast-duration")).description("How long the toast stays on screen (milliseconds).")).defaultValue(5000)).range(1000, 15000).sliderRange(1000, 10000);
        Setting<Boolean> setting = this.toastNotify;
        Objects.requireNonNull(setting);
        Objects.requireNonNull(setting);
        this.toastDuration = settingGroup.add(((IntSetting.Builder) builderSliderRange.visible(setting::get)).build());
        this.soundEnabled = this.sgNotify.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound")).description("Play a notification sound when a spawner is found.")).defaultValue(true)).build());
        SettingGroup settingGroup2 = this.sgNotify;
        DoubleSetting.Builder builderSliderRange2 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-volume")).description("Notification sound volume.")).defaultValue(1.0d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d);
        Setting<Boolean> setting2 = this.soundEnabled;
        Objects.requireNonNull(setting2);
        Objects.requireNonNull(setting2);
        this.soundVolume = settingGroup2.add(((DoubleSetting.Builder) builderSliderRange2.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgNotify;
        DoubleSetting.Builder builderSliderRange3 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-pitch")).description("Notification sound pitch. Higher = more urgent feel.")).defaultValue(1.5d).range(0.5d, 2.0d).sliderRange(0.5d, 2.0d);
        Setting<Boolean> setting3 = this.soundEnabled;
        Objects.requireNonNull(setting3);
        Objects.requireNonNull(setting3);
        this.soundPitch = settingGroup3.add(((DoubleSetting.Builder) builderSliderRange3.visible(setting3::get)).build());
        this.notifyCooldown = this.sgNotify.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("notify-cooldown")).description("Seconds before the same chunk can trigger a notification again.")).defaultValue(60)).range(5, 300).sliderRange(5, 120).visible(() -> {
            return ((Boolean) this.chatNotify.get()).booleanValue() || ((Boolean) this.toastNotify.get()).booleanValue();
        })).build());
        this.renderChunks = this.sgRender.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("render-chunks")).description("Render chunk highlights where spawners are found.")).defaultValue(true)).build());
        SettingGroup settingGroup4 = this.sgRender;
        IntSetting.Builder builderSliderRange4 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("render-bottom-y")).description("Bottom Y of the chunk column outline.")).defaultValue(-64)).range(-64, 320).sliderRange(-64, 320);
        Setting<Boolean> setting4 = this.renderChunks;
        Objects.requireNonNull(setting4);
        Objects.requireNonNull(setting4);
        this.renderBottom = settingGroup4.add(((IntSetting.Builder) builderSliderRange4.visible(setting4::get)).build());
        SettingGroup settingGroup5 = this.sgRender;
        IntSetting.Builder builderSliderRange5 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("render-top-y")).description("Top Y of the chunk column outline.")).defaultValue(128)).range(-64, 320).sliderRange(-64, 320);
        Setting<Boolean> setting5 = this.renderChunks;
        Objects.requireNonNull(setting5);
        Objects.requireNonNull(setting5);
        this.renderTop = settingGroup5.add(((IntSetting.Builder) builderSliderRange5.visible(setting5::get)).build());
        SettingGroup settingGroup6 = this.sgRender;
        IntSetting.Builder builderSliderRange6 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-render-chunk-radius")).description("Chunk highlights auto-remove when farther than this many chunks from you. 0 = keep forever.")).defaultValue(16)).range(0, 64).sliderRange(0, 32);
        Setting<Boolean> setting6 = this.renderChunks;
        Objects.requireNonNull(setting6);
        Objects.requireNonNull(setting6);
        this.maxRenderChunkRadius = settingGroup6.add(((IntSetting.Builder) builderSliderRange6.visible(setting6::get)).build());
        SettingGroup settingGroup7 = this.sgRender;
        EnumSetting.Builder builder = (EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).description("Lines = fastest, Both = best visibility.")).defaultValue(ShapeMode.Both);
        Setting<Boolean> setting7 = this.renderChunks;
        Objects.requireNonNull(setting7);
        Objects.requireNonNull(setting7);
        this.shapeMode = settingGroup7.add(((EnumSetting.Builder) builder.visible(setting7::get)).build());
        SettingGroup settingGroup8 = this.sgRainbow;
        IntSetting.Builder builderSliderRange7 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("rainbow-speed")).description("Milliseconds for one full rainbow cycle. Lower = faster.")).defaultValue(3000)).range(500, 15000).sliderRange(500, 10000);
        Setting<Boolean> setting8 = this.rainbow;
        Objects.requireNonNull(setting8);
        Objects.requireNonNull(setting8);
        this.rainbowSpeed = settingGroup8.add(((IntSetting.Builder) builderSliderRange7.visible(setting8::get)).build());
        SettingGroup settingGroup9 = this.sgRainbow;
        BoolSetting.Builder builder2 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chunk-offset")).description("Offset the rainbow hue per-chunk for a wave effect across multiple spawners.")).defaultValue(true);
        Setting<Boolean> setting9 = this.rainbow;
        Objects.requireNonNull(setting9);
        Objects.requireNonNull(setting9);
        this.chunkOffsetRainbow = settingGroup9.add(((BoolSetting.Builder) builder2.visible(setting9::get)).build());
        SettingGroup settingGroup10 = this.sgRainbow;
        DoubleSetting.Builder builderSliderRange8 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("saturation")).description("Color saturation. 1.0 = vivid, 0.0 = gray.")).defaultValue(1.0d).range(0.0d, 1.0d).sliderRange(0.0d, 1.0d);
        Setting<Boolean> setting10 = this.rainbow;
        Objects.requireNonNull(setting10);
        Objects.requireNonNull(setting10);
        this.rainbowSaturation = settingGroup10.add(((DoubleSetting.Builder) builderSliderRange8.visible(setting10::get)).build());
        SettingGroup settingGroup11 = this.sgRainbow;
        DoubleSetting.Builder builderSliderRange9 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("brightness")).description("Color brightness. 1.0 = full, 0.0 = black.")).defaultValue(1.0d).range(0.0d, 1.0d).sliderRange(0.0d, 1.0d);
        Setting<Boolean> setting11 = this.rainbow;
        Objects.requireNonNull(setting11);
        Objects.requireNonNull(setting11);
        this.rainbowBrightness = settingGroup11.add(((DoubleSetting.Builder) builderSliderRange9.visible(setting11::get)).build());
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
        int v1 = this.totalSpawners;
        return v1 + (this.totalSpawners == 1 ? " spawner" : " spawners") + " / " + this.markedChunks.size() + "c";
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        int v2 = (int) (System.currentTimeMillis() / 1000);
        class_2818 class_2818VarChunk = param1.chunk();
        processChunk(new class_1923(class_2818VarChunk.method_12004().field_9181, class_2818VarChunk.method_12004().field_9180), this.mc.field_1687, v2);
        recomputeTotal();
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_638 class_638Var = this.mc.field_1687;
        int v3 = (int) (System.currentTimeMillis() / 1000);
        scanPriorityChunks(class_638Var, v3);
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
        long v4 = System.nanoTime() + (((long) ((Integer) this.maxScanTimeMs.get()).intValue()) * 1000000);
        int v6 = ((Integer) this.chunksPerBatch.get()).intValue();
        int v7 = 0;
        while (true) {
            if (v7 >= v6 || this.queueIndex >= this.scanQueue.size()) {
                break;
            }
            processChunk(this.scanQueue.get(this.queueIndex), class_638Var, v3);
            if (System.nanoTime() > v4) {
                this.queueIndex++;
                break;
            } else {
                v7++;
                this.queueIndex++;
            }
        }
        if (this.queueIndex >= this.scanQueue.size()) {
            this.queueIndex = 0;
            rebuildScanQueue();
        }
        recomputeTotal();
    }

    private void scanPriorityChunks(class_1937 param1, int param2) {
        class_1923 class_1923VarMethod_31476 = this.mc.field_1724.method_31476();
        int v4 = ((Integer) this.priorityRadius.get()).intValue();
        for (int v5 = -v4; v5 <= v4; v5++) {
            for (int v6 = -v4; v6 <= v4; v6++) {
                processChunk(new class_1923(class_1923VarMethod_31476.field_9181 + v5, class_1923VarMethod_31476.field_9180 + v6), param1, param2);
            }
        }
    }

    private void processChunk(class_1923 param1, class_1937 param2, int param3) {
        if (param2.method_8393(param1.field_9181, param1.field_9180)) {
            class_2818 class_2818VarMethod_8497 = param2.method_8497(param1.field_9181, param1.field_9180);
            if (class_2818VarMethod_8497 instanceof class_2818) {
                long v5 = class_1923.method_8331(param1.field_9181, param1.field_9180);
                List<class_2338> listFindSpawnersInChunk = findSpawnersInChunk(class_2818VarMethod_8497);
                int v8 = listFindSpawnersInChunk.size();
                if (v8 <= 0) {
                    int v9 = this.missedScanCounts.get(v5) + 1;
                    this.missedScanCounts.put(v5, v9);
                    if (v9 >= 40) {
                        this.markedChunks.remove(v5);
                        this.spawnerPositions.remove(v5);
                        this.spawnerCounts.remove(v5);
                        this.lastNotifiedCount.remove(v5);
                        this.missedScanCounts.remove(v5);
                        return;
                    }
                    return;
                }
                this.markedChunks.add(v5);
                this.spawnerPositions.put(v5, listFindSpawnersInChunk);
                int v92 = this.lastNotifiedCount.get(v5);
                int v10 = this.lastNotifyTime.get(v5);
                int v11 = v92 != v8 ? 1 : 0;
                int v12 = param3 - v10 >= ((Integer) this.notifyCooldown.get()).intValue() ? 1 : 0;
                this.spawnerCounts.put(v5, v8);
                this.missedScanCounts.put(v5, 0);
                if (v11 == 0 || v12 == 0) {
                    return;
                }
                this.lastNotifiedCount.put(v5, v8);
                this.lastNotifyTime.put(v5, param3);
                fireNotification(param1, listFindSpawnersInChunk);
            }
        }
    }

    private void recomputeTotal() {
        this.totalSpawners = 0;
        IntIterator it = this.spawnerCounts.values().iterator();
        while (it.hasNext()) {
            int v1 = it.next().intValue();
            this.totalSpawners += v1;
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent param1) {
        if (!((Boolean) this.renderChunks.get()).booleanValue() || this.markedChunks.isEmpty()) {
            return;
        }
        long v2 = System.currentTimeMillis();
        ShapeMode shapeMode = (ShapeMode) this.shapeMode.get();
        double v5 = Math.min(((Integer) this.renderBottom.get()).intValue(), ((Integer) this.renderTop.get()).intValue());
        double v7 = Math.max(((Integer) this.renderBottom.get()).intValue(), ((Integer) this.renderTop.get()).intValue());
        int v9 = ((Integer) this.maxRenderChunkRadius.get()).intValue();
        class_1923 class_1923VarMethod_31476 = this.mc.field_1724 != null ? this.mc.field_1724.method_31476() : null;
        int v11 = class_1923VarMethod_31476 != null ? class_1923VarMethod_31476.field_9181 : 0;
        int v12 = class_1923VarMethod_31476 != null ? class_1923VarMethod_31476.field_9180 : 0;
        int v13 = v9 * v9;
        LongOpenHashSet longOpenHashSet = null;
        for (long v19 : this.markedChunks.toLongArray()) {
            int v21 = class_1923.method_8325(v19);
            int v22 = class_1923.method_8332(v19);
            int v23 = v21 - v11;
            int v24 = v22 - v12;
            if (v9 <= 0 || (v23 * v23) + (v24 * v24) <= v13) {
                int v25 = v21 << 4;
                int v26 = v22 << 4;
                class_238 class_238Var = new class_238(v25, v5, v26, v25 + 16, v7, v26 + 16);
                if (((Boolean) this.rainbow.get()).booleanValue()) {
                    float v28 = computeHue(v2, v21, v22);
                    float v29 = ((Double) this.rainbowSaturation.get()).floatValue();
                    float v30 = ((Double) this.rainbowBrightness.get()).floatValue();
                    hsbToColor(this.renderSide, v28, v29, v30, ((Integer) this.sideColorAlpha.get()).intValue());
                    hsbToColor(this.renderLine, v28, v29, v30, ((Integer) this.lineColorAlpha.get()).intValue());
                    param1.renderer.box(class_238Var, this.renderSide, this.renderLine, shapeMode, EXCLUDE_CAPS);
                } else {
                    param1.renderer.box(class_238Var, (Color) this.sideColor.get(), (Color) this.lineColor.get(), shapeMode, EXCLUDE_CAPS);
                }
            } else {
                if (longOpenHashSet == null) {
                    longOpenHashSet = new LongOpenHashSet();
                }
                longOpenHashSet.add(v19);
            }
        }
        if (longOpenHashSet != null) {
            LongIterator it = longOpenHashSet.iterator();
            while (it.hasNext()) {
                long jLongValue = ((Long) it.next()).longValue();
                this.markedChunks.remove(jLongValue);
                this.spawnerPositions.remove(jLongValue);
                this.spawnerCounts.remove(jLongValue);
                this.lastNotifiedCount.remove(jLongValue);
                this.missedScanCounts.remove(jLongValue);
                this.lastNotifyTime.remove(jLongValue);
            }
        }
    }

    private void rebuildScanQueue() {
        this.scanQueue.clear();
        if (this.mc.field_1724 == null) {
            return;
        }
        class_1923 class_1923VarMethod_31476 = this.mc.field_1724.method_31476();
        int v2 = ((Integer) this.chunkRange.get()).intValue();
        if (0 > v2) {
            return;
        }
        int v5 = 0;
        while (true) {
            if (0 == 0) {
                this.scanQueue.add(new class_1923(class_1923VarMethod_31476.field_9181 + 0, class_1923VarMethod_31476.field_9180 + 0));
            }
            v5++;
        }
    }

    private List<class_2338> findSpawnersInChunk(class_2791 param1) {
        ArrayList arrayList = new ArrayList();
        class_1923 class_1923VarMethod_12004 = param1.method_12004();
        int v4 = class_1923VarMethod_12004.method_8326();
        int v5 = class_1923VarMethod_12004.method_8328();
        int v6 = ((Integer) this.yMin.get()).intValue();
        int v7 = ((Integer) this.yMax.get()).intValue();
        int v8 = param1.method_32891();
        int v9 = param1.method_31597();
        for (int v10 = v8; v10 < v9; v10++) {
            int v11 = v10 << 4;
            if (v11 + 15 >= v6 && v11 <= v7) {
                int v12 = v10 - v8;
                class_2826 class_2826VarMethod_38259 = param1.method_38259(v12);
                if (class_2826VarMethod_38259 != null && !class_2826VarMethod_38259.method_38292() && class_2826VarMethod_38259.method_19523(param0 -> {
                    return param0.method_27852(class_2246.field_10260);
                })) {
                    for (int v14 = 0; v14 < 16; v14++) {
                        int v15 = v11 + v14;
                        if (v15 >= v6 && v15 <= v7) {
                            for (int v16 = 0; v16 < 16; v16++) {
                                for (int v17 = 0; v17 < 16; v17++) {
                                    if (class_2826VarMethod_38259.method_12254(v16, v14, v17).method_27852(class_2246.field_10260)) {
                                        arrayList.add(new class_2338(v4 + v16, v15, v5 + v17));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    private void fireNotification(class_1923 param1, List<class_2338> list) {
        if (((Boolean) this.chatNotify.get()).booleanValue()) {
            sendChatNotification(param1, list);
        }
        if (((Boolean) this.toastNotify.get()).booleanValue()) {
            sendToastNotification(list);
        }
        if (((Boolean) this.soundEnabled.get()).booleanValue()) {
            playNotificationSound();
        }
    }

    private void sendChatNotification(class_1923 param1, List<class_2338> list) {
        for (class_2338 class_2338Var : list) {
            class_5250 class_5250VarMethod_43473 = class_2561.method_43473();
            class_5250VarMethod_43473.method_10852(class_2561.method_43470("Bassa").method_27694(param0 -> {
                return param0.method_10977(class_124.field_1078).method_10982(true);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470(" » ").method_27694(param02 -> {
                return param02.method_10977(class_124.field_1063);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470("spawner").method_27694(param03 -> {
                return param03.method_10977(class_124.field_1061);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470(" | ").method_27694(param04 -> {
                return param04.method_10977(class_124.field_1063);
            }));
            int v6 = class_2338Var.method_10263();
            class_5250VarMethod_43473.method_10852(class_2561.method_43470(v6 + " " + class_2338Var.method_10264() + " " + class_2338Var.method_10260()).method_27694(param05 -> {
                return param05.method_10977(class_124.field_1068);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470(" · ").method_27694(param06 -> {
                return param06.method_10977(class_124.field_1063);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470("chunk " + param1.field_9181 + "," + param1.field_9180).method_27694(param07 -> {
                return param07.method_10977(class_124.field_1080);
            }));
            ChatUtils.sendMsg(class_5250VarMethod_43473);
        }
    }

    private void sendToastNotification(List<class_2338> list) {
        if (this.mc.method_1566() != null) {
            class_2338 class_2338Var = list.get(0);
            int v3 = class_2338Var.method_10263();
            this.mc.method_1566().method_1999(new SpawnerToast(class_2561.method_43470("Spawner located"), class_2561.method_43470(v3 + " " + class_2338Var.method_10264() + " " + class_2338Var.method_10260()), ((Integer) this.toastDuration.get()).intValue()));
        }
    }

    private void playNotificationSound() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        this.mc.field_1687.method_8396(this.mc.field_1724, this.mc.field_1724.method_24515(), class_3417.field_14627, class_3419.field_15250, ((Double) this.soundVolume.get()).floatValue(), ((Double) this.soundPitch.get()).floatValue());
    }

    private float computeHue(long param1, int param3, int param4) {
        float v5 = (param1 % ((long) ((Integer) this.rainbowSpeed.get()).intValue())) / ((Integer) this.rainbowSpeed.get()).intValue();
        if (((Boolean) this.chunkOffsetRainbow.get()).booleanValue()) {
            float v6 = (((param3 * 7) + (param4 * 13)) & 255) / 256.0f;
            v5 = (v5 + v6) % 1.0f;
        }
        return v5;
    }

    private void hsbToColor(Color param1, float param2, float param3, float param4, int param5) {
        float v11;
        float v12;
        float v13;
        float v6 = (param2 - ((float) Math.floor(param2))) * 6.0f;
        float v7 = v6 - ((float) Math.floor(v6));
        float v8 = param4 * (1.0f - param3);
        float v9 = param4 * (1.0f - (param3 * v7));
        float v10 = param4 * (1.0f - (param3 * (1.0f - v7)));
        switch ((int) v6) {
            case 0:
                v11 = param4;
                v12 = v10;
                v13 = v8;
                break;
            case 1:
                v11 = v9;
                v12 = param4;
                v13 = v8;
                break;
            case 2:
                v11 = v8;
                v12 = param4;
                v13 = v10;
                break;
            case 3:
                v11 = v8;
                v12 = v9;
                v13 = param4;
                break;
            case 4:
                v11 = v10;
                v12 = v8;
                v13 = param4;
                break;
            default:
                v11 = param4;
                v12 = v8;
                v13 = v9;
                break;
        }
        param1.set((int) (v11 * 255.0f), (int) (v12 * 255.0f), (int) (v13 * 255.0f), param5);
    }
}
