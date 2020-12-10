package sonnicon.newhorizons.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.blocks.CrystalBlock;
import sonnicon.newhorizons.world.blocks.MirrorBlock;

import static mindustry.type.ItemStack.with;

public class Blocks implements ILoadContent{
    public Block crystalWhite, mirror;

    @Override
    public void loadContent(){
        crystalWhite = new CrystalBlock("crystal-white"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
        }};

        mirror = new MirrorBlock("mirror"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

            size = 2;
        }};
    }
}
