package com.example.user.weatherforecast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String FILE_NAME = "file_name.txt";
    public static final String PROVINCE_NAME = "NAME", DISTRICT_NAME = "District_name";
    private static final String ERROR = "ERROR";
    private String URL = "http://www.havadurumux.net/";
    private String concatSpecialURL, selectedProvince, province;
    private Spinner spinnerProvince, spinnerIlce;
    private Button buttonGoster, buttonKonum;
    private ArrayList<HashMap<String, String>> list;
    private WifiReceiver wifiReceiver;
    private MobileReceiver mobileReceiver;
    private boolean wifi, mobile;
    private TextView warn;
    private String district, selectedDistrict;
    private int resourceId;
    private List<String> listDistrict;
    private String[] districtName;
    private TextView textV;
    private int permission_All = 1;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 2000, FASTEST_INTERVAL = 2000;
    private GoogleApiClient gac;
    private double latitude, longitude;
    private Location location;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private List<Address> adressses;
    private String city_name;
    private File file;
    private LocationManager manager;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permission();
        initialize();
        spinnerHandler();
        spinnerIlceHandler();
        buttonGosterHandler();
        buttonKonumHandler();

        //Wifi olarak internetin bagli olup olmadigi belirleniyor. Eger wifi de degisiklik olurse degisiklige gore islem yapiliyor.
        wifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, filter);

        //Mobilde internetin acik olup olmadigi kontrol ediliyor. Mobildeki internet degisikligi olmasi durumunda bu degisiklik yakalaniyor.
        mobileReceiver = new MobileReceiver();
        registerReceiver(mobileReceiver, filter);

        //Gps'in degismesi gozlemlenerek konum bulma butonu kullaniciya gosterilip gosterilmeyecegi belirleniyor.
        registerReceiver(gpsLocationReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gpsLocationReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gpsLocationReceiver != null)
            unregisterReceiver(gpsLocationReceiver);
    }

    /*Gps durumunun enable olup olmadigi kontrolu gerceklestiriliyor. Gps acik olmasina gore kullaniciya bulundugu lokasyon durumna
    gore hava durumu bilgisini goruntuleme imkani veriyor.*/
    public BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buttonKonum.setVisibility(View.VISIBLE);
                } else {
                    buttonKonum.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    //Application icin gerekli olan izinler kullanicidan isteniyor.
    public void permission() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.MEDIA_CONTENT_CONTROL, Manifest.permission.CAMERA};
        if (!(hasPermission(this, permissions))) {
            ActivityCompat.requestPermissions(this, permissions, permission_All);
        }
    }

    public static boolean hasPermission(Context context, String... permissions) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (context != null) && (permissions != null)) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Gps acik olmasi durumunda bulunulan lokasyon bilgisindeki sehire gore bilgiler internet sitesi uzerinden elde ediliyor.
    public void buttonKonumHandler() {
        buttonKonum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bulunulan sehir bilgisi ve erisilecek olan sitenin url bilgisi tekrardan duzenleniyor.
                province = city_name;
                selectedProvince = city_name;
                selectedProvince = convertString(selectedProvince);
                createNewURL();
                new Data(MainActivity.this).execute();
            }
        });
    }

    //Wifi'ın acik olup olmadiginin kontrolu gerceklestiriliyor.
    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //wi-fi baglantisinin olup olmadigi kontrol ediliyor.
            if (wifiCheck.isConnected()) {
                wifi = true;
            } else {
                wifi = false;
            }
            checkButton();
        }
    }

    //Mobil olarak internete bagli olunup olunmadigi kontrol ediliyor.
    public class MobileReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent ıntent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobileCheck = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            //mobilde internet baglantisinin olup olmadigi kontrol ediliyor.
            if (mobileCheck.isConnected()) {
                mobile = true;
            } else {
                mobile = false;
            }
            checkButton();
        }
    }

    //İnternete baglanti saglanmis ise kullaniciya locasyon uzerinden bulundugu bolgedeki hava durumu bilgisine erisme imkani verilir.
    public void checkButton() {
        if (mobile || wifi) {
            buttonGoster.setVisibility(View.VISIBLE);
            warn.setVisibility(View.INVISIBLE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                buttonKonum.setVisibility(View.INVISIBLE);
            else
                buttonKonum.setVisibility(View.VISIBLE);
        } else {
            buttonGoster.setVisibility(View.INVISIBLE);
            buttonKonum.setVisibility(View.INVISIBLE);
            warn.setVisibility(View.VISIBLE);
        }
    }

    public void initialize() {
        spinnerProvince = (Spinner) findViewById(R.id.spinner);
        spinnerIlce = (Spinner) findViewById(R.id.spinnerIlce);
        buttonGoster = (Button) findViewById(R.id.buttonGoster);
        ArrayList<HashMap<String, String>> list;
        warn = (TextView) findViewById(R.id.warn);
        listDistrict = new ArrayList<String>();
        textV = (TextView) findViewById(R.id.textV);
        buttonKonum = (Button) findViewById(R.id.buttonYer);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        gac = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        //Lokasyon bilgisinin alinmasi icin kullaniliyor.
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        file = new File(this.getFilesDir(), FILE_NAME);
    }

    /*Kullanicinin secim yapacagi iller spinner seklinde gosteriliyor. Bu spinner degerin secilmesinden ile ait olan ilceler
    yeni bir spinnerda gosteriliyor.*/
    public void spinnerHandler() {
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                selectedProvince = spinnerProvince.getSelectedItem().toString();
                if (!selectedProvince.equals("İl seçiniz")) {
                    province = selectedProvince;
                    selectedProvince = convertString(selectedProvince);
                    textV.setVisibility(View.VISIBLE);
                    spinnerIlce.setVisibility(View.VISIBLE);
                    String arrayName = "p" + position;
                    resourceId = getResources().getIdentifier(arrayName, "array", getPackageName());
                    districtName = getResources().getStringArray(resourceId);
                    listDistrict = Arrays.asList(districtName);
                    ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, listDistrict);
                    spinnerIlce.setAdapter(mArrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    //Herhangi bir ilcenin secilmesi sonucu calisacak olan kisimdir.
    public void spinnerIlceHandler() {
        spinnerIlce.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    selectedDistrict = spinnerIlce.getSelectedItem().toString();
                    district = selectedDistrict;
                    selectedDistrict = convertString(selectedDistrict);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /*Gerekli url adresinin olusturulmasi icin gerekli olan String veri istenen formata donusturuluyor
    @param word : Uzerinde degisim islemi gerceklesecek olan String veri
    @return word : Duzeltilmis String veri*/
    public String convertString(String word) {
        word = word.toLowerCase();
        int size = word.length();
        char[] province = word.toCharArray();

        for (int i = 0; i < size; i++) {
            if (province[i] == 'ç') {
                province[i] = 'c';
            } else if (province[i] == 'ğ') {
                province[i] = 'g';
            } else if (province[i] == 'ı') {
                province[i] = 'i';
            } else if (province[i] == 'ö') {
                province[i] = 'o';
            } else if (province[i] == 'ş') {
                province[i] = 's';
            } else if (province[i] == 'ü') {
                province[i] = 'u';
            }
        }
        word = String.valueOf(province);
        return word;
    }

    /*İstenilen yerden hava durumu bilgilerini elde edebilmek amaciyla gerekli url elde ediliyor. Bu url ile istenen sehirin ve
    sehirde bulunan ilcenin hava durumu bilgisi elde ediliyor.*/
    public void createNewURL() {
        concatSpecialURL = URL.concat(selectedProvince);
        concatSpecialURL += "-hava-durumu/";
        if ((selectedDistrict != null) && (selectedDistrict != "")) {
            concatSpecialURL = concatSpecialURL.concat(selectedDistrict);
            concatSpecialURL += "/";
        }
    }

    /*Butun secim islemlerinin gerceklestirilmesinden sonra hava durumu bilgisinin gosterilmesi icin kullanicinin show
    butonuna tiklamasi gerekmektedir.Show butonuna tiklama isleminden sonra ilgili veriler elde edilen url kullanilarak
    kullaniciya gostermek uzere internet uzerinden cekilir ve bu verilerin gosterilmesi icin yeni activitye gecilir.*/
    public void buttonGosterHandler() {
        buttonGoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedProvince.equals("İl seçiniz")) {
                    createNewURL();
                    new Data(MainActivity.this).execute();
                } else {
                    Toast.makeText(MainActivity.this, "Lütfen bir il seçiniz!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*Arka planda internet uzerinden verilerin cekilmesi islemi gerceklestiriliyor.*/
    public class Data extends AsyncTask<Void, Void, Void> {
        Context context;
        ProgressDialog progressDialog;

        public Data(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String veri;
            Document document;
            Elements elements;
            HashMap<String, String> map;
            int k = 0;

            list = new ArrayList<HashMap<String, String>>();
            try {
                //İnternetten verileri cekme islemi icn Jsoup kütüphanesi kullaniliyor.
                document = Jsoup.connect(concatSpecialURL).get();
                elements = document.select("table[id=hor-minimalist-a]").first().getElementsByTag("tr");
                Elements tds = elements.select("td");
                Elements imgSrc = elements.select("img[src]");
                //5 gunluk veri internet uzerinden getiriliyor.
                for (int i = 0; i < 5; i++) {
                    //Veriler daha sonra kullanilmak icin hash mape yerlestiriliyor.
                    map = new HashMap<String, String>();
                    for (int j = 0; j < 4; j++) {
                        if (j == 0) {
                            veri = tds.get(k).toString().replace("<td>", "");
                            veri = veri.replace("</td>", "");
                            map.put("Tarih", veri);
                        } else if (j == 1) {
                            k++;
                            veri = tds.get(k).toString().replace("<td>", "");
                            veri = veri.replace("</td>", "");
                            map.put("Gündüz", veri);
                        } else if (j == 2) {
                            veri = tds.get(k).toString().replace("<td>", "");
                            veri = veri.replace("</td>", "");
                            map.put("Gece", veri);
                        } else {
                            String imgSrcSrc = imgSrc.get(i).attr("src");
                            imgSrcSrc = imgSrcSrc.replace("http://www.havadurumux.net/svg/", "svg");
                            String[] str = imgSrcSrc.split("\\.");
                            imgSrcSrc = str[0];
                            map.put("Condition", imgSrcSrc);
                            list.add(map);
                        }
                        k++;
                    }
                    k--;
                }
            } catch (IOException ex) {
                Log.e(ERROR, ex.getMessage());
            }
            try {
                //Elde edilen veriler dosyaya yaziliyor.
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(list);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressDialog.dismiss();
            Intent intent = new Intent(context, Forecast.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(PROVINCE_NAME, province);
            if ((selectedDistrict != null) && (selectedDistrict != "")) {
                intent.putExtra(DISTRICT_NAME, district);
            }
            context.startActivity(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            locationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null)
                    {
                        updateLocation(location);
                    }
                }
            });
        }
    }

    /*Locasyon degisimlerini yakalamak icin kullaniliyor.
        @param location : Degisen lokasyon bilgisi, icerisinde enlem ve boylam degerleri de bulunmaktadir.
         */
    @Override
    public void onLocationChanged(Location location) {
        if (isLocationEnabled()) {
            if (location != null) {
                updateLocation(location);
            }
        }
    }
    public boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    /*Gonderilen lokasyon bilgisi kullanilarak bulunulan bolgenin hangi şehire ait oldugu elde ediliyor
    @param loc : lokasyon bilgisi, icerisinde enlem ve boylam degerleri de bulunmaktadir.*/
    public void updateLocation(Location loc) {
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            adressses = geocoder.getFromLocation(latitude,longitude,1);
            city_name = adressses.get(0).getAdminArea();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        try {
            location = FusedLocationApi.getLastLocation(gac);
            FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        gac.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gac.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //Geri tusuna basildigi zaman uygulamadan ccikarak, uygulamanın da arka plandan silinmesi saglanir.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}