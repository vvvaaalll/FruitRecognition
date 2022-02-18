package org.tensorflow.lite.examples.classification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterFactory;
import org.tensorflow.lite.InterpreterApi;

import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


public class ImageUpload extends AppCompatActivity {

    ImageView mImage;
    Button mSubmit;

    String imgName;

    FirebaseFirestore fStore;
    StorageReference fStorage;
    String userID;
    FirebaseAuth fAuth;
    int SELECT_PHOTO = 1;
    Uri imgUri;
    Bitmap bitmap;
    TensorBuffer probabilityBuffer;

    InterpreterApi tflite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        mImage   = (ImageView) findViewById(R.id.imageView);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fStorage = FirebaseStorage.getInstance().getReference();

        userID = fAuth.getCurrentUser().getUid();

        mSubmit = findViewById(R.id.addFruitBtn);





        mSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                UploadFile();

                Map<String,Float> results = ProcessImage();

                LinkedHashMap<String, Float> sortedMap = new LinkedHashMap<>();

                results.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

                Set<String> keySet = sortedMap.keySet();

                String[] fruitArray
                        = keySet.toArray(new String[keySet.size()]);



                DocumentReference documentReference = fStore.collection("users").document(userID).collection("fruits").document();
                if (results != null) {

                    HashMap<String, Object> fruit = new HashMap<>();

                    fruit.put("fruit1", fruitArray[6 - 1].toString() );
                    fruit.put("fruit2", fruitArray[5 - 1].toString());
                    fruit.put("fruit3", fruitArray[4 - 1].toString());
                    fruit.put("percentage1", sortedMap.get(fruitArray[6 - 1])*100);
                    fruit.put("percentage2", sortedMap.get(fruitArray[5 - 1])*100);
                    fruit.put("percentage3", sortedMap.get(fruitArray[4 - 1])*100);
                    fruit.put("imgName", imgName);

                    /*
                    * fruit.put("fruit1", fruitArray[6 - 1] );
                    fruit.put("fruit2", fruitArray[5 - 1]);
                    fruit.put("fruit3", fruitArray[4 - 1]);
                    fruit.put("percentage1", sortedMap.get(fruitArray[6 - 1])*100);
                    fruit.put("percentage2", sortedMap.get(fruitArray[5 - 1])*100);
                    fruit.put("percentage3", sortedMap.get(fruitArray[4 - 1])*100);
                    fruit.put("imgName", imgName);
                    * */


                    documentReference.set(fruit).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "onSuccess: fruit added");
                            startActivity(new Intent(getApplicationContext(), FruitsActivity.class));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "onFailure: " + e.toString());

                        }
                    });


                }
            }

        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,SELECT_PHOTO);
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data) {
        super.onActivityResult(requestCode, requestCode, data);
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                mImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadFile() {
        if (imgUri != null) {
            imgName = (System.currentTimeMillis() + "." + getFileExtension(imgUri));
            StorageReference fileReference = fStorage.child("fruits").child(imgName);
            final UploadTask uploadTask = fileReference.putFile(imgUri);


        }
    }

    private Map<String,Float> ProcessImage(){



        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                        .build();
        TensorImage tensorImage = new TensorImage(DataType.UINT8);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tensorImage.load(bitmap);
        tensorImage = imageProcessor.process(tensorImage);

        probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, 6}, DataType.UINT8);

        try{
            MappedByteBuffer tfliteModel
                    = FileUtil.loadMappedFile(ImageUpload.this,
                    "model.tflite");
            tflite = new InterpreterFactory().create(
                    tfliteModel, new InterpreterApi.Options());
        } catch (IOException e){
            Log.e("tfliteSupport", "Error reading model", e);
        }

// Running inference
        if(null != tflite) {
            tflite.run(tensorImage.getBuffer(), probabilityBuffer.getBuffer());
        }

        final String ASSOCIATED_AXIS_LABELS = "labels.txt";
        List<String> associatedAxisLabels = null;

        try {
            associatedAxisLabels = FileUtil.loadLabels(this, ASSOCIATED_AXIS_LABELS);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

        TensorProcessor probabilityProcessor =
                new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

        probabilityBuffer.getFloatArray();

        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            TensorLabel labels = new TensorLabel(associatedAxisLabels,
                    probabilityProcessor.process(probabilityBuffer));

            // Create a map to access the result based on label
            Map<String, Float> floatMap = labels.getMapWithFloatValue();

            return floatMap;
        }

        return null;
    }
}

