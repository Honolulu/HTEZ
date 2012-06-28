/* Copyright 2010 ESRI
 * 
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 * 
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 * 
 * See the “Sample code usage restrictions” document for further information.
 * 
 */

package gov.honolulu.floodzones;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

//import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
//import com.esri.core.geometry.Envelope;
//import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.ags.geocode.Locator;
import com.esri.core.tasks.ags.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.ags.geocode.LocatorReverseGeocodeResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class floodzones extends Activity {

	// http://www.droidnova.com/how-to-create-an-option-menu,427.html
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}

	// http://stackoverflow.com/questions/1564867/adding-a-vertical-scrollbar-to-an-alertdialog-in-android
	public void AboutDialog(){

		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setIcon(R.drawable.icon);
		ad.setTitle("About Honolulu Tsunami Evacuation Zones");
		ad.setView(LayoutInflater.from(this).inflate(R.layout.about,null));
		ad.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
			// *KS: removed for 2.0
			//@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// OK, go back to Main menu
		    }
		});

		ad.show();

	}

	// http://stackoverflow.com/questions/1564867/adding-a-vertical-scrollbar-to-an-alertdialog-in-android
	public void LegendDialog(){

		AlertDialog.Builder ad2 = new AlertDialog.Builder(this);
		ad2.setIcon(R.drawable.ic_menu_mapmode);
		ad2.setTitle("Legend");
		ad2.setView(LayoutInflater.from(this).inflate(R.layout.legend,null));

		ad2.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
		    	// OK, go back to Main menu
		    }
		});

		ad2.show();
		
	}	

	// http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
	public void SearchDialog(){

		// *KS: disable GPS before address search
		LocationService ls = map.getLocationService();
		if (ls.isStarted() == true) {
			ls.stop();
		}
		map.getCallout().hide();
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setIcon(R.drawable.ic_menu_search);
		alert.setTitle("Address Search");
		alert.setMessage("Enter 'Street, City' below (do not include dashes).\n\nFor example:\n85670 Farrington, Waianae");
		
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);

		// http://stackoverflow.com/questions/2451119/dismiss-android-preferences-dialog-on-keyboard-action-done-press
		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);

		// http://stackoverflow.com/questions/4927208/android-edittext-in-alertdialog-seems-too-wide
		input.setHeight(100);

		input.setHint("Street, City");
		alert.setView(input);

		alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				final String search1 = value;
			
				// *KS: used to pass address from dialog
				search2 = search1;
			
				// Do something with value!
				// *KS: 1.0.1 changed from clear() to removeAll()
				//graphicsLayer.clear();
				graphicsLayer.removeAll();
				map.getCallout().hide();
				findMapPoint();

			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();
		
	}
	
	// http://www.droidnova.com/how-to-create-an-option-menu,427.html
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {

        	// *KS: if location service is running, stop it. Else start it, zoom out to initial extent, and zoom in to gps location.					
			case R.id.ml:		map.getCallout().hide();
								graphicsLayer.removeAll();
								LocationService ls = map.getLocationService();
								if (ls.isStarted() == false) {
									ls.setAutoPan(true);
									ls.setBearing(true);	
									ls.start();
									Point point = ls.getPoint();
									// *KS: 1.0.1 envelope no longer accepted
									//map.zoomTo(new Envelope(-17622457.3032347, 2439157.37937499, -17546524.1970318, 2457953.93517276));
									map.zoomToScale(point, 50000);
									// *KS: 101 zoom scale different
									//map.zoomTo(point,20);	
									Toast.makeText(floodzones.this, "Select My Location again to disable GPS",Toast.LENGTH_SHORT).show();
								}
								else {								
									ls.stop();								
								};
								break;
									
	    	case R.id.search:   SearchDialog();
								break;

			// http://blog.endlesswhileloop.com/post/612168891/android-sdk-add-a-share-button-to-your-app
	    	case R.id.share:	Intent iShare = new Intent(Intent.ACTION_SEND);
	        					iShare.setType("text/plain");
	        					iShare.putExtra(Intent.EXTRA_SUBJECT, "Honolulu Tsunami Evacuation Zones Android App");
	        					iShare.putExtra(Intent.EXTRA_TEXT, "Get more info on the Honolulu Tsunami Evacuation Zones App here: http://www.honolulu.gov/mobile/htez.htm #htez");
	        					startActivity(Intent.createChooser(iShare, "Share the HTEZ App via"));
	        					break;	                            

	        case R.id.about:	AboutDialog();
	        					break;

	        case R.id.legend:	LegendDialog();
	        					break;
	                         
	        // http://mobile.tutsplus.com/tutorials/android/android-email-intent/
	        // TODO KS: version number
	        case R.id.email:	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	        					String aEmailList[] = { "androiddev@honolulu.gov" };
	        					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
	        					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Honolulu Tsunami Evacuation Zones Android App (2.0)");
	        					emailIntent.setType("plain/text");
	        					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please enter your questions and/or comments here:\n");
	        					startActivity(emailIntent);
	        					break;
	    }
	    return true;
	}	
	
	/*
	 * ArcGIS Android elements
	 */
	MapView map = null;
	GraphicsLayer graphicsLayer = null;
	Locator al;
	
	// *KS: 101 new variable
	/*
	Envelope initextent;
	public static boolean EXTERNAL_TILE_CACHE_ENABLED = true;
	public static String EXTERNAL_CACHE_DIR_NAME = "A_HTEZ";
	public static long CONTEXT_TILE_CACHE_SIZE = 5000;
	public static int TILE_FILE_KB_SIZE = 50;
	*/

	//Dynamic layer URL from ArcGIS online
	// *KS: from AddLayer example
	String dynamicMapURL = "http://gis.hicentral.com/arcgis/rest/services/OperPublicSafety/MapServer";
	
	String baseMapURL = "http://tiles.arcgis.com/tiles/tNJpAOha4mODLkXz/arcgis/rest/services/CCHBasemap/MapServer";
	//String baseMapURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer";
	//String baseMapURL = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer";
	//String baseMapURL = "http://gis.hicentral.com/arcgis/rest/services/BaseStreetMap10/MapServer";
	
	//Layer id for dynamic layer 
	// *KS: from AddLayer example
	//int usaLayerId;
	
	/*
	 * Android UI elements
	 */
	//Graphic markerGraphic;

	// *KS: used to pass address from dialog
	public String search2;
	
	/*
	 * UI Handler
	 */
	ProgressDialog dialog = null;
	static final int CLOSE_LOADING_WINDOW = 0;
	static final int CANCEL_LOADING_WINDOW = 1;
	Timer cancelLocate = new Timer();

	private Handler uiHandler = new Handler(new Callback() {

		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CLOSE_LOADING_WINDOW:
				if (dialog != null) {
					dialog.dismiss();
				}
				cancelLocate.cancel();
				break;
			case CANCEL_LOADING_WINDOW:
				if (dialog != null) {
					dialog.dismiss();
				}
				Toast toast = Toast.makeText(floodzones.this,
						"Locate canceled", Toast.LENGTH_SHORT);
				toast.show();
				break;
			}
			return false;
		}

	});

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// http://stackoverflow.com/questions/2786720/android-service-ping-url
		try {
            URL url = new URL("http://gis.hicentral.com/arcgis/rest/services/OperPublicSafety/MapServer");
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("User-Agent", "Android Application");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 30); // mTimeout is in seconds
            urlc.connect();
		} catch (MalformedURLException e1) {
            // Auto-generated catch block
    		Toast.makeText(floodzones.this, "Sorry, the Tsunami Evacuation Zone layer is currently unavailable. Please check your Internet connection and try again later.",Toast.LENGTH_LONG).show();
            e1.printStackTrace();
		} catch (IOException e) {
            // Auto-generated catch block
			Toast.makeText(floodzones.this, "Sorry, the Tsunami Evacuation Zone layer is currently unavailable. Please check your Internet connection and try again later.",Toast.LENGTH_LONG).show();
            e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floodzones);
		
		/*
		 * initialize ArcGIS Android MapView, Graphics Layer, Android UI
		 * elements
		 */
		map = (MapView) findViewById(R.id.map);
		// *KS: 101 no longer accepted
		//graphicsLayer = (GraphicsLayer) findViewById(R.id.graphics);
		//map = new MapView(this);
		//initextent = new Envelope(-17622457.3032347, 2439157.37937499, -17546524.1970318, 2457953.93517276);
		//map.setExtent(initextent, 0);
		
		// *KS: 101 new way to add basemap
		//ArcGISDynamicMapServiceLayer baseMap = new ArcGISDynamicMapServiceLayer(baseMapURL);
		
		ArcGISTiledMapServiceLayer baseMap = new ArcGISTiledMapServiceLayer(baseMapURL);		
		this.map.addLayer(baseMap);
		
		//this.map.setResolution(1);
		//this.usaLayerId = 1;
		//map.addLayer(new ArcGISDynamicMapServiceLayer(
				//"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));

		//Creates a dynamic layer using service URL 
		// *KS: from AddLayer example
		// *KS: 101 no longer accepted
		//ArcGISDynamicMapServiceLayer dynamicLayer = new ArcGISDynamicMapServiceLayer(this, this.dynamicMapURL);
		ArcGISDynamicMapServiceLayer dynamicLayer = new ArcGISDynamicMapServiceLayer(dynamicMapURL);
		//Adds layer into the 'MapView'
		this.map.addLayer(dynamicLayer);
		//this.usaLayerId = 1234;
		// *KS: 101 no longer accepted
		//dynamicLayer.setId(this.usaLayerId);	

		// *KS: 101 setOpacity
		float layerOpacity = 0.5f;
		dynamicLayer.setOpacity(layerOpacity);
		dynamicLayer.refresh();
		
		// *KS: 101 graphics handling
		graphicsLayer = new GraphicsLayer();
		map.addLayer(graphicsLayer);
		
		// *KS: add Esri watermark
        ImageView image = (ImageView) findViewById(R.id.esri);
        image.setImageResource(R.drawable.esri);
		
        // *KS: instructions to tap menu button
     	//Toast.makeText(floodzones.this, "Tap the Menu button for more options", Toast.LENGTH_SHORT).show();	
        
		// perform reverse geocode on single tap.
		map.setOnSingleTapListener(new OnSingleTapListener() {		
			
			// *KS: used in if comparison
			public String callout2;
			
			private static final long serialVersionUID = 1L;

			public void onSingleTap(final float x, final float y) {

				// *KS: disable GPS before address search
				LocationService ls = map.getLocationService();
				if (ls.isStarted() == true) {
					ls.stop();
				}
				
				// *KS: 101 clear() changed to removeAll()
				//graphicsLayer.clear();
				graphicsLayer.removeAll();

				// retrieve the user clicked location
				final Point loc = map.toMapPoint(x, y);

				// initialize arcgis locator 
				al = new Locator(
						"http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Locators/ESRI_Geocode_USA/GeocodeServer");
						// *KS: modified to use TA_Address_NA geocoder
						//"http://tasks.arcgisonline.com/ArcGIS/rest/services/Locators/TA_Address_NA/GeocodeServer");
				try {

					// *KS: new for 1.1
					//int wkid = map.getSpatialReference().getID();
					// retrieve LocatorReverseGeocodeResult
					//LocatorReverseGeocodeResult result = al.reverseGeocode(loc, 1000.00, wkid, wkid);
					
					/*
					 * API v1.1 method signature changed to take SpatialReference
					 * parameter instead of int wkid.
					 */
					SpatialReference sr = map.getSpatialReference();
					// retrieve LocatorReverseGeocodeResult
					LocatorReverseGeocodeResult result = al.reverseGeocode(loc,
							1000.00, sr, sr);

					// checks if State and Zip is present in the result
					if (result.getAddressFields().get("State").length() != 2 && result.getAddressFields().get("Zip").length() != 5) {

						// *KS: 101 no longer accepted
						//map.getCallout().setAnchor(Callout.ANCHOR_POSITION_FLOATING);
						map.getCallout().show(loc, message("No Address Found."));

					} else {

						// display the result in map callout 
						// *KS: modified to pass as parameter to Google Navigation
						// *KS: 101 no longer accepted
						//map.getCallout().setAnchor(Callout.ANCHOR_POSITION_FLOATING);
						String msg = result.getAddressFields().get("Address")
								+ "\n"
								+ result.getAddressFields().get("City")
								+ ", "
								+ result.getAddressFields().get("State")
								+ " "
								+ result.getAddressFields().get("Zip");

						// *KS: used in comparison
						final String callout1 = msg;
						
						// *KS: if a callout exists, then continue
						if (map.getCallout().isShowing() == true) {
							
							// http://mobile.tutsplus.com/tutorials/android/java-strings/
							// *KS: if callouts match, add callout to map then pass address as parameter to Google Navigation
							if (callout1.compareTo(callout2) == 0) {
								map.getCallout().show(loc, message(msg));
								// http://groups.google.com/group/android-developers/browse_thread/thread/83d2803f799c6f5f?pli=1
								Intent navigation = new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q=" + msg));
								startActivity(navigation);
							}
							else {
								// *KS: if callouts do not match, display new callout and save as variable for next comparison
								map.getCallout().show(loc, message(msg));
								callout2 = callout1;
								Toast.makeText(floodzones.this, "Tap the same address again to start Google Navigation.",Toast.LENGTH_SHORT).show();
							}
						}
						// *KS: if no callout exists, create one
						else{
							map.getCallout().show(loc, message(msg));
							callout2 = callout1;
							Toast.makeText(floodzones.this, "Tap the same address again to start Google Navigation.",Toast.LENGTH_SHORT).show();
						}			
    					
					}
				} catch (Exception e) {

					e.printStackTrace();
					// *KS: 101 no longer accepted
					//map.getCallout().setAnchor(Callout.ANCHOR_POSITION_FLOATING);
					map.getCallout().show(loc, message("No Address Found."));

				}

			}
		});	
	
	}

	/*
	 * Cutomize the map Callout text
	 */
	private TextView message(String text) {

		final TextView msg = new TextView(this);
		msg.setText(text);
		msg.setTextSize(12);
		msg.setTextColor(Color.BLACK);
		return msg;

	}

	// *KS: added cleanup for location service
	@Override
    protected void onPause() {
        super.onPause();
        map.pause();
        /*
        LocationService ls = map.getLocationService();
		
		if (ls.isStarted() == true) {
			ls.stop();
		}
		*/
    }
	
	@Override 	protected void onResume() {
		super.onResume(); 
		map.unpause();
	}
	
	/*
	 * Execute geocode task asynchronously.
	 */
	class AgsLocator extends AsyncTask<java.util.Map<java.lang.String, java.lang.String>, Void, java.util.List<LocatorGeocodeResult>> {

		@Override
		protected void onPostExecute(java.util.List<LocatorGeocodeResult> result) {

			if (result.size() == 0) {
				Toast toast = Toast.makeText(floodzones.this, "No result found.", Toast.LENGTH_SHORT);
				toast.show();

			} else {
				dialog = ProgressDialog.show(floodzones.this, "", "Loading. Please wait...", true, true);
				cancelLocate = new Timer();
				cancelLocate.schedule(new TimerTask() {

					@Override
					public void run() {
						uiHandler.sendEmptyMessage(CANCEL_LOADING_WINDOW);
					}

				}, 60000);

				// *KS: 101 entire section updated
				/*
				// Create graphic to add locator result to map 
				Graphic graphicS = new Graphic();
				Graphic graphicT = new Graphic();
				
				// *KS: Changed to a bigger blue circle to match LocationService GPS symbol
				graphicS.setSymbol(new SimpleMarkerSymbol(Color.BLUE, 15, SimpleMarkerSymbol.STYLE.CIRCLE));

				graphicS.setGeometry(result.get(0).getLocation());
				graphicT.setGeometry(result.get(0).getLocation());
				
				TextSymbol ts = new TextSymbol(16, result.get(0).getAddress(), Color.BLACK);
				ts.setOffsetX(15);
				ts.setOffsetY(15);
				graphicT.setSymbol(ts);
				*/

				Geometry geomS = result.get(0).getLocation();
				//SimpleMarkerSymbol smsS = new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.DIAMOND);
				SimpleMarkerSymbol smsS = new SimpleMarkerSymbol(Color.BLUE, 15, SimpleMarkerSymbol.STYLE.CIRCLE);
				Graphic graphicS = new Graphic( geomS, smsS );
				
				Geometry geomT = result.get(0).getLocation();
				TextSymbol tsT = new TextSymbol(16, result.get(0).getAddress(), Color.BLACK);
				tsT.setOffsetX(15);
				tsT.setOffsetY(15);
				Graphic graphicT = new Graphic( geomT, tsT );
				
				/*
				 * add the updated graphic to graphics layer and display the
				 * result on the map
				 */
				graphicsLayer.addGraphic(graphicS);
				graphicsLayer.addGraphic(graphicT);

				// zoom to the locator result 
				// *KS: modified to zoom out to initial extent then in closer to result, and finally push tsunami layer back
				// *KS: 101 no longer accepted
				//map.zoomTo(new Envelope(-17622457.3032347, 2439157.37937499, -17546524.1970318, 2457953.93517276));
				// *KS: 101 zoom different
				//map.zoomTo(result.get(0).getLocation(), 20);
				map.zoomToScale(result.get(0).getLocation(), 50000);
				// *KS: 101 no longer accepted
				//map.reorderLayer(1234, 1);

				uiHandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);

				// *KS: 101 no longer accepted
				//graphicsLayer.invalidate();
			}
		}

		@Override
		protected List<LocatorGeocodeResult> doInBackground(Map<String, String>... params) {

			// *KS: new for 1.1
			//int wkid = map.getSpatialReference().getID();
			SpatialReference sr = map.getSpatialReference();
			List<LocatorGeocodeResult> results = null;

			// initialize arcgis locator 
			al = new Locator(
			//"http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Locators/ESRI_Geocode_USA/GeocodeServer");
			// *KS: modified to use TA_Address_NA geocoder
			"http://tasks.arcgisonline.com/ArcGIS/rest/services/Locators/TA_Address_NA/GeocodeServer");
			try {
				/*
				 * API v1.1 method signature changed to take SpatialReference
				 * parameter instead of int wkid.
				 */
				// perform geocode operation
				results = al.geocode(params[0], null, sr);
				// *KS: new for 1.1
				// perform geocode operation
				//results = al.geocode(params[0], null, wkid);
			} catch (Exception e) {
				e.getMessage();
			}
			return results;
		}

	}

	@SuppressWarnings("unchecked")
	public void findMapPoint() {

		try {
			HashMap<String, String> addressFields = new HashMap<String, String>();
			// get the user entered address and create arcgis locator address fields 
			// *KS: modified to accept dialog input and requires only street and city			
			//String line = edtText.getText().toString();
			String line = search2;

			StringTokenizer st = new StringTokenizer(line, ",");

			//if (st.countTokens() == 4) {
			if (st.countTokens() == 3) {

				addressFields.put("Address", st.nextToken());
				addressFields.put("City", st.nextToken());
				//addressFields.put("State", st.nextToken());
				addressFields.put("State", "HI");
				addressFields.put("Zip", st.nextToken());

			//} else if (st.countTokens() == 3) {
			} else if (st.countTokens() == 2) {

				addressFields.put("Address", st.nextToken());
				addressFields.put("City", st.nextToken());
				//addressFields.put("State", st.nextToken());
				addressFields.put("State", "HI");

			} else {

				Toast toast = Toast.makeText(floodzones.this,"Please enter address in correct format.",Toast.LENGTH_SHORT);
				toast.show();

			}

			if (addressFields.get("Address") != null && addressFields.get("State") != null) {

				new AgsLocator().execute(addressFields);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}