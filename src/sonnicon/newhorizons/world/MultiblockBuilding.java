package sonnicon.newhorizons.world;

import mindustry.gen.Building;

public class MultiblockBuilding extends Building{
    @Override
    public void onRemoved(){
        Multiblock.multiblocks.get(block()).remove(tile());
    }
}
