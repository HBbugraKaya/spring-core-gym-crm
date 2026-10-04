# Service Katmani, Validasyon ve Exception Akisi

## 1. Bu Konunun Amaci

Bu notun amaci, projedeki service katmanini sadece "DAO'yu cagiran siniflar" olarak degil, is kurallarinin toplandigi ana uygulama katmani olarak okumayi saglamaktir.

Bu konuyu bitirdiginde sunlari net olarak aciklayabilmelisin:

- Service layer hangi sorumluluklari alir?
- `create`, `update`, `find`, `delete` akislari hangi sirayla calisir?
- Neden `Create...Command` ve `Update...Command` record'lari kullanilir?
- `ValidationSupport` hangi kontrolleri merkezi hale getirir?
- `ValidationException`, `EntityNotFoundException` ve `GymCrmException` arasindaki iliski nedir?
- Update sirasinda username/password neden korunur?
- Training olustururken trainee ve trainer varligi neden service katmaninda kontrol edilir?
- Service unit testleri hangi davranislari garanti altina alir?

Bu konu, projede "disaridan gelen istek nasil is kuralina donusur?" sorusunun merkezindedir.

## 2. Buyuk Resim

Bu proje klasik bir katmanli mimari kullanir:

```text
Facade / Application
        |
        v
Service layer
        |
        v
DAO layer
        |
        v
In-memory Map storage
```

Service layer bu zincirde en kritik kararlari veren katmandir. DAO veriyi saklar ve getirir; fakat "bu veri gecerli mi?", "bu islem yapilabilir mi?", "hangi entity olusmali?", "hangi exception firlatilmali?" gibi kararlar service tarafindadir.

Bu projede service katmani uc ana bolume ayrilir:

- `TraineeServiceImpl`: trainee profilini olusturur, gunceller, siler ve sorgular.
- `TrainerServiceImpl`: trainer profilini olusturur, gunceller ve sorgular.
- `TrainingServiceImpl`: training olusturur ve training sorgular.

Akis genel olarak soyledir:

```text
Command gelir
  |
  v
Service command'i validate eder
  |
  v
Gerekirse username/password uretir veya iliskili entity varligini kontrol eder
  |
  v
Domain nesnesi olusturulur ya da mevcut nesne guncellenir
  |
  v
DAO'ya create/update/delete/find cagrisi yapilir
  |
  v
Sonuc domain nesnesi, Optional, List veya void olarak doner
```

Burada onemli fikir sudur: Service layer, uygulamanin is senaryolarini temsil eder. DAO sadece teknik veri erisimidir.

## 3. Service Layer Kavramlari

### Service layer nedir?

Service layer, kullanicinin veya facade katmaninin yapmak istedigi islemi uygulamanin is kurallarina ceviren katmandir. Bu projede REST controller yoktur; bu yuzden facade ve application siniflari dis dunyaya yakin katmanlar gibi davranir. Ama is kurallarinin asil sahibi service siniflaridir.

Bir service metodu genelde su sorumluluklara sahiptir:

- Giren parametrelerin bos, null veya gecersiz olup olmadigini kontrol eder.
- Gerekli domain nesnesini olusturur veya mevcut nesneyi bulur.
- Is kurallarini uygular.
- DAO'ya dogru veriyi verir.
- Eksik entity veya gecersiz input durumunda anlamli exception firlatir.

### Command record'lari neden var?

Projede create ve update islemleri icin command record'lari kullanilir:

- `CreateTraineeCommand`
- `UpdateTraineeCommand`
- `CreateTrainerCommand`
- `UpdateTrainerCommand`
- `CreateTrainingCommand`

Command record, service metoduna giren ham istegi temsil eder. Ornegin trainee olusturmak icin kullanicidan gelen alanlar sunlardir:

```java
new CreateTraineeCommand(firstName, lastName, dateOfBirth, address, active)
```

Bu command icinde `id`, `username` ve `password` yoktur. Cunku bu alanlar kullanicinin belirledigi alanlar degil, uygulamanin yonettigi alanlardir.

Bu ayrim cok onemlidir:

- Create command: Yeni entity icin kullanicidan alinacak alanlari tasir.
- Update command: Guncellenebilir profil alanlarini tasir.
- Domain entity: Sistemin tam veri modelidir.

### Service ile DAO arasindaki fark

