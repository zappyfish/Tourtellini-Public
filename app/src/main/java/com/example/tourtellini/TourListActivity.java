package com.example.tourtellini;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tourtellini.tours.Tour;
import com.example.tourtellini.tours.TourPreview;

import java.util.ArrayList;
import java.util.List;

public class TourListActivity extends Activity {
    private RecyclerView.Adapter toursAdapter;
    private RecyclerView toursRecyclerView;
    private RecyclerView.LayoutManager toursLayoutManager;
    private ArrayList<TourPreview> toursDataSet = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_list_layout);
        toursLayoutManager = new LinearLayoutManager(this);
        toursRecyclerView = findViewById(R.id.toursList);
        toursRecyclerView.setLayoutManager(toursLayoutManager);
        toursRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        toursAdapter = new TourListAdapter(toursDataSet);
        toursRecyclerView.setAdapter(toursAdapter);


        TourPreview.getAvailableTours(getApplicationContext(), new TourPreview.ToursAvailableCallback() {
            @Override
            public void onAvailable(List<TourPreview> tourPreviews) {
                toursDataSet.clear();
                toursDataSet.addAll(tourPreviews);
                toursAdapter.notifyDataSetChanged();
            }
        });
    }

    public class TourListAdapter extends RecyclerView.Adapter<TourListAdapter.ViewHolder> {
        private List<TourPreview> tourPreviewList;

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tourName;
            public TextView tourDescription;
            public View layout;

            public ViewHolder(View v){
                super(v);
                layout=v;
                tourName = (TextView) v.findViewById(R.id.tourName);
                tourDescription = (TextView) v.findViewById(R.id.tourDescription);
            }

        }
        public TourListAdapter(List<TourPreview> tourPreviewList){
            this.tourPreviewList = tourPreviewList;
        }

        public TourListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.list_item_layout, parent, false);
            TourListAdapter.ViewHolder vh = new TourListAdapter.ViewHolder(v);
            return vh;
        }
        public void onBindViewHolder(TourListAdapter.ViewHolder holder, final int position){
            String title = tourPreviewList.get(position).name();
            String description = tourPreviewList.get(position).description();
            final int id= tourPreviewList.get(position).id();

            holder.tourDescription.setText(description);
            holder.tourName.setText(title);
            if(position %2 == 1)
            {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Intent googleMapIntent = new Intent(getApplicationContext(), TourActivity.class);
                    googleMapIntent.putExtra("id",  id);
                    startActivity(googleMapIntent);
                }
            });
        }
        public int getItemCount(){
            return tourPreviewList.size();
        }
    }
}
