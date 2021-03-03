package com.example.countries;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.MyViewHolder> {
    List<Country> arrayList;
    Activity activity;

    public CountryAdapter(List<Country> arrayList, Activity activity) {
        this.arrayList = arrayList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Country country = arrayList.get(position);
        holder.name.setText(country.getName());
        holder.capital.setText(country.getCapital());
        holder.region.setText(country.getRegion());
        holder.subregion.setText(country.getSubregion());
        holder.population.setText(country.getPopulation());
        holder.borders.setText(country.getBorders());
        holder.languages.setText(country.getLanguages());

        GlideToVectorYou.init().with(activity).load(Uri.parse(country.getFlag()), holder.flag);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView capital;
        public ImageView flag;
        public TextView region;
        public TextView subregion;
        public TextView population;
        public TextView borders;
        public TextView languages;

        public MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            capital = v.findViewById(R.id.capital);
            flag = v.findViewById(R.id.flag);
            region = v.findViewById(R.id.region);
            subregion = v.findViewById(R.id.subregion);
            population = v.findViewById(R.id.population);
            borders = v.findViewById(R.id.borders);
            languages = v.findViewById(R.id.languages);
        }
    }
}
