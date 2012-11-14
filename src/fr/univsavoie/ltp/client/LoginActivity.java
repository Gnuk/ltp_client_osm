package fr.univsavoie.ltp.client;

import fr.univsavoie.ltp.client.tools.SharedVariables;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends MainActivity 
{
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	Intent resultIntent;
	
	private String PUBLIC_STATIC_STRING_IDENTIFIER = "test";
    
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_login);    
        
        resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, 1);
        
        SharedVariables.displayAuthbox = false;
        
        // Ecouteur d'évènement sur le bouton de connexion
        Button boutonConnexion = (Button) findViewById(R.id.btnLogin);
        boutonConnexion.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
				// Récupérer email et mot de passe
		        EditText email = (EditText) findViewById(R.id.fieldEmail);
		        EditText password = (EditText) findViewById(R.id.fieldPassword);
		        
		        // Instance de SharedPreferences pour enregistrer des données dans un fichier
		        SharedPreferences myPrefs = getSharedPreferences("UserPrefs", MODE_WORLD_READABLE); // Ici on permet donc la lecture de notre fichier de préférence à toutes les applications
		        SharedPreferences.Editor prefsEditor = myPrefs.edit(); // Instance de l'editeur permettant d'écrire dans le fichier
		        prefsEditor.putString("Email", email.getText().toString()); // Données
		        prefsEditor.putString("Password", password.getText().toString()); // Données
		        prefsEditor.commit(); // Valider les modifications
				
		        setResult(Activity.RESULT_OK, resultIntent);

		        // On quitter cette activity...
		        // ... et via onActivityResult, on reappel les fonctions de login / affichage de carte
		        // car, il faut resyncronisé par rapport au nouvel utilisateur.
				finish();
			}
		});
	}
}
