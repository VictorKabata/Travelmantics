package com.vickikbt.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Listactivity extends AppCompatActivity {

    private  TextView tvDeals;

    ArrayList<TravelDeal> deals;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_useractivity);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        MenuItem insertMenu=menu.findItem(R.id.add_deal);
        if (FirebaseUtility.isAdmin==true){
            insertMenu.setVisible(true);
        }
        else{
            insertMenu.setVisible(false);
        }

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.add_deal:
                Intent intent=new Intent(this, DealActivity.class);
                startActivity(intent);

            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                            }
                        });
                FirebaseUtility.detachListener();
                Log.d("Logout", "User logged out");
                FirebaseUtility.attachListener();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtility.detachListener();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        FirebaseUtility.openFBReference("traveldeals", this);
        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.rv_Packs);
        final DealAdapter packAdapter=new DealAdapter();
        recyclerView.setAdapter(packAdapter);
        LinearLayoutManager packsLayoutManager=new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(packsLayoutManager);
        FirebaseUtility.attachListener();
    }
}
