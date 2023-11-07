package fr.antek.taleviewer;

import android.graphics.Bitmap;

/**
 * Represents a page within a document or image file, providing functionality to handle splitting and navigation.
 */
public class Page {
    private final File parentFile; // The parent file containing this page.
    private final ImageActivity activity; // The activity associated with this page.
    private String splitStep; // The current split step used for page processing.
    private int pageNumber; // The page number within the document or image.
    private final Settings settings; // Application settings.
    private Boolean doSplit = null; // Flag to determine if splitting is necessary.

    /**
     * Constructs a Page object.
     * @param parentFile The parent file containing this page.
     * @param activity The ImageActivity associated with this page.
     * @param splitStep The current split step for page processing.
     * @param pageNumber The page number within the document or image.
     */
    public Page(File parentFile, ImageActivity activity, String splitStep, int pageNumber) {
        this.parentFile = parentFile;
        this.activity = activity;
        this.settings = activity.getSettings();
        if (splitStep != null){
            if (splitStep.equals("lastPossible")){
                if (settings.getFullAfter()) {
                    this.splitStep = "fullLast";
                } else {
                    this.splitStep = "halfLast";
                }
            }else if (splitStep.equals("fullFirst") || splitStep.equals("halfFirst") || splitStep.equals("fullBetween") || splitStep.equals("halfLast") || splitStep.equals("fullLast")){
                this.splitStep = splitStep;
            }else{
                if (settings.getFullBefore()) {
                    this.splitStep = "fullFirst";
                } else {
                    this.splitStep = "halfFirst";
                }
            }
        }else{
            if (settings.getFullBefore()) {
                this.splitStep = "fullFirst";
            } else {
                this.splitStep = "halfFirst";
            }
        }
        if (parentFile instanceof PDF){
            if (pageNumber == -1){
                this.pageNumber = ((PDF)parentFile).getPdfRenderer().getPageCount();
            }else{
                this.pageNumber = pageNumber;
            }
            ((PDF)parentFile).openPage(this.pageNumber);
        }

        if (parentFile instanceof Image){
            if (!((Image) parentFile).getOpen()){
                ((Image) parentFile).open();
            }
        }
    }

