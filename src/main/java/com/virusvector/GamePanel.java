package com.virusvector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int TILE_SIZE = 40;
    private static final int INFO_PANEL_HEIGHT = 80;
    private static final int ANIMATION_DELAY = 16; // ~60 FPS
    
    private final GameWindow window;
    private final Timer gameTimer;
    private GameState gameState;
    private boolean showMessage;
    private String message;
    private long messageEndTime;
    
    public GamePanel(GameWindow window) {
        this.window = window;
        this.gameState = new GameState();
        this.gameTimer = new Timer(ANIMATION_DELAY, this);
        
        setFocusable(true);
        setRequestFocusEnabled(true);
        addKeyListener(this);
        
        setupUI();
        startNewGame();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        
        // Game panel for the grid
        JPanel gameGridPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                renderGame((Graphics2D) g);
                
                // Draw player at current pixel position during movement
                if (isMoving) {
                    g.setColor(EntityType.PLAYER.getColor());
                    g.fillRect(currentPixelPos.x, currentPixelPos.y, TILE_SIZE, TILE_SIZE);
                }
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(
                    gameState.getGrid().length * TILE_SIZE,
                    gameState.getGrid()[0].length * TILE_SIZE
                );
            }
        };
        
        // Info panel at the bottom
        JPanel infoPanel = new JPanel(new GridLayout(1, 3));
        infoPanel.setPreferredSize(new Dimension(0, INFO_PANEL_HEIGHT));
        infoPanel.setBackground(new Color(30, 30, 40));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Score display
        JLabel scoreLabel = new JLabel("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        
        // Level and lives display
        JLabel levelLabel = new JLabel("Level: 1");
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Time and power-ups display
        JLabel statusLabel = new JLabel("Time: 120s | Lives: 10");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        infoPanel.add(scoreLabel);
        infoPanel.add(levelLabel);
        infoPanel.add(statusLabel);
        
        add(gameGridPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        
        // Update info panel periodically
        Timer infoUpdateTimer = new Timer(100, _ -> {
            scoreLabel.setText(String.format("Score: %,d", gameState.getScore()));
            levelLabel.setText(String.format("Level: %d", gameState.getLevel()));
            String shieldStatus = gameState.hasShield() ? "SHIELD" : "";
            String rangeStatus = gameState.getInfectionRange() > 1 ? "BOOST" : "";
            statusLabel.setText(String.format("Time: %ds | Lives: %d %s %s", 
                gameState.getTimeLeft(), 
                gameState.getLives(),
                shieldStatus,
                rangeStatus));
        });
        infoUpdateTimer.start();
    }
    
    private void startNewGame() {
        gameState = new GameState();
        gameTimer.start();
        showMessage("Infect 70% of the grid to win!");
        requestFocusInWindow();
    }
    
    private void showMessage(String msg) {
        this.message = msg;
        this.showMessage = true;
        this.messageEndTime = System.currentTimeMillis() + 3000; // 3 seconds
    }
    
    private void renderGame(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        EntityType[][] grid = gameState.getGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                EntityType type = grid[x][y];
                g2d.setColor(type.getColor());
                g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                
                // Draw grid lines
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                
                // Draw special effects
                if (type == EntityType.PLAYER) {
                    // Draw player glow
                    g2d.setColor(new Color(0, 255, 0, 100));
                    g2d.fillOval(
                        x * TILE_SIZE - 5, 
                        y * TILE_SIZE - 5, 
                        TILE_SIZE + 10, 
                        TILE_SIZE + 10
                    );
                }
            }
        }
        
        // Draw message if any
        if (showMessage && System.currentTimeMillis() < messageEndTime) {
            drawCenteredMessage(g2d, message);
        } else {
            showMessage = false;
        }
        
        // Draw game over or level complete message
        if (gameState.isGameOver()) {
            drawCenteredMessage(g2d, "GAME OVER - Press R to restart");
        } else if (gameState.isLevelComplete()) {
            drawCenteredMessage(g2d, "LEVEL COMPLETE! - Press N for next level");
        }
    }
    
    private void drawCenteredMessage(Graphics2D g2d, String msg) {
        Font font = new Font("Monospaced", Font.BOLD, 24);
        g2d.setFont(font);
        
        // Draw shadow
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        
        g2d.drawString(msg, x + 2, y + 2);
        
        // Draw main text
        g2d.setColor(Color.WHITE);
        g2d.drawString(msg, x, y);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == gameTimer) {
            gameState.update();
            
            // Check game over or level complete
            if (gameState.isGameOver() || gameState.isLevelComplete()) {
                gameTimer.stop();
            }
            
            repaint();
        }
    }
    
    private boolean isMoving = false;
    private final Point currentPixelPos = new Point();
    private static final int MOVE_SPEED = 8; // Pixels per frame for smoother movement
    private static final int FRAME_DELAY = 16; // ~60 FPS
    private final int[] currentMove = {0, 0}; // [dx, dy] of current move
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (isMoving) return; // Prevent new movement while already moving
        
        if (gameState.isGameOver()) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                startNewGame();
            }
            return;
        }
        
        if (gameState.isLevelComplete()) {
            if (e.getKeyCode() == KeyEvent.VK_N) {
                gameState.nextLevel();
                gameTimer.start();
                showMessage("Level " + gameState.getLevel() + " - Infect 70% of the grid!");
            }
            return;
        }
        
        // Handle player movement
        currentMove[0] = 0;
        currentMove[1] = 0;
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> currentMove[0] = -1;
            case KeyEvent.VK_RIGHT -> currentMove[0] = 1;
            case KeyEvent.VK_UP -> currentMove[1] = -1;
            case KeyEvent.VK_DOWN -> currentMove[1] = 1;
            case KeyEvent.VK_ESCAPE -> { window.showHomeScreen(); return; }
            default -> { return; }
        }
        
        // Calculate target position
        Point playerPos = gameState.getPlayerPos();
        int newX = playerPos.x + currentMove[0];
        int newY = playerPos.y + currentMove[1];
        
        // Check if target position is valid
        if (newX >= 0 && newX < gameState.getGrid().length &&
            newY >= 0 && newY < gameState.getGrid()[0].length) {
            
            EntityType target = gameState.getGrid()[newX][newY];
            if (target == EntityType.WALL) {
                return; // Can't move into walls
            }
            
            // Check what we're moving into for power-up feedback
            EntityType targetType = gameState.getGrid()[newX][newY];
            if (targetType == EntityType.INFECT_UPGRADE) {
                showMessage("Range Boosted!");
            } else if (targetType == EntityType.SHIELD) {
                showMessage("Shield Activated!");
            } else if (targetType == EntityType.TIME_EXTENSION) {
                showMessage("+30 Seconds!");
            } else if (targetType == EntityType.FIREWALL && gameState.getInfectionRange() > 1) {
                showMessage("Firewall Breached! Shielded!");
            }
            
            // Update player position in game state
            gameState.movePlayer(currentMove[0], currentMove[1]);
            
            // Start smooth movement animation
            isMoving = true;
            currentPixelPos.setLocation(
                playerPos.x * TILE_SIZE, 
                playerPos.y * TILE_SIZE
            );
            
            final int targetPixelX = newX * TILE_SIZE;
            final int targetPixelY = newY * TILE_SIZE;
            
            // Start movement animation on a separate thread
            new Thread(() -> {
                try {
                    // Calculate total distance to move
                    int totalPixels = TILE_SIZE;
                    int steps = Math.max(1, totalPixels / MOVE_SPEED);
                    
                    for (int i = 0; i < steps; i++) {
                        // Calculate new position
                        currentPixelPos.x = playerPos.x * TILE_SIZE + (currentMove[0] * i * MOVE_SPEED);
                        currentPixelPos.y = playerPos.y * TILE_SIZE + (currentMove[1] * i * MOVE_SPEED);
                        
                        // Ensure we don't overshoot
                        if (i == steps - 1) {
                            currentPixelPos.x = targetPixelX;
                            currentPixelPos.y = targetPixelY;
                        }
                        
                        // Update the display
                        SwingUtilities.invokeLater(this::repaint);
                        
                        // Small delay for smooth animation
                        try {
                            Thread.sleep(FRAME_DELAY);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } finally {
                    isMoving = false;
                    // Ensure final position is correct
                    currentPixelPos.x = targetPixelX;
                    currentPixelPos.y = targetPixelY;
                    SwingUtilities.invokeLater(this::repaint);
                }
            }).start();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
}
