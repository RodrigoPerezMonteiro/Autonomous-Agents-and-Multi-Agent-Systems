package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Snitch extends VaccumCleaner{

    private boolean isRunning;
    private static final float SPEED = (float) 0.3;
    private Random rnd;
    BufferedImage sprite;

    public Snitch(int _x, int _y, Board _board, BufferedImage _sprite) {
        super(_x, _y, SPEED, 0, 0, 0, null, _board, -1);
        rnd = new Random();
        sprite = _sprite;
    }

    @Override
    public Team getTeam(){
        return null;
    }

    @Override
    public Boolean[] percept() {
        Boolean[] perceptions = new Boolean[1];

        perceptions[0] = (getBoard().perceptEnemy(getX(), getY(), getTeam()) != null);

        return perceptions;
    }

    @Override
    public void think() {
        Boolean[] perceptions = percept();
        Random rnd = new Random(4);

        if(perceptions[0]) teleport(true);
        else if(rnd.nextInt() != 0) teleport(false);
    }

    public void teleport(boolean seesEnemy){

        Board board = getBoard();
        int x = rnd.nextInt(board.getWidth());
        int y = rnd.nextInt(board.getHeight());
        while(board.isOccupied(x, y)){
            x = rnd.nextInt(board.getWidth());
            y = rnd.nextInt(board.getHeight());
        }

        System.out.println("SNITCH FLED FROM: " + getX() + ", " + getY() + " TO: "+ x + ", " + y + " || MOTIF: " + (seesEnemy ? "ENEMY AROUND" : "IT FEELS LIKE IT"));
        board.move(getX(), getY(), x, y, this);
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void render(Graphics2D g) {
        g.drawImage(sprite, BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE*this.getY(), BoardPanel.SQUARE_SIZE, BoardPanel.SQUARE_SIZE, null);
    }
}
