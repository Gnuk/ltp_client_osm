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
