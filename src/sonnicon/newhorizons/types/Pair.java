package sonnicon.newhorizons.types;

public class Pair<T>{
    protected T x;
    protected T y;

    public Pair(){}

    public Pair(T x, T y){
        set(x, y);
    }

    public Pair<T> setX(T x){
        this.x = x;
        return this;
    }

    public T getX(){
        return this.x;
    }

    public Pair<T> setY(T y){
        this.y = y;
        return this;
    }

    public T getY(){
        return this.y;
    }

    public Pair<T> set(T x, T y){
        setX(x);
        setY(y);
        return this;
    }
}
