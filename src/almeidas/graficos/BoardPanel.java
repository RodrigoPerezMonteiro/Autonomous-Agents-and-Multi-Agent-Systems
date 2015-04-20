package almeidas.graficos;

import almeidas.domain.Board;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardPanel extends JPanel{

    public static int N_SQUARES = 10;

    public static final int SQUARE_SIZE = 50;
    public static final int WINDOW_X = N_SQUARES * SQUARE_SIZE;
    public static final int WINDOW_Y = N_SQUARES * SQUARE_SIZE;
    public static final int DELAY = 25;

    private Board board;

    JButton startButton, stopButton, initButton;
    JLabel labelNumberRobots;
    JTextField numberRobots;
    JLabel labelNumberSweepers;
    JLabel numberSweepers;
    JLabel labelNumberCollectors;
    JTextField numberCollectors;
    JLabel labelNumberPowerups;
    JTextField numberPowerups;
    JLabel labelNumberGarbage;
    JTextField numberGarbage;
    JLabel labelSpeed;
    JSlider speed;
    public static JLabel gameOver;
    public static JLabel redWins;
    public static JLabel blueWins;

    public BoardPanel() {
        board = new Board(N_SQUARES, N_SQUARES);
        initBoard();

        // Button for initializing
        ActionListener initButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                board.clear();
                int nRobots = Integer.parseInt(numberRobots.getText());
                int nCollectors = Integer.parseInt(numberCollectors.getText());
                int nPowerups = Integer.parseInt(numberPowerups.getText());
                int nGarbage = Integer.parseInt(numberGarbage.getText());
                gameOver.setVisible(false);
                redWins.setVisible(false);
                blueWins.setVisible(false);

                numberSweepers.setText("" + (nRobots - nCollectors));
                board.start(nRobots, nCollectors, nPowerups, nGarbage);
            }
        };

        //Button for running the threads
        ActionListener startButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                board.startAll();
                board.setStarted(true);
            }
        };

        //Button for stoping the game for a while
        ActionListener stopButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                board.pauseAll();
            }
        };

        initButton = new JButton("Initialize");
        initButton.setLocation(WINDOW_X + 10, 0);
        initButton.setSize(140, 40);
        initButton.addActionListener(initButtonListener);
        this.add(initButton);

        startButton = new JButton("Start");
        startButton.setLocation(WINDOW_X + 10, 40);
        startButton.setSize(140, 40);
        startButton.addActionListener(startButtonListener);
        this.add(startButton);

        stopButton = new JButton("Stop");
        stopButton.setLocation(WINDOW_X + 10, 80);
        stopButton.setSize(140, 40);
        stopButton.addActionListener(stopButtonListener);
        this.add(stopButton);

        // TextField and label to insert number of robots
        numberRobots = new JTextField("10");
        numberRobots.setLocation(WINDOW_X + 110, 120);
        numberRobots.setSize(40, 40);
        numberRobots.setHorizontalAlignment(JTextField.RIGHT);
        labelNumberRobots = new JLabel("# Robots:");
        labelNumberRobots.setLocation(WINDOW_X + 5, 120);
        labelNumberRobots.setSize(100, 40);
        labelNumberRobots.setHorizontalAlignment(JTextField.RIGHT);
        this.add(labelNumberRobots);
        this.add(numberRobots);

        // TextField and label to insert number of collectors per team
        numberCollectors = new JTextField("3");
        numberCollectors.setLocation(WINDOW_X + 110, 160);
        numberCollectors.setSize(40, 40);
        numberCollectors.setHorizontalAlignment(JTextField.RIGHT);
        labelNumberCollectors = new JLabel("# Collectors:");
        labelNumberCollectors.setLocation(WINDOW_X + 5, 160);
        labelNumberCollectors.setSize(100, 40);
        labelNumberCollectors.setHorizontalAlignment(JTextField.RIGHT);
        this.add(labelNumberCollectors);
        this.add(numberCollectors);

        // Labels to display number of sweepers per team
        numberSweepers = new JLabel("7");
        numberSweepers.setLocation(WINDOW_X + 110, 200);
        numberSweepers.setSize(40, 40);
        numberSweepers.setHorizontalAlignment(JTextField.RIGHT);
        labelNumberSweepers = new JLabel("# Sweepers:");
        labelNumberSweepers.setLocation(WINDOW_X + 5, 200);
        labelNumberSweepers.setSize(100, 40);
        labelNumberSweepers.setHorizontalAlignment(JTextField.RIGHT);
        this.add(labelNumberSweepers);
        this.add(numberSweepers);

        // Labels and textfield to enter number powerups
        numberPowerups = new JTextField("10");
        numberPowerups.setLocation(WINDOW_X + 110, 240);
        numberPowerups.setSize(40, 40);
        numberPowerups.setHorizontalAlignment(JTextField.RIGHT);
        labelNumberPowerups = new JLabel("# PowerUps:");
        labelNumberPowerups.setLocation(WINDOW_X + 5, 240);
        labelNumberPowerups.setSize(100, 40);
        labelNumberPowerups.setHorizontalAlignment(JTextField.RIGHT);
        this.add(labelNumberPowerups);
        this.add(numberPowerups);

        // Labels and textfield to enter number garbage
        numberGarbage = new JTextField("50");
        numberGarbage.setLocation(WINDOW_X + 110, 280);
        numberGarbage.setSize(40, 40);
        numberGarbage.setHorizontalAlignment(JTextField.RIGHT);
        labelNumberGarbage = new JLabel("# Garbage:");
        labelNumberGarbage.setLocation(WINDOW_X + 5, 280);
        labelNumberGarbage.setSize(100, 40);
        labelNumberGarbage.setHorizontalAlignment(JTextField.RIGHT);
        this.add(labelNumberGarbage);
        this.add(numberGarbage);

        // Labels and textfield to enter speed

        labelSpeed = new JLabel("Game Speed");
        labelSpeed.setLocation(WINDOW_X + 5, 320);
        labelSpeed.setSize(140, 40);
        labelSpeed.setHorizontalAlignment(JTextField.CENTER);
        speed = new JSlider(1, 20, board.getGameSpeed());
        speed.setLocation(WINDOW_X + 5, 340);
        speed.setSize(140, 40);
        speed.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider j = (JSlider)e.getSource();
                if(!j.getValueIsAdjusting()) {
                    board.setGameSpeed(j.getValue());
                    System.out.println("Changed speed to: " + board.getGameSpeed());
                }
            }
        });
        this.add(labelSpeed);
        this.add(speed);

        //Game Over Button
        gameOver = new JLabel("Game Over");
        gameOver.setLocation(WINDOW_X + 10, 400);
        gameOver.setSize(140, 40);
        gameOver.setHorizontalAlignment(JTextField.CENTER);
        gameOver.setFont(new Font("SansSerif", Font.BOLD, 19));
        gameOver.setOpaque(true);
        gameOver.setBackground(Color.red);
        gameOver.setVisible(false);
        this.add(gameOver);

        redWins = new JLabel("Red Wins");
        redWins.setLocation(WINDOW_X + 10, 440);
        redWins.setSize(140, 40);
        redWins.setHorizontalAlignment(JTextField.CENTER);
        redWins.setFont(new Font("SansSerif", Font.BOLD, 19));
        redWins.setForeground(Color.RED);
        redWins.setVisible(false);

        blueWins = new JLabel("Blue Wins");
        blueWins.setLocation(WINDOW_X + 10, 440);
        blueWins.setSize(140, 40);
        blueWins.setHorizontalAlignment(JTextField.CENTER);
        blueWins.setFont(new Font("SansSerif", Font.BOLD, 19));
        blueWins.setForeground(Color.BLUE);
        blueWins.setVisible(false);

        this.add(redWins);
        this.add(blueWins);

        this.setLayout(null);

        InterfaceRepaintThread IRT = new InterfaceRepaintThread(this);
        IRT.start();
    }

    public Board getBoard(){
        return board;
    }

    public static int getSQUARE_SIZE() {
        return SQUARE_SIZE;
    }

    class InterfaceRepaintThread extends Thread {

        private BoardPanel p;

        public InterfaceRepaintThread(BoardPanel _p) {
            p = _p;
        }

        public void run() {

            while(true) {
                try{
                        this.sleep(DELAY);
                        p.repaint();
                } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        }
    }

    private void initBoard() {

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(WINDOW_X + 160, WINDOW_Y));
        setDoubleBuffered(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        board.render(g2d);
    }

}