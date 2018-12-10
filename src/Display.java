/*
 * Author: Chuning (Tony) Zhu
 * Date: November 28, 2018
 * CIS 120 Final Project: How Far Can You Go?
 */

import javafx.util.Pair;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
class Display extends JPanel {

    private List<Object3D> blocks;
    private Player player;
    private Wall wall;

    private boolean paused = false;
    private boolean isAlive = true;
    private int level_count = 0;
    private int score = 0;

    private final Random rand = new Random();
    private final JLabel status;
    private final Timer timer;

    // Scoreboard buffer
    private static final File scoreBoardFile = new File("files/scoreBoard");
    private LinkedList<Pair<Integer, String>> scoreBuffer;

    // Display constants
    public static final int DISP_WIDTH = 800;
    public static final int DISP_HEIGHT = 600;
    private static final int MIN_BLOCK_WIDTH = 80;
    private static final int SHIFT = DISP_WIDTH / 2;

    // Update interval for timer, in milliseconds
    private static final int INTERVAL = 1000 / 60;
    private static final int PLAYER_SPEED = 4;

    private boolean isADown = false;
    private boolean isDDown = false;

    public Display(JLabel status) {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        timer = new Timer(INTERVAL, e -> tick());
        timer.start();

        this.status = status;
        setFocusable(true);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(!Object3D.isRotating() && !paused) {
                    switch(e.getKeyCode()) {
                        case (KeyEvent.VK_LEFT):
                            if (!player.isFalling()) {
                                Object3D.beginRotation(Direction.CCW);
                                Collections.sort(blocks);
                                for (Object3D bk : blocks) {
                                    bk.updateFinalPosition(Direction.CCW);
                                }
                                player.updateFinalPosition(Direction.CCW);
                                wall.updateFinalPosition(Direction.CCW);
                            } break;
                        case (KeyEvent.VK_RIGHT):
                            if (!player.isFalling()) {
                                Object3D.beginRotation(Direction.CW);
                                Collections.sort(blocks);
                                for (Object3D bk : blocks) {
                                    bk.updateFinalPosition(Direction.CW);
                                }
                                player.updateFinalPosition(Direction.CW);
                                wall.updateFinalPosition(Direction.CW);
                            } break;
                        case (KeyEvent.VK_A):
                            isADown = true;
                            break;
                        case (KeyEvent.VK_D):
                            isDDown = true;
                            break;
                        case (KeyEvent.VK_SPACE):
                            player.jump();
                            break;
                        case (KeyEvent.VK_P):
                            pause();
                            break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    isADown = false;
                } else if (e.getKeyCode() == KeyEvent.VK_D) {
                    isDDown = false;
                }
            }
        });
    }

    private void tick() {
        if (isAlive) {
            if (isADown == isDDown) {
                player.setVx(0);
            } else if (isADown) {
                player.setVx(-PLAYER_SPEED);
            } else {
                player.setVx(PLAYER_SPEED);
            }

            if (Object3D.isRotating()) {
                // Object rotation
                Object3D.rotate();
                synchronized (blocks) {
                    for (Object3D bk : blocks) {
                        bk.updateProjection();
                    }
                }
                player.updateProjection();
                wall.updateProjection();
            } else {
                // Player operation
                if (!player.move()) {
                    isAlive = false;
                }

                // Block operation using ParallelStream
                synchronized (blocks) {
                    blocks = blocks.parallelStream()
                            .filter(Object3D::move)
                            .collect(Collectors.toList());
                }

                // Collision detection
                if (player.isFalling()) {
                    player.tryCollideObjects(blocks);
                } else {
                    player.tryStayOn();
                }

                // Block generation
                updateBlocks();
            }
        } else {
            terminate();
        }

        status.setText("Score: " + score);
        repaint();
    }

    private void updateBlocks() {
        level_count -= Object3D.INIT_VEL_Y;
        if (level_count >= 80 && (blocks.size() < 2 || rand.nextInt(2) == 0)) {
            level_count = 0;
            int x1 = Wall.LEFT_BOUND + rand.nextInt(Wall.WALL_WIDTH - MIN_BLOCK_WIDTH);
            int x2 = x1 + MIN_BLOCK_WIDTH + rand.nextInt(Wall.RIGHT_BOUND - x1 - MIN_BLOCK_WIDTH);
            int y1 = Wall.LEFT_BOUND + rand.nextInt(Wall.WALL_WIDTH - MIN_BLOCK_WIDTH);
            int y2 = y1 + MIN_BLOCK_WIDTH + rand.nextInt(Wall.RIGHT_BOUND - y1 - MIN_BLOCK_WIDTH);

            blocks.add(new Block(x1 - SHIFT, y1 - SHIFT, x2 - SHIFT, y2 - SHIFT,
                       0, -2, 0, new Color(165,232,219)));
            score++;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(Object3D.isRotating()) {
            wall.drawBackEdge(g);
            for (Object3D bk : blocks) {
                bk.rDraw(g);
            }
            wall.rDraw(g);
            player.rDraw(g);
        } else {
            wall.draw(g);
            for (Object3D bk : blocks) {
                bk.draw(g);
            }
            player.draw(g);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DISP_WIDTH, DISP_HEIGHT);
    }

    public void init() {
        try {
            // Initializing the score buffer
            scoreBuffer = new LinkedList<>();
            if (!scoreBoardFile.createNewFile()) {
                BufferedReader fin = new BufferedReader(new FileReader(scoreBoardFile));

                String line = fin.readLine();
                while (line != null) {
                    int dash = line.indexOf("-");
                    if (dash < 0) {
                        line = fin.readLine();
                        continue;
                    }
                    scoreBuffer.add(new Pair<>(Integer.parseInt(line.substring(0, dash).trim()),
                            line.substring(dash + 1).trim()));
                    line = fin.readLine();
                }
                fin.close();
            }
            this.reset();
        } catch (IOException e){
            System.out.print("Caught IO Exception");
        }
    }

    private void terminate() {
        try {
            pause();

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            String name = JOptionPane.showInputDialog(frame, "Your score is: " +
                    score + "\nPlease enter your name:\n(alphanumeric and underscores)");

            while (!(name == null || Pattern.matches("[a-zA-Z0-9_]{1,20}", name))) {
                name = JOptionPane.showInputDialog(frame, "Invalid input! \nYour score is: " +
                        score + "\nPlease enter your name:\n(alphanumeric and underscores)");
            }

            BufferedWriter fout = new BufferedWriter(new FileWriter(scoreBoardFile, false));
            StringBuilder message = new StringBuilder();

            // Sorting given that stored names are already sorted
            if (!(name == null || name.equals(""))) {
                int index = 0;
                while (index < scoreBuffer.size() && scoreBuffer.get(index).getKey() > score){
                    index++;
                }
                scoreBuffer.add(index, new Pair<> (score, name));
            }

            while (scoreBuffer.size() > 15) {
                scoreBuffer.removeLast();
            }

            int count = 0;
            boolean indicated = false;
            for (Pair<Integer, String> i : scoreBuffer) {
                fout.write(i.getKey() + " - " + i.getValue() + "\n");
                if(!indicated && i.getKey() == score && i.getValue().equals(name)) {
                    message.append("> ");
                    indicated = true;
                }
                message.append(++count).append(". ")
                        .append(i.getValue())
                        .append(": ")
                        .append(i.getKey())
                        .append("\n");
            }

            JOptionPane.showMessageDialog(frame, message.toString(), "Scoreboard", JOptionPane.PLAIN_MESSAGE);
            fout.close();
            reset();

        } catch (IOException e) {
            System.out.print("Caught IO Exception");
        }
    }

    public void pause() {
        if (!paused) {
            timer.stop();
            paused = true;
        }
    }

    public void resume() {
        if (paused) {
            timer.start();
            paused = false;
            requestFocusInWindow();
        }
    }

    public void reset() {
        paused = false;
        isAlive = true;
        level_count = 0;
        score = 0;
        isADown = false;
        isDDown = false;

        Object3D.reset();
        player = new Player();
        wall = new Wall();

        Block b1 = new Block(-100, -100, 100, 100,
                0,-2, 0, new Color(165,232,219));
        blocks = new LinkedList<>();
        blocks.add(b1);

        timer.restart();
        requestFocusInWindow();
    }
}