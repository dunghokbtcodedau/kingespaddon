package com.bassa.addon.ui.loading;

/* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/Easing.class */
public final class Easing {
    private Easing() {
    }

    /* JADX INFO: Thrown type has an unknown type hierarchy: java.lang.MatchException */
    public static float apply(Mode param0, float param1, float param2, float param3, float param4, float param5) throws MatchException {
        float v6;
        float param12 = clamp01(param1);
        switch (param0) {
            case LINEAR:
                v6 = param12;
                break;
            case EASE_IN_OUT_CUBIC:
                v6 = easeInOutCubic(param12);
                break;
            case EASE_OUT_EXPO:
                v6 = easeOutExpo(param12);
                break;
            case EASE_IN_OUT_EXPO:
                v6 = easeInOutExpo(param12);
                break;
            case CUBIC_BEZIER:
                v6 = cubicBezier(param2, param3, param4, param5, param12);
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
        return v6;
    }

    public static float easeInOutCubic(float param0) {
        float param02 = clamp01(param0);
        return param02 < 0.5f ? 4.0f * param02 * param02 * param02 : 1.0f - (((float) Math.pow(((-2.0f) * param02) + 2.0f, 3.0d)) / 2.0f);
    }

    public static float easeOutExpo(float param0) {
        if (clamp01(param0) >= 1.0f) {
            return 1.0f;
        }
        return 1.0f - ((float) Math.pow(2.0d, (-10.0f) * param0));
    }

    public static float easeInOutExpo(float param0) {
        float param02 = clamp01(param0);
        if (param02 == 0.0f) {
            return 0.0f;
        }
        if (param02 == 1.0f) {
            return 1.0f;
        }
        return param02 < 0.5f ? ((float) Math.pow(2.0d, (20.0f * param02) - 10.0f)) / 2.0f : (2.0f - ((float) Math.pow(2.0d, ((-20.0f) * param02) + 10.0f))) / 2.0f;
    }

    public static float cubicBezier(float param0, float param1, float param2, float param3, float param4) {
        float v5 = solveBezierParam(clamp01(param4), param0, param2);
        return bezierAxis(v5, param1, param3);
    }

    private static float bezierAxis(float param0, float param1, float param2) {
        float v3 = 1.0f - param0;
        return (3.0f * v3 * v3 * param0 * param1) + (3.0f * v3 * param0 * param0 * param2) + (param0 * param0 * param0);
    }

    private static float bezierAxisDerivative(float param0, float param1, float param2) {
        float v3 = 1.0f - param0;
        return (3.0f * v3 * v3 * param1) + (6.0f * v3 * param0 * (param2 - param1)) + (3.0f * param0 * param0 * (1.0f - param2));
    }

    private static float solveBezierParam(float param0, float param1, float param2) {
        float v3 = param0;
        for (int v4 = 0; v4 < 8; v4++) {
            float v5 = bezierAxis(v3, param1, param2) - param0;
            if (Math.abs(v5) < 1.0E-5f) {
                return v3;
            }
            float v6 = bezierAxisDerivative(v3, param1, param2);
            if (Math.abs(v6) < 1.0E-6f) {
                break;
            }
            v3 -= v5 / v6;
        }
        float f = 0.0f;
        float v52 = 1.0f;
        float v32 = param0;
        for (int i = 0; i < 24; i++) {
            float v7 = bezierAxis(v32, param1, param2);
            if (Math.abs(v7 - param0) < 1.0E-5f) {
                break;
            }
            if (v7 < param0) {
                f = v32;
            } else {
                v52 = v32;
            }
            v32 = (f + v52) / 2.0f;
        }
        return v32;
    }

    private static float clamp01(float param0) {
        if (Float.isNaN(param0) || param0 < 0.0f) {
            return 0.0f;
        }
        if (param0 > 1.0f) {
            return 1.0f;
        }
        return param0;
    }

    /* JADX INFO: loaded from: 1.jar:com/bassa/addon/ui/loading/Easing$Mode.class */
    public enum Mode {
        LINEAR,
        EASE_IN_OUT_CUBIC,
        EASE_OUT_EXPO,
        EASE_IN_OUT_EXPO,
        CUBIC_BEZIER;

        private static Mode[] $values() {
            return new Mode[]{LINEAR, EASE_IN_OUT_CUBIC, EASE_OUT_EXPO, EASE_IN_OUT_EXPO, CUBIC_BEZIER};
        }
    }
}
