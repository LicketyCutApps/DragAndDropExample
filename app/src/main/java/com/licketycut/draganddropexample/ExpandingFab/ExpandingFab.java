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

package com.licketycut.draganddropexample.ExpandingFab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.licketycut.draganddropexample.R;

import java.util.ArrayList;

/**
 * Custom class to create and manage an animated group of expanding {@link FloatingActionButton}s.
 */
public class ExpandingFab {

    private final String TAG ="ExpandingFab";

    // Open/Close toggle.
    private Boolean fabsOpen =false;
    private boolean animationActive =false;

    // Rotation animations, will be loaded from resources.
    private final Animation fab_open;
    private final Animation fab_close;

    // Listener to notify creator of Open/Close events.
    private ExpandingFabListener listener =null;

    private final ArrayList<FloatingActionButton> fabs;

    private final Context context;
    private final ViewGroup parent;
    private final int backgroundColor;
    private final int foregroundColor;

    /**
     *  Initialize class and create Fab which opens and closes the group with animations.
     *
     * @param context           Context required.
     * @param parent            Parent ViewGroup required.
     * @param drawable          Drawable resource for the Open/Close button.
     * @param backgroundColor   Background color int.
     * @param foregroundColor   Foreground color int.
     */
    public ExpandingFab(@NonNull Context context, @NonNull ViewGroup parent,
                        int drawable, int backgroundColor, int foregroundColor){
        this.context = context;
        this.parent = parent;
        this.backgroundColor =backgroundColor;
        this.foregroundColor = foregroundColor;

        // Initialize our list of Fabs.
        fabs =new ArrayList<>();

        // Listener to prevent overlapping animations.
        Animation.AnimationListener openCloseListener =new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
                animationActive =true;
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                animationActive =false;
            }
        };

        // Load animations and set listeners.
        fab_open = AnimationUtils.loadAnimation(context, R.anim.rotate_forward);
        fab_open.setAnimationListener(openCloseListener);
        fab_close = AnimationUtils.loadAnimation(context,R.anim.rotate_backward);
        fab_close.setAnimationListener(openCloseListener);

        // Create first fab, this one opens and closes the group.
        newFab(drawable, backgroundColor, foregroundColor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleFabs();
                }
            });
    }

    /**
     *  Initialize class and create Fab which opens and closes the group with animations.
     *  Generates + button to open which translates into x button to close.
     *
     * @param context   Context required.
     * @param parent    ViewGroup required.
     * @param backgroundColor   Background color int.
     * @param foregroundColor   Foreground color int.
     */
    public ExpandingFab(@NonNull Context context, @NonNull ViewGroup parent,
                        int backgroundColor, int foregroundColor){
        this(context, parent, android.R.drawable.ic_input_add, backgroundColor, foregroundColor);
    }

    /**
     *  Initialize class and create Fab which opens and closes the group with animations.
     *  Generates + button to open which translates into x button to close.
     *  Defaults to backgroundColor BLUE, foregroundColor WHITE.
     *
     * @param context   Context required.
     * @param parent    ViewGroup required.
     */
    public ExpandingFab(@NonNull Context context, @NonNull ViewGroup parent){
        this(context, parent, android.R.drawable.ic_input_add, Color.BLUE, Color.WHITE);
    }

    /**
     * Add a new Fab to the group.
     * Called by other convenience constructors.
     *
     * @param drawable          Drawable resource for new Fab.
     * @param backgroundColor   Background color int.
     * @param foregroundColor   Foreground color int.
     * @return                  FloatingActionButton generated.
     */
    private FloatingActionButton _newFab(int drawable, int backgroundColor, int foregroundColor){
        // Inflate Fab from base layout file.
        LayoutInflater inflater = LayoutInflater.from(context);
        FloatingActionButton fab =
                (FloatingActionButton) inflater.inflate(R.layout.fab, parent, false);

        // Set Fab properties.
        fab.setImageResource(drawable);
        fab.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        fab.setColorFilter(foregroundColor);

        // Add the Fab to the parent viewGroup and our list of Fabs.
        parent.addView(fab);
        fabs.add(fab);

        // Bring the first Fab to the front of the display.
        // The others should reside behind it so it is visible and accepts clicks.
        fabs.get(0).bringToFront();
        return fab;
    }

    /** Create a new Fab using default colors and the specified drawable resource. **/
    public FloatingActionButton newFab(int drawable) {
        return _newFab(drawable, backgroundColor, foregroundColor);
    }

    /** Create a new Fab using the specified colors and drawable resource. **/
    public FloatingActionButton newFab(int drawable, int backgroundColor, int foregroundColor) {
        return _newFab(drawable, backgroundColor, foregroundColor);
    }

    /** Create a new Fab using default colors and the specified drawable resource,
     * and attach the OnClickListener.  **/
    public FloatingActionButton newFab(int drawable, View.OnClickListener onClick) {
        FloatingActionButton fab =_newFab(drawable, backgroundColor, foregroundColor);
        fab.setOnClickListener(onClick);
        return fab;
    }

    /** Create a new Fab using the specified colors and drawable resource,
     * * and attach the OnClickListener.  **/
    public FloatingActionButton newFab(int drawable, int backgroundColor, int foregroundColor,
                                       View.OnClickListener onClick) {
        FloatingActionButton fab =_newFab(drawable, backgroundColor, foregroundColor);
        fab.setOnClickListener(onClick);
        return fab;
    }

    /**
     * Set listener for Open/Close events.
     * @param listener ExpandingFabListener.
     */
    public void onOpenCloseListener(ExpandingFabListener listener){
        this.listener = listener;
    }

    public interface ExpandingFabListener {
        // Listener callbacks for events.
        void onOpen(ArrayList<FloatingActionButton> fabs);
         void onClose(ArrayList<FloatingActionButton> fabs);
    }

    /**
     * Toggle Fabs group between Open and Closed.
     * Public in case the creator wants access.
     */
    public void toggleFabs(){

        if(!animationActive) {

            if (fabsOpen) {
                // Start close rotation on first fab.
                fabs.get(0).startAnimation(fab_close);
                // Loop through remaining fabs; rotate, scale and translate
                // back into closed position and forbid clicks.
                for (int i = 1; i < fabs.size(); i++) {
                    FloatingActionButton fab = fabs.get(i);
                    float height = fab.getHeight() * 1.15f;
                    fab.animate()
                            .translationYBy(i * height)
                            .scaleX(1.0f).scaleY(1.0f)
                            .rotationBy(360)
                            .alpha(0)
                            .setDuration(300).start();
                    fab.setClickable(false);
                }
                fabsOpen = false;
                // If listener has been set then send onClose event
                if (listener != null) {
                    listener.onClose(fabs);
                }
            } else {
                // Start open rotation on first fab.
                fabs.get(0).startAnimation(fab_open);
                // Loop through remaining fabs;
                // rotate, scale and translate into open position and allow clicks.
                for (int i = 1; i < fabs.size(); i++) {
                    FloatingActionButton fab = fabs.get(i);
                    float height = fab.getHeight() * 1.15f;
                    fab.animate()
                            .translationYBy(i * -height)
                            .scaleX(0.85f).scaleY(0.85f)
                            .rotationBy(-360)
                            .alpha(1)
                            .setDuration(300).start();
                    fabs.get(i).setClickable(true);
                }
                fabsOpen = true;
                // If listener has been set then send onOpen event.
                if (listener != null) {
                    listener.onOpen(fabs);
                }
            }

        }
    }
}
