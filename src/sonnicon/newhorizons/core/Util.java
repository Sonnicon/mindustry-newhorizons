package sonnicon.newhorizons.core;

public class Util{
    public static int distance(int a, int b){
        int c = Math.abs(b - a) % 360;
        return c > 180 ? 360 - c : c;
    }

    public static float distance(float a, float b){
        float c = Math.abs(b - a) % 360f;
        return c > 180f ? 360f - c : c;
    }
}
