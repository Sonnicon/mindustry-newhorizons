package sonnicon.newhorizons.world.blocks.beam;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Vars;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.IPowerBeamDamage;

import static sonnicon.newhorizons.core.Util.distance;

public class BlockMirror extends Block{
    protected TextureRegion top;
    protected Rect mirrorHitbox;

    protected static Rect tmp = new Rect();
    protected static Vec2 tmp2 = new Vec2();


    public BlockMirror(String name){
        super(name);
        configurable = true;
        destructible = true;
        solid = true;
        size = 2;

        config(Float.class, (BuildingMirror building, Float value) -> building.setting = value);
    }

    @Override
    public void load(){
        super.load();
        region = Core.atlas.find(Vars.mod.name + "-mirror");
        top = Core.atlas.find(name + "-top");

        mirrorHitbox = mirrorHitbox();
    }

    protected Rect mirrorHitbox(){
        return new Rect().setCentered(0, 0, mindustry.Vars.tilesize / 4f * size, mindustry.Vars.tilesize * size + 2f);
    }

    public class BuildingMirror extends Building implements IPowerBeamDamage{
        protected float setting = 0;

        @Override
        public void draw(){
            super.draw();
            Draw.rect(top, tile.drawx(), tile.drawy(), -config());
        }

        @Override
        public void configure(Object value){
            setting = (float) value % 360f;
            super.configure(setting);
            tile.getLinkedTiles(PowerBeam::recalculateAll);
        }

        @Override
        public Float config(){
            return setting;
        }

        @Override
        public void buildConfiguration(Table table){
            table.field(String.valueOf(config()), (textField, c) -> Character.isDigit(c) || c == '.', input -> configure(Float.parseFloat("0" + input)));
        }

        @Override
        public boolean collide(Bullet other){
            if(other.owner() == this){
                return false;
            }

            tmp = tile.getHitbox(Rect.tmp);
            tmp2 = tmp.getCenter(tmp2);
            float relX = other.getX() - tmp2.x,
                    relY = other.getY() - tmp2.y;
            float transX = (float) (relX * Math.cos(-config()) - relY * Math.sin(-config())),
                    transY = (float) (relX * Math.sin(-config()) + relY * Math.cos(-config()));

            return mirrorHitbox.contains(transX, transY);
        }

        @Override
        public boolean collision(Bullet other){
            float bRotation = (180f - other.rotation()) % 360f;
            BulletType type = other.type();
            if(Types.isLaser(type) && shouldReflectAngle(bRotation)){
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
            }
            return super.collision(other);
        }

        public boolean shouldReflectAngle(float rotation){
            return distance(config(), rotation) < 90f;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(config());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            setting = read.f();
        }

        @Override
        public boolean shouldDamage(PowerBeam beam){
            return !shouldReflectAngle(beam.getRotation() - 180f);
        }

        @Override
        public void damage(PowerBeam beam){
            beam.damage(this);
        }
    }
}
