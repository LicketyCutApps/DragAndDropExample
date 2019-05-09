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

import android.view.View;
import android.view.ViewGroup;

/** Parameters for {@link DragAndDropTouchListener }. */
public class DragAndDropParams {

    private View boundaryView =null;
    private ViewGroup parentView =null;
    private Integer overlapTag =null;

    /** Create DragAndDropParams with boundary, parent and overlap tag. */
    public DragAndDropParams(View boundaryView, View parentView, int overlapTag){
        this.boundaryView = boundaryView;
        this.parentView = (ViewGroup) parentView;
        this.overlapTag = overlapTag;
    }

    /** Create DragAndDropParams with boundary and parent. */
    public DragAndDropParams(View boundaryView, View parentView){
        this.parentView =(ViewGroup) parentView;
        this.boundaryView = boundaryView;
    }

    /** Create DragAndDropParams with parent and overlap tag. */
    public DragAndDropParams(View parentView, int overlapTag){
        this.parentView = (ViewGroup) parentView;
        this.overlapTag = overlapTag;
    }

    /** Create DragAndDropParams with overlap tag only. */
    DragAndDropParams(int overlapTag){
        this.overlapTag = overlapTag;
    }

    View getBoundaryView() {
        return boundaryView;
    }

    ViewGroup getParentView() {
        return parentView;
    }

    Integer getOverlapTag() {
        return overlapTag;
    }
}
