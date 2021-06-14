package sonnicon.newhorizons.world.blocks.defence;

import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.blocks.defense.Wall;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.IPowerBeamDamage;

public class BlockGlassWall extends Wall{
    public BlockGlassWall(String name){
        super(name);
        hasShadow = false;
        fillsTile = false;
    }

    public class BuildingGlassWall extends WallBuild implements IPowerBeamDamage{
        @Override
        public boolean collide(Bullet other){
            return !Types.isLaser(other.type());
        }

        @Override
        public boolean shouldDamage(PowerBeam beam){
            return false;
        }

        @Override
        public void damage(PowerBeam beam){

        }
    }
}
