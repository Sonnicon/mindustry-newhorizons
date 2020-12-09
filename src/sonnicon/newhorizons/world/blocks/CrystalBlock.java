package sonnicon.newhorizons.world.blocks;

import arc.math.Rand;
import mindustry.content.Items;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.Tile;
import sonnicon.newhorizons.content.Types;

import java.util.Random;

import static mindustry.type.ItemStack.with;

public class CrystalBlock extends Block{

    public CrystalBlock(String name){
        super(name);
        requirements(Category.crafting, with(Items.copper, 30, Items.lead, 25));
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
        size = 3;
    }



    protected final Random random = new Random();

    public class CrystalBlockBuild extends Building{
        public CrystalBlockBuild(){
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
            if(Types.lasers.contains(other.type())){
                for(int i = 0; i < 3; i++){
                    Bullet b = type.create(this, null, x(), y(), random.nextInt(360));
                    b.time(other.time() * 0.6f);
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
    }
}
