# Proje Gereksinimleri ve Mimari

## Giris

Bu notun konusu, `spring-core-gym-crm` projesinin ne yapmaya calistigini, hangi sinirlar icinde tasarlandigini ve kod tabanindaki katmanlarin bu gereksinimleri nasil karsiladigini anlamaktir.

Bu dosyayi bitirdiginde su sorulara net cevap verebilmelisin:

- Bu proje neden bir Spring Boot web uygulamasi degil?
- Neden veritabani yerine in-memory `Map` bean'leri kullaniliyor?
- Domain, DAO, service, facade, config, initializer, generator ve test katmanlari hangi sorumluluklari tasiyor?
- README, PLAN ve `pom.xml` dosyalarindaki beklentiler kodda hangi siniflarla karsilaniyor?
- Degerlendirme kriterleri hangi testler ve Maven ayarlariyla guvence altina aliniyor?

## 2. Bu Konunun Amaci

Bu konunun amaci, projeye "kod dosyalari toplamindan" ziyade bir uygulama tasarimi olarak bakmaktir.

Bir projeye hakim olmak icin sadece siniflari tek tek okumak yetmez. Once projenin hangi problemi cozdugunu, hangi teknolojileri neden kullandigini, hangi teknolojileri bilerek kullanmadigini ve hangi katmanin hangi sorumlulugu tasidigini bilmek gerekir.

Bu not su acidan ilk duraktir:

- `README.md` sana projenin nasil build/test/run edilecegini soyler.
- `PLAN.md` projenin basari kriterlerini ve tasarim niyetini anlatir.
- `pom.xml` secilen teknoloji yiginini ve kalite kapilarini gosterir.
- `src/main/java` altindaki paketler bu gereksinimlerin uygulanmis halidir.
- `src/test/java` altindaki testler bu davranislarin tekrar tekrar dogrulanmasini saglar.

Bu dosyayi okurken hedefin her seyi ezberlemek degil, "hangi karar nereden geliyor?" sorusunu cevaplayabilecek hale gelmektir.

## 3. Buyuk Resim

Proje, bir spor salonu icin basit bir CRM moduludur. Temel varliklar:

- `Trainee`: Spor salonunda egitim alan kisi.
- `Trainer`: Egitim veren kisi.
- `Training`: Bir trainee ile bir trainer arasindaki egitim kaydi.
- `TrainingType`: Egitim turu.

Uygulama Spring Core container ile calisir. Yani nesneleri elle `new` ile her yerde olusturmak yerine, Spring bean'leri olusturur, bagimliliklari enjekte eder ve uygulama acilisinda gerekli ilk veriyi yukler.

Genel akis:

```text
GymCrmApplication
  -> AnnotationConfigApplicationContext(AppConfig.class)
      -> AppConfig component scan ve property source ayarlar
      -> StorageConfig uc ayri HashMap bean'i olusturur
      -> StorageBeanPostProcessor initial-data.json dosyasini Map bean'lerine yukler
      -> DAO bean'leri ilgili Map bean'lerini kullanir
      -> Service bean'leri is kurallarini uygular
      -> GymFacade servisleri tek giris noktasi olarak disariya acar
```

Bu proje web request/response akisi uzerine kurulu degildir. Controller, REST endpoint, HTTP status kodu, repository, ORM veya database yoktur. Bunun yerine odak noktasi Spring Core, dependency injection, katmanli mimari, in-memory storage, validation, exception, logging ve testtir.

### Neden Spring Core?

Gorevin ana konusu Spring container'in kendisidir. Bu yuzden proje `spring-context` kullanir:

- `@Configuration` ile Java tabanli configuration yazilir.
- `@ComponentScan` ile component'ler bulunur.
- `@Bean` ile storage bean'leri tanimlanir.
- `@Autowired` ve `@Qualifier` ile bagimliliklar enjekte edilir.
- `BeanPostProcessor` ile bean yasam dongusune mudahale edilip initial data yuklenir.

