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
Our own rendition of the classic board game Settlers of Catan. 

A multiplayer strategy board game where players compete to build and expand their civilizations. Place roads, settlements (1 point each) and cities (2 points) and roll dices to gather resources. Build more infrastructure, block other players, and be the first to 10 points!

---

## Game Rules

### 1. Game Setup
- The game supports 2–4 players playing locally on the same device
- Each player enters a unique name before starting the game
- Players select a city from a predefined list to determine the weather conditions for the game
- After the game starts, players take turns placing their initial settlements and roads
- Initial placement follows a snake order (forward order followed by reverse order):
  - **2 players:** P1 → P2 → P2 → P1
  - **3 players:** P1 → P2 → P3 → P3 → P2 → P1
  - **4 players:** P1 → P2 → P3 → P4 → P4 → P3 → P2 → P1
- During each placement turn, the player places one settlement followed by one road adjacent to that settlement
- Initial placements do not require resources
- Each player starts regular gameplay with 2 settlements and 2 roads

### 2. Turn Rules
- After the initial placement phase, regular gameplay begins with Player 1
- Players take turns in ascending player order (P1 → P2 → P3 → P4, depending on the number of players), returning to Player 1 after the last player finishes their turn
- At the beginning of each turn, the active player rolls 2 six-sided dice once
- The sum of the 2 dice determines which tiles produce resources
- After rolling the dice and collecting resources, the active player can build roads, settlements, or cities if they have sufficient resources and the selected locations are valid
- Once the active player ends their turn, the game proceeds to the next player

### 3. Resource Production
- The game uses five resources: Lumber, Brick, Wool, Grain, and Ore
- The active player rolls 2 six-sided dice, and their results are added together
- Tiles with number tokens matching the dice total produce resources
- Players with settlements or cities adjacent to those tiles receive the corresponding resources:
  - **Settlement:** Produces 1 resource
  - **City:** Produces 2 resources
- Weather-based production bonuses are applied when applicable
- If the dice total is 7, no resources are distributed
- If no players have settlements or cities adjacent to the matching tiles, no resources are distributed

### 4. Building Rules
- Players can build roads, settlements, and cities by spending the required resources
- The resource costs for each structure are:
  - **Road:** 1 Lumber + 1 Brick
  - **Settlement:** 1 Lumber + 1 Brick + 1 Wool + 1 Grain
  - **City:** 2 Grain + 3 Ore
- Roads are placed on edges, while settlements and cities are placed on vertices
- A road must connect to the player's existing road network or building, following the game's placement rules
- A new settlement must connect to the player's road network, except during the initial placement phase
- A settlement must be at least 2 edges away from any other settlement or city, regardless of ownership
- A city can only be built by upgrading an existing settlement owned by the player
- Each settlement is worth 1 victory point, while each city is worth 2 victory points. Upgrading a settlement to a city increases the player's total victory points by 1

### 5. Longest Road
- A player must have a continuous road of at least 5 segments to qualify for the Longest Road bonus
- The player with the longest qualifying road receives 2 additional victory points
- If another player builds a longer continuous road than the current holder, the Longest Road bonus transfers to that player
- When the bonus transfers, the previous holder loses the 2 additional victory points

### 6. Weather Bonus
- When the game starts, the system retrieves the selected city's current weather conditions using the Open-Meteo Weather API
- The weather conditions remain fixed throughout the game
- Each weather category doubles the production of one resource:
  - **Sunny (WMO codes 0–1):** Lumber ×2
  - **Cloudy (WMO codes 2–3):** Brick ×2
  - **Rainy (WMO codes 51–67, 80–82):** Grain ×2
  - **Snowy (WMO codes 71–77, 85–86):** Wool ×2
  - **Stormy (WMO codes 95–99):** Ore ×2
- Weather bonuses only apply when the corresponding resource-producing tiles are activated by the dice
- Other weather conditions, such as fog, do not provide a resource production bonus
- If the API request fails or the weather condition does not have an assigned bonus, normal resource production applies

### 7. Victory Conditions
- Each settlement is worth 1 victory point
- Each city is worth 2 victory points
- The player holding the Longest Road bonus receives 2 additional victory points
- The first player to reach 10 victory points wins the game
- The game ends once a winner is determined