DAO katmani `Map<Long, T>` uzerinde teknik islemler yapar:

- entity'ye id verir.
- map'e kaydeder.
- map'ten siler.
- map'ten bulur.
- var olmayan update/delete icin `EntityNotFoundException` firlatir.

Service katmani ise daha ust seviyede dusunur:

- `firstName` bos olamaz.
- `dateOfBirth` null olamaz.
- `durationMinutes` pozitif olmalidir.
- Training olusturulacaksa trainee ve trainer once var olmalidir.
- Update sirasinda username/password degismemelidir.

Kisaca:

```text
DAO: Veriyi nasil saklarim?
Service: Bu islem is kurallarina gore yapilabilir mi?
```

## 4. Validasyon ve Exception Tasarimi

### ValidationSupport

Projede validasyonlar `src/main/java/com/example/gymcrm/service/ValidationSupport.java` dosyasinda merkezi hale getirilir.

Bu sinif `final` ve constructor'i private oldugu icin sadece static yardimci metotlar icin kullanilir:

```java
public final class ValidationSupport {
    private ValidationSupport() {
    }
}
```

Bu tasarim sunu anlatir: `ValidationSupport` bir bean veya state tasiyan nesne degildir. Sadece ortak validasyon fonksiyonlarini barindirir.

### requireText

`requireText(String value, String fieldName)` iki sey yapar:

- Degerin `null` veya blank olup olmadigini kontrol eder.
- Gecerliyse trim edilmis halini dondurur.

Ornek:

```java
String firstName = requireText(command.firstName(), "firstName");
```

Bu sayede `" Alice "` input'u `"Alice"` olarak normalize edilir. Bos veya sadece space olan degerler `ValidationException` ile reddedilir.

Bu projede `requireText` su alanlarda kullanilir:

- trainee firstName
- trainee lastName
- trainee address
- trainer firstName
- trainer lastName
- training name

### requireNonNull

`requireNonNull(T value, String fieldName)` null olmamasi gereken referans tipleri icin kullanilir.

Ornekler:

```java
requireNonNull(command, "command");
requireNonNull(command.dateOfBirth(), "dateOfBirth");
requireNonNull(command.specialization(), "specialization");
requireNonNull(command.trainingType(), "trainingType");
requireNonNull(command.date(), "date");
```

Bu metot generic oldugu icin gelen tipi koruyarak ayni degeri geri dondurur. Boylece hem validasyon yapilir hem de deger lokal degiskene guvenli sekilde atanir.

### requirePositive

`requirePositive` iki overload ile gelir:

```java
public static long requirePositive(Long value, String fieldName)
public static int requirePositive(int value, String fieldName)
```

`Long` versiyonu id alanlari icin kullanilir. Null veya `<= 0` olan degerleri reddeder.

`int` versiyonu `durationMinutes` gibi primitive alanlar icin kullanilir. Primitive `int` null olamayacagi icin sadece `<= 0` kontrolu yapar.

Ornekler:

```java
long traineeId = requirePositive(command.traineeId(), "traineeId");
long trainerId = requirePositive(command.trainerId(), "trainerId");
int durationMinutes = requirePositive(command.durationMinutes(), "durationMinutes");
```

### Exception hiyerarsisi

Projede ortak base exception:

```text
GymCrmException extends RuntimeException
```

Bunun altinda iki temel uygulama hatasi vardir:

```text
ValidationException extends GymCrmException
EntityNotFoundException extends GymCrmException
```

Bu tasarim sayesinde uygulama seviyesindeki hatalar tek bir ailede toplanir.

### ValidationException

`ValidationException`, input is kurallarina uymadiginda kullanilir.

Ornek durumlar:

- command null geldi.
- firstName bos geldi.
- specialization null geldi.
- id null veya 0 geldi.
- durationMinutes negatif geldi.

Bu hata genelde DAO'ya gitmeden once service katmaninda firlatilir.

Akis:

```text
Gecersiz input
  |
  v
ValidationSupport
  |
  v
ValidationException
```

### EntityNotFoundException

`EntityNotFoundException`, input bicim olarak gecerlidir ama islem yapilacak entity bulunamazsa kullanilir.

Ornek durumlar:

