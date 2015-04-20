package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.domain.Entity;
import almeidas.domain.PowerUp;

import java.awt.*;
import java.util.Random;

public abstract class VaccumCleaner extends Entity implements Runnable{

    private int x, y;
    private float speed, energy, strength, resistance;
    private Board board;
    private Thread thread;
    private boolean isRunning;
    private int id;
    private boolean dead = false;

    Team team;
    PowerUp currentPowerUp;

    public VaccumCleaner(int _x, int _y, float _speed, float _strength, float _resistance, float _energy, Team _team, Board _board, int _id){
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
    }

    public enum Team{
        RED_TEAM, BLUE_TEAM
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


    /*#########################
            Threads
      #########################*/
    @Override
    public void run(){
        int sleepTime = 0;
        while(isRunning){
            try {
                think();
                sleepTime = Math.round((5000 / board.getGameSpeed()) / getSpeed());
                thread.sleep(sleepTime);
            }
            catch(java.lang.InterruptedException IE){}
        }
    }

    public void stop(){
        isRunning = false;
    }

    public void resume(){
        isRunning = true;
    }

    public int getId(){
        return id;
    }

    public Thread getThread(){
        return thread;
    }


    /*#########################
          Perceptions
      #########################*/
    public boolean checkGarbage(){
        return getBoard().perceptGarbage(getX(), getY());
    }

    public boolean checkPowerUp(){
        return getBoard().perceptPowerUp(getX(), getY());
    }

    public int[] checkEnemy(){
        return getBoard().perceptEnemy(getX(), getY(), getTeam());
    }

    /*#########################
            Actions
      #########################*/

    public void takeDamage(float damage) {
        System.out.println("GOT HIT: " + getTeam().toString() + ", ID = " +  getId() + " position = " + getX() + "," + getY() + " damage = " + damage + " life left = " + (resistance-damage));

        resistance -= damage;
        if (resistance <= 0) {
            board.destroyAgent(this);
        }
    }

    public void attack() {
        int[] pos = checkEnemy();

        if(pos!=null){
            getBoard().attackTile(getTeam(), getStrength(), pos[0], pos[1], this);

            System.out.println("ATTACK: " + getTeam().toString() + ", ID = " + getId() + " move = " + getX() + "," + getY() + " -> " + pos[0] + "," + pos[1]);

        }
    }

    public void grabPowerUp() {
        currentPowerUp = getBoard().grabPowerUp(getX(), getY());
        if(currentPowerUp != null) {
            System.out.println("POWERUP: " + getTeam().toString() + ", ID = " +  getId() + " position = " + getX() + "," + getY() + " type = " + currentPowerUp.getType());
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

        if(canDo){
            if(!isOccupied(x, y)){
                getBoard().move(getX(), getY(), getX() + x, getY() + y, this);
                System.out.println("MOVE: " + getTeam().toString() + ", ID = " +  getId() + " move = " + getX() + "," + getY() + " -> " + (getX() + x) + "," + (getY() + y));
                setX(getX() + x);
                setY(getY() + y);
                move = true;
            }
        }
        return move;
    }

    private boolean canDo(int incX, int incY){

        int minX = 0;
        int minY = 0;
        int maxX = getBoard().getWidth() - 1;
        int maxY = getBoard().getHeight() - 1;
        boolean doValue = true;

        if(((getX() + incX) > maxX) || ((getX() + incX) < minX) || ((getY() + incY) > maxY) || ((getY() + incY) < minY)) doValue = false;

        return doValue;
    }

    private boolean isOccupied(int x, int y){ return board.isOccupied(getX() + x, getY() + y); }


    /*#########################
        Getters/Setters
      #########################*/
    public float getSpeed() {
        return speed;
    }

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

    public int getY() {
        return y;
    }

    public void setX(int _x) {
        x = _x;
    }

    public void setY(int _y) {
        y = _y;
    }

    public PowerUp getCurrentPowerUp() {
        return currentPowerUp;
    }

    public void setCurrentPowerUp(PowerUp _powerUp) {
        currentPowerUp = _powerUp;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
