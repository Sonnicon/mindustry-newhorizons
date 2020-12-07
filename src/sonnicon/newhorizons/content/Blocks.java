package sonnicon.newhorizons.content;

import mindustry.world.Block;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.blocks.CrystalBlock;

public class Blocks implements ILoadContent{
    public Block crystalWhite;

    @Override
    public void loadContent(){
        crystalWhite = new CrystalBlock("crystal-white");
    }
}
