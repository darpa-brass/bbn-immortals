package com.bbn.ataklite.entities;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.bbn.ataklite.R;

import java.util.List;

/**
 * Created by awellman@bbn.com on 2/4/16.
 */
public class MonitoredEntityArrayAdapter extends ArrayAdapter<MonitoredEntity> {

    public MonitoredEntityArrayAdapter(Context context, List<MonitoredEntity> users) {
        super(context, -1, users);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO: Use the ViewHolder pattern....

        LayoutInflater infalter = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = infalter.inflate(R.layout.monitored_user_list_item, parent, false);
        TextView callsign = (TextView) row.findViewById(R.id.userName);
        TextView locationReports = (TextView) row.findViewById(R.id.locationReports);
        TextView imageReports = (TextView) row.findViewById(R.id.imageReports);

        MonitoredEntity user = getItem(position);

        Resources res = parent.getResources();
        callsign.setText(String.format(res.getString(R.string.main_user_list_callsign), user.getCallsign()));
        locationReports.setText(String.format(res.getString(R.string.main_user_list_location_report_count), user.getLocationTrackCount()));
        imageReports.setText(String.format(res.getString(R.string.main_user_list_image_reports), user.getPictureUploadCount()));

        return row;
    }
}
