package gov.honolulu.floodzones;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class main extends Activity {

	// http://code.google.com/p/mytracks/source/browse/MyTracks/src/com/google/android/apps/mytracks/Eula.java
	final Activity activity = this;
	private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
	private static final String PREFERENCES_EULA = "eula";
	
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

	    final SharedPreferences preferences =
	        activity.getSharedPreferences(PREFERENCES_EULA, Activity.MODE_PRIVATE);
	    // *KS: added == false condition. If eula not already accepted, call process
	    if (preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false) == false ) {
	    	Eula.showEulaRequireAcceptance(this);
	    	//return;
	    }
	    // *KS: if eula already accepted, proceed to gpscheck
	    else {	
	    	Intent intent = new Intent(activity, checkenablegps.class);activity.startActivity(intent);
	    	activity.finish();
	    }
		
	}
  
}