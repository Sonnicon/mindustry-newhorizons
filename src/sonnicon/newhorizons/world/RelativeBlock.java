package sonnicon.newhorizons.world;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.HashMap;

public class RelativeBlock{
    public final Block block;
    public final short x, y;

    public static final HashMap<Block, HashMap<Integer, RelativeBlock>> relativeBlocks = new HashMap<>();

    // making marginally less objects
    public static RelativeBlock relativeBlock(Block block, short x, short y){
        int position = (x << 16) | y;
        HashMap<Integer, RelativeBlock> layer2 = relativeBlocks.getOrDefault(block, relativeBlocks.put(block, new HashMap<>()));
        return layer2.getOrDefault(position, layer2.put(position, new RelativeBlock(block, x, y)));
    }

    public static RelativeBlock relativeBlock(Block block, int x, int y){
        return relativeBlock(block, (short) x, (short) y);
    }

    protected RelativeBlock(Block block, short x, short y){
        this.block = block;
        this.x = x;
        this.y = y;
    }

    public boolean check(Tile origin){
        return isValid(fetch(origin));
    }

    protected boolean isValid(Tile tile){
        return tile != null && tile.block() == block && tile.team() == tile.team();
    }

    public Tile fetch(Tile origin){
        int offsetX = x, offsetY = y;
        if(origin.build != null && origin.build.block().rotate){
            switch(origin.build.rotation()){
                case (1):{
                    offsetX = -y;
                    //noinspection SuspiciousNameCombination
                    offsetY = x;
                    break;
                }
                case (2):{
                    offsetX = -x;
                    offsetY = -y;
                    break;
                }
                case (3):{
                    //noinspection SuspiciousNameCombination
                    offsetX = y;
                    offsetY = -x;
                    break;
                }
            }
        }

        return Vars.world.tile(origin.x + offsetX, origin.y + offsetY);
    }

    public void draw(Tile origin){
        Tile t = fetch(origin);
        if(isValid(t)) return;

        if(t.block().isAir()){
            Draw.rect(block.region, t.drawx(), t.drawy());
        }else{
            Draw.color(Color.red, 0.5f);
            Draw.z(Layer.blockOver);
            Draw.rect("blank", t.worldx(), t.worldy(), Vars.tilesize, Vars.tilesize);
            // apparently Draw.color() resets alpha
            Draw.color(1, 1, 1, 0.5f);
            Draw.z(Layer.block);
        }
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof RelativeBlock &&
                ((RelativeBlock) obj).block == block &&
                ((RelativeBlock) obj).x == x &&
                ((RelativeBlock) obj).y == y;
    }
}
