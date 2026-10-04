# Domain Modeli ve Java Modelleme Tercihleri

## Bu Konunun Amaci

Bu notun amaci, projedeki domain modelini sadece sinif isimleri olarak degil, Java modelleme kararlari olarak okumayi saglamaktir.

Bu bolumun sonunda sunlari netlestirmis olmalisin:

- `User` neden `abstract class` olarak tasarlandi?
- `Trainee` ve `Trainer` neden `User` sinifindan kalitim aliyor?
- `Training` neden ayri bir entity ve neden `traineeId` / `trainerId` ile iliski kuruyor?
- `TrainingType` enum kullanimi hangi problemi cozer?
- Getter/setter, no-args constructor ve mutable domain modeli Jackson ile nasil calisir?
- Record command nesneleri domain nesnelerinden neden ayridir?
- `toString()` icinde hassas veri gostermeme tercihi neden onemlidir?
- `DomainModelTest` bu kararlarin hangilerini guvence altina alir?

## Buyuk Resim

Bu projede domain modeli, spor salonu CRM sisteminin en temel kavramlarini temsil eder:

- `Trainee`: egitim alan musteri / ogrenci.
- `Trainer`: egitmen.
- `Training`: belirli bir egitmen ve belirli bir trainee arasindaki antrenman kaydi.
- `TrainingType`: antrenman veya uzmanlik turu.
- `User`: `Trainee` ve `Trainer` icin ortak kimlik ve giris bilgilerini tasiyan soyut taban sinif.

Domain modeli uygulamanin merkezindedir. DAO katmani bu nesneleri bellekteki `Map` yapilarinda saklar, service katmani bu nesneleri olusturur ve gunceller, facade katmani ise disaridan kullanimi kolaylastirir.

Bu projede modelleme bilincli olarak basit tutulmustur:

- JPA entity yoktur.
- Veritabani yoktur.
- Nesneler bellekte saklanir.
- Iliskiler object reference yerine `Long id` ile tutulur.
- Domain siniflari Java bean stilindedir.

Bu sadelik, Spring Core, DAO, service ve test akisini daha rahat calismak icin iyi bir zemin olusturur.

## OOP ve Modelleme Kavramlari

### Entity

Entity, kimligi olan ve zaman icinde durumu degisebilen nesnedir. Bu projede `id` alanina sahip siniflar entity gibi davranir.

Ornekler:

- `Trainee`
- `Trainer`
- `Training`

Bir trainee'nin adresi degisebilir ama `id` ayni kaldigi surece sistem acisindan ayni trainee'dir.

### Abstraction

Abstraction, ortak davranis veya ortak veriyi daha genel bir modelde toplama fikridir.

Bu projede `User`, dogrudan kullanilacak bir kisi tipi degildir. `Trainee` ve `Trainer` icin ortak alanlari tasir:

```java
private Long id;
private String firstName;
private String lastName;
private String username;
private String password;
private boolean active;
```

Bu nedenle `User` sinifi `abstract` yapilmistir. Yani:

- `new User(...)` ile nesne uretilmez.
- `Trainee` ve `Trainer` bu ortak modeli genisletir.
- Ortak getter/setter ve `toString()` davranisi tek yerde tutulur.

### Inheritance

Inheritance, bir sinifin baska bir sinifin alanlarini ve davranisini miras almasidir.

Bu projede:

```java
public final class Trainee extends User
public final class Trainer extends User
```

`Trainee` ve `Trainer`, `User` alanlarini tekrar yazmaz. Bunun yerine kendi ozel alanlarini ekler:

- `Trainee`: `dateOfBirth`, `address`
- `Trainer`: `specialization`

Kalitim burada mantiklidir cunku iki sinif da kullanici benzeri ortak ozelliklere sahiptir. Ancak her ikisi de sistemde farkli kavramlardir.

### Encapsulation

Encapsulation, alanlari dogrudan disariya acmak yerine kontrollu erisim saglamaktir.

Bu projede domain alanlari `private` tutulur ve public getter/setter metodlari ile erisilir:

```java
private String firstName;

public String getFirstName() {
    return firstName;
}

public void setFirstName(String firstName) {
    this.firstName = firstName;
}
```

Bu yapi Java bean stilidir. Bu stil hem testlerde kolay okunur hem de Jackson gibi kutuphanelerin nesne olusturup alanlari doldurmasini kolaylastirir.

### Composition ve Association

Composition, bir nesnenin baska parcalardan olusmasidir. Bu projede `Training`, bir antrenman kaydini olusturan alanlari bir araya getirir:

