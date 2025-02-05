package exoPlanet.exoPlanet;

public enum Planet {
    DEFAULT(0),
    IO(1),
    PANDORA(2);

    private final int id;

    Planet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Planet fromId(int id) {
        for (Planet planet : Planet.values()) {
            if (planet.id == id) {
                return planet;
            }
        }
        throw new IllegalArgumentException("Unknown planet ID: " + id);
    }
}

