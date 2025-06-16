package com.virusvector;

public class MainGame {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow gameWindow = new GameWindow();
            gameWindow.showHomeScreen();
        });
    }
}
