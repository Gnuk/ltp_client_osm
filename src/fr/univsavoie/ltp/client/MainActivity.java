package fr.univsavoie.ltp.client;

import java.io.IOException;
import java.security.KeyStore;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity is the main apps class
 *
 */
public class MainActivity extends Activity 
{
	/* Global variables */
	private static MapView myOpenMapView;
	private MapController myMapController;
	private LocationManager locationManager;
	private Location lastLocation;
	private ArrayList<OverlayItem> overlayItemArray;
	private static MinimapOverlay miniMapOverlay;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
    	
    	// Create main activity
        setContentView(R.layout.activity_main);
        
        // Button settings click listner
        Button btSettings = (Button) findViewById(R.id.btSettings);
        btSettings.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
                Intent myIntent = new Intent(view.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
        
        // Button test click listner
        Button btTest = (Button) findViewById(R.id.btTest);
        btTest.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	enableMinimap(false);
            }
        });
        
        // MapView settings
        myOpenMapView = (MapView)findViewById(R.id.openmapview);
        myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);
		myOpenMapView.setBuiltInZoomControls(true);
		myOpenMapView.setMultiTouchControls(true);
		
		// MapController settings
        myMapController = myOpenMapView.getController();
        myMapController.setZoom(6);
          
        // Create map Overlay
        overlayItemArray = new ArrayList<OverlayItem>();
        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
        MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, defaultResourceProxyImpl);
        myOpenMapView.getOverlays().add(myItemizedIconOverlay);
        
        // Create a minimap overlay
        miniMapOverlay = new MinimapOverlay(this, myOpenMapView.getTileRequestCompleteHandler());
        miniMapOverlay.setZoomDifference(5);
        miniMapOverlay.setHeight(200);
        miniMapOverlay.setWidth(200);
        myOpenMapView.getOverlays().add(miniMapOverlay);

        // Trace user thanks to GPS or Network Provider or Passive Provider (with chance, on of them must work !)
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        	lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
        	lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        
        
        if(lastLocation != null) {
        	// Update current location thanks to infos send by location manager
        	updateLoc(lastLocation);
        }
        else
        {
    		// Set default GeoPoint
    		GeoPoint point2 = new GeoPoint(46.227638, 2.213749);
    		myMapController.setCenter(point2);
        }        
        
        //Add Scale Bar
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        myOpenMapView.getOverlays().add(myScaleBarOverlay);   
        
      
        DecimalFormat fmt = new DecimalFormat("#.000");
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String login = preferences.getString("ltp_login", "testuser1");
        String password = preferences.getString("ltp_password", "test");
        String url = preferences.getString("ltp_url", "http://jibiki.univ-savoie.fr/ltp");
           
		HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
			public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
				AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
				CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
				HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

				if (authState.getAuthScheme() == null) {
					AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
					Credentials creds = credsProvider.getCredentials(authScope);
					if (creds != null) {
						authState.setAuthScheme(new BasicScheme());
						authState.setCredentials(creds);
					}
				}
			}
		};
        
        DefaultHttpClient httpClient = new DefaultHttpClient();

        CredentialsProvider authCred = new BasicCredentialsProvider();
        Credentials creds = new UsernamePasswordCredentials(login, password);
        authCred.setCredentials(AuthScope.ANY, creds);
        
        httpClient.addRequestInterceptor(preemptiveAuth, 0);
        httpClient.setCredentialsProvider(authCred);
        
        HttpGet httpGet = new  HttpGet("http://jibiki.univ-savoie.fr/teapot/api/localisations/carron/friendships?format=json");
        //HttpGet httpGet = new  HttpGet(url+"/api/localisations/"+login+"/friendships?format=json");
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String response = null;
        
        ArrayList<OverlayItem> anotherOverlayItemArray = new ArrayList<OverlayItem>();
        
        try 
        {
        	//getNewHttpClient();
        	
			response = httpClient.execute(httpGet, responseHandler);

			JSONObject potes = new JSONObject(response);			
			JSONArray potesArray = potes.getJSONObject("gpx").getJSONArray("wpt");

			// Parse user frinds list into OverlayItem arraylist
	        for (int i = 0 ; (i < potesArray.length()) ; i++ )
	        {        	
	        	// Get current friend
	        	JSONObject pote = potesArray.getJSONObject(i);
	        	
		        // Prepare array of users icons in map
		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("name"), "son status ici", new GeoPoint(pote.getDouble("@lat"), pote.getDouble("@lon"))));
	        }
	      
        } catch (ClientProtocolException e) {
			Log.e("Catch", "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("Catch", "IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e("Catch", "JSONException: " + e.getMessage());
		} catch (Exception e) {
			Log.e("Catch", "Exception: " + e.getMessage());
		}
              
        // Prepare array of users icons in map
        /*ArrayList<OverlayItem> anotherOverlayItemArray;
        anotherOverlayItemArray = new ArrayList<OverlayItem>();
        anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
        anotherOverlayItemArray.add(new OverlayItem("Chuck Norris", "Alors, on va boire une bierre ?", new GeoPoint(38.883333, -77.016667)));
        anotherOverlayItemArray.add(new OverlayItem("Le Pape", "Le vatican est ouvert! !", new GeoPoint(39.916667, 116.383333)));
        anotherOverlayItemArray.add(new OverlayItem("Roger", "Un pote par ici?", new GeoPoint(51.5, -0.116667)));
        anotherOverlayItemArray.add(new OverlayItem("Marie", "Je mange des pates :D", new GeoPoint(52.516667, 13.383333)));
        anotherOverlayItemArray.add(new OverlayItem("Giu", "Italien chaud pour une soirée", new GeoPoint(38.316667, 127.233333)));
        anotherOverlayItemArray.add(new OverlayItem("Mario", "Luigi, je suis la!", new GeoPoint(28.613333, 77.208333)));
        anotherOverlayItemArray.add(new OverlayItem("Kevin", "Trolololol", new GeoPoint(55.75, 37.616667)));
        anotherOverlayItemArray.add(new OverlayItem("Alice", "Mais ou est bob ?", new GeoPoint(48.856667, 2.350833)));
        anotherOverlayItemArray.add(new OverlayItem("Bob", "Mais ou est alice ?", new GeoPoint(45.4, -75.666667)));*/
        
        // Create a new ItemizedOverlayWithFocus with our user item array
        ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
        myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);        
        
        // Setup icon overlay
        anotherItemizedIconOverlay.setFocusItemsOnTap(true);
        anotherItemizedIconOverlay.setFocusedItem(0); 
        
        // Call for update user gps coordinates
        updateUserInterface(lastLocation);      
    }
    
    OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() 
	{
        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            // TODO Auto-generated method stub
            return false;
        }
     
        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(
                MainActivity.this,
                item.mDescription + "\n" + item.mTitle + "\n"
                    + item.mGeoPoint.getLatitudeE6() + " : "
                    + item.mGeoPoint.getLongitudeE6(),
                Toast.LENGTH_LONG).show();
                 
            return true;
        }
    };
    
    @Override
	protected void onResume() 
    {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		locationManager.removeUpdates(myLocationListener);
	}

	private void updateLoc(Location loc)
	{
        // Set new local GeoPoint for point to the map
    	GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
    	myMapController.setCenter(locGeoPoint);
    	
    	setOverlayLoc(loc);
    	
    	myOpenMapView.invalidate();
    	
    	updateUserInterface(loc);
    }
	
	private void updateUserInterface(Location loc)
	{
    	// Update user localization coordinates
        TextView myLocationText = (TextView)findViewById(R.id.textViewGeolocation);
        String latLongString = "";
        if (loc != null) {
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            latLongString = "Lat:" + lat + "\nLong:" + lng;
        } else {
            latLongString = "No location found";
        }
        myLocationText.setText("Your Current Position is:\n" + latLongString);
	}
	
	private void setOverlayLoc(Location overlayloc)
	{
		GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
    	overlayItemArray.clear();
    	
    	OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", overlocGeoPoint);
    	overlayItemArray.add(newMyLocationItem);
	}
    
    private LocationListener myLocationListener = new LocationListener()
    {
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			updateLoc(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
    };
    
    private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem>
    {
		public MyItemizedIconOverlay(
				List<OverlayItem> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy) {
			super(pList, pOnItemGestureListener, pResourceProxy);
		}

		@Override
		public void draw(Canvas canvas, MapView mapview, boolean arg2) 
		{
			super.draw(canvas, mapview, arg2);
			
			if(!overlayItemArray.isEmpty()){
				
				//overlayItemArray have only ONE element only, so I hard code to get(0)
				GeoPoint in = overlayItemArray.get(0).getPoint();
				
				Point out = new Point();
				mapview.getProjection().toPixels(in, out);
				
				Bitmap bm = BitmapFactory.decodeResource(getResources(), 
						R.drawable.ic_menu_mylocation);
				canvas.drawBitmap(bm, 
						out.x - bm.getWidth()/2, 	//shift the bitmap center
						out.y - bm.getHeight()/2, 	//shift the bitmap center
						null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) 
		{
			//return super.onSingleTapUp(event, mapView);
			return true;
		}
    }
    
    public static void enableMinimap(boolean isEnabled)
    {
    	if (isEnabled)
    		myOpenMapView.getOverlays().add(miniMapOverlay);
    	else
    		myOpenMapView.getOverlays().remove(miniMapOverlay);
    }
}
