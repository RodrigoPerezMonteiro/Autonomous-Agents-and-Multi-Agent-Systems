package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Sweeper extends VaccumCleaner{

    public static final float defaultSpeed = 2;
    public static final float defaultEnergy = 40;
    public static final float defaultResistance = 20;
    public static final float defaultStrength = 5;
    private static final int GARBAGE = 0;
    private static final int POWERUP = 1;
    private static final int ENEMY = 2;
    private static final int SNITCH = 3;

    BufferedImage sprite;

    public Sweeper(int _x, int _y, Team _team, BufferedImage _sprite, Board board, int id){
        super(_x, _y, defaultSpeed, defaultStrength, defaultResistance, defaultEnergy, _team, board, id);
        sprite = _sprite;
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
                }else if (perceptions[GARBAGE]) { //meaning the tile has garbage
                    sweepGarbage();
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
        Boolean[] perceptions = new Boolean[4];
        perceptions[GARBAGE] = checkGarbage(); //0 if there's no garbage
        perceptions[POWERUP] = checkPowerUp(); //1 if there's a power up
        perceptions[ENEMY] = (checkEnemy() != null); //1 if there's an enemy
        perceptions[SNITCH] = (hasSnitch() != null); //1 if there's an enemy
        return perceptions;
    }

    //Special actions
    private void sweepGarbage() {
        System.out.println("SWEEP: " + getTeam().toString() + ", ID = "  + getId() + " sweep = " + getX() + "," + getY());
        getBoard().sweep(getX(), getY());
    }

    public void rest(){
        System.out.println("REST: " + getTeam().toString() + ", ID = "  + getId());
        setEnergy(defaultEnergy);
    }
}
