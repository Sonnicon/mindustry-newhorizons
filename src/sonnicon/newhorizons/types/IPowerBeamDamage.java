package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

public interface IPowerBeamDamage{

    boolean shouldDamage(PowerBeam beam);

    void damage(PowerBeam beam);
}
