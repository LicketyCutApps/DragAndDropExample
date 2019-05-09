/*
 * Copyright 2019 Adam Claflin [adam.r.claflin@gmail.com].
 *
 * Licensed under the Attribution-NonCommercial 4.0 International (CC BY-NC 4.0);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.licketycut.draganddropexample.DragAndDrop;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Custom {@link View.OnTouchListener  } to process touch events and move a view,
 * recognize swipe gestures with {@link GestureDetector } and make callbacks as appropriate.
 */
public class DragAndDropTouchListener implements View.OnTouchListener {
    private static final String TAG="DragAndDropTouchListener";

    // Public flags to indicate which movements and gestures to handle.
    public static final int IGNORE_HORIZONTAL =0x01;
    public static final int IGNORE_VERTICAL =0x10;
    public static final int IGNORE_GESTURES =0x100;
    public static final int NO_SNAP_BACK =0x1000;

    // Handler and Runnable to detect long presses.
    private final Handler handler = new Handler();
    private Runnable longPressHandler;

    private final GestureDetector gestureDetector;

    private View boundaryView =null;

    private ViewGroup parentView =null;
    private Integer overlapTag =null;

    // Switches to indicate what we will process.
    private boolean processX =true;
    private boolean processY =true;
    private boolean processGestures =true;
    private boolean snapBack =true;

