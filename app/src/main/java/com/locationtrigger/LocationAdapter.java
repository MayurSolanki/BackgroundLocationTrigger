package com.locationtrigger;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by nikunj on 21/11/17.
 */

class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.MyViewHolder> {

    private List<LocationModel> locationList;
    Context context;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mLatitudeTextView;
        public TextView mLongitudeTextView;
        public TextView mLastUpdateTimeTextView;


        public MyViewHolder(View view) {
            super(view);
            mLatitudeTextView = (TextView) view.findViewById(R.id.latitude_text);
            mLongitudeTextView = (TextView) view.findViewById(R.id.longitude_text);
            mLastUpdateTimeTextView = (TextView) view.findViewById(R.id.last_update_time_text);

        }
    }


    public LocationAdapter(List<LocationModel> categoryList, Context context) {
        this.locationList = categoryList;
        this.context = context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_location, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        LocationModel locationModel = locationList.get(position);


        holder.mLatitudeTextView.setText("Lat : " +locationModel.getLocation().getLatitude());
        holder.mLongitudeTextView.setText("Lon : " +locationModel.getLocation().getLongitude());
        holder.mLongitudeTextView.setText("Time : " +locationModel.getmLocationUpdateTime());






    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }


    public void updateItems(List<LocationModel> newItems) {
        // locationList.clear();
        locationList.addAll(newItems);
       // notifyDataSetChanged();
    }
}