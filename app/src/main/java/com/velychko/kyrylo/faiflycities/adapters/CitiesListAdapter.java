package com.velychko.kyrylo.faiflycities.adapters;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.faiflycities.R;
import com.velychko.kyrylo.faiflycities.data.database.DatabaseDescription;
import com.velychko.kyrylo.faiflycities.ui.activities.CityDetailsActivity;
import com.velychko.kyrylo.faiflycities.utils.Constants;

import static java.security.AccessController.getContext;

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvCity;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.tv_city_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), CityDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_CITY_NAME, tvCity.getText().toString());
            v.getContext().startActivity(intent);
        }
    }
}
