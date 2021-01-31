package sonnicon.newhorizons.types;

import mindustry.type.Liquid;

import java.util.HashMap;

public final class CrystalLiquid{
    // Liquid that this object represents
    private final Liquid liquid;
    // Multiplier for how much crystal capacity this liquid takes
    private final float absorbAmount;
    // Multiplier for how fast this liquid is absorbed
    private final float absorbSpeed;
    // Passive effect on crystal integrity
    private final float integrityRate;

    private static final HashMap<Liquid, CrystalLiquid> crystalLiquids = new HashMap<>();

    public CrystalLiquid(Liquid liquid, float absorbAmount, float absorbSpeed, float integrityRate){
        this.liquid = liquid;
        this.absorbAmount = absorbAmount;
        this.absorbSpeed = absorbSpeed;
        this.integrityRate = integrityRate;

        crystalLiquids.put(liquid, this);
    }

    public static CrystalLiquid get(Liquid liquid){
        CrystalLiquid cl = crystalLiquids.get(liquid);
        if(cl == null){
            return new CrystalLiquid(liquid, 1f, 1f, 1f);
        }
        return cl;
    }

    public Liquid getLiquid(){
        return liquid;
    }

    public float getAbsorbAmount(){
        return absorbAmount;
    }

    public float getAbsorbSpeed(){
        return absorbSpeed;
    }

    public float getIntegrityRate(){
        return integrityRate;
    }
}
