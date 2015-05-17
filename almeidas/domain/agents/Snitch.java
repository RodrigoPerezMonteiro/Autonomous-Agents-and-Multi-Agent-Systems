package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.domain.Perception;
import almeidas.domain.agents.internal.Desire;
import almeidas.domain.agents.internal.Intention;
import almeidas.domain.agents.internal.Plan;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Snitch extends VaccumCleaner {

    private static final float SPEED = (float) 0.2;
    BufferedImage sprite;
    private Random rnd;

    public Snitch(int _x, int _y, Board _board, BufferedImage _sprite) {
        super(_x, _y, SPEED, 0, 0, 0, null, _board, -1, 2, "Reactive", false);
        rnd = new Random();
        sprite = _sprite;
    }

    public void BRF() {
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    protected HashMap<Desire, Float> options() {
        return null;
    }

    @Override
    protected Intention filter() {
        return null;
    }

    @Override
    protected Plan plan() {
        return null;
    }

    @Override
    protected boolean succeeded(Intention intention) {
        return false;
    }

    @Override
    protected boolean impossible(Intention intention) {
        return false;
    }

    @Override
    protected boolean sound(Plan plan) {
        return false;
    }

    @Override
    public Boolean[] percept() {
        Boolean[] perceptions = new Boolean[1];

        perceptions[0] = (getBoard().perceptEnemy(getX(), getY(), getTeam()) != null);

        return perceptions;
    }

    @Override
    public void think() {

        int sleepTime;
        int defaultSleep = 1000;

        Boolean[] perceptions;

        while (getIsRunning()) {
            sleepTime = Math.round((defaultSleep / getBoard().getGameSpeed()) / this.getSpeed());
            perceptions = percept();
            Random rnd = new Random(4);

            if (perceptions[0]) teleport(true);
            else if (rnd.nextInt() != 0) teleport(false);

            try {
                getThread().sleep(sleepTime);
            } catch (InterruptedException e) {
                System.err.println("THREAD SLEEP EXCEPTION");
                e.printStackTrace();
            }
        }
    }

    public void teleport(boolean seesEnemy) {

        Board board = getBoard();
        int x = rnd.nextInt(board.getWidth());
        int y = rnd.nextInt(board.getHeight());
        while (board.isOccupied(x, y)) {
            x = rnd.nextInt(board.getWidth());
            y = rnd.nextInt(board.getHeight());
        }

        System.out.println("SNITCH FLED FROM: " + getX() + ", " + getY() + " TO: " + x + ", " + y + " || MOTIF: " + (seesEnemy ? "ENEMY AROUND" : "IT FEELS LIKE IT"));
        board.move(getX(), getY(), x, y, this);
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void render(Graphics2D g) {
        g.drawImage(sprite, BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE * this.getY(), BoardPanel.SQUARE_SIZE, BoardPanel.SQUARE_SIZE, null);
    }

    @Override
    public int getDefaultResistance() {
        return 0;
    }

    @Override
    public boolean checkNearPerception(ArrayList<Perception> perceptions) {
        return false;
    }

    public int getDefaultEnergy() {
        return 0;
    }

    @Override
    public void thinkHybrid() {

    }


}
