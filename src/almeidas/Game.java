package almeidas;

import java.awt.EventQueue;
import javax.swing.JFrame;

import almeidas.domain.Board;
import almeidas.graficos.BoardPanel;

public class Game extends JFrame{

    private static Board board;

    public Game() {

        BoardPanel bp = new BoardPanel();
        add(bp);
        board = bp.getBoard();

        setResizable(true);
        pack();

        setTitle("ALMEIDAS - REACTIVE ARCHITECTURE");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    public static Board getBoard(){
        return board;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame ex = new Game();
                ex.setVisible(true);
                ex.setResizable(false);
            }
        });

        while(getBoard() == null);
        while(!getBoard().isOver());
        
    }
}
