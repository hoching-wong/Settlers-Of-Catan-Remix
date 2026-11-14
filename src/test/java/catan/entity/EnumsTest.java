package catan.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class EnumsTest {
    @Test
    void gamePhaseValues() {
        assertArrayEquals(new GamePhase[] {GamePhase.SETUP, GamePhase.PLAYING, GamePhase.GAME_OVER},
                GamePhase.values());
    }

    @Test
    void setupStepValues() {
        assertArrayEquals(new SetupStep[] {SetupStep.SETTLEMENT, SetupStep.ROAD}, SetupStep.values());
    }

    @Test
    void structureTypeValues() {
        assertArrayEquals(new StructureType[] {StructureType.ROAD, StructureType.SETTLEMENT, StructureType.CITY},
                StructureType.values());
    }

    @Test
    void resourceTypeValues() {
        assertArrayEquals(new ResourceType[] {ResourceType.LUMBER, ResourceType.BRICK, ResourceType.GRAIN,
                ResourceType.WOOL, ResourceType.ORE}, ResourceType.values());
    }

    @Test
    void terrainTypeValues() {
        assertArrayEquals(new TerrainType[] {TerrainType.FOREST, TerrainType.HILLS, TerrainType.FIELDS,
                TerrainType.PASTURE, TerrainType.MOUNTAINS, TerrainType.DESERT}, TerrainType.values());
    }

    @Test
    void weatherTypeValues() {
        assertArrayEquals(new WeatherType[] {WeatherType.SUNNY, WeatherType.CLOUDY, WeatherType.RAINY,
                WeatherType.SNOWY, WeatherType.STORMY, WeatherType.FOG}, WeatherType.values());
    }
}
