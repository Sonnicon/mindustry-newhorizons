package sonnicon.newhorizons.world.blocks;

import mindustry.content.Items;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.type.Category;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;

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
        //todo
        absorbLasers = false;

        fillsTile = false;
        targetable = false;
        canOverdrive = false;
        hasShadow = false;
        size = 3;
    }



    public class CrystalBlockBuild extends Building{
        public CrystalBlockBuild(){
            super();
        }

        @Override
        public boolean collide(Bullet other){
            //todo
            return Types.lasers.contains(other.type());
        }

        @Override
        public void damage(float damage){

        }
    }
}
