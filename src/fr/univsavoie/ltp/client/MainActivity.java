package fr.univsavoie.ltp.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
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

import fr.univsavoie.ltp.client.map.GPSTracker;
import fr.univsavoie.ltp.client.map.POIInfoWindow;
import fr.univsavoie.ltp.client.map.Session;
import fr.univsavoie.ltp.client.map.Friends;
import fr.univsavoie.ltp.client.map.FriendsAdapter;
import fr.univsavoie.ltp.client.map.Popup;
import fr.univsavoie.ltp.client.map.Tools;
import fr.univsavoie.ltp.client.map.ViaPointInfoWindow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * MainActivity l'activité principale de l'application Android
 */
public class MainActivity extends SherlockActivity implements MapEventsReceiver
{
	/* --------------------------------
	 * Variables globales de l'activité
	 * --------------------------------
	 */
	
	/* Variables pour la bibliothèque OSMdroid */
	private SimpleLocationOverlay locationOverlay;
	private MapView map;
	private MapController mapController;
	private ArrayList<OverlayItem> overlayItemArray;
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> itineraryMarkers;
	protected ExtendedOverlayItem markerStart, markerDestination;
	private SimpleLocationOverlay myLocationOverlay;
	protected Road mRoad;
	protected PathOverlay roadOverlay;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;
	protected static final int ROUTE_REQUEST = 3;
	private ArrayList<POI> mPOIs;
	private ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
	private AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 4;
	private ArrayList<OverlayItem> anotherOverlayItemArray;
	private GPSTracker gps;
	/* Variables de traitements */
	private boolean displayUserInfos;
	private String login;
	private ListView lvListeFriends;
	private List<Friends> listFriends = new ArrayList<Friends>();
	private ScrollView viewMapFilters;
	
	/* Variables de classes */
	private Popup popup;
	private Session session;
	private Tools tools;
	
	
    /* --------------------------------------------------------
     * Evenements de l'activity (onCreate, onResume, onStop...)
     * --------------------------------------------------------
     */
	
