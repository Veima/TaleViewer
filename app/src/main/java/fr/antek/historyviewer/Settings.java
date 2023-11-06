package fr.antek.historyviewer;

/**
 * Represents a configuration class for various display settings in the application.
 */
public class Settings {
    private boolean splitPage = false;      // Indicates whether to split pages for a two-page view.
    private boolean firstPageRight = true;  // Indicates whether the first is the Right.
    private boolean fullBefore = false;     // Indicates whether to show the full page before the center.
    private boolean fullBetween = false;    // Indicates whether to show the full page between the center pages.
    private boolean fullAfter = false;      // Indicates whether to show the full page after the center.
    private int overlap = 0;                // The percentage of overlap for split pages.
    private boolean scroll = false;         // Indicates whether scrolling is enabled.


    public boolean getSplitPage() {
        return splitPage;
    }

    public void setSplitPage(boolean splitPage) {
        this.splitPage = splitPage;
    }

    public boolean getFirstPageRight() {
        return firstPageRight;
    }

    public void setFirstPageRight(boolean firstPage) {
        this.firstPageRight = firstPage;
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
