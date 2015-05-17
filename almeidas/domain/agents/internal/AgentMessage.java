package almeidas.domain.agents.internal;

/**
 * Created by Rodrigo on 10/05/2015.
 */
public class AgentMessage {

    protected int x, y;
    protected TYPE type;
    protected InternalTile.PERCEPTION perception;

    public AgentMessage(int _x, int _y, TYPE _type, InternalTile.PERCEPTION _perception) {
        x = _x;
        y = _y;
        type = _type;
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

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public InternalTile.PERCEPTION getPerception() {
        return perception;
    }

    public void setPerception(InternalTile.PERCEPTION perception) {
        this.perception = perception;
    }

    public enum TYPE {
        WARNING, INFORMATION, HELP_REQUEST
    }
}

