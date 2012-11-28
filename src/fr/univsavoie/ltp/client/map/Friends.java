package fr.univsavoie.ltp.client.map;

public class Friends 
{
	private String nom, prenom;
	private double longitude;
	private double latitude;
	private String status;
	
	public Friends(String pNom, String pStatus, double pLongitude, double pLatitude)
	{
		this.setNom(pNom);
		this.setStatus(pStatus);
		this.setLongitude(pLongitude);
		this.setLatitude(pLatitude);
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