- `TraineeServiceImpl.update(9L, command)` cagrilir ama trainee yoktur.
- `TrainerServiceImpl.update(8L, command)` cagrilir ama trainer yoktur.
- `TrainingServiceImpl.create(command)` icindeki traineeId veya trainerId map'te yoktur.
- DAO delete/update sirasinda ilgili id'yi bulamaz.

Constructor mesaji su formdadir:

```text
Trainee was not found for id=9
```

Bu mesaj, hangi entity'nin hangi id ile bulunamadigini hizli anlamayi saglar.

### GymCrmException

`GymCrmException`, uygulamaya ait runtime exception'lar icin ortak tabandir. Iki constructor'i vardir:

- Sadece message alan constructor.
- Message ve cause alan constructor.

Bu, baska altyapi hatalarini uygulama exception'i olarak sarmalamak gerektiginde kullanisli olur. Ornegin storage initialization tarafinda da bu aileye ait exception tasarimi vardir.

### Neden checked exception degil?

Bu projede validasyon ve bulunamayan entity hatalari `RuntimeException` ailesindedir. Bunun avantaji:

- Service interface'leri `throws` ile kirlenmez.
- Cagiran taraf her methodda zorunlu try/catch yazmaz.
- Hatalar uygulama seviyesinde ortak sekilde ele alinabilir.

Dezavantaji:

- Testlerle davranis netlesmezse bazi hata durumlari gozden kacabilir.

Bu yuzden service unit testleri bu tasarimin tamamlayicisidir.

## 5. Bu Projede Nerede?

| Konu | Dosya |
| --- | --- |
| Trainee service interface | `src/main/java/com/example/gymcrm/service/TraineeService.java` |
| Trainer service interface | `src/main/java/com/example/gymcrm/service/TrainerService.java` |
| Training service interface | `src/main/java/com/example/gymcrm/service/TrainingService.java` |
| Trainee service implementation | `src/main/java/com/example/gymcrm/service/impl/TraineeServiceImpl.java` |
| Trainer service implementation | `src/main/java/com/example/gymcrm/service/impl/TrainerServiceImpl.java` |
| Training service implementation | `src/main/java/com/example/gymcrm/service/impl/TrainingServiceImpl.java` |
| Ortak validasyon yardimcisi | `src/main/java/com/example/gymcrm/service/ValidationSupport.java` |
| Create/update command'lari | `src/main/java/com/example/gymcrm/service/command` |
| Base uygulama exception'i | `src/main/java/com/example/gymcrm/exception/GymCrmException.java` |
| Validasyon exception'i | `src/main/java/com/example/gymcrm/exception/ValidationException.java` |
| Entity bulunamadi exception'i | `src/main/java/com/example/gymcrm/exception/EntityNotFoundException.java` |
| Trainee service testleri | `src/test/java/com/example/gymcrm/service/impl/TraineeServiceImplTest.java` |
| Trainer service testleri | `src/test/java/com/example/gymcrm/service/impl/TrainerServiceImplTest.java` |
| Training service testleri | `src/test/java/com/example/gymcrm/service/impl/TrainingServiceImplTest.java` |
| ValidationSupport testleri | `src/test/java/com/example/gymcrm/service/ValidationSupportTest.java` |

## 6. Kod Akisi

### Trainee create akisi

Ilgili metot:

```text
TraineeServiceImpl.create(CreateTraineeCommand command)
```

Adimlar:

1. `command` null mi kontrol edilir.
2. `firstName` ve `lastName` text olarak validate edilir ve trim edilir.
3. `dateOfBirth` null mi kontrol edilir.
4. `address` text olarak validate edilir ve trim edilir.
5. `UsernameGenerator` ile username uretilir.
6. `PasswordGenerator` ile password uretilir.
7. Yeni `Trainee` nesnesi olusturulur.
8. `traineeDao.create(trainee)` cagrilir.

Akis diyagrami:

```text
CreateTraineeCommand
  |
  v
requireNonNull(command)
  |
  v
requireText(firstName, lastName, address)
  |
  v
requireNonNull(dateOfBirth)
  |
  v
generate username + generate password
  |
  v
new Trainee(...)
  |
  v
traineeDao.create(...)
```

Burada dikkat edilmesi gereken nokta: `username` ve `password` command'dan gelmez. Service tarafinda uretilir. Bu, credential alanlarinin uygulama tarafindan kontrol edilmesini saglar.

### Trainer create akisi

