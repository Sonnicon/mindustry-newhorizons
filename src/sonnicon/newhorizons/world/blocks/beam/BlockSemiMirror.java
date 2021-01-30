package sonnicon.newhorizons.world.blocks.beam;

import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.entities.PowerBeam;

public class BlockSemiMirror extends BlockMirror{

    public BlockSemiMirror(String name){
        super(name);
    }

    public class BuildingSemiMirror extends BuildingMirror{
        @Override
        public boolean collision(Bullet other){
            float bRotation = (180f - other.rotation()) % 360f;
            BulletType type = other.type();
            if(Types.isLaser(type)){
                if(shouldReflectAngle(bRotation)){
                    float bounceAngle = bRotation - 2f * config();
                    if(type.collidesTeam){
                        Bullet b = type.create(this, null, other.x(), other.y(), bounceAngle);
                        b.time(other.time());
                        return true;
                    }else{
                        // Don't create new bullets if not required
                        other.owner(this);
                        other.team(null);
                        other.rotation(bounceAngle);
                        return false;
                    }
                }else if(type.collidesTeam){
                    Bullet b = type.create(this, null, other.x(), other.y(), other.rotation());
                    b.time(other.time());
                    return true;
                }
            }
            return super.collision(other);
        }

        @Override
        public boolean shouldDamage(PowerBeam beam){
            return false;
        }
    }
}
