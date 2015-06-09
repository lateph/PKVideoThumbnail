/**
 * PKVideoThumbnail
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.photokandy.PKVideoThumbnail;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.*;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.provider.DocumentsContract;

/**
 * This class echoes a string called from JavaScript.
 */
public class PKVideoThumbnail extends CordovaPlugin {
    
     private static final String TAG = "Thumb Nail";
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.
     * @param args          JSONArry of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals("createThumbnail")) {
                String sourceVideo = args.getString(0);
                String targetImage = args.getString(1);
                
//                File f = new File( sourceVideo );
//                if(f.exists()){
//                  System.out.print("file ada");
//                  callbackContext.error ( "File Ada. : "  +sourceVideo);
//                  return true;
//                }
//                else{
//                  System.out.print("File tidak ada");
//                  callbackContext.error ( "File Tidak Ada." + sourceVideo );
//                  return true;
//                }
////                
                final File inFile = this.resolveLocalFileSystemURI(sourceVideo);
                if (!inFile.exists()) {
                    Log.d(TAG, "input file does not exist");
                    callbackContext.error("input video does not exist.");
                    return true;
                }
                                
                final String videoSrcPath = inFile.getAbsolutePath();
                
                if(targetImage == null || targetImage.equals("")){
                    targetImage = videoSrcPath + ".jpg";
                }
               
                
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail (  videoSrcPath, MediaStore.Images.Thumbnails.MINI_KIND);
//                
                if(thumbnail == null){
                    callbackContext.error ( "Thumbnail null ."+ sourceVideo+ "||||"+sourceVideo.substring(7) );
                    return true;
                }
                
                
                FileOutputStream theOutputStream;
                try
                {
                    File theOutputFile;
                    Log.d(TAG,"Output image: "+targetImage);
                    if(targetImage == null || targetImage.equals("")){
                        theOutputFile = new File (targetImage);
                    }
                    else{
                        theOutputFile = new File (targetImage.substring(7));
                    }
                    
                    if (!theOutputFile.exists())
                    {
                        if (!theOutputFile.createNewFile())
                        {
                                        callbackContext.error ( "Could not save thumbnail." );
                                        return true;
                        }
                    }
                    if (theOutputFile.canWrite())
                    {
                        theOutputStream = new FileOutputStream (theOutputFile);
                        if (theOutputStream != null)
                        {
                            callbackContext.success ( targetImage );
                            thumbnail.compress(CompressFormat.JPEG, 75, theOutputStream);
                        }
                        else
                        {
                             callbackContext.error ( "Could not save thumbnail; target not writeable");
                             return true;
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                        callbackContext.error ( "I/O exception saving thumbnail" );
                        return true; 
                }
                callbackContext.success ( targetImage );        
                return true; 
                
            } else {
                return false;
            }
        } catch (JSONException e) {
            callbackContext.error ( "JSON Exception" );
            return true;
        } catch(Exception e){
            e.printStackTrace();
            callbackContext.error("File Input Error"+e.getMessage()+":"+args.getString(0));
            return true;
        }
    }

    private File resolveLocalFileSystemURI(String url) throws IOException, JSONException {
        String decoded = URLDecoder.decode(url, "UTF-8");

        File fp = null;

        // Handle the special case where you get an Android content:// uri.
        if (decoded.startsWith("content:")) {
            fp = new File(getPath(this.cordova.getActivity().getApplicationContext(), Uri.parse(decoded)));
        } else {
            // Test to see if this is a valid URL first
            @SuppressWarnings("unused")
            URL testUrl = new URL(decoded);

            if (decoded.startsWith("file://")) {
                int questionMark = decoded.indexOf("?");
                if (questionMark < 0) {
                    fp = new File(decoded.substring(7, decoded.length()));
                } else {
                    fp = new File(decoded.substring(7, questionMark));
                }
            } else if (decoded.startsWith("file:/")) {
                fp = new File(decoded.substring(6, decoded.length()));
            } else {
                fp = new File(decoded);
            }
        }

        if (!fp.exists()) {
            throw new FileNotFoundException();
        }
        if (!fp.canRead()) {
            throw new IOException();
        }
        return fp;
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    public static String getDataColumn(Context context, Uri uri, String selection,
            String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
