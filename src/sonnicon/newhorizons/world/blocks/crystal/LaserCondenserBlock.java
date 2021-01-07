package sonnicon.newhorizons.world.blocks.crystal;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.values.ItemListValue;
import mindustry.world.meta.values.LiquidFilterValue;
import sonnicon.newhorizons.content.Blocks;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.ICatchPowerBeam;
import sonnicon.newhorizons.types.Pair;
import sonnicon.newhorizons.world.MultiblockBuilding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class LaserCondenserBlock extends Block{
    public float maxLensHealth = 200f;
    protected final float bound3 = maxLensHealth * 0.3f, bound2 = maxLensHealth * 0.6f;
    protected static final Boolf<Liquid> coolantFilter = liquid -> liquid.temperature <= 0.5f;

    public LaserCondenserBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
        hasLiquids = true;
        sync = true;
        absorbLasers = true;
        //todo balance
        liquidCapacity = 20f;
        consumes.add(new ConsumeLiquidFilter(coolantFilter, 0f)).update(false);
    }

    @Override
    public void setBars(){
        bars.add("lens-health", (LaserCondenserBlockBuilding building) -> new Bar("stat.lens-health", Pal.health, () -> building.lensHealth / maxLensHealth).blink(Color.white));
        super.setBars();
    }

    @Override
    public void setStats(){
        stats.add(Stat.size, Core.bundle.format("stat.value.irregular"));
        stats.add(Stat.health, health, StatUnit.none);
        stats.add(Stat.health, maxLensHealth, StatUnit.none);

        if(canBeBuilt()){
            String s = Core.bundle.format("stat.value.see-block", Blocks.multiblockAssemblyBlock.localizedName);
            stats.add(Stat.buildTime, s);
            stats.add(Stat.buildCost, s);
        }
        stats.add(Stat.input, new LiquidFilterValue(coolantFilter, Float.NaN, false));
        stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;
    protected final Pair<Float, Float> temp = new Pair<>();
    protected final LinkedList<LinkedList<Pair<Float, Float>>> energiesPool = new LinkedList<>();

    protected static final int meanSize = 40;
    protected static final Random random = new Random();

    public class LaserCondenserBlockBuilding extends MultiblockBuilding implements ICatchPowerBeam{
        protected float energy = 0f, lensHealth = maxLensHealth;
        protected LinkedList<Pair<Float, Float>> energies;
        protected PowerBeam[] beams = new PowerBeam[3];
        protected int beamCount = 1;

        protected ArrayList<PowerBeam> catchedPowerBeams = new ArrayList<>();

        @Override
        public void updateTile(){
            energies.addLast(energies.pop().set(Time.delta, energy * Time.delta));
            float avg = ((float) (energies.stream().mapToDouble(Pair::getY).sum() / energies.stream().mapToDouble(Pair::getX).sum()));
            float power = avg + (float) catchedPowerBeams.stream().mapToDouble(beam -> beam.power).sum() / beamCount;
            for(int i = 0; i < beams.length; i++){
                beams[i].setPower(beamCount > i ? power : 0f);
            }
            energy = 0;
        }

        @Override
        public boolean collision(Bullet other){
            if(efficiency() > 0f && Util.distance(other.rotation(), rotation() * 90f) < 90f &&
                    isOutsideDirection(other.x() - other.deltaX(), other.y() - other.deltaY())){
                if(Types.lasers.contains(other.type())){
                    //todo balancing
                    float bulletEnergy = (other.lifetime() - other.time()) * other.type().damage * 0.005f;
                    float liquidRequired = bulletEnergy * liquids.current().temperature;
                    if(liquids.currentAmount() > liquidRequired){
                        bulletEnergy -= bulletEnergy * liquidCapacity / (liquids().total() * 2f);
                        liquids.remove(liquids.current(), liquidRequired);
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
            // Split healing proportionally with damage
            float d1 = maxHealth - health,
                    d2 = maxLensHealth - lensHealth,
                    mul = amount / (d1 + d2);
            super.heal(Util.ceil(d1 * mul));
            healLens(Util.ceil(d2 * mul));
        }

        public void healLens(float amount){
            lensHealth = Math.min(lensHealth + amount, maxLensHealth);
            updateBeamCount();
        }

        public void damageLens(float amount){
            lensHealth -= amount;
            if(lensHealth <= 0f){
                kill();
            }
            updateBeamCount();
        }

        @Override
        public boolean damaged(){
            return super.damaged() || lensHealth < maxLensHealth;
        }

        protected int updateBeamCount(){
            if(lensHealth < bound3){
                beamCount = 3;
            }else if(lensHealth < bound2){
                beamCount = 2;
            }else{
                beamCount = 1;
            }
            return beamCount;
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
            energies = null;
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

        @Override
        public void addPowerBeam(PowerBeam beam){
            catchedPowerBeams.add(beam);
        }

        @Override
        public boolean removePowerBeam(PowerBeam beam){
            return catchedPowerBeams.remove(beam);
        }
    }
}
