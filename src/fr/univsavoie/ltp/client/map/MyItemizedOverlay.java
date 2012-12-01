package fr.univsavoie.ltp.client.map;

import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import android.app.AlertDialog;
import android.content.Context;

/** Simple implementation of an ItemizedOverlay handling tap events with osmdroid. 
 * 
 * Usage inside your map activity: <br>
 * <code>
 * ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
 * TappableItemizedOverlay<OverlayItem> overlay = new TappableItemizedOverlay(this, list);
 * mapView.getOverlays.add(overlay);
 * </code>
 */

public class MyItemizedOverlay extends ItemizedIconOverlay<OverlayItem>
{
	protected Context mContext;
	
	public MyItemizedOverlay(final Context context, final List<OverlayItem> aList)
	{
		super(context, aList, new OnItemGestureListener<OverlayItem>()
		{
			public boolean onItemSingleTapUp(final int index, final OverlayItem item)
			{
				return false;
			}
			public boolean onItemLongPress(final int index, final OverlayItem item)
			{
				return false;
			}
		});
		mContext = context;
	}
	
	protected boolean onSingleTapUpHelper(final int index, final OverlayItem item, final MapView mapView)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}
	
}
