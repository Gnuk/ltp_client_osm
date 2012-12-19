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

package fr.univsavoie.ltp.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SignupActivity extends Activity 
{
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	Intent resultIntent;
    
    /** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Create settings activity
        setContentView(R.layout.activity_signup);
        
        // Ecouteur d'évènement sur le bouton de connexion
        Button btRegister = (Button) findViewById(R.id.btnRegister);
        btRegister.setOnClickListener(new View.OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
				// Si on a pas d'erreur lors de la vérification des infos saisies dans
				// les champs, on valide et envoie l'inscription au serveur LTP.
				if (checkFields())
				{
			        // Renvoyer une valeur à l'activité principal
			        setResult(Activity.RESULT_OK);
			        
			        // On ferme cette activité
					finish();
				}
			}
		});
	}
    
    /**
     * Procédure pour vérifier les informations saisies dans les champs
     * @return Vrai si pas d'erreur, Faux si erreurs...
     */
    private boolean checkFields()
    {
    	boolean fieldsOk = true;
    	
    	/*
    	 * Verifications champ login
    	 */
		EditText login = (EditText)findViewById(R.id.fieldId);
		if( login.getText().toString().length() == 0 )
		{
			login.setError( "Le nom d'utilisateur est requis !" );
			fieldsOk = false;
		} 
		else if (login.getText().toString().length() <3 )
		{
			login.setError( "Votre nom d'utilisateur doit etre au minimum de 3 caractères !" );
			fieldsOk = false;
		}
		
		/*
		 * Verifications champ email
		 */
		EditText email = (EditText)findViewById(R.id.fieldEmail);
		if( email.getText().toString().length() == 0 ) 
		{
			email.setError( "Une adresse email valide est requis !" );
			fieldsOk = false;
		}
		
		/*
		 * Verifications champs mot de passe
		 */
		EditText password = (EditText)findViewById(R.id.fieldPassword);
		if( password.getText().toString().length() == 0 )
		{
			password.setError( "Un mot de passe est requis !" );
			fieldsOk = false;
		} 
		else if (password.getText().toString().length() <6 )
		{
			password.setError( "Votre mot de passe doit faire au minimum 6 caractères !" );
			fieldsOk = false;
		}
		
		EditText password2 = (EditText)findViewById(R.id.fieldPasswordCheck);
		if( password2.getText().toString().length() == 0 )
		{
			password2.setError( "La véfication de votre mot de passe est requis !" );
			fieldsOk = false;
		} 
		else if (password2.getText().toString().length() <6 )
		{
			password2.setError( "Votre mot de passe doit faire au minimum 6 caractères !" );
			fieldsOk = false;
		}
		
		if (checkPassWordAndConfirmPassword(password.getText().toString(), password2.getText().toString()) == false)
		{
			password.setError( "Le mot de passe de vérification n'est pas identique !" );
			password2.setError( "Le mot de passe de vérification n'est pas identique !" );
			fieldsOk = false;
		}
		
		return fieldsOk;
    }
    
    /**
     * Fonction qui vérifie le mot de passe saisie
     * @param pPassword
     * @param pConfirmPassword
     * @return Vrai si mot de passe de vérif identique a celui de base
     */
    public boolean checkPassWordAndConfirmPassword(String pPassword,String pConfirmPassword) 
    {
        boolean pstatus = false;
        if (pConfirmPassword != null && pPassword != null) 
        {
          if (pPassword.equals(pConfirmPassword)) 
          {
               pstatus = true;
          } 
        }
       return pstatus;
   }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
}
