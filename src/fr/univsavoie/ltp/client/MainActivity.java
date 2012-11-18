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
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import fr.univsavoie.ltp.client.tools.SharedVariables;
import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
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
import android.os.Handler;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity l'activité principale de l'application Android
 */
public class MainActivity extends Activity 
{
	/* --------------------------------
	 * Variables globales de l'activité
	 * --------------------------------
	 */
	
	/* Variables de OSMDroid */
	private SimpleLocationOverlay mMyLocationOverlay;
	private MapView myOpenMapView;
	private MapController myMapController;
	private LocationManager locationManager;
	private Location lastLocation;
	private ArrayList<OverlayItem> overlayItemArray;
	private MinimapOverlay miniMapOverlay;
	
	/* Variables du service HTTP / Apache */
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private ResponseHandler<String> responseHandler;
	
	/* Variables de gestion */
	private boolean displayAuthBox, displayMiniMap;
	private String login;
	private int STATIC_INTEGER_VALUE = 0;
	private BroadcastReceiver myMessageReceiver;
	private Point p;
	
	private PopupWindow popupUserInfos;
	
	
    /* --------------------------------------------------------
     * Evenements de l'activity (onCreate, onResume, onStop...)
     * --------------------------------------------------------
     */
	
	/** Appelé quand l'activité est crée */
    @Override
    public void onCreate(Bundle savedInstanceState)  
    {
    	super.onCreate(savedInstanceState);
    	
    	// Création de l'activité principale
        setContentView(R.layout.activity_main);  
        
        // L'utilisateur est t'il deja connecté ?
        displayAuthBox = SharedVariables.displayAuthbox;
        
		// Instance de SharedPreferences pour lire les données dans un fichier
		SharedPreferences myPrefs = this.getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); 
		login = myPrefs.getString("Email", null);
		displayMiniMap = myPrefs.getBoolean("DisplayMinimap", false);
        
        try 
        {
        	if (displayAuthBox)
        	{
    			if (login == null)
    				displayGuestPopup();
    			else
    				displayUserPopup();
        	}
			
			// MapView settings
			myOpenMapView = (MapView)findViewById(R.id.openmapview);
			myOpenMapView.setTileSource(TileSourceFactory.MAPNIK);
			myOpenMapView.setBuiltInZoomControls(true);
			myOpenMapView.setMultiTouchControls(true);
			
			// MapController settings
			myMapController = myOpenMapView.getController();
			myMapController.setZoom(6);
			
			mMyLocationOverlay = new SimpleLocationOverlay(this);                          
            myOpenMapView.getOverlays().add(mMyLocationOverlay);
			  
			// Create map Overlay
			overlayItemArray = new ArrayList<OverlayItem>();
			DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
			MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, defaultResourceProxyImpl);
			myOpenMapView.getOverlays().add(myItemizedIconOverlay);

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
			
			// Call for update user gps coordinates
			updateUserInterface(lastLocation);
			
			// Connecter l'utilisateur et parser ses amis
			if (login != null)
			{
				// Appeler la fonction pour s'authentifier auprès du service LTP
				auth();
				
				// Appeler la fonction pour parser les amis et les affichés sur la carte
				parseFriends();
			}
			
			//Add Scale Bar
			ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
			myOpenMapView.getOverlays().add(myScaleBarOverlay);
			
			// Create a minimap overlay
			miniMapOverlay = new MinimapOverlay(this, myOpenMapView.getTileRequestCompleteHandler());
			miniMapOverlay.setZoomDifference(10);
			miniMapOverlay.setHeight(100);
			miniMapOverlay.setWidth(100);
			if (displayMiniMap) // Selon les paramètres utilisateur, afficher ou pas la minimap
				myOpenMapView.getOverlays().add(miniMapOverlay);
			else
				if (myOpenMapView.getOverlays().contains(miniMapOverlay))
					myOpenMapView.getOverlays().remove(miniMapOverlay);
			      
