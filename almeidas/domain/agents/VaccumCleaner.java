package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.domain.Entity;
import almeidas.domain.Perception;
import almeidas.domain.PowerUp;
import almeidas.domain.agents.internal.*;
import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class VaccumCleaner extends Entity implements Runnable {

    protected InternalBoard beliefs;
    protected HashMap<Desire, Float> desires;
    protected Intention intention;
    Team team;
    PowerUp currentPowerUp;
    private int x, y;
    private float speed, energy, strength, resistance;
    private Board board;
    private Thread thread;
    private boolean isRunning;
    private int id;
    private int range;
    private boolean dead = false;
    private boolean Reactive, Hybrid, BDI, communicationEnabled;

    public VaccumCleaner(int _x, int _y, float _speed, float _strength, float _resistance, float _energy, Team _team, Board _board, int _id, int _range, String architecture, boolean communication) {
        x = _x;
        y = _y;
        speed = _speed;
        strength = _strength;
        resistance = _resistance;
        energy = _energy;
        team = _team;
        board = _board;
        thread = new Thread(this, "Agent Thread: " + _id);
        isRunning = true;
        id = _id;
        range = _range;
        beliefs = new InternalBoard(board.getWidth(), board.getHeight());
        if (architecture.equals("BDI")) {
            BDI = true;
            Reactive = false;
            Hybrid = false;
        } else if (architecture.equals("Reactive")) {
            BDI = false;
            Reactive = true;
            Hybrid = false;
        } else if (architecture.equals("Hybrid")) {
            BDI = false;
            Reactive = false;
            Hybrid = true;
        }
        communicationEnabled = communication;
    }

    public abstract int getDefaultEnergy();

    public void thinkBDI(){

        int sleepTime;
        int defaultSleep = 3000;

        while (!dead && isRunning) {
            sleepTime = Math.round((defaultSleep / board.getGameSpeed()) / getSpeed());
            ArrayList<Perception> perceptions;

            perceptions = board.perceptBDI(getX(), getY(), getTeam(), range);
            BRF(perceptions);

            if (communicationEnabled) sendMessage(generateMessage());

            desires = options();

            intention = filter();

            if (intention != null) {

                intention.setPlan(plan());

                while (!(intention.getPlan().isEmpty() || succeeded(intention) || impossible(intention)) && !dead && isRunning) {
                    sleepTime = Math.round((defaultSleep / getBoard().getGameSpeed()) / getSpeed());

                    if (getEnergy() == 0) {
                        setEnergy(getDefaultEnergy());
                    } else {

                        Action a = intention.getPlan().popAction();
                        execute(a);

                        perceptions = board.perceptBDI(getX(), getY(), getTeam(), range);
                        BRF(perceptions);

                        if (communicationEnabled) sendMessage(generateMessage());

                        if (!sound(intention.getPlan())) {
                            desires = options();
                            intention = filter();
                            if (intention == null) {
                                break;
                            }
                            intention.setPlan(plan());

                        }
                        setEnergy(getEnergy() - 1);
                    }

                    try {
                        thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        System.err.println("THREAD SLEEP EXCEPTION");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public abstract void thinkHybrid();

    protected abstract HashMap<Desire, Float> options();

    protected Intention filter() {
        Desire finalDesire = null;
        Float finalDesireWeight = 0F;

        // Get the most weighted desire
        for (Map.Entry<Desire, Float> entry : desires.entrySet()) {
            if (entry.getValue() > finalDesireWeight) {
                finalDesire = entry.getKey();
                finalDesireWeight = entry.getValue();
            }
        }

        // Create intention accordingly
        Intention temp_intent = null;
        Pair<Integer, Integer> pos = null;
        try {
            switch (finalDesire.getGoal()) {
                case ATTACK_ENEMY:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.ENEMY);
                    break;
                case CATCH_SNITCH:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.SNITCH);
                    break;
                case DROP_GARBAGE:
                    Random rnd = new Random();
                    int r = rnd.nextInt(4);
                    if (team == Team.BLUE_TEAM) {
                        if(r == 0) pos = new Pair<Integer, Integer>(0, 0);
                        else if(r == 1) pos = new Pair<Integer, Integer>(0, 1);
                        else if(r == 2) pos = new Pair<Integer, Integer>(1, 0);
                        else pos = new Pair<Integer, Integer>(1, 1);
                    } else {
                        if(r == 0) pos = new Pair<Integer, Integer>(board.getWidth() - 1, board.getHeight() - 1);
                        else if(r == 1) pos = new Pair<Integer, Integer>(board.getWidth() - 2, board.getHeight() - 1);
                        else if(r == 2) pos = new Pair<Integer, Integer>(board.getWidth() - 1, board.getHeight() - 2);
                        else pos = new Pair<Integer, Integer>(board.getWidth() - 2, board.getHeight() - 2);
                    }
                    intention = new Intention(finalDesire, pos.getKey(), pos.getValue());
                    break;
                case GRAB_GARBAGE:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.SWEPT_GARBAGE);
                    break;
                case GRAB_POWERUP:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.POWERUP);
                    break;
                case PROTECT_FRIEND:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.RESCUE_FRIEND);
                    break;
                case SWEEP_GARBAGE:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.GARBAGE);
                    break;
                case FOLLOW:
                    pos = beliefs.getNearest(getX(), getY(), InternalTile.PERCEPTION.FRIEND_SWEEPER);
                    break;
                case EXPLORE:
                    int x;
                    int y;

                    boolean gotGoodPosition = false;

                    while (!gotGoodPosition) {
                        Random rand = new Random();
                        boolean canDo;
                        x = rand.nextInt(getBoard().getWidth());
                        y = rand.nextInt(getBoard().getHeight());

                        if (!getBoard().isOccupied(x, y)) {
                            gotGoodPosition = true;
                            pos = new Pair<Integer, Integer>(x, y);
                        }
                    }
                    break;
                default:
                    System.err.println("FILTER: No case selected.");
            }

            System.out.println("GOAL: " + finalDesire.getGoal() + " ID: " + this.getId() + " pos: " + pos);
            if (pos == null) {
                temp_intent = null;
            } else {
                temp_intent = new Intention(finalDesire, pos.getKey(), pos.getValue());
            }

        } catch (NullPointerException npe) {
            System.err.println("FILTER: Null Pointer Exception");
            npe.printStackTrace();
        }

        return temp_intent;
    }

    protected double EuclideanDistance(int x1, int x2, int y1, int y2) {
        return Math.sqrt(Math.abs(Math.pow(x1 - x2, 2)) + Math.abs(Math.pow(y1 - y2, 2)));
    }

    protected double ManhattanDistance(int x1, int x2, int y1, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    protected Plan plan() {
        Plan p = new Plan(intention);
        if (intention == null) {
            System.err.println("ERROR - PLAN: INTENTION IS NULL");
        }
        p.setActions(moveToGoal(intention.getX(), intention.getY()));
        switch (intention.getDesire().getGoal()) {
            case GRAB_POWERUP:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.MOVE));
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.GRAB_POWERUP));
                break;
            case GRAB_GARBAGE:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.MOVE));
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.GRAB_GARBAGE));
                break;
            case SWEEP_GARBAGE:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.MOVE));
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.SWEEP_GARBAGE));
                break;
            case DROP_GARBAGE:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.MOVE));
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.DROP_GARBAGE));
                break;
            case PROTECT_FRIEND:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.ATTACK_ENEMY));
                break;
            case ATTACK_ENEMY:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.ATTACK_ENEMY));
                break;
            case CATCH_SNITCH:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.GRAB_SNITCH));
                break;
            case EXPLORE:
                p.addAction(new Action(intention.getX(), intention.getY(), Action.ACTIONS.MOVE));
                break;
            case FOLLOW:
                // nothing to do here
                break;
        }
        return p;
    }

    protected boolean succeeded(Intention intention) {
        return intention.getPlan().isEmpty();
    }

    protected boolean impossible(Intention intention) {
        boolean res;
        int x = intention.getX(), y = intention.getY();
        switch (intention.getDesire().getGoal()) {
            case ATTACK_ENEMY:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.ENEMY);
            case CATCH_SNITCH:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.SNITCH);
            case DROP_GARBAGE:
                return false; // It should never be impossible to drop the garbage in the base
            case GRAB_GARBAGE:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.SWEPT_GARBAGE);
            case GRAB_POWERUP:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.POWERUP_ENERGY) &&
                        !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.POWERUP_RESISTANCE) &&
                        !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.POWERUP_SPEED) &&
                        !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.POWERUP_STRENGTH);
            case PROTECT_FRIEND:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.RESCUE_FRIEND);
            case SWEEP_GARBAGE:
                return !getInternalBoard().getPerceptions(x, y).contains(InternalTile.PERCEPTION.GARBAGE);
        }
        return false; // else it's not impossible
    }

    protected void execute(Action a) {
        switch (a.getAction()) {
            case MOVE:
                moveBDI(a.getX(), a.getY());
                break;
            case GRAB_GARBAGE:
                ((Collector) this).grabGarbage();
                break;
            case ATTACK_ENEMY:
                this.attack();
                break;
            case GRAB_POWERUP:
                this.grabPowerUp();
                break;
            case GRAB_SNITCH:
                this.grabSnitch();
                break;
            case DROP_GARBAGE:
                ((Collector) this).dropTheGarbage();
                break;
            case SWEEP_GARBAGE:
                ((Sweeper) this).sweepGarbage();
                break;
        }
    }

    protected boolean sound(Plan plan) {
        boolean isSound = true;

        // Can we do the first movement? Is it occupied or is it out of range?
        try {
            Action action = (Action) plan.getActions().toArray()[0];
            if (action.getAction() == Action.ACTIONS.MOVE) {
                isSound &= !board.isOccupied(action.getX(), action.getY());
                isSound &= canDo(action.getX() - this.getX(), action.getY() - this.getY());
            }

            for (Action a : plan.getActions()) {
                isSound &= a.isPossible(beliefs.getPerceptions(a.getX(), a.getY()));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // Plan is empty
        }
        return isSound;
    }

    //BELIEF REVISION FUNCTION

    public void BRF(ArrayList<Perception> perceptions) {

        for (Perception p : perceptions) {
            getInternalBoard().clearPerceptions(p.getX(), p.getY());
        }

        for (Perception p : perceptions) {
            getInternalBoard().addPerception(p.getPerception(), p.getX(), p.getY());
        }
    }

    public boolean getDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public abstract Boolean[] percept();

    public abstract void think();

    public abstract void render(Graphics2D g);

    @Override
    public void run() {
        think();
    }

    /*#########################
            Threads
      #########################*/

    public void stop() {
        isRunning = false;
    }

    protected boolean checkGarbage() {
        return getBoard().perceptGarbage(getX(), getY());
    }

    /*#########################
          Perceptions
      #########################*/

    protected boolean checkPowerUp() {
        return getBoard().perceptPowerUp(getX(), getY());
    }

    protected int[] checkEnemy() {
        return getBoard().perceptEnemy(getX(), getY(), getTeam());
    }

    protected int[] hasSnitch() {
        return getBoard().perceptSnitch(getX(), getY());
    }

    public void receiveMessage(ArrayList<AgentMessage> messages) {
        for (AgentMessage m : messages) {
            getInternalBoard().clearPerceptions(m.getX(), m.getY());
        }
        for (AgentMessage m : messages) {
            processMessage(m);
        }
    }

    /*#########################
          Communication
      #########################*/

    public void sendMessage(ArrayList<AgentMessage> message) {
        getBoard().sendBroadcast(message, this.getTeam());
    }

    public void processMessage(AgentMessage message) {
        getInternalBoard().addPerception(message.getPerception(), message.getX(), message.getY());
    }

    public ArrayList<AgentMessage> generateMessage() {
        ArrayList<AgentMessage> messageList = new ArrayList<AgentMessage>();
        InternalBoard ib = this.getInternalBoard();
        for (int i = 0; i < ib.getWidth(); i++) {
            for (int j = 0; j < ib.getHeight(); j++) {
                for (InternalTile.PERCEPTION currentPerception : beliefs.getPerceptions(i, j)) {

                    if ((currentPerception == InternalTile.PERCEPTION.ENEMY) ||
                            (currentPerception == InternalTile.PERCEPTION.WEAK_ENEMY)) {

                        AgentMessage m = new AgentMessage(i, j, AgentMessage.TYPE.WARNING, currentPerception);
                        messageList.add(m);
                    } else if ((currentPerception == InternalTile.PERCEPTION.SWEPT_GARBAGE) ||
                            (currentPerception == InternalTile.PERCEPTION.GARBAGE) ||
                            (currentPerception == InternalTile.PERCEPTION.RESCUE_FRIEND)) {

                        AgentMessage m = new AgentMessage(i, j, AgentMessage.TYPE.HELP_REQUEST, currentPerception);
                        messageList.add(m);
                    } else {
                        AgentMessage m = new AgentMessage(i, j, AgentMessage.TYPE.INFORMATION, currentPerception);
                        messageList.add(m);
                    }
                }
            }
        }


        return messageList;
    }

    public void grabSnitch() {
        System.out.println("GRAB SNITCH: " + getTeam().toString() + ", ID = " + getId());
        getBoard().grabSnitch(this, hasSnitch());
    }

    /*#########################
            Actions
      #########################*/

    public synchronized void takeDamage(float damage) {
        System.out.println("GOT HIT: " + getTeam().toString() + ", ID = " + getId() + " position = " + getX() + "," + getY() + " damage = " + damage + " life left = " + (resistance - damage));

        resistance -= damage;
        if (resistance <= 0) {
            board.destroyAgent(this);
        }
    }

    public void attack() {
        int[] pos = checkEnemy();

        if (pos != null) {
            getBoard().attackTile(getTeam(), getStrength(), pos[0], pos[1], this);

            System.out.println("ATTACK: " + getTeam().toString() + ", ID = " + getId() + " move = " + getX() + "," + getY() + " -> " + pos[0] + "," + pos[1]);

        }
    }

    public void grabPowerUp() {
        currentPowerUp = getBoard().grabPowerUp(getX(), getY());
        if (currentPowerUp != null) {
            System.out.println("POWERUP: " + getTeam().toString() + ", ID = " + getId() + " position = " + getX() + "," + getY() + " type = " + currentPowerUp.getType());
            switch (currentPowerUp.getType()) {
                case SPEED_UP:
                    setSpeed(getSpeed() + 1);
                    break;
                case STRENGTH_UP:
                    setStrength(getStrength() + currentPowerUp.getValue());
                    break;
                case RESISTANCE_UP:
                    setResistance(getResistance() + currentPowerUp.getValue());
                    break;
                case ENERGY_UP:
                    setEnergy(getEnergy() + currentPowerUp.getValue());
                    break;
            }
        }
    }

    public boolean move() {

        int x;
        int y;
        int r;
        Random rand = new Random();
        boolean canDo;
        boolean move = false;
        r = rand.nextInt(4);

        if (r == 0) {
            x = 0;
            y = 1;
        } else if (r == 1) {
            x = 0;
            y = -1;
        } else if (r == 2) {
            x = 1;
            y = 0;
        } else {
            x = -1;
            y = 0;
        }

        canDo = this.canDo(x, y);

        if (canDo) {
            if (!isOccupied(x, y)) {
                getBoard().move(getX(), getY(), getX() + x, getY() + y, this);
                System.out.println("MOVE: " + getTeam().toString() + ", ID = " + getId() + " move = " + getX() + "," + getY() + " -> " + (getX() + x) + "," + (getY() + y));
                setX(getX() + x);
                setY(getY() + y);
                move = true;
            }
        }
        return move;
    }

    public boolean moveBDI(int x, int y) {
        boolean move = false;
        boolean canDo = board.canDo(x, y, 0, 0);
        if (canDo) {
            if (!getBoard().isOccupied(x, y)) {
                getBoard().move(getX(), getY(), x, y, this);
                System.out.println("MOVE: " + getTeam().toString() + ", ID = " + getId() + " move = " + getX() + "," + getY() + " -> " + x + "," + y);
                setX(x);
                setY(y);
                move = true;
            }
        }
        return move;
    }

    public List<Action> moveToGoal(int goalX, int goalY) {
        LinkedList<Action> actions = new LinkedList<Action>();
        int x = getX();
        int y = getY();
        int currentX, currentY, chosenX, chosenY;
        chosenX = chosenY = 0;
        double currentDistance;
        double minDistance = Double.POSITIVE_INFINITY;

        while ((x != goalX) || (y != goalY)) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    currentX = x + i;
                    currentY = y + j;
                    if (getBoard().canDo(currentX, currentY, 0, 0)) {
                        // Not the same tile
                        if (!((i == 0) && (j == 0))) {
                            // Is not diagonal
                            if (!(i == j || -i == j)) {
                                currentDistance = ManhattanDistance(currentX, goalX, currentY, goalY);
                                if (currentDistance <= minDistance) {
                                    minDistance = currentDistance;
                                    chosenX = currentX;
                                    chosenY = currentY;
                                }
                            }
                        }
                    }
                }
            }
            x = chosenX;
            y = chosenY;
            Action action = new Action(x, y, Action.ACTIONS.MOVE);
            actions.add(action);
        }

        return actions;
    }

    protected boolean canDo(int incX, int incY) {

        int minX = 0;
        int minY = 0;
        int maxX = getBoard().getWidth() - 1;
        int maxY = getBoard().getHeight() - 1;
        boolean doValue = true;

        int range = getRange();


        if ((Math.abs(incX) > range) || (Math.abs(incY) > range)) doValue = false;
        else if (((getX() + incX) > maxX) || ((getX() + incX) < minX) || ((getY() + incY) > maxY) || ((getY() + incY) < minY))
            doValue = false;

        return doValue;
    }

    private boolean isOccupied(int x, int y) {
        return board.isOccupied(getX() + x, getY() + y);
    }

    public float getSpeed() {
        return speed;
    }

    /*#########################
        Getters/Setters
      #########################*/

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getResistance() {
        return resistance;
    }

    public void setResistance(float resistance) {
        this.resistance = resistance;
    }

    public Board getBoard() {
        return board;
    }

    public int getX() {
        return x;
    }

    public void setX(int _x) {
        x = _x;
    }

    public int getY() {
        return y;
    }

    public void setY(int _y) {
        y = _y;
    }

    public Team getTeam() {
        return team;
    }

    public InternalBoard getInternalBoard() {
        return beliefs;

    }

    public int getRange() {
        return this.range;
    }

    public abstract int getDefaultResistance();

    public boolean isReactive() {
        return Reactive;
    }

    public boolean isHybrid() {
        return Hybrid;
    }

    public boolean isBDI() {
        return BDI;
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public int getId() {
        return id;
    }

    public Thread getThread() {
        return thread;
    }

    public boolean isCommunicationEnabled() {
        return communicationEnabled;
    }

    public abstract boolean checkNearPerception(ArrayList<Perception> perceptions);

    public enum Team {
        RED_TEAM, BLUE_TEAM
    }
}