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

A multiplayer strategy board game where players compete to build and expand their civilizations. Place roads, settlements (1 point each) and cities (2 points) and roll dices to gather resources. Build more infrastructure, buy developement cards, block other players, steal resources and be the first to 10 points.

---

## User Stories

### User Story 1
As a player, I want to place roads, settlements and cities so I can expand my civilization.

### User Story 2
As a player, I want to roll dice and receive resources based on the board so I can build more structures.

### User Story 3
As a player, I want to be able to start a game on the same device with 2-4 and be able to play on the same board, switching turns.

### User Story 4
As a player, I want to be able to select a city from the pre-determined list before starting the game so that weather can influence conditions in the game.

### User Story 5
As a player, I want to see everyone's current total points, how much of each resource they have and the weather conditions.

### User Story 6
As a player, I want a history section so I can look back on previous moves made by all players.

### User Story 7
As a player, I want to be able to trade with players and the bank to get other resources for different prices.

### User Story 8
As a player, I want to move the robber and steal 1 random resource from another play adjacent to the robber's hex.

### User Story 9
As a player, I want to get the conditional 2 points if I have the longest road (minimum 5 roads)

### User Story 10
As a player, I want to win the game (stop the game) if someone reaches 10 points

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
6. The system calculates the resources each eligible player should receive
7. The system adds the resources to each player's inventory
8. The system updates the resource counts displayed on the screen
9. The system allows the active player to continue their turn

Alternate path:
4. If the dice result is 7, no resources are distributed, and the robber action is triggered
5. If a hex is blocked by the robber, players do not receive resources from that hex
5. If no players have settlements or cities adjacent to the matching hexes, no resources are distributed

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
1.The system prompts the player to select the number of players (2–4)
2. The system prompts each player to enter their name
3. The player selects the desired number of players
4. The system prompts each player to enter their name.
5. Each player enters their name and confirms their selection
6. The system initializes a shared game board
7. The system assigns each player a starting position 
8. The system determines the order of turns 
9. The system displays the game board and indicates which player's turn it is 
10. Players take turns interacting with the same board 
11. After a player's turn ends, the system switches control to the next player

Alternate path:
2. If the player selects fewer than 2 or more than 4 players, the system does not allow the selection.
4. If a player enters an empty or invalid name, the system prompts them to enter a valid name.
5. If two players enter the same name, the system prompts them to choose different names.
10. If a player attempts to perform an action during another player's turn, the system prevents the action.

Post-conditions:
- A game with 2–4 players is successfully initialized
- All players share the same game board 
- The system tracks whose turn it is and switches turns accordingly

### Use Case 4
Primary actor: Player
Goal: Allows players to select a city from a predetermined list before starting the game so that the city's weather conditions can influence gameplay
Preconditions:
- The game has been launched
- Players are setting up a new game

Main path:
1. A player opens the city selection menu 
2. The system displays a predetermined list of available cities 
3. The player selects a city from the list 
4. The system retrieves the current weather conditions for the selected city 
5. The system displays the selected city and its weather information 
6. The player confirms their city selection 
7. The system saves the selected city and its weather conditions 
8. The player proceeds to start the game 
9. The system applies the corresponding weather effects to the game 
10. The game begins with the selected city's weather conditions influencing gameplay

Alternate path:
3. If the player does not select a city, the system prompts them to make a selection before continuing
4. If weather information cannot be retrieved, the system displays an error message and allows the player to retry
4. If weather information is unavailable, the system applies default weather conditions
6. If the player changes their mind, they can return to the city selection menu and select a different city

Post conditions:
- A city is selected and saved for the game 
- The corresponding weather conditions are retrieved and applied 
- The game begins with weather conditions that may influence gameplay

---

## Minimal Viable Product (MVP)

| Team Member | MVP Use Case |
|-------------|--------------|
| Alison Cai | Use Case 1 |
| Taito Sugiyama | Use Case 2 |
| James Wong | Use Case 3 |
| Chuqian Xiao | Use Case 4 |

---

## Entities

### Entity 1

### Entity 2

(Add additional entities as needed.)

---

## API Usage

**API Name:**

**Documentation:** [Link]

**How it is used in the program:**

---

## Design Diagrams

### UML Class Diagram


### Sequence Diagrams

---

## How to Run

[Instructions for running the program.]

---

## Notes

[Any additional project information.]