    @Override
    public void onCreate(Bundle savedInstanceState)  
    {
    	super.onCreate(savedInstanceState);
    	
    	// Appliquer le thème LTP a l'ActionBar
    	//setTheme(R.style.Theme_ltp);
    	
    	// Création de l'activité principale
        setContentView(R.layout.activity_main);
        
		// Instancier les classes utiles
		popup = new Popup(this);
		session = new Session(this);
		tools = new Tools(this);
        
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
		map.setBuiltInZoomControls(false);
		map.setMultiTouchControls(true);
		
		// MapController settings
		mapController = map.getController();
		
		locationOverlay = new SimpleLocationOverlay(this);                          
        map.getOverlays().add(locationOverlay);
        
		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		// Gestion du trackeur GPS
		gps = new GPSTracker(MainActivity.this);

		// Verifier si le GPS est activé, si c'est le cas, définir la position actuel
		if(gps.canGetLocation())
		{
		    double latitude = gps.getLatitude();
		    double longitude = gps.getLongitude();
		    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
		
		    startPoint = new GeoPoint(latitude, longitude);
		}
		else
		{
		    // Impossible d'obtenir la localité: GPS et RESEAU non activité...
		    gps.showSettingsAlert(); // Demander a l'utilisateur d'activité le GPS
		    
			startPoint = new GeoPoint(46.227638, 2.213749000000);
		}
		
		if (savedInstanceState == null)
		{
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
			mapController.setZoom(14);
			mapController.setCenter(startPoint);
		} 
		else 
		{
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
			mapController.setZoom(savedInstanceState.getInt("zoom_level"));
			mapController.setCenter((GeoPoint)savedInstanceState.getParcelable("map_center"));
		}
		
		myLocationOverlay = new SimpleLocationOverlay(this, new DefaultResourceProxyImpl(this));
		map.getOverlays().add(myLocationOverlay);
		myLocationOverlay.setLocation(startPoint);
		
		// Create map Overlay
		//overlayItemArray = new ArrayList<OverlayItem>();
		//DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
		//MyItemizedIconOverlay myItemizedIconOverlay = new MyItemizedIconOverlay(overlayItemArray, null, defaultResourceProxyImpl);
		//map.getOverlays().add(myItemizedIconOverlay);
		
		// Boutton pour zoomer la carte
		ImageButton btZoomIn = (ImageButton)findViewById(R.id.btZoomIn);
		btZoomIn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				map.getController().zoomIn();
			}
		});
		
		// Boutton pour dezoomer la carte
		ImageButton btZoomOut = (ImageButton)findViewById(R.id.btZoomOut);
		btZoomOut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				map.getController().zoomOut();
			}
		});
        
		// Pointeurs d'itinérairea:
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
		
		//context menu for clicking on the map is registered on this button. 
		registerForContextMenu(searchButton);
        
		// Routes et Itinéraires
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
        setPOITagButton.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
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
				
				destinationPoint = new GeoPoint(item.getLongitude(), item.getLatitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
			    		R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
			}
		});
		
        // Initialiser tout ce qui est données utilisateur propres à l'activité
        init();
    }
    
	protected void onSaveInstanceState (Bundle outState)
	{
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

	// Méthode callback appelée lorqu'un item du menu est sélcetionné
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Le bouton "retour" à le même titre que la page.
		if (item.getTitle().toString().compareTo(getTitle().toString()) == 0)
		{
			finish();
		}
		
		Log.d("Watch", "LOGIN: " + login);

		Log.d("Watch", String.valueOf(item.getItemId()));

		switch (item.getItemId()) 
		{
		// Publier son status
		case 0:
			popup.popupPublishStatus();
			break;
			
		// Quitter l'application
		case 99:
			finish();
			break;			

		// Se refixer au dernier point localisé de l'utilisateur
		case 1:
			tools.relocateUser(mapController, map, gps);
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
			popup.popupDisplayUserInfos();
			break;
			
		// Déconnecter l'utilisateur actif
		case 12:
			session.logout();
			break;

		default:
			break;
		}
		return true;
	}
	
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
	
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onResume() 
    {
		super.onResume();
	}
    
	@Override
	protected void onPause() 
	{
		// On arrête le GPS
		gps.stopUsingGPS();
		super.onPause();
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
	
	/* ----------------------------------------
	 * Fonctions et procédures de l'application
	 * ----------------------------------------
	 */
    
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
		protected String doInBackground(Object... params)
		{
			marker = (ExtendedOverlayItem)params[0];
			return tools.getAddress(marker.getPoint());
		}
		protected void onPostExecute(String result)
		{
			marker.setDescription(result);
			//itineraryMarkers.showBubbleOnItem(???, map); //open bubble on the item
		}
	}
	
	/** add (or replace) an item in markerOverlays. p position. */
	public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, int index, int titleResId, int markerResId, int iconResId)
	{
		if (item != null)
		{
			itineraryMarkers.removeItem(item);
		}
		Drawable marker = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		overlayItem.setMarker(marker);
		if (iconResId != -1)
		{
			overlayItem.setImage(getResources().getDrawable(iconResId));
		}
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
	
	//------------ Routes et Itinéraires
    
	private void putRoadNodes(Road road)
	{
		roadNodeMarkers.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
		for (int i = 0; i < n; i++)
		{
			RoadNode node = road.mNodes.get(i);
			String instructions = (node.mInstructions == null ? "" : node.mInstructions);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step " + (i + 1), instructions, node.mLocation, this);
			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
			if (iconId != R.drawable.ic_empty)
			{
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
	 * Tâche asynchrone afin d'obtenir trajet avec des processus séparés
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
	
	public boolean longPressHelper(IGeoPoint p) 
	{
		tempClickedGeoPoint = new GeoPoint((GeoPoint)p);
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		openContextMenu(searchButton); //menu is hooked on the "Search" button
		return true;
	}
		
	public boolean singleTapUpHelper(IGeoPoint p) 
	{
		viewMapFilters.setVisibility(View.GONE);
		return false;
	}

	//----------- Menu contextuel lors d'un appuis prolongé sur la carte
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
	}
		
	public boolean onContextItemSelected(android.view.MenuItem item) 
	{
		if(item.getItemId() == R.id.menu_departure)
		{
			startPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1);
			getRoadAsync();
			return true;
		}
		else if(item.getItemId() == R.id.menu_destination)
		{
			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
			getRoadAsync();
			return true;
		}
		else if(item.getItemId() == R.id.menu_viapoint)
		{
			GeoPoint viaPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			addViaPoint(viaPoint);
			getRoadAsync();
			return true;
		}
		else
		{
			return false;
		}
	}
		
	/*//------------ Option Menu implementation
	
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
	
	/**
	 * Procédure qui s'occupe de mettre à jours certaines variables
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
		{
			popup.popupGuest();
		}
		else
		{
			if (displayUserInfos)
			{
				popup.popupDisplayUserInfos();
			}
		}
		
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
				session.auth();
				
				// Appeler la fonction pour parser les amis et les affichés sur la carte
				displayFriends();
			}
			
			//Add Scale Bar
			//ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
			//myOpenMapView.getOverlays().add(myScaleBarOverlay);
		} catch (Exception e)
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
     * Fonction pour afficher sur la carte les amis en forme
     * de marqueurs avec a l'interieur son status, ...
     */
	public void displayFriends()
    {
        anotherOverlayItemArray = new ArrayList<OverlayItem>();
        
        try 
        {
        	// Récupérer la liste des amis via appel serveur REST
			JSONArray potesArray = session.parseFriends();
			
			// Vider la liste actuel
			listFriends.clear();

			// Parser la liste des amis dans le OverlayItem ArrayList
	        for (int i = 0 ; (i < potesArray.length()) ; i++ )
	        {
	        	// Obtenir l'amis
	        	JSONObject pote = potesArray.getJSONObject(i);
	        	
		        // Préparé l'array des icônes amis sur la carte
		        anotherOverlayItemArray.add(new OverlayItem("Un pote", pote.getString("content"), new GeoPoint(pote.getDouble("lon"), pote.getDouble("lat"))));
		        
		        // Ajouter l'ami dans la friends ListView
		        listFriends.add(new Friends("Un pote", pote.getString("content"), pote.getDouble("lon"), pote.getDouble("lat")));
	        }
	        
	        // Load & setfriends & notify ListView adapter
			FriendsAdapter adapter = new FriendsAdapter(this, listFriends);       
			lvListeFriends.setAdapter(adapter);
	        adapter.notifyDataSetChanged();
	        
            // Créer un nouveau ItemizedOverlayWithFocus avec notre array d'amis
	        // Ensuite, on redessine la carte pour actualiser les marqueurs
            ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
            map.getOverlays().add(anotherItemizedIconOverlay);
            map.refreshDrawableState();
            map.postInvalidate();
			/*runOnUiThread(new Runnable()
			{
				public void run() {
					map.postInvalidate();
				}
			});*/
			
            // Paramètres pour l'overlay des icônes
            anotherItemizedIconOverlay.setFocusItemsOnTap(true);
            anotherItemizedIconOverlay.setFocusedItem(0);  
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
    
    OnItemGestureListener<OverlayItem> myOnItemGestureListener = new OnItemGestureListener<OverlayItem>() 
	{
        public boolean onItemSingleTapUp(int index, OverlayItem item)
        {
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
	
	/**
	 * Mise a jours des coordonnées géographique de l'utilisateur
	 * @param loc
	 */
	private void updateLoc(Location loc)
	{
        try
        {
			// Met à jour notre localisation sur la carte
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
	 * On met à jour les informations de l'utilisateur de sa situation géographique dans
	 * les divers affichages de l'application.
	 * @param location Coordonnées GPS latitude et longitude.
	 */
	private void updateUserGPSInfos(Location location)
	{
    	try
    	{
			// Update user localization coordinates
			String latLongString = "";
			if (location != null) 
			{
			    double lat = location.getLatitude();
			    double lng = location.getLongitude();
			    latLongString = "Lat:" + lat + ", Long:" + lng;
			    
			    infoBar("T'es localisé: " + this.tools.getAddress(startPoint), true);
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
	
	/**
	 * Permet de changer les coordonnées de l'utilisateur
	 * @param location la localisation à fixer
	 */
	private void setOverlayLoc(Location location)
	{
		overlayItemArray.clear();
		GeoPoint overlocGeoPoint = new GeoPoint(location);
    	OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", overlocGeoPoint);
    	overlayItemArray.add(newMyLocationItem);
	}
}