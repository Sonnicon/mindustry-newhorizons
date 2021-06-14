package sonnicon.newhorizons.world.blocks.beam;

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
import mindustry.world.meta.StatValues;
import sonnicon.newhorizons.content.Blocks;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.IPowerBeamCatch;
import sonnicon.newhorizons.types.Pair;
import sonnicon.newhorizons.world.BuildingMultiblock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class BlockLaserCondenser extends Block{
    public float maxLensHealth = 200f;
    protected final float bound3 = maxLensHealth * 0.3f, bound2 = maxLensHealth * 0.6f;
    protected static final Boolf<Liquid> coolantFilter = liquid -> liquid.temperature <= 0.5f;

    public BlockLaserCondenser(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
        hasLiquids = true;
        sync = true;
        //todo balance
        liquidCapacity = 20f;
        consumes.add(new ConsumeLiquidFilter(coolantFilter, 0f)).update(false);
    }

    @Override
    public void setBars(){
        bars.add("lens-health", (BuildingLaserCondenser building) -> new Bar("stat.lens-health", Pal.health, () -> building.lensHealth / maxLensHealth).blink(Color.white));
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
        stats.add(Stat.input, StatValues.liquids(coolantFilter, Float.NaN, false));
        stats.add(Stat.liquidCapacity, liquidCapacity, StatUnit.liquidUnits);
    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;
    protected final Pair<Float, Float> temp = new Pair<>();
    protected final LinkedList<LinkedList<Pair<Float, Float>>> energiesPool = new LinkedList<>();

    protected static final int meanSize = 40;
    protected static final Random random = new Random();

    public class BuildingLaserCondenser extends BuildingMultiblock implements IPowerBeamCatch{
        protected float energy = 0f, lensHealth = maxLensHealth;
        protected LinkedList<Pair<Float, Float>> energies;
        public PowerBeam[] beams = new PowerBeam[3];
        protected int beamCount = 1;

        protected ArrayList<PowerBeam> catchedPowerBeams = new ArrayList<>();

        @Override
        public void updateTile(){
            energies.addLast(energies.pop().set(Time.delta, energy * Time.delta));
            float avg = (float) (energies.stream().mapToDouble(Pair::getY).sum() / energies.stream().mapToDouble(Pair::getX).sum());
            float catchedPower = (float) catchedPowerBeams.stream().mapToDouble(PowerBeam::getPower).sum();
            float power = (avg + catchedPower) / beamCount;
            //todo balancing


            for(int i = 0; i < beams.length; i++){
                beams[i].setPower(beamCount > i ? power : 0f);
            }
            energy = 0;
        }

        @Override
        public boolean collision(Bullet other){
            if(efficiency() > 0f && Util.distance(other.rotation(), rotation() * 90f) < 90f &&
                    isOutsideDirection(other.x() - other.deltaX(), other.y() - other.deltaY())){
                if(Types.isLaser(other.type())){
                    //todo balancing
                    float bulletEnergy = (other.lifetime() - other.time()) * other.type().damage * 0.005f;
                    float liquidRequired = bulletEnergy * liquids.current().temperature;
                    if(hasEnoughCoolant(liquidRequired)){
                        bulletEnergy -= bulletEnergy * liquidCapacity / (liquids().total() * 2f);
                        liquids.remove(liquids.current(), liquidRequired);
                    }else{
                        //todo balance
                        damageLens(other.damage());
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

        protected boolean hasEnoughCoolant(float amount){
            return liquids.currentAmount() > amount;
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
            return new PowerBeam(temp.getX(), temp.getY(), rot);
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

        public void damageLens(float amount, boolean withEffect){
            float pre = hitTime;
            damageLens(amount);
            if(!withEffect){
                hitTime = pre;
            }
        }

        public void damageContinuousLens(float amount){
            damageLens(amount * Time.delta, hitTime <= -10 + hitDuration);
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
        public boolean shouldCatch(PowerBeam beam){
            // aaa
            Util.blockRotationOffset(temp, x, y, Vars.tilesize * 1.5f, rotation() - 2);
            float adj, opp = Math.abs(temp.getY() - beam.getY());
            // Don't check if angle is always bad
            if(rotation() % 2 == 0){
                if(rotation == 0){
                    if(beam.getRotation() >= 90f && beam.getRotation() <= 270f){
                        return false;
                    }
                }else if(beam.getRotation() <= 90f || beam.getRotation() >= 270f){
                    return false;
                }
                adj = Math.abs(temp.getX() - beam.getX());
            }else{
                if(rotation == 1){
                    if(beam.getRotation() <= 180f){
                        return false;
                    }
                }else if(beam.getRotation() >= 180f){
                    return false;
                }
                adj = opp;
                opp = Math.abs(temp.getX() - beam.getX());
            }
            float calcopp;
            // tan(90) fix
            if((beam.getRotation() + 90f) % 180f == 0){
                calcopp = 0f;
            }else{
                calcopp = (float) Math.abs(Math.tan(Math.toRadians(beam.getRotation())) * adj);
            }
            if(calcopp >= opp - 12.01f && calcopp <= opp + 12.01f){
                return rotation != 1;
            }else{
                return rotation == 1;
            }
        }

        @Override
        public void addPowerBeam(PowerBeam beam){
            catchedPowerBeams.add(beam);
        }

        @Override
        public boolean removePowerBeam(PowerBeam beam){
            return catchedPowerBeams.remove(beam);
        }

        @Override
        public ArrayList<PowerBeam> getPowerBeams(){
            return catchedPowerBeams;
        }

        @Override
        public boolean shouldDamage(PowerBeam beam){
            return true;
        }

        @Override
        public void damage(PowerBeam beam){
            if(beam.getCatching() == this){
                float coolantNeeded = beam.getPower() * Time.delta * 0.1f;
                if(hasEnoughCoolant(coolantNeeded)){
                    liquids.remove(liquids.current(), coolantNeeded);
                }else{
                    //todo balance
                    damageLens(beam.getPower());
                }
            }else{
                beam.damage(this);
            }
        }
    }
}
