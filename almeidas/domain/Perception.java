package almeidas.domain;

import almeidas.domain.agents.internal.InternalTile;

/**
 * Created by Rodrigo on 06/05/2015.
 */
public class Perception {

    protected int x, y;
    protected InternalTile.PERCEPTION perception;

    public Perception(int _x, int _y, InternalTile.PERCEPTION _perception) {
        x = _x;
        y = _y;
        perception = _perception;

    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public InternalTile.PERCEPTION getPerception() {
        return perception;
    }

    public void setPerception(InternalTile.PERCEPTION perception) {
        this.perception = perception;
    }
}
