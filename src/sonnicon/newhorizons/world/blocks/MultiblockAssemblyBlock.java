package sonnicon.newhorizons.world.blocks;

import arc.graphics.g2d.Draw;
import mindustry.gen.Building;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import sonnicon.newhorizons.world.Multiblock;
import sonnicon.newhorizons.world.RelativeBlock;

public class MultiblockAssemblyBlock extends Block{

    public MultiblockAssemblyBlock(String name){
        super(name);

        destructible = true;
        solid = true;
        rotate = true;
        consumesTap = true;
    }

    public class MultiblockAssemblyBlockBuilding extends Building{
        public Multiblock selected = null;

        @Override
        public void draw(){
            super.draw();

            if(selected != null){
                Draw.alpha(0.5f);
                for(RelativeBlock b : selected.blocks){
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
                dialog.cont.pane(pane -> Multiblock.multiblocks.forEach((key, value) -> {
                    pane.button(key.localizedName, () -> {
                        if(tile.build == this){
                            configure(value);
                        }
                        dialog.hide();
                    }).width(300f);
                    pane.row();
                })).width(320f).height(400f);
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
