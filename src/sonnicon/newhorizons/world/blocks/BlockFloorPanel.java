package sonnicon.newhorizons.world.blocks;

import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

public class BlockFloorPanel extends Block{
    public Floor floor;

    public BlockFloorPanel(String name, Floor floor){
        super(name);
        this.floor = floor;

        destructible = true;
        solid = false;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team){
        return super.canPlaceOn(tile, team) && tile.floor() != floor;
    }

    // Should not be using an entity for this, but I am pretty much forced to as far as I can tell
    public class BuildingFloorPanel extends Building{
        @Override
        public void onProximityAdded(){
            tile().setFloor(floor);
            tile.setBlock(Blocks.air);
        }
    }
}
