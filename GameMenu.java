import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;

public class GameMenu extends JFrame {

    private BackgroundMusicPlayer backgroundMusic;
    

    public GameMenu() {
    
        backgroundMusic = new BackgroundMusicPlayer("gamemenu.wav");
        backgroundMusic.start();
        
        animateBackground();
        // Display all previous scores
        

        setTitle("Game Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null); // Center the frame on the screen
        setResizable(false); // Make the frame not resizable

        SnakeTitlePanel titlePanel = new SnakeTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // Use BoxLayout with Y_AXIS alignment to arrange components vertically
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JButton playButton = new JButton("Play");
        JButton aboutButton = new JButton("About");
        JButton displayScoresButton = new JButton("High Scores");
        JButton exitButton = new JButton("Exit");

        // Set preferred size for buttons
        Dimension buttonSize = new Dimension(150, 40);
        playButton.setPreferredSize(buttonSize);
        aboutButton.setPreferredSize(buttonSize);
        displayScoresButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        playButton.setContentAreaFilled(false); // Make the button background transparent
        playButton.setBorderPainted(false); // Hide the button border
        aboutButton.setContentAreaFilled(false); // Make the button background transparent
        aboutButton.setBorderPainted(false); // Hide the button border
        displayScoresButton.setContentAreaFilled(false);
        displayScoresButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false); // Make the button background transparent
        exitButton.setBorderPainted(false); // Hide the button border

        // Add glue to push buttons to the top of the frame
        add(Box.createVerticalGlue());
        
        // Add buttons with rigid areas for spacing
        add(Box.createRigidArea(new Dimension(90, 10))); // Spacing
        add(playButton);
        add(Box.createRigidArea(new Dimension(90, 10))); // Spacing
        add(aboutButton);
        add(Box.createRigidArea(new Dimension(90, 10))); // Spacing
        add(displayScoresButton);
        add(Box.createRigidArea(new Dimension(90, 10))); // Spacing
        add(exitButton);
        
        // Add glue to push buttons to the bottom of the frame
        add(Box.createVerticalGlue());

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                backgroundMusic.stop();
                
                // Start the SnakeGame when the "Play" button is clicked
                dispose(); // Close the menu
                SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
            }
        });

        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                String aboutMessage = "GROUP PROJECT in DATA STRUCTURE & ALGORITHM:\n\n" +
                              "GAMEPLAY:\n" +
                              "Use Keypads or WASD on your keyboard\n\n" +
                              "Snake color changes when it hit certain points\n\n" +
                              "Food: RED = food, GREEN = speed boost, BLACK = bomb\n\n" +
                              "Enter SPACE to pause/resume\n\n\n" +
                              "Constantino, Van Joseph\n" +
                              "Ungay, Jaeny Anne\n" +
                              "Dita, Erica Mae\n" +
                              "Agacoilli, Princess Mae";
                
                JOptionPane.showMessageDialog(GameMenu.this, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        displayScoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                displayAllScores(10); // Call the method to display scores
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backgroundMusic.playSoundEffect("clickgame.wav");
                int choice = JOptionPane.showConfirmDialog(GameMenu.this, "Are you sure you want to exit?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private void animateBackground() {
        Timer colorTimer = new Timer(1000, new ActionListener() {
            private int colorIndex = 0;
            private Color[] colors = {new Color(255, 216, 143), new Color(232, 190, 98), new Color(193, 154, 107)};

            @Override
            public void actionPerformed(ActionEvent e) {
                getContentPane().setBackground(colors[colorIndex]);
                colorIndex = (colorIndex + 1) % colors.length;
            }
        });

        colorTimer.start();
    }

    private void displayAllScores(int topCount) {
    try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
        List<Integer> scores = new ArrayList<>();
        String line;

        int highestScore = 0;

        while ((line = reader.readLine()) != null) {
            int score = Integer.parseInt(line.trim());
            scores.add(score);
            highestScore = Math.max(highestScore, score);
        }

        // Sort the scores in descending order
        scores.sort(Collections.reverseOrder());

        StringBuilder scoreInfo = new StringBuilder("Highest Score: " + highestScore + "\n\n");
        scoreInfo.append("Top ").append(topCount).append(" Scores:\n");

        // Display the top scores, up to the specified count or the available number of scores
        int count = Math.min(topCount, scores.size());
        for (int i = 0; i < count; i++) {
            scoreInfo.append(scores.get(i)).append("\n");
        }

        JOptionPane.showMessageDialog(this, scoreInfo.toString(), "Highest and Top Scores", JOptionPane.INFORMATION_MESSAGE);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
 
    

    public static void main(String[] args) {
        
        GameMenu gameMenu = new GameMenu();
        gameMenu.setVisible(true);

        gameMenu.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (gameMenu.backgroundMusic != null) {
                    gameMenu.backgroundMusic.stop();
                    gameMenu.backgroundMusic.close();
                }
                System.exit(0);
            }
        });

    }
}