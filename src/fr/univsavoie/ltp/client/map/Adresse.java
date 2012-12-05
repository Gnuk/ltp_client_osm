package fr.univsavoie.ltp.client.map;

import java.io.IOException;
import java.util.List;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;

import fr.univsavoie.ltp.client.MainActivity;

import android.location.Address;

public class Adresse
{
    /** 
     * Reverse Geocoding
     */
    public static String getAddress(GeoPoint p, MainActivity mainActivity)
    {
		GeocoderNominatim geocoder = new GeocoderNominatim(mainActivity);
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
}
