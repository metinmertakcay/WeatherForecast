## Program Amacı ##
Program kullanıcının isteğine bağlı olarak seçmiş olduğu il ve (opsiyonel olarak ilçe seçebilir) seçilen ile ait ilçenin hava durumu bilgisini gösterir.

### Hava Durumu Bilgileri Nereden Nasıl Alınmıştır ###
Hava durumu bilgileri [havadurumux](http://www.havadurumux.net/) sitesinden [Jsoup](https://github.com/jhy/jsoup) kütüphanesi kullanılarak (html parse edilerek) alınmıştır. Hava durumu bilgileri [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html) yapısı içerisinde çekilmiştir ve ekranda gösterilebilmesi için bir dosyaya kaydedilmiştir ve dosyadan çekilerek kullanılmıştır.   

### Program Özellikleri ###
Hava durumu bilgilerinin çekilebilmesi için mobil veya wi-fi bağlantısının olması gerekir. İnternet bağlantısının olup olmadığı broadcast kullanılarak kontrol edilmiştir.<br/>
Programın çekiciliğini arttırmak için ana ekranda hava durumu ile ilgili [gif](https://github.com/koral--/android-gif-drawable) kullanılmıştır.<br/>
Kullanıcı cihazın konum özelliğini kullanarak bulunduğu bölgenin hava durumu bilgisine erişebilir.

#### Program Eksikleri ####
Veriler siteden html parse edilerek çekildiği için site sayfasında bilgilerin çekildiği yerde gerçekleşecek bir değişim programın çalışmamasına neden olabilir.<br/>
Hava durumu bilgisini gösteren resimler .svg formatındadır. Bu resimler .jpg formatına çevrilerek projeye dahil edilmiş ve kullanılmıştır.

#### Ekran Görüntüleri ####
Ekran görüntüleri için [tıklayınız.](https://github.com/metinmertakcay/WeatherForecast/tree/master/images)
