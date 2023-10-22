package fr.antek.mangaviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Page {
    private File parentFile;
    private ImageActivity activity;
    private String splitStep;
    private int pageNumber;
    private Settings settings;
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
            if (splitStep.equals("fullFirst")) {
                resultBitmap = bitmapRaw;
            } else if (splitStep.equals("halfFirst")) {
                resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPage(), settings.getOverlap());
            } else if (splitStep.equals("fullBetween")) {
                resultBitmap = bitmapRaw;
            } else if (splitStep.equals("halfLast")) {
                resultBitmap = BitmapUtility.splitPage(bitmapRaw, !settings.getFirstPage(), settings.getOverlap());
            } else if (splitStep.equals("fullLast")) {
                resultBitmap = bitmapRaw;
            }else {
                if (settings.getFullBefore()) {
                    resultBitmap = bitmapRaw;
                    splitStep = "fullFirst";
                } else {
                    resultBitmap = BitmapUtility.splitPage(bitmapRaw, settings.getFirstPage(), settings.getOverlap());
                    splitStep = "halfFirst";
                }
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
            if (splitStep.equals("fullFirst")){
                return getFromPrevBitmap();
            }else if(splitStep.equals("halfFirst")) {
                if (settings.getFullBefore()){
                    return new Page(parentFile, activity, "fullFirst", pageNumber);
                }else{
                    return getFromPrevBitmap();
                }
            }else if(splitStep.equals("fullBetween")) {
                return new Page(parentFile, activity, "halfFirst", pageNumber);
            }else if(splitStep.equals("halfLast")) {
                if (settings.getFullBetween()){
                    return new Page(parentFile, activity, "fullBetween", pageNumber);
                }else{
                    return new Page(parentFile, activity, "halfFirst", pageNumber);
                }
            }else if(splitStep.equals("fullLast")) {
                return new Page(parentFile, activity, "halfLast", pageNumber);
            }else{
                if (settings.getFullAfter()){
                    return new Page(parentFile, activity, "fullLast", pageNumber);
                }else{
                    return new Page(parentFile, activity,  "halfLast", pageNumber);
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
            if (splitStep.equals("fullFirst")){
                return new Page(parentFile, activity, "halfFirst", pageNumber);
            }else if(splitStep.equals("halfFirst")) {
                if (settings.getFullBetween()){
                    return new Page(parentFile, activity, "fullBetween", pageNumber);
                }else{
                    return new Page(parentFile, activity, "halfLast", pageNumber);
                }
            }else if(splitStep.equals("fullBetween")) {
                return new Page(parentFile, activity, "halfLast", pageNumber);
            }else if(splitStep.equals("halfLast")) {
                if (settings.getFullAfter()){
                    return new Page(parentFile, activity, "fullLast", pageNumber);
                }else{
                    return getFromNextBitmap();
                }
            }else if(splitStep.equals("fullLast")) {
                return getFromNextBitmap();
            }else{
                if (settings.getFullBefore()) {
                    return new Page(parentFile, activity, "fullFirst", pageNumber);
                } else {
                    return new Page(parentFile, activity,"halfFirst", pageNumber);
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
