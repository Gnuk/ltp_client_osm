/*
	Copyright 2012 OpenTeamMap
	
	This software is a part of LocalizeTeaPot whose purpose is to Localize your friends.
	
	This software is governed by the CeCILL license under French law and
	abiding by the rules of distribution of free software.  You can  use, 
	modify and/ or redistribute the software under the terms of the CeCILL
	license as circulated by CEA, CNRS and INRIA at the following URL
	"http://www.cecill.info". 
	
	As a counterpart to the access to the source code and  rights to copy,
	modify and redistribute granted by the license, users are provided only
	with a limited warranty  and the software's author,  the holder of the
	economic rights,  and the successive licensors  have only  limited
	liability. 
	
	In this respect, the user's attention is drawn to the risks associated
	with loading,  using,  modifying and/or developing or reproducing the
	software by the user in light of its specific status of free software,
	that may mean  that it is complicated to manipulate,  and  that  also
	therefore means  that it is reserved for developers  and  experienced
	professionals having in-depth computer knowledge. Users are therefore
	encouraged to load and test the software's suitability as regards their
	requirements in conditions enabling the security of their systems and/or 
	data to be ensured and,  more generally, to use and operate it in the 
	same conditions as regards security. 
	
	The fact that you are presently reading this means that you have had
	knowledge of the CeCILL license and that you accept its terms.
 */

package fr.univsavoie.ltp.client.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
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
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

/**
 * Classe qui gère tout ce qui est l'authentification sur le serveur rest,
 * la récupération des données (liste des amis, profil utilisateur, ...) et
 * l'envoie de données (nouveau status de l'utilisateur, ...).
 */
public class Session
{
	/*
	 * Variables globale
	 */
	
	// Méthodes Apache HTTP
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private HttpPost httpPost;
	private HttpPut httpPut;
	
	// Appel d'instance de classe
	private MainActivity activity;
	
	// Traitement des données JSON
	private JSONArray lastJSONArray;
	private List<NameValuePair> listNameValuePair;
	private String JSONDatas;
	private int code;
	
	
	/*
	 * Constructeur
	 */
	
	/**
	 * Constructeur pour récupérer la session en cours
	 * @param activite
	 */
	public Session(MainActivity activite)
	{
		this.activity = activite;
	}
	
	
	/*
	 * Méthodes
	 */
	
