package com.virusvector;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class HomeScreen extends JPanel implements ActionListener {
    private static class BurstParticle {
        float x, y, vx, vy, alpha;
        Color color;
        public BurstParticle(float x, float y, float vx, float vy, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.alpha = 1.0f; this.color = color;
        }
    }
    private final java.util.List<BurstParticle> burstParticles = new ArrayList<>();
    private long lastBurstTime = 0;

    private final Rectangle startButton;
    private final Timer animationTimer;
    @SuppressWarnings("unused") // Used in mouseClicked handler
    private final Runnable onStartGame;
    private float hue = 0f;
    private int animationOffset = 0;
    private final Random random = new Random();
    private final int[][] virusPixels = new int[10][10];

    public HomeScreen(Runnable onStartGame) {
        this.onStartGame = onStartGame;
        
        // Initialize virus pixels (simple 10x10 pixel art)
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                virusPixels[y][x] = random.nextInt(3); // 0=empty, 1=dark, 2=light
            }
        }
        
        // Initialize start button
        startButton = new Rectangle(300, 450, 200, 50);
        
        // Set up animation timer
        animationTimer = new Timer(33, this); // ~30 FPS
        animationTimer.start();
        
        // Enable mouse motion for hover effects
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (startButton.contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
        // Handle mouse clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (startButton.contains(e.getPoint())) {
                    animationTimer.stop();
                    if (onStartGame != null) {
                        onStartGame.run();
                    }
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }
        });
        
        // Request focus for key events
        setFocusable(true);
        requestFocusInWindow();
    }
    


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == animationTimer) {
            // Update animation
            hue = (hue + 0.005f) % 1.0f;
            animationOffset = (animationOffset + 1) % 10;
            // Pixel burst: spawn every 30 frames (~1s)
            long now = System.currentTimeMillis();
            if (now - lastBurstTime > 1000) {
                spawnBurstParticles();
                lastBurstTime = now;
            }
            updateBurstParticles();
            repaint();
        }
    }

    /**
     * Cleans up resources when this panel is no longer in use.
     */
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        removeAll();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw animated background
        drawAnimatedBackground(g2d);
        
        // Draw title with glow effect
        drawTitle(g2d);
        
        // Draw virus animation
        drawVirusAnimation(g2d);
        // Draw pixel burst particles
        drawBurstParticles(g2d);
        // Draw start button with hover effect
        drawStartButton(g2d);
    }
    
    private void drawAnimatedBackground(Graphics2D g2d) {
        // Draw grid background
        g2d.setColor(new Color(15, 15, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw animated grid lines
        g2d.setColor(new Color(30, 30, 60));
        for (int x = 0; x < getWidth(); x += 40) {
            int offset = (int)(Math.sin((x + animationOffset * 4) * 0.05) * 10);
            g2d.drawLine(x, 0, x + offset, getHeight());
        }
        
        // Draw some floating particles
        for (int i = 0; i < 50; i++) {
            int size = random.nextInt(3) + 1;
            int x = random.nextInt(getWidth());
            int y = (random.nextInt(5) * 120 + animationOffset * 2) % getHeight();
            float alpha = random.nextFloat() * 0.5f + 0.3f;
            g2d.setColor(new Color(100, 255, 100, (int)(alpha * 255)));
            g2d.fillOval(x, y, size, size);
        }
    }
    
    private void drawTitle(Graphics2D g2d) {
        String title = "VIRUS VECTOR";
        String subtitle = "INFECTION MAZE";
        
        // Draw main title with glow effect
        drawTextWithGlow(g2d, title, 400, 150, 48, new Color(0, 255, 0));
        
        // Draw subtitle
        g2d.setColor(new Color(200, 200, 200, 200));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
        int subWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, 400 - subWidth / 2, 200);
    }
    
    private void drawTextWithGlow(Graphics2D g2d, String text, int x, int y, int size, Color baseColor) {
        Font font = new Font("Monospaced", Font.BOLD, size);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        
        // Draw glow effect
        for (int i = 5; i > 0; i--) {
            float alpha = 0.1f * (6 - i);
            g2d.setColor(new Color(0, 255, 0, (int)(alpha * 40)));
            for (int dx = -i; dx <= i; dx++) {
                for (int dy = -i; dy <= i; dy++) {
                    g2d.drawString(text, x - textWidth/2 + dx, y + dy);
                }
            }
        }
        
        // Draw main text
        g2d.setColor(baseColor);
        g2d.drawString(text, x - textWidth/2, y);
    }
    
    private void drawVirusAnimation(Graphics2D g2d) {
        int centerX = 400;
        int centerY = 300;
        int size = 100;
        
        // Draw rotating virus particles
        int particles = 8;
        for (int i = 0; i < particles; i++) {
            double angle = Math.toRadians(i * (360.0 / particles) + animationOffset * 5);
            int x = centerX + (int)(Math.cos(angle) * 120);
            int y = centerY + (int)(Math.sin(angle) * 60);
            
            // Draw virus particle
            g2d.setColor(Color.getHSBColor(hue + (float)i/particles, 0.8f, 1.0f));
            g2d.fillOval(x - size/4, y - size/4, size/2, size/2);
            
            // Draw connecting lines
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(new Color(0, 255, 0, 100));
            g2d.drawLine(centerX, centerY, x, y);
        }
        
        // Draw main virus in the center
        int virusSize = size + 20;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (virusPixels[i][j] > 0) {
                    int px = centerX - virusSize/2 + j * (virusSize/10);
                    int py = centerY - virusSize/2 + i * (virusSize/10);
                    Color c = virusPixels[i][j] == 1 ? new Color(0, 255, 0) : new Color(200, 255, 200);
                    g2d.setColor(c);
                    g2d.fillRect(px, py, virusSize/10, virusSize/10);
                }
            }
        }
    }
    
    private void drawStartButton(Graphics2D g2d) {
        // Button glow effect
        for (int i = 5; i > 0; i--) {
            int alpha = 100 - i * 15;
            g2d.setColor(new Color(255, 50, 50, alpha));
            g2d.fillRoundRect(
                startButton.x - i, 
                startButton.y - i, 
                startButton.width + i*2, 
                startButton.height + i*2,
                30, 30
            );
        }
        
        // Button background - safely handle null mouse position
        Point mousePos = getMousePosition();
        boolean isHover = mousePos != null && startButton.contains(mousePos);
        GradientPaint gradient = new GradientPaint(
            startButton.x, startButton.y, 
            isHover ? new Color(255, 80, 80) : new Color(200, 0, 0),
            startButton.x, startButton.y + startButton.height,
            isHover ? new Color(180, 0, 0) : new Color(150, 0, 0)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(
            startButton.x, startButton.y, 
            startButton.width, startButton.height,
            20, 20
        );
        
        // Button border
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(
            startButton.x, startButton.y, 
            startButton.width, startButton.height,
            20, 20
        );
        
        // Button text
        String text = "START INFECTION";
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        
        // Text shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(text, 
            startButton.x + (startButton.width - textWidth) / 2 + 2, 
            startButton.y + startButton.height / 2 + fm.getAscent() / 2 - 1 + 2);
            
        // Main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, 
            startButton.x + (startButton.width - textWidth) / 2, 
            startButton.y + startButton.height / 2 + fm.getAscent() / 2 - 1);
    }

    // --- Pixel burst animation ---
    private void spawnBurstParticles() {
        int centerX = 400;
        int centerY = 300;
        int virusSize = 120;
        int px = centerX;
        int py = centerY;
        for (int i = 0; i < 6; i++) {
            double angle = Math.random() * Math.PI * 2;
            float speed = 2.0f + (float)Math.random() * 1.5f;
            float vx = (float)(Math.cos(angle) * speed);
            float vy = (float)(Math.sin(angle) * speed + 1.5); // Add downward bias
            Color c = Math.random() > 0.5 ? new Color(0,255,0) : new Color(200,255,200);
            burstParticles.add(new BurstParticle(px, py, vx, vy, c));
        }
    }

    private void updateBurstParticles() {
        Iterator<BurstParticle> it = burstParticles.iterator();
        while (it.hasNext()) {
            BurstParticle p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.alpha -= 0.025f + Math.random()*0.01f;
            if (p.alpha <= 0f) it.remove();
        }
    }

    private void drawBurstParticles(Graphics2D g2d) {
        for (BurstParticle p : burstParticles) {
            int size = 8;
            g2d.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int)(p.alpha*255)));
            g2d.fillRect((int)p.x - size/2, (int)p.y - size/2, size, size);
        }
    }
}
