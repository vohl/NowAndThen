package amaturehour.nowandthen;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;



public class StartScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_screen);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        StartScreenFragment fragment = (StartScreenFragment) getFragmentManager()
                .findFragmentById(R.id.contentFragment);

        // Create new fragment and transaction
        Fragment newFragment = new StartScreenFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.contentFragment, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

}
