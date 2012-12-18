package fr.univsavoie.ltp.client.map;

import java.util.List;

import fr.univsavoie.ltp.client.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FriendsAdapter extends BaseAdapter 
{
	List<Friends> friends;
	LayoutInflater inflater;
	
	public FriendsAdapter(Context context,List<Friends> pFriends) 
	{
		inflater = LayoutInflater.from(context);
		this.friends = pFriends;
	}
	
	public int getCount() 
	{
		return friends.size();
	}
	
	public Object getItem(int position) 
	{
		return friends.get(position);
	}
	
	public long getItemId(int position) 
	{
		return position;
	}
	
	private class ViewHolder 
	{
		TextView tvName;
		TextView tvStatus;
		TextView tvLat;
		TextView tvLong;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder;
		
		if (convertView == null) 
		{
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_friends, null);
			holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			holder.tvStatus = (TextView) convertView.findViewById(R.id.tvStatus);
			holder.tvLat = (TextView) convertView.findViewById(R.id.tvLat);
			holder.tvLong = (TextView) convertView.findViewById(R.id.tvLong);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvName.setText(friends.get(position).getNom());
		holder.tvStatus.setText(friends.get(position).getStatus());
		holder.tvLat.setText(String.valueOf(friends.get(position).getLatitude()));
		holder.tvLong.setText(String.valueOf(friends.get(position).getLongitude()));
		
		return convertView;
	}
}

