import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class SnakeGame extends JFrame implements ActionListener, KeyListener {
    private static final int TILE_SIZE = 25;
    private static final int GRID_SIZE = 25;
    private static final int DEFAULT_GAME_SPEED = 150; // milliseconds
    private static int GAME_SPEED = 150; // milliseconds
    private Point speedBoost;  // Food power-up for speed boost
    private Point obstacle;    // Obstacle (bomb)

    private LinkedList<Point> snake;
    private Point food;
    private int direction; // 0: up, 1: right, 2: down, 3: left
    private int score;

    private Timer timer;
    private JLabel scoreLabel;

    private BackgroundMusicPlayer backgroundMusic;
    private boolean gamePaused = false;
    private JLabel pauseLabel;
    private JLabel resumeLabel;
    private BufferedImage buffer;
    

    SnakeGame() {
        backgroundMusic = new BackgroundMusicPlayer("pillarmentheme.wav");
        backgroundMusic.start();
        setTitle("Snake Game");
        setSize(TILE_SIZE * GRID_SIZE, TILE_SIZE * GRID_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // Make the frame not resizable
        loadAllScores();  // Load high score from file

        speedBoost = new Point(-1, -1);  // Initially not on the board
        obstacle = new Point(-1, -1);    // Initially not on the board

        snake = new LinkedList<>();
        snake.add(new Point(GRID_SIZE / 2, GRID_SIZE / 2));
        direction = 1; // start moving right

        generateFood();

        timer = new Timer(GAME_SPEED, this);
        timer.start();

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        scoreLabel.setForeground(Color.RED);
        add(scoreLabel, BorderLayout.NORTH);

        pauseLabel = new JLabel("Game Paused");
        pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pauseLabel.setForeground(Color.RED);
        pauseLabel.setFont(new Font("Arial", Font.BOLD, 30));
        add(pauseLabel, BorderLayout.CENTER);

        resumeLabel = new JLabel("Press 'Space' to resume");
        resumeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resumeLabel.setForeground(Color.BLACK);
        resumeLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        add(resumeLabel, BorderLayout.SOUTH);

        pauseLabel.setVisible(false); // Initially, the label is hidden
        resumeLabel.setVisible(false);

        addKeyListener(this);
        setFocusable(true);
        buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    private List<Integer> allScores;
    private void updateAllScores() {
        allScores.add(score);
        saveAllScores();  // Save all scores to file
    }

    private void loadAllScores() {
        allScores = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allScores.add(Integer.parseInt(line.trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAllScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            for (int s : allScores) {
                writer.write(String.valueOf(s));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  

    private void generateFood() {
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (snake.contains(new Point(x, y)));

        food = new Point(x, y);
    }

    private void generatePowerUps() {
        Random random = new Random();

        // Generate Speed Boost
        if (random.nextDouble() < 0.02) {  // Adjust probability as needed
            do {
                speedBoost.setLocation(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE));
            } while (snake.contains(speedBoost) || speedBoost.equals(food) || speedBoost.equals(obstacle));
        }

        // Generate Obstacle
        if (random.nextDouble() < 0.01) {  // Adjust probability as needed
            do {
                obstacle.setLocation(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE));
            } while (snake.contains(obstacle) || obstacle.equals(food) || obstacle.equals(speedBoost));
        }
    }

    private void move() {
        Point head = snake.getFirst();
        Point newHead;

        switch (direction) {
            case 0: // up
                newHead = new Point(head.x, (head.y - 1 + GRID_SIZE) % GRID_SIZE);
                break;
            case 1: // right
                newHead = new Point((head.x + 1) % GRID_SIZE, head.y);
                break;
            case 2: // down
                newHead = new Point(head.x, (head.y + 1) % GRID_SIZE);
                break;
            case 3: // left
                newHead = new Point((head.x - 1 + GRID_SIZE) % GRID_SIZE, head.y);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }

        if (newHead.equals(food)) {
            snake.addFirst(food);
            score++;
            BackgroundMusicPlayer("eatfood.wav");
            generateFood();
            updateScoreLabel();
        } else {
            snake.addFirst(newHead);
            snake.removeLast();
        }

        if (checkCollision()) {
            gameOver();
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    private void BackgroundMusicPlayer(String filePath) {
        backgroundMusic.playSoundEffect(filePath);
    }

    private void checkPowerUpCollision() {
        if (snake.getFirst().equals(speedBoost)) {
            // Speed Boost
            GAME_SPEED -= 10;  // Adjust speed increase as needed
            timer.setDelay(GAME_SPEED);
            speedBoost.setLocation(-1, -1);  // Remove the power-up
            BackgroundMusicPlayer("speedboost.wav");
        } else if (snake.getFirst().equals(obstacle)) {
            // Bomb - Game Over
            BackgroundMusicPlayer("bomb.wav");
            BackgroundMusicPlayer("gameover.wav");
            gameOver();
        }
    }

    private boolean checkCollision() {
        Point head = snake.getFirst();

        // Check if snake collides with itself
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                BackgroundMusicPlayer("gameover.wav");
                return true;
            }
        }

        // Check if snake collides with walls
        return head.x < 0 || head.x >= GRID_SIZE || head.y < 0 || head.y >= GRID_SIZE;
    }

    private void gameOver() {
        timer.stop();

        backgroundMusic.stop();
        backgroundMusic.close();

        updateAllScores();  // Update and save all scores

        // Display all previous scores
        

        // Create a non-dismissable dialog
        JDialog gameOverDialog = new JDialog(this, "Game Over", true);
        gameOverDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Create components for the dialog
        JLabel messageLabel = new JLabel("Game Over! Your score: " + score);
        JButton tryAgainButton = new JButton("Try Again");
        JButton backToMenuButton = new JButton("Back to Game Menu");
        JButton exitButton = new JButton("Exit");

        // Add components to the dialog
        gameOverDialog.setLayout(new BoxLayout(gameOverDialog.getContentPane(), BoxLayout.Y_AXIS));
        gameOverDialog.add(messageLabel);
        gameOverDialog.add(Box.createRigidArea(new Dimension(90, 10))); // Spacing
        gameOverDialog.add(tryAgainButton);
        gameOverDialog.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
        gameOverDialog.add(backToMenuButton);
        gameOverDialog.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
        gameOverDialog.add(exitButton);

        // Set action listeners for the buttons
        tryAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                gameOverDialog.dispose();
                restartGame();
            }
        });

        backToMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                backgroundMusic.stop(); // Stop Snake Game music
                //backgroundMusic.close(); // Close the Snake Game music player
                gameOverDialog.dispose();
                backToGameMenu();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                System.exit(0);
            }
        });

        // Set the size and center the dialog on the screen
        gameOverDialog.setSize(300, 200);
        gameOverDialog.setLocationRelativeTo(this);

        // Make the dialog visible
        gameOverDialog.setVisible(true);
    }

    private void restartGame() {
        // Reset power-up positions
        speedBoost.setLocation(-1, -1);
        obstacle.setLocation(-1, -1);
        backgroundMusic = new BackgroundMusicPlayer("pillarmentheme.wav");
        backgroundMusic.start();
        // Reset game state and start a new game
        snake.clear();
        snake.add(new Point(GRID_SIZE / 2, GRID_SIZE / 2));
        direction = 1;
        score = 0;
        generateFood();
        updateScoreLabel();
        GAME_SPEED = DEFAULT_GAME_SPEED;
        timer.setDelay(GAME_SPEED);
        timer.start();
        repaint();
    }

    private void backToGameMenu() {
        // Close the current game and show the game menu
        GAME_SPEED = DEFAULT_GAME_SPEED;
        timer.setDelay(GAME_SPEED);
        dispose();
        SwingUtilities.invokeLater(() -> new GameMenu().setVisible(true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        checkPowerUpCollision();  // Check for power-up collisions
        generatePowerUps();       // Generate power-ups
        //repaint();
        SwingUtilities.invokeLater(() -> repaint());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw food
        g.setColor(Color.RED);
        g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw speed boost
        g.setColor(Color.GREEN);
        g.fillRect(speedBoost.x * TILE_SIZE, speedBoost.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw obstacle
        g.setColor(Color.BLACK);
        g.fillRect(obstacle.x * TILE_SIZE, obstacle.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw snake
        for (int i = 0; i < snake.size(); i++) {
            g.setColor(getSnakeColor(i));
            g.fillRect(snake.get(i).x * TILE_SIZE, snake.get(i).y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        }
    }

    private Color getSnakeColor(int index) {
        if (index == 0) {
            return Color.BLACK;
        } else if (score >= 50) {
            return Color.ORANGE;
        } else if (score >= 20) {
            return new Color(128, 0, 128);
        } else if (score >= 10) {
            return Color.RED;
        } else if (score >= 5) {
            return Color.BLUE;
        } else {
            return Color.BLACK;
        }
    }

    private void togglePause() {
        if (gamePaused) {
            timer.start();
            pauseLabel.setVisible(false);
            resumeLabel.setVisible(false);
        } else {
            timer.stop();
            pauseLabel.setVisible(true);
            resumeLabel.setVisible(true);
        }
        gamePaused = !gamePaused;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_SPACE) {
            togglePause();
        }


        if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direction != 2) {
            direction = 0;
        } else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != 3) {
            direction = 1;
        } else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direction != 0) {
            direction = 2;
        } else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direction != 1) {
            direction = 3;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}