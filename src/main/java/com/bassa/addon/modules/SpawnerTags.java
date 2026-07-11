package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2636;
import net.minecraft.class_2818;
import org.joml.Vector3d;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/SpawnerTags.class */
public class SpawnerTags extends KingModule {
    private static final int SCAN_CHUNK_RANGE = 32;
    private static final int SCAN_INTERVAL = 10;
    private static final double Y_OFFSET = 0.85d;
    private final SettingGroup sgGeneral;
    private final Setting<SettingColor> textColor;
    private final Setting<SettingColor> backgroundColor;
    private final Setting<Double> scale;
    private final Long2ObjectMap<SpawnerTag> spawners;
    private final List<SpawnerTag> renderList;
    private final Vector3d tagPos;
    private int tickCounter;
    private int spawnerCount;

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/SpawnerTags$SpawnerTag.class */
    private static final class SpawnerTag {
        private final long key;
        private final class_2338 pos;
        private final String label;
        private double distanceSq;

        private SpawnerTag(long param1, class_2338 param3, String param4) {
            this.key = param1;
            this.pos = param3.method_10062();
            this.label = param4;
        }
    }

    public SpawnerTags() {
        super(KingAddon.CATEGORY, "spawner-tags", "Shows mob type nametags above spawners.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.textColor = this.sgGeneral.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("text-color")).description("Nametag text color.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
        this.backgroundColor = this.sgGeneral.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("background-color")).description("Nametag background color.")).defaultValue(new SettingColor(0, 0, 0, 120)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("scale")).description("Nametag text scale.")).defaultValue(1.15d).range(0.35d, 4.0d).sliderRange(0.5d, 2.5d).build());
        this.spawners = new Long2ObjectOpenHashMap();
        this.renderList = new ArrayList();
        this.tagPos = new Vector3d();
    }

    public void onActivate() {
        this.spawners.clear();
        this.renderList.clear();
        this.tickCounter = 0;
        this.spawnerCount = 0;
        scanAroundPlayer();
    }

    public void onDeactivate() {
        this.spawners.clear();
        this.renderList.clear();
        this.spawnerCount = 0;
    }

    public String getInfoString() {
        return String.valueOf(this.spawnerCount);
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent param1) {
        scanChunk(param1.chunk());
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        int i = this.tickCounter + 1;
        this.tickCounter = i;
        if (i >= SCAN_INTERVAL) {
            this.tickCounter = 0;
            scanAroundPlayer();
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.spawners.isEmpty()) {
            return;
        }
        this.renderList.clear();
        int v2 = maxRenderDistanceSq();
        class_2338 class_2338VarMethod_24515 = this.mc.field_1724.method_24515();
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        ObjectIterator it = this.spawners.values().iterator();
        while (it.hasNext()) {
            SpawnerTag spawnerTag = (SpawnerTag) it.next();
            if (this.mc.field_1687.method_8393(spawnerTag.pos.method_10263() >> 4, spawnerTag.pos.method_10260() >> 4) && class_2338VarMethod_24515.method_10262(spawnerTag.pos) <= v2 && this.mc.field_1687.method_8320(spawnerTag.pos).method_27852(class_2246.field_10260)) {
                spawnerTag.distanceSq = class_2338VarMethod_24515.method_10262(spawnerTag.pos);
                this.renderList.add(spawnerTag);
                long2ObjectOpenHashMap.put(spawnerTag.key, spawnerTag);
            }
        }
        if (long2ObjectOpenHashMap.size() != this.spawners.size()) {
            this.spawners.clear();
            this.spawners.putAll(long2ObjectOpenHashMap);
        }
        if (this.renderList.isEmpty()) {
            this.spawnerCount = 0;
            return;
        }
        this.renderList.sort(Comparator.comparingDouble(param0 -> {
            return -param0.distanceSq;
        }));
        boolean zBooleanValue = ((Boolean) Config.get().customFont.get()).booleanValue();
        TextRenderer textRenderer = TextRenderer.get();
        Color color = new Color((Color) this.textColor.get());
        for (SpawnerTag spawnerTag2 : this.renderList) {
            this.tagPos.set(((double) spawnerTag2.pos.method_10263()) + 0.5d, ((double) spawnerTag2.pos.method_10264()) + Y_OFFSET, ((double) spawnerTag2.pos.method_10260()) + 0.5d);
            if (NametagUtils.to2D(this.tagPos, ((Double) this.scale.get()).doubleValue(), false)) {
                renderTag(textRenderer, spawnerTag2.label, color, zBooleanValue);
            }
        }
        this.spawnerCount = this.spawners.size();
    }

    private int maxRenderDistanceSq() {
        int v1 = ((Integer) this.mc.field_1690.method_42503().method_41753()).intValue();
        int v2 = (v1 + 1) * 16;
        return v2 * v2;
    }

    private void renderTag(TextRenderer param1, String param2, Color param3, boolean param4) {
        NametagUtils.begin(this.tagPos);
        double v5 = param1.getWidth(param2, param4);
        double v7 = param1.getHeight(param4);
        double v9 = v5 / 2.0d;
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad((-v9) - 1.0d, (-v7) - 1.0d, v5 + 2.0d, v7 + 2.0d, (Color) this.backgroundColor.get());
        Renderer2D.COLOR.render();
        param1.beginBig();
        param1.render(param2, -v9, -v7, param3, param4);
        param1.end();
        NametagUtils.end();
    }

    private void scanAroundPlayer() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_1923 class_1923VarMethod_31476 = this.mc.field_1724.method_31476();
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        for (int v3 = -32; v3 <= SCAN_CHUNK_RANGE; v3++) {
            for (int v4 = -32; v4 <= SCAN_CHUNK_RANGE; v4++) {
                int v5 = class_1923VarMethod_31476.field_9181 + v3;
                int v6 = class_1923VarMethod_31476.field_9180 + v4;
                if (this.mc.field_1687.method_8393(v5, v6)) {
                    collectChunkSpawners(this.mc.field_1687.method_8497(v5, v6), long2ObjectOpenHashMap);
                }
            }
        }
        this.spawners.clear();
        this.spawners.putAll(long2ObjectOpenHashMap);
        this.spawnerCount = this.spawners.size();
    }

    private void scanChunk(class_2818 param1) {
        collectChunkSpawners(param1, this.spawners);
        this.spawnerCount = this.spawners.size();
    }

    private void collectChunkSpawners(class_2818 param1, Long2ObjectMap<SpawnerTag> long2ObjectMap) {
        for (Object v4 : param1.method_12214().values()) {
            if (v4 instanceof class_2636 class_2636Var) {
                class_2338 class_2338VarMethod_11016 = class_2636Var.method_11016();
                String strResolveSpawnerLabel = resolveSpawnerLabel(class_2636Var, class_2338VarMethod_11016);
                if (strResolveSpawnerLabel != null) {
                    long v8 = class_2338VarMethod_11016.method_10063();
                    long2ObjectMap.put(v8, new SpawnerTag(v8, class_2338VarMethod_11016, strResolveSpawnerLabel));
                }
            }
        }
    }

    private String resolveSpawnerLabel(class_2636 param1, class_2338 param2) {
        class_1299<?> spawnerEntityType = readSpawnerEntityType(param1, param2);
        if (spawnerEntityType == null) {
            return null;
        }
        return formatMobName(spawnerEntityType);
    }

    private class_1299<?> readSpawnerEntityType(class_2636 param1, class_2338 param2) {
        if (this.mc.field_1687 == null) {
            return null;
        }
        try {
            class_1297 class_1297VarMethod_8283 = param1.method_11390().method_8283(this.mc.field_1687, param2);
            if (class_1297VarMethod_8283 != null) {
                return class_1297VarMethod_8283.method_5864();
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }

    private String formatMobName(class_1299<?> class_1299Var) {
        if (class_1299Var == class_1299.field_6050) {
            return "Zombie Piglin";
        }
        if (class_1299Var == class_1299.field_25751) {
            return "Piglin Brute";
        }
        if (class_1299Var == class_1299.field_6147) {
            return "Iron Golem";
        }
        if (class_1299Var == class_1299.field_6075) {
            return "Skeleton Horse";
        }
        if (class_1299Var == class_1299.field_6048) {
            return "Zombie Horse";
        }
        if (class_1299Var == class_1299.field_6084) {
            return "Cave Spider";
        }
        if (class_1299Var == class_1299.field_6102) {
            return "Magma Cube";
        }
        if (class_1299Var == class_1299.field_6076) {
            return "Wither Skeleton";
        }
        if (class_1299Var == class_1299.field_6099) {
            return "Blaze";
        }
        if (class_1299Var == class_1299.field_6125) {
            return "Silverfish";
        }
        String string = class_1299Var.method_5897().getString();
        return (string == null || string.isBlank()) ? class_1299Var.toString() : string;
    }
}
