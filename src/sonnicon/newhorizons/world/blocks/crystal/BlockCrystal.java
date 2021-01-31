package sonnicon.newhorizons.world.blocks.crystal;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Puddles;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Puddle;
import mindustry.type.Liquid;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.types.CrystalLiquid;

import java.util.HashMap;
import java.util.Random;

public class BlockCrystal extends Block{
    public float liquidCapacity = 100f;

    public BlockCrystal(String name){
        super(name);
        solid = true;
        destructible = true;
        rotate = false;
        breakable = true;
        update = true;
        rebuildable = false;
        enableDrawStatus = false;
        expanded = true;
        absorbLasers = true;
        fillsTile = false;
        targetable = false;
        canOverdrive = false;
        hasShadow = false;
        sync = true;
        size = 3;
    }


    protected final Random random = new Random();

    public class BuildingCrystal extends Building{
        protected float integrity = 100f;

        protected HashMap<Liquid, Float> liquids = new HashMap<>();
        protected float liquidTotal = 0f;


        public BuildingCrystal(){
            super();
        }

        @Override
        public void updateTile(){
            Puddle puddle = Puddles.get(tile);
            if(puddle != null){
                CrystalLiquid puddleLiquid = CrystalLiquid.get(puddle.liquid());

                //todo balance
                float absorb = Math.min(liquidCapacity - liquidTotal, puddle.amount * delta() * puddleLiquid.getAbsorbAmount() * puddleLiquid.getAbsorbSpeed() * 0.1f);
                liquidTotal += absorb;
                liquids.put(puddle.liquid(), liquids.getOrDefault(puddle.liquid(), 0f) + absorb);
                puddle.amount(puddle.amount - absorb / puddleLiquid.getAbsorbAmount());
            }

        }

        @Override
        public void team(Team team){
            this.team = Team.derelict;
        }

        @Override
        public boolean collide(Bullet other){
            return other.owner() != this;
        }

        @Override
        public boolean collision(Bullet other){
            BulletType type = other.type();
            if(Types.isLaser(type)){
                for(int i = 0; i < 3; i++){
                    Bullet b = type.create(this, null, x(), y(), random.nextInt(360));
                    b.time(other.time() * 0.9f);
                }
            }else{
                //todo
            }
            return true;
        }

        @Override
        public void damage(float damage){
            tile.setTeam(Team.derelict);
        }


        @Override
        public void write(Writes write){
            super.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
        }
    }
}
