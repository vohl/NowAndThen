package amaturehour.nt;

import android.app.ActionBar;
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
import android.widget.Toast;
import java.io.IOException;


public class CustomCamera extends Activity implements PictureCallback, SurfaceHolder.Callback {

    public static final String EXTRA_CAMERA_DATA = "camera_data";

    private static final String TAG = "Camera";

    private Camera mCamera;
    private Button mCapture;
    private ImageView mOverlayImage;
    private SurfaceView mCameraPreview;
    private Bitmap mOverlayBitMap;
    private byte[] mCameraData;
    private Boolean mIsCapturing;
    private String mFileName;
    private int mTransparency;
    private int mOrientation;

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
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        mCapture = (Button) findViewById(R.id.btnCapture);
        mCapture.setOnClickListener(btnCaptureClickListener);

        mCameraPreview = (SurfaceView)findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);

        mOverlayImage = (ImageView)findViewById(R.id.supperimpose_view);
        mOverlayImage.setVisibility(View.INVISIBLE);

        mIsCapturing = true;

        Intent intent = getIntent();
        mFileName = intent.getStringExtra(ChoosePicture.OVERLAY_IMAGE);
        try {
            ExifInterface exif = new ExifInterface(mFileName);
            mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        }
        catch (IOException e){
            Log.e(TAG, "Error creating Exif from " + mFileName);
        }

        mOverlayBitMap = BitmapFactory.decodeFile(intent.getStringExtra(ChoosePicture.OVERLAY_IMAGE));
        mOverlayBitMap = rotateBitmap(mOverlayBitMap, mOrientation);
        if(mOverlayBitMap == null){
            Log.e(TAG, "Error making the Bitmap - Null");
        }
        else{
            Log.i(TAG, "Bitmap success - Not Null");
            mOverlayImage.setImageBitmap(mOverlayBitMap);
        }

        mTransparency = 122;

        mOverlayImage.setAlpha(mTransparency);

        mOverlayImage.setVisibility(View.VISIBLE);
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
        mCamera.stopPreview();
        mCamera.release();
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation){
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
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }
}
