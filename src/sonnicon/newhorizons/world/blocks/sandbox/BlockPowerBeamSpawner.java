package sonnicon.newhorizons.world.blocks.sandbox;

import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Block;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.Pair;

public class BlockPowerBeamSpawner extends Block{
    public BlockPowerBeamSpawner(String name){
        super(name);

        destructible = true;
        solid = true;
        configurable = true;
        rotate = true;

        config(Float.class, BuildingPowerBeamSpawner::configure);
    }

    private static final Pair<Float, Float> temp = new Pair<>();

    public class BuildingPowerBeamSpawner extends Building{
        protected PowerBeam beam;
        protected float power = 0f;

        @Override
        public void buildConfiguration(Table table){
            table.field(String.valueOf(config()), (textField, c) -> Character.isDigit(c) || c == '.', input -> configure(Float.parseFloat("0" + input)));
        }

        @Override
        public Float config(){
            return power;
        }

        @Override
        public void configure(Object value){
            power = (Float) value;
            beam.setPower(power);
        }

        @Override
        public void created(){
            Util.blockRotationOffset(temp, x, y, Vars.tilesize * size * 0.5f, rotation);
            beam = new PowerBeam(temp.getX(), temp.getY(), (4 - rotation()) * 90f);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            float rot = (4 - rotation()) * 90f;
            if(rot != beam.getRotation()){
                Util.blockRotationOffset(temp, x, y, Vars.tilesize * size * .5f, rotation);
                beam.set(temp.getX(), temp.getY(), rot);
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            beam.remove();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(power);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            configure(power);
        }
    }
}
