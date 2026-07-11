package com.bassa.addon.mixin;

import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/mixin/HandledScreenAccessor.class */
@Mixin({class_465.class})
public interface HandledScreenAccessor {
    @Accessor("field_2776")
    int bassa$getX();

    @Accessor("field_2800")
    int bassa$getY();

    @Accessor("field_2792")
    int bassa$getBackgroundWidth();

    @Accessor("field_2779")
    int bassa$getBackgroundHeight();
}
