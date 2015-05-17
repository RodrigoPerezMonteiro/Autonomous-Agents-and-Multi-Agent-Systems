package almeidas.domain.agents.internal;

/**
 * Created by Rodrigo on 06/05/2015.
 */
public class Intention {

    protected Plan plan;
    private Desire desire;
    private int x, y;

    public Intention(Desire _desire, int _x, int _y) {
        desire = _desire;
        x = _x;
        y = _y;
    }

    public Desire getDesire() {
        return desire;
    }

    public void setDesire(Desire desire) {
        this.desire = desire;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan p) {
        plan = p;
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

    public Action getNextAction() {
        return plan.popAction();
    }
}