Spring Boot kullanilsaydi cok fazla otomatik konfigurasyon devreye girerdi. Bu projede amac o otomasyonun arkasina saklanmak degil, Spring Core davranisini acik sekilde gormektir.

### Neden in-memory storage?

Gorev, database tasarimi veya kalici veri saklama problemi degildir. Bu nedenle her entity tipi ayri bir `HashMap` bean'inde tutulur:

- `traineeStorage`
- `trainerStorage`
- `trainingStorage`

Bu tercih sunlari ogretir:

- Bean isimleri ve `@Qualifier` neden onemlidir?
- Ayni tipte birden fazla bean varsa Spring hangi bean'i sececegini nasil bilir?
- DAO katmani storage detayini servislerden nasil saklar?
- Initial data uygulama acilisinda nasil yuklenebilir?

In-memory storage gecicidir. Uygulama kapaninca veri kaybolur. Bu, proje icin bir eksik degil, bilincli kapsam kararidir.

### Neden web ve database yok?

Bu proje bir CRM modulunun cekirdek is mantigini calistirir. Web ve database eklenirse:

- Controller, DTO mapping, HTTP validation ve error response gibi yeni konular girer.
- DataSource, transaction, JPA mapping ve migration gibi yeni kararlar gerekir.
- Spring Boot auto-configuration projenin Spring Core odagini zayiflatir.

Bu nedenle kapsam bilincli olarak dar tutulur: once core domain ve Spring wiring dogru kurulacak, sonra istenirse web veya database katmani eklenebilecek bir zemin olusacaktir.

## 4. Gereksinimler ve Kabul Kriterleri

Gereksinimler en net sekilde `README.md`, `PLAN.md` ve `pom.xml` dosyalarindan okunur.

### Kaynaklara gore proje amaci

| Kaynak | Ne soyluyor? | Kodda karsiligi |
| --- | --- | --- |
| `README.md` | Spring Core based Gym CRM module | `AppConfig`, `StorageConfig`, `GymFacade` |
| `README.md` | Trainee, trainer ve training icin ayri in-memory Map storage | `StorageConfig` icindeki uc `@Bean` |
| `README.md` | Initial data file startup sirasinda yuklenir | `StorageBeanPostProcessor` |
| `README.md` | Username duplicate durumunda suffix alir | `UniqueUsernameGenerator` |
| `README.md` | Password random 10 karakterdir | `SecurePasswordGenerator` |
| `README.md` | Hassas veri loglanmaz | DAO logging stili ve `LoggingSafetyTest` |
| `PLAN.md` | Spring Boot, REST API ve veritabani kapsam disidir | `pom.xml` sadece `spring-context` icerir |
| `pom.xml` | Java 21, Spring 6.2.x, Jackson, SLF4J/Logback, JUnit/Mockito/AssertJ/JaCoCo kullanilir | Build ve test altyapisi |

### Kabul kriterleri ve koddaki izleri

| Kabul kriteri | Koddaki / testteki karsilik |
| --- | --- |
| Uygulama hatasiz derlenir ve calisir | `mvnw.cmd clean verify`, `exec-maven-plugin`, `GymCrmApplication` |
| Spring context Java/annotation configuration ile kurulur | `AppConfig`, `@Configuration`, `@ComponentScan`, `@PropertySource` |
| Her entity ayri in-memory `Map` bean'inde tutulur | `StorageConfig`, `StorageBeanNames`, `SpringContextIntegrationTest` |
| Initial data harici property ile belirtilen dosyadan yuklenir | `config/application.properties`, `StorageBeanPostProcessor` |
| Initial data yukleme `BeanPostProcessor` ile yapilir | `StorageBeanPostProcessor implements BeanPostProcessor` |
| DAO bagimliliklari setter injection ile verilir | `TraineeDaoImpl#setStorage`, `TrainerDaoImpl#setStorage`, `TrainingDaoImpl#setStorage` |
| Ayni tipte bean karisikligi `@Qualifier` ile cozulur | DAO storage setter'lari ve `UniqueUsernameGenerator` |
| Service katmani is kurallarini uygular | `TraineeServiceImpl`, `TrainerServiceImpl`, `TrainingServiceImpl` |
| Facade servisleri tek giris noktasi olarak sunar | `GymFacade` |
| Username cakismalari trainee ve trainer birlikte kontrol edilerek engellenir | `UniqueUsernameGenerator` |
| Password rastgele ve 10 karakterdir | `SecurePasswordGenerator`, `GeneratorTest` |
| Update sirasinda username/password korunur | `TraineeServiceImpl#update`, `TrainerServiceImpl#update` |
| Training olustururken trainee/trainer varligi dogrulanir | `TrainingServiceImpl#create` |
| Kontrollu domain exception kullanilir | `EntityNotFoundException`, `ValidationException`, `StorageInitializationException` |
| Hassas veri loglanmaz | `LoggingSafetyTest`, DAO log mesajlari |
| Line coverage en az yuzde 80'dir | `jacoco-maven-plugin` check kuralinda `0.80` |

