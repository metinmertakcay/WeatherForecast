package com.example.user.weatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.user.weatherforecast.MainActivity.DISTRICT_NAME;
import static com.example.user.weatherforecast.MainActivity.FILE_NAME;
import static com.example.user.weatherforecast.MainActivity.PROVINCE_NAME;

public class Forecast extends Activity {

    private ArrayList<HashMap<String,String>> arrayList;
    private HashMap<String,String> map;
    private TextView textProvinceName;
    private GridLayout gridLayout;
    private Button backButton;
    private Intent intent;
    private Cards card;
    private LinearLayout linearLayout;
    private int height , width;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);

        //Secilmis olan sehir ve eger belirlenmis ise sehire ait olan ilce kullaciya textView'a atanarak yerklestiriliyor.
        intent = getIntent();
        initialize();
        if((intent.getStringExtra(DISTRICT_NAME)!=null)&&(intent.getStringExtra(DISTRICT_NAME)!= "")) {
            textProvinceName.setText(intent.getStringExtra(PROVINCE_NAME)+"-"+intent.getStringExtra(DISTRICT_NAME));
        }
        else
        {
            textProvinceName.setText(intent.getStringExtra(PROVINCE_NAME));
        }
        loadArrayList();
        backButtonHandler();
    }

    //Bir onceki sinifta elde edilmis ve dosyaya yazilmis olan veriler bu metod ile dosyadan tekrardan okunuyor.
    private void loadArrayList() {
        File file = new File(this.getFilesDir() , FILE_NAME);
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            //Veriler arrayListe okunma islemi gerceklestiriliyor.
            arrayList = (ArrayList<HashMap<String, String>>) in.readObject();
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //İlklendirme islemleri gerceklestiriliyor.
    public void initialize()
    {
        backButton = (Button)findViewById(R.id.backButton);
        textProvinceName = (TextView)findViewById(R.id.textProvinceName);
        gridLayout = (GridLayout)findViewById(R.id.gridLayout);
        linearLayout = (LinearLayout)findViewById(R.id.lLayout);
    }

    //Olusturulan ekranda gosterilecek olan degerlerin sahip olacagi genislik ve yukseklik degerleri belirlenmesi islemi gerceklestiriliyor.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
        {
            width = (linearLayout.getWidth() - 10) / 4;
            height = (linearLayout.getHeight() - 10) / 5;
            createWeatherTable();
        }
    }

    //İstenilen sehir ve eger secilmis ise sehire ait olan ilcenin 5 gunluk hava durumu bilgisinin gosterilecegi kisim hazirlaniyor.
    public void createWeatherTable()
    {
        for(int i=0 ; i<5 ; i++)
        {
            //ArrayListin icerisinde yer alan hashmap degerleri teker teker okunuyor.
            map = arrayList.get(i);
            for(int j=0 ; j<4 ; j++)
            {
                card = new Cards(this);
                //Her bir deger(gece ve gunduz degerleri, eklenecek olan resim bilgisi ve tarih bilgisi) hashmap ile elde ediliyor.
                if(j == 0)
                {
                    card.setTextView(map.get("Tarih"));
                    gridLayout.addView(card,width,height);
                }
                else if(j == 1)
                {
                    ImageCreate imageView = new ImageCreate(this);
                    int drawableResourceId = this.getResources().getIdentifier(map.get("Condition"),"drawable",this.getPackageName());
                    imageView.setResource(drawableResourceId);
                    gridLayout.addView(imageView,width,height);
                }
                else if(j == 2)
                {
                    card.setTextView(map.get("Gündüz"));
                    gridLayout.addView(card,width,height);
                }
                else
                {
                    card.setTextView(map.get("Gece"));
                    gridLayout.addView(card,width,height);
                }
            }
        }
    }

    //Geri butonuna basilmasi durumunda ana menuye yani sehir ve ilce secimi ekranina geri dondurulmesini saglayan metod.
    public void backButtonHandler()
    {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Forecast.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}