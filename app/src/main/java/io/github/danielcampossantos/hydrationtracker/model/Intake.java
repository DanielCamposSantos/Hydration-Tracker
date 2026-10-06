package io.github.danielcampossantos.hydrationtracker.model;

public class Intake {
    private final int volume;
    private final long timestamp;

    public Intake(int volume, long timestamp) {
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public int getVolume() {
        return volume;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
