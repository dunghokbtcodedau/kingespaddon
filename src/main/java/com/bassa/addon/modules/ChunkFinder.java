package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
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
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1109;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2791;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3417;
import net.minecraft.class_3610;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkFinder.class */
public class ChunkFinder extends KingModule {
    private final SettingGroup sgDetection;
    private final SettingGroup sgRender;
    private final SettingGroup sgBlockHighlight;
    private final SettingGroup sgPerformance;
    private final SettingGroup sgNotifications;
    private final Setting<Boolean> detectDeepslate;
    private final Setting<Boolean> detectCobbledDeepslate;
    private final Setting<Boolean> detectRotatedDeepslate;
    private final Setting<Boolean> detectEndStone;
    private final Setting<Boolean> ignoreExposed;
    private final Setting<Boolean> ignoreTrialChambers;
    private final Setting<Integer> trialChamberThreshold;
    private final Setting<Integer> deepslateThreshold;
    private final Setting<Integer> cobbledDeepslateThreshold;
    private final Setting<Integer> rotatedDeepslateThreshold;
    private final Setting<Integer> endStoneThreshold;
    private final Setting<Double> renderY;
    private final Setting<ShapeMode> renderMode;
    private final Setting<SettingColor> chunkColor;
    private final Setting<Double> thickness;
    private final Setting<Boolean> highlightBlocks;
    private final Setting<Integer> maxBlocksToRender;
    private final Setting<ShapeMode> blockRenderMode;
    private final Setting<SettingColor> deepslateBlockColor;
    private final Setting<SettingColor> cobbledDeepslateBlockColor;
    private final Setting<SettingColor> rotatedDeepslateBlockColor;
    private final Setting<SettingColor> endStoneBlockColor;
    private final Setting<Boolean> useMultiThreading;
    private final Setting<Integer> threadCount;
    private final Setting<Integer> scanInterval;
    private final Setting<Integer> maxConcurrentScans;
    private final Setting<Integer> cleanupInterval;
    private final Setting<Mode> notificationMode;
    private final Setting<Boolean> playSound;
    private final Setting<Boolean> chatAlerts;
    private final Setting<Boolean> trialChamberAlerts;
    private final Setting<Integer> maxAlerts;
    private final Set<class_1923> flaggedChunks;
    private final ConcurrentHashMap<class_1923, ChunkAnalysis> chunkData;
    private final Set<class_1923> scannedChunks;
    private final ConcurrentHashMap<class_1923, Long> notificationTimes;
    private final Queue<Long> recentAlerts;
    private final AtomicLong activeScanCount;
    private final Map<class_2338, SuspiciousBlock> suspiciousBlocks;
    private ExecutorService scannerPool;
    private volatile boolean shouldScan;
    private long lastCleanup;

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkFinder$ChunkAnalysis.class */
    private static class ChunkAnalysis {
        int deepslateCount = 0;
        int cobbledDeepslateCount = 0;
        int rotatedDeepslateCount = 0;
        int endStoneCount = 0;
        int trialChamberCount = 0;

        private ChunkAnalysis() {
        }
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkFinder$Mode.class */
    public enum Mode {
        Chat,
        Toast,
        Both
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkFinder$SuspiciousBlock.class */
    private static class SuspiciousBlock {
        final SuspiciousBlockType type;
        final long detectedTime;

        SuspiciousBlock(SuspiciousBlockType param1, long param2) {
            this.type = param1;
            this.detectedTime = param2;
        }
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkFinder$SuspiciousBlockType.class */
    private enum SuspiciousBlockType {
        DEEPSLATE,
        COBBLED_DEEPSLATE,
        ROTATED_DEEPSLATE,
        END_STONE
    }

