package sonnicon.newhorizons.world.blocks.beam;

import mindustry.world.Tile;
import mindustry.world.blocks.power.PowerGenerator;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.ICatchPowerBeam;
import sonnicon.newhorizons.world.Multiblock;
import sonnicon.newhorizons.world.MultiblockBuilding;
import sonnicon.newhorizons.world.RelativeBlock;

import java.util.ArrayList;
import java.util.Comparator;

public class BeamGeneratorBlock extends PowerGenerator{
    public BeamGeneratorBlock(String name){
        super(name);

        destructible = true;
        rotate = true;
        solid = true;
        update = true;
    }

    public class BeamAbsorberBlockBuilding extends MultiblockBuilding implements ICatchPowerBeam{
        protected ArrayList<PowerBeam> catchedBeams = new ArrayList<>();
        protected Tile catchyTile;

        @Override
        public void unbiasedCreated(){
            super.unbiasedCreated();
            RelativeBlock catchyRelative = Multiblock.getMultiblock(block()).getBlocks().stream().min(Comparator.comparingInt(rel -> rel.x)).orElse(null);
            // Shouldn't happen
            if(catchyRelative == null){
                kill();
                return;
            }
            catchyTile = catchyRelative.fetch(tile());
        }

        @Override
        public float getPowerProduction(){
            //todo balancing
            return (float) catchedBeams.stream().mapToDouble(PowerBeam::getPower).sum() * efficiency() * 100f;
        }

        @Override
        public boolean shouldCatch(PowerBeam beam){
            return Math.abs(catchyTile.worldx() - beam.getEndX()) < 0.001f && Math.abs(catchyTile.worldy() - beam.getEndY()) < 0.001f;
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

        @Override
        public boolean shouldDamage(PowerBeam beam){
            return !shouldCatch(beam);
        }

        @Override
        public void damage(PowerBeam beam){
            beam.damage(this);
        }
    }
}
