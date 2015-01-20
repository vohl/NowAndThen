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
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Context;
import android.os.Environment;
import android.widget.ProgressBar;
import android.app.Dialog;
import android.view.Window;
import android.widget.SeekBar;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditPicture extends Activity{

    private ImageView mUneditableImage;
    private ImageView mEditableImage;
    private ImageView mOverlayImage;
    private ImageView mUnderlayImage;

    private DrawingView mDrawingView;

    private Button mCutButton;

    private String firstFileName;
    private String secondFileName;

    private int mOrientation;

    private Bitmap mUneditableBitmap;
    private Bitmap mEditableBitmap;
    private Bitmap mDrawingBitmap;

    private ProgressBar mProgressBar;

    private static File savedImage;
    private static Context mContext;

    private static int displayWidth;
    private static int displayHeight;
    private static int displayDensity;
    private static int rotationAngle;


    private static enum shapes {RECTANGLE, SQUARE, CIRCLE, TRIANGLE};

    private SeekBar mTransparencySeekBar;
    private SeekBar mOrientationSeekBar;


    private static final String TAG = "EDIT";
    private static final int SUPERIMPOSE = 1;
    private static final int CUSTOM_CAMERA = 2;

    private static final int RECTANGLE = 1;
    private static final int SQUARE = 2;
    private static final int CIRCLE = 3;
    private static final int TRIANGLE = 4;

    private static final int INDEX_OF_WIDTH = 0;
    private static final int INDEX_OF_HEIGHT = 1;
    private static final int INDEX_OF_DENSITY = 2;
    private static final int STRETCH_CONSTANT = 96;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MID_ORIENTATION_RANGE = 5;
    private static final int MID_ORIENTATION = 50;
    private static final int MID_TRANSPARENCY = 125;
    private static final int MAX_TRANSPARENCY = 250;
    private static final int RESAMPLE_IMAGE = 7;



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

        //from start screen activity
        if(origin == SUPERIMPOSE) {
            processSuperimpose(intent);
        }
        //from custom camera activity
        else if(origin == CUSTOM_CAMERA){
            processCustomCamera(intent);
        }

        displayImagesForEdit();

        mDrawingView = (DrawingView)findViewById(R.id.drawing_view);
        mDrawingView.setCurrentShape(RECTANGLE);
        mDrawingView.setVisibility(View.VISIBLE);

        displaySeekbars();

        mCutButton = (Button)findViewById(R.id.cut);

        mCutButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCutButton.setBackgroundResource(R.drawable.cut_pressed);
                    cutoutShapes();
                }
                if(event.getAction() == MotionEvent.ACTION_UP)
                    mCutButton.setBackgroundResource(R.drawable.cut);
                return true;            }
        });

        Log.e(TAG, "Editable bitmap height: " + mEditableBitmap.getHeight() + " & width: " +
            mEditableBitmap.getWidth() + " Uneditable bitmap height: " + mUneditableBitmap.getHeight()
            + " width: " + mUneditableBitmap.getWidth());

