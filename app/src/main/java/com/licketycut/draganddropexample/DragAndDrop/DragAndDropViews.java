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

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/** A class to manage interactions between an active {@link View }
 * and a list of {@link DragAndDropView}s within a parent view. */
class DragAndDropViews {

    private final ArrayList<DragAndDropView> dragAndDropViews = new ArrayList<>();

    private DragAndDropView overlappingView =null;
    private DragAndDropView wasOverlappingView =null;

    /**
     * Initialize the list of DragAndDropViews attached to the parent view.
     * @param view          The active view.
     * @param parentView    Parent view, if designated.
     * @param overlapTag    Overlap tag to compare against, if designated.
     * @param bringToFront  Bring the active view to the front?
     */
    DragAndDropViews(View view, ViewGroup parentView, Integer overlapTag, boolean bringToFront){

        // Get the depth of the active view.
        float frontZ =ViewCompat.getZ(view);

        ViewGroup parent =parentView;
        if(parent ==null){
            // If no parent has been specified then get the direct parent of te active view.
            parent =(ViewGroup) view.getParent();
        }

        for(int index = 0; index< parent.getChildCount(); ++index) {
            View childView =parent.getChildAt(index);
            if(view.getId() != childView.getId()){

                if(overlapTag !=null){
                    // If the overlap tag has been specified.
                    Boolean tag =(Boolean) childView.getTag(overlapTag);
                    if(tag == null || !tag){
                        // We only want to consider views with the overlap tag
                        // for overlap events, so skip this one.
                        continue;
                    }
                }

                float thisZ =ViewCompat.getZ(childView);
                if(thisZ >=frontZ){
                    // If the child view is in front of the active view,
                    // increase placeholder for front most view.
                    frontZ=thisZ +1.0f;
                }

                // Add this child view to our list of drag and drop views.
                DragAndDropView dragAndDropView = new DragAndDropView(childView);
                dragAndDropViews.add(dragAndDropView);
            }
        }

        if(bringToFront){
            // Bring active view to front as requested.
            view.bringToFront();
            parent.invalidate();
            ViewCompat.setZ(view, frontZ);
        }
    }

    /**
     * Test if the center of the active view is overlapping another.
     * @param view  The active view.
     * @return      True if the active view overlaps another.
     */
    boolean isOverlapping(View view){

        for(DragAndDropView dragAndDropView: dragAndDropViews) {
            if (!dragAndDropView.getView().equals(view)) {
                // We don't want to check the active view against itself.
                View testView = dragAndDropView.getView();
                Rect rect = new Rect();
                view.getHitRect(rect);
                Rect testRect = new Rect();
                testView.getHitRect(testRect);

                if (testRect.contains(rect.centerX(), rect.centerY())) {
                    if (overlappingView == dragAndDropView) {
                        return true;
                    } else if (overlappingView != null) {
                        // If the new overlapping view is not the previous overlapping view,
                        // set the previous overlapping view to was overlapping view.
                        overlappingView.setOverlapping(false);
                        wasOverlappingView = overlappingView;
                        overlappingView =null;
                    }
                        overlappingView = dragAndDropView;
                        overlappingView.setOverlapping(true);
                    return true;
                }
            }
        }

        if (overlappingView != null) {
            // If no views overlap but there was a previous overlapping view,
            // set the previous overlapping view to was overlapping view.
            overlappingView.setOverlapping(false);
            wasOverlappingView = overlappingView;
            overlappingView =null;
        }

        return false;
    }

    View getOverlappingView(){
        return overlappingView.getView();
    }

    View removeOverlappingView(){
        View view =overlappingView.getView();
        overlappingView.setOverlapping(false);
        overlappingView =null;
        return view;
    }

    boolean wasOverlapping(){
        return (wasOverlappingView !=null);
    }

    View getWasOverlappingView(){
        return wasOverlappingView.getView();
    }

    View removeWasOverlapping() {
        View view =wasOverlappingView.getView();
        wasOverlappingView =null;
        return view;
    }
}
