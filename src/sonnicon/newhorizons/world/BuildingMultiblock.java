package sonnicon.newhorizons.world;

import arc.Core;
import arc.util.io.Reads;
import mindustry.Vars;
import mindustry.gen.Building;

public abstract class BuildingMultiblock extends Building{
    private boolean readed = false;

    @Override
    public void created(){
        // return if loading, will call from read() when entity is fully loaded.
        if(readed || !Vars.state.isMenu()){
            unbiasedCreated();
        }
    }

    // Doesn't get called at drastically different states based on whether placing or loading a save
    public void unbiasedCreated(){
        Multiblock m = Multiblock.multiblocks.get(block());
        Core.app.post(() -> m.place(tile(), false));

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

    @Override
    public void onRemoved(){
        Multiblock.multiblocks.get(block()).remove(tile());
    }

    @Override
    public void read(Reads read, byte revision){
        super.read(read);
        readed = true;
        created();
    }
}