    /** Default constructor which handles horizontal and vertical movements and gestures. */
    public DragAndDropTouchListener(Context context){
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    /** Constructor for specifying process flags only. */
    public DragAndDropTouchListener(Context context, int flags){
        this(context);
        if((flags & IGNORE_HORIZONTAL) == IGNORE_HORIZONTAL){
            processX =false;
        }

        if((flags & IGNORE_VERTICAL) == IGNORE_VERTICAL){
            processY =false;
        }

        if((flags & IGNORE_GESTURES) == IGNORE_GESTURES){
            processGestures = false;
        }

        if((flags & NO_SNAP_BACK) == NO_SNAP_BACK){
            snapBack = false;
        }
    }

    /**
     * Constructor for specifying params only.
     */
    public DragAndDropTouchListener(Context context, DragAndDropParams params){
        this(context);
        boundaryView = params.getBoundaryView();
        parentView =params.getParentView();
        overlapTag = params.getOverlapTag();
    }

    /**
     * Constructor for specifying params and process flags.
     */
    public DragAndDropTouchListener(Context context, DragAndDropParams params, int flags){
        this(context, flags);
        boundaryView = params.getBoundaryView();
        parentView =params.getParentView();
        overlapTag = params.getOverlapTag();
    }


    /** Start handler for delayed callback to test for long press. */
    private void startLongPressHandler(final View view){
        // If the user has touched and held for longer than our longPress threshold
        // without moving then set our switch and callback OnLongPress.
        longPressHandler = new Runnable() {
            @Override
            public void run() {
                view.performLongClick();
                onLongPress(view);
            }
        };
        handler.postDelayed(longPressHandler, LONG_PRESS_ACTION_THRESHOLD);
    }

    private void stopLongPressHandler(){
        handler.removeCallbacks(longPressHandler);
        longPressHandler = null;
    }

    // We only want to move the view if the user has actually dragged it a bit
    // and not just touched it.
    private boolean hasMoved = false;
    // Initial x and y of the parent View.
    private float initX, initY;
    // Raw x and y values of users initial ACTION_DOWN touch event.
    private float initTouchX, initTouchY;
    // The threshold of pixel variance that we are looking for as an intention to drag.
    private static final int MOVEMENT_ACTION_THRESHOLD = 32;

    // Last time the user began an ACTION_DOWN touch event.
    private long lastTouchDown;
    // If we receive ACTION_DOWN followed by ACTION_UP within the threshold
    // we'll consider it a click event.
    private static final int CLICK_ACTION_THRESHOLD = 200;
    private static final int LONG_PRESS_ACTION_THRESHOLD = 800;

    private DragAndDropViews otherViews;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(hasMoved){
            // If we have a new event and the view has been moved
            // then stop the runnable we have scheduled to test for long press.
            stopLongPressHandler();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // User has started a chain of touch events by touching down.
                lastTouchDown = System.currentTimeMillis();
                hasMoved = false;
                initX = v.getTranslationX();
                initY = v.getTranslationY();
                initTouchX = event.getRawX();
                initTouchY = event.getRawY();

                otherViews =new DragAndDropViews(v, parentView, overlapTag, true);

                startLongPressHandler(v);

                if(processGestures){
                    gestureDetector.onTouchEvent(event);
                }

                // Issue onTouch callback.
                onTouch(v);

                return true;
            case MotionEvent.ACTION_UP:
                // User has stopped touching.
                if (hasMoved) {
                    if(processGestures && !gestureDetector.onTouchEvent(event)) {
                        // Process gestures as requested.
                        if(otherViews.isOverlapping(v)){
                            // Callback OnDrop with last overlapping view.
                            View overlappingView =otherViews.removeOverlappingView();
                            if (onDrop(v,true, overlappingView)){
                                // Event has been processed and consumed, so finish.
                                return true;
                            }
                        } else{
                            // Callback OnDrop with no overlapping view.
                            if(onDrop(v, false, null)){
                                // Event has been processed and consumed, so finish.
                                return true;
                            }
                        }

                        if(snapBack) {
                            // If the event hasn't been processed yet,
                            // return to the initial coordinates as requested.
                            if (processX) {
                                v.setTranslationX(initX);
                            }
                            if (processY) {
                                v.setTranslationY(initY);
                            }
                        }

                    }
                } else{
                    // If user has stopped touching before the threshold,
                    // stop the runnable we have scheduled to test for long press.
                    stopLongPressHandler();
                    if(System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHOLD){
                        // If the user has touched and released within our click threshold,
                        // forward the click to the view.
                        v.performClick();
                        // Event has been processed so finish.
                        return onClick(v);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // User is currently moving the view.
                if (Math.abs(initTouchX - event.getRawX()) > MOVEMENT_ACTION_THRESHOLD
                        || Math.abs(initTouchY - event.getRawY()) > MOVEMENT_ACTION_THRESHOLD) {
                    // We've been dragged far enough to consider it an intentional drag event.
                    hasMoved = true;

                    // Calculate the next x and y positions based on movement
                    // relative to the initial touch.
                    int nextX = (int) (initX + (event.getRawX() - initTouchX));
                    int nextY = (int) (initY + (event.getRawY() - initTouchY));

                    // Set and update the new x and y of the view as requested.
                    if (processX) {
                        if (xWithinBounds(v, boundaryView, nextX)) {
                            v.setTranslationX(nextX);
                        }
                    }
                    if (processY) {
                        if (yWithinBounds(v, boundaryView, nextY)) {
                            v.setTranslationY(nextY);
                        }
                    }

                    if (otherViews.isOverlapping(v)) {
                        // We are overlapping another view.
                        if (otherViews.wasOverlapping()) {
                            // If we were overlapping another, callback onStopOverlap
                            // with last overlapped view.
                            onStopOverlap(v, otherViews.removeWasOverlapping());
                        }

                        // Callback OnOverlap with overlapping view.
                        View overlappingView = otherViews.getOverlappingView();
                        if (onOverlap(v, overlappingView)) {
                            // Event has been processed and consumed, so finish.
                            return true;
                        }
                    } else if (otherViews.wasOverlapping()) {
                        // If we were overlapping a view but aren't anymore,
                        // callback onStopOverlap with last overlapped view.
                        View overlappingView = otherViews.removeWasOverlapping();
                        if (onStopOverlap(v, overlappingView)) {
                            // Event has been processed and consumed, so finish.
                            return true;
                        }
                    }


                    if (processGestures) {
                        // Process gestures as requested and finish.
                        return gestureDetector.onTouchEvent(event);
                    }
                    // Event has been processed and consumed, so finish.
                    return true;
                }
                break;
        }
        // Return false if we haven't consumed the event so it propagates to other handlers
        // Returning true tells os that we've handled it.
        return hasMoved;
    }

    /** Test if the view with a new x coordinate remains inside of the boundary view. */
    private boolean xWithinBounds(View v, View boundaryView, float x){
        if(boundaryView == null){
            // No boundary has been set so there is nothing to check.
            return true;
        }
        float right =x+v.getWidth();
        return x > boundaryView.getTranslationX()
                && right < boundaryView.getTranslationX()+ boundaryView.getWidth();
    }

    /** Test if the view with a new y coordinate remains inside of the boundary view. */
    private boolean yWithinBounds(View v, View boundaryView, float y){
        if(boundaryView == null){
            return true;
        }
        float bottom =y+v.getHeight();
        return y > boundaryView.getTranslationY()
                && bottom < boundaryView.getTranslationY()+ boundaryView.getHeight();
    }

    /** Detect user Fling events and make callbacks as necessary. */
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        // The velocity and movement thresholds which we recognize as swipes.
        private static final int SWIPE_THRESHOLD = 8;
        private static final int SWIPE_VELOCITY_THRESHOLD = 16;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;

            // Calculate the difference in x,y coordinates between event 1 and event 2.
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            
            if (Math.abs(diffX) > Math.abs(diffY)) {
                // If the difference in x is greater than y, check for horizontal gestures.
                if (Math.abs(diffX) > SWIPE_THRESHOLD
                        && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // If the movement and velocity have met the threshold criteria,
                    // then make OnSwipe callbacks.
                    if (diffX > 0) {
                        result = onSwipeRight();
                    } else {
                        result = onSwipeLeft();
                    }
                }
            } else { 
                // If the difference in y is greater than x, check for vertical gestures.
                if (Math.abs(diffY) > SWIPE_THRESHOLD
                        && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    // If the movement and velocity have met the threshold criteria,
                    // then make OnSwipe callbacks.
                    if (diffY > 0) {
                        result = onSwipeDown();
                    } else {
                        result = onSwipeUp();
                    }
                }
            }
            // Return false if we haven't consumed the event so it propagates to other handlers.
            // Returning true indicates that we've handled it.
            return result;
        }
    }

    /** Default touch event and gesture callbacks
     * which return false indicating we have not acted on the events */
    protected boolean onClick(View view) { return false; }

    protected boolean onLongPress(View view) { return false; }

    protected boolean onTouch(View view) { return false; }

    protected boolean onDrag(View view) { return false; }

    protected boolean onDrop(View view, boolean wasOverlapping, View overlappingView )
    { return false; }

    protected boolean onOverlap(View view, View overlappingView) { return false; }

    protected boolean onStopOverlap(View view, View overlappingView) { return false; }

    protected boolean onSwipeRight() { return false; }

    protected boolean onSwipeLeft() { return false; }

    protected boolean onSwipeUp() { return false; }

    protected boolean onSwipeDown() { return false; }
}

