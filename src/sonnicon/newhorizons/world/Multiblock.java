package sonnicon.newhorizons.world;

import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.lang.reflect.Field;
import java.util.*;

import static mindustry.Vars.world;

public class Multiblock{
    public final Block resultBlock;
    public final List<RelativeBlock> blocks;
    public float drawOffsetX, drawOffsetY;

    public static final HashMap<Block, Multiblock> multiblocks = new HashMap<>();

    protected static final ArrayList<Tile> temp = new ArrayList<>();
    protected static Field blockField;

    public Multiblock(Block resultBlock, RelativeBlock... blocks){
        this(resultBlock, Arrays.asList(blocks));
    }

    public Multiblock(Block resultBlock, List<RelativeBlock> blocks){
        this.resultBlock = resultBlock;
        this.blocks = blocks;

        // (max + min) / 2
        drawOffsetX = (blocks.stream().max(Comparator.comparingInt(x -> x.x)).orElseThrow(NoSuchElementException::new).x +
                blocks.stream().min(Comparator.comparingInt(x -> x.x)).orElseThrow(NoSuchElementException::new).x) / 2f;
        drawOffsetY = (blocks.stream().max(Comparator.comparingInt(x -> x.y)).orElseThrow(NoSuchElementException::new).y +
                blocks.stream().min(Comparator.comparingInt(x -> x.y)).orElseThrow(NoSuchElementException::new).y) / 2f;
        multiblocks.put(resultBlock, this);
    }

    public boolean place(Tile origin){
        return place(origin, true);
    }

    public boolean place(Tile origin, boolean verify){
        if(verify && !verify(origin)) return false;

        temp.clear();
        for(RelativeBlock b : blocks){
            Tile t = b.fetch(origin);
            t.setBlock(Blocks.air);
            temp.add(t);
        }

        if(origin.block() != resultBlock){
            origin.setBlock(resultBlock, origin.team(), origin.build.rotation());
        }

        temp.forEach(tile -> {
            setTileBlock(tile, resultBlock);
            tile.build = origin.build;
        });

        return true;
    }

    public void remove(Tile origin){
        for(RelativeBlock b : blocks){
            Tile t = b.fetch(origin);
            setTileBlock(t, Blocks.air);
            t.build = null;
            world.notifyChanged(t);
        }
    }

    void setTileBlock(Tile tile, Block block){
        try{
            if(blockField == null){
                blockField = Tile.class.getDeclaredField("block");
                blockField.setAccessible(true);
            }
            blockField.set(tile, block);
        }catch(NoSuchFieldException | IllegalAccessException ex){
            Log.err(ex);
            ex.printStackTrace();
        }
    }

    public boolean verify(Tile origin){
        for(RelativeBlock b : blocks){
            if(!b.check(origin)) return false;
        }
        return true;
    }
}