			// Prepare array of users icons in map
			/*ArrayList<OverlayItem> anotherOverlayItemArray;
			anotherOverlayItemArray = new ArrayList<OverlayItem>();
			anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
			anotherOverlayItemArray.add(new OverlayItem("Chuck Norris", "Alors, on va boire une bierre ?", new GeoPoint(38.883333, -77.016667)));*/
			
			/*
			 * Evenements sur composants
			 */
			
			// Ecouteur d'évènement sur le bouton des paramètres
			Button btSettings = (Button) findViewById(R.id.btSettings);
			btSettings.setOnClickListener(new View.OnClickListener() 
			{
			    public void onClick(View view) 
			    {
			        Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
			        startActivityForResult(myIntent, 0);
			    }
			});
			
			// Ecouteur d'évènement sur le bouton des paramètres
			Button btAccount = (Button) findViewById(R.id.buttonMyAccount);
			btAccount.setOnClickListener(new View.OnClickListener() 
			{
			    public void onClick(View view) 
			    {
					if (login == null)
						displayGuestPopup();
					else
						displayUserPopup();
			    }
			});
		} 
        catch (Exception e) 
        {
        	Log.e("Catch", "> onCreate() - Exception : " + e.getMessage());
		}
    }
    
	/*@Override
	protected void onStop() 
	{
		//popupUserInfos.dismiss();
		//super.onStop();
	}*/
    
    @Override
	protected void onResume() 
    {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
		} 
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
		}
		super.onResume();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		
		locationManager.removeUpdates(myLocationListener);
		
		if (myMessageReceiver != null){
	        unregisterReceiver(myMessageReceiver );
	        myMessageReceiver = null;
	    }
	}
	
	/*@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		try 
		{
			if (receiver != null) 
			{
				unregisterReceiver(receiver);
				receiver = null;
			}
		} 
		catch (Exception e) 
		{
			
		}
	}*/
	
	
	
	/* ----------------------------------------
	 * Fonctions et procédures de l'application
	 * ----------------------------------------
	 */
    
    /**
     * Fonction pour parser sur la carte, les amis de l'utilisateur connecté
     */
    @SuppressLint("NewApi")
	public void parseFriends()
    {
    	String response = null;
    	
        ArrayList<OverlayItem> anotherOverlayItemArray = new ArrayList<OverlayItem>();
        
        try 
        {
			if (android.os.Build.VERSION.RELEASE.startsWith("2.3") || android.os.Build.VERSION.RELEASE.startsWith("3.") || 
					android.os.Build.VERSION.RELEASE.startsWith("4.")) 
			{
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
				
				Log.i("Watch", "StrictMode");
			}

        	
        	response = httpClient.execute(httpGet, responseHandler);

			JSONObject potes = new JSONObject(response);			
			JSONArray potesArray = potes.getJSONObject("gpx").getJSONArray("wpt");
			
			Log.i("Watch", "Passage par: parseFriends()");
			
			if (potesArray.length() < 1)
				return;

			// Parse user frinds list into OverlayItem arraylist
	        for (int i = 0 ; (i < potesArray.length()) ; i++ )
	        {        	
	        	// Get current friend
	        	JSONObject pote = potesArray.getJSONObject(i);
	        	
		        // Prepare array of users icons in map
		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("name"), pote.getString("desc"), new GeoPoint(pote.getDouble("@lat"), pote.getDouble("@lon"))));
	        }
	        
            // Create a new ItemizedOverlayWithFocus with our user item array
            ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
            myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
            myOpenMapView.refreshDrawableState();
            myOpenMapView.postInvalidate();
            
            // Setup icon overlay
            anotherItemizedIconOverlay.setFocusItemsOnTap(true);
            anotherItemizedIconOverlay.setFocusedItem(0);  
            
			runOnUiThread(new Runnable() {
				public void run() {
					myOpenMapView.postInvalidate();
				}
			});
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
     * S'authentifier auprès du service LocalizeTeaPot
     */
    public void auth() 
	{
		try
		{
			// Instance de SharedPreferences pour lire les données dans un fichier
			SharedPreferences myPrefs = this.getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); 
			String login = myPrefs.getString("Email", null);
			String password = myPrefs.getString("Password", null);
			
			Log.i("Watch", "login : " + login);
			Log.i("Watch", "password : " + password);
			
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
			//registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
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
			//httpGet = new HttpGet("http://jibiki.univ-savoie.fr/ltp/api/localisations/"+login+"/friendships?format=json");
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
	
	/**
	 * Mise a jours des coordonnées géographique de l'utilisateur
	 * @param loc
	 */
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
    
    /**
     * Classe qui gère l'affichage de la localisation GPS sur la MapView
     */
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
				
				Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_menu_mylocation);
				canvas.drawBitmap(bm, 
						out.x - bm.getWidth()/2, 	//shift the bitmap center
						out.y - bm.getHeight()/2, 	//shift the bitmap center
						null);
			}
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event, MapView mapView) 
		{
			return super.onSingleTapUp(event, mapView);
			//return true;
		}
    }
    
    private void displayUserPopup()
    {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = width / 2;
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_account,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		popupUserInfos = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupUserInfos.setBackgroundDrawable(new BitmapDrawable());
		popupUserInfos.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		findViewById(R.id.layoutMain).post(new Runnable() 
		{
			@Override
			public void run() 
			{
				popupUserInfos.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
        // Ecouteur d'évènement sur le bouton pour se déconnecter
		Button close = (Button) layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				popupUserInfos.dismiss();
			}
		});
		
        // Ecouteur d'évènement sur le bouton pour fermer l'application
		Button logout = (Button) layout.findViewById(R.id.btLogout);
		logout.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
		        // Instance de SharedPreferences pour enregistrer des données dans un fichier
		        SharedPreferences myPrefs = getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
		        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
		        prefsEditor.putString("Email", null); // Données
		        prefsEditor.putString("Password", null); // Données
		        prefsEditor.commit(); // Valider les modifications
		        
		        // Redémarrer l'activité pour prendre en compte les modifications
		        resetActivity();
			}
		});
    }
    
    /**
     * Afficher une boite au milieu de la carte si aucun utilisateur est connectés
     * pour proposer a l'invité, de se connecter ou s'incrire aupres du service LTP.
     */
    private void displayGuestPopup()
    {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = width / 2;
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_guest,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popup = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popup.setBackgroundDrawable(new BitmapDrawable());
		popup.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		findViewById(R.id.layoutMain).post(new Runnable() 
		{
			@Override
			public void run() 
			{
				popup.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
        // Ecouteur d'évènement sur le bouton des paramètres
        Button btLogin = (Button) layout.findViewById(R.id.buttonConnexion);
        btLogin.setOnClickListener(new OnClickListener() 
        {
        	@Override
            public void onClick(View view) 
            {
        		//Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        		//startActivity(intent);
        		SharedVariables.displayAuthbox = false;
        		
        		Intent i = new Intent(MainActivity.this, LoginActivity.class);    
        		startActivityForResult(i, STATIC_INTEGER_VALUE);
        		
        		popup.dismiss();
            }
        });

        // Ecouteur d'évènement sur le bouton pour fermer l'application
		Button close = (Button) layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				popup.dismiss();
			}
		});
    }

	/**
	 * Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du téléphone
	 */
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Création d'un MenuInflater qui va permettre d'instancier un Menu XML en un objet Menu
        MenuInflater inflater = getMenuInflater();
        
        // Instanciation du menu XML spécifier en un objet Menu
        inflater.inflate(R.menu.activity_main, menu);
        
        return true;
     }
    
    /**
     * Méthode qui se déclenchera au clic sur un item
     */
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// On regarde quel item a été cliqué grâce à son id et on déclenche une action
		switch (item.getItemId()) 
		{	
		case R.id.menuSettings:
			Intent i = new Intent(this, SettingsActivity.class);
	        startActivity(i);
	        
	        return true;
		case R.id.menuExit:
			finish();
			return true;
		}
		return false;
	}
	
	public void resetActivity()
	{
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) 
		{
			// On relance l'activité car on doit rafraichir la map !
			finish();
			startActivity(getIntent());
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
}
