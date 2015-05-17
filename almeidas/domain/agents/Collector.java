package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.domain.Perception;
import almeidas.domain.agents.internal.Action;
import almeidas.domain.agents.internal.Desire;
import almeidas.domain.agents.internal.InternalTile;
import almeidas.graficos.BoardPanel;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Collector extends VaccumCleaner {

    public static final float defaultSpeed = 1;
    public static final float defaultEnergy = 50;
    public static final float defaultResistance = 40;
    public static final float defaultStrength = 10;
    public static final int garbageCapacity = 50;

    private static final int GARBAGE = 0;
    private static final int SWIPED_GARBAGE = 1;
    private static final int POWERUP = 2;
    private static final int ENEMY = 3;
    private static final int BASE = 4;
    private static final int HAS_GARBAGE = 5;
    private static final int SNITCH = 6;

    private static final int DROP_GARBAGE_THRESHOLD = 5;
    private static final int PROTECT_FRIEND_RESISTANCE_THRESHOLD = (int) defaultResistance / 2;
    private static final int PROTECT_FRIEND_DISTANCE_THRESHOLD = 4;
    private static final int GRAB_POWERUP_THRESHOLD = 2;
    private static final int ATTACK_ENEMY_THRESHOLD = 3;
    public int garbage;
    BufferedImage sprite;

    public Collector(int _x, int _y, Team _team, BufferedImage _sprite, Board board, int id, String architecture, boolean communication) {
        super(_x, _y, defaultSpeed, defaultStrength, defaultResistance, defaultEnergy, _team, board, id, 2, architecture, communication);
        sprite = _sprite;
        garbage = 0;
    }

    public void render(Graphics2D g) {

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("default", Font.BOLD, 10));
        g.setColor(Color.BLUE);
        g.drawString("" + getSpeed(), BoardPanel.SQUARE_SIZE * this.getX() + 35, BoardPanel.SQUARE_SIZE * this.getY() + 10);
        g.setColor(Color.GREEN);
        g.drawString("" + getEnergy(), BoardPanel.SQUARE_SIZE * this.getX() + 30, BoardPanel.SQUARE_SIZE * this.getY() + 20);
        g.setColor(Color.ORANGE);
        g.drawString("" + getResistance(), BoardPanel.SQUARE_SIZE * this.getX() + 30, BoardPanel.SQUARE_SIZE * this.getY() + 30);
        g.setColor(Color.RED);
        g.drawString("" + getStrength(), BoardPanel.SQUARE_SIZE * this.getX() + 30, BoardPanel.SQUARE_SIZE * this.getY() + 40);
        g.setColor(Color.GRAY);
        g.drawString("ID:" + getId(), BoardPanel.SQUARE_SIZE * this.getX() + 20, BoardPanel.SQUARE_SIZE * this.getY() + 50);
        g.setFont(new Font("default", Font.BOLD, 15));
        g.setColor(Color.CYAN);
        g.drawString("" + garbage, BoardPanel.SQUARE_SIZE * this.getX() + 3, BoardPanel.SQUARE_SIZE * this.getY() + 50);

        g.drawImage(sprite, BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE * this.getY(), BoardPanel.SQUARE_SIZE / 2, BoardPanel.SQUARE_SIZE / 2, null);

        if (this.getDead()) {
            g.setFont(new Font("default", Font.ROMAN_BASELINE, 9));
            g.setColor(Color.RED);
            g.drawString("DEAD", BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE * this.getY() + 30);
        }
    }

    public void think() {
        if (this.isReactive()) this.thinkReactive();
        else if (this.isHybrid()) this.thinkHybrid();
        else if (this.isBDI()) this.thinkBDI();
    }

    public void thinkReactive() {
        int sleepTime;
        int defaultSleep = 5000;

        Boolean[] perceptions;

        while (getIsRunning()) {
            sleepTime = Math.round((defaultSleep / getBoard().getGameSpeed()) / this.getSpeed());
            perceptions = percept();
            System.out.println("Collector - ID: " + getId() + " - Perceptions - GARBAGE: " + perceptions[0] + " SWEPT_GARBAGE: " + perceptions[1] + " POWERUP: " + perceptions[2] + " ENEMY: " + perceptions[3] + " BASE: " + perceptions[4] + " HAS_GARBAGE: " + perceptions[5] + " SNITCH: " + perceptions[6]);

            if (!getDead()) {
                if (getEnergy() > 0) {
                    if (perceptions[SNITCH]) {
                        grabSnitch();
                        setEnergy(getEnergy() - 1);
                    } else if (perceptions[SWIPED_GARBAGE]) { //meaning the tile has garbage and is swiped
                        grabGarbage();
                        setEnergy(getEnergy() - 1);
                    } else if (perceptions[BASE] && perceptions[HAS_GARBAGE]) { //meaning the collector is in the mother base and has garbage
                        dropTheGarbage();
                        setEnergy(getEnergy() - 1);
                    } else if (perceptions[POWERUP]) { //check if the tile has a power up
                        grabPowerUp();
                        setEnergy(getEnergy() - 1);
                    } else if (perceptions[ENEMY]) { //see if there's an agent that is an enemy in the title
                        attack();
                        setEnergy(getEnergy() - 1);
                    } else {
                        if (move())
                            setEnergy(getEnergy() - 1);
                    }
                } else rest();
            }

            try {
                getThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                System.err.println("THREAD SLEEP EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    public void thinkHybrid() {
        int sleepTime;
        int defaultSleep = 3000;

        while (!getDead() && getIsRunning()) {
            sleepTime = Math.round((defaultSleep / getBoard().getGameSpeed()) / getSpeed());
            Boolean[] perceptionsReactive = percept();
            ArrayList<Perception> perceptionsBDI = getBoard().perceptBDI(getX(), getY(), getTeam(), getRange());
            BRF(perceptionsBDI);
            if (isCommunicationEnabled()) sendMessage(generateMessage());

            if (!checkNearPerception(perceptionsBDI)) {

                desires = options();

                intention = filter();

                if (intention != null) {

                    intention.setPlan(plan());

                    while (!(intention.getPlan().isEmpty() || succeeded(intention) || impossible(intention)) && !getDead() && getIsRunning()) {
                        sleepTime = Math.round((defaultSleep / getBoard().getGameSpeed()) / getSpeed());
                        if (getEnergy() == 0) {
                            setEnergy(getDefaultEnergy());
                        } else {

                            Action a = intention.getPlan().popAction();
                            execute(a);

                            perceptionsBDI = getBoard().perceptBDI(getX(), getY(), getTeam(), getRange());
                            BRF(perceptionsBDI);

                            if (isCommunicationEnabled()) sendMessage(generateMessage());

                            if (!sound(intention.getPlan())) {
                                desires = options();
                                intention = filter();
                                if (intention == null) {
                                    break;
                                }
                                intention.setPlan(plan());

                            }
                            setEnergy(getEnergy() - 1);
                            try {
                                getThread().sleep(sleepTime);
                            } catch (InterruptedException e) {
                                System.err.println("THREAD SLEEP EXCEPTION");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                if (!getDead()) {
                    if (getEnergy() > 0) {
                        if (perceptionsReactive[SNITCH]) {
                            grabSnitch();
                            setEnergy(getEnergy() - 1);
                        } else if (perceptionsReactive[SWIPED_GARBAGE]) { //meaning the tile has garbage and is swiped
                            grabGarbage();
                            setEnergy(getEnergy() - 1);
                        } else if (perceptionsReactive[BASE] && perceptionsReactive[HAS_GARBAGE]) { //meaning the collector is in the mother base and has garbage
                            dropTheGarbage();
                            setEnergy(getEnergy() - 1);
                        } else if (perceptionsReactive[POWERUP]) { //check if the tile has a power up
                            grabPowerUp();
                            setEnergy(getEnergy() - 1);
                        } else if (perceptionsReactive[ENEMY]) { //see if there's an agent that is an enemy in the title
                            attack();
                            setEnergy(getEnergy() - 1);
                        } else {
                            if (move())
                                setEnergy(getEnergy() - 1);
                        }
                    } else rest();
                }
            }
            try {
                getThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                System.err.println("THREAD SLEEP EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    public Boolean[] percept() {
        Boolean[] perceptions = new Boolean[7];
        perceptions[GARBAGE] = checkGarbage(); //0 if there's no garbage
        perceptions[SWIPED_GARBAGE] = checkSwipedGarbage(); //if there's garbage, 0 if it's raw. 1 if it's swiped
        perceptions[POWERUP] = checkPowerUp(); //1 if there's a power up
        perceptions[ENEMY] = (checkEnemy() != null); //1 if there's an enemy
        perceptions[BASE] = checkBase(); //1 if the agent is in the mother base
        perceptions[HAS_GARBAGE] = hasGarbage(); //1 if the collector is carrying garbage
        perceptions[SNITCH] = (hasSnitch() != null); //1 if the collector is carrying garbage
        return perceptions;
    }

    //Special perceptions
    public boolean hasGarbage() {
        return (garbage > 0);
    }

    public boolean checkBase() {
        return getBoard().perceptBase(getX(), getY(), getTeam());
    }

    public boolean checkSwipedGarbage() {
        return getBoard().perceptSweptGarbage(getX(), getY());
    }

    //Special Actions
    protected void grabGarbage() {
        System.out.println("GRAB: " + getTeam().toString() + ", ID = " + getId() + "grab = " + getX() + "," + getY());

        int garbageAsked = garbageCapacity - garbage;
        garbage += getBoard().grabGarbage(garbageAsked, getX(), getY());
    }

    protected void dropTheGarbage() {
        if (getTeam() == Team.RED_TEAM) {
            getBoard().addScoreRedTeam(garbage);
        } else {
            getBoard().addScoreBlueTeam(garbage);
        }
        System.out.println("DROP: " + getTeam().toString() + ", ID = " + getId() + "drop = " + getX() + "," + getY());
        garbage = 0;
    }

    public void rest() {
        System.out.println("REST: " + getTeam().toString() + ", ID = " + getId());
        setEnergy(defaultEnergy);
    }

    @Override
    protected HashMap<Desire, Float> options() {
        HashMap<Desire, Float> desires = new HashMap<Desire, Float>();

        if (garbage >= DROP_GARBAGE_THRESHOLD || !getBoard().anyGarbageLeft()) {
            desires.put(new Desire(Desire.DESIRES.DROP_GARBAGE), 8F);
        }

        if (garbage <= garbageCapacity) {
            Pair<Integer, Integer> pos = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.SWEPT_GARBAGE);
            if (pos != null) {
                boolean imInTile = !(this.getX() != pos.getKey() && this.getY() != pos.getValue());
                if (!getBoard().isOccupied(pos.getKey(), pos.getValue()) || imInTile)
                    desires.put(new Desire(Desire.DESIRES.GRAB_GARBAGE), 7F);
            }
        }

        if (getResistance() > PROTECT_FRIEND_RESISTANCE_THRESHOLD) {
            Pair<Integer, Integer> pos = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.RESCUE_FRIEND);
            if (pos != null) {
                double distance = EuclideanDistance(getX(), pos.getKey(), getY(), pos.getValue());
                if (distance <= PROTECT_FRIEND_DISTANCE_THRESHOLD) {
                    desires.put(new Desire(Desire.DESIRES.PROTECT_FRIEND), 6F);
                }
            }
        }

        Pair<Integer, Integer> posPowerup = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.POWERUP);
        if (posPowerup != null) {
            if (EuclideanDistance(getX(), posPowerup.getKey(), getY(), posPowerup.getValue()) < GRAB_POWERUP_THRESHOLD) {
                desires.put(new Desire(Desire.DESIRES.GRAB_POWERUP), 5F);
            }
        }

        boolean losing = (getTeam() == Team.BLUE_TEAM ? getBoard().getScoreBlueTeam() - getBoard().getScoreRedTeam() < 0
                : getBoard().getScoreRedTeam() - getBoard().getScoreBlueTeam() < 0);
        Pair<Integer, Integer> posSnitch = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.SNITCH);
        if (posSnitch != null) {
            boolean snitchNear = EuclideanDistance(getX(), posSnitch.getKey(), getY(), posSnitch.getValue()) <= getRange();
            if (losing && snitchNear) {
                desires.put(new Desire(Desire.DESIRES.CATCH_SNITCH), 4F);
            }
            if (!losing && snitchNear) {
                desires.put(new Desire(Desire.DESIRES.CATCH_SNITCH), 1F);
            }
        }

        desires.put(new Desire(Desire.DESIRES.FOLLOW), 3F);

        Pair<Integer, Integer> posEnemy = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.ENEMY);
        if (posEnemy != null) {
            if (EuclideanDistance(getX(), posEnemy.getKey(), getY(), posEnemy.getValue()) < ATTACK_ENEMY_THRESHOLD) {
                desires.put(new Desire(Desire.DESIRES.ATTACK_ENEMY), getBoard().anyGarbageLeft() ? 10F : 2F);
            }
        }

        desires.put(new Desire(Desire.DESIRES.EXPLORE), 0.5F);

        return desires;
    }

    public int getDefaultResistance() {
        return (int) defaultResistance;
    }

    public int getDefaultEnergy() {
        return (int) defaultEnergy;
    }

    public boolean checkNearPerception(ArrayList<Perception> perceptions) {
        boolean isNear = false;
        for (Perception p : perceptions) {
            boolean isInterestingOne = p.getPerception() == InternalTile.PERCEPTION.ENEMY ||
                    p.getPerception() == InternalTile.PERCEPTION.SNITCH;
            if (isInterestingOne)
                isNear |= ManhattanDistance(p.getX(), getX(), p.getY(), getY()) <= 1;
            boolean isInterestingZero = p.getPerception() == InternalTile.PERCEPTION.SWEPT_GARBAGE ||
                    p.getPerception() == InternalTile.PERCEPTION.POWERUP_ENERGY ||
                    p.getPerception() == InternalTile.PERCEPTION.POWERUP_RESISTANCE ||
                    p.getPerception() == InternalTile.PERCEPTION.POWERUP_SPEED ||
                    p.getPerception() == InternalTile.PERCEPTION.POWERUP_STRENGTH;
            if (isInterestingZero)
                isNear |= ManhattanDistance(p.getX(), getX(), p.getY(), getY()) == 0;
        }
        return isNear;
    }
}
