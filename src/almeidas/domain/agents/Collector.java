package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Collector extends VaccumCleaner{

    public static final float defaultSpeed = 1;
    public static final float defaultEnergy = 50;
    public static final float defaultResistance = 40;
    public static final float defaultStrength = 10;
    public static final int garbageCapacity  = 50;
    private static final int GARBAGE = 0;
    private static final int SWIPED_GARBAGE = 1;
    private static final int POWERUP = 2;
    private static final int ENEMY = 3;
    private static final int BASE = 4;
    private static final int HAS_GARBAGE = 5;
    private static final int SNITCH = 6;

    BufferedImage sprite;
    public int garbage;

    public Collector(int _x, int _y, Team _team, BufferedImage _sprite, Board board, int id){
        super(_x, _y, defaultSpeed, defaultStrength, defaultResistance, defaultEnergy, _team, board, id);
        sprite = _sprite;
        garbage = 0;
    }

    public void render (Graphics2D g) {

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("default", Font.BOLD, 10));
        g.setColor(Color.BLUE);
        g.drawString("" + getSpeed(), BoardPanel.SQUARE_SIZE * this.getX() + 25, BoardPanel.SQUARE_SIZE * this.getY() + 10);
        g.setColor(Color.GREEN);
        g.drawString("" + getEnergy(), BoardPanel.SQUARE_SIZE * this.getX() + 25, BoardPanel.SQUARE_SIZE * this.getY() + 20);
        g.setColor(Color.ORANGE);
        g.drawString("" + getResistance(), BoardPanel.SQUARE_SIZE * this.getX() + 25, BoardPanel.SQUARE_SIZE * this.getY() + 30);
        g.setColor(Color.RED);
        g.drawString("" + getStrength(), BoardPanel.SQUARE_SIZE * this.getX() + 25, BoardPanel.SQUARE_SIZE * this.getY() + 40);
        g.setColor(Color.GRAY);
        g.drawString("ID:" + getId(), BoardPanel.SQUARE_SIZE * this.getX() + 20, BoardPanel.SQUARE_SIZE * this.getY() + 50);
        g.setFont(new Font("default", Font.BOLD, 15));
        g.setColor(Color.CYAN);
        g.drawString("" + garbage, BoardPanel.SQUARE_SIZE * this.getX() + 5, BoardPanel.SQUARE_SIZE * this.getY() + 50);

        g.drawImage(sprite, BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE*this.getY(), BoardPanel.SQUARE_SIZE/2, BoardPanel.SQUARE_SIZE/2, null);
    }

    public void think () {
        Boolean[] perceptions;
        perceptions = percept();

        if(!getDead()) {
            if (getEnergy() > 0) {
                if(perceptions[SNITCH]){
                    grabSnitch();
                    setEnergy(getEnergy()-1);
                }else if (perceptions[SWIPED_GARBAGE]) { //meaning the tile has garbage and is swiped
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
                    if(move())
                        setEnergy(getEnergy() - 1);
                }
            }
            else rest();
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
    public boolean hasGarbage(){
        return (garbage > 0);
    }

    public boolean checkBase(){
        return getBoard().perceptBase(getX(), getY(), getTeam());
    }

    public boolean checkSwipedGarbage(){
        return getBoard().perceptSweptGarbage(getX(), getY());
    }

    //Special Actions
    private void grabGarbage() {
        System.out.println("GRAB: " + getTeam().toString() + ", ID = " +  getId() + "grab = " + getX() + "," + getY());

        int garbageAsked = garbageCapacity - garbage;
        garbage += getBoard().grabGarbage(garbageAsked, getX(), getY());
    }

    private void dropTheGarbage() {
        if (getTeam() == Team.RED_TEAM) {
            getBoard().addScoreRedTeam(garbage);
        }
        else {
            getBoard().addScoreBlueTeam(garbage);
        }
        System.out.println("DROP: " + getTeam().toString() + ", ID = " +  getId() + "drop = " + getX() + "," + getY());
        garbage = 0;
    }

    public void rest(){
        System.out.println("REST: " + getTeam().toString() + ", ID = "  + getId());
        setEnergy(defaultEnergy);
    }
}
