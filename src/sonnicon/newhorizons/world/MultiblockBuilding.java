package sonnicon.newhorizons.world;

import arc.Core;
import mindustry.Vars;
import mindustry.gen.Building;

public class MultiblockBuilding extends Building{

    @Override
    public void created(){
        Multiblock m = Multiblock.multiblocks.get(block());
        // not having it delayed partly makes a deepcopy
        Core.app.post(() -> m.place(tile, false));

        if(block().rotate){
            float drawx = 0f, drawy = 0f;
            switch(rotation()){
                case (0):{
                    drawx = m.drawOffsetX;
                    drawy = m.drawOffsetY;
                    break;
                }
                case (1):{
                    drawx = -m.drawOffsetY;
                    drawy = m.drawOffsetX;
                    break;
                }
                case (2):{
                    drawx = -m.drawOffsetX;
                    drawy = -m.drawOffsetY;
                    break;
                }
                case (3):{
                    drawx = m.drawOffsetY;
                    drawy = -m.drawOffsetX;
                    break;
                }
            }
            x += drawx * Vars.tilesize;
            y += drawy * Vars.tilesize;
        }
    }

    @Override
    public void onRemoved(){
        Multiblock.multiblocks.get(block()).remove(tile());
    }
}
