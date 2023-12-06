package fr.antek.taleviewer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

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
                bitmapRaw = generateTextBitmap(getDoc().getName() + " " + getActivity().getString(R.string.ErrorOpen), 720, 1280);
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

    /**
     * Generates a bitmap with specified width and height, containing centered text.
     * This function creates a bitmap with a black background and renders the given text in white color
     * at the center of the bitmap. The text will be automatically wrapped onto multiple lines
     * if it exceeds the specified width.
     *
     * @param text   The text to be rendered on the bitmap.
     * @param width  The width of the generated bitmap.
     * @param height The height of the generated bitmap.
     * @return A Bitmap object containing the rendered text.
     */
    public static Bitmap generateTextBitmap(String text, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.BLACK);

        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);

        StaticLayout staticLayout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        canvas.save();
        canvas.translate(0, (float) ((height - staticLayout.getHeight()) / 2.0));
        staticLayout.draw(canvas);
        canvas.restore();

        return bitmap;
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
