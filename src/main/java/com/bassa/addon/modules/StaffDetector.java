package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.class_124;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import net.minecraft.class_642;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/StaffDetector.class */
public class StaffDetector extends KingModule {
    private static final int[] K = {79, 107, 33, 122, 85, 60, 105, 18, 68, 126, 51, 93, 26, 98, 15, 72};
    private static final int[][] ENCODED_NAMES = {enc("pastagamer08"), enc("W1zoX_"), enc("bautiedgar"), enc("Frwost"), enc("showered")};
    private static final Map<String, String> STAFF_LOOKUP = new HashMap();
    private static final String[][] REGION_PATTERNS;
    private static final String[] REGION_LABELS;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTrigger;
    private final SettingGroup sgRegion;
    private final SettingGroup sgAlert;
    private final SettingGroup sgAction;
    private final Setting<Region> myRegion;
    private final Setting<Integer> alertCooldown;
    private final Setting<ScanTrigger> scanTrigger;
    private final Setting<Integer> scanMinSec;
    private final Setting<Integer> scanMaxSec;
    private final Setting<Integer> triggerYLevel;
    private final Setting<Integer> yDropDistance;
    private final Setting<Integer> triggerCooldown;
    private final Setting<Boolean> autoFindRegion;
    private final Setting<String> findCommand;
    private final Setting<Integer> cmdDelay;
    private final Setting<Integer> listenWindowSec;
    private final Setting<Boolean> hideCommandResponse;
    private final Setting<Boolean> chatAlert;
    private final Setting<Boolean> toastAlert;
    private final Setting<Integer> toastDuration;
    private final Setting<Boolean> soundAlert;
    private final Setting<Double> soundVolume;
    private final Setting<Boolean> sendChatMessage;
    private final Setting<String> chatMessage;
    private final Setting<Integer> msgMinDelay;
    private final Setting<Integer> msgMaxDelay;
    private final Setting<Boolean> autoDisconnect;
    private long nextTimedScan;
    private long lastTriggerScan;
    private double highestRecentY;
    private boolean hasJoinScanned;
    private final Map<String, Long> lastAlertTime;
    private final Set<String> currentlyOnline;
    private final Map<String, Long> pendingMessages;
    private final Map<String, Long> pendingFindCommands;
    private final Random rng;
    private String listeningForStaff;
    private long listenUntil;
    private String lastDetectedStaffRegion;
    private final Map<String, String> staffRegionCache;

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/StaffDetector$DetectionToast.class */
    private static class DetectionToast implements class_368 {
        private static final class_1799 IC_W = new class_1799(class_1802.field_8077);
        private static final class_1799 IC_I = new class_1799(class_1802.field_27070);
        private final class_2561 title;
        private final class_2561 desc;
        private final long dur;
        private final boolean danger;
        private long elapsed;
        private class_368.class_369 vis = class_368.class_369.field_2210;

        DetectionToast(class_2561 param1, class_2561 param2, long param3, boolean param5) {
            this.title = param1;
            this.desc = param2;
            this.dur = param3;
            this.danger = param5;
        }

        public class_368.class_369 method_61988() {
            return this.vis;
        }

        public void method_61989(class_374 param1, long param2) {
            this.elapsed = param2;
            if (this.elapsed >= this.dur) {
                this.vis = class_368.class_369.field_2209;
            }
        }

        public void method_1986(class_332 param1, class_327 param2, long param3) {
            int v5 = this.danger ? -266336248 : -267118560;
            int v6 = this.danger ? -48060 : -14494738;
            param1.method_25294(0, 0, method_29049(), method_29050(), v5);
            param1.method_25294(0, 0, 3, method_29050(), v6);
            param1.method_51427(this.danger ? IC_W : IC_I, 9, 7);
            param1.method_51439(param2, this.title, 32, 8, v6, false);
            param1.method_51439(param2, this.desc, 32, 19, -5195580, false);
        }

        public int method_29049() {
            return 200;
        }

        public int method_29050() {
            return 30;
        }
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/StaffDetector$Region.class */
    public enum Region {
        AUTO("Auto-Detect"),
        EU_WEST("EU West"),
        EU_CENTRAL("EU Central"),
        NA_EAST("NA East"),
        NA_WEST("NA West"),
        ASIA("Asia"),
        OCEANIA("Oceania");

        private final String label;

        Region(String param3) {
            this.label = param3;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.label;
        }

