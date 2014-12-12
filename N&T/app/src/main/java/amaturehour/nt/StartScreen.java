package amaturehour.nt;

import android.graphics.Point;
import android.view.Display;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;


public class StartScreen extends Activity {

    private Button mCapturePicture;
    private Button mSuperImpose;

    private static final String TAG = "StartActivity";
    public final static String OVERLAY_IMAGE = "amaturehour.nt.IMAGE2";
    public final static String UNDERLAY_IMAGE = "amaturehour.nt.IMAGE1";
    private static final int CAMERA_BUTTON = 1;
    private static final int SUPERIMPOSE_BUTTON = 2;
    private int buttonPressed;
    private static final int READ_REQUEST_CODE = 42;
    private String firstSIImage;
    private int counter;


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_GET_CONTENT is the intent to choose a file via the system's file
        // browser and to simply read/import data.
        // With this approach, the app imports a copy of the data, such as an image file.

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".

        Log.d(TAG, "Choosing...");
        Log.e(TAG, "Intent flag we send: " + buttonPressed);

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        super.onActivityResult(requestCode, resultCode, resultData);

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
            if (resultData != null) {
                //get the screen density, width, and height from the device display
                Context appContext = getApplicationContext();
                int density = appContext.getResources().getDisplayMetrics().densityDpi;
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                Log.e(TAG, "The screen width: " + width + " The screen height: " + height);
                //put information in an int array to send through intent to other activities to use
                int[] screenInfo = {width, height, density};
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                String overlayImage = getFileOfUri(uri);
                Log.e(TAG, "Intent flag we get: " + buttonPressed);
                //check to see if current intent was called by custom camera
                if(buttonPressed == CAMERA_BUTTON) {
                    Intent customCameraIntent = new Intent(this, CustomCamera.class);
                    customCameraIntent.putExtra("ScreenInformation", screenInfo);
                    customCameraIntent.putExtra(OVERLAY_IMAGE, overlayImage);
                    startActivity(customCameraIntent);
                }
                else if((buttonPressed == SUPERIMPOSE_BUTTON && firstSIImage == null) ||
                        (buttonPressed == SUPERIMPOSE_BUTTON && counter >= 2)) {

                    counter = 1;
                    firstSIImage = overlayImage;
                    buttonPressed = SUPERIMPOSE_BUTTON;
                    performFileSearch();
                }
                else if(buttonPressed == SUPERIMPOSE_BUTTON && firstSIImage != null){
                    counter++;
                    Intent editPictureIntent = new Intent(this, EditPicture.class);
                    editPictureIntent.addFlags(1);
                    editPictureIntent.putExtra("ScreenInformation", screenInfo);
                    editPictureIntent.putExtra(UNDERLAY_IMAGE, firstSIImage);
                    editPictureIntent.putExtra(OVERLAY_IMAGE, overlayImage);
                    startActivity(editPictureIntent);
                }

            }
        }
        else{
            Log.e(TAG, "Error getting Uri from activity");
        }
    }

    private String getFileOfUri(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String uriString = cursor.getString(column_index);
        Log.i(TAG, "URIString: " + uriString);
        cursor.close();
        return uriString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        counter = 0;
        mCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        mCapturePicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCapturePicture.setBackgroundResource(R.drawable.tap_touch);
                    buttonPressed = CAMERA_BUTTON;
                    performFileSearch();
                }
                if(event.getAction() == MotionEvent.ACTION_UP)
                    mCapturePicture.setBackgroundResource(R.drawable.tap);
                return true;
            }
        });

        mSuperImpose = (Button) findViewById(R.id.btnSuperImpose);
        mSuperImpose.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mSuperImpose.setBackgroundResource(R.drawable.si_touch);
                    buttonPressed = SUPERIMPOSE_BUTTON;
                    performFileSearch();
                }
                if(event.getAction() == MotionEvent.ACTION_UP)
                    mSuperImpose.setBackgroundResource(R.drawable.si);
                return true;
            }
        });
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

}
