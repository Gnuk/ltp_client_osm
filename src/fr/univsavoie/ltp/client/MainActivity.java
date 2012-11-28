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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import fr.univsavoie.ltp.client.map.Friends;
import fr.univsavoie.ltp.client.map.FriendsAdapter;
import fr.univsavoie.ltp.client.map.MyItemizedOverlay;
import fr.univsavoie.ltp.client.map.POIInfoWindow;
import fr.univsavoie.ltp.client.map.ViaPointInfoWindow;
import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity l'activité principale de l'application Android
 */
public class MainActivity extends SherlockActivity implements MapEventsReceiver, LocationListener
{
	/* --------------------------------
	 * Variables globales de l'activité
	 * --------------------------------
	 */
	
	/* Variables de OSMDroid */
	private SimpleLocationOverlay mMyLocationOverlay;
	private MapView map;
	private MapController myMapController;
	private LocationManager locationManager;
	private Location lastLocation;
	private ArrayList<OverlayItem> overlayItemArray;
	
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> itineraryMarkers; 
		//for departure, destination and viapoints
	protected ExtendedOverlayItem markerStart, markerDestination;
	SimpleLocationOverlay myLocationOverlay;

	protected Road mRoad;
	protected PathOverlay roadOverlay;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;
	protected static final int ROUTE_REQUEST = 3;
	
	ArrayList<POI> mPOIs;
	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 4;
	
	/* Variables du service HTTP / Apache */
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private ResponseHandler<String> responseHandler;
	
	/* Variables de gestion */
	private boolean displayAuthBox, displayUserInfos;
	private String login;
	
	private ListView lvListeFriends;
	private List<Friends> listFriends = new ArrayList<Friends>();
	
	/* Variables constantes */
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	private ScrollView viewMapFilters;
	
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
    	//setTheme(R.style.Theme_ltp);
    	
    	// Création de l'activité principale
        setContentView(R.layout.activity_main);
        
        Log.e("Watch", "Activity Start");
        
        // Afficher la ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        
        viewMapFilters = (ScrollView) this.findViewById(R.id.scrollViewMapFilters);
        viewMapFilters.setVisibility(View.GONE);
               
		// MapView settings
		map = (MapView)findViewById(R.id.openmapview);
		map.setTileSource(TileSourceFactory.MAPNIK);
		
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		
		// MapController settings
		myMapController = map.getController();
		
