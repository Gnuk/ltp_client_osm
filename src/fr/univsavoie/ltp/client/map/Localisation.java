package fr.univsavoie.ltp.client.map;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import fr.univsavoie.ltp.client.MainActivity;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Classe Localisation
 * 
 * Permet de mettre à jour la position de l'utilisateur sur la carte.
 */
public class Localisation implements LocationListener
{
	private MainActivity activity;
	private MapView map;
	private GeoPoint startPoint;
	
	private ArrayList<OverlayItem> overlayItemArray;
	
	/**
	 * Constructeur de la classe Localisation
	 * 
	 * @param a une référence vers la MainActivity
	 * @param m une référence vers la MapView
	 * @param s une référence vers le GeoPoint de départ
	 */
	public Localisation(MainActivity a, MapView m, GeoPoint s)
	{
		activity = a;
		map = m;
		startPoint = s;
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

				InfoBar.setText(activity, "T'es localisé: " + Adresse.getAddress(startPoint, activity), true);
			} else
			{
				InfoBar.setText(activity, "Ta position actuel n'a pas été trouvé !" + latLongString, true);
			}
		} catch (Exception e)
		{
			Log.e("Catch", "> updateUserInterface() - Exception : " + e.getMessage());
		}
	}
	
	public void onLocationChanged(Location location)
	{
		updateLoc(location);
	}
	
	public void onProviderDisabled(String provider){}
	
	public void onProviderEnabled(String provider){}
	
	public void onStatusChanged(String provider, int status, Bundle extras){}
}
