package fr.antek.mangaviewer;


import android.view.MotionEvent;

import java.util.ArrayList;

public class TouchAnalyser {
    private ArrayList<Touch> touchList = new ArrayList<>();

    public void touchHandler(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            addTouch(event);
        }else if(event.getAction() == MotionEvent.ACTION_UP) {
            int pointerIndex = event.getActionIndex();
            removeTouch(event.getPointerId(pointerIndex));
        }
    }

    public void addTouch(MotionEvent event){
        int pointerIndex = event.getActionIndex();
        touchList.add(new Touch(event.getPointerId(pointerIndex), event.getX(), event.getY()));
    }

    public void removeTouch(int id){
        for (Touch touch : touchList){
            if (touch.getId() == id){
                touchList.remove(touch);
            }
        }
    }

    public ArrayList<Touch> getTouchList() {
        return touchList;
    }
}
