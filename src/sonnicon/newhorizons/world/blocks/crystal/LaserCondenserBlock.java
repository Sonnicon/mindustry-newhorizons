package sonnicon.newhorizons.world.blocks.crystal;

import arc.graphics.Color;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.Pair;
import sonnicon.newhorizons.world.MultiblockBuilding;

import java.util.LinkedList;
import java.util.Random;

public class LaserCondenserBlock extends Block{
    public float maxLensHealth = 200f;

    public LaserCondenserBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
        hasLiquids = true;
        sync = true;
        //todo balance
        liquidCapacity = 20f;
        consumes.add(new ConsumeLiquidFilter(l -> l.temperature <= 0.5f, 0.01f)).update(false);
    }

    @Override
    public void setBars(){
        bars.add("lens-health", (LaserCondenserBlockBuilding building) -> new Bar("stat.lens-health", Pal.health, () -> building.lensHealth / maxLensHealth).blink(Color.white));
        super.setBars();
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.health, maxLensHealth, StatUnit.none);
    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;
    protected final Pair<Float, Float> temp = new Pair<>();
    protected final LinkedList<LinkedList<Pair<Float, Float>>> energiesPool = new LinkedList<>();

    protected static final int meanSize = 40;
    protected static final Random random = new Random();

    public class LaserCondenserBlockBuilding extends MultiblockBuilding{
        protected float energy = 0f, lensHealth = maxLensHealth;
        protected LinkedList<Pair<Float, Float>> energies;
        protected PowerBeam[] beams = new PowerBeam[3];

        @Override
        public void updateTile(){
            energies.addLast(energies.pop().set(Time.delta, energy * Time.delta));
            float avg = ((float) (energies.stream().mapToDouble(Pair::getY).sum() / energies.stream().mapToDouble(Pair::getX).sum()));
            int count = beamCount();
            for(int i = 0; i < count; i++){
                beams[i].setPower(avg / count);
            }
            for(int i = count; i < beams.length; i++){
                beams[i].setPower(0f);
            }
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
                    //todo balance
                    damageLens(other.damage());
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
        public void unbiasedCreated(){
            super.unbiasedCreated();

            Util.blockRotationOffset(temp, x, y, distance, rotation());
            beams[0] = createBeam((4 - rotation()) * 90f);
            random.setSeed(id());
            for(int i = 1; i < 3; i++){
                beams[i] = createBeam((3 - rotation()) * 90f + random.nextInt(180));
                System.out.println(beams[i].rotation);
            }

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

        PowerBeam createBeam(float rot){
            return new PowerBeam(temp.getX(), temp.getY(), rot, true);
        }

        @Override
        public void heal(){
            super.heal();
            lensHealth = maxLensHealth;
        }

        @Override
        public void heal(float amount){
            float d1 = maxHealth - health,
                    d2 = maxLensHealth - lensHealth,
                    mul = amount / (d1 + d2);
            super.heal((float) Math.ceil(d1 * mul));
            healLens((float) Math.ceil(d2 * mul));
        }

        public void healLens(float amount){
            lensHealth = Math.min(lensHealth + amount, maxLensHealth);
        }

        public void damageLens(float amount){
            lensHealth -= amount;
            if(lensHealth <= 0f){
                kill();
            }
        }

        protected int beamCount(){
            if(lensHealth < maxLensHealth * 0.3f) return 3;
            if(lensHealth < maxLensHealth * 0.6f) return 2;
            return 1;
        }

        @Override
        public void onRemoved(){
            super.onRemoved();

            for(PowerBeam beam : beams){
                if(beam != null){
                    beam.remove();
                }
            }

            energiesPool.add(energies);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            // save average instead of true values to save memory
            write.f((float) energies.stream().mapToDouble(Pair::getX).sum() / meanSize);
            write.f((float) energies.stream().mapToDouble(Pair::getY).sum() / meanSize);
            write.f(lensHealth);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            float x = read.f(), y = read.f();
            energies.forEach(pair -> pair.set(x, y));
            lensHealth = read.f();
        }
    }
}
