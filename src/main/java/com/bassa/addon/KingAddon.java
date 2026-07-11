package com.bassa.addon;

import com.bassa.addon.modules.AmethystClusterEsp;
import com.bassa.addon.modules.ChunkFinder;
import com.bassa.addon.modules.ChunkReloader;
import com.bassa.addon.modules.LightESP;
import com.bassa.addon.modules.SpawnerFinder;
import com.bassa.addon.modules.SpawnerTags;
import com.bassa.addon.modules.StaffDetector;
import com.bassa.addon.modules.SusChunks;
import com.bassa.addon.ui.KingUiAssets;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.class_310;
import org.slf4j.Logger;

public class KingAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("King Debug");

    public void onInitialize() {
        LOG.info("Initializing King Debug");
        KingUiAssets.ensureLoaded(class_310.method_1551());
        Modules.get().add(new ChunkReloader());
        Modules.get().add(new AmethystClusterEsp());
        Modules.get().add(new SpawnerTags());
        Modules.get().add(new SusChunks());
        Modules.get().add(new SpawnerFinder());
        Modules.get().add(new StaffDetector());
        Modules.get().add(new ChunkFinder());
        Modules.get().add(new LightESP());
    }

    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    public String getPackage() {
        return "com.bassa.addon";
    }
}
