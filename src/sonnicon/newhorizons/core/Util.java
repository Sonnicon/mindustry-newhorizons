package sonnicon.newhorizons.core;

import arc.util.Log;
import mindustry.world.Block;
import mindustry.world.Tile;
import sonnicon.newhorizons.types.Pair;

import java.lang.reflect.Field;

public class Util{

    protected static Field blockField;

    // Shortest distance between two angles (deg)
    public static float distance(float a, float b){
        float c = Math.abs(b - a) % 360f;
        return c > 180f ? 360f - c : c;
    }

    // Offset x and y by distance based on rotation (like sin but only cardinal directions)
    public static Pair<Float, Float> blockRotationOffset(Pair<Float, Float> output, float x, float y, float distance, int rotation){
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

    // Better this than eternally casting to and from doubles
    public static float ceil(float value){
        if(value > (int) value){
            return (int) value + 1;
        }else{
            return value;
        }
    }

    // Set a block without all the side effects
    public static void setTileBlock(Tile tile, Block block){
        try{
            if(blockField == null){
                blockField = Tile.class.getDeclaredField("block");
                blockField.setAccessible(true);
            }
            blockField.set(tile, block);
        }catch(NoSuchFieldException | IllegalAccessException ex){
            Log.err(ex);
            ex.printStackTrace();
        }
    }
}
