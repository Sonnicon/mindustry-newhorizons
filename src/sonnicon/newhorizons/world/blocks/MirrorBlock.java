package sonnicon.newhorizons.world.blocks;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.layout.Table;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;

public class MirrorBlock extends Block{
    public TextureRegion top;

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
        top = Core.atlas.find(name + "-top");
    }

    public class MirrorBlockBuilding extends Building{
        public int setting = 0;

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
            return other.owner() != this;
        }

        @Override
        public boolean collision(Bullet other){
            int angle = (180 - (int) other.deltaAngle()) % 360;
            if(Types.lasers.contains(other.type()) && distance(setting, angle) < 90){
                Bullet b = other.type().create(this, null, x(), y(), angle - 2 * setting);
                b.time(other.time());
                return true;
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

    private static int distance(int alpha, int beta) {
        int phi = Math.abs(beta - alpha) % 360;
        return phi > 180 ? 360 - phi : phi;
    }
}
