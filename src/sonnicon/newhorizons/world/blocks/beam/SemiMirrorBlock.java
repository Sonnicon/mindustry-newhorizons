package sonnicon.newhorizons.world.blocks.beam;

import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import sonnicon.newhorizons.content.Types;

public class SemiMirrorBlock extends MirrorBlock{

    public SemiMirrorBlock(String name){
        super(name);
    }

    public class SemiMirrorBlockBuilding extends MirrorBlockBuilding{
        @Override
        public boolean collision(Bullet other){
            BulletType type = other.type();
            if(Types.isLaser(type)){
                float time = other.lifetime - (other.lifetime() - other.time()) / 2;
                int angle = (180 - (int) other.rotation()) % 360;

                if(angle % 180 != setting){
                    Bullet b = type.create(this, null, other.x(), other.y(), angle - 2 * setting);
                    b.time(time);
                }

                if(type.collidesTeam){
                    other = type.create(this, null, other.x(), other.y(), other.rotation());
                    other.time(time);
                    return true;
                }else{
                    // Don't create new bullets if not required
                    other.time(time);
                    other.owner(this);
                    other.team(null);
                    return false;
                }
            }
            return super.collision(other);
        }

        @Override
        public boolean shouldReflectAngle(float rotation){
            return true;
        }
    }
}
