package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

import java.util.ArrayList;

public interface IPowerBeamCatch extends IPowerBeamDamage{

    default boolean shouldCatch(PowerBeam beam){
        return true;
    }

    void addPowerBeam(PowerBeam beam);

    boolean removePowerBeam(PowerBeam beam);

    ArrayList<PowerBeam> getPowerBeams();

    @Override
    default boolean shouldDamage(PowerBeam beam){
        return false;
    }
}
