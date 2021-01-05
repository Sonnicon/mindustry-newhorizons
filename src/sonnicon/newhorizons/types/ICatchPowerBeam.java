package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

public interface ICatchPowerBeam{

    void addPowerBeam(PowerBeam beam);

    boolean removePowerBeam(PowerBeam beam);
}
