package sonnicon.newhorizons.content;


import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.Multiblock;

import static mindustry.content.Blocks.*;
import static sonnicon.newhorizons.world.RelativeBlock.relativeBlock;

public class Multiblocks implements ILoadContent{
    public static Multiblock laserCondesner, beamAbsorber;

    @Override
    public void loadContent(){
        //todo balancing
        laserCondesner = new Multiblock(Blocks.laserCondenser,
                relativeBlock(Blocks.glassWall, -1, 2),
                relativeBlock(liquidJunction, 0, 2),
                relativeBlock(Blocks.glassWall, -1, 1),
                relativeBlock(diode, 0, 1),
                relativeBlock(arc, 1, 1),
                relativeBlock(Blocks.glassWall, -1, 0)
        );

        beamAbsorber = new Multiblock(Blocks.beamGenerator,
                relativeBlock(Blocks.glassWall, -3, 0),
                relativeBlock(battery, -2, 0),
                relativeBlock(battery, -1, 0),
                relativeBlock(surgeWall, -2, 1),
                relativeBlock(surgeWall, -1, 1),
                relativeBlock(surgeWall, -2, -1),
                relativeBlock(surgeWall, -1, -1)
        );
    }
}
