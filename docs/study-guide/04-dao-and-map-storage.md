# DAO Katmani ve In-memory Map Storage

Bu not, projedeki DAO katmanini ve `Map<Long, Entity>` tabanli in-memory storage yapisini ders calisir gibi anlamak icin hazirlandi.

Odak noktamiz su soru:

> "Service katmani neden dogrudan bir `Map` ile konusmuyor da araya DAO koyuyoruz?"

Bu sorunun cevabi, hem temiz katmanli mimariyi hem de bu projedeki Spring Core wiring mantigini anlamayi saglar.

## Bu Konunun Amaci

Bu bolumu bitirdiginde sunlari rahatca aciklayabilmelisin:

- DAO pattern'in ne ise yaradigini.
- Service katmani ile storage detayinin neden ayrildigini.
- Bu projede `Map<Long, Entity>` ile nasil basit bir veritabani benzeri yapi kuruldugunu.
- `AbstractMapDao<T>` sinifinin generic CRUD davranisini nasil topladigini.
- ID sequence mantiginin seeded initial data ile nasil uyumlu calistigini.
- `findAll()` sonucunun neden ID'ye gore siralandigini.
- `Optional<T>` kullaniminin `null` donmekten farkini.
- `TraineeDaoImpl`, `TrainerDaoImpl` ve `TrainingDaoImpl` siniflarinin ortak DAO davranisini nasil kullandigini.
- Setter injection ve `@Qualifier` ile dogru storage bean'inin nasil baglandigini.
- `EntityNotFoundException`, logging ve testlerin bu katmanda neyi garanti ettigini.

## Buyuk Resim

Bu projede kalici bir veritabani yok. Onun yerine Spring tarafindan olusturulan uc ayri `HashMap` storage var:

- `Map<Long, Trainee>`
- `Map<Long, Trainer>`
- `Map<Long, Training>`

Bu map'ler uygulama ayaga kalkarken Spring bean olarak olusturulur. Sonra initial data dosyasindan gelen verilerle doldurulur. DAO siniflari da bu map'leri kullanarak create, update, delete, find ve list operasyonlarini yapar.

Katmanlari yukaridan asagiya soyle dusun:

```text
Facade
  -> Service
      -> DAO interface
          -> DAO implementation
              -> Map<Long, Entity> storage
```

Burada onemli ayrim sudur:

- Facade dis dunyaya kolay bir API sunar.
- Service is kurallarini, validasyonu, username/password uretimini ve iliskisel kontrolleri yapar.
- DAO veri saklama ve veri okuma detayini soyutlar.
- Storage ise su an sadece bellekte duran `HashMap` nesnesidir.

Bu ayrim sayesinde ileride `HashMap` yerine dosya, JDBC, JPA veya baska bir storage kullanmak istersek service katmaninin mantigini bastan yazmak zorunda kalmayiz. DAO interface ayni kalirsa service tarafindaki bagimlilik da daha stabil kalir.

## DAO Pattern Temelleri

DAO, "Data Access Object" kisaltmasidir. Temel amaci, veri erisim detaylarini uygulamanin is kurallarindan ayirmaktir.

Bir service sinifi su sorularla ilgilenmelidir:

- Bu komut gecerli mi?
- Kullanici gerekli alanlari doldurmus mu?
- Training olusturulurken trainee ve trainer gercekten var mi?
- Yeni kullanici icin username ve password nasil uretilmeli?

Bir DAO sinifi ise su sorularla ilgilenmelidir:

- Entity hangi ID ile saklanacak?
- Var olan entity nasil bulunacak?
- Entity nasil guncellenecek?
- Bulunamayan kayit icin ne yapilacak?
- Tum kayitlar hangi sirayla listelenecek?

Bu projede DAO interface'leri service katmaninin baglandigi sozlesmelerdir:

- `src/main/java/com/example/gymcrm/dao/TraineeDao.java`
- `src/main/java/com/example/gymcrm/dao/TrainerDao.java`
- `src/main/java/com/example/gymcrm/dao/TrainingDao.java`

Implementasyonlar ise storage detayini bilir:

- `src/main/java/com/example/gymcrm/dao/impl/TraineeDaoImpl.java`
- `src/main/java/com/example/gymcrm/dao/impl/TrainerDaoImpl.java`
- `src/main/java/com/example/gymcrm/dao/impl/TrainingDaoImpl.java`
- `src/main/java/com/example/gymcrm/dao/impl/AbstractMapDao.java`

