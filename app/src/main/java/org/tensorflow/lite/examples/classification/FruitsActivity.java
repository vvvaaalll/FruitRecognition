package org.tensorflow.lite.examples.classification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class FruitsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseAuth fAuth;
    FruitAdapter myAdapter;
    ArrayList<Fruit> fruitsArrayList;
    String userID;
    FirebaseFirestore fStore;
    StorageReference fStorage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruits);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.fruitListRW);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fStore = FirebaseFirestore.getInstance();
        fAuth     = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid().toString();

        fruitsArrayList = new ArrayList<Fruit>();
        myAdapter = new FruitAdapter(FruitsActivity.this, fruitsArrayList);
        recyclerView.setAdapter(myAdapter);

        EventChangeListener();


    }

    private void EventChangeListener()
    {

        fStore.collection("users").document(userID).collection("fruits")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error != null){
                            Log.e("Firestore error",error.getMessage());
                        }

                        for(DocumentChange fDatabase : value.getDocumentChanges()){

                            if(fDatabase.getType() == DocumentChange.Type.ADDED){
                                Fruit fruit =  fDatabase.getDocument().toObject(Fruit.class);

                                fruit.setFruitID(fDatabase.getDocument().getId());
                                fruitsArrayList.add(fruit);

                            }
                            myAdapter.notifyDataSetChanged();

                        }
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.images_menu, menu);
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutMenu:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
                return true;

            case R.id.deleteAccount:

                fruitsArrayList.forEach((fruit) ->{
                    try {
                        if(fruit.getImgName().isEmpty()) {
                            throw new Exception();
                        }else{
                            FirebaseStorage.getInstance().getReference()
                                    .child("fruits")
                                    .child(fruit.getImgName()).delete();
                        }
                    }catch (Exception e) {

                        Log.e("",e.getMessage());
                    }

                });
                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getUid().toString()).delete();
                FirebaseAuth.getInstance().getCurrentUser().delete();

                Toast.makeText(FruitsActivity.this, "Successfully deleted your account" , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));

                finish();
                return true;

            case R.id.liveCam:
                startActivity(new Intent(getApplicationContext(),ClassifierActivity.class));
                finish();
                return true;

            case R.id.images:
                startActivity(new Intent(getApplicationContext(),FruitsActivity.class));
                finish();
                return true;

            case R.id.addMenu:

                startActivity(new Intent(getApplicationContext(), ImageUpload.class));


                finish();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }


}