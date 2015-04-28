package almeidas.domain;

import almeidas.domain.agents.Collector;
import almeidas.domain.agents.Snitch;
import almeidas.domain.agents.Sweeper;
import almeidas.domain.agents.VaccumCleaner;
import almeidas.graficos.BoardPanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board {

    Tile[][] board;

    private int width, height;
    private int totalGarbage;
    private int scoreBlueTeam, scoreRedTeam;
    private int garbageToSweep = 0;
    private int garbageToCollect = 0;
    private int garbageDelivered = 0;
    private int collectorsAlive = 0;
    private int blueCollectorsAlive = 0;
    private int redCollectorsAlive = 0;
    private int redTeamMembers;
    private int blueTeamMembers;
    private int gameSpeed = 3;
    private boolean started;
    private boolean snitchAlive = true;
    private static final int SNITCH_ID = -1;
    private Graphics2D g2d;

    private static BufferedImage sweeper_red;
    private static BufferedImage sweeper_blue;
    private static BufferedImage collector_red;
    private static BufferedImage collector_blue;
    private static BufferedImage strengthup;
    private static BufferedImage speedup;
    private static BufferedImage energyup;
    private static BufferedImage resistanceup;
    private static BufferedImage snitch;

    //private static final int gameSpeed = 1;

    private boolean over;

    public Board(int _width, int _height) {
        over = false;
        started = false;
        width = _width;
        height = _height;
        scoreBlueTeam = scoreRedTeam = 0;

        board = new Tile[width][height];

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            sweeper_blue = ImageIO.read(classLoader.getResource("resources/images/sweeper_blue.png"));
            sweeper_red = ImageIO.read(classLoader.getResource("resources/images/sweeper_red.png"));
            collector_blue = ImageIO.read(classLoader.getResource("resources/images/collector_blue.png"));
            collector_red = ImageIO.read(classLoader.getResource("resources/images/collector_red.png"));
            speedup = ImageIO.read(classLoader.getResource("resources/images/speedup.png"));
            energyup = ImageIO.read(classLoader.getResource("resources/images/energyup.png"));
            resistanceup = ImageIO.read(classLoader.getResource("resources/images/resistanceup.png"));
            strengthup = ImageIO.read(classLoader.getResource("resources/images/strengthup.png"));
            snitch = ImageIO.read(classLoader.getResource("resources/images/snitch.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.clear();
    }

    public int getGameSpeed(){
        return gameSpeed;
    }

    public void setGameSpeed(int _gameSpeed){
        gameSpeed = _gameSpeed;
    }

    public void setStarted(boolean state){
        started = state;
    }

    public void setOver(boolean state){
        over = state;
    }

    public void startAll(){
        ArrayList<VaccumCleaner> _vcArray = new ArrayList<VaccumCleaner>();
        setStarted(true);
        String agentType;
        ExecutorService executor = Executors.newCachedThreadPool();
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < board[i][j].agents.size(); k++) {
                    //VaccumCleaner v : board[i][j].agents
                    VaccumCleaner v = board[i][j].agents.get(k);
                    if(v.getId() >= 300) agentType = "Collector";
                    else if(v.getId() == SNITCH_ID) agentType = "Snitch";
                    else agentType = "Sweeper";
                    System.out.println("THREAD START: Agent ID = " + v.getId() + " of type: " + agentType);

                    _vcArray.add(v);
                }
            }
        }
        for(int j = 0; j<_vcArray.size(); j++){
            executor.execute(_vcArray.get(j));
        }
    }

    public void pauseAll(){
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (VaccumCleaner v : board[i][j].agents) {

                    System.out.println("THREAD PAUSE: Agent ID = " + v.getId());
                    v.stop();
                }
            }
        }
    }

    public void killAll(){
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (Entity e : board[i][j].agents) {
                    VaccumCleaner v  = (VaccumCleaner)e;
                    System.out.println("THREAD STOP: Agent ID = " + v.getId() + " from " + (v.getTeam() != null ? v.getTeam().toString() : "Snitch"));
                    v.stop();
                }
            }
        }
    }

    public void displayGameOver(){
        System.out.println("GAME OVER, THE WINNER IS: " + getWinner());
        almeidas.graficos.BoardPanel.gameOver.setVisible(true);
        if(getWinner().equals("Red Team")) {
            almeidas.graficos.BoardPanel.redWins.setVisible(true);
        }
        else {
            almeidas.graficos.BoardPanel.blueWins.setVisible(true);
        }
    }

    public String getWinner(){
        String result = "No Winner, it's a Tie!";

        if(collectorsAlive == 0){
            if((redTeamMembers == 0) && (blueTeamMembers > 0)) result = "Blue Team";
            else if((blueTeamMembers == 0) && (redTeamMembers > 0)) result = "Red Team";
        }

        else if((redTeamMembers == 0) && (blueTeamMembers > 0)) result = "Blue Team";
        else if((blueTeamMembers == 0) && (redTeamMembers > 0)) result = "Red Team";
        else if(scoreBlueTeam > scoreRedTeam) result = "Blue Team";
        else if(scoreBlueTeam < scoreRedTeam) result = "Red Team";
        else result = "No Winner, it's a Tie!";

        return result;
    }

    public int getTeamMembersLeft(VaccumCleaner.Team team){
        int membersLeft;

        if(team == (VaccumCleaner.Team.RED_TEAM)) membersLeft = redTeamMembers;
        else membersLeft = blueTeamMembers;

        return membersLeft;
    }

    public boolean isOver(){
        boolean o = false;

        if(started){
            if(over){ System.out.println("GAME OVER -> Motif: Team anihilated"); o = true; } //One of the teams has no members left
            else if(!snitchAlive && (garbageDelivered >= (totalGarbage * (5/4)))){ System.out.println("GAME OVER -> Motif: No garbage left (Snitch captured)"); o = true; }
            else if ((blueCollectorsAlive == 0) && (scoreBlueTeam < scoreRedTeam)){ System.out.println("GAME OVER -> Motif: Red Team is up, no Blue collectors left"); o = true; }
            else if ((redCollectorsAlive == 0) && (scoreRedTeam < scoreBlueTeam)){ System.out.println("GAME OVER -> Motif: Blue Team is up, no Red collectors left"); o = true; }
            else if (collectorsAlive == 0){ System.out.println("GAME OVER -> Motif: No collectors are alive anymore"); o = true; }
        }

        return o;
    }

    public void addAgent(VaccumCleaner agent, int x, int y) {
        board[x][y].agents.add(agent);
    }

    public void clear() {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                board[i][j] = new Tile(i, j, this);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Board perceptions
    public synchronized int[] perceptSnitch(int x, int y){
        VaccumCleaner snitch = null;

        for(int i = -1; i < 1; i++) {
            for (int j = -1; j < 1; j++) {
                if(canDo(x, y, i, j)) {
                    if (board[x+i][y+j].agents.size() > 0) {
                        for (int k = 0; k < board[x+i][y+j].agents.size(); k++) {
                            VaccumCleaner v = board[x+i][y+j].agents.get(k);
                            if (v.getId() == SNITCH_ID) {
                                snitch = v;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if(snitch != null){
            int pos[] = {snitch.getX(), snitch.getY()};
            return pos;
        }
        return null;
    }

    public synchronized void grabSnitch(VaccumCleaner vc, int[] snitchPos){
        VaccumCleaner snitch = null;
        ArrayList<VaccumCleaner> vcArray = board[snitchPos[0]][snitchPos[1]].getAgents();

        if(vcArray.size() > 0){
            for(int i=0; i< vcArray.size(); i++){
                if(vcArray.get(i).getId() == SNITCH_ID){
                    snitch = vcArray.get(i);
                }
            }
        }

        snitch.setDead(true);
        snitchAlive = false;

        boolean remove = vcArray.remove(snitch);
        System.out.println("THREAD STOP: Agent ID = " + snitch.getId() + " (" + (snitch.getTeam() != null ? (snitch.getTeam().toString() + ")") : "Snitch)"));
        snitch.stop();

        if(vc.getTeam() == VaccumCleaner.Team.BLUE_TEAM){
            addScoreBlueTeam((int)Math.round(totalGarbage/4));
        }
        else if(vc.getTeam() == VaccumCleaner.Team.RED_TEAM){
            addScoreRedTeam((int) Math.round(totalGarbage / 4));
        }
    }

    public synchronized boolean perceptGarbage(int x, int y){
        return board[x][y].getGarbage() > 0;
    }

    public synchronized boolean perceptSweptGarbage(int x, int y){
        return board[x][y].getSweptGarbage() > 0;
    }

    public synchronized boolean perceptBase(int x, int y, VaccumCleaner.Team team){
        if(team == VaccumCleaner.Team.BLUE_TEAM){
            return checkBlueBase(x, y);
        } else {
            return checkRedBase(x, y);
        }
    }
    
    

    public boolean checkBlueBase(int x, int y) {
        return ((x == 0 && y == 0) ||
                (x == 0 && y == 1) ||
                (x == 1 && y == 0) ||
                (x == 1 && y == 1) );
    }

    public boolean checkRedBase(int x, int y) {
        return ((x == (width-1) && y == (height-1)) ||
                (x == (width-1) && y == (height-2)) ||
                (x == (width-2) && y == (height-1)) ||
                (x == (width-2) && y == (height-2)) );
    }


    public synchronized int[] perceptEnemy(int x, int y, VaccumCleaner.Team team){
        VaccumCleaner enemy = null;

        for(int i = -1; i < 1; i++) {
            for (int j = -1; j < 1; j++) {
                if(canDo(x, y, i, j)) {
                    if (board[x+i][y+j].agents.size() > 0) {
                        for (int k = 0; k < board[x+i][y+j].agents.size(); k++) {
                                //VaccumCleaner v : board[x+i][y+j].agents) {
                            VaccumCleaner v = board[x+i][y+j].agents.get(k);
                            if (v.getTeam() != team && v.getTeam() != null) {
                                enemy = v;
                                break; //Returns first enemy found (if there is more than one, the one on the northwest corner is returned first
                            }
                        }
                    }
                }
            }
        }

        if(enemy != null){
            int pos[] = {enemy.getX(), enemy.getY()};
            return pos;
        }
        return null;
    }

    public boolean canDo(int x, int y, int incX, int incY){
        int minX = 0;
        int minY = 0;
        int maxX = getWidth();
        int maxY = getHeight();
        boolean doValue = true;

        if(((x + incX) > maxX) || ((x + incX) < minX) || ((y + incY) > maxY) || ((y + incY) < minY)) doValue = false;

        return doValue;
    }

    public synchronized boolean perceptPowerUp(int x, int y){
        return board[x][y].getPowerUp() != null;
    }

    // Board actions
    public synchronized void sweep(int x, int y){
        if(board[x][y].getGarbage() > 0){
            garbageToCollect ++;
            garbageToSweep --;
            board[x][y].setGarbage(board[x][y].getGarbage()-1);
            board[x][y].setSweptGarbage(board[x][y].getSweptGarbage()+1);
        }
    }

    public synchronized int grabGarbage(int quantity, int x, int y){
        if(board[x][y].getSweptGarbage() > quantity){
            board[x][y].setSweptGarbage(board[x][y].getSweptGarbage() - quantity);
            return quantity;
        }
        int garbageGrabbed = board[x][y].getSweptGarbage();
        board[x][y].setSweptGarbage(0);
        return garbageGrabbed;
    }

    public boolean isOccupied(int x, int y){
        return board[x][y].agents.size()>0;
    }

    public synchronized PowerUp grabPowerUp(int x, int y){
        PowerUp powerUp = board[x][y].getPowerUp();
        board[x][y].setPowerUp(null);
        return powerUp;
    }

    public synchronized void attackTile(VaccumCleaner.Team team, float damage, int x, int y, VaccumCleaner vc){
        if(board[x][y].agents.size() > 0){
            for(VaccumCleaner v : board[x][y].agents){
                if(v.getTeam() != team && v.getTeam() != null){
                    v.takeDamage(damage);
                    break;
                }
            }
        }
    }

    public synchronized void move(int fromX, int fromY, int toX, int toY, VaccumCleaner vc){

        board[fromX][fromY].agents.remove(vc);
        board[toX][toY].agents.add(vc);
    }

    public synchronized void destroyAgent(VaccumCleaner vc) {

        if (vc instanceof Collector) {
            collectorsAlive--;
            if (vc.getTeam() == VaccumCleaner.Team.BLUE_TEAM) blueCollectorsAlive--;
            else if (vc.getTeam() == VaccumCleaner.Team.RED_TEAM) redCollectorsAlive--;
        }

        int x = vc.getX();
        int y = vc.getY();

        if(board[x][y].agents.size() > 0) {
            boolean removed = board[x][y].agents.remove(vc);
            System.out.println("THREAD STOP: Agent ID = " + vc.getId() + " from " + (vc.getTeam() != null ? vc.getTeam().toString() : "Snitch"));
            vc.stop();

            if (!vc.getDead()) {
                if (vc.getTeam() == VaccumCleaner.Team.BLUE_TEAM) blueTeamMembers--;
                else if (vc.getTeam() == VaccumCleaner.Team.RED_TEAM) redTeamMembers--;
                int members = getTeamMembersLeft(vc.getTeam());
                System.out.println("DEATH: " + vc.getTeam().toString() + ", Agent ID = " + vc.getId() + " position = " + vc.getX() + "," + vc.getY() + " team members left = " + members);
                vc.setDead(true);
                if (members == 0) setOver(true);
                if (isOver()){
                    killAll();
                    displayGameOver();
                }
            }
        }
    }

    public void addScoreBlueTeam(int increment){
        scoreBlueTeam += increment;
        garbageDelivered += increment;
        board[0][0].setValue(increment);
        if (isOver()){
            killAll();
            displayGameOver();
        }
    }

    public void addScoreRedTeam(int increment){
        scoreRedTeam += increment;
        garbageDelivered += increment;
        board[width-1][height-1].setValue(increment);
        if (isOver()){
            killAll();
            displayGameOver();
        }
    }

    public void start(int numberRobots, int numCollectors, int numPowerups, int numGarbage) {
        totalGarbage = numGarbage;
        collectorsAlive = numCollectors * 2;
        blueCollectorsAlive = redCollectorsAlive = numCollectors;
        redTeamMembers = blueTeamMembers = numberRobots;
        over = false;
        //BLUE TEAM
        int robotsInserted = 0;
        int id_S_blue = 100;
        int id_S_red = 200;
        int id_C_blue  = 300;
        int id_C_red = 400;

        for( int k = 0 ; k < width && robotsInserted < numberRobots; k++ ) {
            for( int j = 0 ; j <= k && robotsInserted < numberRobots; j++ ) {
                int i = k - j;
                if (robotsInserted < numCollectors) {
                    addAgent(new Collector(i, j, VaccumCleaner.Team.BLUE_TEAM, collector_blue, this, id_C_blue), i, j);
                    id_C_blue++;
                }else{
                    addAgent(new Sweeper(i, j, VaccumCleaner.Team.BLUE_TEAM, sweeper_blue, this, id_S_blue), i, j);
                    id_S_blue++;
                }

                robotsInserted++;
            }
        }

        //RED TEAM
        robotsInserted = 0;
        for( int k = width - 1 ; k > 0 && robotsInserted < numberRobots; k-- ) {
            for( int j = width - 1; j > k - 1 && robotsInserted < numberRobots; j-- ) {
                int i = k - j + width - 1;
                if (robotsInserted < numCollectors) {
                    addAgent(new Collector(i, j, VaccumCleaner.Team.RED_TEAM, collector_red, this, id_C_red), i, j);
                    id_C_red++;
                }else{
                    addAgent(new Sweeper(i, j, VaccumCleaner.Team.RED_TEAM, sweeper_red, this, id_S_red), i, j);
                    id_S_red++;
                }
                robotsInserted++;
            }
        }

        // Insert garbage
        garbageToSweep = numGarbage;
        while(numGarbage > 0) {
            boolean gotGoodPosition = false;
            int x = 0, y = 0;
            Random rnd = new Random();
            while(!gotGoodPosition) {
                x = rnd.nextInt(width);
                y = rnd.nextInt(height);
                gotGoodPosition = board[x][y].agents.size() == 0;
            }
            int garbageToDispose = rnd.nextInt(10);
            garbageToDispose = (garbageToDispose > numGarbage) ? numGarbage: garbageToDispose;
            board[x][y].setGarbage(garbageToDispose);
            numGarbage -= garbageToDispose;
        }

        // Insert powerups
        for(int i = 0; i < numPowerups; i++) {

            boolean gotGoodPosition = false;
            int x = 0, y = 0;
            Random rnd = new Random();
            while(!gotGoodPosition) {
                x = rnd.nextInt(width);
                y = rnd.nextInt(height);
                gotGoodPosition = board[x][y].agents.size() == 0 && board[x][y].getPowerUp() == null;
            }

            PowerUp.Type type = PowerUp.Type.values()[rnd.nextInt(PowerUp.Type.values().length)];

            BufferedImage sprite = null;

            switch(type) {
                case SPEED_UP:
                    sprite = speedup;
                    break;
                case STRENGTH_UP:
                    sprite = strengthup;
                    break;
                case RESISTANCE_UP:
                    sprite = resistanceup;
                    break;
                case ENERGY_UP:
                    sprite = energyup;
                    break;
            }

            board[x][y].setPowerUp(new PowerUp(10, type, x, y, sprite));

        }

        //Inserting snitch
        boolean gotGoodPosition = false;
        int x = 0, y = 0;
        Random rnd = new Random();
        while(!gotGoodPosition) {
            x = rnd.nextInt(width);
            y = rnd.nextInt(height);
            gotGoodPosition = board[x][y].agents.size() == 0 && board[x][y].getPowerUp() == null;
        }
        addAgent(new Snitch(x, y, this, snitch), x, y);
    }

    public void render(Graphics2D g) {
        g2d = g;
        renderBoard(g);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                for(int k = 0; k < board[i][j].agents.size(); k++){
                        //Entity e : board[i][j].agents) {
                    Entity e = board[i][j].agents.get(k);
                    e.render(g);
                }
                if(board[i][j].getPowerUp() != null){
                    board[i][j].getPowerUp().render(g);
                }
                board[i][j].render(g);
            }
        }
    }

    public void renderBoard(Graphics2D g) {

        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);

        for (int n = 0; n < almeidas.graficos.BoardPanel.N_SQUARES+1; n += 1) {
            g.drawLine(n*almeidas.graficos.BoardPanel.SQUARE_SIZE, 0, n*almeidas.graficos.BoardPanel.SQUARE_SIZE, almeidas.graficos.BoardPanel.WINDOW_Y);
        }

        for (int n = 0; n < almeidas.graficos.BoardPanel.N_SQUARES+1; n += 1) {
            g.drawLine(0, n*almeidas.graficos.BoardPanel.SQUARE_SIZE, almeidas.graficos.BoardPanel.WINDOW_X, n*almeidas.graficos.BoardPanel.SQUARE_SIZE);
        }

        // Rendering garbage in each tile
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                int garbage = board[x][y].getGarbage();
                int sweptGarbage = board[x][y].getSweptGarbage();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setFont(new Font("default", Font.BOLD, 15));
                g.setColor(Color.BLACK);
                if(garbage > 0) {
                    g.drawString("" + garbage, x*almeidas.graficos.BoardPanel.SQUARE_SIZE+2, y*almeidas.graficos.BoardPanel.SQUARE_SIZE + 13);
                }
                if(sweptGarbage > 0){
                    g.setColor(Color.GREEN);
                    g.drawString("" + sweptGarbage, x*almeidas.graficos.BoardPanel.SQUARE_SIZE+40, y*almeidas.graficos.BoardPanel.SQUARE_SIZE + 13);
                }
            }
        }
    }
}