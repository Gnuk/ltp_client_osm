package fr.univsavoie.ltp.client.map;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Classes outils divers lié a l'interaction avec la carte.
 */
public class Tools 
{
	/* Variables globales */
	private MainActivity mainActivity;
	
	/**
	 * Constructeur
	 * @param pActivity Instance de l'activité en cours
	 */
	public Tools(MainActivity pActivity)
	{
		this.mainActivity = pActivity;
	}
	
	/**
	 * Fonction qui reverse les coordonnées GPS en nom de ville, rue...
	 * @param p Points GPS (latitude, longitude)
	 * @return L'adresse postale
	 */
    public String getAddress(GeoPoint p)
    {
		GeocoderNominatim geocoder = new GeocoderNominatim(this.mainActivity);
		String theAddress;
		try
		{
			double dLatitude = p.getLatitudeE6() * 1E-6;
			double dLongitude = p.getLongitudeE6() * 1E-6;
			List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
			StringBuilder sb = new StringBuilder(); 
			if (addresses.size() > 0)
			{ 
				Address address = addresses.get(0); 
				int n = address.getMaxAddressLineIndex();
				for (int i=0; i<=n; i++)
				{
					if (i!=0)
					{
						sb.append(", ");
					}
					sb.append(address.getAddressLine(i));
				}
				theAddress = new String(sb.toString());
			}
			else
			{
				theAddress = null;
			}
		} catch (IOException e)
		{
			theAddress = null;
		}
		if (theAddress != null)
		{
			return theAddress;
		} else {
			return "";
		}
    }
    
    /**
     * Fonction qui relocalise l'utilisateur en centrant sur la carte
     */
    public void relocateUser(MapController pMapController, MapView pMapView, SimpleLocationOverlay pLocationOverlay, Location pLocation)
    {
    	pMapController.setCenter(new GeoPoint(pLocation.getLatitude(), pLocation.getLongitude()));   	
    	pLocationOverlay.setLocation(new GeoPoint(pLocation.getLatitude(), pLocation.getLongitude()));
    	pMapView.invalidate();
    }
    
	/**
	 * La barre d'infos qui se situe après l'action bar
	 */
	public void infoBar(MainActivity pActivity, String pMessage, boolean isVisible)
	{
		// On définit le message a afficher
		TextView msg = (TextView)pActivity.findViewById(R.id.textViewUserStatus);
		msg.setText(pMessage);

		// On charge le linearLayout ou on affiche la barre d'infos
		LinearLayout layoutInfos = (LinearLayout) pActivity.findViewById(R.id.linearLayoutInfos);

		// On lance une nouvelle animation pour afficher et faire disparaitre la barre
		Animation animationFadeInOut = AnimationUtils.loadAnimation(pActivity, R.anim.fade_in_out);
		layoutInfos.startAnimation(animationFadeInOut);
	}
    
    
	public Location getLastKnownLocation(LocationManager pLocationManager) 
	{
		List<String> providers = pLocationManager.getProviders(true);
		Location bestLocation = null;
		
		for (String provider : providers) 
		{
			Location l = pLocationManager.getLastKnownLocation(provider);

			if (l == null) {
				continue;
			}
			
			if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) 
			{
				bestLocation = l;
			}
		}
		
		if (bestLocation == null) 
		{
			return null;
		}
		
		return bestLocation;
	}
	
	
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(final Context pContext, boolean pWifi, boolean pMobile)
    {
    	String content = "";
    	if (!pWifi) {
    		content += "  - Activez le Wi-Fi.\n";
    	}
    	
    	if (!pMobile) {
    		content += "  - Activez le GPS et la localisation via le réseau mobile.\n";
    	}
    	
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(pContext);
 
        // Setting Dialog Title
        alertDialog.setTitle("Améliorer la précision de votre position");
 
        // Setting Dialog Message
        alertDialog.setMessage("Pour profiter pleinement de LocalizeTeaPot:\n" +
        		content);
 
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                pContext.startActivity(intent);
            }
        });
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }	
}
