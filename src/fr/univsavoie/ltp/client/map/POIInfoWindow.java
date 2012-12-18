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

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.MapView;

import fr.univsavoie.ltp.client.R;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * A customized InfoWindow handling POIs. 
 * We inherit from DefaultInfoWindow as it already provides most of what we want. 
 * And we just add support for a "more info" button. 
 * 
 * @author M.Kergall
 */
public class POIInfoWindow extends DefaultInfoWindow {
	
	private POI mSelectedPOI;
	
	public POIInfoWindow(MapView mapView) {
		super(R.layout.bonuspack_bubble, mapView);
		
		Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
			//bonuspack_bubble layouts already contain a "more info" button. 
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (mSelectedPOI.mUrl != null){
					Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSelectedPOI.mUrl));
					view.getContext().startActivity(myIntent);
				}
			}
		});
	}

	@Override public void onOpen(ExtendedOverlayItem item){
		mSelectedPOI = (POI)item.getRelatedObject();
		
		super.onOpen(item);
		
		//Fetch the thumbnail in background
		if (mSelectedPOI.mThumbnailPath != null){
			ImageView imageView = (ImageView)mView.findViewById(R.id.bubble_image);
			mSelectedPOI.fetchThumbnailOnThread(imageView);
		}
		
		//Show or hide "more info" button:
		if (mSelectedPOI.mUrl != null)
			mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
		else
			mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.GONE);
		
	}
}
