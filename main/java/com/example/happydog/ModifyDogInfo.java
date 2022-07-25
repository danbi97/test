package com.example.happydog;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModifyDogInfo extends AppCompatActivity {

    EditText modify_pname, modify_breed, modify_size, modify_weight, modify_etc;
    Button btn_doginfo_modify;
    TextView dog_img_modify;
    ImageView my_pet_img;
    String userNickname;
    Intent intent;
    Retrofit retrofit;
    int dogId;
    String dog_name, breed, size, weight, etc, pet_image, dog_image, bitmapEncode, realpath, ServerImg, getServerImg;
    Bitmap bitmap;
    Uri uri;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_dog_info);

        modify_pname = findViewById(R.id.modify_pname);
        modify_breed = findViewById(R.id.modify_breed);
        modify_size = findViewById(R.id.modify_size);
        modify_weight = findViewById(R.id.modify_weight);
        modify_etc = findViewById(R.id.modify_etc);
        dog_img_modify = findViewById(R.id.dog_img_modify);
        btn_doginfo_modify = findViewById(R.id.btn_doginfo_modify);
        my_pet_img = findViewById(R.id.my_pet_img);

        intent = getIntent();

        userNickname = intent.getStringExtra("userNickname");
        dog_name = intent.getStringExtra("dog_name");
        breed = intent.getStringExtra("breed");
        size = intent.getStringExtra("size");
        weight = intent.getStringExtra("weight");
        etc = intent.getStringExtra("etc");

        pet_image = intent.getStringExtra("pet_image");
        getServerImg = intent.getStringExtra("getServerImg");

        modify_pname.setText(dog_name);
        modify_breed.setText(breed);
        modify_size.setText(size);
        modify_weight.setText(weight);
        modify_etc.setText(etc);

        dogId = intent.getIntExtra("dog_id", 0);

        Log.d("", "확인 :: " + dogId);

        RetrofitClient();

        Glide.with(this)
                .load(pet_image)
                .into(my_pet_img);

        dog_image = getServerImg;

        btn_doginfo_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Service service = retrofit.create(Service.class);
                Call<ResponseCode2> call = service.DogModify(dogId, modify_pname.getText().toString(), modify_breed.getText().toString(),
                        modify_size.getText().toString(), modify_weight.getText().toString(), modify_etc.getText().toString(), dog_image, userNickname);

                call.enqueue(new Callback<ResponseCode2>() {
                    @Override
                    public void onResponse(Call<ResponseCode2> call, Response<ResponseCode2> response) {
                        Log.d("", "수정 확인 ::  " + response.body().getResponseCode2());

                        Glide.with(ModifyDogInfo.this)
                                .load(dog_image)
                                .into(my_pet_img);

                        Toast.makeText(ModifyDogInfo.this, "강아지 정보가 수정 되었습니다.", Toast.LENGTH_SHORT).show();
                        intent = new Intent(ModifyDogInfo.this, Pet_Info.class);
                        intent.putExtra("userNickname", userNickname);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<ResponseCode2> call, Throwable t) {

                    }
                });

            }
        });

        dog_img_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);

                activityResultLauncher.launch(intent);
            }
        });

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        uri = result.getData().getData();

                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            realpath = getRealPathFromURI(uri);
                            file = new File(realpath);

                            // 비트맵 인코딩
                            ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytearray);

                            byte[] bytes = bytearray.toByteArray();
                            bitmapEncode = Base64.encodeToString(bytes, Base64.NO_WRAP);

                            Log.d("", "이미지 주소 bitmapencode :: " + bitmapEncode);

                            my_pet_img.setImageBitmap(bitmap);

                            int num = realpath.lastIndexOf("/");
                            String pathReplace = realpath.substring(num).replace("/", ",");
                            dog_image = bitmapEncode + pathReplace;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
    );

    // 이미지 파일의 실제 경로 구하는 메서드
    private String getRealPathFromURI(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.46:8080/") // 가져올 데이터가 담긴 서버 주소
                .addConverterFactory(GsonConverterFactory.create()) // 통신 완료 후 어떤 컨버터로 데이터를 파싱할것인지
                .build();
    }
}