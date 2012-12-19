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

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import fr.univsavoie.ltp.client.LoginActivity;
import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.R;
import fr.univsavoie.ltp.client.SignupActivity;

/**
 * Class qui gère l'affichage des différents popup sur la carte
 */
public class Popup
{
	/*
	 * Variables
	 */
	private MainActivity activity;
	
	
	/*
	 * Constructeur
	 */
	public Popup(MainActivity pActivity)
	{
		activity = pActivity;
	}
	
	/*
	 * Méthodes
	 */
	
	/**
     * Afficher sur la map un popup qui affiche les
     * informations de l'utilisateur connecté.
     */
    public final void popupDisplayUserInfos()
    {
		DisplayMetrics dm = new DisplayMetrics();
		this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = (int) (width * 0.75);
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) this.activity.findViewById(R.id.popupAccount);
		LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_account,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupUserInfos = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupUserInfos.setBackgroundDrawable(new BitmapDrawable());
		popupUserInfos.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		this.activity.findViewById(R.id.layoutMain).post(new Runnable() 
		{
			public void run() 
			{
				popupUserInfos.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
        // Ecouteur d'évènement sur le bouton pour se déconnecter
		Button close = (Button) layout.findViewById(R.id.close);
		close.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				popupUserInfos.dismiss();
			}
		});
    }
    
    /**
     * Afficher sur la map un popup qui propose a l'utilisateur
     * de mettre a jours son status
     */
    public final void popupPublishStatus()
    {
		DisplayMetrics dm = new DisplayMetrics();
		this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		
		int popupWidth = (int) (width * 0.75);
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		ScrollView viewGroup = (ScrollView) this.activity.findViewById(R.id.popupStatus);
		LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_set_status,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupPublishStatus = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupPublishStatus.setBackgroundDrawable(new BitmapDrawable());
		popupPublishStatus.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		this.activity.findViewById(R.id.layoutMain).post(new Runnable() 
		{
			public void run() 
			{
				popupPublishStatus.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */

        // Ecouteur d'évènement sur le bouton pour se déconnecter
		Button publish = (Button) layout.findViewById(R.id.btStatusPublish);
		publish.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				EditText userStatus = (EditText) layout.findViewById(R.id.fieldUserStatus);				
				if (userStatus.getText().toString().length() == 0 ) 
				{
					Toast.makeText(activity, "Impossible de publié ton status !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
				} 
				else if (userStatus.getText().toString().length() < 3 ) 
				{
					Toast.makeText(activity, "Impossible de publié ton status !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
				}
				else
				{
					String json = "{\"ltp\":{\"application\":\"Client LTP\",\"status\":{\"lon\" : \"" + String.valueOf(activity.getLongitude()) + "\",\"lat\" : \"" + String.valueOf(activity.getLatitude()) + "\",\"content\" : \"" + userStatus.getText().toString() + "\"}}}";
					activity.getSession().postJSON("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/statuses", "STATUSES", json);
					
					Toast.makeText(activity, "Status mise a jours !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
					
					// Actualise les marqueurs
					activity.displayFriends();
				}
			}
		});
    }
    
    /**
     * Afficher une boite au milieu de la carte si aucun utilisateur est connectés
     * pour proposer a l'invité, de se connecter ou s'incrire aupres du service LTP.
     */
    public final void popupGuest()
    {
		DisplayMetrics dm = new DisplayMetrics();
		this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		//final int height = dm.heightPixels;
		
		int popupWidth = (int) (width * 0.75);
		//int popupHeight = height / 2;
		
		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) this.activity.findViewById(R.id.popupGuest);
		LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_guest,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupGuest = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupGuest.setBackgroundDrawable(new BitmapDrawable());
		popupGuest.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		this.activity.findViewById(R.id.layoutMain).post(new Runnable() 
		{
			public void run() 
			{
				popupGuest.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
        // Ecouteur d'évènement sur le bouton des paramètres
        Button btLogin = (Button) layout.findViewById(R.id.btnPopupLogin);
        btLogin.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View view) 
            {
        		// La constante CODE_MON_ACTIVITE représente l’identifiant de la requête
        		// (requestCode) qui sera utilisé plus tard pour identifier l’activité
        		// renvoyant la valeur de retour.
        		Intent i = new Intent(activity, LoginActivity.class);    
        		activity.startActivityForResult(i, 1);
        		
        		popupGuest.dismiss();
            }
        });
        
        // Ecouteur d'évènement sur le bouton des paramètres
        Button btSignup = (Button) layout.findViewById(R.id.btnPopupSignup);
        btSignup.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View view) 
            {        		
        		// La constante CODE_MON_ACTIVITE représente l’identifiant de la requête
        		// (requestCode) qui sera utilisé plus tard pour identifier l’activité
        		// renvoyant la valeur de retour.
        		Intent i = new Intent(activity, SignupActivity.class);    
        		activity.startActivityForResult(i, 3);
        		
        		popupGuest.dismiss();
            }
        });        

        // Ecouteur d'évènement sur le bouton pour fermer l'application
		Button close = (Button) layout.findViewById(R.id.btnPopupClose);
		close.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) 
			{
				popupGuest.dismiss();
			}
		});
    }
    
    public final void popupGetStatus(JSONArray pStatusesArray)
    {
		DisplayMetrics dm = new DisplayMetrics();
		this.activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		//final int height = dm.heightPixels;
		final int width = dm.widthPixels;
		final int height = dm.heightPixels;
		
		int popupWidth = (int) (width * 0.75);
		int popupHeight = height;
		
		// Inflate the popup_layout.xml
		ScrollView viewGroup = (ScrollView) this.activity.findViewById(R.id.popupGetStatus);
		LayoutInflater layoutInflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = layoutInflater.inflate(R.layout.popup_get_status,viewGroup);		
		layout.setBackgroundResource(R.drawable.popup_gradient);

		// Créer le PopupWindow
		final PopupWindow popupPublishStatus = new PopupWindow(layout, popupWidth, LayoutParams.WRAP_CONTENT, true);
		popupPublishStatus.setBackgroundDrawable(new BitmapDrawable());
		popupPublishStatus.setOutsideTouchable(true);

		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
		final int OFFSET_X = 0;
		final int OFFSET_Y = 0;

		// Displaying the popup at the specified location, + offsets.
		this.activity.findViewById(R.id.layoutMain).post(new Runnable() 
		{
			public void run() 
			{
				popupPublishStatus.showAtLocation(layout, Gravity.CENTER, OFFSET_X, OFFSET_Y);
			}
		});
		
		/*
		 * Evenements composants du PopupWindow
		 */
		
		 // Find the ListView resource. 
		ListView mainListView = (ListView) layout.findViewById( R.id.listViewStatus );
		
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		
		SimpleAdapter adapter = new SimpleAdapter(this.activity, list, R.layout.custom_row_view,
				new String[] {"content","lat","lon"},
				new int[] {R.id.text1,R.id.text2, R.id.text3}
		);
		
		try 
		{
			// Appel REST pour recuperer les status de l'utilisateur connecté
			//Session session = new Session(this.activity);
			//session.parseJSONUrl("https://jibiki.univ-savoie.fr/ltpdev/rest.php/api/1/statuses", "STATUSES", "GET");
			
			// Parser la liste des amis dans le OverlayItem ArrayList
			HashMap<String,String> temp;
	        for (int i = 0 ; (i < 6); i++ )
	        {
	        	// Obtenir le status
	        	JSONObject status = pStatusesArray.getJSONObject(i);
	        	
	        	temp = new HashMap<String,String>();
	    		temp.put("content",status.getString("content"));
	    		temp.put("lat", String.valueOf(status.getDouble("lat")));
	    		temp.put("lon", String.valueOf(status.getDouble("lon")));
	    		list.add(temp);
	        }
		} 
		catch (JSONException jex) 
		{
			Log.e("Catch", "popupGetStatus / JSONException : " + jex.getStackTrace());
		} 
		catch (Exception ex) 
		{
			Log.e("Catch", "popupGetStatus / Exception : " + ex.getLocalizedMessage());
		}
		
		mainListView.setAdapter(adapter);
    }   
}
