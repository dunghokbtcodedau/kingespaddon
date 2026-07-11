package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
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
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1944;
import net.minecraft.class_238;
import net.minecraft.class_2561;
import net.minecraft.class_2791;
import net.minecraft.class_2804;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_4076;
import net.minecraft.class_638;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/AmethystClusterEsp.class */
public class AmethystClusterEsp extends KingModule {
    private static final int RENDER_Y = 63;
    private static final double CHUNK_BOX_HEIGHT = 0.05d;
    private static final int LIGHT_MIN = 5;
    private static final int LIGHT_MAX = 5;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNotify;
    private final SettingGroup sgRender;
    private final Setting<Integer> chunkRange;
    private final Setting<Integer> scanInterval;
    private final Setting<Integer> chunksPerBatch;
    private final Setting<Integer> maxScanTimeMs;
    private final Setting<Integer> minLightPoints;
    private final Setting<Integer> maxLightPoints;
    private final Setting<Integer> yMin;
    private final Setting<Integer> yMax;
    private final Setting<Boolean> notify;
    private final Setting<Boolean> sound;
    private final Setting<Double> soundVolume;
    private final Setting<Integer> notifyCooldown;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<ShapeMode> shapeMode;
    private final LongSet markedChunks;
    private final Long2IntMap detectionCounts;
    private final Long2IntMap lastNotifiedCount;
    private final Long2IntMap lastNotifyTime;
    private final List<class_1923> scanQueue;
    private int tickCounter;
    private int queueIndex;
    private int totalDetections;

