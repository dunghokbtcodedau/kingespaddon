package com.bassa.addon.modules.suschunks;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_310;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/ChunkOverlayRenderer.class */
public final class ChunkOverlayRenderer {
    private static final double VIEW_RADIUS_SQ = 65536.0d;
    private final ScanRegistry registry;
    private final Setting<Boolean> overgrowthToggle;
    private final Setting<Boolean> crystalToggle;
    private final Setting<Boolean> apiaryToggle;
    private final Setting<Boolean> crystalBlockEsp;
    private final Setting<Boolean> crystalTracers;
    private final Setting<Integer> fillAlpha;
    private final Setting<SettingColor> susTint;
    private final Setting<SettingColor> vinesTint;
    private final Setting<SettingColor> clusterTint;
    private final Setting<SettingColor> kelpTint;
    private final Setting<SettingColor> crystalColumnTint;
    private final Setting<SettingColor> crystalBlockTint;
    private final Setting<SettingColor> crystalLineTint;
    private final Setting<SettingColor> apiaryBlockTint;
    private final Setting<SettingColor> apiaryColumnTint;

    public ChunkOverlayRenderer(ScanRegistry param1, Setting<Boolean> setting, Setting<Boolean> setting2, Setting<Boolean> setting3, Setting<Boolean> setting4, Setting<Boolean> setting5, Setting<Integer> setting6, Setting<SettingColor> setting7, Setting<SettingColor> setting8, Setting<SettingColor> setting9, Setting<SettingColor> setting10, Setting<SettingColor> setting11, Setting<SettingColor> setting12, Setting<SettingColor> setting13, Setting<SettingColor> setting14, Setting<SettingColor> setting15) {
        this.registry = param1;
        this.overgrowthToggle = setting;
        this.crystalToggle = setting2;
        this.apiaryToggle = setting3;
        this.crystalBlockEsp = setting4;
        this.crystalTracers = setting5;
        this.fillAlpha = setting6;
        this.susTint = setting7;
        this.vinesTint = setting8;
        this.clusterTint = setting9;
        this.kelpTint = setting10;
        this.crystalColumnTint = setting11;
        this.crystalBlockTint = setting12;
        this.crystalLineTint = setting13;
        this.apiaryBlockTint = setting14;
        this.apiaryColumnTint = setting15;
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void draw(Render3DEvent param1, class_310 param2) throws MatchException {
        if (param2.field_1724 != null) {
            double v3 = param2.field_1724.method_23317();
            double v5 = param2.field_1724.method_23321();
            int v7 = ((Integer) this.fillAlpha.get()).intValue();
            drawAnomalyOverlay(param1, v3, v5, v7);
            if (((Boolean) this.overgrowthToggle.get()).booleanValue()) {
                drawOvergrowthOverlay(param1, v3, v5, v7);
            }
            if (((Boolean) this.crystalToggle.get()).booleanValue()) {
                drawCrystalOverlay(param1);
            }
            if (((Boolean) this.apiaryToggle.get()).booleanValue()) {
                drawApiaryOverlay(param1);
            }
        }
    }

    private void drawAnomalyOverlay(Render3DEvent param1, double param2, double param4, int param6) {
        if (this.registry.mapOverlayColumns.isEmpty()) {
            return;
        }
        SettingColor settingColor = (SettingColor) this.susTint.get();
        SettingColor settingColor2 = new SettingColor(settingColor.r, settingColor.g, settingColor.b, param6);
        for (class_1923 class_1923Var : this.registry.mapOverlayColumns) {
            if (withinHorizon(class_1923Var, param2, param4)) {
                double v11 = class_1923Var.method_8326();
                double v13 = class_1923Var.method_8328();
                param1.renderer.box(v11, 0.0d, v13, v11 + 16.0d, 0.05d, v13 + 16.0d, settingColor2, settingColor2, ShapeMode.Both, 0);
            }
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    private void drawOvergrowthOverlay(Render3DEvent param1, double param2, double param4, int param6) throws MatchException {
        Object obj;
        if (this.registry.overgrowthByColumn.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<class_1923, OvergrowthKind>> it = this.registry.overgrowthByColumn.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<class_1923, OvergrowthKind> entry = it.next();
            if (withinHorizon(entry.getKey(), param2, param4)) {
                switch (entry.getValue()) {
                    case VINES:
                        obj = this.vinesTint.get();
                        break;
                    case CLUSTERS:
                        obj = this.clusterTint.get();
                        break;
                    case KELP:
                        obj = this.kelpTint.get();
                        break;
                    default:
                        throw new MatchException((String) null, (Throwable) null);
                }
                SettingColor settingColor = (SettingColor) obj;
                SettingColor settingColor2 = new SettingColor(settingColor.r, settingColor.g, settingColor.b, param6);
                class_1923 entryKey = entry.getKey();
                param1.renderer.box(entryKey.method_8326(), 60.0d, entryKey.method_8328(), entryKey.method_8326() + 16, 60.05d, entryKey.method_8328() + 16, settingColor2, settingColor2, ShapeMode.Both, 0);
            }
        }
    }

    private void drawCrystalOverlay(Render3DEvent param1) {
        SettingColor settingColor = (SettingColor) this.crystalColumnTint.get();
        SettingColor settingColor2 = (SettingColor) this.crystalBlockTint.get();
        for (Map.Entry<class_1923, Set<class_2338>> entry : this.registry.crystalDeposits.entrySet()) {
            class_1923 key = entry.getKey();
            Set<class_2338> value = entry.getValue();
            if (!value.isEmpty()) {
                param1.renderer.box(key.method_8326(), 55.0d, key.method_8328(), key.method_8326() + 16, 55.05d, key.method_8328() + 16, settingColor, settingColor, ShapeMode.Both, 0);
                if (((Boolean) this.crystalBlockEsp.get()).booleanValue()) {
                    for (class_2338 class_2338Var : value) {
                        param1.renderer.box(class_2338Var.method_10263(), class_2338Var.method_10264(), class_2338Var.method_10260(), ((double) class_2338Var.method_10263()) + 1.0d, ((double) class_2338Var.method_10264()) + 1.0d, ((double) class_2338Var.method_10260()) + 1.0d, settingColor2, settingColor2, ShapeMode.Both, 0);
                    }
                }
                if (((Boolean) this.crystalTracers.get()).booleanValue() && RenderUtils.center != null) {
                    class_2338 class_2338VarAnchorFor = this.registry.crystalLineAnchors.get(key);
                    if (class_2338VarAnchorFor == null) {
                        class_2338VarAnchorFor = CrystalDepositScanner.anchorFor(value);
                        this.registry.crystalLineAnchors.put(key, class_2338VarAnchorFor);
                    }
                    param1.renderer.line(RenderUtils.center.field_1352, RenderUtils.center.field_1351, RenderUtils.center.field_1350, ((double) class_2338VarAnchorFor.method_10263()) + 0.5d, ((double) class_2338VarAnchorFor.method_10264()) + 0.5d, ((double) class_2338VarAnchorFor.method_10260()) + 0.5d, (Color) this.crystalLineTint.get());
                }
            }
        }
        for (class_1923 class_1923Var : this.registry.deepCrystalColumns) {
            param1.renderer.box(class_1923Var.method_8326(), 55.0d, class_1923Var.method_8328(), class_1923Var.method_8326() + 16, 55.05d, class_1923Var.method_8328() + 16, settingColor, settingColor, ShapeMode.Both, 0);
        }
    }

    private void drawApiaryOverlay(Render3DEvent param1) {
        if (this.registry.apiarySites.isEmpty()) {
            return;
        }
        SettingColor settingColor = (SettingColor) this.apiaryColumnTint.get();
        SettingColor settingColor2 = (SettingColor) this.apiaryBlockTint.get();
        for (class_2338 class_2338Var : this.registry.apiarySites.keySet()) {
            int v6 = class_2338Var.method_10263() >> 4;
            int v7 = class_2338Var.method_10260() >> 4;
            double v8 = ((double) v6) * 16.0d;
            double v10 = ((double) v7) * 16.0d;
            param1.renderer.box(v8, 64.0d, v10, v8 + 16.0d, 64.1d, v10 + 16.0d, settingColor, settingColor, ShapeMode.Both, 0);
            param1.renderer.box(class_2338Var.method_10263(), class_2338Var.method_10264(), class_2338Var.method_10260(), ((double) class_2338Var.method_10263()) + 1.0d, ((double) class_2338Var.method_10264()) + 1.0d, ((double) class_2338Var.method_10260()) + 1.0d, settingColor2, settingColor2, ShapeMode.Both, 0);
        }
    }

    private static boolean withinHorizon(class_1923 param0, double param1, double param3) {
        double v5 = ((double) param0.method_33940()) - param1;
        double v7 = ((double) param0.method_33942()) - param3;
        return (v5 * v5) + (v7 * v7) <= VIEW_RADIUS_SQ;
    }
}