### Build, test ve run komutlari

Windows icin:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd exec:java
.\mvnw.cmd exec:java "-Dexec.args=--demo"
```

Bu komutlar su anlama gelir:

- `clean verify`: Projeyi temizler, derler, testleri calistirir, JaCoCo raporu uretir ve coverage esigini kontrol eder.
- `exec:java`: `GymCrmApplication` sinifini calistirir.
- `exec:java "-Dexec.args=--demo"`: Uygulamayi demo senaryo ile calistirir.

## 5. Mimari Katmanlar

Proje klasik katmanli mimariyi basit ama net bir sekilde uygular.

```text
application
  -> facade
      -> service
          -> dao
              -> storage Map beans

config ve initializer bu akisin kurulum tarafini yonetir.
generator username/password uretimi gibi yan is kurallarini tasir.
domain tum katmanlarin uzerinde calistigi modeli verir.
test katmani davranisi guvence altina alir.
```

### Katman sorumluluklari

| Katman / paket | Sorumluluk | Ornek dosyalar |
| --- | --- | --- |
| `domain` | Is modelini temsil eder | `User`, `Trainee`, `Trainer`, `Training`, `TrainingType` |
| `dao` | Storage erisim sozlesmelerini tanimlar | `TraineeDao`, `TrainerDao`, `TrainingDao` |
| `dao.impl` | `Map` uzerinden CRUD davranisini uygular | `AbstractMapDao`, `TraineeDaoImpl` |
| `service` | Use case sozlesmelerini tanimlar | `TraineeService`, `TrainerService`, `TrainingService` |
| `service.impl` | Validasyon ve is kurallarini uygular | `TraineeServiceImpl`, `TrainingServiceImpl` |
| `service.command` | Create/update isteklerini tasir | `CreateTraineeCommand`, `UpdateTrainerCommand` |
| `facade` | Dis dunyaya tek giris noktasi saglar | `GymFacade` |
| `config` | Spring context ve storage bean'lerini kurar | `AppConfig`, `StorageConfig`, `StorageBeanNames` |
| `initializer` | Baslangic verisini storage bean'lerine yukler | `StorageBeanPostProcessor`, `InitialData` |
| `generator` | Username ve password uretimini soyutlar | `UniqueUsernameGenerator`, `SecurePasswordGenerator` |
| `exception` | Kontrollu hata tiplerini tasir | `EntityNotFoundException`, `ValidationException` |
| `application` | Uygulamayi baslatir ve demo akisini calistirir | `GymCrmApplication` |
| `test` | Kabul kriterlerini ve davranislari dogrular | `SpringContextIntegrationTest`, `GeneratorTest` |

### Domain katmani

Domain katmani uygulamanin isimlerini ve temel kavramlarini verir.

- `User` ortak alanlari tasiyan abstract siniftir.
- `Trainee`, `User` uzerine `dateOfBirth` ve `address` ekler.
- `Trainer`, `User` uzerine `specialization` ekler.
- `Training`, trainee/trainer iliskisini ve egitim bilgisini tutar.
- `TrainingType`, gecerliligi bilinen egitim turlerini enum olarak sinirlar.

Bu katman Spring'e bagimli degildir. Bu iyi bir tasarim tercihidir; model siniflari container olmadan da anlasilabilir ve test edilebilir.

### DAO katmani

DAO katmani veriye nasil erisildigini saklar. Servisler dogrudan `HashMap` bilmez; DAO arayuzlerini bilir.

`AbstractMapDao` ortak davranislari toplar:

- ID uretimi
- create/update/delete/find akisi
- storage inject edilmis mi kontrolu
- ID'ye gore sirali `findAll`
- temel logging

Somut DAO siniflari sadece hangi entity ile calistiklarini ve hangi storage bean'ini aldiklarini belirtir.

### Service katmani

Service katmani projenin is kurallarini tasir.

Ornekler:

- Create komutunda zorunlu alanlar kontrol edilir.
- Trainee/trainer icin username ve password uretilir.
- Update sirasinda mevcut entity bulunur, sonra degistirilebilir alanlar guncellenir.
- Training create edilirken ilgili trainee ve trainer var mi kontrol edilir.
- Gecersiz input icin `ValidationException`, bulunamayan kayit icin `EntityNotFoundException` kullanilir.

Bu katman "uygulama ne yapar?" sorusunun ana cevabidir.

### Facade katmani

`GymFacade`, uc servisi tek sinifta toplar:

- `TraineeService`
- `TrainerService`
- `TrainingService`

Facade sayesinde uygulama girisi veya dis kullanim noktasi servisleri tek tek aramak zorunda kalmaz. Bu projede `GymCrmApplication` context'ten `GymFacade` bean'ini alir ve islemleri onun uzerinden yapar.

### Config ve initializer katmani

`AppConfig`, Spring container'in nasil kurulacagini soyler:

- `@Configuration`: Bu sinif configuration sinifidir.
- `@ComponentScan("com.example.gymcrm")`: Component, service, repository gibi bean'leri bulur.
- `@PropertySource("file:${gym.config.path:config/application.properties}")`: Property dosyasini okur.
- `PropertySourcesPlaceholderConfigurer`: `${...}` placeholder'larinin cozulmesini saglar.

`StorageConfig`, uc ayri `HashMap` bean'i olusturur. Bu bean'ler startup sirasinda `StorageBeanPostProcessor` tarafindan initial data ile doldurulur.

### Generator katmani

Username ve password uretimi servislerin icine gomulmemistir. Ayri interface ve implementasyonlara ayrilmistir:

- `UsernameGenerator` -> `UniqueUsernameGenerator`
- `PasswordGenerator` -> `SecurePasswordGenerator`

Bu tercih test edilebilirligi artirir. Ayrica username/password kurali degisirse servis katmanini buyutmeden generator degistirilebilir.

### Test katmani

Testler sadece "kod calisiyor mu?" diye bakmaz; kabul kriterlerini de kontrol eder.

Ornekler:

- `SpringContextIntegrationTest`: Context aciliyor mu, storage'lar ayri mi, facade wire edilmis mi?
- `MapDaoTest`: DAO CRUD ve ID davranislari dogru mu?
- `GeneratorTest`: Username/password kurallari dogru mu?
- `StorageBeanPostProcessorTest`: Initial data yukleme ve hata senaryolari dogru mu?
- `LoggingSafetyTest`: Loglarda hassas veri siziyor mu?
- Service testleri: Validasyon, update, create ve hata akislari dogru mu?

## 6. Bu Projede Nerede?

Bu bolum, nottaki kavramlari dosya dosya takip etmen icin kisa haritadir.

### Dokuman ve build dosyalari

- `README.md`: Projenin kisa tanimi, teknoloji stack'i, build/test/run komutlari.
- `PLAN.md`: Basari kriterleri, tasarim kararlari, katman plani, test plani.
- `pom.xml`: Dependency'ler, Java surumu, test runner, JaCoCo coverage check, exec plugin.

### Spring configuration

- `src/main/java/com/example/gymcrm/config/AppConfig.java`
- `src/main/java/com/example/gymcrm/config/StorageConfig.java`
- `src/main/java/com/example/gymcrm/config/StorageBeanNames.java`
- `config/application.properties`
- `config/initial-data.json`

### Initial data

- `src/main/java/com/example/gymcrm/initializer/StorageBeanPostProcessor.java`
- `src/main/java/com/example/gymcrm/initializer/InitialData.java`
- `src/test/java/com/example/gymcrm/initializer/StorageBeanPostProcessorTest.java`

### Domain

- `src/main/java/com/example/gymcrm/domain/User.java`
- `src/main/java/com/example/gymcrm/domain/Trainee.java`
- `src/main/java/com/example/gymcrm/domain/Trainer.java`
- `src/main/java/com/example/gymcrm/domain/Training.java`
- `src/main/java/com/example/gymcrm/domain/TrainingType.java`

### DAO

- `src/main/java/com/example/gymcrm/dao/TraineeDao.java`
- `src/main/java/com/example/gymcrm/dao/TrainerDao.java`
- `src/main/java/com/example/gymcrm/dao/TrainingDao.java`
- `src/main/java/com/example/gymcrm/dao/impl/AbstractMapDao.java`
- `src/main/java/com/example/gymcrm/dao/impl/TraineeDaoImpl.java`
- `src/main/java/com/example/gymcrm/dao/impl/TrainerDaoImpl.java`
- `src/main/java/com/example/gymcrm/dao/impl/TrainingDaoImpl.java`

### Service ve command'ler

- `src/main/java/com/example/gymcrm/service/TraineeService.java`
- `src/main/java/com/example/gymcrm/service/TrainerService.java`
- `src/main/java/com/example/gymcrm/service/TrainingService.java`
- `src/main/java/com/example/gymcrm/service/ValidationSupport.java`
- `src/main/java/com/example/gymcrm/service/impl/TraineeServiceImpl.java`
- `src/main/java/com/example/gymcrm/service/impl/TrainerServiceImpl.java`
- `src/main/java/com/example/gymcrm/service/impl/TrainingServiceImpl.java`
- `src/main/java/com/example/gymcrm/service/command/*.java`

### Facade ve uygulama girisi

- `src/main/java/com/example/gymcrm/facade/GymFacade.java`
- `src/main/java/com/example/gymcrm/application/GymCrmApplication.java`

### Generator ve exception

- `src/main/java/com/example/gymcrm/generator/UniqueUsernameGenerator.java`
- `src/main/java/com/example/gymcrm/generator/SecurePasswordGenerator.java`
- `src/main/java/com/example/gymcrm/exception/*.java`

### Testler

- `src/test/java/com/example/gymcrm/config/SpringContextIntegrationTest.java`
- `src/test/java/com/example/gymcrm/dao/impl/MapDaoTest.java`
- `src/test/java/com/example/gymcrm/service/impl/*.java`
- `src/test/java/com/example/gymcrm/generator/GeneratorTest.java`
- `src/test/java/com/example/gymcrm/logging/LoggingSafetyTest.java`

## 7. Kod / Dosya Okuma Rotasi

Bu konuyu calisirken asagidaki sirayi takip et. Amac once niyeti, sonra kurulumu, sonra akis ve testleri gormektir.

1. `README.md`
   - Proje kendini nasil tanimliyor?
   - Hangi komutlarla build/test/run ediliyor?
   - Uygulama notlarinda hangi davranislar vurgulanmis?

2. `PLAN.md`
   - Basari kriterlerini oku.
   - "Spring Boot, REST API ve veritabani kapsam disidir" satirini ozellikle not et.
   - Katman ve test planinin bugunku kodla eslesip eslesmedigine bak.

3. `pom.xml`
   - `spring-context` var mi?
   - `spring-boot-starter-*` var mi? Yoksa bu bilincli bir karardir.
   - `jacoco-maven-plugin` hangi coverage esigini koyuyor?
   - `exec-maven-plugin` hangi main class'i calistiriyor?

4. `AppConfig.java`
   - Component scan hangi paketi tarar?
   - Property dosyasi hangi path'ten okunur?
   - `gym.config.path` verilmezse default ne olur?

5. `StorageConfig.java`
   - Kac tane `Map` bean'i olusturulur?
   - Bean isimleri nereden gelir?
   - Hepsi ayni `HashMap` mi, yoksa ayri instance mi?

6. `StorageBeanPostProcessor.java`
   - Sadece hangi bean isimleri icin populate yapar?
   - JSON dosyasi ne zaman okunur?
   - Duplicate ID veya missing file durumunda ne olur?

7. `domain` paketi
   - `User` ortak alanlari nasil topluyor?
   - `Trainee`, `Trainer`, `Training` hangi farkli alanlara sahip?

8. `dao.impl` paketi
   - `AbstractMapDao` tekrar eden CRUD davranisini nasil topluyor?
   - Somut DAO'lar storage bean'ini nasil aliyor?

9. `service.impl` paketi
   - Create/update akislari hangi validasyonlari yapiyor?
   - Training create ederken trainee/trainer varligi nasil dogrulaniyor?
   - Update sirasinda username/password neden yeniden uretilmiyor?

10. `GymFacade.java`
    - Facade hangi servisleri topluyor?
    - Uygulama girisi neden facade uzerinden ilerliyor?

11. `src/test/java`
    - Her kabul kriteri icin hangi testin guvence verdigini bul.
    - Testleri okurken "hangi riski yakaliyor?" diye sor.

## 8. Kritik Tasarim Kararlari

### 1. Spring Boot yerine Spring Core

Bu karar, projenin ogretici merkezini belirler. Spring Boot olsaydi dependency injection yine olurdu ama cok fazla davranis otomatik gelirdi. Burada container acma, configuration, property okuma ve bean post-processing daha gorunur durumdadir.

### 2. Veritabani yerine ayri `HashMap` bean'leri

Her entity icin ayri `Map` kullanmak, hem gereksinimi karsilar hem de bean isimlendirme/qualifier konusunu ogretir.

Yanlis bir tasarim soyle olurdu:

```text
Tek bir Map<Object, Object> icine her seyi koymak.
```

Bu, tip guvenligini ve katman ayrimini zayiflatirdi.

### 3. DAO'larin storage detayini saklamasi

Servis katmani `Map` bilmez. Servis sadece DAO arayuzlerini kullanir. Boylece ileride storage degisse bile servis davranisi daha az etkilenir.

### 4. Service katmaninda is kurallari

Input validasyonu, entity var mi kontrolu, username/password uretimi gibi kararlar service katmaninda veya service tarafindan kullanilan yardimci component'lerde toplanir. DAO sadece saklama/okuma davranisindan sorumludur.

### 5. Facade ile tek giris noktasi

Facade, uygulama disina daha temiz bir API verir. `GymCrmApplication` veya ileride eklenecek bir adapter, birden fazla service'i tek tek bilmek yerine facade ile konusabilir.

### 6. Initial data icin `BeanPostProcessor`

Initial data yukleme isi dogrudan `StorageConfig` icine konmamistir. Bunun yerine bean yasam dongusune giren bir `BeanPostProcessor` kullanilir. Bu sayede storage bean'leri once olusur, sonra isimlerine gore doldurulur.

Bu konu Spring Core icin onemlidir; cunku bean'in sadece olusturulmasini degil, olustuktan sonra islenmesini de gosterir.

### 7. Username ve password icin Strategy benzeri yapi

`UsernameGenerator` ve `PasswordGenerator` interface'leri sayesinde uretim kurali servislerden ayrilir. Bu, hem testlerde fake/mock kullanmayi kolaylastirir hem de ileride kurali degistirmeyi basitlestirir.

### 8. Hassas veri loglamama

CRM verisi kisi bilgisi tasir. Password, adres ve dogum tarihi gibi verilerin loglara cikmamasi gerekir. Projede loglar ID ve sayi gibi teknik bilgilerle sinirli tutulur.

### 9. Coverage kapisi

JaCoCo sadece rapor uretmez, `verify` asamasinda coverage kontrolu yapar. Line coverage `0.80` altina duserse build basarisiz olur. Bu, kalite kriterinin manuel degil otomatik oldugu anlamina gelir.

## 9. Sik Dusulen Hatalar

- Bu projeyi Spring Boot projesi sanmak.
  - `pom.xml` icinde `spring-boot-starter` yoktur. Context manuel olarak `AnnotationConfigApplicationContext` ile acilir.

- Controller veya REST endpoint aramak.
  - Bu projede web katmani yoktur. Disa acilan ana API `GymFacade` sinifidir.

- Storage'i database gibi dusunmek.
  - `HashMap` in-memory'dir. Uygulama kapaninca runtime'da eklenen veriler kalici olmaz.

- Tum entity'leri tek storage'da aramak.
  - Gereksinim her entity icin ayri storage bean'i ister.

- `@Qualifier` ihtiyacini kacirmak.
  - Ayni raw tipte birden fazla `Map` bean'i vardir. Dogru storage'i secmek icin bean ismi gerekir.

- Initial data'nin DAO tarafindan yuklendigini sanmak.
  - Yukleme `StorageBeanPostProcessor` icindedir; DAO sadece kendisine verilen storage uzerinde calisir.

- Username'in sadece trainee'ler icinde unique oldugunu sanmak.
  - `UniqueUsernameGenerator`, trainee ve trainer storage'larini birlikte kontrol eder.

- Update sirasinda username/password uretmek.
  - Mevcut uygulamada update, username ve password'u korur. Create ile update davranislari ayni degildir.

- Logging'de tum entity'yi yazmak.
  - Entity'nin tam halini loglamak password/adres/dogum tarihi gibi hassas alanlari sizdirabilir.

- Testleri sadece coverage icin gormek.
  - Testler ayni zamanda mimari kararlarin bozulmadigini kontrol eder.

## 10. Kendini Test Et

Asagidaki sorulari cevaplamadan sonraki konuya gecme. Mumkunse cevaplari kendi cumlelerinle yaz.

1. Bu proje neden Spring Boot kullanmiyor? Spring Core burada hangi kavramlari daha gorunur hale getiriyor?

2. `StorageConfig` icinde neden uc ayri `Map` bean'i var? Tek bir `Map` kullanilsaydi hangi problemler cikardi?

3. `AppConfig` icindeki `@PropertySource("file:${gym.config.path:config/application.properties}")` ne ise yarar?

4. `StorageBeanPostProcessor` initial data yuklemek icin neden uygun bir noktadir?

5. DAO katmani ile service katmani arasindaki sorumluluk farki nedir?

6. `GymFacade` olmasa `GymCrmApplication` hangi siniflara dogrudan baglanmak zorunda kalirdi?

7. Username uniqueness neden sadece ayni entity tipi icinde degil, trainee ve trainer birlikte dusunulerek saglaniyor?

8. `pom.xml` icindeki JaCoCo check kurali projeyi nasil daha guvenilir yapar?

9. Bu projede "database yok" demek "veri katmani yok" anlamina gelir mi? Neden?

10. Loglarda password, adres veya dogum tarihi cikmasi neden kabul kriteri acisindan risklidir?

## 11. Mini Pratik

Bu pratikleri yaparken once kodu oku, sonra kucuk notlar al. Degisiklik yaparsan `.\mvnw.cmd clean verify` ile sonucu kontrol et.

### Pratik 1: Kabul kriteri haritasi cikar

`PLAN.md` dosyasindaki tamamlanma kriterlerini tek tek oku. Her kriterin yanina su formatta kod karsiligini yaz:

```text
Kriter:
Kod dosyasi:
Test dosyasi:
Nasil dogrulaniyor:
```

En az su kriterleri sec:

- Ayri Map storage
- Initial data yukleme
- Username duplicate kontrolu
- Password uzunlugu
- Logging safety
- Coverage check

### Pratik 2: Uygulama acilis akisini elle ciz

Su siniflari kullanarak bir akis semasi ciz:

```text
GymCrmApplication
AppConfig
StorageConfig
StorageBeanPostProcessor
GymFacade
TraineeServiceImpl
TraineeDaoImpl
```

Her ok icin "ne oluyor?" diye bir cumle yaz. Ornek:

```text
AppConfig -> StorageConfig: Component scan ile configuration bean'i bulunur.
```

### Pratik 3: `pom.xml` kalite kapilarini oku

`pom.xml` icinde su eklentileri bul:

- `maven-compiler-plugin`
- `maven-surefire-plugin`
- `jacoco-maven-plugin`
- `exec-maven-plugin`

Her biri icin bir cumlelik aciklama yaz. Sonra su soruya cevap ver:

```text
Bu plugin kaldirilsa projede hangi guvence veya kolaylik kaybolur?
```

### Pratik 4: Storage bean'lerini takip et

`StorageBeanNames`, `StorageConfig`, DAO implementasyonlari ve `UniqueUsernameGenerator` dosyalarini birlikte oku.

Kendine su sorulari sor:

- Bean isimleri nerede merkezi tutulmus?
- DAO hangi bean'i alacagini nasil biliyor?
- Username generator neden iki storage'a da ihtiyac duyuyor?

### Pratik 5: Testi gereksinime bagla

`SpringContextIntegrationTest` dosyasini oku. Testteki her assertion icin hangi gereksinimi dogruladigini not et.

Ornek format:

```text
Assertion:
Doguladigi gereksinim:
Bozulursa ne olur:
```

## 12. Mulakat / Sinav Sorulari

Bu sorular, projeyi anlatirken teknik derinlik gostermen icin hazirlandi.

1. Spring Core ile Spring Boot arasindaki farki bu proje uzerinden aciklar misin?

2. Ayni tipte birden fazla Spring bean'i varsa `@Qualifier` neden gerekir? Bu projeden ornek ver.

3. `BeanPostProcessor` nedir? Bu projede hangi problemi cozuyor?

4. DAO ve service katmanlarini ayirmak neden onemlidir? Bu proje veritabani kullanmadigi halde DAO katmanina neden ihtiyac duyar?

5. Facade pattern bu projede nasil kullanilmis? Hangi bagimliliklari sadelestirir?

6. In-memory `HashMap` storage kullanmanin avantajlari ve dezavantajlari nelerdir?

7. Username uretiminde trainee ve trainer storage'larini birlikte kontrol etmek hangi is kuralini korur?

8. Update operasyonunda username/password neden yeniden uretilmemeli?

9. JaCoCo coverage threshold build surecine nasil dahil edilmis?

10. Hassas veri loglamamak icin kod yazarken nelere dikkat edersin?

11. Bu projeye ileride database eklemek isteseydin hangi katmanlari degistirirdin, hangilerini korumaya calisirdin?

12. Bu projeye REST API eklemek isteseydin mevcut `GymFacade` sana nasil yardim ederdi?

## 13. Ozet

Bu proje, Spring Core ile yazilmis katmanli bir Gym CRM moduludur. Ana hedef web uygulamasi veya database sistemi kurmak degil; Spring container, dependency injection, bean configuration, bean post-processing, in-memory storage, servis kurallari, facade ve test guvencesini temiz bir ornekte gormektir.

Buyuk resmi su sekilde hatirla:

```text
Config context'i kurar.
StorageConfig Map bean'lerini olusturur.
StorageBeanPostProcessor initial data yukler.
DAO storage'a erisir.
Service is kurallarini uygular.
Generator yardimci uretim kurallarini tasir.
Facade servisleri tek giris noktasinda toplar.
Application facade'i kullanarak uygulamayi baslatir.
Testler kabul kriterlerini korur.
```

Bu konudan sonra Spring Core container ve bean yasam dongusu dosyasina gecmek mantiklidir. Cunku artik "neden bu kadar Spring configuration var?" sorusunun proje baglamindaki cevabini biliyorsun.
