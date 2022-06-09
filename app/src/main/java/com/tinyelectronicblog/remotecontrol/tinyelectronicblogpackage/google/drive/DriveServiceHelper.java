package com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.google.drive;

/**
 * This class is a modified version of DriveServiceHelper.java available at link https://github.com/googleworkspace/android-samples/blob/master/drive/deprecation/app/src/main/java/com/google/android/gms/drive/sample/driveapimigration/DriveServiceHelper.java.
 *
 * The original license is shown below.
 *
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.Pair;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.tinyelectronicblog.remotecontrol.tinyelectronicblogpackage.debug.Debug;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

/**
 * A utility for performing read and write operations on Drive files via the REST API.
 * All functions must be executed in parallel threads (except getDriveService()). Warning, not all exceptions are handled.
 */
public class DriveServiceHelper {

    private final Drive mDriveService;

    public DriveServiceHelper (Drive driveService) {
        mDriveService = driveService;
    }

    static public Drive getDriveService (Context context, GoogleSignInAccount account, String applicationName) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccount(account.getAccount());
        return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .setApplicationName(applicationName)
                .build();
    }

    /**
     * Creates an empty text file. "name" is the name of the file.
     * If "parentFolder" is null or "", then the file is
     * placed in the user's My Drive folder, otherwise in the folder with ID "parentFolderID".
     * If successful it returns the file ID, otherwise null.
     */
    public String createFile (String name, String parentFolderID, boolean isFolder) {
        File metadata;
        if (isFolder) metadata = new File().setMimeType("application/vnd.google-apps.folder").setName(name);
        else metadata = new File().setMimeType("text/plain").setName(name);
        if (parentFolderID != null) if (!parentFolderID.equals("")) metadata.setParents(Collections.singletonList(parentFolderID));
        File googleFile;
        try {
            googleFile = mDriveService.files().create(metadata).execute();
        }
        catch (IOException e) {
            googleFile = null;
            Debug.print(e.toString());
        }
        if (googleFile == null) return null;
        else return googleFile.getId();
    }

    /**
     * Opens the file identified by "fileId" and returns a Pair of its metadata and
     * contents if successful, otherwise null. Use only on plain text files.
     */
    public Pair<File, String> readFile (String fileId) {
        // Retrieve the metadata as a File object.
        File metadata = null;
        try {
            metadata = mDriveService.files().get(fileId).execute();
        } catch (IOException e) {
            Debug.print(e.toString());
        }
        if (metadata == null) return null;
        // Stream the file contents to a String.
        try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            String contents = stringBuilder.toString();
            return Pair.create(metadata, contents);
        }
        catch (IOException e) {
            Debug.print(e.toString());
        }
        return null;
    }

    /**
     * Updates the file identified by "fileId" with the given "name", "content" and, if "oldParentFolder"
     * and "newParentFolder" are different from null and "", the file is moved to the new folder.
     * If this function is used on a folder, then "content" must be null or "".
     * Returns true on success.
     */
    public boolean updateFile (String fileId, String name, String content, String oldParentFolder, String newParentFolder) {
        // Create a File containing any metadata changes.
        File metadata = new File().setName(name);
        // Convert content to an AbstractInputStreamContent instance.
        ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
        // Update the metadata and contents.
        try {
            Drive.Files.Update update =  mDriveService.files().update(fileId, metadata, contentStream);
            if (oldParentFolder != null && newParentFolder != null) if (!oldParentFolder.equals("") && !newParentFolder.equals("")) {
                update.setRemoveParents(oldParentFolder);
                update.setAddParents(newParentFolder);
            }
            update.execute();
        }
        catch (IOException e) {
            Debug.print(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Returns a FileList containing the files searched in the user's My Drive.
     * The q, orderBy and pageToken parameters can assume the values listed in
     * <a href="https://developers.google.com/drive/api/v3/reference/files/list">;
     * if not used they can be null or "". "pageSize" is the maximum number of
     * files to return per search; acceptable values are 1 to 1000 inclusive.
     * Returns null if it fails.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public FileList queryFiles (String q, String orderBy, String pageToken, int pageSize) {
        FileList fileList;
        try {
            Drive.Files.List list = mDriveService.files().list().setSpaces("drive");
            if (orderBy != null) if (!orderBy.equals("")) list.setOrderBy(orderBy);
            if (q != null) if (!q.equals("")) list.setQ(q);
            if (pageToken != null) if (!pageToken.equals("")) list.setQ(pageToken);
            if (pageSize >= 1 && pageSize <= 1000) list.setPageSize(pageSize);
            fileList = list.execute();
        } catch (IOException e) {
            fileList = null;
            Debug.print(e.toString());
        }
        return fileList;
    }

    /**
     * Deletes the file identified by "fileId".
     * Returns true on success.
     */
    public boolean deleteFile (String fileId) {
        try {
            mDriveService.files().delete(fileId).execute();
        } catch (IOException e) {
            Debug.print(e.toString());
            return false;
        }
        return true;
    }

}