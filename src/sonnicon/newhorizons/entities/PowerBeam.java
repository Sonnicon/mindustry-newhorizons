package sonnicon.newhorizons.entities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.entities.Fires;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.world.blocks.crystal.MirrorBlock;
import sonnicon.newhorizons.world.blocks.crystal.SemiMirrorBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class PowerBeam{
    public float x, y, rotation, power = 0f;

    protected PowerBeam childBeam;
    protected boolean parent = true;

    protected static ArrayList<PowerBeam> beamsParents = new ArrayList<>();
    protected static ArrayList<PowerBeam> beams = new ArrayList<>();
    // caching
    protected float length, endX, endY;
    protected HashSet<Tile> enroute = new HashSet<>();

    public static void init(){
        Events.on(EventType.TileChangeEvent.class, tile -> recalculateAll(tile.tile));
        // they would stick around after loading another save
        Events.on(EventType.ResetEvent.class, event -> {
            while(!beams.isEmpty()){
                beams.get(0).remove();
            }
        });
        Events.run(EventType.Trigger.update, () -> {
            for(int i = beamsParents.size() - 1; i >= 0; i--){
                beamsParents.get(i).update();
            }
        });
        Events.run(EventType.Trigger.draw, () -> beams.forEach(PowerBeam::draw));
    }

    public PowerBeam(float x, float y, float rotation, boolean parent){
        set(x, y, rotation, parent);
        beams.add(this);
        if(parent){
            beamsParents.add(this);
        }
    }

    public void set(float x, float y, float rotation, boolean parent){
        this.x = x;
        this.y = y;
        this.rotation = rotation % 360f;
        this.parent = parent;
        length = 0;
    }

    public void setPower(float power){
        this.power = power;
        if(childBeam != null){
            childBeam.setPower(power);
        }
    }

    public static void recalculateAll(){
        recalculateAll(null);
    }

    public static void recalculateAll(Tile tile){
        beams.forEach(beam -> {
            if(tile == null || (beam.enroute.contains(tile))){
                beam.invalidate();
            }
        });
    }

    public void recalculate(){
        // this was a nightmare because I didn't know the trigonometric functions took radians

        // distance to axis
        float distanceX = 0f, distanceY = 0f;
        if((rotation <= 45f || rotation >= 315f) || (rotation >= 135f && rotation <= 225f)){
            distanceX = (rotation > 90f && rotation < 270f) ? -x : Vars.world.width() * Vars.tilesize - x;
        }
        if((rotation >= 225f && rotation <= 315f) || (rotation >= 45f && rotation <= 135f)){
            distanceY = (rotation < 180f) ? -y : Vars.world.height() * Vars.tilesize;
        }
        // for later recalculation, otherwise would be in corner
        if(rotation + 45f % 90f == 0 && distanceX > distanceY){
            distanceX = 0f;
        }

        // 90 degree angles
        if(rotation % 90 != 0){
            final double tan = Math.tan(Math.toRadians(rotation));
            if(distanceX == 0f){
                distanceX = -distanceY / (float) tan;
            }else{
                distanceY = (float) tan * -distanceX;
            }
        }

        // collect and check every tile between origin and end position
        AtomicReference<Tile> last = new AtomicReference<>();
        enroute.clear();
        Tile origin = Vars.world.tileWorld(x, y);
        Vars.world.raycastEachWorld(x, y, x + distanceX, y + distanceY, (rx, ry) -> {
            Tile rt = Vars.world.tile(rx, ry);
            if(rt == null) return true;
            if(rt == origin) return false;
            // add all of multiblocks
            if(rt.block().hasBuilding()){
                rt.build.tile().getLinkedTiles(t -> enroute.add(t));
            }else{
                enroute.add(rt);
            }
            last.set(rt);
            return (!origin.block().hasBuilding() || rt.build != origin.build) && (rt.block().absorbLasers || canReflectMirror(rt));
        });

        // both lengths to last tile
        endX = last.get().worldx();
        endY = last.get().worldy();
        if(endX == x){
            length = (endY - y) / (float) Math.sin(Math.toRadians(rotation));
        }else{
            length = (endX - x) / (float) Math.cos(Math.toRadians(rotation));
        }
        length = Math.abs(length);

        // create or update child beams
        Tile t = last.get();
        if(canReflectMirror(t)){
            if(childBeam == null){
                childBeam = new PowerBeam(endX, endY, (0f + (float) t.build.config()) * 2f - rotation + 180f, false);
            }else{
                childBeam.set(endX, endY, (0f + (float) t.build.config()) * 2f - rotation + 180f, false);
            }
        }else if(childBeam != null){
            childBeam.remove();
            childBeam = null;
        }
    }

    protected boolean canReflectMirror(Tile tile){
        return tile.block() instanceof SemiMirrorBlock ||
                (tile.block() instanceof MirrorBlock && Util.distance(rotation - 180f, (Float) tile.build.config()) < 90f);
    }

    public void invalidate(){
        this.length = 0f;
    }

    public void update(){
        if(length <= 0f){
            recalculate();
        }

        if(childBeam != null){
            childBeam.update();
        }

        if(power < 0.001f){
            power = 0f;
            return;
        }

        Building originBuilding = Vars.world.tileWorld(x, y).block().hasBuilding() ? Vars.world.tileWorld(x, y).build : null;
        enroute.forEach(tile -> {
            if(tile.block().hasBuilding() && tile.build != originBuilding && !(tile.block() instanceof MirrorBlock && canReflectMirror(tile))){
                //todo balance
                tile.build.damageContinuous(power);
            }
        });
    }

    public void draw(){
        if(length <= 0f || !on()) return;
        //todo make shader work
        Draw.draw(Layer.effect, () ->
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
        if(parent){
            beamsParents.remove(this);
        }
    }
}
