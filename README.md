# BetterTogether: A Gamified Habit Tracker (Server)
 
BetterTogether is a gamified habit tracking app that turns self-improvement into an RPG-style adventure. It solves the tedious nature of traditional habit trackers by blending personal growth with RPG mechanics and social accountability. Users complete habits to earn XP, level up their character, and team up with friends for "Boss Raids". This mix of individual progress and team-based goals makes achieving personal goals a fun, collaborative experience.  

This repository contains the backend server, providing the RESTful API and real-time WebSocket services that power the client application.

## Technologies Used

- **Framework**: Spring Boot
- **Language**: Java 17
- **Build Tool**: Gradle
- **Database**: H2 (In-Memory) with JPA/Hibernate
- **Real-time**: Spring WebSockets
- **Auth**: Token-based authentication via HTTP Cookies

## High-Level Components

1. **User & Character Management (`UserService`, `CharacterService`)**: Manages authentication, profiles, character stats, achievements, and the global leaderboard.

2. **Task Processing (`HabitService`, `TodoService`)**: Handles the core loop of creating, scheduling, and completing daily habits and to-dos to earn XP and level up.

3. **Boss Raids (`RaidService`)**: The cooperative multiplayer engine. Schedules and runs live boss fights, tracks group damage, enforces time limits on tasks, and applies health penalties if groups fail.

4. **Live Synchronization (`RaidLiveService`, `CharacterLiveService`)**: Dispatches real-time state updates to connected clients during active raids and for the live online map.

5. **Integrations (`CalendarService`)**: Optional Google Calendar OAuth2 integration for syncing raid schedules directly into users' external calendars.

## Launch & Deployment

Follow these steps to get the server running locally for development.

### Prerequisites

- Java 17 SDK (ensure your `JAVA_HOME` environment variable is set correctly)
- Your IDE of choice (e.g., IntelliJ, Visual Studio Code)
- A tool for API testing, like Postman.

### Running Locally

1. **Clone the repository:**
    ```bash
    git clone <https://github.com/joshuademarco/sopra-fs26-group-08-server.git>
    cd sopra-fs26-group-08-server
    ```

2. **Environment Setup (Optional):**
    If you wish to test the Google Calendar integration, configure the properties in `src/main/resources/application.properties` or set them via environment variables:
    ```properties
    GOOGLE_CLIENT_ID=your_client_id
    GOOGLE_CLIENT_SECRET=your_client_secret
    ```

3. **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The server will start on `http://localhost:8080`.

### Running the Tests


To run the automated test suite, execute the following command:
    ```bash
    ./gradlew test
    ```

## Deployment

This project is set up for continuous deployment using Docker. Pushes to the `main` branch are automatically built and published.

To run the latest image locally using Docker:
```bash
docker pull sopragroup08/sopra-fs26-group-08-server:latest
docker run -p 8080:8080 sopragroup08/sopra-fs26-group-08-server
```

## Roadmap

- **Expanded Boss Raid Mechanics**: Introduce multi-phase bosses and varying raid difficulties.
- **Item Drop System**: Implement logic to drop and equip varying tiers of items (Common, Rare, Legendary).

## Authors & Acknowledgment

- **@alemicap**
- **@joshuademarco**
- **@michaelCHer**
- **@yappayappay**

## License

Distributed under the MIT License. See `License` for more information.
