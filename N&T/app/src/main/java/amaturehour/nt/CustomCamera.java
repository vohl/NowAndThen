package amaturehour.nt;

import android.view.Surface;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.io.FileNotFoundException;
import java.util.List;

public class CustomCamera extends Activity implements PictureCallback, SurfaceHolder.Callback {
    private static final String TAG = "Camera";
    public final static String OVERLAY_IMAGE2 = "amaturehour.nt.CUSTOMO";
    public final static String UNDERLAY_IMAGE2 = "amaturehour.nt.CUSTOMU";

    private Camera mCamera;
    private Button mCapture;
    private SeekBar mTransparencySeekBar;
    private SeekBar mOrientationSeekBar;
    private ImageView mOverlayImage;
    private SurfaceView mCameraPreview;
    private Bitmap mOverlayBitMap;
    private Boolean mIsCapturing;
    private String mFileName;
    private int mOrientation;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MID_ORIENTATION_RANGE = 5;
    private static final int MID_ORIENTATION = 50;
    private static final int MID_TRANSPARENCY = 125;
    private static final int MAX_TRANSPARENCY = 250;
    private static final int INDEX_OF_WIDTH = 0;
    private static final int INDEX_OF_HEIGHT = 1;
    private static final int INDEX_OF_DENSITY = 2;
    private static final int STRETCH_CONSTANT = 96;
    private static int displayWidth;
    private static int displayHeight;
    private static int displayDensity;
    private static int rotationAngle;

    private OnClickListener btnCaptureClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            captureImage(v);
        }
    };

    public void captureImage(View view){

        mCamera.takePicture(null, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        mCapture = (Button) findViewById(R.id.btnCapture);
        mCapture.setOnClickListener(btnCaptureClickListener);

        mCameraPreview = (SurfaceView)findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);

        mOverlayImage = (ImageView)findViewById(R.id.supperimpose_view);
        mOverlayImage.setVisibility(View.INVISIBLE);

        mIsCapturing = true;

        Intent intent = getIntent();
        int[] screenInfo = intent.getIntArrayExtra("ScreenInformation");

        displayWidth = screenInfo[INDEX_OF_WIDTH];
        //need to add a little vertical stretch because of the action bar dimensions??
        displayHeight = screenInfo[INDEX_OF_HEIGHT] + STRETCH_CONSTANT;
        displayDensity = screenInfo[INDEX_OF_DENSITY];

        Log.e(TAG, "display width: " + displayWidth + " display height: " + displayHeight);

        mFileName = intent.getStringExtra(StartScreen.OVERLAY_IMAGE);
        try {
            ExifInterface exif = new ExifInterface(mFileName);
            mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Log.e(TAG, "Orientation: " + mOrientation);
        }
        catch (IOException e){
            Log.e(TAG, "Error creating Exif from " + mFileName);
        }

        mOverlayBitMap = BitmapFactory.decodeFile(intent.getStringExtra(StartScreen.OVERLAY_IMAGE));

        Log.e(TAG, "Bitmap height: " + mOverlayBitMap.getHeight() + " Bitmap width: " + mOverlayBitMap.getWidth());
        Log.e(TAG, "Display height: " + displayHeight + " Display width: " + displayWidth);

        //not sure if we need this
        mOverlayBitMap.setDensity(displayDensity);



        mOverlayBitMap = rotateBitmap(mOverlayBitMap, mOrientation, mOverlayBitMap.getWidth(), mOverlayBitMap.getHeight());

        //check to see if we need to resize the bitmap

        if(mOverlayBitMap.getHeight() > displayHeight || mOverlayBitMap.getWidth() > displayWidth)
            mOverlayBitMap = getResizedBitmap(mOverlayBitMap, displayHeight, displayWidth);


        if(mOverlayBitMap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mOverlayImage.setImageBitmap(mOverlayBitMap);
        }
        //set the transparency to the mid level initially
        mOverlayImage.setAlpha(MID_TRANSPARENCY);
        //make the image visible
        mOverlayImage.setVisibility(View.VISIBLE);

        //get and respond to all the changes to the transparency slider
        mTransparencySeekBar = (SeekBar)findViewById(R.id.sliderTransparency);

        mTransparencySeekBar.setMax(MAX_TRANSPARENCY);
        mTransparencySeekBar.setProgress(MID_TRANSPARENCY);

        mTransparencySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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


        mOrientationSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null){
            try{
                mCamera = Camera.open();
                mCamera.setDisplayOrientation(90);

                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                Camera.Parameters parameter = mCamera.getParameters();
                List<Camera.Size> sizes = parameter.getSupportedPictureSizes();
                parameter.setPictureSize(sizes.get(0).width, sizes.get(0).height);
                parameter.set("orientation", "portrait");
                List<Camera.Size> size = parameter.getSupportedPreviewSizes();
                parameter.setPreviewSize(size.get(0).width, size.get(0).height);
                mCamera.setParameters(parameter);
                mCamera.startPreview();
                if(mIsCapturing){
                    mCamera.startPreview();
                }
            }
            catch(Exception e){
                Toast.makeText(this, "Unable to open camera.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.e(TAG, "Error creating media file, check storage permissions");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            Log.e(TAG, "File written out!!! (theoretically)" + pictureFile.toString());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        String path_to_captured_image = pictureFile.toString();

        Intent edit_intent = new Intent(this, EditPicture.class);
        edit_intent.addFlags(2);
        edit_intent.putExtra("scaleWidth", displayWidth);
        edit_intent.putExtra("scaleHeight", displayHeight);
        edit_intent.putExtra("Dense", displayDensity);
        edit_intent.putExtra("thisMayNotBeUsed", rotationAngle);
        edit_intent.putExtra(OVERLAY_IMAGE2, path_to_captured_image);
        edit_intent.putExtra(UNDERLAY_IMAGE2, mFileName);
        startActivity(edit_intent);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mCamera != null){
            try{
                mCamera.setPreviewDisplay(holder);
                if(mIsCapturing){
                    mCamera.startPreview();
                }
            }
            catch(IOException e){
                Toast.makeText(CustomCamera.this, "Unable to start camera preview", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //NEED TO HANDLE EXCEPTIONS HERE!!!
//        mCamera.stopPreview();
//        mCamera.release();
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation, int width, int height){
        Matrix matrix = new Matrix();

        Log.e(TAG, "Orientation value: " + orientation);

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
}
