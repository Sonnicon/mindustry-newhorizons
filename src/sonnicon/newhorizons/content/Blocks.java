package sonnicon.newhorizons.content;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.world.blocks.MultiblockAssemblyBlock;
import sonnicon.newhorizons.world.blocks.beam.BeamGeneratorBlock;
import sonnicon.newhorizons.world.blocks.beam.LaserCondenserBlock;
import sonnicon.newhorizons.world.blocks.beam.MirrorBlock;
import sonnicon.newhorizons.world.blocks.beam.SemiMirrorBlock;
import sonnicon.newhorizons.world.blocks.crystal.*;
import sonnicon.newhorizons.world.blocks.defence.GlassWallBlock;
import sonnicon.newhorizons.world.blocks.sandbox.PowerBeamSpawnerBlock;

import static mindustry.type.ItemStack.with;

public class Blocks implements ILoadContent{
    public static Block crystalWhite, mirror, semimirror, multiblockAssemblyBlock, glassWall, laserCondenser, powerbeamSpawner, beamGenerator;

    @Override
    public void loadContent(){
        //todo balancing
        crystalWhite = new CrystalBlock("crystal-white");
        crystalWhite.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        mirror = new MirrorBlock("mirror");
        mirror.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        semimirror = new SemiMirrorBlock("semimirror");
        semimirror.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        multiblockAssemblyBlock = new MultiblockAssemblyBlock("multiblock-assembler");
        multiblockAssemblyBlock.requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));

        glassWall = new GlassWallBlock("metaglass-wall");
        glassWall.requirements(Category.defense, with(Items.metaglass, 6));
        glassWall.health = 100;

        laserCondenser = new LaserCondenserBlock("lasercondenser");
        laserCondenser.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
        laserCondenser.health = 400;

        powerbeamSpawner = new PowerBeamSpawnerBlock("powerbeamspawner");
        powerbeamSpawner.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());

        beamGenerator = new BeamGeneratorBlock("beamgenerator");
        beamGenerator.requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
        beamGenerator.health = 400;
    }
}
