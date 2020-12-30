package sonnicon.newhorizons.types;

public class Pair<T, S>{
    protected T x;
    protected S y;

    public Pair(){}

    public Pair(T x, S y){
        set(x, y);
    }

    public Pair<T, S> setX(T x){
        this.x = x;
        return this;
    }

    public T getX(){
        return this.x;
    }

    public Pair<T, S> setY(S y){
        this.y = y;
        return this;
    }

    public S getY(){
        return this.y;
    }

    public Pair<T, S> set(T x, S y){
        setX(x);
        setY(y);
        return this;
    }
}
