package com.vickikbt.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    private static final int PICTURE__RESULT = 42;

    private Button select;
    private EditText txtTitle, txtPrice, txtDescription;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    ImageView imageView;
    Button btnImage;

    TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        txtTitle =(EditText)findViewById(R.id.et_Title);
        txtPrice = (EditText) findViewById(R.id.ediText_price);
        txtDescription = (EditText) findViewById(R.id.ediText_description);
        imageView=(ImageView)findViewById(R.id.image);


        btnImage=(Button)findViewById(R.id.btn_Image);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent select=new Intent(Intent.ACTION_GET_CONTENT);
                select.setType("image/jpeg");
                select.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(select.createChooser(select, "Insert Picture"), PICTURE__RESULT);
            }
        });


        //FirebaseUtility.openFBReference("travelpacks", this);
        firebaseDatabase = FirebaseUtility.firebaseDatabase;
        databaseReference = FirebaseUtility.databaseReference;

        Intent intent=getIntent();
        TravelDeal deal=(TravelDeal)intent.getSerializableExtra("Deal");
        if (deal==null){
            deal=new TravelDeal();
        }
        this.deal =deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);

        if (FirebaseUtility.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
            findViewById(R.id.btn_Image).setEnabled(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
            findViewById(R.id.btn_Image).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Travel deal saved", Toast.LENGTH_SHORT).show();

                return true;

                case R.id.delete_menu:
                    deleteDeal();
                    Toast.makeText(getApplicationContext(), "Deal deleted", Toast.LENGTH_SHORT).show();
                    backToList();
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICTURE__RESULT && resultCode==RESULT_OK){
            Uri imageUri = data.getData();
            final StorageReference ref=FirebaseUtility.storageReference.child(imageUri.getLastPathSegment());
            imageView.setImageURI(imageUri);


            final Task uploadTask = ref.putFile(imageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String message = e.toString();
                    Toast.makeText(DealActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String pictureName=taskSnapshot.getStorage().getPath();
                    deal.setImageName(pictureName);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadImageUrl = task.getResult().toString();
                                deal.setImageUrl(downloadImageUrl);

                                Toast.makeText(DealActivity.this, "got the Product image Url Successfully..." + downloadImageUrl, Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
            });
        }
    }

    private void saveDeal(){
        deal.setTitle( txtTitle.getText().toString());
        deal.setPrice( txtPrice.getText().toString());
        deal.setDescription( txtDescription.getText().toString());

        if(deal.getId()==null){
            databaseReference.push().setValue(deal);
        }
        else {
            databaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal(){
        if (deal==null){
            Toast.makeText(getApplicationContext(), "Save deal before deleting" , Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.child(deal.getId()).removeValue();

        if (deal.getImageName()!=null && deal.getImageName().isEmpty()==false){
            StorageReference picRef=FirebaseUtility.firebaseStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Succefully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }

    }

    private void backToList(){
        Intent intent=new Intent(this, Listactivity.class);
        startActivity(intent);
    }

    private void enableEditText(boolean isEnable){
        txtTitle.setEnabled(isEnable);
        txtDescription.setEnabled(isEnable);
        txtPrice.setEnabled(isEnable);
    }

    private void showImage(String url){
        if (url!=null && url.isEmpty()==false){
            int width= Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

}
