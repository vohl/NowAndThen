package amaturehour.nt;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;


public class StartScreen extends Activity {

    private Button mCapturePicture;
    private Button mSuperImpose;

    private OnClickListener btnCapturePictureClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            takePicture(v);
        }
    };

    private OnClickListener btnSuperImposeClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            superImpose(v);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        mCapturePicture = (Button) findViewById(R.id.btnCapturePicture);
        mCapturePicture.setOnClickListener(btnCapturePictureClickListener);

        mSuperImpose = (Button) findViewById(R.id.btnSuperImpose);
        mSuperImpose.setOnClickListener(btnSuperImposeClickListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_screen, menu);
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

    public void takePicture(View view){
        Intent pic_intent = new Intent(this, ChoosePicture.class);
        startActivity(pic_intent);
    }

    public void superImpose(View view) {
        Intent pic_intent = new Intent(this, ChoosePicture.class);
        startActivity(pic_intent);
    }
}
