package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2666;
import net.minecraft.class_2803;
import net.minecraft.class_5250;
import net.minecraft.class_8791;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkReloader.class */
public class ChunkReloader extends KingModule {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> triggerY;
    private final Setting<Integer> normalRD;
    private final Setting<Integer> lowRD;
    private final Setting<Integer> reloadDelay;
    private final Setting<Integer> cooldownSec;
    private State state;
    private int tickCounter;
    private long lastReloadTime;
    private int currentJitteredDelay;

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/ChunkReloader$State.class */
    private enum State {
        IDLE,
        DROPPED,
        RESTORING;

        private static State[] $values() {
            return new State[]{IDLE, DROPPED, RESTORING};
        }
    }

    public ChunkReloader() {
        super(KingAddon.CATEGORY, "chunk-reloader", "Forces chunk reload at deepslate level for DonutSMP xray vision.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.triggerY = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("trigger-y")).description("Y level at or below which chunk reload triggers.")).defaultValue(-5)).range(-64, 320).sliderRange(-64, 0).build());
        this.normalRD = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("normal-render-distance")).description("Your normal render distance to restore after reload.")).defaultValue(12)).range(2, 32).sliderRange(2, 32).build());
        this.lowRD = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("low-render-distance")).description("Temporary low render distance to force chunk unload.")).defaultValue(2)).range(2, 8).sliderRange(2, 8).build());
        this.reloadDelay = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("reload-delay-ticks")).description("Ticks to wait at low render distance before restoring.")).defaultValue(10)).range(2, 40).sliderRange(2, 40).build());
        this.cooldownSec = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("cooldown-seconds")).description("Cooldown between chunk reloads in seconds.")).defaultValue(15)).range(5, 120).sliderRange(5, 60).build());
        this.state = State.IDLE;
        this.tickCounter = 0;
        this.lastReloadTime = 0L;
        this.currentJitteredDelay = 0;
    }

    public void onActivate() {
        this.state = State.IDLE;
        this.tickCounter = 0;
        this.lastReloadTime = 0L;
    }

    public void onDeactivate() {
        if (this.state == State.DROPPED && this.mc.field_1690 != null) {
            this.mc.field_1690.method_42503().method_41748((Integer) this.normalRD.get());
        }
        this.state = State.IDLE;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive param1) {
        if (this.state == State.DROPPED && (param1.packet instanceof class_2666)) {
            param1.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.field_1690 == null) {
            return;
        }
        double v2 = this.mc.field_1724.method_23318();
        switch (this.state) {
            case IDLE:
                double v4 = ((double) ((Integer) this.triggerY.get()).intValue()) + 1.5258986444880094d;
                if (v2 <= v4) {
                    long v6 = System.currentTimeMillis();
                    long v8 = ((long) ((Integer) this.cooldownSec.get()).intValue()) * 1000;
                    if (v6 - this.lastReloadTime >= v8) {
                        class_8791 class_8791VarMethod_53842 = this.mc.field_1690.method_53842();
                        this.mc.method_1562().method_52787(new class_2803(new class_8791(class_8791VarMethod_53842.comp_1951(), ((Integer) this.lowRD.get()).intValue(), class_8791VarMethod_53842.comp_1953(), class_8791VarMethod_53842.comp_1954(), class_8791VarMethod_53842.comp_1955(), class_8791VarMethod_53842.comp_1956(), class_8791VarMethod_53842.comp_1957(), class_8791VarMethod_53842.comp_1958(), class_8791VarMethod_53842.comp_2906())));
                        this.state = State.DROPPED;
                        this.tickCounter = 0;
                        this.currentJitteredDelay = ((Integer) this.reloadDelay.get()).intValue() + 10;
                        sendStyledMessage("§8[§6⚡§8] §eSpoofing view distance to §c" + String.valueOf(this.lowRD.get()) + " §eto force server chunk drop...");
                        break;
                    }
                }
                break;
            case DROPPED:
                this.tickCounter++;
                if (this.tickCounter >= this.currentJitteredDelay) {
                    class_8791 class_8791VarMethod_538422 = this.mc.field_1690.method_53842();
                    this.mc.method_1562().method_52787(new class_2803(new class_8791(class_8791VarMethod_538422.comp_1951(), ((Integer) this.normalRD.get()).intValue(), class_8791VarMethod_538422.comp_1953(), class_8791VarMethod_538422.comp_1954(), class_8791VarMethod_538422.comp_1955(), class_8791VarMethod_538422.comp_1956(), class_8791VarMethod_538422.comp_1957(), class_8791VarMethod_538422.comp_1958(), class_8791VarMethod_538422.comp_2906())));
                    this.state = State.RESTORING;
                    this.tickCounter = 0;
                    this.lastReloadTime = System.currentTimeMillis();
                    sendReloadCompleteMessage();
                }
                break;
            case RESTORING:
                this.tickCounter++;
                if (this.tickCounter >= 5) {
                    this.state = State.IDLE;
                    this.tickCounter = 0;
                }
                break;
        }
    }

    private void sendReloadCompleteMessage() {
        class_5250 class_5250VarMethod_43473 = class_2561.method_43473();
        class_5250VarMethod_43473.method_10852(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param0 -> {
            return param0.method_10977(class_124.field_1063);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_43473);
        class_5250 class_5250VarMethod_434732 = class_2561.method_43473();
        class_5250VarMethod_434732.method_10852(class_2561.method_43470(" ⚡ ").method_27694(param02 -> {
            return param02.method_10977(class_124.field_1065).method_10982(true);
        }));
        class_5250VarMethod_434732.method_10852(class_2561.method_43470("CHUNKS RELOADED").method_27694(param03 -> {
            return param03.method_10977(class_124.field_1060).method_10982(true);
        }));
        class_5250VarMethod_434732.method_10852(class_2561.method_43470(" ⚡").method_27694(param04 -> {
            return param04.method_10977(class_124.field_1065).method_10982(true);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_434732);
        class_5250 class_5250VarMethod_434733 = class_2561.method_43473();
        class_5250VarMethod_434733.method_10852(class_2561.method_43470(" 📡 ").method_27694(param05 -> {
            return param05.method_10977(class_124.field_1075);
        }));
        class_5250VarMethod_434733.method_10852(class_2561.method_43470("Render: ").method_27694(param06 -> {
            return param06.method_10977(class_124.field_1080);
        }));
        class_5250VarMethod_434733.method_10852(class_2561.method_43470(String.valueOf(this.lowRD.get())).method_27694(param07 -> {
            return param07.method_10977(class_124.field_1061).method_10982(true);
        }));
        class_5250VarMethod_434733.method_10852(class_2561.method_43470(" → ").method_27694(param08 -> {
            return param08.method_10977(class_124.field_1063);
        }));
        class_5250VarMethod_434733.method_10852(class_2561.method_43470(String.valueOf(this.normalRD.get())).method_27694(param09 -> {
            return param09.method_10977(class_124.field_1060).method_10982(true);
        }));
        class_5250VarMethod_434733.method_10852(class_2561.method_43470(" chunks").method_27694(param010 -> {
            return param010.method_10977(class_124.field_1080);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_434733);
        class_5250 class_5250VarMethod_434734 = class_2561.method_43473();
        class_5250VarMethod_434734.method_10852(class_2561.method_43470(" 🔍 ").method_27694(param011 -> {
            return param011.method_10977(class_124.field_1076);
        }));
        class_5250VarMethod_434734.method_10852(class_2561.method_43470("Y: ").method_27694(param012 -> {
            return param012.method_10977(class_124.field_1080);
        }));
        class_5250VarMethod_434734.method_10852(class_2561.method_43470(String.format("%.1f", Double.valueOf(this.mc.field_1724.method_23318()))).method_27694(param013 -> {
            return param013.method_10977(class_124.field_1054).method_10982(true);
        }));
        class_5250VarMethod_434734.method_10852(class_2561.method_43470(" | Deepslate vision active").method_27694(param014 -> {
            return param014.method_10977(class_124.field_1064);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_434734);
        class_5250 class_5250VarMethod_434735 = class_2561.method_43473();
        class_5250VarMethod_434735.method_10852(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param015 -> {
            return param015.method_10977(class_124.field_1063);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_434735);
    }

    private void sendStyledMessage(String param1) {
        if (this.mc.field_1724 != null) {
            ChatUtils.sendMsg(class_2561.method_43470(param1));
        }
    }
}
