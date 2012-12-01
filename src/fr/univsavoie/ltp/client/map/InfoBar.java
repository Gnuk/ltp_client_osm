package fr.univsavoie.ltp.client.map;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.R;

public class InfoBar
{
	/**
	 * La barre d'infos qui se situe après l'action bar
	 */
	public static void setText(MainActivity activity, String pMessage, boolean isVisible)
	{
		// On définit le message a afficher
		TextView msg = (TextView)activity.findViewById(R.id.textViewUserStatus);
		msg.setText(pMessage);

		// On charge le linearLayout ou on affiche la barre d'infos
		LinearLayout layoutInfos = (LinearLayout) activity.findViewById(R.id.linearLayoutInfos);

		// On lance une nouvelle animation pour afficher et faire disparaitre la barre
		Animation animationFadeInOut = AnimationUtils.loadAnimation(activity, R.anim.fade_in_out);
		layoutInfos.startAnimation(animationFadeInOut);
	}
}
