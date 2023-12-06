package fr.antek.taleviewer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;


/**
 * Represents an image file in the application's file system, extending the File class.
 */
public class Image extends File{
    private Bitmap miniature = null; // A miniature version of the image.
    private Bitmap bitmapRaw; // The full-resolution raw image.
    private Boolean isWide; // Indicates whether the image is wide or not.
    private Boolean isOpen = false; // Flag to indicate whether the image is currently open.

    /**
     * Constructs an Image object.
     * @param activity The parent AppCompatActivity.
     * @param parentPath The path of the parent directory.
     * @param docFile The DocumentFile representing this image file.
     * @param parentFile The parent directory.
     */
    public Image(AppCompatActivity activity, String parentPath, DocumentFile docFile, Directory parentFile) {
        super(activity, parentPath, docFile, parentFile);
    }

    /**
     * Opens the image file, loading its raw content.
     * This method should be called before accessing the image data.
     * @throws RuntimeException if there is an error opening the image.
     */
    public void open() {
        try {
            bitmapRaw = MediaStore.Images.Media.getBitmap(super.getActivity().getContentResolver(), super.getDoc().getUri());
            if (bitmapRaw == null){
                bitmapRaw = BitmapUtility.generateTextBitmap(getDoc().getName() + " " + getActivity().getString(R.string.ErrorOpen), 720, 1280);
            }
            isWide = bitmapRaw.getWidth()>bitmapRaw.getHeight();
            isOpen = true;


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the image file, releasing any loaded image data.
     */
    public void close(){
        bitmapRaw = null;
        isOpen = false;
    }

    /**
     * Gets the full-resolution raw image.
     * If the image is not open, it opens it first.
     * @return The full-resolution raw image Bitmap.
     */
    public Bitmap getBitmapRaw() {
        if (!isOpen){
            open();
        }
        return bitmapRaw;
    }

    public Uri getUri(){
        return super.getDoc().getUri();
    }

    public void setMiniature(Bitmap miniature) {
        this.miniature = miniature;
    }

    public Bitmap getMiniature() {
        return miniature;
    }

    public Boolean getWide() {
        return isWide;
    }

    public Boolean getOpen() {
        return isOpen;
    }
}
