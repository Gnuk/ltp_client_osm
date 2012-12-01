package fr.univsavoie.ltp.client.map;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
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
		
		int popupWidth = width / 2;
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
			@Override
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
			@Override
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
		
		int popupWidth = width / 2;
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
			@Override
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
			@Override
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
					Toast.makeText(activity, "Status mise a jours !", Toast.LENGTH_LONG).show();
					popupPublishStatus.dismiss();
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
		
		int popupWidth = width / 2;
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
			@Override
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
        	@Override
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
        	@Override
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
			@Override
			public void onClick(View v) 
			{
				popupGuest.dismiss();
			}
		});
    }
}
