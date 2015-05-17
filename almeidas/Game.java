package almeidas;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Game extends JFrame {

    private static Board board;

    public Game() {

        BoardPanel bp = new BoardPanel();
        add(bp);
        board = bp.getBoard();

        setResizable(true);
        pack();

        setTitle("ALMEIDAS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static Board getBoard() {
        return board;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame ex = new Game();

                URL iconURL = getClass().getResource("garbage-man.png");
                ImageIcon icon = new ImageIcon(iconURL);

                ex.setIconImage(icon.getImage());
                ex.setVisible(true);
                ex.setResizable(false);
            }
        });

        while (getBoard() == null) ;
        while (!getBoard().isOver()) ;

    }
}
