package sonnicon.newhorizons.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.gl.Shader;
import arc.math.geom.Vec2;
import arc.util.Time;
import sonnicon.newhorizons.core.Vars;
import sonnicon.newhorizons.entities.PowerBeam;
import sonnicon.newhorizons.types.ILoadContent;

public class Shaders implements ILoadContent{
    public static ShaderPowerBeam powerbeam;

    protected static Vec2 temp = new Vec2();

    @Override
    public void loadContent(){
        powerbeam = new ShaderPowerBeam();
    }

    public abstract class ModShader extends Shader{
        public ModShader(String frag){
            super(Core.files.internal("shaders/default.vert").readString(), frag);
        }

        public ModShader(Fi frag){
            this(frag.readString());
        }
    }

    public class ShaderPowerBeam extends ModShader{
        protected PowerBeam beam;

        public ShaderPowerBeam(){
            super(Vars.mod.root.child("shaders").child("powerbeam.frag"));
        }

        @Override
        public void apply(){
            temp.set(beam.getX(), beam.getY());
            Core.camera.project(temp);
            setUniformf("u_time", Time.globalTime / 100f);
            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformf("u_scale", mindustry.Vars.renderer.getScale());
            setUniformf("u_rotation", (float) Math.toRadians(beam.getRotation()));
            setUniformf("u_origin", temp.getX() / Core.graphics.getWidth(), temp.getY() / Core.graphics.getHeight());
            setUniformf("u_power", beam.getPower() / 60f);
        }

        public void set(PowerBeam beam){
            this.beam = beam;
        }
    }
}
