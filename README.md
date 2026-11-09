# Settlers-Of-Catan-Remix

## Team Information

### Team Members
- Taito Sugiyama
- James Wong
- Chuqian Xiao
- Alison Cai

---

## Project Description

### Domain

[Project description]

---

## User Stories

### User Story 1

### User Story 2

### User Story 3

(Add additional user stories as needed.)

---

## Use Cases

### Use Case 1

### Use Case 2

### Use Case 3

(Add additional use cases as needed.)

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case |
|-------------|--------------|
| Name | Use Case |
| Name | Use Case |
| Name | Use Case |

---

## Entities

### Entity 1

### Entity 2

(Add additional entities as needed.)

---

## API Usage

**API Name:** Open-Meteo Weather API

**Documentation:** https://open-meteo.com/en/docs

**How it is used in the program:**
Our game will use the Open-Meteo Weather API to retrieve real-time weather conditions and incorporate them into the gameplay.

- The game will provide a list of predefined cities (e.g., Toronto, London, Tokyo, and New York) for players to choose from.
- Each city will have predefined coordinates (latitude and longitude), which will be used to request its current weather conditions from the API.
- The API will be called once when the game starts, and the retrieved weather conditions will remain fixed throughout the game.
- The program will use the weather code returned by the API to classify the weather into five main categories. Each category will double the production of one of the five resources:
    - **Sunny (WMO code 0–1):** Lumber production ×2
    - **Cloudy (WMO code 2–3):** Brick production ×2
    - **Rainy (WMO codes 51–67, 80–82):** Grain production ×2
    - **Snowy (WMO codes 71–77, 85–86):** Wool production ×2
    - **Stormy (WMO codes 95–99):** Ore production ×2
- Other weather conditions, such as fog (WMO codes 45 and 48), will not provide any additional resource bonuses.
- Weather bonuses will only apply when the corresponding resource-producing tiles are activated by the dice. Normal resource production will remain unchanged for all other resources.
- If the API request fails, the game will continue with normal resource production.

---

## Design Diagrams

### UML Class Diagram


### Sequence Diagrams

---

## How to Run

### Requirements

- Java Development Kit (JDK 26)
- IntelliJ IDEA
- Maven (bundled with IntelliJ IDEA)
- Internet connection (for retrieving weather data)

### Running the Game

1. Open the GitHub repository and click the green **Code** button.
2. Select **HTTPS** and copy the repository URL.
3. Open IntelliJ IDEA and select **Clone Repository**.
4. Paste the copied URL into the **URL** field, choose a local directory, and click **Clone**.
5. Wait for IntelliJ IDEA to open the project and load the Maven dependencies.
6. Go to **File → Project Structure → Project** and make sure the Project SDK is set to **JDK 26**.
7. Locate the main Java class containing the `main()` method.
8. Run the `main()` method to launch the game in a Java Swing window.


### Starting a Game

1. Select the number of players (2–4 players).
2. Select a city from the list of predefined cities.
3. Click **Start Game** to begin.
4. The game will retrieve the selected city's current weather conditions using the Open-Meteo Weather API.
5. The weather conditions will remain fixed throughout the game and determine which resource receives a production bonus.
6. Players take turns placing their initial settlements and roads in a snake order (forward order followed by reverse order):
  - **2 players:** P1 → P2 → P2 → P1
  - **3 players:** P1 → P2 → P3 → P3 → P2 → P1
  - **4 players:** P1 → P2 → P3 → P4 → P4 → P3 → P2 → P1
7. During each placement turn, the player places one settlement followed by one adjacent road.
8. Once all players have placed two settlements and two roads, regular gameplay begins.
9. Players take turns rolling dice, collecting resources, and building roads, settlements, and cities.
10. The first player to reach 10 victory points wins the game.


### Weather API

An internet connection is required to retrieve weather data when starting a new game. If the API request fails or the weather condition does not have an assigned bonus, the game will continue with normal resource production.

---

## Notes

[Any additional project information.]