---

## User Stories

### User Story 1
As a player, I want to place roads, settlements and cities so I can expand my civilization.

### User Story 2
As a player, I want to roll dice and receive resources based on the board so I can build more structures.

### User Story 3
As a player, I want to be able to start a game on the same device with 2-4 players and be able to play on the same board, switching turns.

### User Story 4
As a player, I want to be able to select a city from the pre-determined list before starting the game so that weather can influence conditions in the game.

### User Story 5
As a player, I want to see everyone's current total points, how much of each resource they have and the weather conditions.

### User Story 6
As a player, I want to get the conditional 2 points if I have the longest road (minimum 5 roads).

### User Story 7
As a player, I want to win the game (stop the game) if someone reaches 10 points.

--

## Use Cases

### Use Case 1
Primary actor: Player
Goal: Allows a player to build roads, settlements, and cities to expand their civilization and earn points
Preconditions:
- The player is in an active game
- It is their turn
- Sufficient resources to build the selected structure

Main path:
1. The player opens the building menu
2. The system displays the available structures (roads, settlements, and cities) and their resource costs
3. The player selects the structure they want to build
4. The system checks whether the player has enough resources
5. The system checks whether the selected location is valid according to the game rules
6. The system deducts the required resources from the player's inventory
7. The system places the structure on the board
8. The system updates the player's total points if applicable
9. The system updates the board to display the newly built structure

Alternate path:
4. If the player does not have enough resources, the system displays an error message and the player cannot build
5. If the selected location is invalid, the system displays an error message and allows the player to select another location
5. If the player attempts to build a city without an existing settlement at the selected location, the system rejects the action

Post conditions:
- The structure is successfully placed on the board
- The player's resources and points are updated accordingly

### Use Case 2
Primary actor: Player
Goal: Allows a player to roll the dice and collect resources from eligible hexes based on the dice result and their settlements or cities
Preconditions:
- The player is in an active game
- It is their turn
- The player has not already rolled during their current turn

Main path:
1. The player selects the Roll Dice button
2. The system generates a random result from rolling 2 six-sided dice
3. The system displays the total dice result
4. The system identifies all hexes with a matching number
5. The system checks which players have settlements or cities adjacent to those hexes
6. The system calculates the resources each eligible player should receive, applying the weather-based production bonus when applicable.
7. The system adds the resources to each player's inventory
8. The system updates the resource counts displayed on the screen
9. The system allows the active player to continue their turn

Alternate path:
4. If the dice result is 7, no resources are distributed.
5. If no players have settlements or cities adjacent to the matching hexes, no resources are distributed.

Post conditions:
- The dice result is displayed
- All eligible players receive the appropriate resources
- Player inventories are updated

### Use Case 3
Primary actor: Player
Goal: Allows 2–4 players to play a game on the same device, sharing the same board and taking turns
Preconditions:
- The game has been launched

Main path:
1. The system prompts the player to select the number of players (2–4)
2. The player selects the desired number of players
3. The system prompts each player to enter their name.
4. Each player enters their name and confirms their selection
5. The system initializes a shared game board
6. The system establishes the initial placement order using a snake order (forward order followed by reverse order), and each player places one settlement and one adjacent road during each placement turn.
7. Once all players have placed two settlements and two roads, regular gameplay begins with Player 1, following the player order (P1 → P2 → P3 → P4, depending on the number of players).
8. The system displays the game board and indicates which player's turn it is 
9. Players take turns interacting with the same board 
10. After a player's turn ends, the system switches control to the next player

Alternate path:
2. If the player selects fewer than 2 or more than 4 players, the system does not allow the selection.
4. If a player enters an empty or invalid name, the system prompts them to enter a valid name.
4. If two players enter the same name, the system prompts them to choose different names.
9. If a player attempts to perform an action during another player's turn, the system prevents the action.

Post-conditions:
- A game with 2–4 players is successfully initialized
- All players share the same game board 
- The system tracks whose turn it is and switches turns accordingly

### Use Case 4
Primary actor: Player
Goal: Allows players to select a city before starting the game and use its real-time weather conditions to determine resource production bonuses.
Preconditions:
- The game has been launched.
- Players are setting up a new game.

