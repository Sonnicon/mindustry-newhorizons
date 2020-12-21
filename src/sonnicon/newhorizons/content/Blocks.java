package sonnicon.newhorizons.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.blocks.crystal.CrystalBlock;
import sonnicon.newhorizons.world.blocks.crystal.MirrorBlock;
import sonnicon.newhorizons.world.blocks.MultiblockAssemblyBlock;
import sonnicon.newhorizons.world.blocks.crystal.SemiMirrorBlock;
import sonnicon.newhorizons.world.blocks.defence.GlassWallBlock;

import static mindustry.type.ItemStack.with;

public class Blocks implements ILoadContent{
    public static Block crystalWhite, mirror, semimirror, multiblockAssemblyBlock, glassWall;

    @Override
    public void loadContent(){
        crystalWhite = new CrystalBlock("crystal-white"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
        }};

        mirror = new MirrorBlock("mirror"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

            size = 2;
        }};

        semimirror = new SemiMirrorBlock("semimirror"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

            size = 2;
        }};

        multiblockAssemblyBlock = new MultiblockAssemblyBlock("multiblock-assembler"){{
            requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
        }};

        glassWall = new GlassWallBlock("metaglass-wall"){{
            requirements(Category.defense, with(Items.metaglass, 6));
            hasShadow = false;
            fillsTile = false;
            health = 100;
        }};
    }
}