        private static Region[] $values() {
            return new Region[]{AUTO, EU_WEST, EU_CENTRAL, NA_EAST, NA_WEST, ASIA, OCEANIA};
        }
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/StaffDetector$ScanTrigger.class */
    public enum ScanTrigger {
        TIMED("Timed"),
        Y_LEVEL("Below Y Level"),
        Y_DROP("Y Drop"),
        ON_JOIN("On Server Join"),
        COMBINED("Combined (All)");

        private final String label;

        ScanTrigger(String param3) {
            this.label = param3;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.label;
        }

        private static ScanTrigger[] $values() {
            return new ScanTrigger[]{TIMED, Y_LEVEL, Y_DROP, ON_JOIN, COMBINED};
        }
    }

    private static int[] enc(String param0) {
        byte[] bytes = param0.getBytes(StandardCharsets.UTF_8);
        int[] iArr = new int[bytes.length];
        for (int v3 = 0; v3 < bytes.length; v3++) {
            iArr[v3] = ((bytes[v3] & 255) ^ K[v3 % K.length]) ^ ((v3 * 55) & 255);
        }
        return iArr;
    }

    private static String dec(int[] param0) {
        byte[] bArr = new byte[param0.length];
        for (int v2 = 0; v2 < param0.length; v2++) {
            bArr[v2] = (byte) ((param0[v2] ^ K[v2 % K.length]) ^ ((v2 * 55) & 255));
        }
        return new String(bArr, StandardCharsets.UTF_8);
    }

