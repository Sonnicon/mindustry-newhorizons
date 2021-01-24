package sonnicon.newhorizons.world.blocks.liquid;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.Puddles;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Stat;
import sonnicon.newhorizons.core.Util;
import sonnicon.newhorizons.types.Pair;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class BlockPhaseConduitDispenser extends Block{
    public float range = 12;
    public float speed = 0.5f;
    protected static final Pair<Integer, Integer> temp = new Pair<>();

    public BlockPhaseConduitDispenser(String name){
        super(name);

        destructible = true;
        solid = true;
        configurable = true;
        update = true;
        rotate = true;
        hasLiquids = true;
        outputsLiquid = false;
        hasPower = true;
        canOverdrive = false;
        liquidCapacity = speed * 250f;
        consumes.power(0.30f);
        group = BlockGroup.liquids;

        config(Integer.class, Building::configure);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.tiles, Blocks.phaseConduit.localizedName);
    }

    @Override
    public boolean canReplace(Block other){
        return other == Blocks.phaseConduit || Vars.state.rules.infiniteResources;
    }

    public class BuildingPhaseConduitDispenser extends Building{
        protected int setting = (int) (range / 2f);
        protected float uptime = 0f;
        protected Tile target;

        @Override
        public Integer config(){
            return setting;
        }

        @Override
        public void configure(Object value){
            setting = (int) value;
            target = null;
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.black5, t -> {
                t.slider(0, range, 1, config(), value -> configure((int) value));
                t.label(() -> config().toString()).padLeft(8f).minWidth(20f);
            }).margin(4f);
        }

        @Override
        public void updateTile(){
            if(target == null){
                Util.blockRotationOffset(temp, tileX(), tileY(), config(), rotation);
                target = world.tile(Math.max(0, Math.min(world.tiles.width, temp.getX())), Math.max(0, Math.min(world.tiles.height, temp.getY())));
                if(target == null) return;
            }

            if(consValid()){
                float alpha = 0.04f;
                if(hasPower){
                    alpha *= efficiency();
                }
                uptime = Mathf.lerpDelta(uptime, 1f, alpha);
            }else{
                uptime = Mathf.lerpDelta(uptime, 0f, 0.02f);
            }

            if(uptime >= 0.5f){
                float amount = Math.min(liquids.currentAmount(), speed * Time.delta);
                liquids.remove(liquids.current(), amount);
                Puddles.deposit(target, liquids.current(), amount);
            }

        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return team == source.team && (liquids.current() == liquid || liquids.currentAmount() < 0.001f);
        }

        @Override
        public void draw(){
            super.draw();
            if(target == null){
                return;
            }

            Draw.z(Layer.power);

            float opacity = Core.settings.getInt("bridgeopacity") / 100f;
            if(Mathf.zero(opacity)) return;

            int i = relativeTo(target.x, target.y);

            Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));
            Draw.alpha(Math.max(uptime, 0.25f) * opacity);

            Lines.stroke(8f);

            Tmp.v1.set(x, y).sub(target.worldx(), target.worldy()).setLength(tilesize / 2f).scl(-1f);

            Lines.line(((ItemBridge) Blocks.phaseConduit).bridgeRegion,
                    x + Tmp.v1.x,
                    y + Tmp.v1.y,
                    target.worldx() - Tmp.v1.x,
                    target.worldy() - Tmp.v1.y, false);

            int dist = Math.max(Math.abs(target.x - tile.x), Math.abs(target.y - tile.y));

            float time = Time.time / 1.7f;
            int arrows = (dist) * tilesize / 4 - 2;

            Draw.color();

            for(int a = 0; a < arrows; a++){
                Draw.alpha(Mathf.absin(a / (float) arrows - time / 100f, 0.1f, 1f) * uptime * opacity);
                Draw.rect(((ItemBridge) Blocks.phaseConduit).arrowRegion,
                        x + Geometry.d4(i).x * (tilesize / 2f + a * 4f + time % 4f),
                        y + Geometry.d4(i).y * (tilesize / 2f + a * 4f + time % 4f), i * 90f);
            }
            Draw.reset();
        }
    }
}
