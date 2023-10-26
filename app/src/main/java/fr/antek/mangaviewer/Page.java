package fr.antek.mangaviewer;

import android.graphics.Bitmap;

public class Page {
    private final File parentFile;
    private final ImageActivity activity;
    private String splitStep;
    private int pageNumber;
    private final Settings settings;
    private Boolean doSplit = null;

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
                case "fullFirst":
                case "fullBetween":
                case "fullLast":
                    resultBitmap = bitmapRaw;
                    break;
                case "halfFirst":
                    resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPage(), settings.getOverlap());
                    break;
                case "halfLast":
                    resultBitmap = BitmapUtility.splitPage(bitmapRaw, !settings.getFirstPage(), settings.getOverlap());
                    break;
                default:
                    if (settings.getFullBefore()) {
                        resultBitmap = bitmapRaw;
                        splitStep = "fullFirst";
                    } else {
                        resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPage(), settings.getOverlap());
                        splitStep = "halfFirst";
                    }
                    break;
            }
        }else{
            resultBitmap = bitmapRaw;
        }

        return resultBitmap;
    }

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

    public Page getPrevPage(){
        if (doSplit == null) {
            findSplit();
        }
        if (doSplit) {
            switch (splitStep) {
                case "fullFirst":
                    return getFromPrevBitmap();
                case "halfFirst":
                    if (settings.getFullBefore()) {
                        return new Page(parentFile, activity, "fullFirst", pageNumber);
                    } else {
                        return getFromPrevBitmap();
                    }
                case "fullBetween":
                    return new Page(parentFile, activity, "halfFirst", pageNumber);
                case "halfLast":
                    if (settings.getFullBetween()) {
                        return new Page(parentFile, activity, "fullBetween", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfFirst", pageNumber);
                    }
                case "fullLast":
                    return new Page(parentFile, activity, "halfLast", pageNumber);
                default:
                    if (settings.getFullAfter()) {
                        return new Page(parentFile, activity, "fullLast", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfLast", pageNumber);
                    }
            }
        }else{
            return getFromPrevBitmap();
        }
    }

    public Page getFromPrevBitmap(){
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

    public Page getNextPage(){
        if (doSplit == null) {
            findSplit();
        }
        if (doSplit) {
            switch (splitStep) {
                case "fullFirst":
                    return new Page(parentFile, activity, "halfFirst", pageNumber);
                case "halfFirst":
                    if (settings.getFullBetween()) {
                        return new Page(parentFile, activity, "fullBetween", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfLast", pageNumber);
                    }
                case "fullBetween":
                    return new Page(parentFile, activity, "halfLast", pageNumber);
                case "halfLast":
                    if (settings.getFullAfter()) {
                        return new Page(parentFile, activity, "fullLast", pageNumber);
                    } else {
                        return getFromNextBitmap();
                    }
                case "fullLast":
                    return getFromNextBitmap();
                default:
                    if (settings.getFullBefore()) {
                        return new Page(parentFile, activity, "fullFirst", pageNumber);
                    } else {
                        return new Page(parentFile, activity, "halfFirst", pageNumber);
                    }
            }
        }else{
            return getFromNextBitmap();
        }
    }

    public Page getFromNextBitmap(){
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
