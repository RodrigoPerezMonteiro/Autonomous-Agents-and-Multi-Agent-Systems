package almeidas.domain.agents;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by theburdencarrier on 27/04/15.
 */
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
    public Boolean[] percept() {
        return new Boolean[0];
    }

    @Override
    public void think() {
        Board board = getBoard();
        int x = rnd.nextInt(board.getWidth());
        int y = rnd.nextInt(board.getHeight());
        while(board.isOccupied(x, y)){
            x = rnd.nextInt(board.getWidth());
            y = rnd.nextInt(board.getHeight());
        }
        board.move(getX(), getY(), x, y, this);
        this.setX(x);
        this.setY(y);
    }

    @Override
    public void render(Graphics2D g) {
        g.drawImage(sprite, BoardPanel.SQUARE_SIZE * this.getX(), BoardPanel.SQUARE_SIZE*this.getY(), BoardPanel.SQUARE_SIZE, BoardPanel.SQUARE_SIZE, null);
    }
}
