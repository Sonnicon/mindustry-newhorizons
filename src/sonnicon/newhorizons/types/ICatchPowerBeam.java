package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

import java.util.ArrayList;

public interface ICatchPowerBeam extends IDamagePowerBeam{

    default boolean shouldCatch(PowerBeam beam){
        return true;
    }

    void addPowerBeam(PowerBeam beam);

    boolean removePowerBeam(PowerBeam beam);

    ArrayList<PowerBeam> getPowerBeams();

    @Override
    default boolean damage(PowerBeam beam){
        return false;
    }
}
