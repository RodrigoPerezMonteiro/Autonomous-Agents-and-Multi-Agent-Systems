package almeidas.domain.agents.internal;

import java.util.Collection;

/**
 * Created by Rodrigo on 06/05/2015.
 */
public class Action {
    protected int x, y;
    protected ACTIONS action;

    public Action(int _x, int _y, ACTIONS _action) {
        x = _x;
        y = _y;
        action = _action;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPossible(Collection<InternalTile.PERCEPTION> perceptions) {
        if (action == ACTIONS.GRAB_GARBAGE) {
            return perceptions.contains(InternalTile.PERCEPTION.SWEPT_GARBAGE);
        }
        if (action == ACTIONS.ATTACK_ENEMY) {
            return perceptions.contains(InternalTile.PERCEPTION.ENEMY);
        }
        if (action == ACTIONS.GRAB_POWERUP) {
            return perceptions.contains(InternalTile.PERCEPTION.POWERUP_ENERGY) ||
                    perceptions.contains(InternalTile.PERCEPTION.POWERUP_RESISTANCE) ||
                    perceptions.contains(InternalTile.PERCEPTION.POWERUP_SPEED) ||
                    perceptions.contains(InternalTile.PERCEPTION.POWERUP_STRENGTH);
        }
        if (action == ACTIONS.GRAB_SNITCH) {
            return perceptions.contains(InternalTile.PERCEPTION.SNITCH);
        }
        if (action == ACTIONS.SWEEP_GARBAGE) {
            return perceptions.contains(InternalTile.PERCEPTION.GARBAGE);
        }
        return true;
    }

    public ACTIONS getAction() {
        return action;
    }

    public enum ACTIONS {
        MOVE, GRAB_GARBAGE, ATTACK_ENEMY, GRAB_POWERUP, GRAB_SNITCH, DROP_GARBAGE, SWEEP_GARBAGE
    }
}