Main path:
1. The system displays a predefined list of available cities.
2. The player selects a city from the list.
3. The player clicks the Start Game button.
4. The system retrieves the predefined coordinates of the selected city.
5. The system sends a request to the Open-Meteo Weather API.
6. The system receives the current weather code from the API.
7. The system classifies the weather code and determines the corresponding resource production bonus.
8. The system displays the selected city, weather conditions, and resource production bonus.
9. The system saves the weather conditions and production bonus for the entire game.
10. The game proceeds to the initial settlement and road placement phase.

Alternate path:
2. If the player does not select a city, the system prompts them to select one before starting the game.
5. If the API request fails, the system continues without a weather bonus.
7. If the weather code does not correspond to any predefined weather category, the system applies normal resource production.

Postconditions:
- The selected city is saved for the game.
- The weather conditions and corresponding resource production bonus are determined if weather data is available.
- The weather bonus remains fixed throughout the game.
- The game proceeds to the initial placement phase.

### Use Case 5
Primary Actor: Player
Goal: Allows players to view game state information, including point totals, resource inventories, and current weather condition
Preconditions:
- The player is in an active game
- Game board and UI elements are loaded

Main path:
1. The system continuously displays the status panel on the screen throughout the game
2. The system reads and displays each player's current total victory points
3. The system reads and displays the count of each resource type (e.g., Brick, Lumber, Ore, Grain, Wool) in each player's inventory
4. The system displays the active weather conditions and any associated weather-based resource bonuses selected during game setup
5. The system indicates which player is currently active
6. The system updates the displayed points, resources, and turn indicator immediately whenever a game action changes any player's state

Alternate path:
1. If resource amounts or scores change (e.g., after building, trading, or rolling dice), the system immediately refreshes the status display to reflect the new values
2. If a player hides or toggles their inventory view (if UI toggle is supported), the system allows them to re-expand it to view their resource breakdown

Post conditions:
- All players have continuous access to player's score, resource count, active turn indicator, and global weather status

### Use Case 6
Primary Actor: Player
Goal: Grants 2 conditional victory points to the player who constructs the longest continuous road network of at least 5 segments, and reassigns or removes the points if another player surpasses or breaks that record
Preconditions:
- Player is in an active game
- It's their turn
- The player constructs a road segment or upgrades the board state

Main path:
1. The player places a new road segment on the board (following Use Case 1)
2. The system calculates the longest continuous path of connected road segments for the active player 
3. The system compares the active player's longest road length against the current threshold (minimum 5) and any existing "Longest Road" holder's record
4. The active player achieves a continuous road length of at least 5 segments and higher than any other player's current road length 
5. The system assigns the "Longest Road" title and 2 conditional victory points to the active player 
6. If another player previously held the title, the system deducts 2 victory points from that player and transfers the title 
7. The system updates the game status display to reflect the score change

Alternate path:
3. If the player's continuous road length is less than 5, no "Longest Road" points are awarded
4. If the active player's continuous road length ties the current holder's road length, the current holder retains the title and the 2 points
6. If a built settlement or city breaks a continuous road network, reducing the holder's road length below 5 (or below another player's length), the system recalculates all players' road lengths and either reassigns the 2 points to the new leader or removes them completely if no player meets the 5-road minimum

Post conditions:
- The 2 conditional points for Longest Road are correctly assigned to the eligible player or unassigned if no player qualifies
- All total victory point displays are updated

### Use Case 7
Primary Actor: Player
Goal: End the game immediately when a player reaches 10 victory points and declare that player the winner
Preconditions:
- The player is in an active game
- The player performs an action that modifies their victory points

Main path:
1. A player completes an action that increases their total victory points (e.g., building a settlement/city or securing Longest Road)
2. The system recalculates the player's total victory points 
3. The system checks whether the player's total victory points have reached or exceeded the winning threshold of 10 points 
4. The system triggers the game-over sequence and stops further gameplay actions 
5. The system displays a victory screen naming the winning player and showing final player scores and statistics 
6. The system offers options to restart a new game or exit to the main menu

Alternate path:
3. If the player's point total is less than 10 points, the game continues normally and turn control passes to the next phase or player

