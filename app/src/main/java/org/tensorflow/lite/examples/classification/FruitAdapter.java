package org.tensorflow.lite.examples.classification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.MyViewHolder> {

    Context context;
    ArrayList<Fruit> list;

    public FruitAdapter(Context context, ArrayList<Fruit> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.fruit_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Fruit fruit = list.get(position);

        holder.fruit1.setText(fruit.getFruit1() + ": ");
        holder.fruit2.setText(fruit.getFruit2() + ": ");
        holder.fruit3.setText(fruit.getFruit3() + ": ");

        holder.percentage1.setText(fruit.getPercentage1() + "%");
        holder.percentage2.setText(fruit.getPercentage2() + "%");
        holder.percentage3.setText(fruit.getPercentage3() + "%");
        holder.fruit = fruit;

        if(!TextUtils.isEmpty(fruit.getImgName())) {

            FirebaseStorage fStorage = FirebaseStorage.getInstance();
            StorageReference refImage = fStorage.getReference()
                    .child("fruits")
                    .child(fruit.getImgName());

            refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(holder.imageView);

                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }





    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView fruit1, fruit2, fruit3;
        TextView percentage1, percentage2, percentage3;

        ImageView imageView;

        Fruit fruit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            fruit1 = itemView.findViewById(R.id.fruit1);
            fruit2 = itemView.findViewById(R.id.fruit2);
            fruit3 = itemView.findViewById(R.id.fruit3);

            percentage1 = itemView.findViewById(R.id.percent1);
            percentage2 = itemView.findViewById(R.id.percent2);
            percentage3 = itemView.findViewById(R.id.percent3);
            imageView = itemView.findViewById(R.id.fruitImage);

            itemView.findViewById(R.id.RW_deletebtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder.setTitle("Do you want to delete this tarantula?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //DELETE FROM FIRESTORE AND FIRESTORAGE
                            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
                            try {
                                if (fruit.getImgName().isEmpty()) {
                                    throw new Exception();
                                }
                                else{
                                    FirebaseStorage.getInstance().getReference()
                                            .child("fruits")
                                            .child(fruit.getImgName()).delete();


                                }
                            }catch (Exception e){
                                Log.e("",e.getMessage());

                            }

                            FirebaseFirestore.getInstance().collection("users").document(userID)
                                    .collection("fruits").document(fruit.getFruitID()).delete();


                            itemView.getContext().startActivity(new Intent(itemView.getContext(), FruitsActivity.class));

                            Toast.makeText(itemView.getContext(), "Successfully deleted fruit" , Toast.LENGTH_SHORT).show();

                        }
                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    AlertDialog ad = builder.create();
                    ad.show();
                }
            });

        }
    }
}
