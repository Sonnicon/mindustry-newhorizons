package sonnicon.newhorizons.content;

import arc.Events;
import mindustry.content.Bullets;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import sonnicon.newhorizons.types.IInit;

import java.util.HashSet;

public class Types implements IInit{
    public static HashSet<BulletType> lasers;

    @Override
    public void init(){
        Events.on(EventType.ClientLoadEvent.class, event -> {
            lasers = new HashSet<BulletType>(){{
                //todo
            }};
        });
    }
}