		mMyLocationOverlay = new SimpleLocationOverlay(this);                          
        map.getOverlays().add(mMyLocationOverlay);
        
		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		
		// Geolocalisation de l'utilisateur
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} 
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		{
			lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} 
		
		if (savedInstanceState == null)
		{
			// On définit le point de départ en fonction des coordonnées GPS
			if (lastLocation != null) 
			{
				startPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
			} 
			else 
			{
				startPoint = new GeoPoint(46.227638, 2.213749000000);
			}
			
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
			myMapController.setZoom(7);
			myMapController.setCenter(startPoint);
		} 
		else 
		{
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
			myMapController.setZoom(savedInstanceState.getInt("zoom_level"));
			myMapController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));
		}
		
		myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
		map.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);
		
		// Create map Overlay
		//overlayItemArray = new ArrayList<OverlayItem>();
		//DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		//MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, defaultResourceProxyImpl);
		//map.getOverlays().add(myItemizedIconOverlay);
        
		// Itinerary markers:
		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
		itineraryMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, waypointsItems, 
				map, new ViaPointInfoWindow(R.layout.itinerary_bubble, map));
		map.getOverlays().add(itineraryMarkers);
		updateUIWithItineraryMarkers();
		
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchLocationButton();
			}
		});
		
		registerForContextMenu(searchButton);
		//context menu for clicking on the map is registered on this button. 
		//(a little bit strange, but if we register it on mapView, it will catch map drag events)
        
		//Route and Directions
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
    	roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		map.getOverlays().add(roadNodeMarkers);
		
		if (savedInstanceState != null)
		{
			mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoad(mRoad);
		}
		
		//POIs:
        //POI search interface:
        String[] poiTags = getResources().getStringArray(R.array.poi_tags);
        poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, poiTags);
        poiTagText.setAdapter(adapter);
        Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
        setPOITagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Hide the soft keyboard:
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				//Start search:
				getPOIAsync(poiTagText.getText().toString());
			}
		});
        
        //POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				poiItems, map, new POIInfoWindow(map));
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState != null){
			mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs);
		}
		
		// Load friends ListView
		lvListeFriends = (ListView)findViewById(R.id.listViewFriends);
		//lvListeFriends.setBackgroundResource(R.drawable.listview_roundcorner_item);
		lvListeFriends.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
			{
				Friends item = (Friends) adapter.getItemAtPosition(position);
				
				/*AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
				adb.setTitle("ListView OnClick");
				//adb.setMessage("Selected Item is = " + lvListeFriends.getItemAtPosition(position));
				adb.setMessage("Selected Item is = " + item.getLatitude() + " " + item.getLongitude());
				adb.setPositiveButton("Ok", null);
				adb.show();*/
				
				
				//startPoint = new GeoPoint(item.getLatitude(), item.getLongitude());
				//markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
				//R.string.departure, R.drawable.marker_departure, -1);
				//getRoadAsync();
				
				destinationPoint = new GeoPoint(item.getLongitude(), item.getLatitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
			    		R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
			}
		});
		
		
        
        // Initialiser tout ce qui est données utilisateur propres a l'activité
        init();
    }
    
	@Override
	protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("start", startPoint);
		outState.putParcelable("destination", destinationPoint);
		outState.putParcelableArrayList("viapoints", viaPoints);
		outState.putParcelable("road", mRoad);
		outState.putInt("zoom_level", map.getZoomLevel());
		GeoPoint c = (GeoPoint) map.getMapCenter();
		outState.putParcelable("map_center", c);
		outState.putParcelableArrayList("poi", mPOIs);
	}
    
	// Création d'un menu contenant un bouton rafraichir et un menu déroulant
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add(0, 1, 0, R.string.back_to_my_pos).setIcon(R.drawable.ic_10_device_access_location_found)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, 2, 1, R.string.user_app_settings).setIcon(R.drawable.ic_2_action_settings)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(0, 3, 2, R.string.refresh_map).setIcon(R.drawable.ic_1_navigation_refresh)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(0, 4, 3, R.string.search_on_map).setIcon(R.drawable.ic_2_action_search)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		// Afficher le menu selon si utilisateur connecté ou pas
		
		if (login != null) 
		{
			menu.add(0, 0, 0, R.string.publish_status).setIcon(R.drawable.ic_6_social_chat)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			
			SubMenu sub = menu.addSubMenu(0, 10, 3, R.string.my_account);
			sub.add(0, 11, 4, R.string.disply_user_infos);
			sub.add(0, 12, 5, R.string.logout);
			sub.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} 
		else 
		{
			SubMenu sub = menu.addSubMenu(0, 20, 6, R.string.my_account);
			sub.add(0, 21, 6, R.string.signup);
			sub.add(0, 22, 7, R.string.signin);
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
		// Publier son status
		case 0:
			popupPublishStatus();
			break;
			
		// Quitter l'application
		case 99:
			finish();
			break;			

		// Se refixer au dernier point localisé de l'utilisateur
		case 1:
			relocateUser();
			break;

		// Afficher les préférences
		case 2:
			Intent i = new Intent(MainActivity.this, UserPreferencesActivity.class);
			startActivityForResult(i, 2);
			break;
			
		// Rechercher sur la carte
		case 4:
			viewMapFilters.setVisibility(View.VISIBLE);
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
				Toast.makeText(this, R.string.auth_ok, Toast.LENGTH_LONG).show();
				//setup();
				//auth();
				//parseFriends();
				finish();
				startActivity(getIntent());
				return;
			case RESULT_CANCELED:
				Toast.makeText(this, R.string.auth_fail, Toast.LENGTH_LONG).show();
				return;
				
			case ROUTE_REQUEST : 
				if (resultCode == RESULT_OK) {
					int nodeId = data.getIntExtra("NODE_ID", 0);
					map.getController().setCenter(mRoad.mNodes.get(nodeId).mLocation);
					roadNodeMarkers.showBubbleOnItem(nodeId, map);
				}
				break;
			case POIS_REQUEST:
				if (resultCode == RESULT_OK) {
					int id = data.getIntExtra("ID", 0);
					map.getController().setCenter(mPOIs.get(id).mLocation);
					poiMarkers.showBubbleOnItem(id, map);
				}
				break;				
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
				Toast.makeText(this, R.string.prefs_update_error, Toast.LENGTH_LONG).show();
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
				Toast.makeText(this, R.string.signup_ok, Toast.LENGTH_LONG).show();
				return;
			case RESULT_CANCELED:
				Toast.makeText(this, R.string.signup_fail, Toast.LENGTH_LONG).show();
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
     * Test MyItemizedOverlay object
     */
    public void putMyItemizedOverlay(GeoPoint p)
    {
		ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
		MyItemizedOverlay myOverlays = new MyItemizedOverlay(this, list);
		OverlayItem overlayItem = new OverlayItem("Home Sweet Home", "This is the place I live", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		Drawable marker = getResources().getDrawable(R.drawable.marker_departure);
		overlayItem.setMarker(marker);
		myOverlays.addItem(overlayItem);
		map.getOverlays().add(myOverlays);    	
		map.invalidate();
    }
    
    /** 
     * Reverse Geocoding
     */
    public String getAddress(GeoPoint p)
    {
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		String theAddress;
		try {
			double dLatitude = p.getLatitudeE6() * 1E-6;
			double dLongitude = p.getLongitudeE6() * 1E-6;
			List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
			StringBuilder sb = new StringBuilder(); 
			if (addresses.size() > 0) { 
				Address address = addresses.get(0); 
				int n = address.getMaxAddressLineIndex();
				for (int i=0; i<=n; i++) {
					if (i!=0) 
						sb.append(", ");
					sb.append(address.getAddressLine(i));
				}
				theAddress = new String(sb.toString());
			} else {
				theAddress = null;
			}
		} catch (IOException e) {
			theAddress = null;
		}
		if (theAddress != null) {
			return theAddress;
		} else {
			return "";
		}
    }
    
    /**
     * Geocoding of the destination address
     */
	public void handleSearchLocationButton()
	{
		EditText destinationEdit = (EditText)findViewById(R.id.editDestination);
		//Hide the soft keyboard:
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(destinationEdit.getWindowToken(), 0);
		
		String destinationAddress = destinationEdit.getText().toString();
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		try {
			List<Address> foundAdresses = geocoder.getFromLocationName(destinationAddress, 1);
			if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
			    		R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
			}
		} catch (Exception e) {
			Toast.makeText(this, "Geocoding error", Toast.LENGTH_SHORT).show();
		}
	}
	
	//Async task to reverse-geocode the marker position in a separate thread:
	private class GeocodingTask extends AsyncTask<Object, Void, String> 
	{
		ExtendedOverlayItem marker;
		protected String doInBackground(Object... params) {
			marker = (ExtendedOverlayItem)params[0];
			return getAddress(marker.getPoint());
		}
		protected void onPostExecute(String result) {
			marker.setDescription(result);
			//itineraryMarkers.showBubbleOnItem(???, map); //open bubble on the item
		}
	}
	
	/** add (or replace) an item in markerOverlays. p position. */
    public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, int index,
    		int titleResId, int markerResId, int iconResId) 
    {
		if (item != null){
			itineraryMarkers.removeItem(item);
		}
		Drawable marker = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		overlayItem.setMarker(marker);
		if (iconResId != -1)
			overlayItem.setImage(getResources().getDrawable(iconResId));
		overlayItem.setRelatedObject(index);
		itineraryMarkers.addItem(overlayItem);
		map.invalidate();
		//Start geocoding task to update the description of the marker with its address:
		new GeocodingTask().execute(overlayItem);
		return overlayItem;
	}
    
	public void addViaPoint(GeoPoint p)
	{
		viaPoints.add(p);
		putMarkerItem(null, p, viaPoints.size()-1,
			R.string.via_point, R.drawable.marker_via, -1);
	}
	
	public void removePoint(int index)
	{
		if (index == START_INDEX)
			startPoint = null;
		else if (index == DEST_INDEX)
			destinationPoint = null;
		else 
			viaPoints.remove(index);
		getRoadAsync();
		updateUIWithItineraryMarkers();
	}
	
	public void updateUIWithItineraryMarkers()
	{
		itineraryMarkers.removeAllItems();
		
		//Start marker:
		if (startPoint != null){
			markerStart = putMarkerItem(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1);
		}
		
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			putMarkerItem(null, viaPoints.get(index), index, 
				R.string.via_point, R.drawable.marker_via, -1);
		}
		
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = putMarkerItem(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
		}
	}
	
    //------------ Route and Directions
    
    private void putRoadNodes(Road road)
    {
		roadNodeMarkers.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(
    				"Step " + (i+1), instructions, 
    				node.mLocation, this);
    		nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
    		nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
    		nodeMarker.setMarker(marker);
    		int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
    		if (iconId != R.drawable.ic_empty){
	    		Drawable icon = getResources().getDrawable(iconId);
	    		nodeMarker.setImage(icon);
    		}
    		roadNodeMarkers.addItem(nodeMarker);
    	}
    }
    
	void updateUIWithRoad(Road road)
	{
		roadNodeMarkers.removeAllItems();
		List<Overlay> mapOverlays = map.getOverlays();
		if (roadOverlay != null){
			mapOverlays.remove(roadOverlay);
		}
		if (road == null)
			return;
		if (road.mStatus == Road.STATUS_DEFAULT)
			Toast.makeText(map.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
			//we set the road overlay at the "bottom", just above the MapEventsOverlay,
			//to avoid covering the other overlays. 
		mapOverlays.add(removedOverlay);
		putRoadNodes(road);
		map.invalidate();
		//Set route info in the text view:
		//((TextView)findViewById(R.id.routeInfo)).setText(road.getLengthDurationText(-1));
		infoBar(road.getLengthDurationText(-1), true);
    }
	
	/**
	 * Async task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> 
	{
		protected Road doInBackground(Object... params) 
		{
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			//RoadManager roadManager = new GoogleRoadManager();
			RoadManager roadManager = new OSRMRoadManager();
			/*
			RoadManager roadManager = new MapQuestRoadManager();
			Locale locale = Locale.getDefault();
			roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
			*/
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) 
		{
			mRoad = result;
			updateUIWithRoad(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}
	
	public void getRoadAsync()
	{
		mRoad = null;
		if (startPoint == null || destinationPoint == null){
			updateUIWithRoad(mRoad);
			return;
		}
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(startPoint);
		//add intermediate via points:
		for (GeoPoint p:viaPoints){
			waypoints.add(p); 
		}
		waypoints.add(destinationPoint);
		new UpdateRoadTask().execute(waypoints);
	}
	
	//----------------- POIs
	
	void updateUIWithPOI(ArrayList<POI> pois)
	{
		if (pois != null){
			for (POI poi:pois){
				ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(
					poi.mType, poi.mDescription, 
					poi.mLocation, this);
				Drawable marker = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					marker = getResources().getDrawable(R.drawable.marker_poi_default);
				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
					if (poi.mRank < 90)
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
					else
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
					marker = getResources().getDrawable(R.drawable.marker_poi_flickr);
				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
					marker = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
					poiMarker.setSubDescription(poi.mCategory);
				}
				poiMarker.setMarker(marker);
				poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
				//thumbnail loading moved in POIInfoWindow.onOpen for better performances. 
				poiMarker.setRelatedObject(poi);
				poiMarkers.addItem(poiMarker);
			}
		}
		map.invalidate();
	}
	
	ExecutorService mThreadPool = Executors.newFixedThreadPool(5);
	/** Loads all thumbnails in background */
	void startAsyncThumbnailsLoading(ArrayList<POI> pois){
		/* Try to stop existing threads:
		 * not sure it has any effect... 
		if (mThreadPool != null){
			//Stop threads if any:
			mThreadPool.shutdownNow();
		}
		mThreadPool = Executors.newFixedThreadPool(5);
		*/
		for (int i=0; i<pois.size(); i++){
			final int index = i;
			final POI poi = pois.get(index);
			mThreadPool.submit(new Runnable(){
				@Override public void run(){
					Bitmap b = poi.getThumbnail();
					if (b != null){
						/*
						//Change POI marker:
						ExtendedOverlayItem item = poiMarkers.getItem(index);
						b = Bitmap.createScaledBitmap(b, 48, 48, true);
						BitmapDrawable bd = new BitmapDrawable(getResources(), b);
						item.setMarker(bd);
						*/
					}
				}
			});
		}
	}
	
	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> 
	{
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params) {
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals("")){
				return null;
			} else if (mTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
				//ArrayList<POI> pois = poiProvider.getPOICloseTo(point, 30, 20.0);
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.startsWith("picasa")){
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				BoundingBoxE6 bb = map.getBoundingBox();
				String q = mTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else {
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
				ArrayList<POI> pois;
				if (destinationPoint == null){
					BoundingBoxE6 bb = map.getBoundingBox();
					pois = poiProvider.getPOIInside(bb, mTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mTag, 100, 2.0);
				}
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) 
		{
			mPOIs = pois;
			if (mTag.equals("")){
				//no search, no message
			} else if (mPOIs == null){
				Toast.makeText(getApplicationContext(), "Technical issue when getting "+mTag+ " POI.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), ""+mPOIs.size()+" "+mTag+ " entries found", Toast.LENGTH_LONG).show();
				if (mTag.equals("flickr")||mTag.startsWith("picasa")||mTag.equals("wikipedia"))
					startAsyncThumbnailsLoading(mPOIs);
			}
			updateUIWithPOI(mPOIs);
		}
	}
	
	void getPOIAsync(String tag){
		poiMarkers.removeAllItems();
		new POITask().execute(tag);
	}
	
	//------------ MapEventsReceiver implementation

		GeoPoint tempClickedGeoPoint; //any other way to pass the position to the menu ???
		
		@Override 
		public boolean longPressHelper(IGeoPoint p) 
		{
			tempClickedGeoPoint = new GeoPoint((GeoPoint)p);
			Button searchButton = (Button)findViewById(R.id.buttonSearch);
			openContextMenu(searchButton); //menu is hooked on the "Search" button
			return true;
		}

		@Override 
		public boolean singleTapUpHelper(IGeoPoint p) 
		{
			viewMapFilters.setVisibility(View.GONE);
			return false;
		}

		//----------- Context Menu when clicking on the map
		@Override 
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
		{
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.map_menu, menu);
		}
		
		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) 
		{
			switch (item.getItemId()) 
			{
			case R.id.menu_departure:
				startPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
				markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
					R.string.departure, R.drawable.marker_departure, -1);
				getRoadAsync();
				return true;
			case R.id.menu_destination:
				destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
					R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				return true;
			case R.id.menu_viapoint:
				GeoPoint viaPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
				addViaPoint(viaPoint);
				getRoadAsync();
				return true;
			default:
				return false;
			}
		}/*
		
		//------------ Option Menu implementation
		
		@Override 
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.option_menu, menu);
			return true;
		}
		
		@Override 
		public boolean onPrepareOptionsMenu(Menu menu) {
			if (mRoad != null && mRoad.mNodes.size()>0)
				menu.findItem(R.id.menu_itinerary).setEnabled(true);
			else 
				menu.findItem(R.id.menu_itinerary).setEnabled(false);
			if (mPOIs != null && mPOIs.size()>0)
				menu.findItem(R.id.menu_pois).setEnabled(true);
			else 
				menu.findItem(R.id.menu_pois).setEnabled(false);
			return true;
		}
		
		@Override 
		public boolean onOptionsItemSelected(MenuItem item) {
			Intent myIntent;
			switch (item.getItemId()) {
			case R.id.menu_itinerary:
				myIntent = new Intent(this, RouteActivity.class);
				myIntent.putExtra("ROAD", mRoad);
				myIntent.putExtra("NODE_ID", roadNodeMarkers.getBubbledItemId());
				startActivityForResult(myIntent, ROUTE_REQUEST);
				return true;
			case R.id.menu_pois:
				myIntent = new Intent(this, POIActivity.class);
				myIntent.putParcelableArrayListExtra("POI", mPOIs);
				myIntent.putExtra("ID", poiMarkers.getBubbledItemId());
				startActivityForResult(myIntent, POIS_REQUEST);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}*/
		
		//------------ LocationListener implementation
		@Override public void onLocationChanged(final Location pLoc) {
	            myLocationOverlay.setLocation(new GeoPoint(pLoc));
	    }

		@Override public void onProviderDisabled(String provider) {
		}

		@Override public void onProviderEnabled(String provider) {
		}

		@Override public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	
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
		if (login == null)
			infoBar("Salut, Etranger, connecte toi!", true);
		else
			infoBar("Salut, " + login + "! ", true);
	}
	
    /**
     * Initialiser les composants de l'application
     */
    private void init()
    {
    	setup();
        
        try 
        {
			// Connecter l'utilisateur et parser ses amis
			if (login != null)
			{
				// Appeler la fonction pour s'authentifier auprès du service LTP
				auth();
				
				// Appeler la fonction pour parser les amis et les affichés sur la carte
				parseFriends();
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
			
			listFriends.clear();

			// Parse user frinds list into OverlayItem arraylist
	        for (int i = 0 ; (i < potesArray.length()) ; i++ )
	        {        	
	        	// Get current friend
	        	JSONObject pote = potesArray.getJSONObject(i);
	        	
		        // Prepare array of users icons in map
		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("name"), pote.getString("desc"), new GeoPoint(pote.getDouble("@lat"), pote.getDouble("@lon"))));
		        
		        // Add friend info to friends ListView
		        listFriends.add(new Friends(pote.getString("name"), pote.getString("desc"), pote.getDouble("@lat"), pote.getDouble("@lon")));
	        }
	        
	        // Load & setfriends & notify ListView adapter
			FriendsAdapter adapter = new FriendsAdapter(this, listFriends);       
			lvListeFriends.setAdapter(adapter);
	        adapter.notifyDataSetChanged();
	        
            // Create a new ItemizedOverlayWithFocus with our user item array
            ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
            map.getOverlays().add(anotherItemizedIconOverlay);
            map.refreshDrawableState();
            map.postInvalidate();
            
            // Setup icon overlay
            anotherItemizedIconOverlay.setFocusItemsOnTap(true);
            anotherItemizedIconOverlay.setFocusedItem(0);  
            
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					map.postInvalidate();
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
			return false;
		}
    };
    
    private void relocateUser()
    {
    	myMapController.setCenter(startPoint);   	
    	map.invalidate();
    }
	
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
			
			map.invalidate();
			
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
			    
				//Geocoder gcd = new Geocoder(this, Locale.getDefault());
				//List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
				//if (addresses.size() > 0) 
				    //System.out.println(addresses.get(0).getLocality());
			    
			    infoBar("T'es localisé: " + getAddress(startPoint), true);
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
		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popupAccount);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_account,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupUserInfos = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
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
     * Afficher sur la map un popup qui propose a l'utilisateur
     * de mettre a jours son status
     */
    private void popupPublishStatus()
    {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = width / 2;
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		ScrollView viewGroup = (ScrollView) this.findViewById(R.id.popupStatus);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_set_status,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupPublishStatus = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupPublishStatus.setBackgroundDrawable(new BitmapDrawable());
		popupPublishStatus.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		findViewById(R.id.layoutMain).post(new Runnable() 
		{
			@Override
			public void run() 
			{
				popupPublishStatus.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */

        // Ecouteur d'évènement sur le bouton pour se déconnecter
		Button publish = (Button) layout.findViewById(R.id.btStatusPublish);
		publish.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				EditText userStatus = (EditText) layout.findViewById(R.id.fieldUserStatus);				
				if (userStatus.getText().toString().length() == 0 ) 
				{
					Toast.makeText(MainActivity.this, "Impossible de publié ton status !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
				} 
				else if (userStatus.getText().toString().length() < 3 ) 
				{
					Toast.makeText(MainActivity.this, "Impossible de publié ton status !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
				}
				else
				{
					Toast.makeText(MainActivity.this, "Status mise a jours !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
				}
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
		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popupGuest);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_guest,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupGuest = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
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