Ilgili metot:

```text
TrainerServiceImpl.create(CreateTrainerCommand command)
```

Adimlar:

1. `command` null mi kontrol edilir.
2. `firstName` ve `lastName` validate edilip trim edilir.
3. `specialization` null mi kontrol edilir.
4. Username ve password uretilir.
5. Yeni `Trainer` nesnesi olusturulur.
6. `trainerDao.create(trainer)` cagrilir.

Trainee create akisina cok benzer. Temel fark, trainee icin `dateOfBirth` ve `address` varken trainer icin `specialization` vardir.

### Trainee update akisi

Ilgili metot:

```text
TraineeServiceImpl.update(Long id, UpdateTraineeCommand command)
```

Adimlar:

1. `id` pozitif mi kontrol edilir.
2. `command` null mi kontrol edilir.
3. Guncellenebilir alanlar validate edilir.
4. `traineeDao.findById(id)` ile mevcut trainee aranir.
5. Bulunamazsa `EntityNotFoundException("Trainee", id)` firlatilir.
6. Bulunursa profil alanlari set edilir:
   - firstName
   - lastName
   - dateOfBirth
   - address
   - active
7. `traineeDao.update(trainee)` cagrilir.

En kritik nokta:

```text
username ve password set edilmez.
```

Yani update metodu profil bilgilerini gunceller ama kimlik bilgilerini korur.

Testte bu davranis su senaryo ile garanti altina alinir:

```text
updatesProfileButPreservesCredentials
```

Bu test, update sonrasi `username` ve `password` alanlarinin eski degerinde kaldigini kontrol eder.

### Trainer update akisi

Ilgili metot:

```text
TrainerServiceImpl.update(Long id, UpdateTrainerCommand command)
```

Adimlar:

1. `id` pozitif mi kontrol edilir.
2. `command` null mi kontrol edilir.
3. `firstName`, `lastName`, `specialization` validate edilir.
4. `trainerDao.findById(id)` ile mevcut trainer aranir.
5. Bulunamazsa `EntityNotFoundException("Trainer", id)` firlatilir.
6. Bulunursa profil alanlari set edilir:
   - firstName
   - lastName
   - specialization
   - active
7. `trainerDao.update(trainer)` cagrilir.

Burada da `username` ve `password` korunur.

### Delete akisi

Delete sadece trainee icin vardir:

```text
TraineeServiceImpl.delete(Long id)
```

Service tarafinda yapilan is:

```text
requirePositive(id, "id")
traineeDao.deleteById(id)
```

Burada service ayrica `findById` ile on kontrol yapmaz. Silme isleminin gercek varlik kontrolunu DAO yapar. DAO ilgili id'yi bulamazsa `EntityNotFoundException` firlatir.

Bu tasarimda iki seviye vardir:

- Service: id bicim olarak gecerli mi?
- DAO: bu id storage'da gercekten var mi?

### Find akisi

Find metotlari su sekildedir:

```text
findById(Long id)
findAll()
```

`findById` once id'nin pozitif olup olmadigini kontrol eder, sonra DAO'dan `Optional` doner.

Ornek:

```java
return traineeDao.findById(requirePositive(id, "id"));
```

Bulunamayan entity icin `findById` exception firlatmaz. Bunun yerine `Optional.empty()` donebilir.

Bu tasarim update ile find arasindaki farki gosterir:

- `findById`: "Varsa getir" anlamindadir.
- `update`: "Bu entity mutlaka var olmali, yoksa islem yapilamaz" anlamindadir.

`findAll` ise validasyon gerektirmez ve dogrudan DAO'ya delege edilir.

### Training create akisi

Ilgili metot:

```text
TrainingServiceImpl.create(CreateTrainingCommand command)
```

Adimlar:

1. `command` null mi kontrol edilir.
2. `traineeId` pozitif mi kontrol edilir.
3. `trainerId` pozitif mi kontrol edilir.
4. `name` text olarak validate edilir ve trim edilir.
5. `trainingType` null mi kontrol edilir.
6. `date` null mi kontrol edilir.
7. `durationMinutes` pozitif mi kontrol edilir.
8. `traineeDao.findById(traineeId)` ile trainee varligi kontrol edilir.
9. `trainerDao.findById(trainerId)` ile trainer varligi kontrol edilir.
10. Yeni `Training` nesnesi olusturulur.
11. `trainingDao.create(training)` cagrilir.

