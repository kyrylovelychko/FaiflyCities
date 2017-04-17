package com.velychko.kyrylo.faiflycities.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.velychko.kyrylo.faiflycities.R;

public class CitiesListAdapter
        extends RecyclerView.Adapter<CitiesListAdapter.ViewHolder> {

//    private static String countConstantWord;

    private Cursor cursor;


    public CitiesListAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_city, parent, false);
//        countConstantWord =
//                parent.getResources().getString(R.string.dialog_constant_string_count_of_cities);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);


//        String countryName =
//                cursor.getString(cursor.getColumnIndex(DatabaseDescription.Cities.COLUMN_COUNTRY));
//        int countOfCities =
//                cursor.getInt(cursor.getColumnIndex(Constants.SQL_ALIAS_COUNT_OF_CITIES));
//
//        holder.tvCountry.setText(countryName);
//        holder.tvCountOfCities.setText(String.format(countConstantWord, countOfCities));
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCountry;
        private TextView tvCountOfCities;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
