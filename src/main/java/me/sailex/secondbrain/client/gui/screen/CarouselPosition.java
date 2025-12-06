package me.sailex.secondbrain.client.gui.screen;

public enum CarouselPosition {
    LEFT(-1),
    CENTER(0),
    RIGHT(1);

    private final int position;

    CarouselPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
