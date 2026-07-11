package com.bassa.addon.modules.suschunks;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1923;
import net.minecraft.class_2338;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/ScanRegistry.class */
public final class ScanRegistry {
    public final Set<class_1923> anomalySeeds = ConcurrentHashMap.newKeySet();
    public final Set<class_1923> mapOverlayColumns = ConcurrentHashMap.newKeySet();
    public final Set<class_1923> scheduledColumns = ConcurrentHashMap.newKeySet();
    public final Map<class_2338, String> apiarySites = new ConcurrentHashMap();
    public final Set<class_2338> apiaryAnnounced = ConcurrentHashMap.newKeySet();
    public final Map<class_1923, OvergrowthKind> overgrowthByColumn = new ConcurrentHashMap();
    public final Map<class_1923, Set<class_2338>> crystalDeposits = new ConcurrentHashMap();
    public final Map<class_1923, class_2338> crystalLineAnchors = new ConcurrentHashMap();
    public final Set<class_1923> deepCrystalColumns = ConcurrentHashMap.newKeySet();
    public final Set<class_1923> crystalAlerted = ConcurrentHashMap.newKeySet();
    public final Set<class_1923> deepCrystalAlerted = ConcurrentHashMap.newKeySet();

    public void reset() {
        this.anomalySeeds.clear();
        this.mapOverlayColumns.clear();
        this.scheduledColumns.clear();
        this.apiarySites.clear();
        this.apiaryAnnounced.clear();
        this.overgrowthByColumn.clear();
        this.crystalDeposits.clear();
        this.crystalLineAnchors.clear();
        this.deepCrystalColumns.clear();
        this.crystalAlerted.clear();
        this.deepCrystalAlerted.clear();
    }
}
