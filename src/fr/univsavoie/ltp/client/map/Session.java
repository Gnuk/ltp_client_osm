package fr.univsavoie.ltp.client.map;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
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

import android.content.Intent;
import android.content.SharedPreferences;
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
	 * Variables
	 */
	
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	private ResponseHandler<String> responseHandler;
	private MainActivity activity;
	
	
	/*
	 * Constructeur
	 */
	public Session(MainActivity activite)
	{
		this.activity = activite;
	}
	
	
	/*
	 * Méthodes
	 */
	
	 /**
     * S'authentifier auprès du service LocalizeTeaPot
     */
    public final void auth() 
	{
		try
		{
			// Instance de SharedPreferences pour lire les données dans un fichier
			SharedPreferences myPrefs = activity.getSharedPreferences("UserPrefs", this.activity.MODE_WORLD_READABLE); 
			String login = myPrefs.getString("Email", null);
			String password = myPrefs.getString("Password", null);
			
			Log.i("Watch", "login : " + login);
			Log.i("Watch", "password : " + password);
			
			HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() 
			{
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

			setHttpClient(new DefaultHttpClient(ccm, params));

			CredentialsProvider authCred = new BasicCredentialsProvider();
			Credentials creds = new UsernamePasswordCredentials(login, password);
			authCred.setCredentials(AuthScope.ANY, creds);

			getHttpClient().addRequestInterceptor(preemptiveAuth, 0);
			getHttpClient().setCredentialsProvider(authCred);
			
			setHttpGet(new HttpGet("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/statuses"));
			setResponseHandler(new BasicResponseHandler());
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
    
    /**
     * Fonction qui recupère la liste des amis de l'utilisateur connecté
     * @return Retourne un JSONArray a utilisé dans une boucle pour extraire les données JSON
     */
    public final JSONArray parseFriends()
    {
    	String response = null;
        
        try 
        {
        	// FIX POUR EVITER QUE L'APPLI PLANTE SUR DES VERSION ANDROID AU SDK > 8
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
					Log.e("Catch", "> parseFriends() - SDK_ERR: " + ex.getMessage());
			    }
			}
        	
			// Recuperer la reponse de la connexion REST
        	response = getHttpClient().execute(getHttpGet(), getResponseHandler());

        	// Créer des objets JSON pour récupérer les informations récupéré sur le serveur REST
			JSONObject potes = new JSONObject(response);		
			JSONArray potesArray = potes.getJSONObject("ltp").getJSONArray("statuses");
	
			// On retourne la liste des amis
			return potesArray;
		} 
        catch (IOException e) 
        {
			Log.e("Catch", "> parseFriends() - IOException: " + e.getMessage());
		} 
        catch (JSONException e) 
        {
			Log.e("Catch", "> parseFriends() - JSONException: " + e.getMessage());
		}
        catch (Exception e) 
        {
			Log.e("Catch", "> parseFriends() - Exception: " + e.getMessage());
		}
        
        return null;
    }
    
    /**
     * Procédure qui déconnecte l'utilisateur actif
     */
    public final void logout()
    {
        // Instance de SharedPreferences pour enregistrer des données dans un fichier
        SharedPreferences myPrefs = this.activity.getSharedPreferences("UserPrefs", this.activity.MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
        prefsEditor.putString("Email", null); // Données
        prefsEditor.putString("Password", null); // Données
        prefsEditor.commit(); // Valider les modifications
        
        // Redémarrer l'activité pour prendre en compte les modifications
		Intent intent = this.activity.getIntent();
		this.activity.finish();
		this.activity.startActivity(intent);
    }

    
    /*
     * Getters & Setters
     */
    
	public DefaultHttpClient getHttpClient() 
	{
		return httpClient;
	}

	public void setHttpClient(DefaultHttpClient httpClient) 
	{
		this.httpClient = httpClient;
	}

	public HttpGet getHttpGet() 
	{
		return httpGet;
	}

	public void setHttpGet(HttpGet httpGet) 
	{
		this.httpGet = httpGet;
	}

	public ResponseHandler<String> getResponseHandler() 
	{
		return responseHandler;
	}

	public void setResponseHandler(ResponseHandler<String> responseHandler) 
	{
		this.responseHandler = responseHandler;
	}
}
