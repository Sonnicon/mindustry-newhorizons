package sonnicon.newhorizons.world.floors;

import arc.Events;
import arc.util.Time;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Puddle;
import mindustry.world.blocks.environment.Floor;

public class FloorSilicon extends Floor{
    public FloorSilicon(String name){
        super(name);
    }

    public static void initialize(){
        Events.run(EventType.Trigger.update,
                () -> Groups.puddle.forEach((Puddle puddle) -> {
                    if(puddle.tile().floor() instanceof FloorSilicon){
                        puddle.amount += Time.delta * (1f - puddle.liquid().viscosity) / (8f);
                    }
                }));
    }
}