### Neden service dogrudan Map kullanmiyor?

Service dogrudan `Map<Long, Trainee>` kullansaydi:

- ID uretme mantigi service icine dagilirdi.
- `put`, `replace`, `remove`, `get` gibi storage detaylari is kurallariyla karisirdi.
- Trainee, Trainer ve Training icin ayni CRUD kodlari tekrar edilirdi.
- Storage tipi degisirse service siniflari da etkilenirdi.
- Testlerde is kurali testi ile storage davranisi testi birbirine karisirdi.

DAO pattern bu karisimi engeller. Service, "trainee olustur" der. DAO, bunun map uzerinde nasil yapilacagini bilir.

## In-memory Storage Mantigi

In-memory storage, verinin uygulama belleginde tutulmasi demektir. Bu projede bu is icin `HashMap` kullanilir.

Basit model sudur:

```java
Map<Long, Trainee> traineeStorage = new HashMap<>();
```

Burada:

- Key: entity ID'si, yani `Long`.
- Value: saklanan domain nesnesi, yani `Trainee`, `Trainer` veya `Training`.

Ornek:

```text
traineeStorage
  1 -> Trainee{id=1, active=true}
  2 -> Trainee{id=2, active=false}
```

### Bu yaklasimin avantajlari

- Basittir.
- Veritabani kurulumu gerektirmez.
- Spring Core, DAO, service ve test mantigini ogrenmek icin uygundur.
- Unit testlerde kolayca yeni `HashMap` verilerek izole calisilir.

### Bu yaklasimin sinirlari

- Uygulama kapaninca veri kaybolur.
- `HashMap` varsayilan haliyle thread-safe degildir.
- Transaction yoktur.
- Gercek veritabani sorgulari, index, constraint ve relation davranislari yoktur.
- Buyuk veri icin uygun degildir.

Bu sinirlar bu proje icin kabul edilebilir, cunku proje Spring Core katmanlarini ve temiz sorumluluk ayrimini calistirmaya odaklaniyor.

## Bu Projede Nerede?

Bu konuyu anlamak icin takip edilecek ana dosyalar:

```text
src/main/java/com/example/gymcrm/config/StorageConfig.java
src/main/java/com/example/gymcrm/config/StorageBeanNames.java
src/main/java/com/example/gymcrm/initializer/StorageBeanPostProcessor.java
src/main/java/com/example/gymcrm/dao/TraineeDao.java
src/main/java/com/example/gymcrm/dao/TrainerDao.java
src/main/java/com/example/gymcrm/dao/TrainingDao.java
src/main/java/com/example/gymcrm/dao/impl/AbstractMapDao.java
src/main/java/com/example/gymcrm/dao/impl/TraineeDaoImpl.java
src/main/java/com/example/gymcrm/dao/impl/TrainerDaoImpl.java
src/main/java/com/example/gymcrm/dao/impl/TrainingDaoImpl.java
src/main/java/com/example/gymcrm/exception/EntityNotFoundException.java
src/test/java/com/example/gymcrm/dao/impl/MapDaoTest.java
src/test/java/com/example/gymcrm/logging/LoggingSafetyTest.java
src/test/java/com/example/gymcrm/config/SpringContextIntegrationTest.java
```

### Storage bean'leri

`StorageConfig` uc ayri map bean'i uretir:

```java
@Bean(StorageBeanNames.TRAINEE_STORAGE)
public Map<Long, Trainee> traineeStorage() {
    return new HashMap<>();
}
```

Ayni sekilde trainer ve training storage bean'leri de vardir.

Bu uc bean'in ayri olmasi cok onemlidir. Trainee kayitlari trainer map'ine, training kayitlari trainee map'ine karismamalidir.

### Bean isimleri

`StorageBeanNames` sabitleri string typo riskini azaltir:

```java
public static final String TRAINEE_STORAGE = "traineeStorage";
public static final String TRAINER_STORAGE = "trainerStorage";
public static final String TRAINING_STORAGE = "trainingStorage";
```

Bu sabitler hem `StorageConfig` hem de DAO setter injection tarafinda kullanilir.

### Initial data

