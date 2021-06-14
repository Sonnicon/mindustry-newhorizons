package sonnicon.newhorizons.content;

import arc.Events;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBoltBulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.game.EventType;
import sonnicon.newhorizons.types.ILoadInit;

import java.util.HashSet;

public class Types implements ILoadInit{
    protected static final HashSet<BulletType> lasers = new HashSet<>();

    @Override
    public void initialize(){
        Events.on(EventType.ClientLoadEvent.class, event -> lasers.addAll(
            Vars.content.bullets().select(bulletType ->
                bulletType instanceof LaserBoltBulletType ||
                bulletType instanceof LaserBulletType)
            .list()));
    }

    public static boolean isLaser(BulletType type){
        return lasers.contains(type);
    }
}