- `id`
- `traineeId`
- `trainerId`
- `name`
- `trainingType`
- `date`
- `durationMinutes`

Dikkat edilmesi gereken nokta: `Training` sinifi icinde `Trainee trainee` veya `Trainer trainer` nesnesi tutulmaz. Bunun yerine `Long traineeId` ve `Long trainerId` tutulur.

Bu tercih su avantajlari saglar:

- Bellekteki `Map<Long, Trainee>` ve `Map<Long, Trainer>` yapilariyla uyumludur.
- Nesneler arasinda karmasik referans grafikleri olusmaz.
- JSON initial data daha sade kalir.
- DAO ve service validasyon akisi daha net olur.

Bu nedenle burada "Training, Trainee ve Trainer nesnelerini gomulu olarak tasir" demek dogru olmaz. Daha dogru ifade sudur: `Training`, trainee ve trainer ile id uzerinden iliski kuran ayri bir entity'dir.

### Mutability

Mutable nesne, olustuktan sonra alanlari degisebilen nesnedir. Bu projedeki domain siniflari mutable'dir.

Ornek:

```java
trainee.setFirstName("Alice");
trainee.setActive(true);
```

Bu tercih, Java bean stili ve Jackson deserialization ile uyumludur. Ancak mutable model kullanirken service katmaninin validasyon sorumlulugu daha onemli hale gelir. Cunku herhangi bir setter teorik olarak gecersiz deger yazabilir.

## Bu Projede Nerede?

Domain modeli su pakettedir:

- `src/main/java/com/example/gymcrm/domain/User.java`
- `src/main/java/com/example/gymcrm/domain/Trainee.java`
- `src/main/java/com/example/gymcrm/domain/Trainer.java`
- `src/main/java/com/example/gymcrm/domain/Training.java`
- `src/main/java/com/example/gymcrm/domain/TrainingType.java`

Command nesneleri su pakettedir:

- `src/main/java/com/example/gymcrm/service/command/CreateTraineeCommand.java`
- `src/main/java/com/example/gymcrm/service/command/UpdateTraineeCommand.java`
- `src/main/java/com/example/gymcrm/service/command/CreateTrainerCommand.java`
- `src/main/java/com/example/gymcrm/service/command/UpdateTrainerCommand.java`
- `src/main/java/com/example/gymcrm/service/command/CreateTrainingCommand.java`

Domain modelini guvence altina alan test:

- `src/test/java/com/example/gymcrm/domain/DomainModelTest.java`

Initial data akisi icin ilgili dosyalar:

- `config/initial-data.json`
- `src/main/java/com/example/gymcrm/initializer/InitialData.java`
- `src/main/java/com/example/gymcrm/initializer/StorageBeanPostProcessor.java`

## Entity Iliskileri

Projede domain iliskilerini soyle okuyabilirsin:

```text
User (abstract)
  |
  +-- Trainee
  |     - dateOfBirth
  |     - address
  |
  +-- Trainer
        - specialization: TrainingType

Training
  - traineeId: Long
  - trainerId: Long
  - trainingType: TrainingType
```

`Trainee` ve `Trainer`, `User` sinifindan kalitim alir.

`Training`, `User` sinifindan kalitim almaz. Cunku training bir kullanici degildir; bir etkinlik / kayit entity'sidir.

`Training`, trainee ve trainer ile id uzerinden iliski kurar:

```java
private Long traineeId;
private Long trainerId;
```

Bu iliski service katmaninda kontrol edilir. `TrainingServiceImpl.create(...)`, training kaydi olusturmadan once ilgili trainee ve trainer var mi diye DAO uzerinden bakar.

`TrainingType` iki yerde kullanilir:

- `Trainer.specialization`: egitmenin uzmanlik alani.
- `Training.trainingType`: antrenmanin turu.

## Kod Akisi

### 1. Initial data dosyasindan domain nesnesine

Baslangic verisi `config/initial-data.json` icindedir. Ornegin trainee verisi JSON olarak su alanlari tasir:

```json
{
  "id": 1,
  "firstName": "Alice",
  "lastName": "Brown",
  "username": "Alice.Brown",
  "password": "A1b2C3d4E5",
  "active": true,
  "dateOfBirth": "1995-04-12",
  "address": "Sample address"
}
```

`StorageBeanPostProcessor`, Jackson `ObjectMapper` kullanarak bu JSON'u `InitialData` record nesnesine okur. `InitialData` icinde `List<Trainee>`, `List<Trainer>` ve `List<Training>` vardir.

Jackson'in bunu yapabilmesi icin domain siniflarinda no-args constructor ve setter metodlari bulunur:

