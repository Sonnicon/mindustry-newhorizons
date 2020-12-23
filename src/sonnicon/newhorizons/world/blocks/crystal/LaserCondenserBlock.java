package sonnicon.newhorizons.world.blocks.crystal;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.world.MultiblockBuilding;

import java.util.ArrayList;

public class LaserCondenserBlock extends Block{
    public LaserCondenserBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        update = true;
    }

    @Override
    public void load(){
        super.load();

        Events.on(EventType.TileChangeEvent.class, tile -> {
            recalculateBeams();
        });
    }

    public void recalculateBeams(){
        buildings.forEach(b -> b.length = -1);
    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;
    protected final ArrayList<LaserCondenserBlockBuilding> buildings = new ArrayList<>();

    public class LaserCondenserBlockBuilding extends MultiblockBuilding{
        public float energy = 0f;
        public float time = 0f;

        //caching for draw()
        public float length = -1;

        @Override
        public void updateTile(){
            System.out.println(length);
            team(Team.derelict);

            time += Time.delta;
            if(time >= 8f){
                time /= 2f;
                energy /= 2f;
            }

            if(length < 0f){
                Tile t1 = Vars.world.tileWorld(x, y);
                Tile t2;
                while(true){
                    t2 = t1.nearby(rotation);
                    if(t2 == null || t2.block().absorbLasers){
                        length = t1.x * Vars.tilesize - x;
                        break;
                    }
                    t1 = t2;
                }
            }
        }

        @Override
        public void draw(){
            super.draw();

            if(length < 0f) return;
            Draw.z(Layer.power);
            Drawf.laser(null,
                    Core.atlas.find("blank"),
                    Core.atlas.find("blank"),
                    //todo optimise
                    x + (int) Math.cos(rotation) * distance,
                    y + (int) Math.sin(rotation) * distance,
                    x + (int) Math.cos(rotation) * length,
                    y + (int) Math.sin(rotation) * length,
                    1);
            Draw.reset();
        }

        @Override
        public boolean collision(Bullet other){
            if(Util.distance(other.rotation(), rotation() * 90f) < 90f &&
                    isOutsideDirection(other.x() - other.deltaX(), other.y() - other.deltaY())){
                if(Types.lasers.contains(other.type())){
                    //todo balancing
                    energy += (other.lifetime() - other.time()) * other.type().damage * 0.25;
                }else{
                    //todo
                }
                return true;
            }

            return super.collision(other);
        }

        protected boolean isOutsideDirection(float prevX, float prevY){
            switch(rotation()){
                case (0):{
                    return x - distance > prevX;
                }
                case (1):{
                    return y - distance > prevY;
                }
                case (2):{
                    return x + distance < prevX;
                }
                case (3):{
                    return y + distance < prevY;
                }
                default:{
                    throw new NumberFormatException();
                }
            }
        }

        @Override
        public void created(){
            super.created();
            buildings.add(this);
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            buildings.add(this);
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(energy);
            write.f(time);
        }

        @Override
        public void read(Reads read){
            super.read(read);

            energy = read.f();
            time = read.f();
        }
    }
}
