package fr.univsavoie.ltp.client.map;

import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.MapView;

import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.R;
import android.view.View;
import android.widget.Button;

/**
 * A customized InfoWindow handling "itinerary" points (start, destination and via-points). 
 * We inherit from DefaultInfoWindow as it already provides most of what we want. 
 * And we just add support for a "remove" button. 
 * 
 * @author M.Kergall
 */
public class ViaPointInfoWindow extends DefaultInfoWindow
{

	int mSelectedPoint;
	
	public ViaPointInfoWindow(int layoutResId, MapView mapView)
	{
		super(layoutResId, mapView);
		Button boutonSupprimer = (Button)(mView.findViewById(R.id.bubble_delete));
		boutonSupprimer.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				MainActivity mapActivity = (MainActivity) view.getContext();
				mapActivity.removePoint(mSelectedPoint);
				close();
			}
		});
	}
	
	public void onOpen(ExtendedOverlayItem item)
	{
		mSelectedPoint = (Integer)item.getRelatedObject();
		super.onOpen(item);
	}

}