`StorageBeanPostProcessor`, map bean'leri olustuktan sonra initial data dosyasindan gelen verileri dogru map'e koyar:

```text
traineeStorage -> Trainee listesi
trainerStorage -> Trainer listesi
trainingStorage -> Training listesi
```

Bu nokta sequence icin onemli:

1. Spring map bean'ini olusturur.
2. `StorageBeanPostProcessor` map'i initial data ile doldurur.
3. DAO, setter injection ile bu map'i alir.
4. `AbstractMapDao.initializeStorage()` mevcut en buyuk ID'yi bulup sequence'i oradan baslatir.

Bu yuzden initial data'da ID `1` varsa yeni create edilen entity `2` ile devam eder.

## Kod Akisi

Bu bolumde bir trainee create islemini adim adim izleyelim.

### 1. StorageConfig map bean'i olusturur

```text
StorageConfig.traineeStorage()
  -> new HashMap<Long, Trainee>()
```

Spring bu map'i `traineeStorage` adi ile application context'e koyar.

### 2. StorageBeanPostProcessor initial data'yi yukler

`StorageBeanPostProcessor.postProcessAfterInitialization()` gelen bean'in bir `Map` olup olmadigini kontrol eder. Bean adi `traineeStorage` ise trainee verilerini yukler.

Bu islemde:

- ID null olamaz.
- ID pozitif olmalidir.
- Gerekli alanlar bos olamaz.
- Ayni ID iki kere gelirse hata atilir.

Hata durumunda `StorageInitializationException` kullanilir.

### 3. TraineeDaoImpl dogru map'i alir

`TraineeDaoImpl` su setter ile storage'i alir:

```java
@Autowired
public void setStorage(@Qualifier(StorageBeanNames.TRAINEE_STORAGE) Map<Long, Trainee> storage) {
    initializeStorage(storage);
}
```

Burada iki Spring kavrami birlikte kullanilir:

- `@Autowired`: Spring bu setter'i dependency injection icin cagirir.
- `@Qualifier`: Ayni turden birden fazla `Map` bean'i oldugu icin hangisinin verilecegini netlestirir.

Neden `@Qualifier` gerekli?

Cunku context icinde en az uc `Map` bean'i vardir:

```text
Map<Long, Trainee>
Map<Long, Trainer>
Map<Long, Training>
```

Java generic bilgisi runtime'da sinirli oldugu ve Spring icinde ayni raw type olan `Map` icin birden fazla aday bulundugu icin bean ismiyle dogru storage secilir.

### 4. AbstractMapDao storage'i saklar ve sequence'i ayarlar

`initializeStorage()` metodu iki is yapar:

```java
this.storage = storage;
sequence = storage.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
```

Anlami:

- DAO artik hangi map uzerinde calisacagini bilir.
- Mevcut ID'ler icinde en buyuk olan bulunur.
- Yeni create islemi bu en buyuk ID'den sonra devam eder.

Ornek:

```text
storage key'leri: 1, 5, 9
sequence = 9
sonraki create id = 10
```

Storage bos ise:

```text
sequence = 0
sonraki create id = 1
```

### 5. Service create cagrisini DAO'ya yollar

`TraineeServiceImpl.create()` validasyon, username ve password uretiminden sonra yeni bir `Trainee` nesnesi olusturur. Bu nesnenin ID'si baslangicta `null` olur.

Sonra:

```java
return traineeDao.create(trainee);
```

### 6. TraineeDaoImpl ortak createEntity metodunu kullanir

`TraineeDaoImpl.create()` sadece su isi yapar:

```java
return createEntity(trainee);
```

Asil generic logic `AbstractMapDao` icindedir:

```java
long id = ++sequence;
setId(entity, id);
storage.put(id, entity);
```

Bu siralama onemlidir:

1. Sequence bir artirilir.
2. Entity'nin ID alani set edilir.
3. Entity map'e eklenir.
4. Log yazilir.

### 7. findById Optional doner

`findEntity()` su sekilde calisir:

```java
return Optional.ofNullable(storage.get(id));
```

Map'te kayit varsa:

```text
Optional[entity]
```

Kayit yoksa:

```text
Optional.empty()
```

Bu sayede DAO `null` dondurmek yerine bulunamama durumunu method imzasinda acikca gosterir.

### 8. update var olmayan ID icin hata atar

