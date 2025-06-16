# Virus Vector: Infection Maze

A Java-based puzzle game where you play as a virus trying to infect a maze while avoiding obstacles and collecting power-ups.

![image](https://github.com/user-attachments/assets/3b6c5d53-dc79-4abb-bd3c-c409f7726671)

![image](https://github.com/user-attachments/assets/5d861721-6b96-4260-b42c-6f1feec8ed4f)

## 🎮 Game Overview

In **Virus Vector: Infection Maze**, you control a virus that must spread through a maze, infecting empty tiles while avoiding hazards. The game features multiple levels, power-ups, and increasing difficulty.

## 🚀 Features

- **Infection Mechanic**: Spread the virus to adjacent tiles to turn them green
- **Obstacles**:
  - **Firewalls (Orange)**: 
    - Block your movement by default
    - Can be passed when you have an active INFECT_UPGRADE (light blue power-up)
    - Passing through grants you a shield
    - Number increases with each level
  - **Anti-Virus (Red)**: Restarts game from level 1 if hit without shield
- **Power-ups**:
  - **Infectivity Upgrade (Light Blue)**: Increases infection range to 3x3 for 10 seconds
  - **Shield (Blue)**: Protects against anti-virus traps for 15 seconds
  - **Time Extension (Yellow)**: Adds 30 seconds to the timer (appears from level 3+)
- **Progressive Difficulty**: Each level introduces new challenges
- **Lives System**: 10 lives per level
- **Visual Feedback**: Clear indicators for power-ups and game state

## 🛠️ Tech Stack

- **Language**: Java 17+
- **Core Libraries**:
  - `javax.swing.*` - GUI components and windows
  - `java.awt.*` - Graphics rendering and event handling
  - `java.util.*` - Data structures and utilities
- **Build**: Direct compilation (No build tools)
- **Dependencies**: None (Pure Java SE)
- **Performance**: 60 FPS target with smooth animations
- **Threading**: Event Dispatch Thread for UI, separate thread for animations
- **Key Features**:
  - Custom game loop using `javax.swing.Timer`
  - Object-oriented design with clear separation of concerns
  - Event-driven architecture using AWT/Swing event model

## 📁 Project Structure

```text
GAME/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── virusvector/
│                   ├── MainGame.java         # Entry point with main()
│                   ├── GameWindow.java       # Main JFrame and window management
│                   ├── HomeScreen.java       # Animated main menu with interactive elements
│                   ├── GamePanel.java        # Game rendering and input handling
│                   ├── GameState.java        # Core game logic and state management
│                   └── EntityType.java       # Game entity definitions and properties
└── README.md
```

### Key Components

- **MainGame.java**: Initializes the application and starts the game window
- **GameWindow.java**: Manages the main JFrame and screen transitions
- **HomeScreen.java**: Implements the animated main menu with particle effects
- **GamePanel.java**: Handles rendering, user input, and the game loop
- **GameState.java**: Contains all game rules, state, and logic
- **EntityType.java**: Defines game entities and their properties

## 🏗️ Implementation Details

### Game Architecture

- **MVC-like Pattern**:
  - **Model**: `GameState` manages all game data and rules
  - **View**: `GamePanel` handles rendering
  - **Controller**: Input handling spread across components

### Technical Highlights

- **Custom Rendering**: Overridden `paintComponent` for all graphics
- **Event Handling**: Combines AWT and Swing event listeners
- **State Management**: Centralized game state with clear separation from UI
- **Animation System**: Uses `javax.swing.Timer` for smooth animations

### Performance

- Lightweight implementation with minimal object creation during gameplay
- Efficient repaint region management
- No external dependencies for maximum portability

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or later
- No additional libraries or tools required

### Building from Source

```bash
# Compile all Java files
javac src/main/java/com/virusvector/*.java -d out/

# Run the game
java -cp out/ com.virusvector.MainGame
```

## 🛠️ Development

### Potential Enhancements

1. **Build System**: Add Maven/Gradle for dependency management
2. **Testing**: Implement unit tests with JUnit
3. **Packaging**: Create executable JAR with proper manifest
4. **Asset Management**: Organize resources (images, sounds) properly
5. **Logging**: Add logging framework for debugging
6. **Configuration**: Externalize game settings (e.g., window size, controls)

## 🎮 Controls

- **Arrow Keys**: Move the virus one tile at a time
- **R**: Restart after game over
- **N**: Start next level (when level complete)
- **ESC**: Return to home screen (pauses current game)

## 🚀 How to Run

1. Ensure you have Java 17 or later installed
2. Compile all Java files:
   ```bash
   javac src/main/java/com/virusvector/*.java -d out/
   ```
3. Run the game:
   ```bash
   java -cp out/ com.virusvector.MainGame
   ```

## 🎯 Game Rules

1. **Objective**:
   - Infect at least 70% of the maze to complete each level
   - Score points for each infected tile (10 points each)
   - Earn level completion bonus (level × 1000 points)

2. **Lives**:
   - Start with 10 lives
   - Lose a life when time runs out
   - Game restarts from level 1 if hit by antivirus without shield

3. **Power-ups**:
   - **Light Blue (INFECT_UPGRADE)**: 
     - Increases infection range to 3x3 for 10 seconds
     - Allows passing through orange firewalls
   - **Blue (SHIELD)**: 
     - Protection against antivirus for 15 seconds
     - Also obtained when passing firewalls with INFECT_UPGRADE
   - **Yellow (TIME_EXTENSION)**: +30 seconds (level 3+ only)

4. **Level Progression**:
   - Each level adds more antivirus and firewalls
   - Firewalls can be passed with infection upgrade active
   - Passing firewalls grants shields
   - Time carries over to next level
   - Score multiplier increases with level

## 🖼️ Customization

To add a custom window icon, place a 64x64 PNG file named `icon.png` in the `resources` directory.

## 🐛 Known Issues

- Window icon may not load if the resource path is incorrect
- Some visual artifacts may occur during rapid movement

## 📜 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Credits

- Developed with ❤️ by [ANKAN BASU]
- Sound effects and graphics created with [Tool Name] (if applicable)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

🎮 **Happy Gaming!** Spread the infection and conquer the maze! 🦠
