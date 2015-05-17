package almeidas.domain.agents.internal;

import java.util.*;

public class InternalTile {

    private Set<PERCEPTION> perceptions;

    public InternalTile() {
        perceptions = new HashSet<PERCEPTION>();
    }

    public void addPerception(PERCEPTION p) {
        perceptions.add(p);
    }

    public void removePerception(PERCEPTION p) {
        perceptions.remove(p);
    }

    public void clearPerceptions() {
        perceptions.clear();
    }

    public Collection<PERCEPTION> getPerceptions() {
        ArrayList<InternalTile.PERCEPTION> _perceptions = new ArrayList<InternalTile.PERCEPTION>();
        for (InternalTile.PERCEPTION p : perceptions) {
            _perceptions.add(p);
        }
        return Collections.unmodifiableCollection(_perceptions);
    }

    public enum PERCEPTION {
        SNITCH, GARBAGE, SWEPT_GARBAGE, ENEMY, WEAK_ENEMY, FRIEND, FRIEND_SWEEPER, RESCUE_FRIEND,
        POWERUP_ENERGY, POWERUP_STRENGTH, POWERUP_SPEED, POWERUP_RESISTANCE,
        POWERUP // used only for search for generic POWERUP
    }


}