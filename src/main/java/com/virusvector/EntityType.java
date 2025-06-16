package com.virusvector;

import java.awt.Color;

/**
 * Represents different types of entities in the game.
 */
public enum EntityType {
    // Basic tiles
    EMPTY(0, new Color(30, 30, 40)),
    WALL(1, new Color(60, 60, 80)),
    
    // Player and infection
    PLAYER(2, new Color(0, 200, 0)),
    INFECTED(3, new Color(0, 180, 0)),
    
    // Obstacles
    FIREWALL(4, new Color(200, 100, 0)),
    ANTIVIRUS(5, new Color(200, 0, 0)),
    
    // Power-ups
    INFECT_UPGRADE(6, new Color(0, 200, 200)),
    SHIELD(7, new Color(0, 100, 200)),
    TIME_EXTENSION(8, new Color(200, 200, 0));
    
    private final int value;
    private final Color color;
    
    EntityType(int value, Color color) {
        this.value = value;
        this.color = color;
    }
    
    public int getValue() {
        return value;
    }
    
    public Color getColor() {
        return color;
    }
    
    public static EntityType fromValue(int value) {
        for (EntityType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return EMPTY;
    }
}
