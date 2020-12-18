package sonnicon.newhorizons.world;

import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Tile;

public class RelativeBlock{
    public final Block block;
    public final int x, y;

    public RelativeBlock(Block block, int x, int y){
        this.block = block;
        this.x = x;
        this.y = y;
    }

    public boolean check(Tile origin){
        Tile target = fetch(origin);
        return target != null && target.block() == block && target.team() == origin.team();
    }

    public Tile fetch(Tile origin){
        int offsetX = x, offsetY = y;
        if(origin.build != null && origin.block().rotate){
            switch(origin.build.rotation()){
                case(0):{
                    offsetY |= -1;
                    break;
                }
                case(3):{
                    offsetY |= -1;
                }
                case(2):{
                    offsetX |= -1;
                    break;
                }
            }
        }

        return Vars.world.tile(origin.x + offsetX, origin.y + offsetY);
    }
}
