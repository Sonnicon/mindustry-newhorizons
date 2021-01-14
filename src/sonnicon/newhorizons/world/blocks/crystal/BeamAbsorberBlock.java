package sonnicon.newhorizons.world.blocks.crystal;

import mindustry.world.blocks.power.PowerGenerator;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.ICatchPowerBeam;
import sonnicon.newhorizons.world.MultiblockBuilding;

import java.util.ArrayList;

public class BeamAbsorberBlock extends PowerGenerator{
    public BeamAbsorberBlock(String name){
        super(name);

        absorbLasers = true;
        destructible = true;
        rotate = true;
        solid = true;
        update = true;
    }

    public class BeamAbsorberBlockBuilding extends MultiblockBuilding implements ICatchPowerBeam{
        protected ArrayList<PowerBeam> catchedBeams = new ArrayList<>();

        @Override
        public float getPowerProduction(){
            //todo balancing
            return (float) catchedBeams.stream().mapToDouble(PowerBeam::getPower).sum() * efficiency() * 100f;
        }

        @Override
        public boolean shouldCatch(PowerBeam beam){
            return true;
        }

        @Override
        public void addPowerBeam(PowerBeam beam){
            catchedBeams.add(beam);
        }

        @Override
        public boolean removePowerBeam(PowerBeam beam){
            return catchedBeams.remove(beam);
        }

        @Override
        public ArrayList<PowerBeam> getPowerBeams(){
            return catchedBeams;
        }
    }
}
