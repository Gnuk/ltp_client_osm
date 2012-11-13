package fr.univsavoie.ltp.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends MainActivity 
{
	SharedPreferences settings;
	SharedPreferences.Editor editor;
    
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_login);
        
        // Instancier les préférences partagés
        settings = getApplicationContext().getSharedPreferences("USER_SETTINGS", 0);
        editor = settings.edit();
        
        // Ecouteur d'évènement sur le bouton de connexion
        Button boutonConnexion = (Button) findViewById(R.id.btnLogin);
        boutonConnexion.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
				// Recuperer email et mot de passe
		        EditText email = (EditText) findViewById(R.id.fieldEmail);
		        EditText password = (EditText) findViewById(R.id.fieldPassword);
		        
		        // Sauvegarder les préférences de l'utilisateur connecté
				SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
				editor.putString("USER_EMAIL", email.getText().toString());
				editor.putString("USER_PASSWORD", password.getText().toString());
				editor.commit();
				
				// Appeler la fonction pour s'authentifier auprès du service LTP
				auth();
				
				// Appeler la fonction pour parser les amis et les affichés sur la carte
				parseFriends();
				
				// On fermer l'activité
				finish();
			}
		});
	}
}
