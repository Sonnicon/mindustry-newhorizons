package sonnicon.newhorizons.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.blocks.BlockMultiblockAssembly;
import sonnicon.newhorizons.world.blocks.beam.BlockBeamGeneratorBlock;
import sonnicon.newhorizons.world.blocks.beam.BlockLaserCondenser;
import sonnicon.newhorizons.world.blocks.beam.BlockMirror;
import sonnicon.newhorizons.world.blocks.beam.BlockSemiMirror;
import sonnicon.newhorizons.world.blocks.crystal.BlockCrystal;
import sonnicon.newhorizons.world.blocks.defence.BlockGlassWall;
import sonnicon.newhorizons.world.blocks.liquid.BlockPhaseConduitDispenser;
import sonnicon.newhorizons.world.blocks.sandbox.BlockPowerBeamSpawner;

import static mindustry.type.ItemStack.with;

public class Blocks implements ILoadContent{
    public static Block crystalWhite, mirror, semimirror, multiblockAssemblyBlock, glassWall, laserCondenser, powerbeamSpawner, beamGenerator, phaseConduitDispenser;

    @Override
    public void loadContent(){
        //todo balancing
        crystalWhite = new BlockCrystal("crystal-white");
        crystalWhite.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        mirror = new BlockMirror("mirror");
        mirror.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        semimirror = new BlockSemiMirror("semimirror");
        semimirror.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        multiblockAssemblyBlock = new BlockMultiblockAssembly("multiblock-assembler");
        multiblockAssemblyBlock.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        glassWall = new BlockGlassWall("metaglass-wall");
        glassWall.requirements(Category.defense, with(Items.metaglass, 6));
        glassWall.health = 100;

        laserCondenser = new BlockLaserCondenser("lasercondenser");
        laserCondenser.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
        laserCondenser.health = 400;

        powerbeamSpawner = new BlockPowerBeamSpawner("powerbeamspawner");
        powerbeamSpawner.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());

        beamGenerator = new BlockBeamGeneratorBlock("beamgenerator");
        beamGenerator.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
        beamGenerator.health = 400;

        phaseConduitDispenser = new BlockPhaseConduitDispenser("phaseConduitDispenser");
        phaseConduitDispenser.requirements(Category.liquid, with(Items.copper, 30, Items.lead, 25));
    }
}