Post conditions:
- The game state changes from active to completed
- Gameplay interactions are disabled
- Winning player and final standing are displayed on screen
- Option to restart the game or exit to main menu is displayed

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case |
|-------------|--------------|
| Alison Cai | Use Case 1 |
| Taito Sugiyama | Use Case 2 |
| James Wong | Use Case 3, 7 |
| Chuqian Xiao | Use Case 4, 5, 6  |

---

## Entities

### Entity 1: Game
**Instance variables**
- Board board
- List<Player> players
- Player currentPlayer
- GamePhase phase
- WeatherData weather
- int currentPlayerIndex
- Dice dice
- boolean hasRolled
- SetupStep setupStep
- int setupIndex
- Vertex lastPlacedSettlement
- Player longestRoadHolder

**Methods**
- startGame()
- startSetupPhase()
- advanceSetup()
- canRoll()
- markRolled()
- endTurn()
- getPlayers()
- getCurrentPlayer()
- checkVictory()
- getWinner()

### Entity 2: Board
**Instance variables**
- List<Tile> tiles
- List<Vertex> vertices
- List<Edge> edges

**Methods**
- Board(List<Tile> tiles, List<Vertex> vertices, List<Edge> edges) (normally built by `BoardFactory`)
- getTiles(), getVertices(), getEdges() (unmodifiable lists)
- getTile(int tileId) (throws `IllegalArgumentException` for an unknown id)
- getVertex(int vertexId) (same)
- getEdge(int edgeId) (same)
- getMatchingTiles(int diceTotal) (tiles whose token equals the total; 7 matches none, and the desert never matches)
- getTilesAdjacentTo(Vertex vertex) (the 1 to 3 tiles that have the vertex as a corner)
- isValidSettlementLocation(Vertex vertex, Player player, boolean isSetup) (the vertex must be on this board, empty, and have no occupied neighbour, so settlements and cities stay at least 2 edges apart whoever owns them; outside setup it must also touch one of the player's roads)
- isValidRoadLocation(Edge edge, Player player, Vertex setupAnchor) (setupAnchor is the settlement just placed during setup; null otherwise. The edge must be on this board and empty. In setup it must touch the anchor. Otherwise it must connect to the player's own building or road, and an opponent's settlement or city blocks the connection through that vertex)
- placeBuilding(Vertex vertex, Building building) (places a settlement and adds it to the owner's buildings; rejects a City, an occupied vertex, a mismatched location and a vertex from another board)
- upgradeToCity(Vertex vertex, City city) (replaces the city owner's own settlement with the city, on the vertex and in the owner's buildings; rejects an empty vertex, an existing city, another player's settlement and a mismatched location)
- placeRoad(Edge edge, Road road) (places a road and adds it to the owner's roads; rejects an occupied edge, a mismatched location and an edge from another board)

The `place...` and `upgradeToCity` methods update both the board and the owner's `Player`, so callers must not also call `Player.addBuilding`, `removeBuilding` or `addRoad`, or the points and roads would be counted twice. They only check that the placement is consistent (they throw `IllegalArgumentException` otherwise), not the game rules, so call the `isValid...` methods first. Resource costs and turn checks are the use case's job.

### Entity 3: Tile
**Instance variables**
- int id
- TerrainType terrain
- ResourceType resource (null for the desert)
- int numberToken (none for the desert)
- List<Vertex> vertices
- List<Edge> edges

**Methods**
- Tile(int id, TerrainType terrain, int numberToken) (the resource is derived from the terrain; the token must be 2-12 except 7, or `Tile.NO_TOKEN` (0) for the desert; invalid combinations throw `IllegalArgumentException`)
- resourceFor(TerrainType terrain) (static: FOREST→LUMBER, HILLS→BRICK, FIELDS→GRAIN, PASTURE→WOOL, MOUNTAINS→ORE, DESERT→null)
- getId()
- getTerrain()
- getResourceType()
- getNumberToken() (`Tile.NO_TOKEN` for the desert)
- hasNumberToken()
- getVertices() (unmodifiable view, the 6 corners in order)
- getEdges() (unmodifiable view, the 6 sides; edge i joins corners i and i+1)

### Entity 4: Vertex
**Instance variables**
- int id
- List<Edge> edges
- Building building

**Methods**
- getId()
- isOccupied()
- getBuilding()
- setBuilding(Building building)
- getAdjacentVertices() (the vertices one edge away)
- getAdjacentEdges()

Edges are added to a vertex only by constructing an `Edge`, which registers itself with both of its vertices.

### Entity 5: Edge
**Instance variables**
- int id
- Vertex vertex1
- Vertex vertex2
- Road road

**Methods**
- getId()
- getVertex1()
- getVertex2()
- getOtherVertex(Vertex vertex) (the endpoint that is not the given vertex; throws if the vertex is not on this edge)
- isOccupied()
- getRoad()
- setRoad(Road road)
- getAdjacentEdges() (edges sharing a vertex with this one, not including itself)

The constructor `Edge(int id, Vertex vertex1, Vertex vertex2)` registers the edge with both vertices and rejects null or identical vertices.

### Entity 6: Player
**Instance variables**
- String name
- ResourceHand resources
- List<Building> buildings
- List<Road> roads
- boolean hasLongestRoad

**Methods**
- getName()
- addBuilding(Building building) (only updates the player's list. Do not call it directly when placing: `Board.placeBuilding` and `Board.upgradeToCity` already do)
- removeBuilding(Building building)
- addRoad(Road road) (only updates the player's list. Do not call it directly when placing: `Board.placeRoad` already does)
- getResources()
- getBuildings() (unmodifiable view)
- getRoads() (unmodifiable view)
- hasLongestRoad()
- setHasLongestRoad(boolean value)
- getVictoryPoints() (computed: sum of building points, plus 2 if hasLongestRoad)

### Entity 7: ResourceHand
**Instance variables**
- Map<ResourceType, Integer> resources

**Methods**
- addResource(ResourceType type, int amount)
- removeResource(ResourceType type, int amount)
- getResourceCount(ResourceType type)
- canAfford(Map<ResourceType, Integer> cost)
- spendResources(Map<ResourceType, Integer> cost) (throws and changes nothing if the hand cannot afford it)
- getTotalResources()

Negative amounts, and removing more than the hand holds, throw `IllegalArgumentException`.

### Entity 8: Building (abstract class)
**Instance variables**
- Player owner
- Vertex location

**Methods**
- getOwner()
- getLocation()
- getVictoryPoints() (abstract)

The constructor is `protected` and rejects a null owner or location. `Settlement(Player owner, Vertex location)` and `City(Player owner, Vertex location)` are the public constructors.

### Entity 9: Settlement (extends Building)
**Instance variables**
- No additional instance variables.

**Methods**
- getVictoryPoints()

### Entity 10: City (extends Building)
**Instance variables**
- No additional instance variables.

**Methods**
- getVictoryPoints()

### Entity 11: Road
**Instance variables**
- Player owner
- Edge location

**Methods**
- Road(Player owner, Edge location) (rejects null arguments)
- getOwner()
- getLocation()

### Supporting entities and value objects

### Entity 12: WeatherData
**Instance variables**
- WeatherType condition
- ResourceType bonusResource
- int multiplier

**Methods**
- fromWmoCode(int code) (fog codes 45 and 48 give condition FOG with no bonus; a code with no category gives none())
- none() (no bonus; used when the API fails or the code has no category. The condition and bonus resource are null and the multiplier is 1)
- getCondition()
- getBonusResource()
- getMultiplier()
- hasBonus()

### Entity 13: Dice
**Instance variables**
- int die1
- int die2
- Random random

**Methods**
- Dice(Random random) (a seeded Random can be passed in for testing)
- roll() (returns void; read the result with getDie1(), getDie2() and getTotal(). The dice read 0 before the first roll)
- getDie1()
- getDie2()
- getTotal()

### Entity 14: CityLocation
**Instance variables**
- String name
- double latitude
- double longitude

**Methods**
- CityLocation(String name, double latitude, double longitude)
- defaults() (the predefined city list: Toronto, London, Tokyo, New York)
- getName()
- getLatitude()
- getLongitude()

### Entity 15: BoardFactory
**Instance variables**
- No instance variables.

**Methods**
- createStandardBoard(Random random) (static; creates the 19 tiles, 54 vertices and 72 edges, wires up adjacency, and shuffles terrain and number tokens)

Terrain mix: 4 forest, 3 hills, 4 fields, 4 pasture, 3 mountains, 1 desert. Number tokens: 2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12, assigned randomly to the non-desert tiles with no adjacency restriction (for example 6 and 8 may touch). Ids are sequential from 0. The same seeded `Random` always gives the same board.

### Entity 16: BuildCost
**Instance variables**
- No instance variables.

**Methods**
- of(StructureType type) (returns the Map<ResourceType, Integer> cost)

### Domain services
These hold game rules that do not belong to a single entity. They have no state.

### Service 1: ProductionCalculator
**Methods**
- distribute(Board board, int diceTotal, WeatherData weather) (static; returns the resources each player receives; settlement x1, city x2, weather multiplier on the bonus resource)

`distribute` returns an unmodifiable `Map<Player, Map<ResourceType, Integer>>`. Players who receive nothing are not in the map, and a total of 7, no matching tile, or no adjacent building gives an empty map. The weather multiplier applies only to the bonus resource and only for tiles that were rolled. It does not change any player's hand: the roll-dice use case adds the result to each player's `ResourceHand`.

### Service 2: LongestRoadCalculator
**Methods**
- longestRoadOf(Player player, Board board) (longest continuous road, not counting paths broken by another player's settlement or city)
- findHolder(List<Player> players, Board board, Player currentHolder) (returns the player who should hold Longest Road, or null; minimum 5 segments, a tie keeps the current holder)

**Enumerations**

These are supporting types rather than regular entities.
- **Enum:**    Values
- **GamePhase:**    SETUP, PLAYING, GAME_OVER
- **SetupStep:**    SETTLEMENT, ROAD
- **StructureType:**    ROAD, SETTLEMENT, CITY
- **ResourceType:**    LUMBER, BRICK, GRAIN, WOOL, ORE
- **TerrainType:**    FOREST, HILLS, FIELDS, PASTURE, MOUNTAINS, DESERT
- **WeatherType:**    SUNNY, CLOUDY, RAINY, SNOWY, STORMY, FOG

---

## API Usage

**API Name:** Open-Meteo Weather API

**Documentation:** https://open-meteo.com/en/docs

**How it is used in the program:**
Our game will use the Open-Meteo Weather API to retrieve real-time weather conditions and incorporate them into the gameplay.

- The game will provide a list of predefined cities (e.g., Toronto, London, Tokyo, and New York) for players to choose from
- Each city will have predefined coordinates (latitude and longitude), which will be used to request its current weather conditions from the API
- The API will be called once when the game starts, and the retrieved weather conditions will remain fixed throughout the game
- The program will use the weather code returned by the API to classify the weather into five main categories. Each category will double the production of one of the 5 resources:
    - **Sunny (WMO code 0–1):** Lumber production ×2
    - **Cloudy (WMO code 2–3):** Brick production ×2
    - **Rainy (WMO codes 51–67, 80–82):** Grain production ×2
    - **Snowy (WMO codes 71–77, 85–86):** Wool production ×2
    - **Stormy (WMO codes 95–99):** Ore production ×2
- Other weather conditions, such as fog (WMO codes 45 and 48), will not provide any additional resource bonuses
- Weather bonuses will only apply when the corresponding resource-producing tiles are activated by the dice. Normal resource production will remain unchanged for all other resources
- If the API request fails, the game will continue with normal resource production

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

1. Open the GitHub repository and click the green **Code** button
2. Select **HTTPS** and copy the repository URL
3. Open IntelliJ IDEA and select **Clone Repository**
4. Paste the copied URL into the **URL** field, choose a local directory, and click **Clone**
5. Wait for IntelliJ IDEA to open the project and load the Maven dependencies
6. Go to **File → Project Structure → Project** and make sure the Project SDK is set to **JDK 26**
7. Locate the main Java class containing the `main()` method
8. Run the `main()` method to launch the game in a Java Swing window

### Weather API

An internet connection is required to retrieve weather data when starting a new game. If the API request fails or the weather condition does not have an assigned bonus, the game will continue with normal resource production.


---

## Notes

[Any additional project information.]
