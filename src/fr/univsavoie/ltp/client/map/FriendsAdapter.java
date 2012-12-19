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

