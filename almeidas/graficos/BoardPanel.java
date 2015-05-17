package almeidas.graficos;

import almeidas.domain.Board;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class BoardPanel extends JPanel {

    public static final int SQUARE_SIZE = 50;
    public static final int DELAY = 25;
    public static int N_SQUARES = 15;
    public static final int WINDOW_X = N_SQUARES * SQUARE_SIZE;
    public static final int WINDOW_Y = N_SQUARES * SQUARE_SIZE;
    public static Timer timer;
    public static JLabel gameOver;
    public static JLabel redWins;
    public static JLabel blueWins;
    protected int nRobots;
    protected int nCollectors;
    protected int nPowerups;
    protected int nGarbage;
    protected String currentArchitecture = "BDI";
    protected boolean communication = true;
    ActionListener timerListener;
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
    JLabel comboLabel;
    JComboBox comboboxxx;
    JLabel communicationLabel;
    JComboBox communicationBox;
    JLabel timeLabel;
    long start, end, elapsed, last, temp;
    private Board board;

    public BoardPanel() {
        board = new Board(N_SQUARES, N_SQUARES, currentArchitecture, communication);
        initBoard();

        // Button for initializing
        ActionListener initButtonListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                board.clear();
                nRobots = Integer.parseInt(numberRobots.getText());
                nCollectors = Integer.parseInt(numberCollectors.getText());
                nPowerups = Integer.parseInt(numberPowerups.getText());
                nGarbage = Integer.parseInt(numberGarbage.getText());
                gameOver.setVisible(false);
                redWins.setVisible(false);
                blueWins.setVisible(false);
                timer = new Timer(10, timerListener);
                timer.setInitialDelay(0);
                temp = 0;
                timeLabel.setText(String.format("%02d:%02d.%03d",
                                TimeUnit.MILLISECONDS.toMinutes(temp),
                                TimeUnit.MILLISECONDS.toSeconds(temp) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(temp)),
                                (temp - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(temp))))
                );
                speed.setValue(1);
                numberSweepers.setText("" + (nRobots - nCollectors));
                board = new Board(N_SQUARES, N_SQUARES, currentArchitecture, communication);
                initBoard();
                board.start(nRobots, nCollectors, nPowerups, nGarbage);
            }
        };

        //Button for running the threads
        ActionListener startButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                board.startAll();
                board.setStarted(true);
                start = System.currentTimeMillis();
                last = start;
                temp = 0;
                timer.start();
            }
        };

        //Button for stoping the game for a while
        ActionListener stopButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                board.pauseAll();
                timer.stop();
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
        numberGarbage = new JTextField("100");
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
                JSlider j = (JSlider) e.getSource();
                if (!j.getValueIsAdjusting()) {
                    board.setGameSpeed(j.getValue());
                    System.out.println("Changed speed to: " + board.getGameSpeed());
                }
            }
        });
        this.add(labelSpeed);
        this.add(speed);

        //COMBOBOXXXXXXX
        comboLabel = new JLabel("Architecture");
        comboLabel.setLocation(WINDOW_X + 5, 370);
        comboLabel.setSize(140, 40);
        comboLabel.setHorizontalAlignment(JTextField.CENTER);

        String[] comboOpts = {"BDI", "Hybrid", "Reactive"};
        comboboxxx = new JComboBox(comboOpts);
        comboboxxx.setSelectedIndex(0);
        comboboxxx.setLocation(WINDOW_X + 5, 400);
        comboboxxx.setSize(150, 30);
        comboboxxx.setVisible(true);

        comboboxxx.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox cb = (JComboBox) ae.getSource();
                String arch = (String) cb.getSelectedItem();

                currentArchitecture = arch;
                board = new Board(N_SQUARES, N_SQUARES, arch, communication);
                initBoard();
                board.start(nRobots, nCollectors, nPowerups, nGarbage);
                System.out.println("Arquitecture set to " + arch);
            }
        });

        this.add(comboboxxx);
        this.add(comboLabel);

        //COMBOBOXXXXXXX
        communicationLabel = new JLabel("Communications");
        communicationLabel.setLocation(WINDOW_X + 5, 430);
        communicationLabel.setSize(140, 40);
        communicationLabel.setHorizontalAlignment(JTextField.CENTER);

        String[] communicationOpts = {"ENABLED", "DISABLED"};
        communicationBox = new JComboBox(communicationOpts);
        communicationBox.setSelectedIndex(0);
        communicationBox.setLocation(WINDOW_X + 5, 460);
        communicationBox.setSize(150, 30);
        communicationBox.setVisible(true);

        communicationBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JComboBox cb = (JComboBox) ae.getSource();
                String option = (String) cb.getSelectedItem();

                boolean communicationsON = communication;

                if (option.equals("ENABLED")) {
                    communicationsON = true;
                } else if (option.equals("DISABLED")) {
                    communicationsON = false;
                }

                communication = communicationsON;

                board = new Board(N_SQUARES, N_SQUARES, currentArchitecture, communication);
                initBoard();
                board.start(nRobots, nCollectors, nPowerups, nGarbage);
                System.out.println("Communications " + option);
            }
        });

        this.add(communicationBox);
        this.add(communicationLabel);


        //TIMER
        timeLabel = new JLabel("00:00.000");
        timeLabel.setLocation(WINDOW_X + 5, 500);
        timeLabel.setSize(140, 40);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        timeLabel.setHorizontalAlignment(JTextField.CENTER);
        timerListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                end = System.currentTimeMillis();
                elapsed = (end - last);
                temp += elapsed * board.getGameSpeed();

                timeLabel.setText(String.format("%02d:%02d.%03d",
                                TimeUnit.MILLISECONDS.toMinutes(temp),
                                TimeUnit.MILLISECONDS.toSeconds(temp) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(temp)),
                                (temp - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(temp))))
                );
                last = end;
            }
        };

        this.add(timeLabel);
        timer = new Timer(10, timerListener);
        timer.setInitialDelay(0);


        //Game Over Button
        gameOver = new JLabel("Game Over");
        gameOver.setLocation(WINDOW_X + 10, 570);
        gameOver.setSize(140, 40);
        gameOver.setHorizontalAlignment(JTextField.CENTER);
        gameOver.setFont(new Font("SansSerif", Font.BOLD, 19));
        gameOver.setOpaque(true);
        gameOver.setBackground(Color.red);
        gameOver.setVisible(false);
        this.add(gameOver);

        redWins = new JLabel("Red Wins");
        redWins.setLocation(WINDOW_X + 10, 610);
        redWins.setSize(140, 40);
        redWins.setHorizontalAlignment(JTextField.CENTER);
        redWins.setFont(new Font("SansSerif", Font.BOLD, 19));
        redWins.setForeground(Color.RED);
        redWins.setVisible(false);

        blueWins = new JLabel("Blue Wins");
        blueWins.setLocation(WINDOW_X + 10, 610);
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

    public static int getSQUARE_SIZE() {
        return SQUARE_SIZE;
    }

    public Board getBoard() {
        return board;
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

    class InterfaceRepaintThread extends Thread {

        private BoardPanel p;

        public InterfaceRepaintThread(BoardPanel _p) {
            p = _p;
        }

        public void run() {

            while (true) {
                try {
                    this.sleep(DELAY);
                    p.repaint();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}