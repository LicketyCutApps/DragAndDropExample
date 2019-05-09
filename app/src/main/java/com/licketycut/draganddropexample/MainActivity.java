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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.licketycut.draganddropexample.DragAndDrop.DragAndDropParams;
import com.licketycut.draganddropexample.DragAndDrop.DragAndDropTouchListener;
import com.licketycut.draganddropexample.ExpandingFab.ExpandingFab;

import org.apache.commons.io.comparator.CompositeFileComparator;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * A demonstration of a limited file manager utilizing {@link FileFolderButton}s
 * which can be manipulated by my custom {@link DragAndDropTouchListener}.
 */
// We understand the implications, our extended touch listener issues performClick() as necessary.
@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity {

    private final static String TAG="DragAndDropExample";

    // This is just a demonstration of drag and drop, not a full fledged file manager.
    private final static int NESTED_LIMIT =16;

    enum SortOrder{ NAME, MODIFIED}
    private SortOrder sortOrder =SortOrder.MODIFIED;

    private RelativeLayout boundaryView;
    private Toolbar toolbar;
    private DragAndDropTouchListener dragAndDropTouchListener;

    // The following variables are static so they persist through configuration changes.
    private static FileFolderGrid fileFolderGrid;

    private static File rootFolder;
    private static File currentFolder;

    private static int nextFolderNum =1;
    private static int nextFileNum =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the action bar which includes the options menu.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add click listener to the back button on the toolbar.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        boundaryView =findViewById(R.id.file_folder_layout);

        // Create a custom touch listener used for FileFolderButtons.
        dragAndDropTouchListener = new DragAndDropTouchListener(getBaseContext(),
                // Set boundaryView as the container and parent
                // and only allow folders to be considered for overlap.
                new DragAndDropParams(boundaryView, boundaryView, R.string.folder_tag)) {
            @Override
            public boolean onDrop(View view, boolean wasOverlapping, View overlappingView) {
                if (wasOverlapping) {
                    // Find the FileFolderButton which was dropped by view:id.
                    FileFolderButton fileFolderButton = fileFolderGrid.findById(view.getId());

                    // We should only be receiving folders so find which was dropped on.
                    FileFolderButton fileFolderButtonOverlapping =
                            fileFolderGrid.findById(overlappingView.getId());
                    File toFolder = fileFolderButtonOverlapping.getFile();

                    File[] files = toFolder.listFiles();
                    if (files.length > NESTED_LIMIT) {
                        // If the folder is full, alert the user.
                        reachedNestedLimit();
                        return false;
                    } else {
                        // Move the file into the folder which it was dropped on and update.
                        if (moveFile(fileFolderButton, toFolder)) {
                            updateCurrentFolder(currentFolder);
                        }
                    }
                    // We've handled the event and the listener should stop processing the view.
                    return true;
                }
                return false;
            }

            @Override
            protected boolean onClick(View view) {
                // Find the FileFolderButton which was clicked by view:id.
                FileFolderButton fileFolderButton = fileFolderGrid.findById(view.getId());
                if (fileFolderButton.isFolder()) {
                    // Since we are handling clicks with a custom callback,
                    // we have to issue the click sound manually.
                    AudioManager audioManager =
                            (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        // Click only sounds if user has touch sounds enabled.
                        audioManager.playSoundEffect(SoundEffectConstants.CLICK);
                    }

                    // If it was a folder, open it.
                    updateCurrentFolder(fileFolderButton.getFile());
                    // We've handled the event and the listener should stop processing the view.
                    return true;
                }
                return false;
            }

            @Override
            protected boolean onLongPress(View view) {
                // Find the FileFolderButton which was long pressed by view:id.
                final FileFolderButton fileFolderButton = fileFolderGrid.findById(view.getId());

                // Create a PopupMenu anchored to the FileFolderButton.
                Context wrapper = new ContextThemeWrapper(getBaseContext(), R.style.PopUpMenu);
                final PopupMenu popupMenu = new android.widget.PopupMenu(wrapper, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_move_file_root:
                                if (moveFile(fileFolderButton, rootFolder)) {
                                    updateCurrentFolder(currentFolder);
                                }
                                break;
                            case R.id.menu_item_move_file_up:
                                if (moveFile(fileFolderButton, currentFolder.getParentFile())) {
                                    updateCurrentFolder(currentFolder);
                                }
                                break;
                            case R.id.menu_item_delete_file:
                                File file = fileFolderButton.getFile();
                                if (file.delete()) {
                                    updateCurrentFolder(currentFolder);
                                }
                                break;
                        }
                        popupMenu.dismiss();
                        return true;
                    }
                });

                if (currentFolder.getPath().equals(rootFolder.getPath())) {
                    // If user is in the root folder, remove unneeded options from the popup menu.
                    Menu m = popupMenu.getMenu();
                    m.removeItem(R.id.menu_item_move_file_up);
                    m.removeItem(R.id.menu_item_move_file_root);
                }

                popupMenu.show();
                // We've handled the event and the listener should stop processing the view.
                return true;
            }
        };

        // Add a listener to be called when boundaryView has been laid out.
        ViewTreeObserver observer = boundaryView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                if (fileFolderGrid != null) {
                    // On configuration change the fileFolderGrid is already populated.
                    // Recreate the grid for the new configuration and repopulate it.
                    fileFolderGrid = new FileFolderGrid(
                            getBaseContext(), boundaryView, 48, 12, NESTED_LIMIT);
                    updateCurrentFolder(currentFolder);
                } else {
                    // This must be a fresh run,
                    // so we need to initialize our root folder and the grid.
                    rootFolder=new File(getFilesDir(),TAG);
                    fileFolderGrid = new FileFolderGrid(
                            getBaseContext(), boundaryView, 48, 12, NESTED_LIMIT);

                    if (!rootFolder.exists()) {
                        // If the root folder doesn't exist, create it, add one folder and one file.
                        if(rootFolder.mkdirs()){
                            createFolder(rootFolder);
                            createFile(rootFolder);
                        }
                    } else{
                        // If the root folder exists then add the FileFolderButtons to the grid
                        // and set the folder and file counts.
                        File[] fileList = rootFolder.listFiles();
                        addFilesInOrder(fileList);
                        setNextFileFolderNums(fileList);
                    }
                    currentFolder = rootFolder;
                }
                toolbar.setTitle(currentFolder.getName());
                boundaryView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        // Create our ExpandingFab using the current ViewGroup, the + button,
        // Dark Gray background and White foreground.
        final ExpandingFab expandingFab =
                new ExpandingFab( this, (ViewGroup) findViewById(android.R.id.content),
                android.R.drawable.ic_input_add, Color.DKGRAY, Color.WHITE);

        // Add a new folder Fab and attach the OnClickListener.
        expandingFab.newFab(R.drawable.ic_add_folder, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileFolderGrid.isFull()) {
                    // Folder is full so alert the user.
                    reachedNestedLimit();
                } else {
                    createFolder(currentFolder);
                }
            }
        });

        // Add a new file Fab and attach the OnClickListener.
        expandingFab.newFab(R.drawable.ic_add_file, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileFolderGrid.isFull()) {
                    // Folder is full so alert the user.
                    reachedNestedLimit();
                } else{
                    createFile(currentFolder);
                }
            }
        });
    }

    /** Recursive method which finds all sub files and folders and updates counters. */
    private void setNextFileFolderNums(File[] folder){
        if(folder != null){
            for (File file : folder) {
                if (file.isFile()) {
                    ++nextFileNum;
                } else if (file.isDirectory()) {
                    ++nextFolderNum;
                    // This is a folder so,
                    // call this method recursively to count sub files and folders.
                    setNextFileFolderNums(file.listFiles());
                }
            }
        }

    }

    /** Create a new FileFolderButton, add our custom touch listener
     * and attach it to the FileFolderGrid. */
    private void addFileFolderButton(File file){
        FileFolderButton fileFolder =
                new FileFolderButton(getBaseContext(), file, boundaryView, file.isDirectory());
        fileFolder.setOnTouchListener(dragAndDropTouchListener);
        fileFolderGrid.setNextOpenButton(fileFolder);
    }

    /** Clear the current FileFolderButtons, change currentFolder and update grid. */
    private void updateCurrentFolder(File folder){

        currentFolder =folder;

        // Remove and retrieve a list of the current FileFolderButtons in the grid
        // and remove them from the boundaryView.
        ArrayList<FileFolderButton> fileFolderButtons =fileFolderGrid.removeFileFolderButtons();
        for(FileFolderButton button: fileFolderButtons){
            boundaryView.removeView(button);
        }

        addFilesInOrder(currentFolder.listFiles());

        toolbar.setTitle(currentFolder.getName());
    }

    /** Add FileFolderButtons found in fileList and sort them based on sortOrder. */
    @SuppressWarnings("unchecked")  // We are aware that the Comparators are unchecked.
    private void addFilesInOrder(File[] fileList){

        switch(sortOrder){
            case MODIFIED:
                Arrays.sort(fileList,LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                break;
            case NAME:
                // Sort alphanumerically with folders first.
                CompositeFileComparator comparator =
                        new CompositeFileComparator(
                                DirectoryFileComparator.DIRECTORY_COMPARATOR,
                                NameFileComparator.NAME_COMPARATOR);
                Arrays.sort(fileList, comparator);
                break;
        }
        for(File file : fileList){
            addFileFolderButton(file);
        }
    }

    /** Create new file and button.
     *  Add it to the grid and update the file number count.
     */
    private void createFile(File file){
        File newFile =new File(file, "File\n"+ nextFileNum);
        try {
            if(newFile.createNewFile()) {
                addFileFolderButton(newFile);
                ++nextFileNum;
            }
        }catch (java.io.IOException e){
            Log.w(TAG, "Could not create file :"+e.getMessage());
        }

    }

    /** Create new folder and button.
     *  Add it to the grid and update the file number count.
     */
    private void createFolder(File folder){
        File newFolder =new File(folder, "Folder\n"+ nextFolderNum);
        if(newFolder.mkdir()) {
            addFileFolderButton(newFolder);
            ++nextFolderNum;
        }
    }

    /**
     * Move file referenced by button into a different folder.
     *
     * @param fileFolderButton  Button containing file to move.
     * @param toFolder          Folder into which to move.
     * @return                  Success?
     */
    private boolean moveFile(FileFolderButton fileFolderButton, File toFolder){
        // Generate the new filename with updated path.
        File file =fileFolderButton.getFile();
        File newFile =new File(toFolder.getPath()+"/"+file.getName());

        // Rename file with the new path and remove the button from the current grid.
        if(file.renameTo(newFile)) {
            fileFolderGrid.removeButton(fileFolderButton);
            boundaryView.removeView(fileFolderButton);
            return true;
        }else{
            Log.w(TAG, "Could not move file :"+newFile.getName()+" to : "+newFile.getPath());
        }

        return false;
    }

    /** Recursive method which finds all sub files and folders and deletes them. */
    private void deleteFilesFolders(File[] folder){
        if(folder != null){
            for (File file : folder) {
                if (file.isFile()) {
                    if(!file.delete()) {
                        Log.w(TAG, "Could not delete file : "+file.getPath());
                    }
                } else if (file.isDirectory()) {
                    // This is a folder so,
                    // call this method recursively to delete sub files and folders.
                    deleteFilesFolders(file.listFiles());
                    // After recursive deletes, delete this folder itself.
                    if(!file.delete()) {
                        Log.w(TAG, "Could not delete folder : "+file.getPath());
                    }
                }
            }
        }

    }

    /** Alert user that the folder limit has been reached. */
    private void reachedNestedLimit(){
        Toast.makeText(getBaseContext(),
                "Sorry this Demo has a limit of " + NESTED_LIMIT + " nested files.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_item_sort_by) {
            switch(sortOrder){
                // Toggle the sort order.
                case NAME:
                    item.setTitle(R.string.action_sort_by_name);
                    sortOrder = SortOrder.MODIFIED;
                    break;
                case MODIFIED:
                    item.setTitle(R.string.action_sort_by_modified);
                    sortOrder = SortOrder.NAME;
                    break;
            }
            // Update grid with the new sort order.
            updateCurrentFolder(currentFolder);
            return true;
        } else if(id ==R.id.menu_item_delete_all_reset){
            // User wants to delete all files and reset to the initial state.
            deleteFilesFolders(rootFolder.listFiles());
            nextFolderNum = 1;
            nextFileNum = 1;
            createFolder(rootFolder);
            createFile(rootFolder);
            updateCurrentFolder(rootFolder);
        } else if(id ==R.id.menu_item_exit){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentFolder.getPath().equals(rootFolder.getPath())){
            // If the currentFolder is the rootFolder then exit.
            super.onBackPressed();
        }else {
            // If currentFolder is a subfolder, move up one depth and update the grid.
            updateCurrentFolder(currentFolder.getParentFile());
        }
    }
}
