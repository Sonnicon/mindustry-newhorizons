package sonnicon.newhorizons.world.blocks.crystal;

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

import static sonnicon.newhorizons.core.Util.distance;

public class MirrorBlock extends Block{
    protected TextureRegion top;
    protected Rect mirrorHitbox;

    protected static Rect tmp = new Rect();
    protected static Vec2 tmp2 = new Vec2();


    public MirrorBlock(String name){
        super(name);
        configurable = true;
        destructible = true;
        solid = true;

        config(Float.class, (MirrorBlockBuilding building, Float value) -> building.setting = value);
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

    public class MirrorBlockBuilding extends Building{
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
        }

        @Override
        public Float config(){
            return setting;
        }

        @Override
        public void buildConfiguration(Table table){
            table.field(String.valueOf(config()), (textField, c) -> Character.isDigit(c), input -> configure(Float.parseFloat("0" + input)));
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
            if(Types.lasers.contains(type) && distance(config(), bRotation) < 90f){
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
    }
}
