package sonnicon.newhorizons.world.blocks.crystal;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.world.Block;
import sonnicon.newhorizons.content.Types;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.entities.PowerBeam;
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

    }

    // collision distance from center
    protected float distance = Vars.tilesize * 1.5f;

    public class LaserCondenserBlockBuilding extends MultiblockBuilding{
        public float energy = 0f;
        public float time = 0f;

        public PowerBeam beam;

        @Override
        public void updateTile(){
            team(Team.derelict);

            time += Time.delta;
            if(time >= 8f){
                time /= 2f;
                energy /= 2f;
            }
        }

        @Override
        public void draw(){
            super.draw();
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
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            if(beam != null){
                beam.remove();
            }
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
