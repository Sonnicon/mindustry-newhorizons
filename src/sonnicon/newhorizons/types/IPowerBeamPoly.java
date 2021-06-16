package sonnicon.newhorizons.types;

import sonnicon.newhorizons.entities.PowerBeam;

public interface IPowerBeamPoly{
    // These don't actually change beam draw angle, only length
    float getInterceptX(PowerBeam beam, boolean end);

    float getInterceptY(PowerBeam beam, boolean end);

    float getInterceptRotation(PowerBeam beam, boolean end);
}
