package sonnicon.newhorizons.world.blocks.crystal;

import mindustry.world.Block;
import sonnicon.newhorizons.world.MultiblockBuilding;

public class LaserCondenserBlock extends Block{
    public LaserCondenserBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
    }

    public class LaserCondenserBlockBuilding extends MultiblockBuilding{
    }
}