```java
public Trainee() {
}
```

Jackson genel olarak su sekilde calisir:

1. Bos nesne olusturur.
2. JSON alanlarini setter'lar uzerinden doldurur.
3. Ortaya domain nesnesi cikar.

`LocalDate` alanlari icin `StorageBeanPostProcessor` icinde `JavaTimeModule` register edilir. Bu sayede `"1995-04-12"` gibi tarih string'leri `LocalDate` olarak okunabilir.

### 2. CreateTraineeCommand'den Trainee entity'sine

Yeni trainee olusturma akisi soyledir:

1. Disaridan `CreateTraineeCommand` gelir.
2. `TraineeServiceImpl.create(...)` command nesnesini validate eder.
3. `UsernameGenerator` ile username uretilir.
4. `PasswordGenerator` ile password uretilir.
5. Yeni `Trainee` domain nesnesi constructor ile kurulur.
6. `TraineeDao.create(...)` nesneyi storage'a kaydeder.

Burada command nesnesi ile domain nesnesi ayni sey degildir.

Command:

- Dis girdiyi temsil eder.
- Record oldugu icin compact ve immutable'dir.
- `id`, `username`, `password` gibi sistem tarafindan uretilen alanlari tasimaz.

Domain:

- Sistemin sakladigi asil entity'dir.
- `id`, `username`, `password`, `active` gibi kalici alanlari tasir.
- DAO tarafindan saklanir.

### 3. CreateTrainerCommand'den Trainer entity'sine

Trainer olusturma akisi trainee'ye benzer:

1. `CreateTrainerCommand` gelir.
2. `firstName`, `lastName`, `specialization` validate edilir.
3. Username ve password uretilir.
4. `Trainer` domain nesnesi olusturulur.
5. DAO kaydi yapar.

`specialization` alani `TrainingType` enum oldugu icin rastgele string kabul edilmez. Kod tarafinda sadece enum sabitlerinden biri kullanilabilir.

### 4. CreateTrainingCommand'den Training entity'sine

Training olusturma akisi biraz farklidir:

1. `CreateTrainingCommand` gelir.
2. `traineeId`, `trainerId`, `name`, `trainingType`, `date`, `durationMinutes` validate edilir.
3. `TraineeDao.findById(...)` ile trainee var mi kontrol edilir.
4. `TrainerDao.findById(...)` ile trainer var mi kontrol edilir.
5. Yeni `Training` nesnesi olusturulur.
6. `TrainingDao.create(...)` ile kaydedilir.

Bu akista `Training` nesnesi, trainee ve trainer nesnelerini dogrudan icine almaz. Sadece id'lerini tasir. Bu nedenle varlik kontrolu service katmaninda yapilir.

### 5. Update akisi

Update command nesneleri de record'dur:

- `UpdateTraineeCommand`
- `UpdateTrainerCommand`

Update sirasinda service once ilgili entity'yi DAO'dan bulur. Sonra setter'lar ile degistirilebilir alanlari gunceller.

Ornegin trainee update akisi:

```text
id + UpdateTraineeCommand
  -> id pozitif mi?
  -> command null mi?
  -> firstName, lastName, dateOfBirth, address gecerli mi?
  -> DAO'dan mevcut Trainee bulunur
  -> setter'lar ile alanlar guncellenir
  -> DAO update edilir
```

## Kisa Kod Okuma Notlari

### User.java

`User`, ortak kullanici alanlarini tasir:

- `id`
- `firstName`
- `lastName`
- `username`
- `password`
- `active`

Sinif `abstract` oldugu icin dogrudan kullanici tipi degildir. Bu, modelde "sistemde sadece user diye bir entity yok; trainee ve trainer var" mesajini verir.

No-args constructor `protected` olarak tanimlidir. Alt siniflar bu constructor'i kullanabilir. `Trainee` ve `Trainer` ise Jackson ve test kolayligi icin public no-args constructor saglar.

`toString()` sadece sinif adi, `id` ve `active` bilgisini yazar:

```java
return getClass().getSimpleName() + "{id=" + id + ", active=" + active + '}';
```

Bu tercih bilerek yapilmistir. `username`, `password`, `firstName`, `lastName` gibi bilgiler loglarda veya hata mesajlarinda gorunmemelidir.

### Trainee.java

`Trainee`, `User` sinifini genisletir ve iki alan ekler:

- `dateOfBirth`
- `address`

Sinif `final` oldugu icin baska siniflar `Trainee`'den kalitim alamaz. Bu, domain modelinin gereksiz sekilde genislemesini engeller ve "Trainee en son somut tiptir" anlamina gelir.