`updateEntity()` once entity'den ID'yi alir. Sonra `storage.replace(id, entity)` kullanir.

`replace` davranisi onemli:

- ID varsa value degisir ve eski value doner.
- ID yoksa `null` doner.

Bu projede ID yoksa:

```java
throw new EntityNotFoundException(entityType(), id);
```

Yani update, "yoksa create et" davranisi gostermez. Sadece var olan kaydi gunceller.

### 9. delete var olmayan ID icin hata atar

`deleteEntity()` su mantikla calisir:

```java
if (id == null || storage.remove(id) == null) {
    throw new EntityNotFoundException(entityType(), id);
}
```

Yani:

- ID null ise hata.
- ID yoksa hata.
- ID varsa kayit silinir.

Bu davranis `TraineeDaoImpl` icin vardir. Trainer ve Training DAO interface'lerinde delete operasyonu bulunmaz.

### 10. findAll ID'ye gore sirali liste doner

`findAllEntities()` map value'larini alir, ID'ye gore siralar ve listeye cevirir:

```java
storage.values().stream()
        .sorted(Comparator.comparing(this::getId))
        .toList();
```

`HashMap` kendi basina sirali degildir. Bu nedenle list sonucunun stabil olmasi icin ID siralamasi yapilir.

Bu test yazarken de onemlidir. Sirali liste sayesinde testlerde:

```java
containsExactly(5L, 6L)
```

gibi net beklentiler kurulabilir.

## Kisa Kod Okuma Notlari

### AbstractMapDao

Dosya:

```text
src/main/java/com/example/gymcrm/dao/impl/AbstractMapDao.java
```

Bu sinif package-private tanimlanmistir:

```java
abstract class AbstractMapDao<T>
```

Yani sadece ayni package icinden gorulur. Dis katmanlar bu sinifa baglanmaz. Service katmani DAO interface'lerine baglanir.

Temel alanlar:

```java
private Map<Long, T> storage;
private long sequence;
```

Bu sinif:

- Storage referansini tutar.
- ID sequence degerini tutar.
- Create, update, delete, find, findAll davranisini ortaklastirir.
- Entity tipine ozel ID okuma/yazma isini alt siniflara birakir.

Alt siniflar su uc metodu implemente etmek zorundadir:

```java
protected abstract String entityType();
protected abstract Long getId(T entity);
protected abstract void setId(T entity, Long id);
```

Bu tasarim sayesinde `AbstractMapDao` generic kalir. `Trainee`, `Trainer` ve `Training` siniflarinin ID alanina nasil ulasilacagini bilmek zorunda kalmaz.

### TraineeDaoImpl

Dosya:

```text
src/main/java/com/example/gymcrm/dao/impl/TraineeDaoImpl.java
```

Ozellikleri:

- `@Repository` ile Spring bean olur.
- `TraineeDao` interface'ini uygular.
- `AbstractMapDao<Trainee>` sinifindan ortak CRUD davranisini alir.
- `deleteById` destekler.
- Storage olarak `traineeStorage` bean'ini alir.

Bu sinifta dikkat edilecek sey sudur: CRUD methodlari neredeyse hic is kurali icermez. Sadece interface methodlarini ortak generic methodlara yonlendirir.

### TrainerDaoImpl

Dosya:

```text
src/main/java/com/example/gymcrm/dao/impl/TrainerDaoImpl.java
```

Ozellikleri:

- `TrainerDao` interface'ini uygular.
- `AbstractMapDao<Trainer>` sinifindan ortak davranisi alir.
- Create, update, findById ve findAll destekler.
- Delete operasyonu yoktur, cunku `TrainerDao` interface'inde tanimli degildir.
- Storage olarak `trainerStorage` bean'ini alir.

`entityType()` metodu `"Trainer"` doner. Bu deger logging ve exception mesajlarinda kullanilir.

### TrainingDaoImpl

Dosya:

```text
src/main/java/com/example/gymcrm/dao/impl/TrainingDaoImpl.java
```

Ozellikleri:

- `TrainingDao` interface'ini uygular.
- `AbstractMapDao<Training>` sinifindan ortak davranisi alir.
- Create, findById ve findAll destekler.
- Update ve delete operasyonu yoktur.
- Storage olarak `trainingStorage` bean'ini alir.

