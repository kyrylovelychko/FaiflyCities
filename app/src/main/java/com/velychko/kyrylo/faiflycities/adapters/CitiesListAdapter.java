package com.velychko.kyrylo.faiflycities.adapters;

import android.database.Cursor;
import android.hardware.Camera;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription;

public class CitiesListAdapter
        extends RecyclerView.Adapter<CitiesListAdapter.ViewHolder> {

    private Cursor cursor;

    public CitiesListAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String cityName =
                cursor.getString(cursor.getColumnIndex(DatabaseDescription.Cities.COLUMN_CITY));
        holder.tvCity.setText(cityName);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCity;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.tv_city_name);
        }
    }
}