### Trainer.java

`Trainer`, `User` sinifini genisletir ve `specialization` alanini ekler.

`specialization` tipi `TrainingType` enum'dur. Bu sayede egitmen uzmanligi belli bir liste ile sinirlanir.

### Training.java

`Training`, kendi basina ayri bir entity'dir. `User` ile kalitim iliskisi yoktur.

Alanlari:

- `id`: training kaydinin kimligi.
- `traineeId`: egitim alan kisinin id'si.
- `trainerId`: egitmenin id'si.
- `name`: training adi.
- `trainingType`: training turu.
- `date`: training tarihi.
- `durationMinutes`: dakika cinsinden sure.

`toString()` sadece `id`, `traineeId` ve `trainerId` bilgisini yazar. `name` gibi detaylar ciktiya alinmaz.

### TrainingType.java

Enum sabitleri:

```java
FITNESS,
YOGA,
CARDIO,
STRENGTH,
STRETCHING
```

Enum kullanmak, string bazli hatalari azaltir. Ornegin `"Yoga"`, `"YOGA"`, `"yoga"` gibi farkli yazimlar yerine kod tarafinda tek bir `TrainingType.YOGA` sabiti kullanilir.

JSON icinde enum degerleri string olarak yazilir:

```json
"trainingType": "FITNESS"
```

Jackson bu string'i enum sabitine map eder. Deger enum'da yoksa okuma islemi hata verir.

### Command Record'lari

Command nesneleri domain paketi icinde degil, service command paketi icindedir.

Record kullanimi su avantajlari saglar:

- Daha az boilerplate kod.
- Immutable veri tasiyici.
- Constructor, accessor, `equals`, `hashCode`, `toString` otomatik uretilir.
- Service metoduna gelen girdiyi sade sekilde temsil eder.

Ornek:

```java
public record CreateTrainingCommand(
        Long traineeId,
        Long trainerId,
        String name,
        TrainingType trainingType,
        LocalDate date,
        int durationMinutes) {
}
```

Record command nesneleri, domain entity yerine gecmez. Domain entity kalici modeldir; command sadece bir islem istegidir.

### DomainModelTest.java

`DomainModelTest`, domain siniflarinin iki temel davranisini kontrol eder:

1. Getter/setter metodlari alanlari dogru okur ve yazar.
2. `toString()` hassas verileri ciktiya eklemez.

Trainee icin test sunlari kontrol eder:

- `id`, `firstName`, `lastName`, `username`, `password`, `active`, `dateOfBirth`, `address` getter/setter akisi.
- `toString()` icinde `secret1234`, `Private address`, `Alice.Brown` gecmemesi.
- Trainer specialization alaninin `TrainingType.STRENGTH` olarak okunabilmesi.

Training icin test sunlari kontrol eder:

- `id`, `traineeId`, `trainerId`, `name`, `trainingType`, `date`, `durationMinutes` getter/setter akisi.
- `toString()` icinde private session name bilgisinin gecmemesi.

Bu testler basit gorunur ama modelleme kararlarini sabitler. Yarin biri `toString()` metoduna password eklerse test bunu yakalar.

## Sik Dusulen Hatalar

### 1. User'i somut entity gibi dusunmek

`User` tek basina sistemde kaydedilen bir tip degildir. Ortak alanlari toplamak icin soyut taban siniftir.

Dogru okuma:

```text
Trainee is a User
Trainer is a User
Training is not a User
```

### 2. Training icine Trainee ve Trainer nesnesi koymaya calismak

Bu projede `Training` iliskileri id ile kurar. `Training` icine `Trainee` ve `Trainer` nesnesi koymak basit Map storage modelini karmasiklastirir.

### 3. Enum yerine string kullanmak

`TrainingType` yerine string kullanmak yazim hatalarina acik olur. Enum, izin verilen degerleri kod seviyesinde sinirlar.

### 4. No-args constructor'i gereksiz sanmak

No-args constructor, Jackson'in JSON'dan nesne uretmesi icin onemlidir. Testlerde de bos nesne uretip setter ile alan doldurmayi kolaylastirir.

### 5. Setter oldugu icin validasyonun domain icinde oldugunu varsaymak

Domain siniflarinda setter'lar degerleri dogrudan yazar. Bu projede validasyonun ana yeri service katmanidir.

### 6. Record command ile domain entity'yi karistirmak

`CreateTraineeCommand` kalici entity degildir. Sadece create isteginde gelen bilgileri tasir. `Trainee` ise sistemde saklanan domain nesnesidir.

### 7. toString icinde fazla veri gostermek

