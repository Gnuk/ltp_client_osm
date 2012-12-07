package fr.univsavoie.ltp.client.map;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import fr.univsavoie.ltp.client.MainActivity;

import android.location.Address;

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
    public void relocateUser(MapController pMapController, MapView pMapView, GPSTracker pGPS)
    {
    	pMapController.setCenter(new GeoPoint(pGPS.getLatitude(), pGPS.getLongitude()));   	
    	pMapView.invalidate();
    }

}
