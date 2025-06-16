package com.virusvector;

import javax.swing.*;
import java.awt.*;

public final class GameWindow extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String GAME_TITLE = "VIRUS VECTOR - Infection Maze";
    
    private JPanel currentScreen;

    public GameWindow() {
        // Initialize window properties
        initWindow();
        
        // Initialize with home screen
        showHomeScreen();
        setVisible(true);
    }
    
    private void initWindow() {
        setTitle(GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Use default icon if custom icon not found
            System.out.println("Could not load custom window icon");
        }
    }
    
    private void cleanupCurrentScreen() {
        if (currentScreen != null) {
            if (currentScreen instanceof HomeScreen homeScreen) {
                homeScreen.dispose();
            }
            remove(currentScreen);
            currentScreen = null;
        }
    }

    /**
     * Updates the window title with an optional subtitle
     * @param subtitle Optional subtitle to append to the main title (null or empty for just the main title)
     */
    public void updateTitle(String subtitle) {
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            setTitle(GAME_TITLE + " - " + subtitle);
        } else {
            setTitle(GAME_TITLE);
        }
    }


    public void showHomeScreen() {
        cleanupCurrentScreen();
        
        // Create and show new home screen
        currentScreen = new HomeScreen(this::startGame);
        setContentPane(currentScreen);
        updateTitle("Main Menu");
        revalidate();
        repaint();
    }

    public void startGame() {
        cleanupCurrentScreen();
        
        GamePanel gamePanel = new GamePanel(this);
        currentScreen = gamePanel;
        
        // Add the game panel to a JPanel with BorderLayout
        JPanel container = new JPanel(new BorderLayout());
        container.add(gamePanel, BorderLayout.CENTER);
        
        // Add to window
        setContentPane(container);
        updateTitle("Level 1");
        revalidate();
        
        // Request focus for the game panel
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    }
}
