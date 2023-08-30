package be.swsb.coderetreat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class RoverTest {

    @Test
    fun `default Rover has starting position at 00 and facing North`() {
        assertThat(Rover()).isEqualTo(Rover(Position(0, 0), Direction.North))
    }

    @Test
    fun `A default Rover can move forwards when receiving f`() {
        Rover()
                .receive(Command.Forward).also { assertThat(it).isEqualTo(Rover(Position(0, 1))) }
                .receive(Command.Forward, Command.Forward).also { assertThat(it).isEqualTo(Rover(Position(0, 3))) }
    }

    @Test
    fun `Rover can move forwards`() {
        assertThat(Rover().receive(Command.Forward)).isEqualTo(Rover(Position(0, 1)))
        assertThat(Rover(direction = Direction.East).receive(Command.Forward)).isEqualTo(Rover(position = Position(1, 0), direction = Direction.East))
        assertThat(Rover(direction = Direction.South).receive(Command.Forward)).isEqualTo(Rover(position = Position(0, -1), direction = Direction.South))
        assertThat(Rover(direction = Direction.West).receive(Command.Forward)).isEqualTo(Rover(position = Position(-1, 0), direction = Direction.West))
    }

    @Test
    fun `Rover facing north turns right when receiving r`() {
        Rover()
                .receive(Command.Right).also { assertThat(it).isEqualTo(Rover(direction = Direction.East)) }
                .receive(Command.Right).also { assertThat(it).isEqualTo(Rover(direction = Direction.South)) }
                .receive(Command.Right).also { assertThat(it).isEqualTo(Rover(direction = Direction.West)) }
                .receive(Command.Right).also { assertThat(it).isEqualTo(Rover(direction = Direction.North)) }

    }

    @Test
    fun `Rover can ride around on a small planet`() {
        val rover = Rover(planet = Planet(2, 2)).receive(Command.Forward, Command.Forward)
        assertThat(rover).isEqualTo(Rover(planet = Planet(2, 2)))
    }

    @Test
    fun `A rover should not drive over an obstacle`() {
        val planetWithObstacle = Planet(100, 100, listOf(Obstacle(0, 1)))
        val rover = Rover(planet = planetWithObstacle).receive(Command.Forward)
        assertThat(rover).isEqualTo(Rover(planet = planetWithObstacle))
    }

    @Test
    fun `A rover should ignore all following commands after encountering an obstacle`() {
        val planetWithObstacle = Planet(100, 100, listOf(Obstacle(0, 1)))
        val rover = Rover(planet = planetWithObstacle).receive(Command.Forward, Command.Right)
        assertThat(rover).isEqualTo(Rover(planet = planetWithObstacle))
    }
}

typealias Obstacle = Position

data class Planet(private val maxX: Int, private val maxY: Int, val obstacles: List<Obstacle> = emptyList()) {
    fun bound(position: Position): Position {
        return Position(position.x % (maxX - 1), position.y % (maxY - 1))
    }

}

enum class Direction {
    North,
    East,
    South,
    West

}

data class Rover(
        private val position: Position = Position(0, 0),
        private val direction: Direction = Direction.North,
        private val planet: Planet = Planet(100, 100)
) {
    fun receive(vararg commands: Command): Rover {
        return commands.fold(this) { acc, command ->
            when (command) {
                Command.Forward -> acc.moveForward()
                Command.Right -> acc.turnRight()
            }

        }
    }

    private fun turnRight(): Rover = when (this.direction) {
        Direction.North -> copy(direction = Direction.East)
        Direction.East -> copy(direction = Direction.South)
        Direction.South -> copy(direction = Direction.West)
        Direction.West -> copy(direction = Direction.North)
    }

    private fun moveForward(): Rover = when (this.direction) {
        Direction.North -> this.position + Position(0, 1)
        Direction.East -> this.position + Position(1, 0)
        Direction.South -> this.position + Position(0, -1)
        Direction.West -> this.position + Position(-1, 0)
    }
            .boundBy(planet)
            .unlessObstacle(this.position)
            .let { copy(position = it) }

    private fun Position.boundBy(planet: Planet) = planet.bound(this)
    private fun Position.unlessObstacle(fallBackPosition: Position) =
            if (this in planet.obstacles) fallBackPosition
            else this
}

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
}

enum class Command {
    Forward,
    Right,
}