//        splitVerticalMiddle();

    }

    //sets all usable information in this edit picture activity based on
    //this activity being called from superimpose activity
    private void processSuperimpose(Intent intent){
        int[] screenInfo = intent.getIntArrayExtra("ScreenInformation");
        displayWidth = screenInfo[INDEX_OF_WIDTH];
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

        //set the options to return a bitmap that can fit on any devices screen size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = RESAMPLE_IMAGE;
        o.outHeight = displayHeight;
        o.outWidth = displayWidth;
        o.inMutable = true;

        mUneditableBitmap = BitmapFactory.decodeFile(firstFileName, o);

        Log.e(TAG, "Uneditable Bitmap: " + firstFileName);
        if(mUneditableBitmap == null)
            Log.e(TAG, "Something's wrong with the file you dummy!!!");

        Log.e(TAG, "orientation" + mOrientation + " UEB width: " + mUneditableBitmap.getWidth() + " UEB height: " + mUneditableBitmap.getHeight());

        mUneditableBitmap = rotateBitmap(mUneditableBitmap, mOrientation, mUneditableBitmap.getWidth(), mUneditableBitmap.getHeight());

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

        mEditableBitmap = BitmapFactory.decodeFile(secondFileName, o);

        mEditableBitmap = rotateBitmap(mEditableBitmap, mOrientation, mEditableBitmap.getWidth(), mEditableBitmap.getHeight());

        if(mEditableBitmap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
        }

        Log.e(TAG, "Editable bitmap height in process Superimpose: " + mEditableBitmap.getHeight() + " & width: " +
                mEditableBitmap.getWidth() + " Uneditable bitmap height: " + mUneditableBitmap.getHeight()
                + " width: " + mUneditableBitmap.getWidth());

    }

    //sets all usable information in this edit picture activity based on
    //this activity being called from custom camera activity
    private void processCustomCamera(Intent intent){
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

        //set the options to return a bitmap that can fit on any devices screen size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = RESAMPLE_IMAGE;
        o.outHeight = displayHeight;
        o.outWidth = displayWidth;
        o.inMutable = true;

        mUneditableBitmap = BitmapFactory.decodeFile(firstFileName, o);
        mUneditableBitmap = rotateBitmap(mUneditableBitmap, mOrientation, mUneditableBitmap.getWidth(), mUneditableBitmap.getHeight());

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

        mEditableBitmap = BitmapFactory.decodeFile(secondFileName, o);

        mEditableBitmap.setDensity(displayDensity);

        //mEditableBitmap = rotateBitmap(mEditableBitmap, mOrientation, mEditableBitmap.getWidth(), mEditableBitmap.getHeight());
        if (mEditableBitmap.getWidth() > mEditableBitmap.getHeight())
            mEditableBitmap = fixOrientation(mEditableBitmap);

        if(mEditableBitmap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mUneditableImage.setImageBitmap(mEditableBitmap);
        }
        mUneditableImage.setVisibility(View.VISIBLE);

        //not sure if we need this
        mEditableBitmap.setDensity(displayDensity);

        if(mEditableBitmap.getWidth() > mEditableBitmap.getHeight())
            mEditableBitmap = fixOrientation(mEditableBitmap);


        Log.e(TAG, "Editable bitmap height in process custom camera: " + mEditableBitmap.getHeight() + " & width: " +
                mEditableBitmap.getWidth() + " Uneditable bitmap height: " + mUneditableBitmap.getHeight()
                + " width: " + mUneditableBitmap.getWidth());

    }

    //combines the two photos vertically down the middle of the two photos
    private void splitVerticalMiddle(){
        Bitmap[] bmpArray = {mEditableBitmap, mUneditableBitmap};

        mContext = mEditableImage.getContext();

        mProgressBar.setVisibility(View.VISIBLE);

        new ProcessImageTask().execute(bmpArray);
    }

    //combines the two photos vertically down the middle of the two photos
    private void cutoutShapes(){

        Bitmap[] bmpArray = {mEditableBitmap, mUneditableBitmap, mDrawingView.mBitmap};

        mProgressBar.setVisibility(View.VISIBLE);

        new ProcessImageTask().execute(bmpArray);
    }

    //get the views and images and display them for the user to edit
    //and interact with
    private void displayImagesForEdit(){
        mUnderlayImage = (ImageView)findViewById(R.id.uneditable_image);
        if(mUneditableBitmap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mUnderlayImage.setImageBitmap(mUneditableBitmap);
        }
        mUnderlayImage.setAlpha(MID_TRANSPARENCY);

        //make the image visible
        mUnderlayImage.setVisibility(View.VISIBLE);

        mOverlayImage = (ImageView)findViewById(R.id.editable_image);

        if(mEditableBitmap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mOverlayImage.setImageBitmap(mEditableBitmap);
        }
        //set the transparency to the mid level initially
        mOverlayImage.setAlpha(MID_TRANSPARENCY);
        //make the image visible
        mOverlayImage.setVisibility(View.VISIBLE);
    }

    private void displaySeekbars(){

        //get and respond to all the changes to the transparency slider
        mTransparencySeekBar = (SeekBar)findViewById(R.id.sliderTransparency);

        mTransparencySeekBar.setMax(MAX_TRANSPARENCY);
        mTransparencySeekBar.setProgress(MID_TRANSPARENCY);

        mTransparencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                if(progress <= MAX_TRANSPARENCY) {
                    mOverlayImage.setAlpha(progress);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //get and respond to all the changes to the orientation slider
        mOrientationSeekBar = (SeekBar)findViewById(R.id.sliderRotater);

        mOrientationSeekBar.setProgress(MID_ORIENTATION);


        mOrientationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                Log.i(TAG, "Progress before gravity: " + progress);
                progress -= MID_ORIENTATION;
                if((progress <= MID_ORIENTATION_RANGE && progress >= 0) ||
                        (progress <= 0 && progress >= -MID_ORIENTATION_RANGE)) {
                    progress = 0;
                }
                Log.i(TAG, "Progress after gravity: " + progress);
                rotationAngle = progress;
                mOverlayImage.setRotation(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    // Create a File for saving an image or video
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
        Bitmap cuts = Bitmap.createScaledBitmap(srcBmp[2], srcBmp[0].getWidth(), srcBmp[0].getHeight(), false);
        for (int i = 0; i < srcBmp[0].getWidth(); i++) {
            for(int j = 0; j < srcBmp[0].getHeight(); j++){
                if(cuts.getPixel(i, j) != 0) {
                    srcBmp[0].setPixel(i, j, srcBmp[1].getPixel(i, j));
                }
            }
        }
        return  srcBmp[0];
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
            mUneditableImage.setVisibility(View.INVISIBLE);
            mOrientationSeekBar.setVisibility(View.INVISIBLE);
            mTransparencySeekBar.setVisibility(View.INVISIBLE);
            mDrawingView.setVisibility(View.INVISIBLE);
            mCutButton.setVisibility(View.INVISIBLE);

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
                    mEditableBitmap.recycle();
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
