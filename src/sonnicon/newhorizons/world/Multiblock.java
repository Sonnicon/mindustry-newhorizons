package sonnicon.newhorizons.world;

import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import sonnicon.newhorizons.core.Util;

import java.lang.reflect.Field;
import java.util.*;

import static mindustry.Vars.world;

public class Multiblock{
    // Block placed when assembled
    public final Block resultBlock;
    // Blocks and positions for assembly
    protected final List<RelativeBlock> blocks;
    // Offset for drawing as entity tile is not always center
    protected final float drawOffsetX, drawOffsetY;
    // Total build cost
    public final ItemStack[] costs;

    // Every loaded multiblock
    protected static final HashMap<Block, Multiblock> multiblocks = new HashMap<>();

    protected static final ArrayList<Tile> temp = new ArrayList<>();

    public Multiblock(Block resultBlock, RelativeBlock... blocks){
        this(resultBlock, Arrays.asList(blocks));
    }

    public Multiblock(Block resultBlock, List<RelativeBlock> blocks){
        this.resultBlock = resultBlock;
        this.blocks = blocks;

        // Total up costs
        HashMap<Item, ItemStack> c = new HashMap<>();
        blocks.stream().map(block -> block.block.requirements).forEach((ItemStack[] itemStacks) -> {
            for(ItemStack stack : itemStacks){
                if(c.containsKey(stack.item)){
                    c.get(stack.item).amount += stack.amount;
                }else{
                    c.put(stack.item, stack);
                }
            }
        });
        costs = c.values().toArray(new ItemStack[]{});

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

        float health = 0f;
        if(verify){
            health += origin.build.health() / origin.build.maxHealth();
        }

        temp.clear();
        for(RelativeBlock b : blocks){
            Tile t = b.fetch(origin);
            if(verify){
                health += t.build.health() / t.build.maxHealth();
            }
            t.setBlock(Blocks.air);
            temp.add(t);
        }

        if(origin.block() != resultBlock){
            origin.setBlock(resultBlock, origin.team(), origin.build.rotation());
        }

        temp.forEach(tile -> {
            Util.setTileBlock(tile, resultBlock);
            tile.build = origin.build;
        });

        if(verify){
            origin.build.health(origin.build.health() * health / (temp.size() + 1));
        }

        return true;
    }

    public void remove(Tile origin){
        for(RelativeBlock b : blocks){
            Tile t = b.fetch(origin);
            Util.setTileBlock(t, Blocks.air);
            t.build = null;
            world.notifyChanged(t);
        }
    }

    public boolean verify(Tile origin){
        for(RelativeBlock b : blocks){
            if(!b.check(origin)) return false;
        }
        return true;
    }

    public List<RelativeBlock> getBlocks(){
        return blocks;
    }

    public static HashMap<Block, Multiblock> getMultiblocks(){
        return multiblocks;
    }
}
