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
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class UserPreferencesActivity extends PreferenceActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_preferences);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			Intent result = new Intent("Complete");
    		setResult(Activity.RESULT_OK, result);
			finish();
			return true;
		}
		return false;
	}
}
