# Settlers of Catan Remix

Java 26, Maven, Swing. A local 2-4 player Catan variant with a weather-based resource bonus from the Open-Meteo API. The README is the source of truth for the game rules, use cases and entity signatures. Read it before changing any entity.

## Build and test

- `mvn test` runs the unit tests (JUnit 5).
- `mvn compile` compiles only.
- The project needs JDK 26. Check with `mvn -version`; it must report Java 26.

## Architecture: Clean Architecture

Dependencies point inward only: Frameworks -> Interface Adapters -> Use Cases -> Entities. An inner layer must never import from an outer layer.

Base package: `catan`

```
catan/
  entity/             Game, Board, Tile, Vertex, Edge, Player, ResourceHand, Building,
                      Settlement, City, Road, Dice, WeatherData, CityLocation,
                      BoardFactory, BuildCost, ProductionCalculator,
                      LongestRoadCalculator, and the enums
  usecase/
    port/             GameRepository, WeatherGateway (interfaces only)
    startgame/        UC3 + UC4
    placeinitial/     setup phase, snake order
    rolldice/         UC2
    build/            UC1 (also triggers UC6 and UC7)
    endturn/
    restartgame/      UC7 restart / exit
    gamestate/        UC5
  interfaceadapter/
    controller/
    presenter/
    viewmodel/
  framework/
    ui/swing/         views
    weather/          OpenMeteoWeatherGateway
    persistence/      InMemoryGameRepository
  app/                Main, AppBuilder (the only place concrete classes are wired together)
```

### Layer rules

- `entity`: plain Java only. No Swing, no HTTP, no JSON, no imports from any other package in this project.
- `usecase`: depends on `entity` and on its own interfaces. It must not import Swing, `java.net`, or anything from `interfaceadapter` or `framework`.
- Every use case package has the same five pieces: `InputBoundary`, `Interactor`, `InputData`, `OutputData`, `OutputBoundary`.
- Interactors never touch the UI. They report results through the `OutputBoundary`, which a presenter implements.
- Presenters update a view model. Swing views read the view model and call controllers. Views never call interactors or entities directly.
- Anything that talks to the outside world (HTTP, storage) is an interface in `usecase.port`, implemented in `framework`.
- Only `AppBuilder` creates concrete implementations and connects them.

## Game rules that are easy to get wrong

- Settlements and cities must be at least 2 edges from any other settlement or city, regardless of owner.
- Initial placement uses snake order and costs no resources. Each placement turn is one settlement followed by one adjacent road.
- A city can only be built by upgrading the player's own settlement.
- A dice total of 7 distributes no resources.
- Weather is fetched once at game start and never changes. If the API fails or the weather code has no category, production is normal.
- The weather bonus doubles only the one bonus resource, and only when a matching tile is rolled.
- Longest Road needs a continuous road of at least 5 segments. A tie keeps the current holder. An opponent's settlement or city on a road can break it.
- `Player.getVictoryPoints()` is computed from buildings plus 2 for Longest Road. Never store a separate point total.
- The first player to reach 10 points wins and the game ends immediately.

## Conventions

- Write JUnit 5 tests next to every new class, in `src/test/java` under the same package.
- Interactors are tested with fake `GameRepository` and `WeatherGateway` implementations, not real ones.
- Pass `Random` into `Dice` and `BoardFactory` so tests can use a seeded one.
- Do not change a shared entity's public method signature without checking the README and telling the team.
- Keep commits small. One use case per branch.
