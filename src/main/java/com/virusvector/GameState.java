package com.virusvector;

import java.awt.Point;
import java.util.Random;

/**
 * Manages the game state including the grid, player position, score, and game logic.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class GameState {
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 15;
    private static final int INFECTION_PERCENTAGE_TO_WIN = 70;
    private static final int MAX_LIVES = 10;
    private static final int LEVEL_TIME_SECONDS = 120;
    
    private EntityType[][] grid;
    private Point playerPos;
    private int score;
    private int level;
    private int lives;
    private int timeLeft;
    private long lastUpdateTime;
    private boolean gameOver;
    private boolean levelComplete;
    private final Random random;

    // Enemy tracking
    private List<Point> patrollingEnemies = new ArrayList<>();
    private HashMap<Point, Boolean> patrollingDirections = new HashMap<>(); // true: right, false: left
    private List<Point> chasingEnemies = new ArrayList<>();
    private List<Point> randomEnemies = new ArrayList<>();
    private List<Point> spawners = new ArrayList<>();
    private HashMap<Point, Long> spawnerTimers = new HashMap<>();

    // Power-up states
    private boolean hasShield;
    private int infectionRange;
    private long shieldEndTime;
    private long infectionEndTime;
    
    public GameState() {
        this.random = new Random();
        initializeGame();
    }
    
    private void initializeGame() {
        score = 0;
        level = 1;
        lives = MAX_LIVES;
        hasShield = false;
        infectionRange = 1;
        initializeLevel();
    }
    
    public void startNewGame() {
        initializeGame();
    }
    
    private void initializeLevel() {
        grid = new EntityType[GRID_WIDTH][GRID_HEIGHT];
        patrollingEnemies.clear();
        chasingEnemies.clear();
        randomEnemies.clear();
        spawners.clear();
        patrollingDirections.clear();
        spawnerTimers.clear();
        
        // Fill with empty tiles
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid[x][y] = EntityType.EMPTY;
            }
        }
        
        // Place player in the center
        playerPos = new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2);
        grid[playerPos.x][playerPos.y] = EntityType.PLAYER;
        
        // Place walls and obstacles
        placeObstacles();
        placePowerUps();
        placeStrategicEnemiesAndSpawners();
        
        // Reset level state
        timeLeft = LEVEL_TIME_SECONDS * 1000; // Convert to milliseconds
        lastUpdateTime = System.currentTimeMillis();
        gameOver = false;
        levelComplete = false;
    }

    private void placeStrategicEnemiesAndSpawners() {
        // Place patrolling enemies
        for (int i = 0; i < Math.max(1, level / 2); i++) {
            Point p = placeRandomStrategicEntity(EntityType.PATROLLING_ANTIVIRUS);
            patrollingEnemies.add(p);
            patrollingDirections.put(p, random.nextBoolean());
        }
        // Place chasing enemies
        for (int i = 0; i < level / 2; i++) {
            Point p = placeRandomStrategicEntity(EntityType.CHASING_ANTIVIRUS);
            chasingEnemies.add(p);
        }
        // Place random-walking enemies
        for (int i = 0; i < level / 2; i++) {
            Point p = placeRandomStrategicEntity(EntityType.RANDOM_ANTIVIRUS);
            randomEnemies.add(p);
        }
        // Place enemy spawners
        for (int i = 0; i < Math.max(1, level / 3); i++) {
            Point p = placeRandomStrategicEntity(EntityType.ENEMY_SPAWNER);
            spawners.add(p);
            spawnerTimers.put(p, System.currentTimeMillis());
        }
    }

    private Point placeRandomStrategicEntity(EntityType type) {
        int x, y;
        do {
            x = 1 + random.nextInt(GRID_WIDTH - 2);
            y = 1 + random.nextInt(GRID_HEIGHT - 2);
        } while (grid[x][y] != EntityType.EMPTY || (x == playerPos.x && y == playerPos.y));
        grid[x][y] = type;
        return new Point(x, y);
    }
    
    private void placeObstacles() {
        // Place walls around the border
        for (int x = 0; x < GRID_WIDTH; x++) {
            grid[x][0] = EntityType.WALL;
            grid[x][GRID_HEIGHT - 1] = EntityType.WALL;
        }
        for (int y = 0; y < GRID_HEIGHT; y++) {
            grid[0][y] = EntityType.WALL;
            grid[GRID_WIDTH - 1][y] = EntityType.WALL;
        }
        
        // Place random walls, firewalls and anti-virus
        int numWalls = (GRID_WIDTH * GRID_HEIGHT) / 15; // ~6.7% walls (reduced from 10%)
        int numAntivirus = level + 1; // More antivirus in higher levels
        int numFirewalls = Math.min(level, 5); // 1-5 firewalls based on level
        
        placeRandomEntities(EntityType.WALL, numWalls);
        placeRandomEntities(EntityType.ANTIVIRUS, numAntivirus);
        placeRandomEntities(EntityType.FIREWALL, numFirewalls);
    }
    
    private void placePowerUps() {
        // Ensure at least one power-up per level
        placeRandomEntity(EntityType.INFECT_UPGRADE);
        placeRandomEntity(EntityType.SHIELD);
        
        // More power-ups in higher levels
        if (level > 2) {
            placeRandomEntity(EntityType.TIME_EXTENSION);
        }
    }
    
    private void placeRandomEntities(EntityType type, int count) {
        for (int i = 0; i < count; i++) {
            placeRandomEntity(type);
        }
    }
    
    private void placeRandomEntity(EntityType type) {
        int x, y;
        do {
            x = 1 + random.nextInt(GRID_WIDTH - 2);
            y = 1 + random.nextInt(GRID_HEIGHT - 2);
        } while (grid[x][y] != EntityType.EMPTY || (x == playerPos.x && y == playerPos.y));
        
        grid[x][y] = type;
    }
    
    public void update() {
        if (gameOver || levelComplete) return;
        
        // Update timer
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        
        timeLeft -= deltaTime;
        
        // Check for time up
        if (timeLeft <= 0) {
            timeLeft = 0;
            loseLife();
        }
        
        // Check power-up expiration
        checkPowerUpExpiration(currentTime);

        // Move patrolling enemies
        List<Point> newPatrolling = new ArrayList<>();
        HashMap<Point, Boolean> newDirections = new HashMap<>();
        for (Point p : patrollingEnemies) {
            boolean right = patrollingDirections.getOrDefault(p, true);
            int nx = p.x + (right ? 1 : -1);
            int ny = p.y;
            if (nx <= 0 || nx >= GRID_WIDTH - 1 || grid[nx][ny] != EntityType.EMPTY) {
                right = !right;
                nx = p.x + (right ? 1 : -1);
            }
            if (nx > 0 && nx < GRID_WIDTH - 1 && grid[nx][ny] == EntityType.EMPTY) {
                grid[p.x][p.y] = EntityType.EMPTY;
                p.setLocation(nx, ny);
                grid[nx][ny] = EntityType.PATROLLING_ANTIVIRUS;
            }
            newPatrolling.add(new Point(p));
            newDirections.put(new Point(p), right);
        }
        patrollingEnemies = newPatrolling;
        patrollingDirections = newDirections;

        // Move chasing enemies
        List<Point> newChasing = new ArrayList<>();
        for (Point p : chasingEnemies) {
            int dx = Integer.compare(playerPos.x, p.x);
            int dy = Integer.compare(playerPos.y, p.y);
            int nx = p.x + dx;
            int ny = p.y + dy;
            if (nx > 0 && nx < GRID_WIDTH - 1 && ny > 0 && ny < GRID_HEIGHT - 1 && grid[nx][ny] == EntityType.EMPTY) {
                grid[p.x][p.y] = EntityType.EMPTY;
                p.setLocation(nx, ny);
                grid[nx][ny] = EntityType.CHASING_ANTIVIRUS;
            }
            newChasing.add(new Point(p));
        }
        chasingEnemies = newChasing;

        // Move random enemies
        List<Point> newRandom = new ArrayList<>();
        for (Point p : randomEnemies) {
            int[] dirs = {-1, 0, 1};
            int dx = dirs[random.nextInt(3)];
            int dy = dirs[random.nextInt(3)];
            int nx = p.x + dx;
            int ny = p.y + dy;
            if (nx > 0 && nx < GRID_WIDTH - 1 && ny > 0 && ny < GRID_HEIGHT - 1 && grid[nx][ny] == EntityType.EMPTY) {
                grid[p.x][p.y] = EntityType.EMPTY;
                p.setLocation(nx, ny);
                grid[nx][ny] = EntityType.RANDOM_ANTIVIRUS;
            }
            newRandom.add(new Point(p));
        }
        randomEnemies = newRandom;

        // Enemy spawner logic
        for (Point s : spawners) {
            long lastSpawn = spawnerTimers.getOrDefault(s, 0L);
            if (currentTime - lastSpawn > 4000) { // spawn every 4 seconds
                // Find adjacent empty cell
                int[][] adj = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] d : adj) {
                    int nx = s.x + d[0];
                    int ny = s.y + d[1];
                    if (nx > 0 && nx < GRID_WIDTH - 1 && ny > 0 && ny < GRID_HEIGHT - 1 && grid[nx][ny] == EntityType.EMPTY) {
                        EntityType enemyType = switch (random.nextInt(3)) {
                            case 0 -> EntityType.PATROLLING_ANTIVIRUS;
                            case 1 -> EntityType.CHASING_ANTIVIRUS;
                            default -> EntityType.RANDOM_ANTIVIRUS;
                        };
                        grid[nx][ny] = enemyType;
                        if (enemyType == EntityType.PATROLLING_ANTIVIRUS) {
                            patrollingEnemies.add(new Point(nx, ny));
                            patrollingDirections.put(new Point(nx, ny), random.nextBoolean());
                        } else if (enemyType == EntityType.CHASING_ANTIVIRUS) {
                            chasingEnemies.add(new Point(nx, ny));
                        } else {
                            randomEnemies.add(new Point(nx, ny));
                        }
                        spawnerTimers.put(s, currentTime);
                        break;
                    }
                }
            }
        }
    }
    
    private void checkPowerUpExpiration(long currentTime) {
        if (hasShield && currentTime > shieldEndTime) {
            hasShield = false;
        }
        if (infectionRange > 1 && currentTime > infectionEndTime) {
            infectionRange = 1;
        }
    }
    
    public void movePlayer(int dx, int dy) {
        if (gameOver || levelComplete) return;
        
        int newX = playerPos.x + dx;
        int newY = playerPos.y + dy;
        
        // Check bounds
        if (newX < 0 || newX >= GRID_WIDTH || newY < 0 || newY >= GRID_HEIGHT) {
            return;
        }
        
        EntityType target = grid[newX][newY];
        boolean canMove = true;
        
        // Handle different entity types
        switch (target) {
            case EMPTY, INFECTED, PLAYER -> {
                // These don't block movement
            }
            case WALL -> {
                canMove = false; // Can't move through walls
            }
            case FIREWALL -> {
                if (infectionRange > 1) {
                    // Can pass through with upgrade
                    hasShield = true;
                    shieldEndTime = System.currentTimeMillis() + 10000; // 10 second shield
                } else {
                    canMove = false;
                }
            }
            case ANTIVIRUS, PATROLLING_ANTIVIRUS, CHASING_ANTIVIRUS, RANDOM_ANTIVIRUS -> {
                if (!hasShield) {
                    loseLife();
                    return;
                } else {
                    // Remove enemy from tracking list
                    if (target == EntityType.PATROLLING_ANTIVIRUS) {
                        patrollingEnemies.removeIf(e -> e.equals(new Point(newX, newY)));
                        patrollingDirections.remove(new Point(newX, newY));
                    } else if (target == EntityType.CHASING_ANTIVIRUS) {
                        chasingEnemies.removeIf(e -> e.equals(new Point(newX, newY)));
                    } else if (target == EntityType.RANDOM_ANTIVIRUS) {
                        randomEnemies.removeIf(e -> e.equals(new Point(newX, newY)));
                    }
                }
            }
            case ENEMY_SPAWNER -> {
                // Destroy the spawner
                spawners.removeIf(e -> e.equals(new Point(newX, newY)));
                spawnerTimers.remove(new Point(newX, newY));
            }
            case INFECT_UPGRADE -> {
                infectionRange = 3;
                infectionEndTime = System.currentTimeMillis() + 10000; // 10 seconds
            }
            case SHIELD -> {
                hasShield = true;
                shieldEndTime = System.currentTimeMillis() + 15000; // 15 seconds
            }
            case TIME_EXTENSION -> {
                timeLeft += 30000; // 30 seconds
            }
        }
        
        if (!canMove) {
            return;
        }
        
        // Move player one tile at a time
        grid[playerPos.x][playerPos.y] = EntityType.INFECTED;
        playerPos.setLocation(newX, newY);
        grid[newX][newY] = EntityType.PLAYER;
        
        // Infect adjacent tiles based on current range
        infectAdjacentTiles();
        
        // Check win condition
        checkWinCondition();
    }
    
    private void infectAdjacentTiles() {
        for (int dx = -infectionRange; dx <= infectionRange; dx++) {
            for (int dy = -infectionRange; dy <= infectionRange; dy++) {
                if (dx == 0 && dy == 0) continue; // Skip the player's position
                
                int x = playerPos.x + dx;
                int y = playerPos.y + dy;
                
                // Check bounds and distance (circular infection pattern)
                if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    if (distance <= infectionRange) {
                        // Only infect empty tiles
                        if (grid[x][y] == EntityType.EMPTY) {
                            grid[x][y] = EntityType.INFECTED;
                            score += 10;
                        }
                    }
                }
            }
        }
    }
    
    private void checkWinCondition() {
        int totalCells = (GRID_WIDTH - 2) * (GRID_HEIGHT - 2); // Exclude borders
        int infectedCells = 0;
        
        for (int x = 1; x < GRID_WIDTH - 1; x++) {
            for (int y = 1; y < GRID_HEIGHT - 1; y++) {
                if (grid[x][y] == EntityType.INFECTED || grid[x][y] == EntityType.PLAYER) {
                    infectedCells++;
                }
            }
        }
        
        int infectionPercentage = (infectedCells * 100) / totalCells;
        
        if (infectionPercentage >= INFECTION_PERCENTAGE_TO_WIN) {
            levelComplete = true;
            score += level * 1000; // Bonus points for completing level
        }
    }
    
    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
        } else {
            // Reset level but keep score and lives
            initializeLevel();
        }
    }
    
    public void nextLevel() {
        if (levelComplete) {
            level++;
            initializeLevel();
        }
    }
    
    // Getters
    public EntityType[][] getGrid() { return grid; }
    public Point getPlayerPos() { return new Point(playerPos); }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getLives() { return lives; }
    public int getTimeLeft() { return timeLeft / 1000; } // Convert to seconds
    public boolean isGameOver() { return gameOver; }
    public boolean isLevelComplete() { return levelComplete; }
    public boolean hasShield() { return hasShield; }
    public int getInfectionRange() { return infectionRange; }
}
