package com.bassa.addon.modules.suschunks;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/modules/suschunks/AlertDispatcher.class */
public final class AlertDispatcher {
    private final class_310 client;

    public AlertDispatcher(class_310 param1) {
        this.client = param1;
    }

    public void onMainThread(Runnable param1) {
        this.client.execute(param1);
    }

    public void susColumn(class_1923 param1, int param2, boolean param3, boolean param4) {
        onMainThread(() -> {
            if (this.client.field_1724 != null) {
                if (param3) {
                    int v5 = param1.method_33940();
                    ChatUtils.sendMsg(class_2561.method_43470("§9§lBassa§r§8 »§r §3sus chunk §8| §f" + v5 + " " + param1.method_33942() + " §8· §b" + param2 + " §7clusters"));
                }
                if (param4) {
                    this.client.field_1724.method_5783((class_3414) class_3417.field_14622.comp_349(), 1.0f, 1.2f);
                }
            }
        });
    }

    public void apiary(class_2338 param1, String param2, boolean param3, boolean param4) {
        onMainThread(() -> {
            if (this.client.field_1724 != null) {
                if (param3) {
                    ChatUtils.sendMsg(class_2561.method_43470("§9§lBassa§r§8 »§r §elvl5 " + param2 + " §8| §f" + param1.method_10263() + " " + param1.method_10264() + " " + param1.method_10260()));
                }
                if (param4) {
                    this.client.field_1724.method_5783(class_3417.field_14627, 1.0f, 1.2f);
                }
            }
        });
    }

    public void overgrowth(class_1923 param1, String param2, boolean param3, boolean param4) {
        onMainThread(() -> {
            if (this.client.field_1724 != null) {
                if (param3) {
                    ChatUtils.sendMsg(class_2561.method_43470("§9§lBassa§r§8 »§r §a" + param2 + " §8| §f" + param1.method_8326() + " " + param1.method_8328()));
                }
                if (param4) {
                    this.client.field_1724.method_5783(class_3417.field_14627, 1.0f, 1.2f);
                }
            }
        });
    }

    public void surfaceCrystal(class_1923 param1, int param2, boolean param3, boolean param4) {
        onMainThread(() -> {
            if (this.client.field_1724 != null) {
                if (param3) {
                    int v5 = param1.method_33940();
                    ChatUtils.sendMsg(class_2561.method_43470("§9§lBassa§r§8 »§r §dgeode §8| §f" + v5 + " " + param1.method_33942() + " §8· §d" + param2 + " §7clusters"));
                }
                if (param4) {
                    this.client.field_1724.method_5783(class_3417.field_26980, 0.6f, 1.05f);
                }
            }
        });
    }

    public void deepCrystal(class_1923 param1, boolean param2, boolean param3) {
        onMainThread(() -> {
            if (this.client.field_1724 != null) {
                if (param2) {
                    int v4 = param1.method_33940();
                    ChatUtils.sendMsg(class_2561.method_43470("§9§lBassa§r§8 »§r §5underground amethyst §8| §f" + v4 + " " + param1.method_33942()));
                }
                if (param3) {
                    this.client.field_1724.method_5783(class_3417.field_26980, 0.6f, 1.05f);
                }
            }
        });
    }
}
