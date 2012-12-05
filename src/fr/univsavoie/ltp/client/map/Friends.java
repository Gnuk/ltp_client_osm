package fr.univsavoie.ltp.client.map;

public class Friends 
{
	private String nom;
	private double longitude;
	private double latitude;
	private String status;
	
	public Friends(String n, String s, double lat, double lon)
	{
		nom = n;
		status = s;
		latitude = lat;
		longitude = lon;
	}
	
	public String getNom()
	{
		return nom;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public double getLatitude()
	{
		return latitude;
	}
	
	public double getLongitude()
	{
		return longitude;
	}
}
