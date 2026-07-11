package com.bassa.addon.ui.loading;

import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/KingLoadingScreen.class */
public final class KingLoadingScreen extends class_437 {
    private final LoadingConfig config;
    private final LoadingController controller;
    private final Runnable onComplete;
    private final LoadingProgress progress;
    private TextureCarousel carousel;
    private LoadingRenderer renderer;
    private boolean dispatched;

    public KingLoadingScreen(LoadingConfig param1, LoadingController param2, Runnable param3) {
        super(class_2561.method_43470("King Loading"));
        this.progress = new LoadingProgress();
        this.config = param1 == null ? LoadingConfig.defaults() : param1;
        this.controller = param2 == null ? new LoadingController() : param2;
        this.onComplete = param3;
    }

    public static KingLoadingScreen create(class_2960 param0, LoadingController param1, Runnable param2) {
        return new KingLoadingScreen(LoadingConfig.loadOrDefault(param0), param1, param2);
    }

    protected void method_25426() {
        this.carousel = new TextureCarousel(this.config.textures(), this.config.carouselDwellMillis(), this.config.carouselFadeMillis(), this.config.carouselEasing(), this.config.carouselBezier());
        this.renderer = new LoadingRenderer(this.carousel, this.config.progressEasing(), this.config.progressBezier());
        if (!this.dispatched) {
            this.dispatched = true;
            this.controller.start(this.progress, this.onComplete);
        }
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public void method_25394(class_332 param1, int param2, int param3, float param4) throws MatchException {
        if (this.renderer != null) {
            this.renderer.render(param1, this.progress, this.field_22789, this.field_22790);
        }
    }

    public void method_25420(class_332 param1, int param2, int param3, float param4) {
    }

    public boolean method_25422() {
        return false;
    }

    public LoadingProgress progressHandle() {
        return this.progress;
    }
}
