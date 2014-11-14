package amaturehour.nt;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class CustomCamera extends Activity implements PictureCallback, SurfaceHolder.Callback {

    public static final String EXTRA_CAMERA_DATA = "camera_data";

    private Camera mCamera;
    private ImageView mOverlayImage;
    private SurfaceView mCameraPreview;
    private byte[] mCameraData;
    private Boolean mIsCapturing;

    public void captureImage(View view){
        mCamera.takePicture(null, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mOverlayImage = (ImageView)findViewById(R.id.overlay_image);
        mOverlayImage.setVisibility(View.INVISIBLE);

        mCameraPreview = (SurfaceView)findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);

        mIsCapturing = true;

        Intent intent = getIntent();
        Uri uri = intent.getData();
        String thisBitch = uri.toString();
        try{
            URL aURL = new URL(thisBitch);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        }
        catch(IOException e){
            Log.e("dasd", "asdas", e);
        }
        //mOverlayImage.setAlpha((float) .75);
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
//                if(mIsCapturing){
//                    mCamera.startPreview();
//                }
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
        if (mCamera != null) {
//            try {
//                mCamera.setPreviewDisplay(holder);
//                if (mIsCapturing) {
//                    mCamera.startPreview();
//                }
//            } catch (IOException e) {
//                Toast.makeText(CustomCamera.this, "Unable to start camera preview", Toast.LENGTH_LONG).show();
//            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