Akis:

```text
CreateTrainingCommand
  |
  v
validate command fields
  |
  v
check trainee exists
  |
  v
check trainer exists
  |
  v
new Training(...)
  |
  v
trainingDao.create(...)
```

Bu akisin en onemli is kurali sudur:

```text
Training, var olmayan trainee veya trainer uzerine olusturulamaz.
```

Bu kontrol DAO katmaninda degil, service katmaninda yapilir. Cunku bu bir storage teknigi degil, uygulama is kuralidir.

## 7. Kisa Kod Okuma Notlari

- Service implementation siniflari `@Service` ile isaretlidir. Bu, Spring'in bu siniflari component scan ile bean olarak bulmasini saglar.
- Bagimliliklar setter injection ile verilir. Testlerde bu setter'lar kullanilarak mock nesneler elle enjekte edilir.
- `ValidationSupport` metotlari static import edilir. Bu, service kodunu daha okunabilir yapar.
- Create metotlari yeni entity olusturur; update metotlari once mevcut entity'yi DAO'dan bulur.
- `Create...Command` icinde sistem tarafindan uretilen alanlar yoktur.
- `Update...Command` icinde username/password yoktur. Bu alanlar profile update ile degistirilmez.
- `findById` metotlari `Optional` doner; update metotlari entity yoksa exception firlatir.
- `TrainingServiceImpl` uc DAO'ya bagimlidir: training kaydetmek icin `TrainingDao`, referans kontrolu icin `TraineeDao` ve `TrainerDao`.
- Validation once gelir, DAO cagrilari sonra gelir. Boylece gecersiz input storage katmanina ulasmaz.
- DAO update/delete de kendi tarafinda missing entity durumunu kontrol eder. Bu, service tarafinda hata kacsa bile alt katmanda da koruma oldugunu gosterir.

## 8. Sik Dusulen Hatalar

### 1. Validasyonu DAO'ya tasimak

DAO'nun gorevi `firstName` bos mu, `durationMinutes` pozitif mi gibi is kurallarini bilmek degildir. Bu kontroller service katmaninda kalmalidir.

Yanlis dusunce:

```text
DAO zaten kaydediyor, orada kontrol ederim.
```

Daha dogru dusunce:

```text
DAO storage islemini yapar. Service is kurali ve validasyon uygular.
```

### 2. Update sirasinda yeni username/password uretmek

Create sirasinda username/password uretmek dogrudur. Ancak update sirasinda bunlari yeniden uretmek kullanicinin kimlik bilgilerini beklenmedik sekilde degistirir.

Bu yuzden update command'lari bu alanlari tasimaz ve update metotlari bu alanlara dokunmaz.

### 3. `findById` bulunamayinca exception beklemek

Bu projede `findById` metotlari `Optional` doner. Entity bulunamadiginda `Optional.empty()` beklenmelidir.

Exception beklenen yerler:

- update
- delete
- training create icindeki referans kontrolu

### 4. `requireText` sonucunu kullanmamak

`requireText` sadece kontrol yapmaz; ayni zamanda trim edilmis degeri dondurur.

Yanlis:

```java
requireText(command.firstName(), "firstName");
String firstName = command.firstName();
```

Daha dogru:

```java
String firstName = requireText(command.firstName(), "firstName");
```

Ilk ornekte validation yapilsa bile normalize edilmis deger kullanilmaz.

### 5. Training olustururken sadece id'leri kaydetmek

`Training` nesnesi sadece `traineeId` ve `trainerId` tasiyor diye, bu id'lerin gercekten var olup olmadigi onemsiz degildir. Service once iki referansin da varligini kontrol eder.

### 6. Null command'i unutmak

Command null olabilir. Bu yuzden her create/update metodu once `requireNonNull(command, "command")` ile baslar.

Bu kontrol olmazsa daha sonra `command.firstName()` gibi cagrilarda `NullPointerException` olusur. Proje bunun yerine daha anlamli `ValidationException` uretir.

### 7. Unit testlerde Spring context baslatmak

Service unit testlerinde Spring context baslatmaya gerek yoktur. Testler Mockito ile DAO ve generator bagimliliklarini mock'lar. Bu sayede testler hizli ve odakli kalir.

## 9. Kendini Test Et