Training icin trainee ve trainer var mi kontrolu DAO'da yapilmaz. Bu kontrol `TrainingServiceImpl.create()` icindedir:

```text
traineeDao.findById(traineeId)
trainerDao.findById(trainerId)
```

Yani DAO sadece storage operasyonunu yapar; is kurali service tarafinda kalir.

### EntityNotFoundException

Dosya:

```text
src/main/java/com/example/gymcrm/exception/EntityNotFoundException.java
```

Mesaj formati:

```text
{EntityType} was not found for id={id}
```

Bu exception su durumlarda gorulur:

- DAO update etmek istedigi ID'yi storage'da bulamazsa.
- DAO delete etmek istedigi ID'yi storage'da bulamazsa.
- Service, update veya training create sirasinda ilgili entity'yi bulamazsa.

DAO seviyesinde ozellikle update/delete icin anlamlidir. Cunku find operasyonu hata atmak yerine `Optional.empty()` doner.

### Logging

`AbstractMapDao` her alt sinif icin logger'i dinamik olarak alir:

```java
private final Logger logger = LoggerFactory.getLogger(getClass());
```

Bu sayede log kaynagi `TraineeDaoImpl`, `TrainerDaoImpl` veya `TrainingDaoImpl` olarak gorunebilir.

Log davranisi:

- Create: `info`
- Update: `info`
- Delete: `info`
- Missing update/delete: `warn`
- Find: `debug`
- Find all: `debug`

Loglarda hassas alanlar yazilmaz. Mesela password, address veya dateOfBirth loga basilmamalidir. `LoggingSafetyTest` bunu garanti etmeye calisir.

## Sik Dusulen Hatalar

### 1. DAO icine service validasyonu koymak

DAO'nun gorevi "firstName bos mu?" gibi validasyonlar yapmak degildir. Bu kontroller service katmaninda yapilir.

DAO su seviyede kalmalidir:

- Kayit var mi?
- ID atanacak mi?
- Map'e eklenecek mi?
- Map'ten silinecek mi?

### 2. `@Qualifier` kullanmayi unutmak

Projede ayni turden birden fazla `Map` bean'i oldugu icin Spring dogru storage'i kendiliginden secemeyebilir.

Yanlis baglama ornegi:

```java
@Autowired
public void setStorage(Map<Long, Trainee> storage) {
    initializeStorage(storage);
}
```

Dogru baglama:

```java
@Autowired
public void setStorage(@Qualifier(StorageBeanNames.TRAINEE_STORAGE) Map<Long, Trainee> storage) {
    initializeStorage(storage);
}
```

### 3. HashMap'in sirali oldugunu varsaymak

`HashMap` insertion order veya ID order garantisi vermez. Bu yuzden `findAllEntities()` ID'ye gore sort eder.

### 4. Update sirasinda `put` kullanmak

`put` kullanilsaydi olmayan ID ile update yapmak yeni kayit yaratabilirdi. Bu projede update sadece var olan kayit icin gecerlidir. Bu yuzden `replace` kullanilir.

### 5. `Optional.get()` ile dogrudan acmak

DAO `Optional<T>` dondugunde cagrilan tarafta once varlik kontrolu yapilmalidir.

Daha guvenli kullanim:

```java
Trainee trainee = traineeDao.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Trainee", id));
```

### 6. Sequence'in initial data'yi dikkate almasini unutmak

`sequence` sifirdan baslamaz. Storage icindeki en buyuk ID neyse oradan devam eder.

Bu yuzden initial data'da trainee ID'si `1` ise yeni trainee ID'si `2` olur.

### 7. Map storage'in kalici oldugunu sanmak

Bu storage uygulama bellegindedir. Uygulama yeniden baslayinca runtime'da eklenen veriler kaybolur. Sadece initial data tekrar yuklenir.

### 8. Logging'de entity'nin tamamini yazmak

Entity'nin tamamini loglamak password gibi hassas alanlari sizdirabilir. Bu projedeki loglar sadece entity type, ID ve count gibi guvenli bilgileri yazar.

## Kendini Test Et

