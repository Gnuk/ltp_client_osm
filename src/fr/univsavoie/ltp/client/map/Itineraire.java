package fr.univsavoie.ltp.client.map;

import java.util.List;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import fr.univsavoie.ltp.client.MainActivity;
import fr.univsavoie.ltp.client.R;

public class Itineraire
{
	public static void updateUIWithRoad(MainActivity activity, Road road, ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers, PathOverlay roadOverlay, MapView map)
	{
		roadNodeMarkers.removeAllItems();
		List<Overlay> mapOverlays = map.getOverlays();
		if (roadOverlay != null)
		{
			mapOverlays.remove(roadOverlay);
		}
		if (road == null)
		{
			return;
		}
		if (road.mStatus == Road.STATUS_DEFAULT)
		{
			Toast.makeText(map.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
		}
		roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
		
		mapOverlays.add(removedOverlay);
		putRoadNodes(activity, road, roadNodeMarkers, roadOverlay, map);
		map.invalidate();
		
		InfoBar.setText(activity, road.getLengthDurationText(-1), true);
	}
	
	private static void putRoadNodes(MainActivity activity, Road road, ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers, PathOverlay roadOverlay, MapView map)
	{
		roadNodeMarkers.removeAllItems();
		Drawable marker = activity.getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		TypedArray iconIds = activity.getResources().obtainTypedArray(R.array.direction_icons);
		for (int i = 0; i < n; i++)
		{
			RoadNode node = road.mNodes.get(i);
			String instructions = (node.mInstructions == null ? "" : node.mInstructions);
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Step " + (i + 1), instructions, node.mLocation, activity);
			nodeMarker.setSubDescription(road.getLengthDurationText(node.mLength, node.mDuration));
			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
			nodeMarker.setMarker(marker);
			int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
			if (iconId != R.drawable.ic_empty)
			{
				Drawable icon = activity.getResources().getDrawable(iconId);
				nodeMarker.setImage(icon);
			}
			roadNodeMarkers.addItem(nodeMarker);
		}
	}
}
