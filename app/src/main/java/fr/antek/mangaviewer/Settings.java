package fr.antek.mangaviewer;

public class Settings {
    private boolean splitPage = false;
    private boolean firstPage = true;
    private boolean fullBefore = false;
    private boolean fullBetween = false;
    private boolean fullAfter = false;
    private int overlap = 0;
    private boolean scroll  = false;

    public boolean getSplitPage() {
        return splitPage;
    }

    public void setSplitPage(boolean splitPage) {
        this.splitPage = splitPage;
    }

    public boolean getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(boolean firstPage) {
        this.firstPage = firstPage;
    }

    public boolean getFullBefore() {
        return fullBefore;
    }

    public void setFullBefore(boolean fullBefore) {
        this.fullBefore = fullBefore;
    }

    public boolean getFullBetween() {
        return fullBetween;
    }

    public void setFullBetween(boolean fullBetween) {
        this.fullBetween = fullBetween;
    }

    public boolean getFullAfter() {
        return fullAfter;
    }

    public void setFullAfter(boolean fullAfter) {
        this.fullAfter = fullAfter;
    }

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public boolean getScroll() {
        return scroll;
    }

    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }
}