    /**
     * Procédure qui s'authentifie sur le serveur REST avec les données utilisateurs de façon sécurisé (protocole HTTPS).
     * Appeler secureAuth() avant chaque nouvelles requêtes HTTP (get, post, ...)
     */
	private void secureAuth() 
	{
		try 
		{
			// Instance de SharedPreferences pour lire les données dans un fichier
			SharedPreferences myPrefs = activity.getSharedPreferences("UserPrefs", activity.MODE_WORLD_READABLE);
			String login = myPrefs.getString("Login", null);
			String password = myPrefs.getString("Password", null);

			HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() 
			{
				public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException 
				{
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

			// Setup a custom SSL Factory object which simply ignore the certificates validation and accept all type of self signed certificates
			SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
			sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Enable HTTP parameters
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			// Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
			SchemeRegistry registry = new SchemeRegistry();
			// registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sslFactory, 443));

			// Create a new connection manager using the newly created registry and then create a new HTTP client using this connection manager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			httpClient = new DefaultHttpClient(ccm, params);

			CredentialsProvider authCred = new BasicCredentialsProvider();
			Credentials creds = new UsernamePasswordCredentials(login, password);
			authCred.setCredentials(AuthScope.ANY, creds);

			httpClient.addRequestInterceptor(preemptiveAuth, 0);
			httpClient.setCredentialsProvider(authCred);
		} 
		catch (KeyManagementException e) 
		{
			e.printStackTrace();
		} 
		catch (UnrecoverableKeyException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		} 
		catch (KeyStoreException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Classe spéciale AsyncTask pour faire proprement des requêtes HTTP
	 * Permet de lancer la commande GET pour récupérer la liste des amis de l'utilisateur connecté
	 */
    class HttpMapGetFriendsTask extends AsyncTask<String, Object, String>
    {
        protected String doInBackground(String... url) 
        {
        	String json = null;
        	
            try 
            {
            	//ResponseHandler<String> responseHandler = new BasicResponseHandler();
            	//httpResponse = httpClient.execute(httpGet, responseHandler);
            	
            	HttpResponse httpResp = httpClient.execute(httpGet);
            	setCode(httpResp.getStatusLine().getStatusCode());
            	
            	Log.d("Watch", String.valueOf(getCode()));
            	
            	BufferedReader reader = new BufferedReader(new InputStreamReader(httpResp.getEntity().getContent(), "UTF-8"));
            	json = reader.readLine();;
            }
            catch (Exception e) 
            {
            	Log.e("Catch", "RetreiveHttpClientTask: " + e.getLocalizedMessage());
            }
			return json;
        }

        protected void onPostExecute(String result) 
        {
        	try 
        	{
            	// Créer des objets JSON pour récupérer les informations récupéré sur le serveur REST
    			JSONObject potes = new JSONObject(result);		
    			JSONArray potesArray = potes.getJSONObject("ltp").getJSONArray("friends");
    			activity.parseJSONResult(potesArray);
			} 
        	catch (Exception e) 
        	{
				Log.e("Catch", "RetreiveHttpClientTask / onPostExecute : " + e.getLocalizedMessage());
			}
        }
     }
    

    
	/**
	 * Classe spéciale AsyncTask pour faire proprement des requêtes HTTP
	 * Permet de lancer la commande GET pour recuperer les derniers status de l'utilisateur connecté
	 */
    class HttpMapGetStatusesTask extends AsyncTask<String, Object, String>
    {
        protected String doInBackground(String... url) 
        {
        	String httpResponse = null;
            try 
            {
            	ResponseHandler<String> responseHandler = new BasicResponseHandler();
            	httpResponse = httpClient.execute(httpGet, responseHandler);
            }
            catch (Exception e) 
            {
            	Log.e("Catch", "RetreiveHttpClientTask: " + e.getLocalizedMessage());
            }
			return httpResponse;
        }

        protected void onPostExecute(String result) 
        {
        	try 
        	{
            	// Créer des objets JSON pour récupérer les informations récupéré sur le serveur REST
    			JSONObject statuses = new JSONObject(result);		
    			JSONArray statusesArray = statuses.getJSONObject("ltp").getJSONArray("statuses");
    			activity.getPopup().popupGetStatus(statusesArray);
			} 
        	catch (Exception e) 
        	{
				Log.e("Catch", "HttpMapGetStatusesTask / onPostExecute : " + e.getLocalizedMessage());
			}
        }
     }
    
	/**
	 * Classe spéciale AsyncTask pour faire proprement des requêtes HTTP
	 * Permet de lancer la commande POST pour l'envoie de status
	 */
    class HttpPostStatusesTask extends AsyncTask<String, Object, String>
    {
        protected String doInBackground(String... json) 
        {
        	String httpResponse = null;
            try 
            {
            	// Créer des objets JSON pour récupérer les informations récupéré sur le serveur REST
    			//JSONObject status = new JSONObject();	
    			
                // Remplir l'objet json des données client
                /*for (NameValuePair nameValuePair : getListNameValuePair()) {
                	status.put(nameValuePair.getName(), nameValuePair.getValue());
				}*/
            	ResponseHandler<String> responseHandler = new BasicResponseHandler();
            	
                httpPost.setEntity(new ByteArrayEntity(json[0].getBytes("UTF8")));
                httpPost.setHeader( "Content-Type", "application/json");
                
            	httpResponse = httpClient.execute(httpPost, responseHandler);
            }
            catch (Exception e) 
            {
            	Log.e("Catch", "HttpPostStatusesTask: " + e.getLocalizedMessage());
            }
			return httpResponse;
        }

        protected void onPostExecute(String result) 
        {
        	
        }
     }
    
	/**
	 * Classe spéciale AsyncTask pour faire proprement des requêtes HTTP
	 * Permet de lancer la commande PUT pour le Tracker
	 */
    class HttpPutTrackerTask extends AsyncTask<String, Object, String>
    {
        protected String doInBackground(String... json) 
        {
        	String httpResponse = null;
            try 
            {
            	Log.d("Watch", "JSON: " + json[0]);
            	
            	ResponseHandler<String> responseHandler = new BasicResponseHandler();
            	
            	httpPut.setEntity(new ByteArrayEntity(json[0].getBytes("UTF8")));
            	httpPut.setHeader( "Content-Type", "application/json");
                
            	httpResponse = httpClient.execute(httpPut, responseHandler);
            }
            catch (Exception e) 
            {
            	Log.e("Catch", "HttpPutTrackerTask: " + e.getLocalizedMessage());
            }
			return httpResponse;
        }

        protected void onPostExecute(String result) 
        {
        	try 
        	{
            	// Créer des objets JSON pour récupérer les informations récupéré sur le serveur REST
    			JSONObject statuses = new JSONObject(result);		
    			JSONArray statusesArray = statuses.getJSONObject("ltp").getJSONArray("statuses");
    			activity.getPopup().popupGetStatus(statusesArray);
			} 
        	catch (Exception e) 
        	{
				Log.e("Catch", "HttpPutTrackerTask / onPostExecute : " + e.getLocalizedMessage());
			}
        }
    }
    
    /**
     * Méthode POST en JSON pour envoyé des données sur le serveur REST
     * @param pUrl 		URL du service
     * @param pType 	Type du service
     * @param pDatas 	Données a envoyé (au format JSON)
     */
    public final void postJSON(String pUrl, String pType, String pDatas)
    {
        try 
        {
        	// Etablir une connexion sécurisé entre l'application client et serveur
			secureAuth();
			
			// Nouvelle requete http de type POST
			httpPost = new HttpPost(pUrl);
			
			if (pType == "FRIENDS")
			{
				//new HttpMapGetFriendsTask().execute();
			} 
			else if (pType == "STATUSES")
			{
				new HttpPostStatusesTask().execute(pDatas);
			} 
			else if (pType == "TRACKER")
			{
				//new HttpMapPostTrackerTask().execute();
			}
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> postJSON() - Exception: " + e.getMessage());
		}
    }
    
    /**
     * Méthode GET pour la récuperation des données sur le serveur REST
     * @param pUrl		URL du service
     * @param pType 	Nom du service
     */
    public final void getJSON(String pUrl, String pType)
    {
        try 
        {
        	// Etablir une connexion sécurisé entre l'application client et serveur
			secureAuth();
			
			// Nouvelle requete http de type GET
			httpGet = new HttpGet(pUrl);
			
			if (pType == "FRIENDS")
			{
				new HttpMapGetFriendsTask().execute();
			} 
			else if (pType == "STATUSES")
			{
				new HttpMapGetStatusesTask().execute();
			}
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> getJSON() - Exception: " + e.getMessage());
		} 
    }
    
    /**
     * Méthode PUT pour l'envoie de données sur le serveur REST
     * @param pUrl		Adresse du service
     * @param pType		Type de service
     * @param pDatas	Données a envoyé au format JSON
     */
    public final void putJSON(String pUrl, String pType, String pDatas)
    {
        try 
        {
        	// Etablir une connexion sécurisé entre l'application client et serveur
			secureAuth();
			
			// Nouvelle requete http de type GET
			httpPut = new HttpPut(pUrl);
			
			if (pType == "TRACKER")
			{
				new HttpPutTrackerTask().execute(pDatas);
			}
		} 
        catch (Exception e) 
        {
			Log.e("Catch", "> putJSON() - Exception: " + e.getLocalizedMessage());
		} 
    }
    
    /**
     * Procédure qui déconnecte l'utilisateur actif
     */
    public final void logout()
    {
        // Instance de SharedPreferences pour enregistrer des données dans un fichier
        SharedPreferences myPrefs = this.activity.getSharedPreferences("UserPrefs", this.activity.MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
        prefsEditor.putString("Login", null); // Données
        prefsEditor.putString("Password", null); // Données
        prefsEditor.commit(); // Valider les modifications
        
        // Redémarrer l'activité pour prendre en compte les modifications
		Intent intent = this.activity.getIntent();
		this.activity.finish();
		this.activity.startActivity(intent);
    }
    
    /**
     * On remplit le JSONArray avec le dernier contenu de la requete REST.
     * Ayez donc bien a l'esprit que cette méthode est remplit avec les dernieres
     * données recuperer sur le serveur REST !
     * @param pJSONArray L'array peuplé de données utiles
     * @return Le meme array passé en paramètre !
     */
    public JSONArray fillJSONArray(JSONArray pJSONArray)
    {
    	return pJSONArray;
    }
    
    
    /*
     * Getteurs & Setteurs
     * A les appeler quand on veux traiter des requêtes REST d'une page a une autre
     */

	public JSONArray getLastJSONArray() {
		return lastJSONArray;
	}

	public void setLastJSONArray(JSONArray lastJSONArray) {
		this.lastJSONArray = lastJSONArray;
	}

	public List<NameValuePair> getListNameValuePair() {
		return listNameValuePair;
	}

	public void setListNameValuePair(List<NameValuePair> listNameValuePair) {
		this.listNameValuePair = listNameValuePair;
	}

	public String getJSONDatas() {
		return JSONDatas;
	}

	public void setJSONDatas(String jSONDatas) {
		JSONDatas = jSONDatas;
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}
}