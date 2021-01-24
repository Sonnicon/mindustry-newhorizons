package sonnicon.newhorizons.world.blocks;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.util.Align;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.ui.ItemDisplay;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import sonnicon.newhorizons.world.Multiblock;
import sonnicon.newhorizons.world.RelativeBlock;

import static mindustry.Vars.ui;

public class BlockMultiblockAssembly extends Block{

    public BlockMultiblockAssembly(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        consumesTap = true;
    }

    public class BuildingMultiblockAssembly extends Building{
        public Multiblock selected = null;

        @Override
        public void draw(){
            super.draw();

            if(selected != null){
                Draw.alpha(0.5f);
                for(RelativeBlock b : selected.getBlocks()){
                    b.draw(tile);
                }
                Draw.reset();
            }
        }

        @Override
        public void tapped(){
            if(selected == null){
                BaseDialog dialog = new BaseDialog("Multiblocks");
                dialog.addCloseButton();
                dialog.cont.pane(pane -> Multiblock.getMultiblocks().forEach((key, value) -> {
                    pane.table(Styles.black3, t -> {
                        t.button(key.localizedName, () -> {
                            if(tile.build == this){
                                configure(value);
                            }
                            dialog.hide();
                        }).width(300f);
                        t.button("?", Styles.defaultt, () -> {
                            ui.content.show(value.resultBlock);
                            Events.fire(new EventType.BlockInfoEvent());
                        }).padLeft(2f).name("blockinfo");
                        t.row();
                        t.table(costs -> {
                            for(ItemStack stack : value.costs){
                                costs.add(new ItemDisplay(stack.item, stack.amount, false)).pad(5f).size(40f);
                            }
                        });
                    }).fillX();
                    pane.row();
                })).fill().height(400f);
                dialog.show();
            }else{
                if(selected.verify(tile())){
                    tile().setBlock(selected.resultBlock, team(), rotation());
                }else{
                    selected = null;
                }
            }
        }

        @Override
        public void configure(Object value){
            selected = (Multiblock) value;
        }

        @Override
        public Multiblock config(){
            return selected;
        }
    }
}
