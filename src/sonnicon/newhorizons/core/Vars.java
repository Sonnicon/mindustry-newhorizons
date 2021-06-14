package sonnicon.newhorizons.core;

import mindustry.mod.Mods;
import sonnicon.newhorizons.Newhorizons;
import sonnicon.newhorizons.content.Blocks;
import sonnicon.newhorizons.content.Floors;
import sonnicon.newhorizons.content.Multiblocks;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.graphics.Shaders;
import sonnicon.newhorizons.types.ILoadInit;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.types.ILoadable;
import sonnicon.newhorizons.world.floors.FloorSilicon;

import java.util.Arrays;

public class Vars{
    protected static final ILoadable[] loadables = {
            new Shaders(),
            new Floors(),
            new Blocks(),
            new Multiblocks(),
            new Types()
    };

    public static final Mods.LoadedMod mod = mindustry.Vars.mods.getMod(Newhorizons.class);

    public static void init(){
        Arrays.stream(loadables)
                .filter(i -> i instanceof ILoadInit)
                .forEachOrdered(i -> ((ILoadInit) i).initialize());
        PowerBeam.initialize();
        FloorSilicon.initialize();
    }

    public static void loadContent(){
        Arrays.stream(loadables)
                .filter(i -> i instanceof ILoadContent)
                .forEachOrdered(i -> ((ILoadContent) i).loadContent());
    }
}
