package sonnicon.newhorizons;

import mindustry.mod.Mod;
import sonnicon.newhorizons.core.Vars;

public class Newhorizons extends Mod{

    @Override
    public void init(){
        Vars.init();
    }

    @Override
    public void loadContent(){
        Vars.loadContent();
    }
}
