package sonnicon.newhorizons.world.blocks.defence;

import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.Wall;
import sonnicon.newhorizons.content.Types;

public class GlassWallBlock extends Wall{
    public GlassWallBlock(String name){
        super(name);

    }

    public class GlassWallBlockBuilding extends Building{
        @Override
        public boolean collide(Bullet other){
            return !Types.lasers.contains(other.type());
        }
    }
}