1. DAO pattern'in temel amaci nedir? Bu projede hangi problemi cozer?
2. `TraineeServiceImpl` neden dogrudan `Map<Long, Trainee>` kullanmiyor?
3. `StorageConfig` icinde neden uc ayri `HashMap` bean'i var?
4. `@Qualifier(StorageBeanNames.TRAINEE_STORAGE)` kaldirilirsa hangi problem ortaya cikabilir?
5. `AbstractMapDao.initializeStorage()` sequence degerini nasil hesaplar?
6. Initial data'da en buyuk trainee ID'si `7` ise yeni trainee create edilince ID ne olur?
7. `findById()` neden `null` yerine `Optional<T>` donuyor?
8. `updateEntity()` neden `storage.put(id, entity)` yerine `storage.replace(id, entity)` kullaniyor?
9. `findAllEntities()` neden ID'ye gore sort ediyor?
10. `EntityNotFoundException` DAO katmaninda hangi operasyonlarda atilir?
11. `TrainingDaoImpl` neden update/delete operasyonu sunmuyor?
12. `LoggingSafetyTest` DAO logging davranisi hakkinda neyi koruyor?

## Mini Pratik

1. `MapDaoTest.traineeDaoSupportsFullCrudAndContinuesSeededSequence()` testini kagit ustunde adim adim takip et. Storage baslangicta hangi key'lere sahip, create sonrasi sequence ve key'ler ne oluyor?
2. `TrainerDaoImpl` icin storage'a onceden `10L` ID'li bir trainer koydugunu varsay. Sonraki `create()` cagrisinda beklenen ID'yi tahmin et ve nedenini acikla.
3. `AbstractMapDao.updateEntity()` metodunda `replace` yerine `put` kullanilsaydi `traineeDaoRejectsMissingUpdatesAndDeletes()` testi nasil etkilenirdi?
4. `findAllEntities()` icindeki `sorted(...)` satirini zihinsel olarak kaldir. `HashMap` ile testlerde hangi tur belirsizlikler cikabilir?
5. `TraineeDaoImpl.setStorage()` metodundaki qualifier'i bilerek yanlis bean adiyla degistirdigini varsay. Uygulama davranisinda ne tur karisikliklar beklenir?

## Mulakat / Sinav Sorulari

1. DAO pattern ile Repository pattern arasinda nasil bir benzerlik vardir? Bu projede `@Repository` ne ifade eder?
2. In-memory storage kullanan bir uygulamada veri kaliciligi problemi nasil cozulur?
3. `HashMap` thread-safe olmadigi icin cok kullanicili bir ortamda hangi riskler dogar?
4. Generic bir abstract DAO yazmanin faydasi nedir? Dezavantaji olabilir mi?
5. `Optional<T>` donmek ile exception atmak arasindaki tasarim farki nedir?
6. Bir update operasyonunda "kayit yoksa hata" davranisini nasil garanti edersin?
7. Spring'de ayni turden birden fazla bean varsa dogru bean nasil secilir?
8. Loglama yaparken neden entity'nin tamamini loglamak risklidir?
9. `findAll()` sonucunun sirali olmasi API kullanicisi ve testler icin neden onemlidir?
10. Bu DAO katmanini ileride JPA'ya tasimak istesen hangi katmanlar daha az etkilenir?

## Ozet

Bu projede DAO katmani, service katmanini storage detayindan ayirir. Service is kurallarini ve validasyonu yonetirken, DAO map uzerinden veri erisimini yapar.

`StorageConfig` uc ayri `HashMap` bean'i uretir. `StorageBeanPostProcessor` bu map'leri initial data ile doldurur. DAO implementasyonlari setter injection ve `@Qualifier` ile dogru map'i alir.

`AbstractMapDao<T>` ortak CRUD davranisini toplar:

- `createEntity()` yeni ID uretir, entity'ye set eder ve map'e koyar.
- `updateEntity()` sadece var olan ID'yi gunceller.
- `deleteEntity()` sadece var olan ID'yi siler.
- `findEntity()` `Optional<T>` doner.
- `findAllEntities()` ID'ye gore sirali liste doner.

`TraineeDaoImpl`, `TrainerDaoImpl` ve `TrainingDaoImpl` ise bu generic davranisi kendi entity tiplerine baglar. Her biri sadece kendi storage bean'ini kullanir ve kendi interface'inin sundugu operasyonlari disari acar.

Bu katmani iyi anlamak, projenin geri kalanini okumayi kolaylastirir. Cunku service testlerinde ve facade akisyonunda gordugun bircok davranisin en alt veri erisim karsiligi burada bulunur.
