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
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.File;

/** Extended {@link AppCompatButton} to represent files and folders for DragAndDropExample */
public class FileFolderButton extends AppCompatButton {

    private boolean isFolder;

    private File file;

    /** Default constructors required by AppCompatButton */
    public FileFolderButton(Context context){
        super(context);
    }
    public FileFolderButton(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    /**
     * Generate a new file or folder button.
     * @param context   Context.
     * @param file      The file to attach to this button.
     * @param parent    The parent layout.
     * @param isFolder  Is this a folder or a file?
     */
    public FileFolderButton(Context context, File file, RelativeLayout parent, boolean isFolder) {
        super(context);

        this.file =file;
        this.isFolder = isFolder;
        if(isFolder){
            setBackground(AppCompatResources.getDrawable(context, R.drawable.ic_folder));
            // The tag that we will be using in our DragAndDropTouchListener
            // to identify this as a folder and available to be overlapped and dropped on.
            setTag(R.string.folder_tag, true);

        } else{
            setBackground(AppCompatResources.getDrawable(context, R.drawable.ic_file));
        }

        setText(file.getName());
        setGravity(Gravity.CENTER);
        setTextAlignment(AppCompatButton.TEXT_ALIGNMENT_CENTER);
        setSoundEffectsEnabled(false);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutParams(params);

        setId(ViewCompat.generateViewId());
        parent.addView(this);

    }

    public File getFile() {
        return file;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
