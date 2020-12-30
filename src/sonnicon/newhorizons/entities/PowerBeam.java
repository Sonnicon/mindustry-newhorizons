package sonnicon.newhorizons.entities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public class PowerBeam{
    public float x, y, rotation, power = 0f;

    protected PowerBeam childBeam;
    protected static ArrayList<PowerBeam> beams = new ArrayList<>();
    // caching
    protected float length, endX, endY;
    protected HashSet<Tile> enroute = new HashSet<>();

    public static void init(){
        Events.on(EventType.TileChangeEvent.class, tile -> recalculateAll(tile.tile));
        Events.run(EventType.Trigger.update, () -> beams.forEach(PowerBeam::update));
        Events.run(EventType.Trigger.draw, () -> beams.forEach(PowerBeam::draw));
    }

    public PowerBeam(float x, float y, float rotation){
        this.x = x;
        this.y = y;
        this.rotation = rotation % 360f;
        beams.add(this);
        recalculate();
    }

    public static void recalculateAll(){
        recalculateAll(null);
    }

    public static void recalculateAll(Tile tile){
        beams.forEach(beam -> {
            if(tile == null || beam.enroute.contains(tile)){
                beam.recalculate();
            }
        });
    }

    public void recalculate(){
        // this was a nightmare because I didn't know the trigonometric functions took radians
        float distanceX = 0f, distanceY = 0f;
        if((rotation <= 45f || rotation >= 315f) || (rotation >= 135f && rotation <= 225f)){
            distanceX = (rotation > 90f && rotation < 270f) ? -x : Vars.world.width() * Vars.tilesize - x;
        }
        if((rotation >= 225f && rotation <= 315f) || (rotation >= 45f && rotation <= 135f)){
            distanceY = (rotation < 180f) ? -y : Vars.world.height() * Vars.tilesize;
        }
        if(rotation + 45f % 90f == 0 && distanceX > distanceY){
            distanceX = 0f;
        }
        if(rotation % 90 != 0){
            final double tan = Math.tan(Math.toRadians(rotation));
            if(distanceX == 0f){
                distanceX = -distanceY / (float) tan;
            }else{
                distanceY = (float) tan * -distanceX;
            }
        }
        AtomicReference<Tile> last = new AtomicReference<>();
        enroute.clear();
        Vars.world.raycastEachWorld(x, y, x + distanceX, y + distanceY, (rx, ry) -> {
            Tile rt = Vars.world.tile(rx, ry);
            if(rt == null) return true;
            enroute.add(rt);
            last.set(rt);
            return rt.block().absorbLasers;
        });
        endX = last.get().worldx();
        endY = last.get().worldy();
        length = (endX - x) / (float) Math.cos(Math.toRadians(rotation));
    }

    public void update(){
        if(length < 0f){
            recalculate();
        }

        if(power < 0.001f){
            power = 0f;
            return;
        }

        // todo damage
    }

    public void draw(){
        if(length < 0f || !on()) return;
        //todo make shader work
        Draw.draw(Layer.end, () ->
                Drawf.laser(null, Core.atlas.find("blank"), Core.atlas.find("blank"), x, y, endX, endY, power)
        );
    }

    public boolean on(){
        return power > 0f;
    }

    public void remove(){
        if(childBeam != null){
            childBeam.remove();
        }
        beams.remove(this);
    }
}
