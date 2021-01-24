package sonnicon.newhorizons.world.blocks.sandbox;

import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.world.Block;
import sonnicon.newhorizons.entities.PowerBeam;

public class BlockPowerBeamSpawner extends Block{
    public BlockPowerBeamSpawner(String name){
        super(name);

        destructible = true;
        solid = true;
        configurable = true;
        rotate = true;

        config(Float.class, BuildingPowerBeamSpawner::configure);
    }

    public class BuildingPowerBeamSpawner extends Building{
        protected PowerBeam beam;

        @Override
        public void buildConfiguration(Table table){
            table.field(String.valueOf(config()), (textField, c) -> Character.isDigit(c) || c == '.', input -> configure(Float.parseFloat("0" + input)));
        }

        @Override
        public Float config(){
            return beam.getPower();
        }

        @Override
        public void configure(Object value){
            beam.setPower((Float) value);
        }

        @Override
        public void created(){
            beam = new PowerBeam(x, y, (4 - rotation()) * 90f);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            float rot = (4 - rotation()) * 90f;
            if(rot != beam.getRotation()){
                beam.setRotation(rot);
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
            write.f(beam.getPower());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            configure(read.f());
        }
    }
}
