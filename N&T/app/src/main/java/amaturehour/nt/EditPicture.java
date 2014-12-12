package amaturehour.nt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Context;
import android.os.Environment;
import android.widget.ProgressBar;
import android.app.Dialog;
import android.view.Window;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditPicture extends Activity{
    private static final String TAG = "EDIT";

    private Button mSquareCut;
    private Button mCircleCut;
    private Button mFinish;
    private ImageView mUneditableImage;
    private ImageView mEditableImage;
    private String firstFileName;
    private String secondFileName;
    private int mOrientation;
    private Bitmap mUneditableBitmap;
    private Bitmap mEditableBitmap;
    private ProgressBar mProgressBar;
    private static File savedImage;

    private static int displayWidth;
    private static int displayHeight;
    private static int displayDensity;
    private static int rotationAngle;
    private static Context mContext;
    private static final int INDEX_OF_WIDTH = 0;
    private static final int INDEX_OF_HEIGHT = 1;
    private static final int INDEX_OF_DENSITY = 2;
    private static final int STRETCH_CONSTANT = 96;
    private static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);


        mEditableImage = (ImageView)findViewById(R.id.editable_image);
        mEditableImage.setVisibility(View.INVISIBLE);

        mUneditableImage = (ImageView)findViewById(R.id.uneditable_image);
        mUneditableImage.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        int origin = intent.getFlags();


        if(origin == 1){
            int[] screenInfo = intent.getIntArrayExtra("ScreenInformation");

            displayWidth = screenInfo[INDEX_OF_WIDTH];
            //need to add a little vertical stretch because of the action bar dimensions??
            displayHeight = screenInfo[INDEX_OF_HEIGHT];
            displayDensity = screenInfo[INDEX_OF_DENSITY];
            firstFileName = intent.getStringExtra(StartScreen.UNDERLAY_IMAGE);
            secondFileName = intent.getStringExtra(StartScreen.OVERLAY_IMAGE);
            try {
                ExifInterface exif = new ExifInterface(firstFileName);
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                Log.e(TAG, "Orientation: " + mOrientation);

            }
            catch (IOException e){
                Log.e(TAG, "Error creating Exif from " + firstFileName);
            }
            mUneditableBitmap = BitmapFactory.decodeFile(firstFileName);
            mUneditableBitmap.setDensity(displayDensity);
            mUneditableBitmap = rotateBitmap(mUneditableBitmap, mOrientation, mUneditableBitmap.getWidth(), mUneditableBitmap.getHeight());

            if(mUneditableBitmap.getHeight() > displayHeight || mUneditableBitmap.getWidth() > displayWidth)
                mUneditableBitmap = getResizedBitmap(mUneditableBitmap, displayHeight, displayWidth);
            if(mUneditableBitmap == null){
                Log.e(TAG, "Error making the Bitmap - Null");
            }
            else{
                Log.i(TAG, "Bitmap success - Not Null");
                mUneditableImage.setImageBitmap(mUneditableBitmap);
            }
            mUneditableImage.setVisibility(View.VISIBLE);

            try{
                ExifInterface exif = new ExifInterface(secondFileName);
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            catch (IOException e){
                Log.e(TAG, "Error creating Exif from " + secondFileName);
            }
            mEditableBitmap = BitmapFactory.decodeFile(secondFileName);


            mEditableBitmap.setDensity(displayDensity);
            mEditableBitmap = rotateBitmap(mEditableBitmap, mOrientation, mEditableBitmap.getWidth(), mEditableBitmap.getHeight());

            if(mEditableBitmap.getHeight() > displayHeight || mEditableBitmap.getWidth() > displayWidth)
                mEditableBitmap = getResizedBitmap(mEditableBitmap, displayHeight, displayWidth);
            if(mEditableBitmap == null){
                Log.e(TAG, "Error making the Bitmap - Null");
            }
            else{
                Log.i(TAG, "Bitmap success - Not Null");
            }
        }
        else if(origin == 2){
            displayWidth = intent.getIntExtra("scaleWidth", 1);
            displayHeight = intent.getIntExtra("scaleHeight", 1);
            displayDensity = intent.getIntExtra("Dense", 1);
            rotationAngle = intent.getIntExtra("thisMayNotBeUsed", 1);
            firstFileName = intent.getStringExtra(CustomCamera.UNDERLAY_IMAGE2);
            Log.e(TAG, "1st File name: " + firstFileName);
            secondFileName = intent.getStringExtra(CustomCamera.OVERLAY_IMAGE2);
            Log.e(TAG, "2nd File name: " + secondFileName);
            try{
                ExifInterface exif = new ExifInterface(firstFileName);
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            catch (IOException e){
                Log.e(TAG, "Error creating Exif from " + firstFileName);
            }
            mUneditableBitmap = BitmapFactory.decodeFile(firstFileName);
            mUneditableBitmap.setDensity(displayDensity);
            mUneditableBitmap = rotateBitmap(mUneditableBitmap, mOrientation, mUneditableBitmap.getWidth(), mUneditableBitmap.getHeight());


            if(mUneditableBitmap.getHeight() > displayHeight || mUneditableBitmap.getWidth() > displayWidth)
                mUneditableBitmap = getResizedBitmap(mUneditableBitmap, displayHeight, displayWidth);

            if(mUneditableBitmap == null){
                Log.e(TAG, "Error making the Bitmap - Null");
            }
            else{
                Log.i(TAG, "Bitmap success - Not Null");
                mUneditableImage.setImageBitmap(mUneditableBitmap);
            }
            mUneditableImage.setVisibility(View.VISIBLE);

            try{
                ExifInterface exif = new ExifInterface(secondFileName);
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            catch (IOException e){
                Log.e(TAG, "Error creating Exif from " + secondFileName);
            }
            mEditableBitmap = BitmapFactory.decodeFile(secondFileName);
            mEditableBitmap.setDensity(displayDensity);

            //mEditableBitmap = rotateBitmap(mEditableBitmap, mOrientation, mEditableBitmap.getWidth(), mEditableBitmap.getHeight());
            if (mEditableBitmap.getWidth() > mEditableBitmap.getHeight()) {
                mEditableBitmap = fixOrientation(mEditableBitmap);
            }

            if(mEditableBitmap.getHeight() > displayHeight || mEditableBitmap.getWidth() > displayWidth)
                mEditableBitmap = getResizedBitmap(mEditableBitmap, displayHeight, displayWidth);
            if(mEditableBitmap == null){
                Log.e(TAG, "Error making the Bitmap - Null");
            }
            else{
                Log.i(TAG, "Bitmap success - Not Null");
                mUneditableImage.setImageBitmap(mEditableBitmap);
            }
            mUneditableImage.setVisibility(View.VISIBLE);
        }

        Bitmap[] bmpArray = {mEditableBitmap, mUneditableBitmap};

        mContext = mEditableImage.getContext();

        mProgressBar.setVisibility(View.VISIBLE);

        new ProcessImageTask().execute(bmpArray);

    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
//        // This location works best if you want the created images to be shared
//        // between applications and persist after your app has been uninstalled.
//
//        // Create the storage directory if it does not exist
//        if (! mediaStorageDir.exists()){
//            if (! mediaStorageDir.mkdirs()){
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + "/storage/emulated/0/DCIM/Camera")));
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File fileToReturn;
        if (type == MEDIA_TYPE_IMAGE){
            fileToReturn = new File("/storage/emulated/0/DCIM/Camera" + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return fileToReturn;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_edit_picture, container, false);
            return rootView;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation, int width, int height){
        Matrix matrix = new Matrix();

        Log.e(TAG, "Orientation value in rotate: " + orientation);

        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            default:
                return bitmap;
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        double ratio = ((1.0 * bm.getHeight()) * newWidth) / (bm.getHeight() * newHeight);
        Log.e(TAG, "ratio: " + ratio);
        int width = (int) (bm.getWidth() * ratio);
        int height = (int) (bm.getHeight() * ratio);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, width, height, false);
        return resizedBitmap;
    }

    public Bitmap combineBitmap(Bitmap[] srcBmp) {
        Bitmap combo = Bitmap.createScaledBitmap(srcBmp[0], srcBmp[0].getWidth(), srcBmp[0].getHeight(), false);

        for (int i = 0; i < srcBmp[0].getWidth(); i++) {
            for(int j = 0; j < srcBmp[0].getHeight(); j++){
                if(i < srcBmp[0].getWidth()/2)
                    combo.setPixel(i, j, srcBmp[0].getPixel(i, j));
                else
                    combo.setPixel(i, j, srcBmp[1].getPixel(i, j));
            }
        }
        return combo;
    }

    //change orientation of images to portrait
    public Bitmap fixOrientation(Bitmap mBitmap){
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            mBitmap = Bitmap.createBitmap(mBitmap , 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            return mBitmap;
    }

    //set up a different thread to edit the picture
    private class ProcessImageTask extends AsyncTask<Bitmap[], Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap[]... params) {
            mEditableBitmap = combineBitmap(params[0]);
            return mEditableBitmap;
        }

        protected void onPostExecute(Bitmap result){
            mEditableImage.setImageBitmap(result);
            mProgressBar.setVisibility(View.INVISIBLE);
            mEditableImage.setVisibility(View.VISIBLE);


            //ask if user wants to save the image
            AlertDialog.Builder builder = new AlertDialog.Builder(mEditableImage.getContext());
            builder.setMessage("Save image?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        FileOutputStream fos = new FileOutputStream(secondFileName);
                        mEditableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        Log.e(TAG, "File written out!!! (theoretically)" + secondFileName);
                        Log.e(TAG, "File path: " + secondFileName);
                        fos.flush();
                        fos.close();

                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                }
            });

            //if no, delete the image and finish this activity
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File fileToDelete = new File(secondFileName);
                    fileToDelete.delete();
                    finish();
                }
            });

            //create the dialog and show it
            Dialog dialog = builder.create();
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            dialog.show();
        }
    }
}
