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

/** Simple {@link View} wrapper class to handle overlap state */
class DragAndDropView{

    private final View view;

    private final float baseScaleX;
    private final float baseScaleY;

    DragAndDropView(View view){

        this.view = view;
        baseScaleX =view.getScaleX();
        baseScaleY =view.getScaleY();
    }

    View getView(){
        return view;
    }

    void setOverlapping(boolean overlapping){
        if(overlapping){
                view.setScaleX(baseScaleX *1.1f);
                view.setScaleY(baseScaleY *1.1f);
        } else{
                view.setScaleX(baseScaleX);
                view.setScaleY(baseScaleY);
        }
    }

}
