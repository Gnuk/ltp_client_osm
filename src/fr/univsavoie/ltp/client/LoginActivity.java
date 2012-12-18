package fr.univsavoie.ltp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import fr.univsavoie.ltp.client.tools.SimpleSSLSocketFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity 
{
	private EditText login;
	private EditText password;
	private Button btConnexion;
	
	private DefaultHttpClient httpClient;
	private HttpGet httpGet;
	
    /** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_login);      
             
        // Ecouteur d'évènement sur le bouton de connexion
        btConnexion = (Button) findViewById(R.id.btnLogin);
        btConnexion.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				// Récupérer email et mot de passe
		        login = (EditText) findViewById(R.id.fieldEmail);
		        password = (EditText) findViewById(R.id.fieldPassword);
		        
		        auth();
		        
		        httpGet = new HttpGet("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/user");
		        
		        new HttpGetUserTask().execute();
			}
		});
	}

	/**
	 * Classe spéciale AsyncTask pour faire proprement des requêtes HTTP
	 */
    class HttpGetUserTask extends AsyncTask<String, Object, String>
    {
    	int code;
    	
        protected String doInBackground(String... url) 
        {
        	String json = null;
        	
            try
            {
				runOnUiThread(new Runnable() {
					public void run() {
						btConnexion.setEnabled(false);
					}
				});
            	
            	HttpResponse httpResp = httpClient.execute(httpGet);
            	code = httpResp.getStatusLine().getStatusCode();
            	
            	BufferedReader reader = new BufferedReader(new InputStreamReader(httpResp.getEntity().getContent(), "UTF-8"));
            	json = reader.readLine();
            }
            catch (Exception e) 
            {
            	Log.e("Catch", "HttpGetUserTask: " + e.getLocalizedMessage());
            }
			return json;
        }

        protected void onPostExecute(String result) 
        {
        	// On arrive pas a authentifié l'utilisateur (erreur de login ?)
        	if (code != 200)
        	{
        		Toast.makeText(LoginActivity.this, "Login et/ou mot de passe incorrecte !", Toast.LENGTH_LONG).show();
        		
				runOnUiThread(new Runnable() 
				{
					public void run() 
					{
						btConnexion.setEnabled(true);
					}
				});
        	}
        	else
        	{
		        // Instance de SharedPreferences pour enregistrer des données dans un fichier
		        SharedPreferences myPrefs = getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
		        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
		        prefsEditor.putString("Login", login.getText().toString()); // Données
		        //prefsEditor.putString("Email", null); // Données
		        prefsEditor.putString("Password", password.getText().toString()); // Données
		        prefsEditor.commit(); // Valider les modifications
		        
		        // Renvoyer une valeur à l'activité principal
		        setResult(Activity.RESULT_OK);

		        // On quitter cette activity...
				finish();
        	}
        }
    }
    
    /**
     * Pavé de code permetant de se connecter de façon sécurisé au serveur
     */
    private void auth()
    {
    	try 
    	{
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
    		Credentials creds = new UsernamePasswordCredentials(login.getText().toString(), password.getText().toString());
    		authCred.setCredentials(AuthScope.ANY, creds);

    		httpClient.addRequestInterceptor(preemptiveAuth, 0);
    		httpClient.setCredentialsProvider(authCred);
		} 
    	catch (Exception e) 
    	{
    		Log.e("Catch", "Auth: " + e.getLocalizedMessage());
		}
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) 
	    {
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
