package sonnicon.newhorizons.core;

import sonnicon.newhorizons.types.Pair;

public class Util{

    public static float distance(float a, float b){
        float c = Math.abs(b - a) % 360f;
        return c > 180f ? 360f - c : c;
    }

    public static Pair<Float> blockRotationOffset(Pair<Float> output, float x, float y, float distance, int rotation){
        // could use trigonometric functions, but this is faster for block rotations
        switch(rotation % 4){
            case (0):{
                return output.set(x + distance, y);
            }
            case (1):{
                return output.set(x, y + distance);
            }
            case (2):{
                return output.set(x - distance, y);
            }
            case (3):{
                return output.set(x, y - distance);
            }
        }
        throw new IllegalStateException("If you're seeing this, the code is in what I thought was an unreachable state.\nI could give you advice for what to do. But honestly, why should you trust me? I clearly screwed this up.\nI'm writing a message that should never appear, yet I know it will probably appear someday.\nOn a deep level, I know I'm not up to this task. I'm so sorry.");
    }
}
