package com.bassa.addon.modules.suschunks;

import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.class_310;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/DetectorContext.class */
public final class DetectorContext {
    public final class_310 client;
    public final ScanRegistry registry;
    public final AlertDispatcher alerts;
    public final Setting<Boolean> chatAlerts;
    public final Setting<Boolean> soundAlert;
    public final Setting<Integer> sensitivity;
    public final Setting<Boolean> beehiveEnabled;
    public final Setting<Integer> beehiveRange;
    public final Setting<Boolean> overgrowthEnabled;
    public final Setting<Integer> vineChainMin;
    public final Setting<Boolean> crystalEnabled;
    public final Setting<Integer> crystalMinHits;
    public final Setting<Boolean> deepCrystalEnabled;

    public DetectorContext(class_310 param1, ScanRegistry param2, AlertDispatcher param3, Setting<Boolean> setting, Setting<Boolean> setting2, Setting<Integer> setting3, Setting<Boolean> setting4, Setting<Integer> setting5, Setting<Boolean> setting6, Setting<Integer> setting7, Setting<Boolean> setting8, Setting<Integer> setting9, Setting<Boolean> setting10) {
        this.client = param1;
        this.registry = param2;
        this.alerts = param3;
        this.chatAlerts = setting;
        this.soundAlert = setting2;
        this.sensitivity = setting3;
        this.beehiveEnabled = setting4;
        this.beehiveRange = setting5;
        this.overgrowthEnabled = setting6;
        this.vineChainMin = setting7;
        this.crystalEnabled = setting8;
        this.crystalMinHits = setting9;
        this.deepCrystalEnabled = setting10;
    }

    public boolean notifyChat() {
        return ((Boolean) this.chatAlerts.get()).booleanValue();
    }

    public boolean notifySound() {
        return ((Boolean) this.soundAlert.get()).booleanValue();
    }
}