    /**
     * Retrieves the bitmap representation of the page.
     * @return The Bitmap representing the page's content.
     */
    public Bitmap getBitmap(){
        if (doSplit == null) {
            findSplit();
        }
        Bitmap resultBitmap;
        Bitmap bitmapRaw;
        if (parentFile instanceof Image){
            bitmapRaw = ((Image) parentFile).getBitmapRaw();
        }else if (parentFile instanceof PDF){
            PDFPage pdfPage = ((PDF) parentFile).getPage(pageNumber);
            bitmapRaw = pdfPage.getBitmapRaw();
        }else{
            bitmapRaw = null;
        }
        if (doSplit){
            switch (splitStep) {
                case "fullFirst", "fullBetween", "fullLast" -> resultBitmap = bitmapRaw;
                case "halfFirst" ->
                        resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPageRight(), settings.getOverlap());
                case "halfLast" ->
                        resultBitmap = BitmapUtility.splitPage(bitmapRaw, !settings.getFirstPageRight(), settings.getOverlap());
                default -> {
                    if (settings.getFullBefore()) {
                        resultBitmap = bitmapRaw;
                        splitStep = "fullFirst";
                    } else {
                        resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPageRight(), settings.getOverlap());
                        splitStep = "halfFirst";
                    }
                }
            }
        }else{
            resultBitmap = bitmapRaw;
        }

        return resultBitmap;
    }

    /**
     * Determines whether splitting is needed for the page based on its width and application settings.
     */
    public void findSplit() {
        Boolean wide;
        if (parentFile instanceof Image){
            wide = ((Image) parentFile).getWide();
        }else if (parentFile instanceof PDF){
            wide = ((PDF) parentFile).getPage(pageNumber).getWide();
        }else{
            wide = false;
        }
        doSplit = (wide && settings.getSplitPage());
    }

    /**
     * Retrieves the previous page (if any) based on the current split step.
     * @return The previous page or null if not applicable.
     */
    public Page getPrevPage(){
        if (doSplit == null) {
            findSplit();
        }
        if (doSplit) {
            switch (splitStep) {
                case "fullFirst" -> {
                    return getPrevImage();
                }
                case "halfFirst" -> {
                    if (settings.getFullBefore()) {
                        return new Page(parentFile, activity, "fullFirst", pageNumber);
                    } else {
                        return getPrevImage();
                    }
                }
                case "fullBetween" -> {
                    return new Page(parentFile, activity, "halfFirst", pageNumber);
                }
                case "halfLast" -> {
                    if (settings.getFullBetween()) {
                        return new Page(parentFile, activity, "fullBetween", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfFirst", pageNumber);
                    }
                }
                case "fullLast" -> {
                    return new Page(parentFile, activity, "halfLast", pageNumber);
                }
                default -> {
                    if (settings.getFullAfter()) {
                        return new Page(parentFile, activity, "fullLast", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfLast", pageNumber);
                    }
                }
            }
        }else{
            return getPrevImage();
        }
    }

    /**
     * Retrieves the previous page based on the current split step.
     * @return The previous page or null if not applicable.
     */
    public Page getPrevImage(){
        if (parentFile instanceof Image){
            File prevFile = parentFile.getPrev();
            if (prevFile == null){
                return null;
            }else{
                return new Page(prevFile, activity,  "lastPossible", -1);
            }
        }else if (parentFile instanceof PDF){
            if (pageNumber == 1){
                File prevFile = parentFile.getPrev();
                if (prevFile == null){
                    return null;
                }else{
                    return new Page(prevFile, activity,  "lastPossible", -1);
                }
            }else{
                return new Page(parentFile, activity,  "lastPossible", pageNumber-1);
            }
        }else{
            return null;
        }
    }

    /**
     * Retrieves the next page (if any) based on the current split step.
     * @return The next page or null if not applicable.
     */
    public Page getNextPage(){
        if (doSplit == null) {
            findSplit();
        }
        if (doSplit) {
            switch (splitStep) {
                case "fullFirst" -> {
                    return new Page(parentFile, activity, "halfFirst", pageNumber);
                }
                case "halfFirst" -> {
                    if (settings.getFullBetween()) {
                        return new Page(parentFile, activity, "fullBetween", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfLast", pageNumber);
                    }
                }
                case "fullBetween" -> {
                    return new Page(parentFile, activity, "halfLast", pageNumber);
                }
                case "halfLast" -> {
                    if (settings.getFullAfter()) {
                        return new Page(parentFile, activity, "fullLast", pageNumber);
                    } else {
                        return getNextImage();
                    }
                }
                case "fullLast" -> {
                    return getNextImage();
                }
                default -> {
                    if (settings.getFullBefore()) {
                        return new Page(parentFile, activity, "fullFirst", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfFirst", pageNumber);
                    }
                }
            }
        }else{
            return getNextImage();
        }
    }

    /**
     * Retrieves the next page based on the current split step.
     * @return The next page or null if not applicable.
     */
    public Page getNextImage(){
        if (parentFile instanceof Image){
            File nextFile = parentFile.getNext();
            if (nextFile == null){
                return null;
            }else{
                return new Page(nextFile, activity,  "firstPossible", 1);
            }
        }else if (parentFile instanceof PDF){
            if (pageNumber == ((PDF)parentFile).getPdfRenderer().getPageCount()){
                File nextFile = parentFile.getNext();
                if (nextFile == null){
                    return null;
                }else{
                    return new Page(nextFile, activity,  "firstPossible", 1);
                }
            }else{
                return new Page(parentFile, activity,  "firstPossible", pageNumber+1);
            }
        }else{
            return null;
        }
    }

    public String getSplitStep() {
        return splitStep;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public File getParentFile() {
        return parentFile;
    }


}
