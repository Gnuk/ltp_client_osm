package fr.univsavoie.ltp.client;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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

import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity l'activit� principale
 * de l'application Android
 */
public class MainActivity extends Activity 
{
	//The "x" and "y" position of the "Show Button" on screen.
	Point p;
	
	/* Global variables */
	private static MapView myOpenMapView;
	private MapController myMapController;
	private LocationManager locationManager;
	private Location lastLocation;
	private ArrayList<OverlayItem> overlayItemArray;
	private static MinimapOverlay miniMapOverlay;
	private boolean estAfficherMiniCarte = true;
	
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private ResponseHandler<String> responseHandler;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)  
    {
    	super.onCreate(savedInstanceState);
    	
    	// Cr�ation de l'activit� principale
        setContentView(R.layout.activity_main);    
     
        try {
			// Ecouteur d'�v�nement sur le bouton des param�tres
			Button btSettings = (Button) findViewById(R.id.btSettings);
			btSettings.setOnClickListener(new View.OnClickListener() 
			{
			    public void onClick(View view) 
			    {
			        Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
			        startActivityForResult(myIntent, 0);
			    }
			});
			
			// Ecouteur d'�v�nement sur le bouton d'affichage de la mini-carte
			Button btTest = (Button) findViewById(R.id.btTest);
			btTest.setOnClickListener(new View.OnClickListener() 
			{
			    public void onClick(View view) 
			    {
			    	if(estAfficherMiniCarte)
			    	{
			    		estAfficherMiniCarte = false;
			    		enableMinimap(estAfficherMiniCarte);
			    	}
			    	else
			    	{
			    		estAfficherMiniCarte = true;
			    		enableMinimap(estAfficherMiniCarte);
			    	}
			    }
			});
			
			Button btn_show = (Button) findViewById(R.id.buttonMyAccount);
			btn_show.setOnClickListener(new OnClickListener() {
			  @Override
			  public void onClick(View arg0) {

			    //Open popup window
			    if (p != null)
			    showPopup(MainActivity.this, p);
			  }
			});
			
			// Ecouteur d'�v�nement sur le bouton de connexion
			/*final EditText champID = (EditText) findViewById(R.id.champId);
			final EditText champMDP = (EditText) findViewById(R.id.champMDP);
			Button boutonConnexion = (Button) findViewById(R.id.boutonConnexion);
			boutonConnexion.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					System.out.println(champID.getText());
					System.out.println(champMDP.getText());
				}
			});*/
			
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
			//WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			//Display display = wm.getDefaultDisplay();
			//miniMapOverlay = new MinimapOverlay(this, myOpenMapView.getTileRequestCompleteHandler());
			//miniMapOverlay.setZoomDifference(5);
			//miniMapOverlay.setHeight(100);
			//miniMapOverlay.setWidth(100);
			//myOpenMapView.getOverlays().add(miniMapOverlay);

			// Trace user thanks to GPS or Network Provider or Passive Provider (with chance, on of them must work !)
			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			
			if(lastLocation != null) 
			{
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
			
			// Obtenir les pr�f�rences partag�es
			//SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
			//String login = prefs.getString("USER_EMAIL", null);
			//String password = prefs.getString("USER_PASSWORD", null);

			//if (login != null)
			//{
				// Appeler la fonction pour s'authentifier aupr�s du service LTP
				//auth();
				
				// Appeler la fonction pour parser les amis et les affich�s sur la carte
				//parseFriends();
			//}

			// Call for update user gps coordinates
			updateUserInterface(lastLocation);
			      
			// Prepare array of users icons in map
			/*ArrayList<OverlayItem> anotherOverlayItemArray;
			anotherOverlayItemArray = new ArrayList<OverlayItem>();
			anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
			anotherOverlayItemArray.add(new OverlayItem("Chuck Norris", "Alors, on va boire une bierre ?", new GeoPoint(38.883333, -77.016667)));
			anotherOverlayItemArray.add(new OverlayItem("Le Pape", "Le vatican est ouvert! !", new GeoPoint(39.916667, 116.383333)));
			anotherOverlayItemArray.add(new OverlayItem("Roger", "Un pote par ici?", new GeoPoint(51.5, -0.116667)));
			anotherOverlayItemArray.add(new OverlayItem("Marie", "Je mange des pates :D", new GeoPoint(52.516667, 13.383333)));
			anotherOverlayItemArray.add(new OverlayItem("Giu", "Italien chaud pour une soir�e", new GeoPoint(38.316667, 127.233333)));
			anotherOverlayItemArray.add(new OverlayItem("Mario", "Luigi, je suis la!", new GeoPoint(28.613333, 77.208333)));
			anotherOverlayItemArray.add(new OverlayItem("Kevin", "Trolololol", new GeoPoint(55.75, 37.616667)));
			anotherOverlayItemArray.add(new OverlayItem("Alice", "Mais ou est bob ?", new GeoPoint(48.856667, 2.350833)));
			anotherOverlayItemArray.add(new OverlayItem("Bob", "Mais ou est alice ?", new GeoPoint(45.4, -75.666667)));*/
		} 
        catch (Exception e) 
        {
        	Log.e("Catch", "> onCreate() - Exception : " + e.getMessage());
		}
    }
    
    /**
     * Fonction pour parser sur la carte, les amis de l'utilisateur connect�
     */
    public void parseFriends()
    {
    	String response = null;
    	
        ArrayList<OverlayItem> anotherOverlayItemArray = new ArrayList<OverlayItem>();
        
        try 
        {
        	response = httpClient.execute(httpGet, responseHandler);

			JSONObject potes = new JSONObject(response);			
			JSONArray potesArray = potes.getJSONObject("gpx").getJSONArray("wpt");

			// Parse user frinds list into OverlayItem arraylist
	        for (int i = 0 ; (i < potesArray.length()) ; i++ )
	        {        	
	        	// Get current friend
	        	JSONObject pote = potesArray.getJSONObject(i);
	        	
		        // Prepare array of users icons in map
		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("name"), "status", new GeoPoint(pote.getDouble("@lat"), pote.getDouble("@lon"))));
	        }
	        
            // Create a new ItemizedOverlayWithFocus with our user item array
            ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
            myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);   
            
            // Setup icon overlay
            anotherItemizedIconOverlay.setFocusItemsOnTap(true);
            anotherItemizedIconOverlay.setFocusedItem(0);   
        } 
        catch (ClientProtocolException e) 
        {
			Log.e("Catch", "> parseFriends() - ClientProtocolException: " + e.getMessage());
		} 
        catch (IOException e) 
        {
			Log.e("Catch", "> parseFriends() - IOException: " + e.getMessage());
		} 
        catch (JSONException e) 
        {
			Log.e("Catch", "> parseFriends() - JSONException: " + e.getMessage());
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> parseFriends() - Exception: " + e.getMessage());
		}
    }
    
    /**
     * S'authentifier aupr�s du service LocalizeTeaPot
     */
    public void auth() 
	{
		try 
		{
			// Obtenir les pr�f�rences partag�es
			SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
			String login = prefs.getString("USER_EMAIL", null);
			String password = prefs.getString("USER_PASSWORD", null);
			
			Log.i("Watch", "Login : " + login);
			Log.i("Watch", "Mot de passe : " + password);
			
			HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() 
			{
				public void process(final HttpRequest request,
						final HttpContext context) throws HttpException, IOException {
					AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
					CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
					HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

					if (authState.getAuthScheme() == null) 
					{
						AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
						Credentials creds = credsProvider.getCredentials(authScope);
						if (creds != null) 
						{
							authState.setAuthScheme(new BasicScheme());
							authState.setCredentials(creds);
						}
					}
				}
			};

			// Setup a custom SSL Factory object which simply ignore the
			// certificates
			// validation and accept all type of self signed certificates
			SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
			sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Enable HTTP parameters
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			// Register the HTTP and HTTPS Protocols. For HTTPS, register our
			// custom SSL Factory object.
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sslFactory, 443));

			// Create a new connection manager using the newly created registry
			// and then create a new HTTP client using this connection manager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager( params, registry);

			httpClient = new DefaultHttpClient(ccm, params);
			//httpClient = new DefaultHttpClient();

			CredentialsProvider authCred = new BasicCredentialsProvider();
			Credentials creds = new UsernamePasswordCredentials(login, password);
			authCred.setCredentials(AuthScope.ANY, creds);

			httpClient.addRequestInterceptor(preemptiveAuth, 0);
			httpClient.setCredentialsProvider(authCred);

			//httpGet = new HttpGet("http://jibiki.univ-savoie.fr/teapot/api/localisations/carron/friendships?format=json");
			httpGet = new HttpGet("https://jibiki.univ-savoie.fr/ltpdev/?p=json");
			//HttpGet httpGet = new HttpGet(url+"/api/localisations/"+login+"/friendships?format=json");
			responseHandler = new BasicResponseHandler();
		} 
		catch (KeyManagementException e1) 
		{
			Log.e("Catch", "> auth() - KeyManagementException: " + e1.getMessage());
		} 
		catch (NoSuchAlgorithmException e1) 
		{
			Log.e("Catch", "> auth() - NoSuchAlgorithmException: " + e1.getMessage());
		} 
		catch (KeyStoreException e1) 
		{
			Log.e("Catch", "> auth() - KeyStoreException: " + e1.getMessage());
		} 
		catch (UnrecoverableKeyException e1) 
		{
			Log.e("Catch", "> auth() - UnrecoverableKeyException: " + e1.getMessage());
		}
	}
    
    OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() 
	{
        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
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
        try 
        {
			// Set new local GeoPoint for point to the map
			GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
			myMapController.setCenter(locGeoPoint);
			
			setOverlayLoc(loc);
			
			myOpenMapView.invalidate();
			
			updateUserInterface(loc);
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> updateLoc() - Exception : "  + e.getMessage());
		}
    }
	
	private void updateUserInterface(Location loc)
	{
    	try 
    	{
			// Update user localization coordinates
			TextView myLocationText = (TextView)findViewById(R.id.textViewGeolocation);
			String latLongString = "";
			if (loc != null) {
			    double lat = loc.getLatitude();
			    double lng = loc.getLongitude();
			    latLongString = "Lat:" + lat + ", Long:" + lng;
			} else {
			    latLongString = "No location found";
			}
			myLocationText.setText("Your Current Position is: " + latLongString);
		} 
    	catch (Exception e) 
    	{
			Log.e("Catch", "> updateUserInterface() - Exception : "  + e.getMessage());
		}
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
			updateLoc(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
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
    
	// Get the x and y position after the button is draw on screen
	// (It's important to note that we can't get the position in the onCreate(),
	// because at that stage most probably the view isn't drawn yet, so it will
	// return (0, 0))
	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{

		int[] location = new int[2];
		Button button = (Button) findViewById(R.id.buttonMyAccount);

		// Get the x, y location and store it in the location[] array
		// location[0] = x, location[1] = y.
		if (button != null)
			button.getLocationOnScreen(location);

		// Initialize the Point with x, and y positions
		p = new Point();
		p.x = location[0];
		p.y = location[1];
	}
 
 	/**
 	 * Afficher une popup
 	 * @param context
 	 * @param p
 	 */
	private void showPopup(final Activity context, Point p) 
	{
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = width / 2;
		int popupHeight = height / 2;

		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_account_layout,viewGroup);
		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Creating the PopupWindow
		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth(popupWidth);
		popup.setHeight(popupHeight);
		popup.setFocusable(true);

		// Some offset to align the popup a bit to the right, and a bit down,
		// relative to button's position.
		int OFFSET_X = popupWidth / 2;
		int OFFSET_Y = popupHeight / 2;

		// Clear the default translucent background
		popup.setBackgroundDrawable(new BitmapDrawable());

		// Displaying the popup at the specified location, + offsets.
		popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
		
        // Ecouteur d'�v�nement sur le bouton des param�tres
        Button btLogin = (Button) layout.findViewById(R.id.buttonConnexion);
        btLogin.setOnClickListener(new OnClickListener() 
        {
        	@Override
            public void onClick(View view) 
            {
        		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        		startActivity(intent);
            }
        });

		// Getting a reference to Close button, and close the popup when
		// clicked.
		Button close = (Button) layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popup.dismiss();
			}
		});
	}
    
	/**
	 * Afficher ou pas la minimap
	 * @param isEnabled
	 */
	public static void enableMinimap(boolean isEnabled) 
	{
		if (isEnabled)
			myOpenMapView.getOverlays().add(miniMapOverlay);
		else
			myOpenMapView.getOverlays().remove(miniMapOverlay);
	}
}
