package com.bassa.addon.ui.loading;

import com.bassa.addon.KingAddon;
import com.bassa.addon.ui.loading.Easing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;

public record LoadingConfig(
        int frameUpdateRateHz,
        long carouselDwellMillis,
        long carouselFadeMillis,
        Easing.Mode carouselEasing,
        float[] carouselBezier,
        Easing.Mode progressEasing,
        float[] progressBezier,
        List<class_2960> textures,
        Map<String, String> stageText
) {
    public static LoadingConfig defaults() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(class_2960.method_60655("bassa", "textures/ui/loading/frame_0.png"));
        arrayList.add(class_2960.method_60655("bassa", "textures/ui/loading/frame_1.png"));
        arrayList.add(class_2960.method_60655("bassa", "textures/ui/loading/frame_2.png"));
        arrayList.add(class_2960.method_60655("bassa", "textures/ui/background.png"));
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("assets", "Loading assets…");
        linkedHashMap.put("modules", "Registering modules…");
        linkedHashMap.put("config", "Applying configuration…");
        linkedHashMap.put("finalize", "Finalizing…");
        return new LoadingConfig(60, 4200L, 900L, Easing.Mode.EASE_IN_OUT_CUBIC, new float[]{0.4f, 0.0f, 0.2f, 1.0f}, Easing.Mode.EASE_OUT_EXPO, new float[]{0.16f, 1.0f, 0.3f, 1.0f}, arrayList, linkedHashMap);
    }

    public static LoadingConfig loadOrDefault(class_2960 param0) {
        class_310 class_310VarMethod_1551 = class_310.method_1551();
        if (class_310VarMethod_1551 == null) {
            return defaults();
        }
        try {
            class_3298 class_3298Var = (class_3298) class_310VarMethod_1551.method_1478().method_14486(param0).orElse((class_3298) null);
            if (class_3298Var == null) {
                return defaults();
            }
            InputStream inputStreamMethod_14482 = class_3298Var.method_14482();
            try {
                LoadingConfig loadingConfigFromJson = fromJson(new String(inputStreamMethod_14482.readAllBytes(), StandardCharsets.UTF_8));
                if (inputStreamMethod_14482 != null) {
                    inputStreamMethod_14482.close();
                }
                return loadingConfigFromJson;
            } finally {
            }
        } catch (Exception v2) {
            KingAddon.LOG.warn("King loading config '{}' unreadable; using defaults.", param0, v2);
            return defaults();
        }
    }

    public static LoadingConfig fromJson(String param0) {
        LoadingConfig loadingConfigDefaults = defaults();
        try {
            JsonObject asJsonObject = JsonParser.parseString(param0).getAsJsonObject();
            int v3 = getInt(asJsonObject, "frameUpdateRateHz", loadingConfigDefaults.frameUpdateRateHz());
            long v4 = getLong(asJsonObject, "carouselDwellMillis", loadingConfigDefaults.carouselDwellMillis());
            long v6 = getLong(asJsonObject, "carouselFadeMillis", loadingConfigDefaults.carouselFadeMillis());
            Easing.Mode mode = getMode(asJsonObject, "carouselEasing", loadingConfigDefaults.carouselEasing());
            float[] bezier = getBezier(asJsonObject, "carouselBezier", loadingConfigDefaults.carouselBezier());
            Easing.Mode mode2 = getMode(asJsonObject, "progressEasing", loadingConfigDefaults.progressEasing());
            float[] bezier2 = getBezier(asJsonObject, "progressBezier", loadingConfigDefaults.progressBezier());
            List<class_2960> arrayList = new ArrayList();
            if (asJsonObject.has("textures") && asJsonObject.get("textures").isJsonArray()) {
                Iterator it = asJsonObject.getAsJsonArray("textures").iterator();
                while (it.hasNext()) {
                    class_2960 class_2960VarMethod_12829 = class_2960.method_12829(((JsonElement) it.next()).getAsString());
                    if (class_2960VarMethod_12829 != null) {
                        arrayList.add(class_2960VarMethod_12829);
                    }
                }
            }
            if (arrayList.isEmpty()) {
                arrayList = loadingConfigDefaults.textures();
            }
            Map<String, String> linkedHashMap = new LinkedHashMap();
            if (asJsonObject.has("stageText") && asJsonObject.get("stageText").isJsonObject()) {
                for (Map.Entry entry : asJsonObject.getAsJsonObject("stageText").entrySet()) {
                    linkedHashMap.put((String) entry.getKey(), ((JsonElement) entry.getValue()).getAsString());
                }
            }
            if (linkedHashMap.isEmpty()) {
                linkedHashMap = loadingConfigDefaults.stageText();
            }
            return new LoadingConfig(v3, v4, v6, mode, bezier, mode2, bezier2, arrayList, linkedHashMap);
        } catch (Exception e) {
            KingAddon.LOG.warn("King loading config JSON malformed; using defaults.", e);
            return loadingConfigDefaults;
        }
    }

    public String stageText(String param1, String param2) {
        String str = this.stageText.get(param1);
        return str != null ? str : param2;
    }

    public long frameIntervalMillis() {
        int v1 = Math.max(15, Math.min(240, this.frameUpdateRateHz));
        return 1000 / ((long) v1);
    }

    private static int getInt(JsonObject param0, String param1, int param2) {
        return param0.has(param1) ? param0.get(param1).getAsInt() : param2;
    }

    private static long getLong(JsonObject param0, String param1, long param2) {
        return param0.has(param1) ? param0.get(param1).getAsLong() : param2;
    }

    private static Easing.Mode getMode(JsonObject param0, String param1, Easing.Mode param2) {
        if (!param0.has(param1)) {
            return param2;
        }
        try {
            return Easing.Mode.valueOf(param0.get(param1).getAsString().trim().toUpperCase());
        } catch (Exception unused) {
            return param2;
        }
    }

    private static float[] getBezier(JsonObject param0, String param1, float[] param2) {
        if (param0.has(param1) && param0.get(param1).isJsonArray()) {
            JsonArray asJsonArray = param0.getAsJsonArray(param1);
            return asJsonArray.size() != 4 ? param2 : new float[]{asJsonArray.get(0).getAsFloat(), asJsonArray.get(1).getAsFloat(), asJsonArray.get(2).getAsFloat(), asJsonArray.get(3).getAsFloat()};
        }
        return param2;
    }
}
