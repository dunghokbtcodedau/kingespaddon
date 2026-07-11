package com.bassa.addon.modules;

import com.bassa.addon.KingAddon;
import com.bassa.addon.modules.suschunks.AlertDispatcher;
import com.bassa.addon.modules.suschunks.ChunkOverlayRenderer;
import com.bassa.addon.modules.suschunks.DetectorContext;
import com.bassa.addon.modules.suschunks.RegionScanCoordinator;
import com.bassa.addon.ui.KingUiLayout;
import java.util.Objects;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/SusChunks.class */
public class SusChunks extends KingModule {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDetectors;
    private final SettingGroup sgColors;
    private final Setting<Boolean> chatAlerts;
    private final Setting<Boolean> soundAlert;
    private final Setting<Integer> sensitivity;
    private final Setting<Integer> chunkAlpha;
    private final Setting<Boolean> beehiveFinder;
    private final Setting<Integer> beehiveRange;
    private final Setting<Boolean> growthFinder;
    private final Setting<Integer> growthMinVines;
    private final Setting<Boolean> geodeFinder;
    private final Setting<Integer> geodeMinClusters;
    private final Setting<Boolean> geodeUnderground;
    private final Setting<Boolean> geodeEsp;
    private final Setting<Boolean> geodeTracers;
    private final Setting<SettingColor> susColor;
    private final Setting<SettingColor> vinesColor;
    private final Setting<SettingColor> clustersColor;
    private final Setting<SettingColor> kelpColor;
    private final Setting<SettingColor> geodeChunkColor;
    private final Setting<SettingColor> geodeEspColor;
    private final Setting<SettingColor> geodeTracerColor;
    private final Setting<SettingColor> beehiveColor;
    private final Setting<SettingColor> beehiveChunkColor;
    private final RegionScanCoordinator coordinator;
    private final ChunkOverlayRenderer overlay;

