package almeidas.domain.agents.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rodrigo on 06/05/2015.
 */
public class Plan {

    protected List<Action> actions;
    protected Intention intention;

    public Plan(Intention _intention) {
        actions = new LinkedList<Action>();
        intention = _intention;
    }

    public void addAction(Action a) {
        actions.add(a);
    }

    public Action popAction() {
        return actions.remove(0);
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(actions);
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }
}