1. Service layer ile DAO layer arasindaki temel sorumluluk farki nedir?
2. `CreateTraineeCommand` neden username ve password alanlarini icermez?
3. `requireText` sadece validation mi yapar, yoksa baska bir davranisi daha var mi?
4. `requirePositive(Long value, String fieldName)` neden `Long` alir ama `long` dondurur?
5. `ValidationException` hangi durumlarda firlatilir? En az uc ornek ver.
6. `EntityNotFoundException` ile `ValidationException` arasindaki fark nedir?
7. `findById` metodu entity bulamazsa neden exception firlatmak yerine `Optional.empty()` donebilir?
8. Update sirasinda username/password alanlarina dokunulmamasinin nedeni nedir?
9. Training create akisi neden hem `TraineeDao` hem `TrainerDao` kullanir?
10. Service unit testlerinde neden gercek DAO yerine mock DAO kullanilir?

## 10. Mini Pratik

1. `TraineeServiceImplTest` icine, `update` metoduna `id = 0L` verildiginde `ValidationException` firlatildigini test eden bir test ekle.
2. `TrainingServiceImplTest` icine, `CreateTrainingCommand` icinde blank `name` verildiginde DAO'larin cagrilmadigini dogrulayan bir test yaz.
3. `TrainerServiceImplTest` icinde, `create` sirasinda `" John "` ve `" Smith "` input'larinin username generator'a trim edilmis `"John"` ve `"Smith"` olarak gittigini verify et.
4. `ValidationSupportTest` icine `requirePositive((Long) null, "id")` icin ayri bir test ekle.
5. `TraineeServiceImpl.delete` icin id gecerli ama DAO entity bulamiyorsa `EntityNotFoundException` propagates olur mu, bunu testle goster.

Mini pratikleri yaparken hedef su olsun:

```text
Her test tek bir davranisi anlatsin.
Test ismi davranisi cumle gibi okusun.
Mock kullanimi test edilen service davranisini desteklesin, onun onune gecmesin.
```

## 11. Mulakat / Sinav Sorulari

1. Service layer neden controller/facade ile DAO arasinda ayri bir katman olarak tutulur?
2. Validation logic'i entity constructor'larina koymak ile service katmaninda yapmak arasindaki farklar nelerdir?
3. Runtime exception kullanmanin avantaj ve riskleri nelerdir?
4. `Optional<T>` donmek ile exception firlatmak arasinda nasil karar verirsin?
5. Update command'larinda credential alanlarini disarida birakmak hangi guvenlik ve tasarim problemlerini onler?
6. Bir training olusturulurken referans entity'lerin varligini kontrol etmek hangi mimari sorumluluga girer?
7. Unit test ile integration test arasindaki farki bu projedeki service testleri uzerinden acikla.
8. Setter injection'in test edilebilirlik acisindan avantajlari nelerdir? Dezavantajlari ne olabilir?
9. `ValidationSupport` static utility olarak tasarlanmis. Bunun yerine bean olarak tasarlansaydi ne degisirdi?
10. DAO da `EntityNotFoundException` firlatabiliyorken service update metodlari neden once `findById` yapiyor?

## 12. Ozet

Service katmani bu projede is kurallarinin ana sahibidir. Command record'lari disaridan gelen create/update isteklerini tasir, service bu command'lari validate eder, gerekli domain nesnelerini olusturur veya mevcut nesneleri gunceller ve DAO'ya delege eder.

`ValidationSupport` ortak validasyon davranislarini merkezi hale getirir. `requireText` bos metni reddeder ve trim eder, `requireNonNull` null referanslari reddeder, `requirePositive` id ve sure gibi pozitif olmasi gereken degerleri kontrol eder.

Exception tasariminda `GymCrmException` ortak tabandir. Gecersiz input icin `ValidationException`, bicim olarak gecerli ama bulunamayan entity durumlari icin `EntityNotFoundException` kullanilir.

Create akislari username/password uretir; update akislari bu alanlari korur. Training create akisi, sadece training kaydetmez; once trainee ve trainer referanslarinin gercekten var oldugunu dogrular.

Service unit testleri bu davranislarin guvencesidir. Mock DAO ve mock generator kullanarak servislerin validasyon, orchestration, credential koruma, missing entity ve delegation davranislari izole sekilde test edilir.
