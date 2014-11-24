package amaturehour.nt;

import android.content.Context;
import android.app.ActionBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
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
import java.io.IOException;


public class CustomCamera extends Activity implements PictureCallback, SurfaceHolder.Callback {

    public static final String EXTRA_CAMERA_DATA = "camera_data";

    private static final String TAG = "Camera";

    private Camera mCamera;
    private Button mCapture;
    private SeekBar mTransparencySeekBar;
    private ImageView mOverlayImage;
    private SurfaceView mCameraPreview;
    private Bitmap mOverlayBitMap;
    private byte[] mCameraData;
    private Boolean mIsCapturing;
    private String mFileName;
    private int mOrientation;

    private static final int MIN_TRANSPARENCY = 0;
    private static final int MID_TRANSPARENCY = 125;
    private static final int MAX_TRANSPARENCY = 250;
    private static final int INDEX_OF_WIDTH = 0;
    private static final int INDEX_OF_HEIGHT = 1;
    private static final int INDEX_OF_DENSITY = 2;
    private static final int STRETCH_CONSTANT = 96;

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

        int displayWidth = screenInfo[INDEX_OF_WIDTH];
        //need to add a little vertical stretch because of the action bar dimensions??
        int displayHeight = screenInfo[INDEX_OF_HEIGHT] + STRETCH_CONSTANT;
        int displayDensity = screenInfo[INDEX_OF_DENSITY];

        Log.e(TAG, "display width: " + displayWidth + " display height: " + displayHeight);

        mFileName = intent.getStringExtra(StartScreen.OVERLAY_IMAGE);
        try {
            ExifInterface exif = new ExifInterface(mFileName);
            mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        }
        catch (IOException e){
            Log.e(TAG, "Error creating Exif from " + mFileName);
        }

        mOverlayBitMap = BitmapFactory.decodeFile(intent.getStringExtra(StartScreen.OVERLAY_IMAGE));
        mOverlayBitMap = getResizedBitmap(mOverlayBitMap, displayHeight, displayWidth);
        mOverlayBitMap = rotateBitmap(mOverlayBitMap, mOrientation, displayWidth, displayHeight);
        if(mOverlayBitMap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mOverlayImage.setImageBitmap(mOverlayBitMap);
        }

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

        mOverlayImage.setAlpha(MID_TRANSPARENCY);

        mOverlayImage.setVisibility(View.VISIBLE);
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

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        Intent edit_intent = new Intent(this, EditPicture.class);
        edit_intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
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
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }
}
