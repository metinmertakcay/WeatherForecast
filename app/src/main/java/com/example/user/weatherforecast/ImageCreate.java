package com.example.user.weatherforecast;

import android.widget.FrameLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.content.Context;

//Bu sinif kisiye gosterilecek olan hava durumu bilgisi kisminda  hava durumu resminin yerlestirilmesi icin olusturulmustur.
public class ImageCreate extends FrameLayout {

    //Uzerine resim eklenecek olan viewGroup nesnesi
    private ImageView imageView;

    public ImageCreate(Context context) {
        super(context);
        initialize();
    }

    public ImageCreate(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    //İlklendirme islemleri gerceklestiriliyor.
    public void initialize()
    {
        imageView = new ImageView(getContext());
        LayoutParams layoutParams = new LayoutParams(-1,-1);
        layoutParams.setMargins(7,7,7,7);
        addView(imageView,layoutParams);
    }

    /*Alinan id degerine gore imageView'a icon,image yerlestirme islemi gerceklestiriliyor.
    @param resourceId : image, iconun sahip oldugu id degeri.*/
    public void setResource(int resourceId)
    {
        imageView.setImageResource(resourceId);
    }
}