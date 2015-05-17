package almeidas.domain.agents.internal;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InternalBoard {
    private final Lock lock = new ReentrantLock();
    InternalTile[][] board;
    int width, height;


    public InternalBoard(int _width, int _height) {
        width = _width;
        height = _height;
        board = new InternalTile[width][height];
        clear();
    }

    public void addPerception(InternalTile.PERCEPTION p, int x, int y) {
        lock.lock();
        board[x][y].addPerception(p);
        lock.unlock();
    }

    public void removePerception(InternalTile.PERCEPTION p, int x, int y) {
        lock.lock();
        board[x][y].removePerception(p);
        lock.unlock();
    }

    public Collection<InternalTile.PERCEPTION> getPerceptions(int x, int y) {
        lock.lock();
        ArrayList<InternalTile.PERCEPTION> perceptions = new ArrayList<InternalTile.PERCEPTION>();
        for (InternalTile.PERCEPTION p : board[x][y].getPerceptions()) {
            perceptions.add(p);
        }
        lock.unlock();
        return Collections.unmodifiableCollection(perceptions);
    }

    public void clear() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[i][j] = new InternalTile();
            }
        }
    }

    public void clearPerceptions(int x, int y) {
        lock.lock();
        board[x][y].clearPerceptions();
        lock.unlock();
    }

    public Pair<Integer, Integer> getNearest(int x, int y, InternalTile.PERCEPTION perception) {
        lock.lock();
        Pair<Integer, Integer> nearest = null;
        search:
        for (int a = 1; a < width; a++) {
            for (int i = x - a; i < x + a; i++) {
                for (int j = y - a; j < y + a; j++) {
                    if (canDo(i, j)) {
                        if (perception == InternalTile.PERCEPTION.POWERUP) {
                            if (board[i][j].getPerceptions().contains(InternalTile.PERCEPTION.POWERUP_ENERGY)) {
                                nearest = new Pair<Integer, Integer>(i, j);
                                break search;
                            }
                            if (board[i][j].getPerceptions().contains(InternalTile.PERCEPTION.POWERUP_RESISTANCE)) {
                                nearest = new Pair<Integer, Integer>(i, j);
                                break search;
                            }
                            if (board[i][j].getPerceptions().contains(InternalTile.PERCEPTION.POWERUP_SPEED)) {
                                nearest = new Pair<Integer, Integer>(i, j);
                                break search;
                            }
                            if (board[i][j].getPerceptions().contains(InternalTile.PERCEPTION.POWERUP_STRENGTH)) {
                                nearest = new Pair<Integer, Integer>(i, j);
                                break search;
                            }
                        } else if (board[i][j].getPerceptions().contains(perception)) {
                            nearest = new Pair<Integer, Integer>(i, j);
                            break search;
                        }
                    }
                }
            }
        }
        lock.unlock();
        return nearest;
    }

    protected boolean canDo(int x, int y) {

        int minX = 0;
        int minY = 0;
        int maxX = width - 1;
        int maxY = height - 1;
        boolean doValue = true;

        if ((x > maxX) || (x < minX) || (y > maxY) || (y < minY)) doValue = false;

        return doValue;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
