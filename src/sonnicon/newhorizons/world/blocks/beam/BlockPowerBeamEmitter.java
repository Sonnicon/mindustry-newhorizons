package sonnicon.newhorizons.world.blocks.beam;

import mindustry.Vars;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquid;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.Pair;

public class BlockPowerBeamEmitter extends Block{
    public BlockPowerBeamEmitter(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
        hasLiquids = true;
        outputsLiquid = false;
        hasPower = true;
        canOverdrive = false;
        consumes.add(new ConsumeLiquid(Liquids.cryofluid, 0.05f));
        consumes.power(15f);

        config(Float.class, BuildingPowerBeamSpawner::configure);
    }

    private static final Pair<Float, Float> temp = new Pair<>();

    public class BuildingPowerBeamSpawner extends Building{
        protected PowerBeam beam;

        @Override
        public void updateTile(){
            if(cons.valid()){
                beam.setPower(0.1f);
                consume();
            }else{
                beam.setPower(0f);
            }
        }

        @Override
        public void created(){
            Util.blockRotationOffset(temp, x, y, Vars.tilesize * size * .5f, rotation);
            beam = new PowerBeam(temp.getX(), temp.getY(), (4 - rotation()) * 90f);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            float rot = (4 - rotation()) * 90f;
            if(rot != beam.getRotation()){
                beam.setRotation(rot);
                Util.blockRotationOffset(temp, x, y, Vars.tilesize * size * .5f, rotation);
                beam.set(x, y);
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            beam.remove();
        }
    }
}
