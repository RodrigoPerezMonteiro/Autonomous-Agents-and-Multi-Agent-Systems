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

public class Sweeper extends VaccumCleaner {

    public static final float defaultSpeed = 2;
    public static final float defaultEnergy = 40;
    public static final float defaultResistance = 20;
    public static final float defaultStrength = 5;
    private static final int GARBAGE = 0;
    private static final int POWERUP = 1;
    private static final int ENEMY = 2;
    private static final int SNITCH = 3;

    private static final int GRAB_POWERUP_THRESHOLD = 2;
    private static final int PROTECT_FRIEND_RESISTANCE_THRESHOLD = (int) defaultResistance / 2;
    private static final int PROTECT_FRIEND_DISTANCE_THRESHOLD = 4;
    private static final int ATTACK_ENEMY_THRESHOLD = 3;

    BufferedImage sprite;

    public Sweeper(int _x, int _y, Team _team, BufferedImage _sprite, Board board, int id, String architecture, boolean communication) {
        super(_x, _y, defaultSpeed, defaultStrength, defaultResistance, defaultEnergy, _team, board, id, 2, architecture, communication);
        sprite = _sprite;
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
        g.drawString("" + getStrength(), BoardPanel.SQUARE_SIZE * this.getX() + 35, BoardPanel.SQUARE_SIZE * this.getY() + 40);
        g.setColor(Color.GRAY);
        g.drawString("ID:" + getId(), BoardPanel.SQUARE_SIZE * this.getX() + 20, BoardPanel.SQUARE_SIZE * this.getY() + 50);

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
            System.out.println("Sweeper - ID: " + getId() + " - Perceptions - GARBAGE: " + perceptions[0] + " POWERUP: " + perceptions[1] + " ENEMY: " + perceptions[2] + " SNITCH: " + perceptions[3]);
            if (!getDead()) {
                if (getEnergy() > 0) {
                    if (perceptions[SNITCH]) {
                        grabSnitch();
                        setEnergy(getEnergy() - 1);
                    } else if (perceptions[GARBAGE]) { //meaning the tile has garbage
                        sweepGarbage();
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
                        } else if (perceptionsReactive[GARBAGE]) { //meaning the tile has garbage
                            sweepGarbage();
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
        Boolean[] perceptions = new Boolean[4];
        perceptions[GARBAGE] = checkGarbage(); //0 if there's no garbage
        perceptions[POWERUP] = checkPowerUp(); //1 if there's a power up
        perceptions[ENEMY] = (checkEnemy() != null); //1 if there's an enemy
        perceptions[SNITCH] = (hasSnitch() != null); //1 if there's an enemy
        return perceptions;
    }

    //Special actions
    protected void sweepGarbage() {
        System.out.println("SWEEP: " + getTeam().toString() + ", ID = " + getId() + " sweep = " + getX() + "," + getY());
        getBoard().sweep(getX(), getY());
    }

    public void rest() {
        System.out.println("REST: " + getTeam().toString() + ", ID = " + getId());
        setEnergy(defaultEnergy);
    }

    @Override
    protected HashMap<Desire, Float> options() {
        HashMap<Desire, Float> desires = new HashMap<Desire, Float>();
        Pair<Integer, Integer> pos = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.GARBAGE);
        if (pos != null) {
            boolean imInTile = !(this.getX() != pos.getKey() && this.getY() != pos.getValue());
            boolean isTileOccupied = getBoard().isOccupied(pos.getKey(), pos.getValue());
            if (!isTileOccupied || imInTile) {
                desires.put(new Desire(Desire.DESIRES.SWEEP_GARBAGE), 7F);
                //System.err.println("SWEEP_GARBAGE DESIRE ID: " + this.getId() + " position: " + getX() + ", " + getY());
            }
        }

        Pair<Integer, Integer> posPowerup = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.POWERUP);
        if (posPowerup != null) {
            if (EuclideanDistance(getX(), posPowerup.getKey(), getY(), posPowerup.getValue()) < GRAB_POWERUP_THRESHOLD) {
                desires.put(new Desire(Desire.DESIRES.GRAB_POWERUP), 6F);
            }
        }

        boolean losing = (getTeam() == Team.BLUE_TEAM ? getBoard().getScoreBlueTeam() - getBoard().getScoreRedTeam() < 0
                : getBoard().getScoreRedTeam() - getBoard().getScoreBlueTeam() < 0);
        Pair<Integer, Integer> posSnitch = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.SNITCH);
        if (posSnitch != null) {
            boolean snitchNear = EuclideanDistance(getX(), posSnitch.getKey(), getY(), posSnitch.getValue()) <= getRange();
            if (losing && snitchNear) {
                desires.put(new Desire(Desire.DESIRES.CATCH_SNITCH), 5F);
            }
            if (!losing && snitchNear) {
                desires.put(new Desire(Desire.DESIRES.CATCH_SNITCH), 2F);
            }
        }

        if (getResistance() > PROTECT_FRIEND_RESISTANCE_THRESHOLD) {
            Pair<Integer, Integer> posFriend = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.RESCUE_FRIEND);
            if (posFriend != null) {
                double distance = EuclideanDistance(getX(), posFriend.getKey(), getY(), posFriend.getValue());
                if (distance <= PROTECT_FRIEND_DISTANCE_THRESHOLD) {
                    desires.put(new Desire(Desire.DESIRES.PROTECT_FRIEND), 4F);
                }
            }
        }

        Pair<Integer, Integer> posEnemy = getInternalBoard().getNearest(getX(), getY(), InternalTile.PERCEPTION.ENEMY);
        if (posEnemy != null) {
            if (EuclideanDistance(getX(), posEnemy.getKey(), getY(), posEnemy.getValue()) < ATTACK_ENEMY_THRESHOLD) {
                desires.put(new Desire(Desire.DESIRES.ATTACK_ENEMY), getBoard().anyGarbageLeft() ? 10F : 3F);
            }
        }

        desires.put(new Desire(Desire.DESIRES.EXPLORE), 1F);

        return desires;
    }

    @Override
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
            boolean isInterestingZero = p.getPerception() == InternalTile.PERCEPTION.GARBAGE ||
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