    public SusChunks() {
        super(KingAddon.CATEGORY, "sus-chunks", "Flags cluster concentrations and suspicious regions across chunk sets without performance impact.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDetectors = this.settings.createGroup("Detectors");
        this.sgColors = this.settings.createGroup("Colors");
        this.chatAlerts = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("chat-alerts")).description("Send a chat message when suspicious chunks are flagged.")).defaultValue(true)).build());
        this.soundAlert = this.sgGeneral.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("sound-alerts")).description("Play a sound when suspicious chunks are flagged.")).defaultValue(true)).build());
        this.sensitivity = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("sensitivity")).description("Minimum number of hidden cluster signatures in a chunk before it is flagged.")).defaultValue(30)).min(1).max(100).sliderRange(1, 100).build());
        this.chunkAlpha = this.sgGeneral.add(((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("chunk-alpha")).description("Fill alpha of the flagged chunk markers.")).defaultValue(80)).min(0).max(255).sliderRange(0, 255).build());
        this.beehiveFinder = this.sgDetectors.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("beehive-detector")).description("Also find and flag fully grown (level 5) beehives and bee nests.")).defaultValue(false)).build());
        SettingGroup settingGroup = this.sgDetectors;
        IntSetting.Builder builderSliderRange = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("beehive-range")).description("Block radius to search for beehives/nests.")).defaultValue(Integer.valueOf(KingUiLayout.TITLE_LOGO_WIDTH))).min(8).sliderRange(8, 512);
        Setting<Boolean> setting = this.beehiveFinder;
        Objects.requireNonNull(setting);
        Objects.requireNonNull(setting);
        this.beehiveRange = settingGroup.add(((IntSetting.Builder) builderSliderRange.visible(setting::get)).build());
        this.growthFinder = this.sgDetectors.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("overgrown-detector")).description("Also flag chunks with grown vines, amethyst clusters, or full-grown kelp.")).defaultValue(false)).build());
        SettingGroup settingGroup2 = this.sgDetectors;
        IntSetting.Builder builderSliderRange2 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("growth-min-vines")).description("Minimum vine chain length before a chunk is flagged.")).defaultValue(15)).min(1).max(128).sliderRange(1, 50);
        Setting<Boolean> setting2 = this.growthFinder;
        Objects.requireNonNull(setting2);
        Objects.requireNonNull(setting2);
        this.growthMinVines = settingGroup2.add(((IntSetting.Builder) builderSliderRange2.visible(setting2::get)).build());
        this.geodeFinder = this.sgDetectors.add(((BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("geode-detector")).description("Also detect amethyst geodes (block ESP + chunk mark + tracers).")).defaultValue(false)).build());
        SettingGroup settingGroup3 = this.sgDetectors;
        IntSetting.Builder builderSliderRange3 = ((IntSetting.Builder) ((IntSetting.Builder) ((IntSetting.Builder) new IntSetting.Builder().name("geode-min-clusters")).description("Minimum cluster signatures in a chunk to flag a geode.")).defaultValue(5)).min(1).max(60).sliderRange(1, 60);
        Setting<Boolean> setting3 = this.geodeFinder;
        Objects.requireNonNull(setting3);
        Objects.requireNonNull(setting3);
        this.geodeMinClusters = settingGroup3.add(((IntSetting.Builder) builderSliderRange3.visible(setting3::get)).build());
        SettingGroup settingGroup4 = this.sgDetectors;
        BoolSetting.Builder builder = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("geode-underground")).description("Also flag deep (Y 0-5) amethyst as underground geodes.")).defaultValue(true);
        Setting<Boolean> setting4 = this.geodeFinder;
        Objects.requireNonNull(setting4);
        Objects.requireNonNull(setting4);
        this.geodeUnderground = settingGroup4.add(((BoolSetting.Builder) builder.visible(setting4::get)).build());
        SettingGroup settingGroup5 = this.sgDetectors;
        BoolSetting.Builder builder2 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("geode-block-esp")).description("Render a box on each detected geode cluster.")).defaultValue(true);
        Setting<Boolean> setting5 = this.geodeFinder;
        Objects.requireNonNull(setting5);
        Objects.requireNonNull(setting5);
        this.geodeEsp = settingGroup5.add(((BoolSetting.Builder) builder2.visible(setting5::get)).build());
        SettingGroup settingGroup6 = this.sgDetectors;
        BoolSetting.Builder builder3 = (BoolSetting.Builder) ((BoolSetting.Builder) ((BoolSetting.Builder) new BoolSetting.Builder().name("geode-tracers")).description("Draw a tracer to the nearest cluster in each geode chunk.")).defaultValue(true);
        Setting<Boolean> setting6 = this.geodeFinder;
        Objects.requireNonNull(setting6);
        Objects.requireNonNull(setting6);
        this.geodeTracers = settingGroup6.add(((BoolSetting.Builder) builder3.visible(setting6::get)).build());
        this.susColor = this.sgColors.add(((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("sus-chunk-color")).description("Color for flagged suspicious chunks (alpha driven by chunk-alpha).")).defaultValue(new SettingColor(255, 0, 0, 80)).build());
        SettingGroup settingGroup7 = this.sgColors;
        ColorSetting.Builder builderDefaultValue = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("vines-color")).description("Color for grown-vine chunks (Overgrown Detector).")).defaultValue(new SettingColor(170, 0, 255, 80));
        Setting<Boolean> setting7 = this.growthFinder;
        Objects.requireNonNull(setting7);
        Objects.requireNonNull(setting7);
        this.vinesColor = settingGroup7.add(((ColorSetting.Builder) builderDefaultValue.visible(setting7::get)).build());
        SettingGroup settingGroup8 = this.sgColors;
        ColorSetting.Builder builderDefaultValue2 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("clusters-color")).description("Color for grown-cluster chunks (Overgrown Detector).")).defaultValue(new SettingColor(0, 255, 0, 80));
        Setting<Boolean> setting8 = this.growthFinder;
        Objects.requireNonNull(setting8);
        Objects.requireNonNull(setting8);
        this.clustersColor = settingGroup8.add(((ColorSetting.Builder) builderDefaultValue2.visible(setting8::get)).build());
        SettingGroup settingGroup9 = this.sgColors;
        ColorSetting.Builder builderDefaultValue3 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("kelp-color")).description("Color for grown-kelp chunks (Overgrown Detector).")).defaultValue(new SettingColor(0, 200, 255, 80));
        Setting<Boolean> setting9 = this.growthFinder;
        Objects.requireNonNull(setting9);
        Objects.requireNonNull(setting9);
        this.kelpColor = settingGroup9.add(((ColorSetting.Builder) builderDefaultValue3.visible(setting9::get)).build());
        SettingGroup settingGroup10 = this.sgColors;
        ColorSetting.Builder builderDefaultValue4 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("geode-chunk-color")).description("Chunk-mark color for geodes (Geode Detector).")).defaultValue(new SettingColor(180, 100, 255, 200));
        Setting<Boolean> setting10 = this.geodeFinder;
        Objects.requireNonNull(setting10);
        Objects.requireNonNull(setting10);
        this.geodeChunkColor = settingGroup10.add(((ColorSetting.Builder) builderDefaultValue4.visible(setting10::get)).build());
        SettingGroup settingGroup11 = this.sgColors;
        ColorSetting.Builder builderDefaultValue5 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("geode-esp-color")).description("Block-ESP color for geode clusters (Geode Detector).")).defaultValue(new SettingColor(180, 100, 255, 180));
        Setting<Boolean> setting11 = this.geodeFinder;
        Objects.requireNonNull(setting11);
        Objects.requireNonNull(setting11);
        this.geodeEspColor = settingGroup11.add(((ColorSetting.Builder) builderDefaultValue5.visible(setting11::get)).build());
        SettingGroup settingGroup12 = this.sgColors;
        ColorSetting.Builder builderDefaultValue6 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("geode-tracer-color")).description("Tracer color for geodes (Geode Detector).")).defaultValue(new SettingColor(180, 100, 255, 220));
        Setting<Boolean> setting12 = this.geodeFinder;
        Objects.requireNonNull(setting12);
        Objects.requireNonNull(setting12);
        this.geodeTracerColor = settingGroup12.add(((ColorSetting.Builder) builderDefaultValue6.visible(setting12::get)).build());
        SettingGroup settingGroup13 = this.sgColors;
        ColorSetting.Builder builderDefaultValue7 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("beehive-color")).description("Block-ESP color for beehives/nests (Beehive Detector).")).defaultValue(new SettingColor(255, 255, 0, 255));
        Setting<Boolean> setting13 = this.beehiveFinder;
        Objects.requireNonNull(setting13);
        Objects.requireNonNull(setting13);
        this.beehiveColor = settingGroup13.add(((ColorSetting.Builder) builderDefaultValue7.visible(setting13::get)).build());
        SettingGroup settingGroup14 = this.sgColors;
        ColorSetting.Builder builderDefaultValue8 = ((ColorSetting.Builder) ((ColorSetting.Builder) new ColorSetting.Builder().name("beehive-chunk-color")).description("Chunk-mark color for beehives/nests (Beehive Detector).")).defaultValue(new SettingColor(255, 255, 0, 45));
        Setting<Boolean> setting14 = this.beehiveFinder;
        Objects.requireNonNull(setting14);
        Objects.requireNonNull(setting14);
        this.beehiveChunkColor = settingGroup14.add(((ColorSetting.Builder) builderDefaultValue8.visible(setting14::get)).build());
        this.coordinator = new RegionScanCoordinator();
        this.overlay = new ChunkOverlayRenderer(this.coordinator.registry(), this.growthFinder, this.geodeFinder, this.beehiveFinder, this.geodeEsp, this.geodeTracers, this.chunkAlpha, this.susColor, this.vinesColor, this.clustersColor, this.kelpColor, this.geodeChunkColor, this.geodeEspColor, this.geodeTracerColor, this.beehiveColor, this.beehiveChunkColor);
    }

    public void onActivate() {
        this.coordinator.boot(buildContext());
    }

    public void onDeactivate() {
        this.coordinator.halt();
    }

    @EventHandler
    private void onTick(TickEvent.Post param1) {
        this.coordinator.pulse();
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    @EventHandler
    private void onRender3D(Render3DEvent param1) throws MatchException {
        this.overlay.draw(param1, this.mc);
    }

    private DetectorContext buildContext() {
        return new DetectorContext(this.mc, this.coordinator.registry(), new AlertDispatcher(this.mc), this.chatAlerts, this.soundAlert, this.sensitivity, this.beehiveFinder, this.beehiveRange, this.growthFinder, this.growthMinVines, this.geodeFinder, this.geodeMinClusters, this.geodeUnderground);
    }
}
