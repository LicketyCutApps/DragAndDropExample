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

package com.licketycut.draganddropexample;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/** Grid array of {@link FileFolderButton}s and methods to manipulate it. */
class FileFolderGrid {

    private final int widthPixels;
    private final int heightPixels;
    private final int halfMargin;

    private final FileFolderButton[][] fileFolderGrid;
    private final int columns;
    private final int rows;
    private final int nestedLimit;

    /**
     * Create a new grid to suit the current configuration.
     * @param context       Context.
     * @param view          View into which to create the grid.
     * @param cellSizeDp      Cell size represented in dp
     * @param marginDp        Margin size represented in dp.
     * @param nestedLimit   Limit of available cells per grid.
     */
    FileFolderGrid(Context context, RelativeLayout view, int cellSizeDp, int marginDp, int nestedLimit){

        this.nestedLimit =nestedLimit;

        // Calculate cell size and margin from dp to pixels.
        DisplayMetrics displayMetrics =context.getResources().getDisplayMetrics();
        int marginPx =(int) Math.ceil(marginDp * displayMetrics.density);
        int cellSizePx =(int) Math.ceil(cellSizeDp * displayMetrics.density);

        widthPixels =view.getWidth();
        heightPixels =view.getHeight();

        // Used to place buttons inside the grid.
        halfMargin =marginPx/2;

        columns =(widthPixels / (cellSizePx+marginPx))-1;
        rows =(heightPixels / (cellSizePx+marginPx))-1;

        fileFolderGrid = new FileFolderButton[rows][columns];
        //clearGrid();
    }


    /** Find the next available cell in the grid and fill it */
    void setNextOpenButton(FileFolderButton fileFolderButton){
        for(int row =0; row <rows; row++){
            for(int column =0; column <columns; column++){
                if(fileFolderGrid[row][column] ==null) {
                    // If this cell is available then fill it.
                    fileFolderGrid[row][column] = fileFolderButton;

                    // If column or row equals zero then half the margin will be correct.
                    int x =halfMargin, y =halfMargin;

                    // Add column / row by pixel position as needed.
                    if(column >0){
                        x +=(widthPixels/columns) *column;
                    }
                    if(row >0){
                        y +=(heightPixels/rows) * row;
                    }

                    // Set the button to the appropriate place in the display.
                    fileFolderButton.setX(x);
                    fileFolderButton.setY(y);
                    return;
                }
            }
        }
    }

    /** Find the button in the grid and remove it */
    void removeButton(FileFolderButton fileFolderButton){
        for(int row =0; row <rows; row++){
            for(int column =0; column <columns; column++){
                if(fileFolderGrid[row][column] ==fileFolderButton)
                    fileFolderGrid[row][column] =null;
            }
        }
    }

    /** Find the button referenced by the view:id and return it. */
    FileFolderButton findById(int id){
        for(int row =0; row <rows; row++){
            for(int column =0; column <columns; column++) {
                if (fileFolderGrid[row][column] != null) {
                    if (fileFolderGrid[row][column].getId() == id)
                        return fileFolderGrid[row][column];
                }
            }
        }
        return null;
    }

    /** Remove all of the buttons from the grid and return them in an {@link ArrayList} */
    ArrayList<FileFolderButton> removeFileFolderButtons(){
        ArrayList<FileFolderButton> fileFolderButtonList = new ArrayList<>();

        //We want to maintain array order in list and only add valid FileFolderButtons.
        for(int row =0; row <rows; row++){
            for(int column =0; column <columns; column++){
                if(fileFolderGrid[row][column] !=null) {
                    // Add the button to the list and clear it from the grid.
                    fileFolderButtonList.add(fileFolderGrid[row][column]);
                    fileFolderGrid[row][column] =null;
                }
            }
        }

        return fileFolderButtonList;
    }


    /** The grid is full if we have reached the array limits or nested limit. */
    boolean isFull(){
        for(int row =0; row <rows; row++){
            for(int column =0; column <columns; column++){
                if(column +(row*columns) >=nestedLimit){
                    return true;
                }
                if(fileFolderGrid[row][column] ==null){
                    return false;
                }
            }
        }

        // The array is full.
        return true;
    }
}
