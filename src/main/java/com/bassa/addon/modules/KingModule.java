package com.bassa.addon.modules;

import java.lang.reflect.Field;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/KingModule.class */
public abstract class KingModule extends Module {
    protected KingModule(Category param1, String param2, String param3) {
        super(param1, "kingdebug-" + param2, param3);
        restoreTitle(Utils.nameToTitle(param2));
    }

    private void restoreTitle(String param1) {
        try {
            Field declaredField = Module.class.getDeclaredField("title");
            declaredField.setAccessible(true);
            declaredField.set(this, param1);
        } catch (ReflectiveOperationException unused) {
        }
    }
}