    public ChunkFinder() {
        super(KingAddon.CATEGORY, "chunk-finder", "ChunkFinderV4");
        this.sgDetection = this.settings.createGroup("Detection");
        this.sgRender = this.settings.createGroup("Render");
        this.sgBlockHighlight = this.settings.createGroup("Block Highlighting");
        this.sgPerformance = this.settings.createGroup("Performance");
        this.sgNotifications = this.settings.createGroup("Notifications");
        this.detectDeepslate = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("detect-deepslate")).description("Find deepslate blocks")).defaultValue(false)).build());
        this.detectCobbledDeepslate = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("detect-cobbled-deepslate")).description("Find cobbled deepslate blocks")).defaultValue(true)).build());
        this.detectRotatedDeepslate = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("detect-rotated-deepslate")).description("Find rotated deepslate blocks")).defaultValue(true)).build());
        this.detectEndStone = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("detect-end-stone")).description("Find end stone blocks (disabled in The End dimension)")).defaultValue(true)).build());
        this.ignoreExposed = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("ignore-exposed")).description("Ignore suspicious blocks that are exposed to air or fluid (treats water/lava like air)")).defaultValue(true)).build());
        this.ignoreTrialChambers = this.sgDetection.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("ignore-trial-chambers")).description("Ignore chunks containing trial chambers (based on waxed copper blocks and tuff bricks)")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgDetection;
        IntSetting.Builder builderSliderRange = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("trial-chamber-threshold")).description("Minimum waxed copper or tuff brick blocks to identify a trial chamber")).defaultValue(50)).range(1, 50).sliderRange(1, 50);
        Setting<Boolean> setting = this.ignoreTrialChambers;
        Objects.requireNonNull(setting);
        this.trialChamberThreshold = settingGroup.add(((IntSetting.Builder) builderSliderRange.visible(setting::get)).build());
        SettingGroup settingGroup2 = this.sgDetection;
        IntSetting.Builder builderSliderRange2 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("deepslate-threshold")).description("Min deepslate to flag chunk")).defaultValue(1)).range(1, 15).sliderRange(1, 15);
        Setting<Boolean> setting2 = this.detectDeepslate;
        Objects.requireNonNull(setting2);
        this.deepslateThreshold = settingGroup2.add(((IntSetting.Builder) builderSliderRange2.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgDetection;
        IntSetting.Builder builderSliderRange3 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("cobbled-deepslate-threshold")).description("Min cobbled deepslate to flag chunk")).defaultValue(4)).range(1, 15).sliderRange(1, 15);
        Setting<Boolean> setting3 = this.detectCobbledDeepslate;
        Objects.requireNonNull(setting3);
        this.cobbledDeepslateThreshold = settingGroup3.add(((IntSetting.Builder) builderSliderRange3.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgDetection;
        IntSetting.Builder builderSliderRange4 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("rotated-threshold")).description("Min rotated deepslate to flag chunk")).defaultValue(3)).range(1, 20).sliderRange(1, 20);
        Setting<Boolean> setting4 = this.detectRotatedDeepslate;
        Objects.requireNonNull(setting4);
        this.rotatedDeepslateThreshold = settingGroup4.add(((IntSetting.Builder) builderSliderRange4.visible(setting4::get)).build());
        SettingGroup settingGroup5 = this.sgDetection;
        IntSetting.Builder builderSliderRange5 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("end-stone-threshold")).description("Min end stone count to flag chunk")).defaultValue(2)).range(1, 15).sliderRange(1, 15);
        Setting<Boolean> setting5 = this.detectEndStone;
        Objects.requireNonNull(setting5);
        this.endStoneThreshold = settingGroup5.add(((IntSetting.Builder) builderSliderRange5.visible(setting5::get)).build());
        this.renderY = this.sgRender.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("render-height")).description("Height to render chunk highlights")).defaultValue(64.0d).range(-64.0d, 320.0d).sliderRange(-64.0d, 320.0d).build());
        this.renderMode = this.sgRender.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("render-mode")).description("How to render highlighted chunks")).defaultValue(ShapeMode.Both)).build());
        this.chunkColor = this.sgRender.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("chunk-color")).description("Color for suspicious chunks")).defaultValue(new SettingColor(255, 215, 0, 120)).build());
        this.thickness = this.sgRender.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("thickness")).description("Thickness of highlight box")).defaultValue(0.3d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d).build());
        this.highlightBlocks = this.sgBlockHighlight.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("highlight-blocks")).description("Highlight individual suspicious blocks")).defaultValue(true)).build());
        SettingGroup settingGroup6 = this.sgBlockHighlight;
        IntSetting.Builder builderSliderRange6 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-blocks-render")).description("Maximum number of blocks to highlight (performance)")).defaultValue(200)).range(50, 1000).sliderRange(50, 1000);
        Setting<Boolean> setting6 = this.highlightBlocks;
        Objects.requireNonNull(setting6);
        this.maxBlocksToRender = settingGroup6.add(((IntSetting.Builder) builderSliderRange6.visible(setting6::get)).build());
        SettingGroup settingGroup7 = this.sgBlockHighlight;
        EnumSetting.Builder builder = (EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("block-render-mode")).description("How to render individual blocks")).defaultValue(ShapeMode.Lines);
        Setting<Boolean> setting7 = this.highlightBlocks;
        Objects.requireNonNull(setting7);
        this.blockRenderMode = settingGroup7.add(((EnumSetting.Builder) builder.visible(setting7::get)).build());
        SettingGroup settingGroup8 = this.sgBlockHighlight;
        ColorSetting.Builder builderDefaultValue = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("deepslate-color")).description("Color for deepslate blocks")).defaultValue(new SettingColor(100, 100, 100, 200));
        Setting<Boolean> setting8 = this.highlightBlocks;
        Objects.requireNonNull(setting8);
        this.deepslateBlockColor = settingGroup8.add(((ColorSetting.Builder) builderDefaultValue.visible(setting8::get)).build());
        SettingGroup settingGroup9 = this.sgBlockHighlight;
        ColorSetting.Builder builderDefaultValue2 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("cobbled-deepslate-color")).description("Color for cobbled deepslate blocks")).defaultValue(new SettingColor(80, 80, 80, 200));
        Setting<Boolean> setting9 = this.highlightBlocks;
        Objects.requireNonNull(setting9);
        this.cobbledDeepslateBlockColor = settingGroup9.add(((ColorSetting.Builder) builderDefaultValue2.visible(setting9::get)).build());
        SettingGroup settingGroup10 = this.sgBlockHighlight;
        ColorSetting.Builder builderDefaultValue3 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("rotated-deepslate-color")).description("Color for rotated deepslate blocks")).defaultValue(new SettingColor(120, 0, 120, 200));
        Setting<Boolean> setting10 = this.highlightBlocks;
        Objects.requireNonNull(setting10);
        this.rotatedDeepslateBlockColor = settingGroup10.add(((ColorSetting.Builder) builderDefaultValue3.visible(setting10::get)).build());
        SettingGroup settingGroup11 = this.sgBlockHighlight;
        ColorSetting.Builder builderDefaultValue4 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("end-stone-color")).description("Color for end stone blocks")).defaultValue(new SettingColor(255, 255, 200, 200));
        Setting<Boolean> setting11 = this.highlightBlocks;
        Objects.requireNonNull(setting11);
        this.endStoneBlockColor = settingGroup11.add(((ColorSetting.Builder) builderDefaultValue4.visible(setting11::get)).build());
        this.useMultiThreading = this.sgPerformance.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("threading")).description("Use background threads for scanning")).defaultValue(true)).build());
        SettingGroup settingGroup12 = this.sgPerformance;
        IntSetting.Builder builderSliderRange7 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("thread-count")).description("Number of worker threads")).defaultValue(Integer.valueOf(Math.max(1, Runtime.getRuntime().availableProcessors() / 2)))).range(1, 4).sliderRange(1, 4);
        Setting<Boolean> setting12 = this.useMultiThreading;
        Objects.requireNonNull(setting12);
        this.threadCount = settingGroup12.add(((IntSetting.Builder) builderSliderRange7.visible(setting12::get)).build());
        this.scanInterval = this.sgPerformance.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("scan-delay")).description("Milliseconds between scans")).defaultValue(100)).range(50, 2000).sliderRange(50, 2000).build());
        this.maxConcurrentScans = this.sgPerformance.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-concurrent-scans")).description("Max chunks scanned simultaneously")).defaultValue(3)).range(1, 8).sliderRange(1, 8).build());
        this.cleanupInterval = this.sgPerformance.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("cleanup-interval")).description("Seconds between distant chunk cleanup")).defaultValue(30)).range(15, 300).sliderRange(15, 300).build());
        this.notificationMode = this.sgNotifications.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("notification-mode")).description("How to notify when suspicious chunks are detected")).defaultValue(Mode.Both)).build());
        this.playSound = this.sgNotifications.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound-alerts")).description("Play sound when suspicious chunks or blocks are found")).defaultValue(true)).build());
        this.chatAlerts = this.sgNotifications.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chat-alerts")).description("Send chat notifications for suspicious chunks or blocks")).defaultValue(true)).build());
        this.trialChamberAlerts = this.sgNotifications.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("trial-chamber-alerts")).description("Send chat notifications for trial chambers")).defaultValue(false)).build());
        this.maxAlerts = this.sgNotifications.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-alerts")).description("Max alerts per minute")).defaultValue(5)).range(1, 20).sliderRange(1, 20).build());
        this.flaggedChunks = ConcurrentHashMap.newKeySet();
        this.chunkData = new ConcurrentHashMap<>();
        this.scannedChunks = ConcurrentHashMap.newKeySet();
        this.notificationTimes = new ConcurrentHashMap<>();
        this.recentAlerts = new ConcurrentLinkedQueue();
        this.activeScanCount = new AtomicLong(0L);
        this.suspiciousBlocks = new ConcurrentHashMap();
        this.shouldScan = false;
        this.lastCleanup = 0L;
    }

    public void onActivate() {
        if (this.mc.field_1687 == null) {
            return;
        }
        clearAll();
        this.shouldScan = true;
        this.lastCleanup = System.currentTimeMillis();
        if (!((Boolean) this.useMultiThreading.get()).booleanValue()) {
            startInitialScan();
        } else {
            this.scannerPool = Executors.newFixedThreadPool(((Integer) this.threadCount.get()).intValue(), param0 -> {
                Thread thread = new Thread(param0, "ChunkFinder-Worker");
                thread.setDaemon(true);
                thread.setPriority(4);
                return thread;
            });
            startInitialScan();
        }
    }

    public void onDeactivate() {
        this.shouldScan = false;
        if (this.scannerPool != null) {
            this.scannerPool.shutdownNow();
            this.scannerPool = null;
        }
        clearAll();
    }

    private void clearAll() {
        this.flaggedChunks.clear();
        this.chunkData.clear();
        this.scannedChunks.clear();
        this.notificationTimes.clear();
        this.recentAlerts.clear();
        this.suspiciousBlocks.clear();
        this.activeScanCount.set(0L);
    }

    @EventHandler
    private void onTick(TickEvent.Pre param1) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return;
        }
        long v2 = System.currentTimeMillis();
        while (!this.recentAlerts.isEmpty() && v2 - this.recentAlerts.peek().longValue() > 60000) {
            this.recentAlerts.poll();
        }
        if (v2 - this.lastCleanup > ((long) ((Integer) this.cleanupInterval.get()).intValue()) * 1000) {
            performCleanup();
            this.lastCleanup = v2;
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkDataEvent param1) {
        if (!this.shouldScan || this.activeScanCount.get() >= ((Integer) this.maxConcurrentScans.get()).intValue()) {
            return;
        }
        Object v2 = param1.chunk().method_12004();
        if (this.scannedChunks.contains(v2)) {
            return;
        }
        scheduleChunkScan(param1.chunk());
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent param1) {
        if (this.shouldScan) {
            class_2338 class_2338Var = param1.pos;
            if (class_2338Var.method_10264() < 0 || class_2338Var.method_10264() > 128) {
                return;
            }
            class_2680 class_2680Var = param1.newState;
            if (isRelevantBlock(class_2680Var) || class_2680Var.method_26215()) {
                class_1923 class_1923Var = new class_1923(class_2338Var);
                scheduleChunkScan(this.mc.field_1687.method_8497(class_1923Var.field_9181, class_1923Var.field_9180));
            }
        }
    }

    private boolean isRelevantBlock(class_2680 param1) {
        Object v2 = param1.method_26204();
        return v2 == class_2246.field_28888 || v2 == class_2246.field_29031 || v2 == class_2246.field_28892 || v2 == class_2246.field_28900 || v2 == class_2246.field_28896 || v2 == class_2246.field_28904 || v2 == class_2246.field_10471 || v2 == class_2246.field_27133 || v2 == class_2246.field_33407 || v2 == class_2246.field_47035;
    }

    private void startInitialScan() {
        Runnable runnable = () -> {
            try {
                for (Object v2 : Utils.chunks()) {
                    if (!this.shouldScan) {
                        break;
                    }
                    if (v2 instanceof class_2818) {
                        class_2818 class_2818Var = (class_2818) v2;
                        if (this.activeScanCount.get() < ((Integer) this.maxConcurrentScans.get()).intValue()) {
                            if (!((Boolean) this.useMultiThreading.get()).booleanValue() || this.scannerPool == null) {
                                analyzeChunk(class_2818Var);
                            } else {
                                this.scannerPool.submit(() -> {
                                    analyzeChunk(class_2818Var);
                                });
                            }
                        }
                        Thread.sleep(((Integer) this.scanInterval.get()).intValue());
                    }
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        };
        if (!((Boolean) this.useMultiThreading.get()).booleanValue() || this.scannerPool == null) {
            new Thread(runnable, "ChunkFinder-Initial").start();
        } else {
            this.scannerPool.submit(runnable);
        }
    }

    private void scheduleChunkScan(class_2791 param1) {
        if (param1 instanceof class_2818) {
            class_2818 class_2818Var = (class_2818) param1;
            if (this.activeScanCount.get() >= ((Integer) this.maxConcurrentScans.get()).intValue()) {
                return;
            }
            Runnable runnable = () -> {
                try {
                    Thread.sleep(((Integer) this.scanInterval.get()).intValue() / 2);
                    analyzeChunk(class_2818Var);
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            };
            if (!((Boolean) this.useMultiThreading.get()).booleanValue() || this.scannerPool == null) {
                new Thread(runnable, "ChunkFinder-Scan").start();
            } else {
                this.scannerPool.submit(runnable);
            }
        }
    }

    private void analyzeChunk(class_2818 param1) {
        if (!this.shouldScan || param1 == null) {
            return;
        }
        class_1923 class_1923VarMethod_12004 = param1.method_12004();
        if (this.scannedChunks.contains(class_1923VarMethod_12004)) {
            return;
        }
        this.activeScanCount.incrementAndGet();
        try {
            this.scannedChunks.add(class_1923VarMethod_12004);
            int v4 = Math.min(param1.method_31607() + param1.method_31605(), 128);
            ChunkAnalysis chunkAnalysis = new ChunkAnalysis();
            scanChunkSections(param1, chunkAnalysis, 0, v4);
            this.chunkData.put(class_1923VarMethod_12004, chunkAnalysis);
            evaluateChunk(class_1923VarMethod_12004, chunkAnalysis);
        } finally {
            this.activeScanCount.decrementAndGet();
        }
    }

    private void scanChunkSections(class_2818 param1, ChunkAnalysis param2, int param3, int param4) {
        class_2826[] class_2826VarArrMethod_12006 = param1.method_12006();
        for (int v6 = 0; v6 < class_2826VarArrMethod_12006.length && this.shouldScan; v6++) {
            class_2826 class_2826Var = class_2826VarArrMethod_12006[v6];
            if (class_2826Var != null && !class_2826Var.method_38292()) {
                int v8 = param1.method_31607() + (v6 * 16);
                int v9 = Math.max(0, param3 - v8);
                int v10 = Math.min(15, param4 - v8);
                if (v9 <= 15 && v10 >= 0) {
                    for (int v11 = 0; v11 < 16; v11++) {
                        for (int v12 = 0; v12 < 16; v12++) {
                            for (int v13 = v9; v13 <= v10; v13++) {
                                if (!this.shouldScan) {
                                    return;
                                }
                                class_2680 class_2680VarMethod_12254 = class_2826Var.method_12254(v11, v13, v12);
                                int v15 = v8 + v13;
                                analyzeBlock(new class_2338(param1.method_12004().method_8326() + v11, v15, param1.method_12004().method_8328() + v12), class_2680VarMethod_12254, v15, param2);
                            }
                        }
                    }
                }
            }
        }
    }

    private void analyzeBlock(class_2338 param1, class_2680 param2, int param3, ChunkAnalysis param4) {
        SuspiciousBlockType suspiciousBlockType = null;
        if (((Boolean) this.ignoreTrialChambers.get()).booleanValue() && isTrialChamberBlock(param2)) {
            param4.trialChamberCount++;
        }
        boolean zIsExposedToAirOrFluid = false;
        if (((Boolean) this.ignoreExposed.get()).booleanValue()) {
            zIsExposedToAirOrFluid = isExposedToAirOrFluid(param1);
        }
        if (((Boolean) this.detectDeepslate.get()).booleanValue() && isNormalDeepslate(param2) && !zIsExposedToAirOrFluid && !isInLargeDeepslateLine(param1, param3)) {
            param4.deepslateCount++;
            suspiciousBlockType = SuspiciousBlockType.DEEPSLATE;
        }
        if (((Boolean) this.detectRotatedDeepslate.get()).booleanValue() && isRotatedDeepslateBlock(param2) && !zIsExposedToAirOrFluid) {
            param4.rotatedDeepslateCount++;
            suspiciousBlockType = SuspiciousBlockType.ROTATED_DEEPSLATE;
        }
        if (((Boolean) this.detectCobbledDeepslate.get()).booleanValue() && isCobbledDeepslate(param2) && !zIsExposedToAirOrFluid) {
            param4.cobbledDeepslateCount++;
            suspiciousBlockType = SuspiciousBlockType.COBBLED_DEEPSLATE;
        }
        if (((Boolean) this.detectEndStone.get()).booleanValue() && isEndStone(param2) && this.mc.field_1687.method_27983() != class_1937.field_25181 && !zIsExposedToAirOrFluid) {
            param4.endStoneCount++;
            suspiciousBlockType = SuspiciousBlockType.END_STONE;
        }
        if (suspiciousBlockType == null || !((Boolean) this.highlightBlocks.get()).booleanValue()) {
            return;
        }
        this.suspiciousBlocks.put(param1, new SuspiciousBlock(suspiciousBlockType, System.currentTimeMillis()));
    }

    private boolean isValidBlockPos(class_2338 param1) {
        return param1.method_10264() >= this.mc.field_1687.method_31607() && param1.method_10264() < this.mc.field_1687.method_31605();
    }

    private boolean isExposedToAirOrFluid(class_2338 param1) {
        if (this.mc.field_1687 == null) {
            return false;
        }
        for (class_2350 class_2350Var : class_2350.values()) {
            class_2338 class_2338VarMethod_10093 = param1.method_10093(class_2350Var);
            if (isValidBlockPos(class_2338VarMethod_10093)) {
                class_2680 class_2680VarMethod_8320 = this.mc.field_1687.method_8320(class_2338VarMethod_10093);
                if (class_2680VarMethod_8320.method_26215()) {
                    return true;
                }
                class_3610 class_3610VarMethod_26227 = class_2680VarMethod_8320.method_26227();
                if (class_3610VarMethod_26227 != null && !class_3610VarMethod_26227.method_15769()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInLargeDeepslateLine(class_2338 param1, int param2) {
        if (this.mc.field_1687 == null) {
            return false;
        }
        int v3 = param2 > -8 ? 50 : 20;
        int v4 = 1;
        for (int v5 = 1; v5 < v3; v5++) {
            class_2338 class_2338VarMethod_10079 = param1.method_10079(class_2350.field_11034, v5);
            if (!isValidBlockPos(class_2338VarMethod_10079) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_10079))) {
                break;
            }
            v4++;
        }
        for (int v52 = 1; v52 < v3; v52++) {
            class_2338 class_2338VarMethod_100792 = param1.method_10079(class_2350.field_11039, v52);
            if (!isValidBlockPos(class_2338VarMethod_100792) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_100792))) {
                break;
            }
            v4++;
        }
        if (v4 >= v3) {
            return true;
        }
        int v53 = 1;
        for (int i = 1; i < v3; i++) {
            class_2338 class_2338VarMethod_100793 = param1.method_10079(class_2350.field_11035, i);
            if (!isValidBlockPos(class_2338VarMethod_100793) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_100793))) {
                break;
            }
            v53++;
        }
        for (int i2 = 1; i2 < v3; i2++) {
            class_2338 class_2338VarMethod_100794 = param1.method_10079(class_2350.field_11043, i2);
            if (!isValidBlockPos(class_2338VarMethod_100794) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_100794))) {
                break;
            }
            v53++;
        }
        if (v53 >= v3) {
            return true;
        }
        if (param2 <= 0) {
            return false;
        }
        int i3 = 1;
        for (int i4 = 1; i4 < v3; i4++) {
            class_2338 class_2338VarMethod_100795 = param1.method_10079(class_2350.field_11036, i4);
            if (!isValidBlockPos(class_2338VarMethod_100795) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_100795))) {
                break;
            }
            i3++;
        }
        for (int i5 = 1; i5 < v3; i5++) {
            class_2338 class_2338VarMethod_100796 = param1.method_10079(class_2350.field_11033, i5);
            if (!isValidBlockPos(class_2338VarMethod_100796) || !isNormalDeepslate(this.mc.field_1687.method_8320(class_2338VarMethod_100796))) {
                break;
            }
            i3++;
        }
        return i3 >= v3;
    }

    private void evaluateChunk(class_1923 param1, ChunkAnalysis param2) {
        if (((Boolean) this.ignoreTrialChambers.get()).booleanValue() && param2.trialChamberCount >= ((Integer) this.trialChamberThreshold.get()).intValue()) {
            if (((Boolean) this.trialChamberAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
                notifyTrialChamber(String.format("ChunkFinder [%d, %d] - Trial chamber detected - Copper/Tuff blocks: %d", Integer.valueOf(param1.field_9181), Integer.valueOf(param1.field_9180), Integer.valueOf(param2.trialChamberCount)));
            }
            this.flaggedChunks.remove(param1);
            this.notificationTimes.remove(param1);
            return;
        }
        boolean z = false;
        StringBuilder sb = new StringBuilder();
        if (((Boolean) this.detectDeepslate.get()).booleanValue() && param2.deepslateCount >= ((Integer) this.deepslateThreshold.get()).intValue()) {
            z = true;
            sb.append("Deepslate[").append(param2.deepslateCount).append("] ");
        }
        if (((Boolean) this.detectCobbledDeepslate.get()).booleanValue() && param2.cobbledDeepslateCount >= ((Integer) this.cobbledDeepslateThreshold.get()).intValue()) {
            z = true;
            sb.append("CobbledDeepslate[").append(param2.cobbledDeepslateCount).append("] ");
        }
        if (((Boolean) this.detectRotatedDeepslate.get()).booleanValue() && param2.rotatedDeepslateCount >= ((Integer) this.rotatedDeepslateThreshold.get()).intValue()) {
            z = true;
            sb.append("RotatedDeepslate[").append(param2.rotatedDeepslateCount).append("] ");
        }
        if (((Boolean) this.detectEndStone.get()).booleanValue() && param2.endStoneCount >= ((Integer) this.endStoneThreshold.get()).intValue()) {
            z = true;
            sb.append("EndStone[").append(param2.endStoneCount).append("] ");
        }
        if (!z) {
            this.flaggedChunks.remove(param1);
            this.notificationTimes.remove(param1);
        } else if (this.flaggedChunks.add(param1)) {
            notifyChunkFound(param1, sb.toString().trim());
        }
    }

    private boolean isNormalDeepslate(class_2680 param1) {
        Object v2 = param1.method_26204();
        if (v2 != class_2246.field_28888 || !param1.method_28498(class_2741.field_12496)) {
            return false;
        }
        Object v3 = (class_2350.class_2351) param1.method_11654(class_2741.field_12496);
        return v3 == class_2350.class_2351.field_11052;
    }

    private boolean isCobbledDeepslate(class_2680 param1) {
        return param1.method_26204() == class_2246.field_29031;
    }

    private boolean isRotatedDeepslateBlock(class_2680 param1) {
        Object v2 = param1.method_26204();
        if (v2 != class_2246.field_28888 || !param1.method_28498(class_2741.field_12496)) {
            return false;
        }
        Object v3 = (class_2350.class_2351) param1.method_11654(class_2741.field_12496);
        return v3 != class_2350.class_2351.field_11052;
    }

    private boolean isEndStone(class_2680 param1) {
        return param1.method_26204() == class_2246.field_10471;
    }

    private boolean isTrialChamberBlock(class_2680 param1) {
        Object v2 = param1.method_26204();
        return v2 == class_2246.field_27133 || v2 == class_2246.field_33407 || v2 == class_2246.field_47035;
    }

    private void notifyChunkFound(class_1923 param1, String param2) {
        long v3 = System.currentTimeMillis();
        if (this.recentAlerts.size() >= ((Integer) this.maxAlerts.get()).intValue()) {
            return;
        }
        Long l = this.notificationTimes.get(param1);
        if (l == null || v3 - l.longValue() >= 45000) {
            String str = String.format("ChunkFinder [%d, %d] - Suspicious chunk detected - %s", Integer.valueOf(param1.field_9181), Integer.valueOf(param1.field_9180), param2);
            this.mc.execute(() -> {
                switch ((Mode) this.notificationMode.get()) {
                    case Chat:
                        if (((Boolean) this.chatAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
                            this.mc.field_1724.method_7353(class_2561.method_43470(str), false);
                        }
                        break;
                    case Toast:
                        this.mc.method_1566().method_1999(new MeteorToast.Builder("ChunkFinder").text(str).icon(class_1802.field_8106).build());
                        break;
                    case Both:
                        if (((Boolean) this.chatAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
                            this.mc.field_1724.method_7353(class_2561.method_43470(str), false);
                        }
                        this.mc.method_1566().method_1999(new MeteorToast.Builder("ChunkFinder").text(str).icon(class_1802.field_8106).build());
                        break;
                }
                if (((Boolean) this.playSound.get()).booleanValue()) {
                    this.mc.method_1483().method_4873(class_1109.method_4758(class_3417.field_14627, 1.5f));
                }
                this.recentAlerts.offer(Long.valueOf(v3));
                this.notificationTimes.put(param1, Long.valueOf(v3));
            });
        }
    }

    private void notifyTrialChamber(String param1) {
        long v2 = System.currentTimeMillis();
        if (this.recentAlerts.size() >= ((Integer) this.maxAlerts.get()).intValue()) {
            return;
        }
        String[] strArrSplit = param1.split(" - ", 2);
        String strReplace = strArrSplit[0].replace("ChunkFinder ", "");
        String str = strArrSplit.length > 1 ? strArrSplit[1] : "";
        this.mc.execute(() -> {
            switch ((Mode) this.notificationMode.get()) {
                case Chat:
                    if (((Boolean) this.trialChamberAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
                        this.mc.field_1724.method_7353(class_2561.method_43470(param1), false);
                    }
                    break;
                case Toast:
                    this.mc.method_1566().method_1999(new MeteorToast.Builder("ChunkFinder").text(String.format("%s - %s", strReplace, str)).icon(class_1802.field_8106).build());
                    break;
                case Both:
                    if (((Boolean) this.trialChamberAlerts.get()).booleanValue() && this.mc.field_1724 != null) {
                        this.mc.field_1724.method_7353(class_2561.method_43470(param1), false);
                    }
                    this.mc.method_1566().method_1999(new MeteorToast.Builder("ChunkFinder").text(String.format("%s - %s", strReplace, str)).icon(class_1802.field_8106).build());
                    break;
            }
            if (((Boolean) this.playSound.get()).booleanValue()) {
                this.mc.method_1483().method_4873(class_1109.method_4758(class_3417.field_14627, 1.5f));
            }
            this.recentAlerts.offer(Long.valueOf(v2));
        });
    }

    private void performCleanup() {
        if (this.mc.field_1724 == null) {
            return;
        }
        int v1 = ((Integer) this.mc.field_1690.method_42503().method_41753()).intValue();
        int v2 = ((int) this.mc.field_1724.method_23317()) / 16;
        int v3 = ((int) this.mc.field_1724.method_23321()) / 16;
        this.flaggedChunks.removeIf(param4 -> {
            int v5 = Math.abs(param4.field_9181 - v2);
            int v6 = Math.abs(param4.field_9180 - v3);
            boolean z = v5 > v1 + 5 || v6 > v1 + 5;
            if (z) {
                this.chunkData.remove(param4);
                this.notificationTimes.remove(param4);
            }
            return z;
        });
        this.scannedChunks.removeIf(param3 -> {
            int v4 = Math.abs(param3.field_9181 - v2);
            int v5 = Math.abs(param3.field_9180 - v3);
            return v4 > v1 + 3 || v5 > v1 + 3;
        });
        this.suspiciousBlocks.entrySet().removeIf(param2 -> {
            double v4 = this.mc.field_1724.method_73189().method_1022(class_243.method_24953((class_2338) param2.getKey()));
            return v4 > ((double) ((v1 * 16) + 80));
        });
    }

    @EventHandler
    private void onRender3D(Render3DEvent param1) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (!this.flaggedChunks.isEmpty()) {
            Color color = new Color((Color) this.chunkColor.get());
            int v3 = 0;
            for (class_1923 class_1923Var : this.flaggedChunks) {
                int i = v3;
                v3++;
                if (i > 50) {
                    break;
                } else {
                    renderChunkHighlight(param1, class_1923Var, color);
                }
            }
        }
        if (((Boolean) this.highlightBlocks.get()).booleanValue()) {
            renderSuspiciousBlocks(param1);
        }
    }

    private void renderChunkHighlight(Render3DEvent param1, class_1923 param2, Color param3) {
        int v4 = param2.method_8326();
        int v5 = param2.method_8328();
        int v6 = param2.method_8327();
        int v7 = param2.method_8329();
        double v8 = ((Double) this.renderY.get()).doubleValue();
        double v10 = ((Double) this.thickness.get()).doubleValue();
        param1.renderer.box(new class_238(v4, v8, v5, v6 + 1, v8 + v10, v7 + 1), param3, param3, (ShapeMode) this.renderMode.get(), 0);
    }

    private void renderSuspiciousBlocks(Render3DEvent param1) {
        Color colorForBlockType;
        int v2 = 0;
        for (Map.Entry<class_2338, SuspiciousBlock> entry : this.suspiciousBlocks.entrySet()) {
            if (v2 >= ((Integer) this.maxBlocksToRender.get()).intValue()) {
                return;
            }
            class_2338 key = entry.getKey();
            SuspiciousBlock value = entry.getValue();
            double v7 = this.mc.field_1724.method_73189().method_1022(class_243.method_24953(key));
            if (v7 <= ((Integer) this.mc.field_1690.method_42503().method_41753()).intValue() * 16 && (colorForBlockType = getColorForBlockType(value.type)) != null) {
                param1.renderer.box(new class_238(key), colorForBlockType, colorForBlockType, (ShapeMode) this.blockRenderMode.get(), 0);
                v2++;
            }
        }
    }

    private Color getColorForBlockType(SuspiciousBlockType param1) {
        switch (param1) {
            case DEEPSLATE:
                return new Color((Color) this.deepslateBlockColor.get());
            case COBBLED_DEEPSLATE:
                return new Color((Color) this.cobbledDeepslateBlockColor.get());
            case ROTATED_DEEPSLATE:
                return new Color((Color) this.rotatedDeepslateBlockColor.get());
            case END_STONE:
                return new Color((Color) this.endStoneBlockColor.get());
            default:
                return null;
        }
    }

    public String getInfoString() {
        return ((Boolean) this.highlightBlocks.get()).booleanValue() ? String.format("C:%d B:%d", Integer.valueOf(this.flaggedChunks.size()), Integer.valueOf(this.suspiciousBlocks.size())) : String.valueOf(this.flaggedChunks.size());
    }
}
