package almeidas.domain;

import almeidas.domain.agents.VaccumCleaner;
import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.util.ArrayList;

public class Tile {

    public ArrayList<VaccumCleaner> agents;
    private PowerUp powerUp;

    private int garbage;
    private int sweptGarbage;
    private int value;
    private int x, y;
    private Board board;

    public Tile(int X, int Y, Board b) {
        agents = new ArrayList<VaccumCleaner>();
        powerUp = null;
        garbage = 0;
        sweptGarbage = 0;
        value = 0;
        x = X;
        y = Y;
        board = b;
    }

    public void setValue(int increment){
        value += increment;
    }

    public int getValue(){
        return value;
    }

    public int getGarbage() {
        return garbage;
    }

    public void setGarbage(int garbage) {
        this.garbage = garbage;
    }

    public int getSweptGarbage() {
        return sweptGarbage;
    }

    public void setSweptGarbage(int _sweptGarbage) {
        sweptGarbage = _sweptGarbage;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public void setPowerUp(PowerUp powerUp) {
        this.powerUp = powerUp;
    }

    public void render(Graphics2D g){

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("default", Font.BOLD, 14));

        if(x == 0 && y == 0){
            g.setColor(Color.BLUE);
            g.drawString("" + this.getValue(), BoardPanel.SQUARE_SIZE * x + 40, BoardPanel.SQUARE_SIZE * y + 50);
            g.setStroke(new BasicStroke(4));
            g.drawLine(0, 0, 0, BoardPanel.SQUARE_SIZE * 2);
            g.drawLine(0, 0, BoardPanel.SQUARE_SIZE*2, 0);
            g.drawLine(0, BoardPanel.SQUARE_SIZE*2, BoardPanel.SQUARE_SIZE*2, BoardPanel.SQUARE_SIZE*2);
            g.drawLine( BoardPanel.SQUARE_SIZE*2, 0, BoardPanel.SQUARE_SIZE*2, BoardPanel.SQUARE_SIZE*2);
        }
        else if((x == (board.getWidth() - 1)) && (y == (board.getHeight() -1))) {
            g.setColor(Color.RED);
            g.drawString("" + this.getValue(), BoardPanel.SQUARE_SIZE * x + 40, BoardPanel.SQUARE_SIZE * y + 50);
            g.setStroke(new BasicStroke(4));
            int xw = BoardPanel.SQUARE_SIZE * (x+1);
            int yh = BoardPanel.SQUARE_SIZE * (y+1);
            g.drawLine(xw, yh, xw, yh - (BoardPanel.SQUARE_SIZE*2));
            g.drawLine(xw, yh, xw - (BoardPanel.SQUARE_SIZE*2), yh);
            g.drawLine(xw - (BoardPanel.SQUARE_SIZE*2), yh, xw - (BoardPanel.SQUARE_SIZE*2), yh - (BoardPanel.SQUARE_SIZE*2));
            g.drawLine(xw, yh - (BoardPanel.SQUARE_SIZE*2), xw - (BoardPanel.SQUARE_SIZE*2), yh - (BoardPanel.SQUARE_SIZE*2));
        }
    }
}
