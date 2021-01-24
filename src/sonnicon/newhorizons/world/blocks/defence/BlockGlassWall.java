package sonnicon.newhorizons.world.blocks.defence;

import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.Wall;
import sonnicon.newhorizons.content.Types;

public class BlockGlassWall extends Wall{
    public BlockGlassWall(String name){
        super(name);
        hasShadow = false;
        fillsTile = false;
    }

    public class BuildingGlassWall extends Building{
        @Override
        public boolean collide(Bullet other){
            return !Types.isLaser(other.type());
        }
    }
}