`toString()` genelde loglarda gorunur. Password, address, username, ad-soyad veya ozel session name gibi verileri buraya eklemek guvenlik ve gizlilik riskidir.

### 8. Boolean getter ismini karistirmak

`active` boolean oldugu icin getter metodu `getActive()` degil `isActive()` olarak tanimlanmistir. Bu Java bean konvansiyonudur.

## Kendini Test Et

1. `User` sinifi neden `abstract` yapilmis olabilir? Bu karar hangi modelleme mesajini verir?
2. `Trainee` ve `Trainer` siniflarinin `final` olmasi ne anlama gelir?
3. `Training` neden `User` sinifindan kalitim almamalidir?
4. `Training` sinifi neden `Trainee` ve `Trainer` nesnelerini degil de `traineeId` ve `trainerId` alanlarini tutuyor?
5. `TrainingType` enum kullanmak string kullanmaya gore hangi hatalari engeller?
6. Jackson icin no-args constructor ve setter metodlari neden islevseldir?
7. Record command nesneleri ile domain entity nesneleri arasindaki temel fark nedir?
8. `toString()` icinde password yazmak neden tehlikelidir?
9. `DomainModelTest` sadece getter/setter testi gibi gorunse de hangi tasarim kararlarini korur?
10. `CreateTrainingCommand` icindeki `durationMinutes` degeri nerede validate edilir?

## Mini Pratik

1. `DomainModelTest` dosyasini ac ve `Trainee.toString()` ciktisinin `firstName` icermedigini kontrol eden yeni bir assertion eklemeyi dene. Sonra testin neden anlamli oldugunu kendi cumlenle yaz.

2. `TrainingType` enum'una teorik olarak `PILATES` eklenseydi hangi dosyalari kontrol etmen gerektigini listele. Kod degistirmeden once JSON, service ve test etkilerini dusun.

3. `CreateTraineeCommand` ile `Trainee` arasindaki alan farklarini tablo halinde yaz. Hangi alanlar kullanicidan gelir, hangileri sistem tarafindan uretilir?

4. `Training` sinifinda `traineeId` yerine `Trainee trainee` tutulsaydi ne gibi avantaj ve dezavantajlar dogardi? En az iki avantaj ve iki dezavantaj yaz.

5. `User.toString()` metoduna yanlislikla `username` eklendigini varsay. Hangi testin kirilmasini beklersin ve neden?

## Mulakat / Sinav Sorulari

1. Java'da `abstract class` ile `interface` arasindaki fark nedir? Bu projede `User` neden interface degil abstract class olarak daha uygundur?

2. Inheritance ve composition arasindaki fark nedir? Bu projede hangi yerlerde inheritance, hangi yerlerde id tabanli association kullanilmistir?

3. Java bean standardi nedir? Getter/setter ve no-args constructor neden framework'ler icin onemlidir?

4. Enum kullanmanin avantajlari ve dezavantajlari nelerdir? Yeni bir training type eklemek hangi etkileri dogurabilir?

5. Mutable domain modeli ile immutable record modeli arasindaki fark nedir? Bu projede neden domain mutable, command nesneleri record olarak tasarlanmis olabilir?

6. `toString()` metodunda hassas veri gostermemek neden bir logging safety konusudur?

7. Domain testleri neyi test etmelidir? Sadece getter/setter test etmek her zaman yeterli midir?

8. `Training` icinde `traineeId` ve `trainerId` kullanmak ile object reference kullanmak arasindaki trade-off nedir?

## Ozet

Bu projedeki domain modeli sade ama bilincli tercihlerden olusur.

`User`, `Trainee` ve `Trainer` icin ortak alanlari tasiyan soyut taban siniftir. `Trainee` ve `Trainer`, bu ortak modeli kalitimla genisletir. `Training` ise bir kullanici degil, ayri bir antrenman kaydi entity'sidir ve trainee/trainer iliskisini id uzerinden kurar.

`TrainingType`, izin verilen antrenman ve uzmanlik turlerini enum ile sinirlar. Domain siniflari Java bean stilinde mutable olarak tasarlanmistir; bu tercih Jackson ile JSON'dan nesne olusturmayi ve testlerde setter/getter ile calismayi kolaylastirir.

Record command nesneleri, service katmanina gelen create/update isteklerini temsil eder. Domain entity'lerin yerine gecmezler; sadece islem girdisi tasirlar.

`toString()` metodlari hassas verileri gostermeyecek sekilde kisitli tutulmustur. `DomainModelTest`, hem alan erisimlerini hem de bu logging safety tercihini korur.
