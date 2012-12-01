package fr.univsavoie.ltp.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity 
{
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	Intent resultIntent;
	
	/** Méthode appelée la première fois lors de la création de la classe. */
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		// Create settings activity
		setContentView(R.layout.activity_login);

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
				prefsEditor.putString("Email", email.getText().toString());
				prefsEditor.putString("Password", password.getText().toString());
				prefsEditor.commit(); // Valider les modifications
				
				// Renvoyer une valeur à l'activité principal
				setResult(Activity.RESULT_OK);
				
				// On quitter cette activity...
				// ... et via onActivityResult, on reappel les fonctions de login / affichage de carte
				// car, il faut resyncronisé par rapport au nouvel utilisateur.
				finish();
			}
		});
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
