package de.hochschule_bochum.blootothcontroller;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**6
 * Created by nikla on 08.07.2017.
 */

public class DeviceAdapter implements ListAdapter {

    private ArrayList<BTDevice> devices;
    private Context context;

    public DeviceAdapter(Context context, ArrayList<BTDevice> devices) {
        this.devices = devices;
        this.context = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return !((BTDevice) getItem(position)).isSelected();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.row, null);
        }

        BTDevice d = (BTDevice) getItem(position);
        ((TextView) convertView.findViewById(R.id.toptext)).setText(d.getDeviceName());
        ((TextView) convertView.findViewById(R.id.bottomtext)).setText(d.getMac());

        if (d.isSelected()) {
            convertView.findViewById(R.id.icon).setBackgroundColor(context.getResources().getColor(R.color.colorOn));
        } else {
            convertView.findViewById(R.id.icon).setBackgroundColor(context.getResources().getColor(R.color.colorOff));
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return devices.isEmpty();
    }
}
