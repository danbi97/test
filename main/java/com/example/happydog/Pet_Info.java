package com.example.happydog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Pet_Info extends AppCompatActivity {
    ImageView load_pet_img;
    TextView load_pet_name, load_pet_type, load_pet_size, load_pet_weight, load_pet_unique, btn_pet_modify;
    Intent intent;
    String dog_name, breed, size, weight, etc, pet_image;
    String userNickname, dog_image, getServerImg;
    Retrofit retrofit;
    int GET_GALLERY_IMAGE = 200;
    int dogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info);

        load_pet_img = findViewById(R.id.load_pet_img);
        load_pet_name = findViewById(R.id.load_pet_name);
        load_pet_type = findViewById(R.id.load_pet_type);
        load_pet_size = findViewById(R.id.load_pet_size);
        load_pet_weight = findViewById(R.id.load_pet_weight);
        load_pet_unique = findViewById(R.id.load_pet_unique);
        btn_pet_modify = findViewById(R.id.btn_pet_modify);

        intent = getIntent();
        userNickname = intent.getStringExtra("userNickname");
        dog_name = intent.getStringExtra("dog_name");
        breed = intent.getStringExtra("breed");
        size = intent.getStringExtra("size");
        weight = intent.getStringExtra("weight");
        etc = intent.getStringExtra("etc");

        RetrofitClient();

        Service service = retrofit.create(Service.class);
        Call<userDog> call = service.DogDetail(userNickname);

        call.enqueue(new Callback<userDog>() {
            @Override
            public void onResponse(Call<userDog> call, Response<userDog> response) {
                dog_image = response.body().getDog_image().replace("localhost", "192.168.0.46");

                load_pet_name.setText(response.body().getDog_name());
                load_pet_type.setText(response.body().getBreed());
                load_pet_size.setText(response.body().getSize());
                load_pet_weight.setText(response.body().getWeight());
                load_pet_unique.setText(response.body().getEtc());

                getServerImg = response.body().getDog_image();

                Glide.with(Pet_Info.this)
                        .load(dog_image)
                        .into(load_pet_img);

                Log.d("", "사진 경로 확인 :" + dog_image);;

                dogId = response.body().getId();
            }

            @Override
            public void onFailure(Call<userDog> call, Throwable t) {

            }
        });

        btn_pet_modify.setOnClickListener(new View.OnClickListener() { // 정보 수정페이지로 넘어감
            @Override
            public void onClick(View v) {
                intent = new Intent(Pet_Info.this, ModifyDogInfo.class);
                intent.putExtra("dog_id", dogId);

                intent.putExtra("dog_name", load_pet_name.getText().toString());
                intent.putExtra("breed", load_pet_type.getText().toString());
                intent.putExtra("size", load_pet_size.getText().toString());
                intent.putExtra("weight", load_pet_weight.getText().toString());
                intent.putExtra("etc", load_pet_unique.getText().toString());
                intent.putExtra("pet_image", dog_image);
                intent.putExtra("getServerImg", getServerImg);
                intent.putExtra("userNickname", userNickname);

                startActivity(intent);
            }
        });

    }

    public void RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.46:8080/") // 가져올 데이터가 담긴 서버 주소
                .addConverterFactory(GsonConverterFactory.create()) // 통신 완료 후 어떤 컨버터로 데이터를 파싱할것인지
                .build();
    }
}