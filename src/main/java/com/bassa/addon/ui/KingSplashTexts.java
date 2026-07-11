package com.bassa.addon.ui;

import net.minecraft.class_2561;
import net.minecraft.class_5819;
import net.minecraft.class_8519;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingSplashTexts.class */
public final class KingSplashTexts {
    private static final class_5819 RANDOM = class_5819.method_43047();
    private static final String[] LINES = {"https://discord.gg/b7DdS3f6Fh", "Kingdebug Addon Is the Best", "Kingdebug", "I Love Kingdebug Addon"};

    private KingSplashTexts() {
    }

    public static class_8519 randomRenderer() {
        return new class_8519(styled(LINES[RANDOM.method_43048(LINES.length)]));
    }

    private static class_2561 styled(String param0) {
        return class_2561.method_43470(param0).method_27694(param02 -> {
            return param02.method_36139(16776960);
        });
    }
}
