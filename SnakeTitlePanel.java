import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SnakeTitlePanel extends JPanel {
    private Timer colorTimer;
    private int colorIndex;
    private Color[] colors = {new Color(255, 216, 143), new Color(232, 190, 98), new Color(193, 154, 107)};

    SnakeTitlePanel() {
        animateBackground();
    }

    private void animateBackground() {
        colorTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBackground(colors[colorIndex]);
                colorIndex = (colorIndex + 1) % colors.length;
            }
        });

        colorTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Arial", Font.BOLD, 50);
        g.setFont(font);
        g.setColor(Color.BLACK);

        String title = "SNAKE GAME";
        int x = getWidth() / 2 - g.getFontMetrics().stringWidth(title) / 2;
        int y = 60;

        g.drawString(title, x, y);
    }
}