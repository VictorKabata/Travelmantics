package com.vickikbt.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder>  {

    ArrayList<TravelDeal> deals;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    private ImageView imageDeal;

    public DealAdapter(){
        //FirebaseUtility.openFBReference("travelpacks" , this);
        firebaseDatabase = FirebaseUtility.firebaseDatabase;
        databaseReference = FirebaseUtility.databaseReference;

        deals=FirebaseUtility.travelPackages;

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TravelDeal tp = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal: ", tp.getTitle());
                tp.setId(dataSnapshot.getKey());
                deals.add(tp);

                notifyItemInserted(deals.size()-1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReference.addChildEventListener(childEventListener);

    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        View itemView= LayoutInflater.from(context)
                .inflate(R.layout.rv_rows, parent, false);

        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {

        TravelDeal travelPackage=deals.get(position);
        holder.bind(travelPackage);

    }

    @Override
    public int getItemCount() {
        return deals.size();
    }


    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvTitle, tvDescription, tvPrice;

        public DealViewHolder(View itemView) {
            super(itemView);

            tvTitle=(TextView)itemView.findViewById(R.id.tv_Title);
            tvDescription=(TextView)itemView.findViewById(R.id.tv_Description);
            tvPrice=(TextView)itemView.findViewById(R.id.tv_Price);
            imageDeal=(ImageView)itemView.findViewById(R.id.imageDeal);

            itemView.setOnClickListener(this);

        }

        public void bind(TravelDeal deal){

            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());

        }

        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            Log.d("Click", String.valueOf(position));
            TravelDeal selectedDeal=deals.get(position);
            Intent intent=new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }
    }

    private void showImage(String url){
        if (url!=null && url.isEmpty()==false){
            Picasso.with(imageDeal.getContext())
                    .load(url)
                    .resize(160, 160)
                    .centerCrop()
                    .into(imageDeal);
        }
    }
}
