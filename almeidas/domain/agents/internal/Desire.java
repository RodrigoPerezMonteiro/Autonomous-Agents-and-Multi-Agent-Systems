package almeidas.domain.agents.internal;

/**
 * Created by Rodrigo on 06/05/2015.
 */
public class Desire {
    protected DESIRES goal;

    public Desire(DESIRES _goal) {
        goal = _goal;
    }

    public DESIRES getGoal() {
        return goal;
    }

    public enum DESIRES {
        GRAB_POWERUP, GRAB_GARBAGE, SWEEP_GARBAGE, DROP_GARBAGE, PROTECT_FRIEND, ATTACK_ENEMY, CATCH_SNITCH, EXPLORE, FOLLOW
    }
}
