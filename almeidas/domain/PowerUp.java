package almeidas.domain;

import almeidas.graficos.BoardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PowerUp extends Entity {

    int x, y;
    BufferedImage sprite;
    private float value;
    private Type type;

    public PowerUp(float _value, Type _type, int _x, int _y, BufferedImage _sprite) {
        value = _value;
        type = _type;
        x = _x;
        y = _y;
        sprite = _sprite;
    }

    public float getValue() {
        if (getType() == Type.SPEED_UP) this.setValue(1);
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("default", Font.BOLD, 12));
        g.setColor(Color.BLACK);
        g.drawString("" + getValue(), BoardPanel.SQUARE_SIZE * this.getX() + BoardPanel.getSQUARE_SIZE() - 25, BoardPanel.SQUARE_SIZE * this.getY() + BoardPanel.getSQUARE_SIZE() - 5);
        g.drawImage(sprite, (BoardPanel.SQUARE_SIZE * this.getX()), (BoardPanel.SQUARE_SIZE * this.getY()) + BoardPanel.getSQUARE_SIZE() - (3 * BoardPanel.getSQUARE_SIZE() / 10), BoardPanel.SQUARE_SIZE / 4, BoardPanel.SQUARE_SIZE / 4, null);
    }

    public enum Type {
        SPEED_UP, STRENGTH_UP, RESISTANCE_UP, ENERGY_UP
    }

}
