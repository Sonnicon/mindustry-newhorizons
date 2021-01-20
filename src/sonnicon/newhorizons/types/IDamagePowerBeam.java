package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

public interface IDamagePowerBeam{

    boolean shouldDamage(PowerBeam beam);

    void damage(PowerBeam beam);
}
