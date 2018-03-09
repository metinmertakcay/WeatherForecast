package com.example.user.weatherforecast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

//Kullaniciya gosterilecek olan hava durumu bilgilerini gostermek icin olusturulmus siniftir.
public class Cards extends FrameLayout {

    //Uzerine yazi yazilacak(gerekli bilgiler yerlestirilecek) olan textView nesnesi
    private TextView textView;

    public Cards(Context context) {
        super(context);
        init();
    }

    public Cards(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //İlklendirme islemleri gerceklestiriliyor.
    public void init()
    {
        textView = new TextView(getContext());
        textView.setTextSize(11);
        textView.setBackgroundColor(getResources().getColor(R.color.White));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.Black));
        LayoutParams layoutParams = new LayoutParams(-1,-1);
        layoutParams.setMargins(7,7,7,7);
        addView(textView,layoutParams);
    }

    /*Bu metod gonderilen string degerinin ilklendirme islemi yapilmis olan textView nesnesine yerlestirilme islemini yapar.
    @param data : Gunduz gece dereceleri veya tarih bilgisi */
    public void setTextView(String data)
    {
        textView.setText(data);
    }
}
