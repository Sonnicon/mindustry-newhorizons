package sonnicon.newhorizons.core;

import sonnicon.newhorizons.content.Blocks;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.types.IInit;
import sonnicon.newhorizons.types.ILoadContent;
import sonnicon.newhorizons.types.ILoadable;

import java.util.Arrays;

public class Vars{
    public static ILoadable[] loadables = {
            new Blocks(),
            new Types()
    };

    public static void init(){
        Arrays.stream(loadables)
                .filter(i -> i instanceof IInit)
                .forEachOrdered(i -> ((IInit) i).init());
    }

    public static void loadContent(){
        Arrays.stream(loadables)
                .filter(i -> i instanceof ILoadContent)
                .forEachOrdered(i -> ((ILoadContent) i).loadContent());
    }
}
