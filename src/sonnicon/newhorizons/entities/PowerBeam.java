package sonnicon.newhorizons.entities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import sonnicon.newhorizons.types.IPowerBeamCatch;
import sonnicon.newhorizons.types.IPowerBeamDamage;
import sonnicon.newhorizons.world.blocks.beam.BlockLaserCondenser;
import sonnicon.newhorizons.world.blocks.beam.BlockMirror;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PowerBeam{
    // Position of start of beam
    protected float x, y, rotation;
    // Damage and width of beam
    protected float power = 0f;
    // Beams before and after redirection
    protected PowerBeam childBeam, parentBeam;

    // All loaded beams without parents
    protected static ArrayList<PowerBeam> beams = new ArrayList<>();
    // End tile
    protected float length, endX, endY;
    // Tiles on beam path (reduce recalculations)
    protected HashSet<Tile> enroute = new HashSet<>();
    // Object currently catching the beam
    protected IPowerBeamCatch catchPowerBeam;

    // Register events
    public static void init(){
        Events.on(EventType.TileChangeEvent.class, tile -> recalculateAll(tile.tile));

        Events.on(EventType.ResetEvent.class, event -> {
            while(!beams.isEmpty()){
                beams.get(0).remove();
            }
        });

        Events.run(EventType.Trigger.update, () -> {
            // Update from end because removals can happen
            for(int i = beams.size() - 1; i >= 0; i--){
                if(i >= beams.size()){
                    continue;
                }
                beams.get(i).update();
            }
        });

        Events.run(EventType.Trigger.draw, () -> beams.forEach(PowerBeam::draw));
    }

    public PowerBeam(float x, float y, float rotation){
        this(x, y, rotation, null);
    }

    public PowerBeam(float x, float y, float rotation, PowerBeam parentBeam){
        set(x, y, rotation, parentBeam);
        if(parentBeam == null){
            beams.add(this);
        }
    }

    // Move beam and recalculate
    public void set(float x, float y, float rotation, PowerBeam parentBeam){
        this.x = x;
        this.y = y;
        this.rotation = (rotation % 360f + 360f) % 360f;
        if(parentBeam != null){
            this.parentBeam = parentBeam;
            setPower(parentBeam.power);
        }
        invalidate();
    }

    // Recalculate every single PowerBeam
    public static void recalculateAll(){
        recalculateAll(null);
    }

    // Recalculate every PowerBeam that crosses through a tile
    public static void recalculateAll(Tile tile){
        beams.forEach(beam -> beam.recalculate(tile));
    }

    // Try to recalculate this PowerBeam if it crosses through a tile
    public void recalculate(Tile tile){
        if(tile == null || enroute.contains(tile)){
            recalculate();
        }else if(hasChild()){
            childBeam.recalculate(tile);
        }
    }

    // Catastrophic raycast recalculation
    public void recalculate(){
        // This was a nightmare because I didn't know the trigonometric functions took radians

        // Distance to edge of map
        float distanceX = 0f, distanceY = 0f;
        if((rotation <= 45f || rotation >= 315f) || (rotation >= 135f && rotation <= 225f)){
            distanceX = (rotation > 90f && rotation < 270f) ? -x : Vars.world.width() * Vars.tilesize - x;
        }
        if((rotation >= 225f && rotation <= 315f) || (rotation >= 45f && rotation <= 135f)){
            distanceY = (rotation < 180f) ? -y : Vars.world.height() * Vars.tilesize;
        }
        // For later recalculation, otherwise would be in corner
        if(rotation + 45f % 90f == 0 && distanceX > distanceY && distanceY != 0){
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

        // Collect and check every tile between origin and end position
        AtomicReference<Tile> last = new AtomicReference<>();
        enroute.clear();
        removeCatched();
        Tile origin = Vars.world.tileWorld(x, y);
        Vars.world.raycastEachWorld(x, y, x + distanceX, y + distanceY, (rx, ry) -> {
            Tile rt = Vars.world.tile(rx, ry);
            if(rt == null) return true;
            if(rt == origin || (origin.block().hasBuilding() && rt.build == origin.build)) return false;
            // Add all of multiblocks
            if(rt.block().hasBuilding()){
                rt.build.tile().getLinkedTiles(t -> enroute.add(t));
            }else{
                enroute.add(rt);
            }
            last.set(rt);
            // ew
            return (rt.block() instanceof BlockMirror && ((BlockMirror.BuildingMirror) rt.build).shouldReflectAngle(rotation + 180f)) ||
                    (rt.build instanceof IPowerBeamCatch && ((IPowerBeamCatch) rt.build).shouldCatch(this))
                    || rt.block().absorbLasers;
        });

        // Catch
        Tile lastTile = last.get();
        endX = lastTile.worldx();
        endY = lastTile.worldy();
        if(shouldCatch(lastTile)){
            ((IPowerBeamCatch) lastTile.build).addPowerBeam(this);
            catchPowerBeam = (IPowerBeamCatch) lastTile.build;
        }

        // Both lengths to last tile
        if(endX == x){
            length = (endY - y) / (float) Math.sin(Math.toRadians(rotation));
        }else{
            length = (endX - x) / (float) Math.cos(Math.toRadians(rotation));
        }
        length = Math.abs(length);

        // Deal with child beam
        Tile t = last.get();
        if(t.block() instanceof BlockMirror && ((BlockMirror.BuildingMirror) t.build).shouldReflectAngle(rotation + 180f)){
            if(!hasChild()){
                childBeam = new PowerBeam(endX, endY, (0f + (float) t.build.config()) * 2f - rotation + 180f, this);
            }else{
                childBeam.set(endX, endY, (0f + (float) t.build.config()) * 2f - rotation + 180f, this);
            }
        }else if(hasChild()){
            childBeam.remove();
            childBeam = null;
        }
    }

    // Ensure no infinite beam loop
    protected boolean shouldCatch(Tile lastTile){
        if(lastTile.build instanceof IPowerBeamCatch &&
                ((IPowerBeamCatch) lastTile.build).shouldCatch(this)){
            if(lastTile.block() instanceof BlockLaserCondenser){
                Set<PowerBeam> beams = Arrays.stream(((BlockLaserCondenser.BuildingLaserCondenser) lastTile.build).beams)
                        .collect(Collectors.toSet());
                PowerBeam pb = this;
                while(pb != null){
                    if(beams.contains(pb)){
                        return false;
                    }
                    pb = pb.parentBeam;
                }
            }
            return true;
        }
        return false;
    }

    // Update beam and child
    public void update(){
        if(length <= 0f){
            recalculate();
        }

        if(hasChild()){
            childBeam.update();
        }

        if(power < 0.001f){
            power = 0f;
            return;
        }

        Building originBuilding = Vars.world.tileWorld(x, y).block().hasBuilding() ? Vars.world.tileWorld(x, y).build : null;
        enroute.forEach(tile -> {
            Building build = tile.build;
            if(build != null && build != originBuilding){
                if(build instanceof IPowerBeamDamage){
                    if(((IPowerBeamDamage) build).shouldDamage(this)){
                        ((IPowerBeamDamage) build).damage(this);
                    }
                }else{
                    damage(build);
                }
            }
        });
    }

    public void damage(Building building){
        //todo balance
        building.damageContinuous(power);
    }

    // Draw beam and child
    public void draw(){
        if(!hasParent() && (length <= 0f || !isOn())) return;
        //todo make shader work
        Draw.draw(Layer.effect, () ->
                Drawf.laser(null, Core.atlas.find("blank"), Core.atlas.find("blank"), x, y, endX, endY, power)
        );
        if(hasChild()){
            childBeam.draw();
        }
    }

    // Force recalculation on next update
    public void invalidate(){
        this.length = 0f;
    }

    // Set power of beam and child beam
    public void setPower(float power){
        this.power = power;
        if(hasChild()){
            childBeam.setPower(power);
        }
    }

    public float getPower(){
        return power;
    }

    public void setRotation(float rotation){
        this.rotation = (rotation % 360f + 360f) % 360f;
        invalidate();
    }

    public float getRotation(){
        return rotation;
    }

    public boolean isOn(){
        return power > 0f;
    }

    public boolean hasParent(){
        return parentBeam != null;
    }

    public boolean hasChild(){
        return childBeam != null;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getEndX(){
        return endX;
    }

    public float getEndY(){
        return endY;
    }

    public IPowerBeamCatch getCatching(){
        return catchPowerBeam;
    }

    // Remove beam and all accessors
    public void remove(){
        if(hasChild()){
            childBeam.remove();
        }
        removeCatched();
        if(!hasParent()){
            beams.remove(this);
        }
    }

    protected void removeCatched(){
        if(catchPowerBeam != null){
            catchPowerBeam.removePowerBeam(this);
            catchPowerBeam = null;
        }
    }
}
