package gov.honolulu.floodzones;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class checkenablegps extends Activity {
    
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
        CheckEnableGPS();
		
	}
	
	// http://android-er.blogspot.com/2010/10/check-and-prompt-user-to-enable-gps.html
	private void CheckEnableGPS(){    
		String provider = Settings.Secure.getString(getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if(!provider.equals("")){
				//GPS Enabled *KS: disabled toast
				//Toast.makeText(main.this, "Location Enabled: " + provider,Toast.LENGTH_LONG).show();
				// *KS: instructions to tap menu button
		     	Toast.makeText(checkenablegps.this, "Tap the Menu button for more options", Toast.LENGTH_LONG).show();	
				Intent intent = new Intent(checkenablegps.this, floodzones.class);
				startActivity(intent);
				this.finish();
			}
			else{
				Toast.makeText(checkenablegps.this, "Please enable Wireless Networks and GPS Satellites",Toast.LENGTH_LONG).show();
				//*KS: this works with vanilla Android
				//Intent intent2 = new Intent(Settings.ACTION_SECURITY_SETTINGS);startActivity(intent2);
				//*KS: this works with vanilla Android and Sense
				Intent intent2 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);startActivity(intent2);
				}
			}

	// *KS: recheck if GPS/network location was enabled
    @Override
    protected void onResume() {
    	
        super.onResume();
        CheckEnableGPS();
        
    }
    
}