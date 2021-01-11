package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

import java.util.ArrayList;

public interface ICatchPowerBeam{

    void addPowerBeam(PowerBeam beam);

    boolean removePowerBeam(PowerBeam beam);

    ArrayList<PowerBeam> getPowerBeams();
}
