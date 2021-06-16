package sonnicon.newhorizons.entities;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import sonnicon.newhorizons.graphics.Shaders;
import sonnicon.newhorizons.types.IPowerBeamCatch;
import sonnicon.newhorizons.types.IPowerBeamDamage;
import sonnicon.newhorizons.types.IPowerBeamPoly;
import sonnicon.newhorizons.world.blocks.beam.BlockLaserCondenser;
import sonnicon.newhorizons.world.blocks.beam.BlockMirror;

import java.util.*;
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
    // End of beam
    protected float length, endX, endY;
    // Origin and ending tiles (if applicable)
    protected Tile startTile, endTile;
    // Tiles on beam path (reduce recalculations)
    protected HashSet<Tile> enroute = new HashSet<>();
    // Stop damaging after recalculation due to damage
    protected boolean endOfDamage = false;
    // Object currently catching the beam
    protected IPowerBeamCatch catchPowerBeam;
    // Drawing coordinates
    protected float x1, y1, x2, y2, x3, y3, x4, y4;

    // Register events
    public static void initialize(){
        Events.on(EventType.TileChangeEvent.class, event -> event.tile.getLinkedTiles(PowerBeam::recalculateAll));

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
        if(parentBeam != null){
            this.parentBeam = parentBeam;
            setPower(parentBeam.power);
        }
        set(x, y, rotation);
    }

    public void set(float x, float y, float rotation){
        this.rotation = (rotation % 360f + 360f) % 360f;
        set(x, y);
    }

    public void set(float x, float y){
        this.x = x;
        this.y = y;
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
            endOfDamage = true;
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
        startTile = Vars.world.tileWorld(x, y);
        Vars.world.raycastEachWorld(x, y, x + distanceX, y + distanceY, (rx, ry) -> {
            Tile rt = Vars.world.tile(rx, ry);
            if(rt == null) return true;
            if(rt == startTile || (startTile.block().hasBuilding() && rt.build == startTile.build)) return false;
            enroute.add(rt);
            last.set(rt);
            endX = rt.worldx();
            endY = rt.worldy();

            if((rt.block() instanceof BlockMirror && ((BlockMirror.BuildingMirror) rt.build).shouldReflectAngle(rotation + 180f)) || rt.block().absorbLasers){
                return true;
            }else if(rt.build instanceof IPowerBeamCatch && ((IPowerBeamCatch) rt.build).shouldCatch(this)){
                ((IPowerBeamCatch) rt.build).addPowerBeam(this);
                catchPowerBeam = (IPowerBeamCatch) rt.build;
                return true;
            }
            return false;
        });

        // Both lengths to last tile
        if(endX == x){
            length = (endY - y) / (float) Math.sin(Math.toRadians(rotation));
        }else{
            length = (endX - x) / (float) Math.cos(Math.toRadians(rotation));
        }
        length = Math.abs(length);

        // Deal with child beam
        endTile = last.get();
        // todo move this to mirror
        if(endTile.block() instanceof BlockMirror && ((BlockMirror.BuildingMirror) endTile.build).shouldReflectAngle(rotation + 180f) && (!hasParent() || !parentBeam.isParentLoop(this))){
            float rot = (0f + (float) endTile.build.config()) * 2f - rotation + 180f;
            if(!hasChild()){
                childBeam = new PowerBeam(endX, endY, rot, this);
            }else{
                childBeam.set(endX, endY, rot, this);
            }
        }else if(hasChild()){
            childBeam.remove();
            childBeam = null;
        }

        calculateDraw();
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

        endOfDamage = false;
        Building originBuilding = Vars.world.tileWorld(x, y).block().hasBuilding() ? Vars.world.tileWorld(x, y).build : null;
        Iterator<Tile> tiles = enroute.iterator();
        while(tiles.hasNext() && !endOfDamage){
            Building build = tiles.next().build;
            if(build != null && build != originBuilding){
                if(build instanceof IPowerBeamDamage){
                    if(((IPowerBeamDamage) build).shouldDamage(this)){
                        ((IPowerBeamDamage) build).damage(this);
                    }
                }else{
                    damage(build);
                }
            }
        }
    }

    public void damage(Building building){
        //todo balance
        building.damageContinuous(power);
    }

    // Draw beam and child
    public void draw(){
        if(!hasParent() && (length <= 0f || !isOn())) return;
        //todo make shader work
        Draw.draw(Layer.effect, () -> {
            Shaders.powerbeam.set(this);
            Draw.shader(Shaders.powerbeam);
            // poly machine broke
            // Fill.poly(x1, y1, x2, y2, x3, y3, x4, y4);
            Fill.tri(x1, y1, x2, y2, x3, y3);
            Fill.tri(x2, y2, x3, y3, x4, y4);
            Draw.shader();
        });
        if(hasChild()){
            childBeam.draw();
        }
    }

    protected void calculateDraw(){
        // Drawing stuffs
        float radius = 12f * getPower();
        setPolyBeam(startTile != null ? startTile.build : null, false, radius);
        setPolyBeam(endTile != null ? endTile.build : null, true, radius);
    }

    protected void setPolyBeam(Building building, boolean end, float radius){
        boolean usePoly = building instanceof IPowerBeamPoly;

        float radians = (float) Math.toRadians(usePoly ? ((IPowerBeamPoly) building).getInterceptRotation(this, end) : getRotation());
        float sin = (float) (radius * Math.sin(radians));
        float cos = (float) (radius * Math.cos(radians));

        float tempx = usePoly ? ((IPowerBeamPoly) building).getInterceptX(this, end) : (end ? getEndX() : getX());
        float tempy = usePoly ? ((IPowerBeamPoly) building).getInterceptY(this, end) : (end ? getEndY() : getY());

        if(end){
            x1 = tempx + sin;
            y1 = tempy + cos;
            x2 = tempx - sin;
            y2 = tempy - cos;
        }else{
            x3 = tempx + sin;
            y3 = tempy + cos;
            x4 = tempx - sin;
            y4 = tempy - cos;
        }
    }

    // Force recalculation on next update
    public void invalidate(){
        this.length = 0f;
    }

    // Set power of beam and child beam
    public void setPower(float power){
        if(Math.abs(power - getPower()) > 0.01f){
            this.power = power;
            if(hasChild()){
                childBeam.setPower(power);
            }
        }

        calculateDraw();
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

    protected boolean isParentLoop(PowerBeam beam){
        return (Math.abs(beam.getEndX() - getX()) < 0.1f && Math.abs(beam.getEndY() - getY()) < 0.1f && Math.abs(beam.getRotation() - getRotation()) < 0.1f) ||
                (hasParent() && parentBeam.isParentLoop(beam));
    }
}
