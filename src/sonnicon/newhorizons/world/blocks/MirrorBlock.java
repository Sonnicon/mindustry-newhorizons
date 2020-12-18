package sonnicon.newhorizons.world.blocks;

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

        config(Integer.class, (MirrorBlockBuilding building, Integer value) -> building.setting = value);
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
        protected int setting = 0;

        @Override
        public void draw(){
            super.draw();
            Draw.rect(top, tile.drawx(), tile.drawy(), -setting);
        }

        @Override
        public void configure(Object value){
            super.configure((int) value % 360);
        }

        @Override
        public Object config(){
            return setting;
        }

        @Override
        public void buildConfiguration(Table table){
            table.field(String.valueOf(setting), (textField, c) -> Character.isDigit(c), input -> configure(Integer.parseInt("0" + input)));
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
            float transX = (float) (relX * Math.cos(-setting) - relY * Math.sin(-setting)),
                    transY = (float) (relX * Math.sin(-setting) + relY * Math.cos(-setting));

            return mirrorHitbox.contains(transX, transY);
        }

        @Override
        public boolean collision(Bullet other){
            int bRotation = (180 - (int) other.rotation()) % 360;

            BulletType type = other.type();
            if(Types.lasers.contains(type) && distance(setting, bRotation) < 90){
                int bounceAngle = bRotation - 2 * setting;
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
        public void readAll(Reads read, byte revision){
            super.readAll(read, revision);

            setting = read.i();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(setting);
        }
    }

    protected static int distance(int alpha, int beta){
        int phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }
}
