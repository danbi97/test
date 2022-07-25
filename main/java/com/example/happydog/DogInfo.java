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

public class DogInfo extends AppCompatActivity {
    ImageView my_pet_img;
    TextView dog_img_upload;
    EditText dog_info_name, dog_info_type, dog_info_size, dog_info_weight, dog_info_unique;
    Button btn_doginfo_upload;
    Retrofit retrofit;
    Intent intent;
    String userNickname, dog_image, pet_image, bitmapEncode, realpath;
    String dog_name, breed, size, weight, etc;
    Bitmap bitmap;
    Uri uri;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_info);

        my_pet_img = findViewById(R.id.my_pet_img);
        dog_img_upload = findViewById(R.id.dog_img_upload);
        dog_info_name = findViewById(R.id.dog_info_name);
        dog_info_type = findViewById(R.id.dog_info_type);
        dog_info_size = findViewById(R.id.dog_info_size);
        dog_info_weight = findViewById(R.id.dog_info_weight);
        dog_info_unique = findViewById(R.id.dog_info_unique);
        btn_doginfo_upload = findViewById(R.id.btn_doginfo_upload);

        RetrofitClient();

        intent = getIntent();
        userNickname = intent.getStringExtra("userNickname");

        Glide.with(this)
                .load(pet_image)
                .into(my_pet_img);

        dog_image = pet_image;

        dog_img_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);

                activityResultLauncher.launch(intent);

            }
        });

        btn_doginfo_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dog_name = dog_info_name.getText().toString();
                breed = dog_info_type.getText().toString();
                size = dog_info_size.getText().toString();
                weight = dog_info_weight.getText().toString();
                etc = dog_info_unique.getText().toString();

                Service service = retrofit.create(Service.class);
                Call<ResponseCode2> call = service.DogJoin(dog_name, breed, size, weight, etc, dog_image, userNickname);

                call.enqueue(new Callback<ResponseCode2>() {
                    @Override
                    public void onResponse(Call<ResponseCode2> call, Response<ResponseCode2> response) {
                        Log.d("", "확인 :: " + response.body().getResponseCode2());

                        Toast.makeText(DogInfo.this, "강아지 정보가 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                        intent = new Intent(DogInfo.this, Pet_Info.class);

                        intent.putExtra("userNickname", userNickname);
                        intent.putExtra("dog_name", dog_name);
                        intent.putExtra("breed", breed);
                        intent.putExtra("size", size);
                        intent.putExtra("weight", weight);
                        intent.putExtra("etc", etc);
                        intent.putExtra("dog_image", dog_image); // 인코딩한 이미지 소스
                        intent.putExtra("pet_image", pet_image);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<ResponseCode2> call, Throwable t) {

                    }
                });

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