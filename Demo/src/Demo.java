import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

public class Demo extends JFrame implements KeyListener, ActionListener {

    final int screenWidth = 1280;
    final int screenHeight = 720;

    private Image offScreenImage;
    private Graphics gImage;

    final int initX = screenWidth / 2;
    final int initY = screenHeight / 2;

    final int PadWidth = 30;
    final int PadHeight = 200;
    final int PadOffset = 20;

    Timer ballTimer;

    int ballSpeedX = 6, ballSpeedY = 6;
    final int DELAY_MS = 10;
    int ballX = initX;
    int ballY = initY;
    final int ballRadius = 80;

    int playerSpeed = 30;
    int[] playerX = new int[2];
    int[] playerY = new int[2];
    int[] playerScore = new int[2];

    int scene = 1;
    private Image background;
    private BufferedImage ball;
    private Image logo;
    private Image start;
    private PlayBackThread begin;
    int degree = 0;

    java.util.Timer timer = new java.util.Timer(true);

    public Demo() {
        setTitle("Demo");
        setSize(screenWidth, screenHeight);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        addKeyListener(this);

        catchImg();
        begin = new PlayBackThread("begin.wav");

        ballTimer = new Timer(DELAY_MS, this);
        ballTimer.setInitialDelay(10);
        ballTimer.start();

        setVisible(true);
    }

    private void initGame() {
        for (int i = 0; i < 2; i++) {
            playerY[i] = initY;
        }

        playerX[0] = PadWidth - PadOffset;
        playerX[1] = screenWidth - PadWidth - PadOffset;

        for (int i = 0; i < 2; i++) {
            playerScore[i] = 0;
        }

        ballX = initX;
        ballY = initY;

    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = this.createImage(screenWidth, screenHeight);
            gImage = offScreenImage.getGraphics();
        }
        gImage.setColor(getBackground());
        gImage.fillRect(0, 0, (int) this.getSize().getWidth(), (int) this.getSize().getHeight());
        drawBackground();
        if (scene == 1) {
            drawLogo();
        } else if (scene == 2) {
            drawPlayerPad();
            drawBall();
        }
        g.drawImage(offScreenImage, 0, 0, null);
        degree += 6;
        degree %= 360;
    }

    private void drawPlayerPad() {
        gImage.setColor(Color.RED);
        gImage.fillRect(playerX[0], playerY[0], PadWidth, PadHeight);
        gImage.setColor(Color.BLUE);
        gImage.fillRect(playerX[1], playerY[1], PadWidth, PadHeight);
    }

    private void drawBall() {
        double rotationRequired = Math.toRadians(degree);
        double locationX = ball.getWidth(null) / 2;
        double locationY = ball.getHeight(null) / 2;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        gImage.drawImage(op.filter(ball, null), ballX - (ball.getWidth(null) / 2), ballY - (ball.getHeight(null) / 2), null);
    }

    private void drawBackground() {
        gImage.drawImage(background, 0, 0, screenWidth, screenHeight, null);
    }

    private void drawLogo() {
        gImage.drawImage(logo, 0, 0, screenWidth, screenHeight, null);
        gImage.drawImage(start, 0, 0, screenWidth, screenHeight, null);
    }

    private void catchImg() {
        try {
            background = ImageIO.read(new File("background.jpeg"));
            ball = ImageIO.read(new File("ball.png"));
            logo = ImageIO.read(new File("logo.png"));
            start = ImageIO.read(new File("start.png"));
        } catch (IOException ex) {
        }
    }

    TimerTask loopStart = new TimerTask() {
        public void run() {
            PlayBackThread loop = new PlayBackThread("loop.wav");
            loop.start();
        }
    };
    TimerTask init = new TimerTask() {
        public void run() {
            scene = 2;
            initGame();
        }
    };

    public static void main(String[] args) {
        new Demo();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Move the player on the left
        if (key == KeyEvent.VK_UP)
            playerY[1] -= playerSpeed;

        if (key == KeyEvent.VK_DOWN)
            playerY[1] += playerSpeed;

        // Move the player on the right
        if (key == KeyEvent.VK_W)
            playerY[0] -= playerSpeed;

        if (key == KeyEvent.VK_X)
            playerY[0] += playerSpeed;

        if (key == KeyEvent.VK_SPACE) {
            begin.start();
            timer.schedule(init, 5700);
            timer.schedule(loopStart, 5700,95000);
        }

        checkPadPosRange();
        repaint();
    }

    private void checkPadPosRange() {
        for (int i = 0; i < 2; i++) {
            if (playerY[i] < 0) playerY[i] = 0;
            if (playerY[i] > screenHeight - PadHeight) playerY[i] = screenHeight - PadHeight;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballX >= screenWidth - ballRadius || ballX <= ballRadius) {
            ballSpeedX = -ballSpeedX;

            if (ballX <= 0) {
                playerScore[1]++;
            } else {
                playerScore[0]++;
            }
        }
        if (ballY >= screenHeight - ballRadius || ballY <= ballRadius) ballSpeedY = -ballSpeedY;

        if (ballX <= playerX[0] + PadWidth + ballRadius && ballX >= playerX[0] &&
                ballY <= playerY[0] + PadHeight + ballRadius && ballY >= playerY[0])
            ballSpeedX = -ballSpeedX;

        if (ballX <= playerX[1] - ballRadius + PadWidth && ballX >= playerX[1] - ballRadius &&
                ballY <= playerY[1] + PadHeight && ballY >= playerY[1])
            ballSpeedX = -ballSpeedX;

        this.repaint();
    }
}