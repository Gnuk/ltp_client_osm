package fr.univsavoie.ltp.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.location.*;
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
import org.osmdroid.views.*;
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

import fr.univsavoie.ltp.client.map.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
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
	
	/* Variables pour la bibliothèque OSMdroid */
	private SimpleLocationOverlay locationOverlay;
	private ArrayList<OverlayItem> overlayItemArray;
	private MapView map;
	private MapController mapController;
	private LocationManager locationManager;
	private Location lastLocation;
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
	
	/* Variables de traitements */
	private boolean displayUserInfos;
	private String login;
	private ListView lvListeFriends;
	private List<Friends> listFriends = new ArrayList<Friends>();
	private ScrollView viewMapFilters;
	
	/* Variables de classes */
	private Popup popup;
	private Session session;
	
	/*
	 * --------------------------------------------------------
	 * Evenements de l'activity (onCreate, onResume, onStop...)
	 * --------------------------------------------------------
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Appliquer le thème LTP a l'ActionBar
		// setTheme(R.style.Theme_ltp);

		// Création de l'activité principale
		setContentView(R.layout.activity_main);

		Log.e("Watch", "Activity Start");

		// Afficher la ActionBar
		ActionBar mActionBar = getSupportActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);

		viewMapFilters = (ScrollView) this
				.findViewById(R.id.scrollViewMapFilters);
		viewMapFilters.setVisibility(View.GONE);

		// MapView settings
		map = (MapView) findViewById(R.id.openmapview);
		map.setTileSource(TileSourceFactory.MAPNIK);

		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		
		// MapController settings
		mapController = map.getController();
		
		locationOverlay = new SimpleLocationOverlay(this);
		map.getOverlays().add(locationOverlay);
        
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
				lastLocation = new Location("");
				lastLocation.setLatitude(46.227638);
				lastLocation.setLongitude(2.213749000000);
				startPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
			}
			
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
			mapController.setZoom(7);
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
        
		// Pointeurs d'itinéraires:
		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
		itineraryMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, waypointsItems, map, new ViaPoint(R.layout.itinerary_bubble, map));
		map.getOverlays().add(itineraryMarkers);
		updateUIWithItineraryMarkers();
		
		Button searchButton = (Button)findViewById(R.id.buttonSearch);
		searchButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
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
		
        //POI interface pour la recherche
        String[] poiTags = getResources().getStringArray(R.array.poi_tags);
        poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, poiTags);
        poiTagText.setAdapter(adapter);
        Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
        setPOITagButton.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
				// Masquer le clavier
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				// Lancer la recherche
				getPOIAsync(poiTagText.getText().toString());
			}
		});
        
        //POI marqueurs:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, poiItems, map, new POIInfoWindow(map));
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState != null)
		{
			mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs);
		}
		
		// charger la  liste des amis
		lvListeFriends = (ListView)findViewById(R.id.listViewFriends);
		//lvListeFriends.setBackgroundResource(R.drawable.listview_roundcorner_item);
		lvListeFriends.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) 
			{
				Friends item = (Friends) adapter.getItemAtPosition(position);
				
				destinationPoint = new GeoPoint(item.getLongitude(), item.getLatitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX, R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
			}
		});
		
		// Instancier les classes utiles
		popup = new Popup(this);
		session = new Session(this);
        
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
		menu.add(0, 1, 0, R.string.back_to_my_pos).setIcon(R.drawable.ic_10_device_access_location_found).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(0, 2, 1, R.string.user_app_settings).setIcon(R.drawable.ic_2_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(0, 3, 2, R.string.refresh_map).setIcon(R.drawable.ic_1_navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		menu.add(0, 4, 3, R.string.search_on_map).setIcon(R.drawable.ic_2_action_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
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

	// Méthode appelée lorqu'un item du menu est sélectionné
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
	}
	
	protected void onResume() 
    {
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, myLocationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, myLocationListener);
		 
		super.onResume();
	}
    
	protected void onPause() 
	{
		super.onPause();
	}
	
	protected void onStop() 
	{
		super.onStop();
	}
    
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
			if (foundAdresses.size() == 0)
			{ //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else
			{
				Address address = foundAdresses.get(0); //get first address
				destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
				markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
			    		R.string.destination, R.drawable.marker_destination, -1);
				getRoadAsync();
				map.getController().setCenter(destinationPoint);
			}
		} catch (Exception e)
		{
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
			return Adresse.getAddress(marker.getPoint(), MainActivity.this);
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
		if (startPoint != null)
		{
			markerStart = putMarkerItem(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1);
		}
		
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++)
		{
			putMarkerItem(null, viaPoints.get(index), index, 
				R.string.via_point, R.drawable.marker_via, -1);
		}
		
		//Destination marker if any:
		if (destinationPoint != null)
		{
			markerDestination = putMarkerItem(null, destinationPoint, DEST_INDEX, R.string.destination, R.drawable.marker_destination, -1);
		}
	}
	
	/**
	 * Tâche asynchrone afin d'obtenir le trajet avec des processus séparés
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
	
	public void updateUIWithPOI(ArrayList<POI> pois)
	{
		if (pois != null)
		{
			for (POI poi:pois)
			{
				ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(poi.mType, poi.mDescription, poi.mLocation, this);
				Drawable marker = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM)
				{
					marker = getResources().getDrawable(R.drawable.marker_poi_default);
				}
				else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA)
				{
					if (poi.mRank < 90)
					{
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
					}
					else
					{
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
					}
				}
				else if (poi.mServiceId == POI.POI_SERVICE_FLICKR)
				{
					marker = getResources().getDrawable(R.drawable.marker_poi_flickr);
				}
				else if (poi.mServiceId == POI.POI_SERVICE_PICASA)
				{
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
	
	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> 
	{
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params)
		{
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals(""))
			{
				return null;
			} else if (mTag.equals("wikipedia"))
			{
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
				//ArrayList<POI> pois = poiProvider.getPOICloseTo(point, 30, 20.0);
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.equals("flickr"))
			{
				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.startsWith("picasa"))
			{
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				BoundingBoxE6 bb = map.getBoundingBox();
				String q = mTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else
			{
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
				ArrayList<POI> pois;
				if (destinationPoint == null)
				{
					BoundingBoxE6 bb = map.getBoundingBox();
					pois = poiProvider.getPOIInside(bb, mTag, 100);
				} else
				{
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mTag, 100, 2.0);
				}
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) 
		{
			mPOIs = pois;
			if (mTag.equals(""))
			{
				//no search, no message
			} else if (mPOIs == null)
			{
				Toast.makeText(getApplicationContext(), "Technical issue when getting "+mTag+ " POI.", Toast.LENGTH_LONG).show();
			} else
			{
				Toast.makeText(getApplicationContext(), ""+mPOIs.size()+" "+mTag+ " entries found", Toast.LENGTH_LONG).show();
			}
			updateUIWithPOI(mPOIs);
		}
	}
	
	public void getPOIAsync(String tag)
	{
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
		else if (item.getItemId() == R.id.menu_destination)
		{
			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
			getRoadAsync();
			return true;
		}
		else if (item.getItemId() == R.id.menu_viapoint)
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
	
	//------------ LocationListener implementation
	public void onLocationChanged(final Location location)
	{
		//myLocationOverlay.setLocation(new GeoPoint(location));
		
		GeoPoint p = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
		myLocationOverlay.setLocation(p);
	}
	
	/**
	 * Permet de changer les coordonnées de l'utilisateur
	 * @param location la nouvelle localisation
	 */
	private void setOverlayLoc(Location location)
	{
		overlayItemArray.clear();
		GeoPoint overlocGeoPoint = new GeoPoint(location);
		OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", overlocGeoPoint);
		overlayItemArray.add(newMyLocationItem);
	}
	
	/**
	 * Mise a jours des coordonnées géographique de l'utilisateur
	 * @param location la nouvelle localisation
	 */
	private void updateLoc(Location location)
	{
		try
		{
			// Met à jour notre localisation sur la carte
			setOverlayLoc(location);
			map.invalidate();
			updateUserGPSInfos(location);
		} catch (Exception e)
		{
			Log.e("Catch", "> updateLoc() - Exception : " + e.getMessage());
		}
	}
	
	/**
	 * On met à jour les informations de l'utilisateur sur sa situation géographique dans
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

				InfoBar.setText(this, "T'es localisé: " + Adresse.getAddress(startPoint, this), true);
			} else
			{
				InfoBar.setText(this, "Ta position actuel n'a pas été trouvé !" + latLongString, true);
			}
		} catch (Exception e)
		{
			Log.e("Catch", "> updateUserInterface() - Exception : " + e.getMessage());
		}
	}
	
    private LocationListener myLocationListener = new LocationListener()
    {
		public void onLocationChanged(Location location)
		{
			updateLoc(location);
		}
		
		public void onProviderDisabled(String provider){
			
		}
		
		public void onProviderEnabled(String provider){
			
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras){
			
		}
    };


	public void onProviderDisabled(String provider) {}

	public void onProviderEnabled(String provider) 
	{
		String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		String newStatus = "";
		switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				newStatus = "OUT_OF_SERVICE";
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				newStatus = "TEMPORARILY_UNAVAILABLE";
				break;
			case LocationProvider.AVAILABLE:
				newStatus = "AVAILABLE";
				break;
		}
		String msg = String.format(getResources().getString(R.string.provider_disabled), provider, newStatus);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
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
			InfoBar.setText(this, "Salut, Etranger, connecte toi!", true);
		else
			InfoBar.setText(this, "Salut, " + login + "! ", true);
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
		        anotherOverlayItemArray.add(new OverlayItem(pote.getString("name"), pote.getString("desc"), new GeoPoint(pote.getDouble("@lat"), pote.getDouble("@lon"))));
		        
		        // Ajouter l'ami dans la friends ListView
		        listFriends.add(new Friends(pote.getString("name"), pote.getString("desc"), pote.getDouble("@lat"), pote.getDouble("@lon")));
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
        	Toast.makeText(MainActivity.this, item.mDescription + "\n" + item.mTitle + "\n" + item.mGeoPoint.getLatitudeE6() + " : " + item.mGeoPoint.getLongitudeE6(), Toast.LENGTH_LONG).show();
            
			return true;
		}
    	
		public boolean onItemLongPress(int arg0, OverlayItem arg1)
		{
			return false;
		}
	};
	
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
		InfoBar.setText(this, road.getLengthDurationText(-1), true);
    }
	
	/**
	 * Fonction qui relocalise l'utilisateur en centrant sur la carte
	 */
	private void relocateUser()
    {
		mapController.setCenter(new GeoPoint(lastLocation.getLatitude(),lastLocation.getLongitude()));
		map.invalidate();
	}
}