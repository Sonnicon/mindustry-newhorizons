package sonnicon.newhorizons.world.blocks;

import mindustry.gen.Bullet;
import sonnicon.newhorizons.content.Types;

public class SemiMirrorBlock extends MirrorBlock{

    public SemiMirrorBlock(String name){
        super(name);
    }

    public class SemiMirrorBlockBuilding extends MirrorBlockBuilding{
        @Override
        public boolean collision(Bullet other){
            int angle = (180 - (int) other.deltaAngle()) % 360;
            if(!Types.lasers.contains(other.type())){
                Bullet b = other.type().create(this, null, other.x(), other.y(), angle - 2 * setting);
                float time = other.lifetime - (other.lifetime() - other.time()) / 2;
                b.time(time);
                other.time(time);
                other.owner(b.owner());
                other.team(b.team());
                return false;
            }
            return super.collision(other);
        }
    }
}
