package sonnicon.newhorizons.content;

import mindustry.world.blocks.environment.Floor;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.floors.FloorSilicon;

public class Floors implements ILoadContent{
    public static Floor floorSilicon;

    @Override
    public void loadContent(){
        floorSilicon = new FloorSilicon("floorSilicon");
    }
}
