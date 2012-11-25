package fr.univsavoie.ltp.client;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import fr.univsavoie.ltp.client.tools.SharedVariables;
import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity l'activité principale de l'application Android
 */
public class MainActivity extends SherlockActivity 
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
	
	/* Variables du service HTTP / Apache */
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private ResponseHandler<String> responseHandler;
	
	/* Variables de gestion */
	private boolean displayAuthBox, displayUserInfos;
	private String login;
	private PopupWindow popupGuest, popupUserInfos;
	
	/* Variables constantes */
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	
    /* --------------------------------------------------------
     * Evenements de l'activity (onCreate, onResume, onStop...)
     * --------------------------------------------------------
     */
	
	/** Appelé quand l'activité est crée */
    @Override
    public void onCreate(Bundle savedInstanceState)  
    {
    	super.onCreate(savedInstanceState);
    	
    	// Appliquer le thème LTP a l'ActionBar
    	setTheme(R.style.Theme_ltp);
    	
    	// Création de l'activité principale
        setContentView(R.layout.activity_main);
        
        Log.e("Watch", "Activity Start");
        
        // Afficher la ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        
        // Initialiser tout ce qui est données utilisateur propres a l'activité
        init();
        
        
    }
    
	// Création d'un menu contenant un bouton rafraichir et un menu déroulant
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add(0, 1, 0, "Retour à ma position").setIcon(R.drawable.ic_10_device_access_location_found)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, 2, 1, "Paramètres de l'application").setIcon(R.drawable.ic_2_action_settings)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(0, 3, 2, "Rafraichir la carte").setIcon(R.drawable.ic_1_navigation_refresh)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		// Afficher le menu selon si utilisateur connecté ou pas
		
		if (login != null) 
		{
			menu.add(0, 0, 0, "Publier un status").setIcon(R.drawable.ic_6_social_chat)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			
			SubMenu sub = menu.addSubMenu(0, 10, 3, "Mon compte");
			sub.add(0, 11, 4, "Afficher mes informations");
			sub.add(0, 12, 5, "Se déconnecter");
			sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} 
		else 
		{
			SubMenu sub = menu.addSubMenu(0, 20, 6, "Mon compte");
			sub.add(0, 21, 7, "S'inscrire");
			sub.add(0, 22, 8, "Se connecter");
			sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}

		return true;
	}

	// Methode callback appelée lorqu'un item du menu est sélcetionné
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Le bouton "retour" a le même title que la page.
		if (item.getTitle().toString().compareTo(getTitle().toString()) == 0)
			finish();
		
		Log.d("Watch", "LOGIN: " + login);

		Log.d("Watch", String.valueOf(item.getItemId()));

		switch (item.getItemId()) 
		{
		// Quitter l'application
		case 0:
			finish();
			break;

		// Se refixer au dernier point localisé de l'utilisateur
		case 1:
			updateLoc(lastLocation);
			break;

		// Afficher les préférences
		case 2:
			Intent i = new Intent(MainActivity.this, UserPreferencesActivity.class);
			startActivityForResult(i, 2);
			break;

		// Inscrire l'utilisateur
		case 21:
			Intent j = new Intent(MainActivity.this, SignupActivity.class);
			startActivityForResult(j, 3);
			break;
			
		// Connecter l'utilisateur
		case 22:
			Intent k = new Intent(MainActivity.this, LoginActivity.class);
			startActivityForResult(k, 1);
			break;
			
		// Afficher les dernieres infos de l'utilisateur connecté
		case 11:
			popupDisplayUserInfos();
			break;			
			
		// Déconnecter l'utilisateur actif
		case 12:
			userLogout();
			break;

		default:
			break;
		}
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);

		// Le code de requête est utilisé pour identifier l’activité enfant
		switch (requestCode) 
		{
		// Cas si authentification
		case 1:
			switch (resultCode) 
			{
			case RESULT_OK:
				Toast.makeText(this, "Authentification réussit !", Toast.LENGTH_LONG).show();
				setup();
				auth();
				parseFriends();
				return;
			case RESULT_CANCELED:
				Toast.makeText(this, "Authentification échouée !", Toast.LENGTH_LONG).show();
				return;
			default:
				// Faire quelque chose
				return;
			}

		// Cas si paramètres utilisateur
		case 2:
			switch (resultCode) 
			{
			case RESULT_OK:
				// On relance l'activité car on doit rafraichir la map !
				finish();
				startActivity(getIntent());
				return;
			case RESULT_CANCELED:
				Toast.makeText(this, "Erreur lors de la modification des préférences !", Toast.LENGTH_LONG).show();
				return;
			default:
				// Faire quelque chose
				return;
			}

		// Cas si inscription
		case 3:
			switch (resultCode) 
			{
			case RESULT_OK:
				Toast.makeText(this, "Inscription réussit : vous pouvez vous authentifier !", Toast.LENGTH_LONG).show();
				return;
			case RESULT_CANCELED:
				Toast.makeText(this, "Echec lors de l'inscription: verifier vos paramètres de connexion réseau !", Toast.LENGTH_LONG).show();
				return;
			default:
				// Faire quelque chose
				return;
			}

			// Aucune activité en retour...
		default:
			// Faire quelque chose
			return;
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		Toast.makeText(MainActivity.this,
				"onConfigurationChanged(): " + newConfig.toString(),
				Toast.LENGTH_SHORT).show();
	}
    
	@Override
	protected void onStop() 
	{
		super.onStop();
	}
    
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();

		if (login == null) {
			SharedVariables.displayAuthbox = true;
		}

		finish();
	}
	
    @Override
	protected void onResume() 
    {
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, myLocationListener);
		} 
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, myLocationListener);
		}
		 
		super.onResume();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
	}
	
	
	
	/* ----------------------------------------
	 * Fonctions et procédures de l'application
	 * ----------------------------------------
	 */
	
	/**
	 * Procédure qui s'occupe de mettre a jours certains variables
	 * locales à l'activité par rapport à la session utilisateur, affichage
	 * ou pas de certains composants...
	 */
	private void setup()
	{
		// On recupère les préférences utilisateurs paramètrer dans l'activité des Paramètres
		SharedPreferences userPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		displayUserInfos = userPrefs.getBoolean("checkBoxDisplayUserInfos", false);
		
        // L'utilisateur est t'il deja connecté ?
        displayAuthBox = SharedVariables.displayAuthbox;
        
		// Instance de SharedPreferences pour lire les données dans un fichier
		SharedPreferences myPrefs = this.getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); 
		login = myPrefs.getString("Email", null);
		
		// Afficher la boite de dialogue au démarrage ?
		// Si oui, celle anonyme ou utilisateur connecté ?
		if (login == null)
			popupGuest();
		else
			if (displayUserInfos)
				popupDisplayUserInfos();
    	
    	// On met a jours la barre infos de l'utilisateur
    	TextView txtUserStatus = (TextView)findViewById(R.id.textViewUserStatus);
		if (login == null)
			txtUserStatus.setText("Salut, Etranger, connecte toi!");
		else
			txtUserStatus.setText("Salut, " + login + "! ");
	}
	
    /**
     * Initialiser les composants de l'application
     */
    private void init()
    {
    	setup();
        
        try 
        {
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
			
			// Call for update user gps coordinates
			updateUserGPSInfos(lastLocation);
			
			// Connecter l'utilisateur et parser ses amis
			if (login != null)
			{
				// Appeler la fonction pour s'authentifier auprès du service LTP
				auth();
				
				// Appeler la fonction pour parser les amis et les affichés sur la carte
				parseFriends();
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
			//ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
			//myOpenMapView.getOverlays().add(myScaleBarOverlay);
			      
			// Prepare array of users icons in map
			/*ArrayList<OverlayItem> anotherOverlayItemArray;
			anotherOverlayItemArray = new ArrayList<OverlayItem>();
			anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
			anotherOverlayItemArray.add(new OverlayItem("Chuck Norris", "Alors, on va boire une bierre ?", new GeoPoint(38.883333, -77.016667)));*/
        } 
        catch (Exception e) 
        {
        	Log.e("Catch", "> onCreate() - Exception : " + e.getMessage());
		}
    }
    
    /**
     * La barre d'infos qui se situe après l'action bar
     */
    public void infoBar(String pMessage, boolean isVisible)
    {
    	// On définit le message a afficher
    	TextView msg = (TextView)findViewById(R.id.textViewUserStatus);
    	msg.setText(pMessage);
    	
    	// On charge le linearLayout ou on affiche la barre d'infos
		LinearLayout layoutInfos = (LinearLayout) findViewById(R.id.linearLayoutInfos);
		
		// On lance une nouvelle animation pour afficher et faire disparaitre la barre
		Animation animationFadeInOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in_out);
		layoutInfos.startAnimation(animationFadeInOut);
		
		/*int fadeInDuration = 3500; // Configure time values here
	    int timeBetween = 5000;
	    int fadeOutDuration = 3500;
		
		Animation fadeIn = new AlphaAnimation(0, 1);
	    fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
	    fadeIn.setDuration(fadeInDuration);

	    Animation fadeOut = new AlphaAnimation(1, 0);
	    fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
	    fadeOut.setStartOffset(fadeInDuration + timeBetween);
	    fadeOut.setDuration(fadeOutDuration);

	    AnimationSet animation = new AnimationSet(false); // change to false
	    animation.addAnimation(fadeIn);
	    animation.addAnimation(fadeOut);
	    //animation.setRepeatCount(0);
	    layoutInfos.setAnimation(animation);*/
    }
    
    /**
     * Fonction pour parser sur la carte, les amis de l'utilisateur connecté
     */
	public void parseFriends()
    {
    	String response = null;
    	
        anotherOverlayItemArray = new ArrayList<OverlayItem>();
        
        try 
        {
        	// FIX POUR EVITER QUE L'APPLI PLANTE SUR DES VERSION ANDROID AU SDK > 8
        	// Ce code remplace le StrictMode qui est pas dispo dans les versions d'android < 2.3...
        	// mais c'est pas terrible, ca peux ne pas fonctionner d'une config a une autre !
        	int SDK_INT = android.os.Build.VERSION.SDK_INT;
			if (SDK_INT>8) 
			{
				try 
				{
			        Class<?> strictModeClass = Class.forName("android.os.StrictMode", true, Thread.currentThread()
			                .getContextClassLoader());

			        Class<?> threadPolicyClass = Class.forName("android.os.StrictMode$ThreadPolicy", true, Thread
			                .currentThread().getContextClassLoader());

			        Class<?> threadPolicyBuilderClass = Class.forName("android.os.StrictMode$ThreadPolicy$Builder", true,
			                Thread.currentThread().getContextClassLoader());

			        Method setThreadPolicyMethod = strictModeClass.getMethod("setThreadPolicy", threadPolicyClass);

			        Method detectAllMethod = threadPolicyBuilderClass.getMethod("detectAll");
			        Method penaltyMethod = threadPolicyBuilderClass.getMethod("penaltyLog");
			        Method buildMethod = threadPolicyBuilderClass.getMethod("build");

			        Constructor<?> threadPolicyBuilderConstructor = threadPolicyBuilderClass.getConstructor();
			        Object threadPolicyBuilderObject = threadPolicyBuilderConstructor.newInstance();

			        Object obj = detectAllMethod.invoke(threadPolicyBuilderObject);

			        obj = penaltyMethod.invoke(obj);
			        Object threadPolicyObject = buildMethod.invoke(obj);
			        setThreadPolicyMethod.invoke(strictModeClass, threadPolicyObject);

			    } 
				catch (Exception ex) 
				{
			        
			    }
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
				@Override
				public void run() {
					myOpenMapView.postInvalidate();
				}
			});
		} catch (ClientProtocolException e) {
			Log.e("Catch", "> parseFriends() - ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e("Catch", "> parseFriends() - IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e("Catch", "> parseFriends() - JSONException: " + e.getMessage());
		} catch (Exception e) {
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
				@Override
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
			httpGet = new HttpGet("https://jibiki.univ-savoie.fr/ltpdev/?p=rest&format=json&service=status&method=get");
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
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(
                MainActivity.this,
                item.mDescription + "\n" + item.mTitle + "\n"
                    + item.mGeoPoint.getLatitudeE6() + " : "
                    + item.mGeoPoint.getLongitudeE6(),
                Toast.LENGTH_LONG).show();
                 
            return true;
        }

		@Override
		public boolean onItemLongPress(int arg0, OverlayItem arg1) {
			Log.d("Watch", "ouais!!!!!");
			return false;
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
			
			updateUserGPSInfos(loc);
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> updateLoc() - Exception : "  + e.getMessage());
		}
    }
	
	/**
	 * On met a jours les infos utilisateurs de sa situation géographique dans
	 * les divers affichages de l'application.
	 * @param loc Coordonnées GPS lat et long.
	 */
	private void updateUserGPSInfos(Location loc)
	{
    	try
    	{
			// Update user localization coordinates
			String latLongString = "";
			if (loc != null) 
			{
			    double lat = loc.getLatitude();
			    double lng = loc.getLongitude();
			    latLongString = "Lat:" + lat + ", Long:" + lng;
			    
				Geocoder gcd = new Geocoder(this, Locale.getDefault());
				List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
				if (addresses.size() > 0) 
				    System.out.println(addresses.get(0).getLocality());
			    
			    infoBar("T'es localisé: " + addresses.get(0).getLocality(), true);
			} 
			else 
			{
				infoBar("Ta position actuel n'a pas été trouvé !" + latLongString, true);
			}
			
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
			//updateLoc(location);
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
		public MyItemizedIconOverlay
		(
				List<OverlayItem> pList,
				org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
				ResourceProxy pResourceProxy) 
		{
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
			//return super.onSingleTapUp(event, mapView);
			return true;
		}
		
		@Override
		public boolean onLongPress(MotionEvent event, MapView mapView)
		{
			return false;
			//return super.onLongPress(event, mapView);
		}
    }
    
    /**
     * Procédure qui déconnecte l'utilisateur actif
     */
    private void userLogout()
    {
        // Instance de SharedPreferences pour enregistrer des données dans un fichier
        SharedPreferences myPrefs = getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
        prefsEditor.putString("Email", null); // Données
        prefsEditor.putString("Password", null); // Données
        prefsEditor.commit(); // Valider les modifications
        
        SharedVariables.displayAuthbox = true;
        
        // Redémarrer l'activité pour prendre en compte les modifications
		Intent intent = getIntent();
		finish();
		startActivity(intent);
    }
    
    /**
     * Afficher sur la map un popup qui affiche les
     * informations de l'utilisateur connecté.
     */
    private void popupDisplayUserInfos()
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
    }
    
    /**
     * Afficher une boite au milieu de la carte si aucun utilisateur est connectés
     * pour proposer a l'invité, de se connecter ou s'incrire aupres du service LTP.
     */
    private void popupGuest()
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
		popupGuest = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupGuest.setBackgroundDrawable(new BitmapDrawable());
		popupGuest.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		findViewById(R.id.layoutMain).post(new Runnable() 
		{
			@Override
			public void run() 
			{
				popupGuest.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
        // Ecouteur d'évènement sur le bouton des paramètres
        Button btLogin = (Button) layout.findViewById(R.id.btnPopupLogin);
        btLogin.setOnClickListener(new OnClickListener() 
        {
        	@Override
            public void onClick(View view) 
            {
        		//Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        		//startActivity(intent);
        		SharedVariables.displayAuthbox = false;
        		
        		// La constante CODE_MON_ACTIVITE représente l’identifiant de la requête
        		// (requestCode) qui sera utilisé plus tard pour identifier l’activité
        		// renvoyant la valeur de retour.
        		Intent i = new Intent(MainActivity.this, LoginActivity.class);    
        		startActivityForResult(i, 1);
        		
        		popupGuest.dismiss();
            }
        });
        
        // Ecouteur d'évènement sur le bouton des paramètres
        Button btSignup = (Button) layout.findViewById(R.id.btnPopupSignup);
        btSignup.setOnClickListener(new OnClickListener() 
        {
        	@Override
            public void onClick(View view) 
            {
        		//Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        		//startActivity(intent);
        		SharedVariables.displayAuthbox = false;
        		
        		// La constante CODE_MON_ACTIVITE représente l’identifiant de la requête
        		// (requestCode) qui sera utilisé plus tard pour identifier l’activité
        		// renvoyant la valeur de retour.
        		Intent i = new Intent(MainActivity.this, SignupActivity.class);    
        		startActivityForResult(i, 3);
        		
        		popupGuest.dismiss();
            }
        });        

        // Ecouteur d'évènement sur le bouton pour fermer l'application
		Button close = (Button) layout.findViewById(R.id.btnPopupClose);
		close.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				popupGuest.dismiss();
			}
		});
    }
}
