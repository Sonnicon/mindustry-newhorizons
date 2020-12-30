package sonnicon.newhorizons.world.blocks.crystal;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidFilter;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.Pair;
import sonnicon.newhorizons.world.MultiblockBuilding;

import java.util.LinkedList;

public class LaserCondenserBlock extends Block{
    public LaserCondenserBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
        hasLiquids = true;
        //todo balance
        liquidCapacity = 20f;
        consumes.add(new ConsumeLiquidFilter(l -> l.temperature <= 0.5f, 0.04f)).update(false);
    }

    @Override
    public void load(){
        super.load();
    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;
    protected final Pair<Float, Float> temp = new Pair<>();
    protected final LinkedList<LinkedList<Pair<Float, Float>>> energiesPool = new LinkedList<>();

    protected static final int meanSize = 40;

    public class LaserCondenserBlockBuilding extends MultiblockBuilding{
        protected float energy = 0f;
        protected LinkedList<Pair<Float, Float>> energies;

        public PowerBeam beam;

        @Override
        public void updateTile(){
            //todo remove
            team(Team.derelict);

            energies.addLast(energies.pop().set(Time.delta, energy * Time.delta));
            float mean = (float) (energies.stream().mapToDouble(Pair::getY).sum() / energies.stream().mapToDouble(Pair::getX).sum());
            beam.power = mean;
            energy = 0;
        }

        @Override
        public void draw(){
            super.draw();
        }

        @Override
        public boolean collision(Bullet other){
            if(efficiency() > 0f && Util.distance(other.rotation(), rotation() * 90f) < 90f &&
                    isOutsideDirection(other.x() - other.deltaX(), other.y() - other.deltaY())){
                if(Types.lasers.contains(other.type())){
                    //todo balancing
                    float bulletEnergy = (other.lifetime() - other.time()) * other.type().damage * 0.005f;
                    // here instead of updateTile() to punish bullet spam, hence decreasing amount of bullets
                    if(consValid()){
                        consume();
                        bulletEnergy -= bulletEnergy * liquidCapacity / (liquids().total() * 2);
                    }else{
                        super.collision(other);
                    }
                    energy += bulletEnergy;
                }else{
                    //todo
                }
                return true;
            }
            return super.collision(other);
        }

        protected boolean isOutsideDirection(float prevX, float prevY){
            switch(rotation()){
                case (0):{
                    return x - distance > prevX;
                }
                case (1):{
                    return y - distance > prevY;
                }
                case (2):{
                    return x + distance < prevX;
                }
                case (3):{
                    return y + distance < prevY;
                }
                default:{
                    throw new NumberFormatException();
                }
            }
        }

        @Override
        public void created(){
            super.created();

            Util.blockRotationOffset(temp, x, y, distance, rotation());
            this.beam = new PowerBeam(temp.getX(), temp.getY(), rotation());

            if(energiesPool.isEmpty()){
                energies = new LinkedList<>();
                for(int i = 0; i < meanSize; i++){
                    energies.add(new Pair<>(0f, 0f));
                }
            }else{
                energies = energiesPool.pop();
                energies.forEach(pair -> pair.set(0f, 0f));
            }
        }

        @Override
        public void onRemoved(){
            super.onRemoved();

            if(beam != null){
                beam.remove();
            }

            energiesPool.add(energies);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            // save average instead of true values to save memory
            write.f((float) energies.stream().mapToDouble(Pair::getX).sum() / meanSize);
            write.f((float) energies.stream().mapToDouble(Pair::getY).sum() / meanSize);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            float x = read.f(), y = read.f();
            energies.forEach(pair -> pair.set(x, y));
        }
    }
}