    public AmethystClusterEsp() {
        super(KingAddon.CATEGORY, "amethyst-cluster-esp", "Detects amethyst geodes via light signature (bypasses DonutSMP cluster hiding).");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNotify = this.settings.createGroup("Notify");
        this.sgRender = this.settings.createGroup("Render");
        this.chunkRange = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("chunk-range")).description("Chunk radius to scan around you.")).defaultValue(6)).range(2, 16).sliderRange(2, 16).build());
        this.scanInterval = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("scan-interval")).description("Ticks between chunk scan batches.")).defaultValue(1)).range(1, 20).sliderRange(1, 20).build());
        this.chunksPerBatch = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("chunks-per-batch")).description("Chunks scanned per batch.")).defaultValue(8)).range(1, 64).sliderRange(1, 64).build());
        this.maxScanTimeMs = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-scan-time-ms")).description("Max milliseconds spent scanning per tick.")).defaultValue(5)).range(1, 15).sliderRange(1, 15).build());
        this.minLightPoints = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("min-light-points")).description("Minimum amethyst-range lit air blocks in a chunk to mark it.")).defaultValue(8)).min(1).sliderRange(1, 100).build());
        this.maxLightPoints = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("max-light-points")).description("Maximum light points to mark a chunk (filters noise from lava bleed).")).defaultValue(500)).min(1).sliderRange(1, 500).build());
        this.yMin = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("y-min")).description("Minimum Y level to scan.")).defaultValue(-64)).range(-64, 320).sliderRange(-64, 64).build());
        this.yMax = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("y-max")).description("Maximum Y level to scan.")).defaultValue(30)).range(-64, 320).sliderRange(0, 320).build());
        this.notify = this.sgNotify.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("notify")).description("Show a notification when amethyst light is detected.")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgNotify;
        BoolSetting.Builder builder = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound")).description("Play a sound with notifications.")).defaultValue(true);
        Setting<Boolean> setting = this.notify;
        Objects.requireNonNull(setting);
        Objects.requireNonNull(setting);
        this.sound = settingGroup.add(((BoolSetting.Builder) builder.visible(setting::get)).build());
        this.soundVolume = this.sgNotify.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-volume")).description("Notification sound volume.")).defaultValue(1.0d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d).visible(() -> {
            return ((Boolean) this.notify.get()).booleanValue() && ((Boolean) this.sound.get()).booleanValue();
        })).build());
        SettingGroup settingGroup2 = this.sgNotify;
        IntSetting.Builder builderSliderRange = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("notify-cooldown")).description("Seconds before the same chunk can notify again.")).defaultValue(30)).range(5, 120).sliderRange(5, 120);
        Setting<Boolean> setting2 = this.notify;
        Objects.requireNonNull(setting2);
        Objects.requireNonNull(setting2);
        this.notifyCooldown = settingGroup2.add(((IntSetting.Builder) builderSliderRange.visible(setting2::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("side-color")).description("Fill color for chunk markers.")).defaultValue(new SettingColor(255, 105, 180, 35)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("line-color")).description("Outline color for chunk markers.")).defaultValue(new SettingColor(255, 105, 180, 255)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("shape-mode")).description("Lines is fastest.")).defaultValue(ShapeMode.Lines)).build());
        this.markedChunks = new LongOpenHashSet();
        this.detectionCounts = new Long2IntOpenHashMap();
        this.lastNotifiedCount = new Long2IntOpenHashMap();
        this.lastNotifyTime = new Long2IntOpenHashMap();
        this.scanQueue = new ArrayList();
        this.detectionCounts.defaultReturnValue(0);
        this.lastNotifiedCount.defaultReturnValue(-1);
        this.lastNotifyTime.defaultReturnValue(0);
    }

    public void onActivate() {
        this.markedChunks.clear();
        this.detectionCounts.clear();
        this.lastNotifiedCount.clear();
        this.lastNotifyTime.clear();
        this.scanQueue.clear();
        this.tickCounter = 0;
        this.queueIndex = 0;
        this.totalDetections = 0;
        rebuildScanQueue();
    }

    public void onDeactivate() {
        this.markedChunks.clear();
        this.detectionCounts.clear();
        this.scanQueue.clear();
        this.totalDetections = 0;
    }

    public String getInfoString() {
        int v1 = this.markedChunks.size();
        return v1 + "c/" + this.totalDetections;
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        int i = this.tickCounter;
        this.tickCounter = i + 1;
        if (i >= ((Integer) this.scanInterval.get()).intValue()) {
            this.tickCounter = 0;
            if (this.scanQueue.isEmpty()) {
                rebuildScanQueue();
            }
            if (this.scanQueue.isEmpty()) {
                return;
            }
            class_638 class_638Var = this.mc.field_1687;
            int v3 = ((Integer) this.chunksPerBatch.get()).intValue();
            int v4 = (int) (System.currentTimeMillis() / 1000);
            long v5 = System.nanoTime() + (((long) ((Integer) this.maxScanTimeMs.get()).intValue()) * 1000000);
            int v7 = 0;
            while (true) {
                if (v7 >= v3 || this.queueIndex >= this.scanQueue.size()) {
                    break;
                }
                class_1923 class_1923Var = this.scanQueue.get(this.queueIndex);
                if (class_638Var.method_8393(class_1923Var.field_9181, class_1923Var.field_9180)) {
                    class_2818 class_2818VarMethod_8497 = class_638Var.method_8497(class_1923Var.field_9181, class_1923Var.field_9180);
                    if (class_2818VarMethod_8497 instanceof class_2818) {
                        long v10 = class_1923.method_8331(class_1923Var.field_9181, class_1923Var.field_9180);
                        int v12 = scanChunkForLight(class_2818VarMethod_8497);
                        int v13 = (v12 < ((Integer) this.minLightPoints.get()).intValue() || v12 > ((Integer) this.maxLightPoints.get()).intValue()) ? 0 : 1;
                        if (v13 != 0) {
                            this.markedChunks.add(v10);
                            this.detectionCounts.put(v10, v12);
                        } else {
                            this.markedChunks.remove(v10);
                            this.detectionCounts.remove(v10);
                            this.lastNotifiedCount.remove(v10);
                        }
                        if (v13 != 0 && ((Boolean) this.notify.get()).booleanValue()) {
                            int v14 = this.lastNotifiedCount.get(v10);
                            int v15 = this.lastNotifyTime.get(v10);
                            int v16 = v14 != v12 ? 1 : 0;
                            int v17 = v4 - v15 >= ((Integer) this.notifyCooldown.get()).intValue() ? 1 : 0;
                            if (v16 != 0 && v17 != 0) {
                                this.lastNotifiedCount.put(v10, v12);
                                this.lastNotifyTime.put(v10, v4);
                                sendClusterNotification(class_1923Var, v12);
                            }
                        }
                        if (System.nanoTime() > v5) {
                            this.queueIndex++;
                            break;
                        }
                    } else {
                        continue;
                    }
                }
                v7++;
                this.queueIndex++;
            }
            if (this.queueIndex >= this.scanQueue.size()) {
                this.queueIndex = 0;
                rebuildScanQueue();
            }
            this.totalDetections = 0;
            IntIterator it = this.detectionCounts.values().iterator();
            while (it.hasNext()) {
                int v72 = it.next().intValue();
                this.totalDetections += v72;
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent param1) {
        if (this.markedChunks.isEmpty()) {
            return;
        }
        SettingColor settingColor = (SettingColor) this.sideColor.get();
        SettingColor settingColor2 = (SettingColor) this.lineColor.get();
        ShapeMode shapeMode = (ShapeMode) this.shapeMode.get();
        for (long v9 : this.markedChunks.toLongArray()) {
            int v11 = class_1923.method_8325(v9);
            int v12 = class_1923.method_8332(v9);
            int v13 = v11 << 4;
            int v14 = v12 << 4;
            param1.renderer.box(new class_238(v13, 63.0d, v14, v13 + 16, 63.05d, v14 + 16), settingColor, settingColor2, shapeMode, 0);
        }
    }

    private void rebuildScanQueue() {
        this.scanQueue.clear();
        if (this.mc.field_1724 != null) {
            class_1923 class_1923VarMethod_31476 = this.mc.field_1724.method_31476();
            int v2 = ((Integer) this.chunkRange.get()).intValue();
            for (int v3 = -v2; v3 <= v2; v3++) {
                for (int v4 = -v2; v4 <= v2; v4++) {
                    this.scanQueue.add(new class_1923(class_1923VarMethod_31476.field_9181 + v3, class_1923VarMethod_31476.field_9180 + v4));
                }
            }
        }
    }

    private int scanChunkForLight(class_2791 param1) {
        class_2804 class_2804VarMethod_15544;
        int v2 = 0;
        int v3 = ((Integer) this.yMin.get()).intValue();
        int v4 = ((Integer) this.yMax.get()).intValue();
        class_638 class_638Var = this.mc.field_1687;
        if (class_638Var == null) {
            return 0;
        }
        int v6 = param1.method_12004().field_9181;
        int v7 = param1.method_12004().field_9180;
        int v8 = param1.method_32891();
        int v9 = param1.method_31597();
        for (int v10 = v8; v10 < v9; v10++) {
            int v11 = v10 << 4;
            if (v11 + 15 >= v3 && v11 <= v4 && (class_2804VarMethod_15544 = class_638Var.method_22336().method_15562(class_1944.field_9282).method_15544(class_4076.method_18676(v6, v10, v7))) != null) {
                int v13 = v10 - v8;
                class_2826 class_2826VarMethod_38259 = param1.method_38259(v13);
                int v15 = (class_2826VarMethod_38259 == null || class_2826VarMethod_38259.method_38292()) ? 1 : 0;
                int v16 = Math.max(0, v3 - v11);
                int v17 = Math.min(15, v4 - v11);
                for (int v18 = v16; v18 <= v17; v18++) {
                    for (int v19 = 0; v19 < 16; v19++) {
                        for (int v20 = 0; v20 < 16; v20++) {
                            int v21 = class_2804VarMethod_15544.method_12139(v19, v18, v20);
                            if (v21 >= 5 && v21 <= 5) {
                                if (v15 != 0) {
                                    v2++;
                                } else if (class_2826VarMethod_38259.method_12254(v19, v18, v20).method_26215()) {
                                    v2++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return v2;
    }

    private void sendClusterNotification(class_1923 param1, int param2) {
        ChatUtils.sendMsg(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param0 -> {
            return param0.method_36139(3816016);
        }));
        ChatUtils.sendMsg(class_2561.method_43473().method_10852(class_2561.method_43470(" ◆ ").method_27694(param02 -> {
            return param02.method_36139(14514175).method_10982(true);
        })).method_10852(class_2561.method_43470("AMETHYST GEODE").method_27694(param03 -> {
            return param03.method_36139(14514175).method_10982(true);
        })).method_10852(class_2561.method_43470(" ◆").method_27694(param04 -> {
            return param04.method_36139(14514175).method_10982(true);
        })));
        ChatUtils.sendMsg(class_2561.method_43473().method_10852(class_2561.method_43470(" ▸ ").method_27694(param05 -> {
            return param05.method_36139(12290303);
        })).method_10852(class_2561.method_43470("Light points: ").method_27694(param06 -> {
            return param06.method_36139(8952234);
        })).method_10852(class_2561.method_43470(String.valueOf(param2)).method_27694(param07 -> {
            return param07.method_36139(14514175).method_10982(true);
        })).method_10852(class_2561.method_43470("  •  ").method_27694(param08 -> {
            return param08.method_36139(5592422);
        })).method_10852(class_2561.method_43470("Chunk ").method_27694(param09 -> {
            return param09.method_36139(8952234);
        })).method_10852(class_2561.method_43470("[" + param1.field_9181 + ", " + param1.field_9180 + "]").method_27694(param010 -> {
            return param010.method_36139(16777215);
        })));
        ChatUtils.sendMsg(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param011 -> {
            return param011.method_36139(3816016);
        }));
        if (!((Boolean) this.sound.get()).booleanValue() || this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        float v5 = ((Double) this.soundVolume.get()).floatValue();
        float v6 = Math.min(1.2f, 0.85f + (param2 * 0.03f));
        this.mc.field_1687.method_8396(this.mc.field_1724, this.mc.field_1724.method_24515(), class_3417.field_26980, class_3419.field_15256, v5, v6);
    }
}
