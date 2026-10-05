package io.github.danielcampossantos.hydrationtracker.model;

public class Intake {
    private int volume;
    private long timestamp;

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
