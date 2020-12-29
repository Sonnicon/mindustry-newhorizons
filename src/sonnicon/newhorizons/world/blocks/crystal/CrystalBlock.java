package sonnicon.newhorizons.world.blocks.crystal;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;

import java.util.Random;

public class CrystalBlock extends Block{

    public CrystalBlock(String name){
        super(name);
        solid = true;
        destructible = true;
        rotate = false;
        breakable = true;
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

    public class CrystalBlockBuilding extends Building{
        public CrystalBlockBuilding(){
            super();
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
            if(Types.lasers.contains(type)){
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
