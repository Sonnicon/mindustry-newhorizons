package sonnicon.newhorizons.world;

import arc.graphics.g2d.Draw;
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
        if(origin.build != null && origin.build.block().rotate){
            switch(origin.build.rotation()){
                case (1):{
                    offsetX = -y;
                    offsetY = x;
                    break;
                }
                case (2):{
                    offsetX = -x;
                    offsetY = -y;
                    break;
                }
                case (3):{
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
        Draw.rect(block.region, t.drawx(), t.drawy());
    }
}
