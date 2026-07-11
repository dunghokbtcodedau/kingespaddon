package com.bassa.addon.ui;

import net.minecraft.class_437;
import net.minecraft.class_465;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/KingUiScreenProfile.class */
public enum KingUiScreenProfile {
    FULL_MENU,
    CONTAINER,
    OVERLAY;

    public static KingUiScreenProfile classify(class_437 param0) {
        if (param0 instanceof class_465) {
            return CONTAINER;
        }
        return (param0.field_22789 <= 0 || param0.field_22790 <= 0 || param0.field_22789 >= 320) ? FULL_MENU : OVERLAY;
    }
}
