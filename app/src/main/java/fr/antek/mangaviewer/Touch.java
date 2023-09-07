package fr.antek.mangaviewer;

public class Touch {
    private final int id;
    private final float startX;
    private final float startY;
    private float currentX;
    private float currentY;

    public Touch(int id, float startX, float startY) {
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.currentX = startX;
        this.currentY = startY;
    }

    public void setCurrentX(int currentX) {
        this.currentX = currentX;
    }

    public void setCurrentY(int currentY) {
        this.currentY = currentY;
    }

    public int getId() {
        return id;
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }
}