    private static String h(String param0) {
        try {
            byte[] bArrDigest = MessageDigest.getInstance("SHA-256").digest(param0.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : bArrDigest) {
                sb.append(String.format("%02x", Integer.valueOf(b & 255)));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public StaffDetector() {
        super(KingAddon.CATEGORY, "staff-detector", "Detects DonutSMP staff in the tab list and alerts you with region info.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTrigger = this.settings.createGroup("Scan Triggers");
        this.sgRegion = this.settings.createGroup("Region Detection");
        this.sgAlert = this.settings.createGroup("Alerts");
        this.sgAction = this.settings.createGroup("Actions");
        this.myRegion = this.sgGeneral.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("my-region")).description("Your region. Used to compare with staff's detected region.")).defaultValue(Region.AUTO)).build());
        this.alertCooldown = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("alert-cooldown")).description("Seconds before re-alerting about the same player.")).defaultValue(300)).range(30, 900).sliderRange(60, 600).build());
        this.scanTrigger = this.sgTrigger.add(((EnumSetting.Builder) ((EnumSetting.Builder) ((EnumSetting.Builder) new EnumSetting.Builder().name("trigger-mode")).description("When to scan the tab list. Combined = all triggers active at once.")).defaultValue(ScanTrigger.COMBINED)).build());
        this.scanMinSec = this.sgTrigger.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("scan-min-seconds")).description("Minimum seconds between timed scans.")).defaultValue(60)).range(10, 300).sliderRange(10, 180).visible(() -> {
            return this.scanTrigger.get() == ScanTrigger.TIMED || this.scanTrigger.get() == ScanTrigger.COMBINED;
        })).build());
        this.scanMaxSec = this.sgTrigger.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("scan-max-seconds")).description("Maximum seconds between timed scans.")).defaultValue(120)).range(20, 600).sliderRange(20, 300).visible(() -> {
            return this.scanTrigger.get() == ScanTrigger.TIMED || this.scanTrigger.get() == ScanTrigger.COMBINED;
        })).build());
        this.triggerYLevel = this.sgTrigger.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("trigger-y-level")).description("Scan triggers when you are below this Y level.")).defaultValue(-10)).range(-64, 320).sliderRange(-64, 100).visible(() -> {
            return this.scanTrigger.get() == ScanTrigger.Y_LEVEL || this.scanTrigger.get() == ScanTrigger.COMBINED;
        })).build());
        this.yDropDistance = this.sgTrigger.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("y-drop-blocks")).description("Scan triggers when you drop this many blocks from your highest recent Y.")).defaultValue(15)).range(5, 100).sliderRange(5, 50).visible(() -> {
            return this.scanTrigger.get() == ScanTrigger.Y_DROP || this.scanTrigger.get() == ScanTrigger.COMBINED;
        })).build());
        this.triggerCooldown = this.sgTrigger.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("trigger-cooldown")).description("Minimum seconds between trigger-based scans to avoid spam.")).defaultValue(30)).range(5, 120).sliderRange(5, 60).visible(() -> {
            return this.scanTrigger.get() != ScanTrigger.TIMED;
        })).build());
        this.autoFindRegion = this.sgRegion.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-find-region")).description("Automatically run a command to find which region staff is on when detected.")).defaultValue(true)).build());
        SettingGroup settingGroup = this.sgRegion;
        StringSetting.Builder builder = (StringSetting.Builder) ((StringSetting.Builder) ((StringSetting.Builder) new StringSetting.Builder().name("find-command")).description("Command to find a player's region. Use {name} for the staff name. Don't include /.")).defaultValue("findplayer {name}");
        Setting<Boolean> setting = this.autoFindRegion;
        Objects.requireNonNull(setting);
        Objects.requireNonNull(setting);
        this.findCommand = settingGroup.add(((StringSetting.Builder) builder.visible(setting::get)).build());
        SettingGroup settingGroup2 = this.sgRegion;
        IntSetting.Builder builderSliderRange = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("command-delay-ms")).description("Random delay before sending the find command to look natural.")).defaultValue(2000)).range(500, 10000).sliderRange(500, 5000);
        Setting<Boolean> setting2 = this.autoFindRegion;
        Objects.requireNonNull(setting2);
        Objects.requireNonNull(setting2);
        this.cmdDelay = settingGroup2.add(((IntSetting.Builder) builderSliderRange.visible(setting2::get)).build());
        SettingGroup settingGroup3 = this.sgRegion;
        IntSetting.Builder builderSliderRange2 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("listen-window")).description("Seconds to listen for region info in chat after running the find command.")).defaultValue(5)).range(2, 15).sliderRange(2, 10);
        Setting<Boolean> setting3 = this.autoFindRegion;
        Objects.requireNonNull(setting3);
        Objects.requireNonNull(setting3);
        this.listenWindowSec = settingGroup3.add(((IntSetting.Builder) builderSliderRange2.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgRegion;
        BoolSetting.Builder builder2 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("hide-response")).description("Hide the server's response to the find command from your chat.")).defaultValue(true);
        Setting<Boolean> setting4 = this.autoFindRegion;
        Objects.requireNonNull(setting4);
        Objects.requireNonNull(setting4);
        this.hideCommandResponse = settingGroup4.add(((BoolSetting.Builder) builder2.visible(setting4::get)).build());
        this.chatAlert = this.sgAlert.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chat-alert")).description("Show a styled chat warning when a match is found.")).defaultValue(true)).build());
        this.toastAlert = this.sgAlert.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("toast-alert")).description("Show a toast notification on match.")).defaultValue(true)).build());
        SettingGroup settingGroup5 = this.sgAlert;
        IntSetting.Builder builderSliderRange3 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("toast-duration")).description("Toast display time in milliseconds.")).defaultValue(6000)).range(1000, 15000).sliderRange(2000, 10000);
        Setting<Boolean> setting5 = this.toastAlert;
        Objects.requireNonNull(setting5);
        Objects.requireNonNull(setting5);
        this.toastDuration = settingGroup5.add(((IntSetting.Builder) builderSliderRange3.visible(setting5::get)).build());
        this.soundAlert = this.sgAlert.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound")).description("Play a warning sound on match.")).defaultValue(true)).build());
        SettingGroup settingGroup6 = this.sgAlert;
        DoubleSetting.Builder builderSliderRange4 = ((DoubleSetting.Builder) ((DoubleSetting.Builder) new DoubleSetting.Builder().name("sound-volume")).description("Alert sound volume.")).defaultValue(1.0d).range(0.1d, 2.0d).sliderRange(0.1d, 2.0d);
        Setting<Boolean> setting6 = this.soundAlert;
        Objects.requireNonNull(setting6);
        Objects.requireNonNull(setting6);
        this.soundVolume = settingGroup6.add(((DoubleSetting.Builder) builderSliderRange4.visible(setting6::get)).build());
        this.sendChatMessage = this.sgAction.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("send-chat-msg")).description("Auto-send a chat message when a same-region match is found. Visible to everyone!")).defaultValue(false)).build());
        SettingGroup settingGroup7 = this.sgAction;
        StringSetting.Builder builder3 = (StringSetting.Builder) ((StringSetting.Builder) ((StringSetting.Builder) new StringSetting.Builder().name("chat-message")).description("Message to send. {staff} = name, {region} = region.")).defaultValue("hey {staff}!");
        Setting<Boolean> setting7 = this.sendChatMessage;
        Objects.requireNonNull(setting7);
        Objects.requireNonNull(setting7);
        this.chatMessage = settingGroup7.add(((StringSetting.Builder) builder3.visible(setting7::get)).build());
        SettingGroup settingGroup8 = this.sgAction;
        IntSetting.Builder builderSliderRange5 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("msg-min-delay")).description("Minimum delay (ms) before sending chat message.")).defaultValue(2000)).range(500, 10000).sliderRange(500, 5000);
        Setting<Boolean> setting8 = this.sendChatMessage;
        Objects.requireNonNull(setting8);
        Objects.requireNonNull(setting8);
        this.msgMinDelay = settingGroup8.add(((IntSetting.Builder) builderSliderRange5.visible(setting8::get)).build());
        SettingGroup settingGroup9 = this.sgAction;
        IntSetting.Builder builderSliderRange6 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("msg-max-delay")).description("Maximum delay (ms) before sending chat message.")).defaultValue(6000)).range(1000, 15000).sliderRange(1000, 10000);
        Setting<Boolean> setting9 = this.sendChatMessage;
        Objects.requireNonNull(setting9);
        Objects.requireNonNull(setting9);
        this.msgMaxDelay = settingGroup9.add(((IntSetting.Builder) builderSliderRange6.visible(setting9::get)).build());
        this.autoDisconnect = this.sgAction.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("auto-disconnect")).description("Disconnect when a same-region match is detected.")).defaultValue(false)).build());
        this.lastAlertTime = new HashMap();
        this.currentlyOnline = new HashSet();
        this.pendingMessages = new LinkedHashMap();
        this.pendingFindCommands = new LinkedHashMap();
        this.rng = new Random();
        this.listeningForStaff = null;
        this.listenUntil = 0L;
        this.lastDetectedStaffRegion = null;
        this.staffRegionCache = new LinkedHashMap();
    }

    public void onActivate() {
        this.nextTimedScan = System.currentTimeMillis() + 3000;
        this.lastTriggerScan = 0L;
        this.highestRecentY = -999.0d;
        this.hasJoinScanned = false;
        this.lastAlertTime.clear();
        this.currentlyOnline.clear();
        this.pendingMessages.clear();
        this.pendingFindCommands.clear();
        this.staffRegionCache.clear();
        this.listeningForStaff = null;
        this.listenUntil = 0L;
    }

    public void onDeactivate() {
        this.lastAlertTime.clear();
        this.currentlyOnline.clear();
        this.pendingMessages.clear();
        this.pendingFindCommands.clear();
        this.staffRegionCache.clear();
        this.listeningForStaff = null;
    }

    public String getInfoString() {
        if (this.currentlyOnline.isEmpty()) {
            return "clear";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.currentlyOnline.size()).append(" staff");
        for (String str : this.currentlyOnline) {
            String str2 = this.staffRegionCache.get(str);
            if (str2 != null) {
                sb.append(" · ").append(str).append("[").append(str2).append("]");
            }
        }
        return sb.toString();
    }

    private void scheduleNextTimed() {
        int v1 = ((Integer) this.scanMinSec.get()).intValue() * 1000;
        int v2 = Math.max(v1 + 1000, ((Integer) this.scanMaxSec.get()).intValue() * 1000);
        this.nextTimedScan = System.currentTimeMillis() + ((long) v1) + ((long) this.rng.nextInt(v2 - v1));
    }

    private boolean triggerCooldownOk() {
        return System.currentTimeMillis() - this.lastTriggerScan >= ((long) ((Integer) this.triggerCooldown.get()).intValue()) * 1000;
    }

    private void markTriggerScan() {
        this.lastTriggerScan = System.currentTimeMillis();
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || this.mc.method_1562() == null) {
            return;
        }
        drainPending();
        drainFindCommands();
        if (this.listeningForStaff != null && System.currentTimeMillis() > this.listenUntil) {
            if (this.lastDetectedStaffRegion == null) {
                this.staffRegionCache.put(this.listeningForStaff, "Unknown");
            }
            this.listeningForStaff = null;
        }
        Object v2 = (ScanTrigger) this.scanTrigger.get();
        double v3 = this.mc.field_1724.method_23318();
        long v5 = System.currentTimeMillis();
        int v7 = 0;
        if ((v2 == ScanTrigger.TIMED || v2 == ScanTrigger.COMBINED) && v5 >= this.nextTimedScan) {
            scheduleNextTimed();
            v7 = 1;
        }
        if ((v2 == ScanTrigger.Y_LEVEL || v2 == ScanTrigger.COMBINED) && v3 <= ((Integer) this.triggerYLevel.get()).intValue() && triggerCooldownOk()) {
            v7 = 1;
            markTriggerScan();
        }
        if (v2 == ScanTrigger.Y_DROP || v2 == ScanTrigger.COMBINED) {
            if (v3 > this.highestRecentY) {
                this.highestRecentY = v3;
            }
            double v8 = this.highestRecentY - v3;
            if (v8 >= ((Integer) this.yDropDistance.get()).intValue() && triggerCooldownOk()) {
                v7 = 1;
                markTriggerScan();
                this.highestRecentY = v3;
            }
        }
        if ((v2 == ScanTrigger.ON_JOIN || v2 == ScanTrigger.COMBINED) && !this.hasJoinScanned && this.mc.field_1724.field_6012 > 60) {
            this.hasJoinScanned = true;
            v7 = 1;
        }
        if (v7 != 0) {
            runScan();
        }
    }

    @EventHandler
    private void onChatMessage(ReceiveMessageEvent param1) {
        if (this.listeningForStaff == null || System.currentTimeMillis() > this.listenUntil) {
            return;
        }
        String lowerCase = param1.getMessage().getString().toLowerCase(Locale.ROOT);
        String regionFromText = parseRegionFromText(lowerCase);
        if (regionFromText == null) {
            if (((Boolean) this.hideCommandResponse.get()).booleanValue() && lowerCase.contains(this.listeningForStaff.toLowerCase(Locale.ROOT))) {
                param1.cancel();
                return;
            }
            return;
        }
        this.lastDetectedStaffRegion = regionFromText;
        this.staffRegionCache.put(this.listeningForStaff, regionFromText);
        boolean zEqualsIgnoreCase = getMyRegionLabel().equalsIgnoreCase(regionFromText);
        class_5250 class_5250VarMethod_43473 = class_2561.method_43473();
        class_5250VarMethod_43473.method_10852(class_2561.method_43470("Bassa").method_27694(param0 -> {
            return param0.method_10977(class_124.field_1078).method_10982(true);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(" » ").method_27694(param02 -> {
            return param02.method_10977(class_124.field_1063);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(this.listeningForStaff).method_27694(param03 -> {
            return param03.method_10977(class_124.field_1054).method_10982(true);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(" is on ").method_27694(param04 -> {
            return param04.method_10977(class_124.field_1080);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(regionFromText).method_27694(param05 -> {
            return param05.method_10977(class_124.field_1060).method_10982(true);
        }));
        if (zEqualsIgnoreCase) {
            class_5250VarMethod_43473.method_10852(class_2561.method_43470(" — ").method_27694(param06 -> {
                return param06.method_10977(class_124.field_1063);
            }));
            class_5250VarMethod_43473.method_10852(class_2561.method_43470("⚠ SAME AS YOU!").method_27694(param07 -> {
                return param07.method_10977(class_124.field_1061).method_10982(true);
            }));
        }
        ChatUtils.sendMsg(class_5250VarMethod_43473);
        if (zEqualsIgnoreCase) {
            if (((Boolean) this.soundAlert.get()).booleanValue()) {
                soundNotify(true);
            }
            if (((Boolean) this.sendChatMessage.get()).booleanValue()) {
                int v7 = ((Integer) this.msgMinDelay.get()).intValue();
                int v8 = Math.max(v7 + 100, ((Integer) this.msgMaxDelay.get()).intValue());
                long v9 = System.currentTimeMillis() + ((long) v7) + ((long) this.rng.nextInt(v8 - v7));
                this.pendingMessages.put(this.listeningForStaff, Long.valueOf(v9));
            }
            if (((Boolean) this.autoDisconnect.get()).booleanValue() && this.mc.method_1562() != null) {
                this.mc.method_1562().method_48296().method_10747(class_2561.method_43470("Connection lost"));
            }
        }
        this.listeningForStaff = null;
        if (((Boolean) this.hideCommandResponse.get()).booleanValue()) {
            param1.cancel();
        }
    }

    private String parseRegionFromText(String param1) {
        for (int v2 = 0; v2 < REGION_PATTERNS.length; v2++) {
            for (String str : REGION_PATTERNS[v2]) {
                if (param1.contains(str)) {
                    return REGION_LABELS[v2];
                }
            }
        }
        return null;
    }

    private String getMyRegionLabel() {
        String regionFromText;
        if (this.myRegion.get() != Region.AUTO) {
            return ((Region) this.myRegion.get()).toString();
        }
        class_642 class_642VarMethod_1558 = this.mc.method_1558();
        return (class_642VarMethod_1558 == null || (regionFromText = parseRegionFromText(class_642VarMethod_1558.field_3761.toLowerCase(Locale.ROOT))) == null) ? "Unknown" : regionFromText;
    }

    private void runScan() {
        Collection collectionMethod_2880 = this.mc.method_1562().method_2880();
        long v2 = System.currentTimeMillis() / 1000;
        HashSet hashSet = new HashSet();
        ArrayList<String> arrayList = new ArrayList();
        Iterator it = collectionMethod_2880.iterator();
        while (it.hasNext()) {
            String strName = ((class_640) it.next()).method_2966().name();
            if (strName != null && !strName.isEmpty()) {
                Object v9 = h(strName.toLowerCase(Locale.ROOT));
                String str = STAFF_LOOKUP.get(v9);
                if (str != null) {
                    hashSet.add(str);
                    Long l = this.lastAlertTime.get(str);
                    if (l == null || v2 - l.longValue() >= ((Integer) this.alertCooldown.get()).intValue()) {
                        this.lastAlertTime.put(str, Long.valueOf(v2));
                        arrayList.add(str);
                    }
                }
            }
        }
        this.currentlyOnline.clear();
        this.currentlyOnline.addAll(hashSet);
        for (String str2 : arrayList) {
            alert(str2, this.staffRegionCache.get(str2));
            if (((Boolean) this.autoFindRegion.get()).booleanValue()) {
                this.pendingFindCommands.put(str2, Long.valueOf(System.currentTimeMillis() + ((long) (500 + this.rng.nextInt(Math.max(1, ((Integer) this.cmdDelay.get()).intValue()))))));
            }
        }
    }

    private void alert(String param1, String param2) {
        if (((Boolean) this.chatAlert.get()).booleanValue()) {
            chatNotify(param1, param2);
        }
        if (((Boolean) this.toastAlert.get()).booleanValue()) {
            toastNotify(param1, param2);
        }
        if (((Boolean) this.soundAlert.get()).booleanValue()) {
            soundNotify(false);
        }
    }

    private void chatNotify(String param1, String param2) {
        ChatUtils.sendMsg(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param0 -> {
            return param0.method_10977(class_124.field_1063);
        }));
        class_5250 class_5250VarMethod_43473 = class_2561.method_43473();
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(" ⚠ ").method_27694(param02 -> {
            return param02.method_10977(class_124.field_1061).method_10982(true);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470("STAFF DETECTED").method_27694(param03 -> {
            return param03.method_10977(class_124.field_1061).method_10982(true);
        }));
        class_5250VarMethod_43473.method_10852(class_2561.method_43470(" ⚠").method_27694(param04 -> {
            return param04.method_10977(class_124.field_1061).method_10982(true);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_43473);
        class_5250 class_5250VarMethod_434732 = class_2561.method_43473();
        class_5250VarMethod_434732.method_10852(class_2561.method_43470(" 👤 ").method_27694(param05 -> {
            return param05.method_10977(class_124.field_1065);
        }));
        class_5250VarMethod_434732.method_10852(class_2561.method_43470("Name: ").method_27694(param06 -> {
            return param06.method_10977(class_124.field_1080);
        }));
        class_5250VarMethod_434732.method_10852(class_2561.method_43470(param1).method_27694(param07 -> {
            return param07.method_10977(class_124.field_1054).method_10982(true);
        }));
        ChatUtils.sendMsg(class_5250VarMethod_434732);
        if (param2 != null) {
            class_5250 class_5250VarMethod_434733 = class_2561.method_43473();
            class_5250VarMethod_434733.method_10852(class_2561.method_43470(" 🗺 ").method_27694(param08 -> {
                return param08.method_10977(class_124.field_1075);
            }));
            class_5250VarMethod_434733.method_10852(class_2561.method_43470("Last known region: ").method_27694(param09 -> {
                return param09.method_10977(class_124.field_1080);
            }));
            class_5250VarMethod_434733.method_10852(class_2561.method_43470(param2).method_27694(param010 -> {
                return param010.method_10977(class_124.field_1060).method_10982(true);
            }));
            ChatUtils.sendMsg(class_5250VarMethod_434733);
        }
        if (((Boolean) this.autoFindRegion.get()).booleanValue()) {
            class_5250 class_5250VarMethod_434734 = class_2561.method_43473();
            class_5250VarMethod_434734.method_10852(class_2561.method_43470(" 🔍 ").method_27694(param011 -> {
                return param011.method_10977(class_124.field_1076);
            }));
            class_5250VarMethod_434734.method_10852(class_2561.method_43470("Looking up current region...").method_27694(param012 -> {
                return param012.method_10977(class_124.field_1080);
            }));
            ChatUtils.sendMsg(class_5250VarMethod_434734);
        }
        ChatUtils.sendMsg(class_2561.method_43470("━━━━━━━━━━━━━━━━━━━━━━━━━━━").method_27694(param013 -> {
            return param013.method_10977(class_124.field_1063);
        }));
    }

    private void toastNotify(String param1, String param2) {
        if (this.mc.method_1566() != null) {
            this.mc.method_1566().method_1999(new DetectionToast(class_2561.method_43470("⚠ STAFF DETECTED"), class_2561.method_43470(param2 != null ? param1 + " [" + param2 + "]" : param1 + " [checking...]"), ((Integer) this.toastDuration.get()).intValue(), true));
        }
    }

    private void soundNotify(boolean param1) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        this.mc.field_1687.method_8396(this.mc.field_1724, this.mc.field_1724.method_24515(), param1 ? class_3417.field_38075 : class_3417.field_14627, class_3419.field_15250, ((Double) this.soundVolume.get()).floatValue(), param1 ? 0.5f : 1.5f);
    }

    private void drainFindCommands() {
        if (this.pendingFindCommands.isEmpty()) {
            return;
        }
        long v1 = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = this.pendingFindCommands.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> next = it.next();
            if (v1 >= next.getValue().longValue()) {
                String key = next.getKey();
                String strReplace = ((String) this.findCommand.get()).replace("{name}", key);
                if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
                    this.listeningForStaff = key;
                    this.lastDetectedStaffRegion = null;
                    this.listenUntil = System.currentTimeMillis() + (((long) ((Integer) this.listenWindowSec.get()).intValue()) * 1000);
                    this.mc.field_1724.field_3944.method_45730(strReplace);
                }
                it.remove();
                return;
            }
        }
    }

    private void drainPending() {
        if (this.pendingMessages.isEmpty()) {
            return;
        }
        long v1 = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = this.pendingMessages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> next = it.next();
            if (v1 >= next.getValue().longValue()) {
                String key = next.getKey();
                String strReplace = ((String) this.chatMessage.get()).replace("{staff}", key).replace("{region}", this.staffRegionCache.getOrDefault(key, "Unknown"));
                if (this.mc.field_1724 != null && this.mc.field_1724.field_3944 != null) {
                    this.mc.field_1724.field_3944.method_45729(strReplace);
                }
                it.remove();
            }
        }
    }

    /* JADX WARN: Type inference failed for: r0v11, types: [java.lang.String[], java.lang.String[][]] */
    /* JADX WARN: Type inference failed for: r0v3, types: [int[], int[][]] */
    static {
        for (int[] iArr : ENCODED_NAMES) {
            String strDec = dec(iArr);
            STAFF_LOOKUP.put(h(strDec.toLowerCase(Locale.ROOT)), strDec);
        }
        REGION_PATTERNS = new String[][]{new String[]{"eu west", "eu-west", "euw", "europe west", "europe-west"}, new String[]{"eu central", "eu-central", "euc", "europe central", "europe-central"}, new String[]{"na east", "na-east", "nae", "us east", "us-east", "north america east"}, new String[]{"na west", "na-west", "naw", "us west", "us-west", "north america west"}, new String[]{"asia", "as-", "sea", "southeast asia"}, new String[]{"oceania", "oce", "australia", "aus"}};
        REGION_LABELS = new String[]{"EU West", "EU Central", "NA East", "NA West", "Asia", "Oceania"};
    }
}
