# Username ve Password Uretimi

Bu not, projedeki username ve password uretim mantigini ders calisir gibi
tekrar etmek icin hazirlandi. Once generator ve Strategy fikrini anlayacagiz,
sonra bu fikrin `UniqueUsernameGenerator`, `SecurePasswordGenerator`,
service katmani ve testlerde nasil kullanildigini takip edecegiz.

## Ana Dosyalar

**Ana dosyalar:**

- `src/main/java/com/example/gymcrm/generator/UsernameGenerator.java`
- `src/main/java/com/example/gymcrm/generator/PasswordGenerator.java`
- `src/main/java/com/example/gymcrm/generator/UniqueUsernameGenerator.java`
- `src/main/java/com/example/gymcrm/generator/SecurePasswordGenerator.java`
- `src/main/java/com/example/gymcrm/service/impl/TraineeServiceImpl.java`
- `src/main/java/com/example/gymcrm/service/impl/TrainerServiceImpl.java`
- `src/main/java/com/example/gymcrm/domain/User.java`
- `src/test/java/com/example/gymcrm/generator/GeneratorTest.java`

## 2. Bu Konunun Amaci

Bu konunun amaci, yeni bir trainee veya trainer olusturulurken sistemin:

- Kullaniciya nasil benzersiz bir `username` verdigini,
- Gecici/parola degeri gibi dusunulebilecek `password` bilgisini nasil
  urettigini,
- Bu iki davranisi neden service siniflarinin icine gommek yerine ayri
  generator siniflarina cikardigini,
- Neden hassas veriyi `toString`, log veya hata mesajlarina tasimamak
  gerektigini,
- Bu davranislarin testlerle nasil guvence altina alindigini

anlamaktir.

Bu bolumu bitirdiginde su sorulara rahat cevap verebilmelisin:

- `UsernameGenerator` neden interface?
- `UniqueUsernameGenerator` iki farkli storage'i neden birlikte kontrol ediyor?
- `John.Smith`, `john.smith` ve `JOHN.SMITH1` ayni algoritma icinde nasil
  degerlendiriliyor?
- Password uretiminde neden `SecureRandom` kullaniliyor?
- `User.toString()` neden password yazdirmiyor?

## 3. Buyuk Resim

Gym CRM projesinde kullanici olusturma akisi kabaca soyledir:

```text
CreateTraineeCommand / CreateTrainerCommand
        |
        v
TraineeServiceImpl / TrainerServiceImpl
        |
        |-- firstName, lastName ve diger alanlari validate eder
        |-- UsernameGenerator.generate(firstName, lastName)
        |-- PasswordGenerator.generate()
        v
Trainee / Trainer nesnesi
        |
        v
TraineeDao / TrainerDao
        |
        v
Map<Long, Trainee> / Map<Long, Trainer>
```

Burada onemli karar sudur:

Service katmani "kullanici nasil olusturulur?" sorusunu yonetir. Username ve
password algoritmasinin teknik detayini ise generator siniflarina birakir.

Bu sayede:

- Service siniflari daha okunur kalir.
- Username algoritmasi tek yerde degisir.
- Password algoritmasi tek yerde degisir.
- Testlerde generator mock'lanabilir.
- Ileride farkli bir generator stratejisi eklemek kolaylasir.

## 4. Generator / Strategy Kavramlari

### Generator Nedir?

Generator, belirli bir kurala gore yeni bir deger ureten siniftir.

Bu projede iki generator vardir:

```java
public interface UsernameGenerator {
    String generate(String firstName, String lastName);
}
```

```java
public interface PasswordGenerator {
    String generate();
}
```

`UsernameGenerator`, isim ve soyisimden username uretir.
`PasswordGenerator`, parametre almadan password uretir.

### Strategy Gibi Dusunmek

Strategy pattern'in temel fikri sudur:

> Bir davranisin algoritmasi degisebiliyorsa, o algoritmayi kullanan sinif
> somut implementasyona degil, bir interface'e baglansin.

Bu projede service siniflari su alanlara sahiptir:

```java
private UsernameGenerator usernameGenerator;
private PasswordGenerator passwordGenerator;
```

Yani `TraineeServiceImpl` ve `TrainerServiceImpl` sunu bilmez:

- Username hangi algoritmayla uretiliyor?
- Password hangi karakter setinden uretiliyor?
- Password uzunlugu kac?
- Username benzersizligi nasil kontrol ediliyor?

Sadece sunu bilir:

```text
Bana firstName ve lastName verildiginde username ureten bir nesne var.
Bana cagrildiginda password ureten bir nesne var.
```

Bu yaklasim Strategy mantigina benzer. Cunku servisler davranisin sozlesmesine
baglidir, davranisin ic detayina degil.

### Bu Projede Strategy'nin Faydasi

Ornek olarak ileride su istek gelse:

- Username `firstName.lastName` yerine `firstName_lastName` olsun.
- Password 10 yerine 16 karakter olsun.
- Password en az bir rakam, bir buyuk harf ve bir ozel karakter icersin.
- Test ortaminda sabit password ureten generator kullanilsin.

Bu degisikliklerin cogu service katmanina dokunmadan generator
implementasyonunda yapilabilir.

## 5. Username Algoritmasi

Username algoritmasi `UniqueUsernameGenerator` sinifindadir.

Sinifin gorevi:

```text
firstName + "." + lastName formatinda username uret.
Bu username hem trainee hem trainer tarafinda benzersiz olsun.
Karsilastirmayi case-insensitive yap.
Ayni isim-soyisimden daha once varsa numeric suffix ekle.
```

### 5.1 Storage Bagimliliklari

`UniqueUsernameGenerator`, iki storage'a ihtiyac duyar:

```java
private Map<Long, Trainee> traineeStorage;
private Map<Long, Trainer> trainerStorage;
```

Bu iki map setter injection ile alinir:

```java
@Autowired
public void setTraineeStorage(@Qualifier(StorageBeanNames.TRAINEE_STORAGE) Map<Long, Trainee> traineeStorage)
```

```java
@Autowired
public void setTrainerStorage(@Qualifier(StorageBeanNames.TRAINER_STORAGE) Map<Long, Trainer> trainerStorage)
```

Burada `@Qualifier` onemlidir. Cunku Spring context icinde birden fazla
`Map` bean'i olabilir. Spring'e "hangi Map?" sorusunun cevabi verilir:

- `TRAINEE_STORAGE`
- `TRAINER_STORAGE`

Algoritma sadece trainee'leri kontrol etseydi, ayni username bir trainer icin
tekrar uretilebilirdi. Bu yuzden iki storage birlikte okunur.

### 5.2 Storage Zorunlulugu

`generate` metodunun ilk adimi:

```java
requireStorage();
```

Bu metod iki storage'in da enjekte edilmis olmasini bekler. Eksikse:

```text
IllegalStateException("User storages have not been injected")
```

firlatir.

Bu davranis testte de vardir:

```java
usernameGeneratorRequiresBothStorages()
```

Bu test, sadece trainee storage set edilirse ve trainer storage eksik kalirsa
generator'in calismamasi gerektigini dogrular.

### 5.3 Trim

Algoritma once gelen isim ve soyismi trim eder:

```java
String trimmedFirstName = firstName.trim();
String trimmedLastName = lastName.trim();
```

Yani:

```text
" John "  -> "John"
" Smith " -> "Smith"
```

Bu sayede disaridan gelen bosluklar username'e tasinmaz.

Ornek:

```text
generate(" John ", " Smith ")
```

once su hale gelir:

```text
firstName = "John"
lastName = "Smith"
```

sonra base username:

```text
John.Smith
```

olur.

Not: `generate` metodu `firstName` ve `lastName` icin direkt `trim()` cagirir.
Bu yuzden null deger service katmaninda daha once engellenmelidir. Projede
`TraineeServiceImpl` ve `TrainerServiceImpl`, `requireText` ile bunu yapar.

### 5.4 Base Username

Base username su formatta uretilir:

```java
String base = trimmedFirstName + "." + trimmedLastName;
```

Ornekler:

```text
firstName = "John", lastName = "Smith" -> "John.Smith"
firstName = "Jane", lastName = "Doe"   -> "Jane.Doe"
firstName = "Ali",  lastName = "Veli"  -> "Ali.Veli"
```

Algoritma orijinal harf buyuklugunu korur. Yani kullanici `John` girdiyse
username `John.Smith` olarak doner. Ancak benzersizlik kontrolunde kucuk/buyuk
harf farki yok sayilir.

### 5.5 Case-insensitive Normalize

Karsilastirma icin normalize metodu kullanilir:

```java
private String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
}
```

Bu metot:

- Null ise bos string yapar.
- Bas ve sondaki bosluklari siler.
- `Locale.ROOT` ile kucuk harfe cevirir.

Ornek:

```text
" John.Smith " -> "john.smith"
"JOHN.SMITH1"  -> "john.smith1"
"john.smith"   -> "john.smith"
null           -> ""
```

Neden `Locale.ROOT`?

Locale'e bagli harf donusumleri bazen beklenmeyen sonuc uretebilir. Ozellikle
Turkce `I/i` gibi karakterlerde locale etkisi gorulebilir. `Locale.ROOT`,
teknik ve locale'den bagimsiz normalize islemleri icin daha guvenli bir
tercihtir.

### 5.6 Trainee + Trainer Ortak Kontrol

Mevcut tum user'lar su metodla tek stream haline getirilir:

```java
private Stream<? extends User> users() {
    return Stream.concat(traineeStorage.values().stream(), trainerStorage.values().stream());
}
```

Sonra unavailable username set'i olusturulur:

```java
private Set<String> unavailableUsernames() {
    Set<String> unavailable = new HashSet<>();
    users().map(User::getUsername).map(this::normalize).forEach(unavailable::add);
    return unavailable;
}
```

Bu set'te daha once kullanilmis tum username'ler normalize edilmis halde durur.

Ornek storage durumu:

```text
trainee username: "john.smith"
trainer username: "JOHN.SMITH1"
```

`unavailable` set'i:

```text
["john.smith", "john.smith1"]
```

Yeni istek:

```text
generate(" John ", " Smith ")
```

Base:

```text
John.Smith
```

Normalize edilmis hali:

```text
john.smith
```

Bu deger unavailable icinde oldugu icin kullanilamaz.

### 5.7 NamePair ve Suffix Baslangici

Algoritmada sadece username set'i yoktur. Bir de ayni isim-soyisimden kac
kayit oldugu sayilir:

```java
NamePair namePair = NamePair.of(trimmedFirstName, trimmedLastName);
long suffix = existingNamePairCount(namePair);
```

`NamePair`, firstName ve lastName bilgisini normalize ederek tutan bir record'dur:

```java
private record NamePair(String firstName, String lastName)
```

Bu sayede su isimler ayni kisi adi cifti gibi sayilir:

```text
"John", "Smith"
" john ", " smith "
"JOHN", "SMITH"
```

Hepsinin normalized pair'i:

```text
("john", "smith")
```

### 5.8 Suffix Mantigi

Kodun kritik bolumu:

```java
long suffix = existingNamePairCount(namePair);
String candidate = suffix == 0 ? base : base + suffix;
while (unavailable.contains(normalize(candidate))) {
    suffix++;
    candidate = base + suffix;
}
return candidate;
```

Algoritmanin adimlari:

1. Ayni isim-soyisimden kac user var, say.
2. Sayi 0 ise ilk aday `base` olsun.
3. Sayi 0 degilse ilk aday `base + suffix` olsun.
4. Aday username daha once kullanilmissa suffix'i arttir.
5. Bos bir username bulunana kadar devam et.

### 5.9 Ornek 1: Hic Cakisma Yok

Storage:

```text
trainees: []
trainers: []
```

Cagri:

```text
generate("Jane", "Doe")
```

Adimlar:

```text
trimmedFirstName = "Jane"
trimmedLastName  = "Doe"
base             = "Jane.Doe"
namePair         = ("jane", "doe")
existing count   = 0
candidate        = "Jane.Doe"
unavailable      = []
sonuc            = "Jane.Doe"
```

### 5.10 Ornek 2: Ayni Username Trainee'de Var

Storage:

```text
trainee username: "john.smith"
```

Cagri:

```text
generate("John", "Smith")
```

Adimlar:

```text
base           = "John.Smith"
namePair       = ("john", "smith")
existing count = 1
candidate      = "John.Smith1"
```

Eger `John.Smith1` bos ise sonuc:

```text
John.Smith1
```

### 5.11 Ornek 3: Trainee ve Trainer Birlikte Kontrol Edilir

Storage:

```text
trainee: username = "john.smith",  firstName = "John", lastName = "Smith"
trainer: username = "JOHN.SMITH1", firstName = "John", lastName = "Smith"
```

Cagri:

```text
generate(" John ", " Smith ")
```

Adimlar:

```text
trim             -> "John", "Smith"
base             -> "John.Smith"
unavailable      -> ["john.smith", "john.smith1"]
existing count   -> 2
ilk candidate    -> "John.Smith2"
normalize        -> "john.smith2"
unavailable'da?  -> hayir
sonuc            -> "John.Smith2"
```

Bu davranis testte su isimle guvence altindadir:

```text
usernameChecksBothStoragesCaseInsensitivelyAndAddsSuffixes
```

### 5.12 Ornek 4: Suffix Sadece Username'e Gore Degil, NamePair'e Gore Baslar

Testte su senaryo vardir:

```text
trainee:
  firstName = "Jane"
  lastName  = "Doe"
  username  = "Old.Username"

trainer:
  firstName = "Different"
  lastName  = "Person"
  username  = "Jane.Doe"
```

Cagri:

```text
generate("Jane", "Doe")
```

Burada iki bilgi ayri ayri onemlidir:

```text
existingNamePairCount("Jane", "Doe") = 1
unavailable usernames includes "jane.doe"
```

Bu yuzden ilk aday:

```text
Jane.Doe1
```

olur.

Bu test bize sunu ogretir:

- Suffix baslangici ayni isim-soyisim sayisina gore belirlenir.
- Ama nihai benzersizlik username set'i ile kontrol edilir.
- Yani algoritma hem kisi adi tekrarini hem de username cakismasini dikkate alir.

### 5.13 Ornek 5: Silinen User'dan Sonra Username Tekrar Kullanilabilir

Testte su akis vardir:

1. Bos trainee storage ile generator olusturulur.
2. `Deleted.User` uretilir.
3. Bu username'e sahip trainee storage'a eklenir.
4. Sonra trainee storage'dan silinir.
5. Tekrar `generate("Deleted", "User")` cagirilir.

Sonuc:

```text
Deleted.User
```

Neden?

Cunku generator kalici bir "once kullanildi" listesi tutmaz. Her cagrisinda
mevcut storage'lari okur. Storage'dan silinen user artik username
benzersizlik kontrolune dahil degildir.

Bu davranis testte:

```text
usernameCanBeReusedAfterEntityIsRemovedFromStorage
```

ile dogrulanir.

## 6. Password Algoritmasi

Password algoritmasi `SecurePasswordGenerator` sinifindadir.

Gorevi:

```text
10 karakter uzunlugunda, alphanumeric bir password uret.
```

### 6.1 SecureRandom

Sinifta su alan vardir:

```java
private final SecureRandom secureRandom = new SecureRandom();
```

`SecureRandom`, guvenlik amacli rastgelelik icin `Random` sinifina gore daha
uygun bir tercihtir.

Basit `Random` daha cok simulasyon, oyun, basit rastgelelik gibi alanlarda
kullanilir. Password gibi tahmin edilmesi zor olmasi gereken verilerde
`SecureRandom` tercih edilir.

### 6.2 Password Uzunlugu

Uzunluk sabiti:

```java
static final int PASSWORD_LENGTH = 10;
```

Bu projede her password tam 10 karakterdir.

Testte bu garanti su assertion ile kontrol edilir:

```java
assertThat(password).hasSize(10)
```

### 6.3 Alphanumeric Karakter Seti

Kullanilan karakter seti:

```java
"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
```

Yani password su karakterlerden olusabilir:

- Buyuk harfler: `A-Z`
- Kucuk harfler: `a-z`
- Rakamlar: `0-9`

Ozel karakter yoktur.

Testte regex:

```java
[A-Za-z0-9]{10}
```

ile dogrulanir.

### 6.4 Uretim Dongusu

Kodun mantigi:

```java
char[] password = new char[PASSWORD_LENGTH];
for (int i = 0; i < password.length; i++) {
    password[i] = ALPHANUMERIC[secureRandom.nextInt(ALPHANUMERIC.length)];
}
return new String(password);
```

Adim adim:

1. 10 elemanli bir `char[]` olustur.
2. Her pozisyon icin `ALPHANUMERIC` dizisinden rastgele bir index sec.
3. Secilen karakteri password dizisine koy.
4. Dizi dolunca `String`e cevir.

Ornek uretimler:

```text
A1b2C3d4E5
x9Qp7Lm2Za
K0tY6nB3rS
```

Not: Bunlar ornek formatlardir. Gercek uretim rastgeledir.

### 6.5 Rastgelelik Testi Ne Kadar Test Edilir?

`GeneratorTest` icinde password icin 50 kez uretim yapilir:

```java
for (int i = 0; i < 50; i++) {
    String password = generator.generate();
    assertThat(password).hasSize(10).matches("[A-Za-z0-9]{10}");
    passwords.add(password);
}
assertThat(passwords).hasSizeGreaterThan(1);
```

Bu test sunlari garanti eder:

- Her password 10 karakterdir.
- Her password sadece alphanumeric karakterlerden olusur.
- 50 uretimin hepsi ayni degildir.

Bu test sunu garanti etmez:

- Kriptografik olarak mukemmel rastgelelik.
- Her password en az bir buyuk harf, bir kucuk harf ve bir rakam icerir.
- Uretilen password asla tekrar etmez.

Testin amaci algoritmanin temel sozlesmesini kontrol etmektir.

## 7. Bu Projede Nerede?

### 7.1 Interface'ler

`UsernameGenerator.java`

```text
com.example.gymcrm.generator.UsernameGenerator
```

Sozlesme:

```text
String generate(String firstName, String lastName)
```

`PasswordGenerator.java`

```text
com.example.gymcrm.generator.PasswordGenerator
```

Sozlesme:

```text
String generate()
```

### 7.2 Implementasyonlar

`UniqueUsernameGenerator.java`

- `@Component` ile Spring bean olur.
- `UsernameGenerator` interface'ini uygular.
- Trainee ve trainer storage'lari birlikte kontrol eder.
- Username kontrolunu case-insensitive yapar.
- Ayni isim-soyisim tekrarinda suffix ekler.

`SecurePasswordGenerator.java`

- `@Component` ile Spring bean olur.
- `PasswordGenerator` interface'ini uygular.
- `SecureRandom` ile 10 karakterlik alphanumeric password uretir.

### 7.3 Service Katmani

`TraineeServiceImpl.create` icinde:

```java
Trainee trainee = new Trainee(null, firstName, lastName,
        usernameGenerator.generate(firstName, lastName), passwordGenerator.generate(),
        command.active(), dateOfBirth, address);
```

`TrainerServiceImpl.create` icinde:

```java
Trainer trainer = new Trainer(null, firstName, lastName,
        usernameGenerator.generate(firstName, lastName), passwordGenerator.generate(),
        command.active(), specialization);
```

Iki service de ayni generator interface'lerini kullanir. Bu sayede trainer ve
trainee username'leri ayni global kurala bagli kalir.

### 7.4 Domain Modeli ve Hassas Veri

`User` sinifi su alanlara sahiptir:

```text
id
firstName
lastName
username
password
active
```

Ancak `toString()` sadece sunu doner:

```java
return getClass().getSimpleName() + "{id=" + id + ", active=" + active + '}';
```

Yani `username`, `password`, `firstName`, `lastName` gibi bilgiler
`toString()` icinde yoktur.

Bu karar ozellikle password icin onemlidir. Nesneler log'lanirken veya hata
mesajlarinda yanlislikla `toString()` cagrilirsa password disari sizmaz.

Bu projede password in-memory tutuluyor olabilir, ama yine de temel prensip
degismez:

```text
Hassas veriyi gereksiz yere log'lama.
Hassas veriyi toString icine koyma.
Hassas veriyi exception mesajina tasima.
```

Bu baglanti username icin de dusunulebilir. Username password kadar gizli
olmasa da kisiyi tanimlamaya yarayan bir veridir. Gereksiz log kirliliginden
ve veri sizmasindan kacinmak iyi bir aliskanliktir.

## 8. Kod Akisi

### 8.1 Trainee Olusturma Akisi

```text
TraineeServiceImpl.create(command)
        |
        |-- requireNonNull(command, "command")
        |-- requireText(command.firstName(), "firstName")
        |-- requireText(command.lastName(), "lastName")
        |-- requireNonNull(command.dateOfBirth(), "dateOfBirth")
        |-- requireText(command.address(), "address")
        |
        |-- usernameGenerator.generate(firstName, lastName)
        |       |
        |       |-- requireStorage()
        |       |-- trim firstName / lastName
        |       |-- base = firstName + "." + lastName
        |       |-- unavailableUsernames()
        |       |-- existingNamePairCount()
        |       |-- suffix gerekirse arttir
        |       v
        |     username
        |
        |-- passwordGenerator.generate()
        |       |
        |       |-- 10 elemanli char[] olustur
        |       |-- SecureRandom ile karakter sec
        |       v
        |     password
        |
        |-- new Trainee(...)
        |
        v
traineeDao.create(trainee)
```

### 8.2 Trainer Olusturma Akisi

```text
TrainerServiceImpl.create(command)
        |
        |-- command validate edilir
        |-- firstName ve lastName alinir
        |-- specialization kontrol edilir
        |-- usernameGenerator.generate(firstName, lastName)
        |-- passwordGenerator.generate()
        |-- new Trainer(...)
        v
trainerDao.create(trainer)
```

### 8.3 Update Akisinda Generator Kullanilmaz

`TraineeServiceImpl.update` ve `TrainerServiceImpl.update` icinde username veya
password yeniden uretilmez.

Update akisi:

- Mevcut entity bulunur.
- firstName, lastName ve diger update edilebilir alanlar set edilir.
- DAO update cagrilir.

Bu su anlama gelir:

```text
Kullanici olusturuldugunda username/password uretimi yapilir.
Kullanici guncellendiginde username/password otomatik degismez.
```

Bu davranis sinav veya mulakat sorusu olarak sorulabilir. Cunku firstName veya
lastName update edilince username'in de degisip degismemesi bir is kuralidir.
Bu projede degismiyor.

## 9. Kisa Kod Okuma Notlari

- `UsernameGenerator` ve `PasswordGenerator` cok kucuk interface'lerdir. Bu
  kucukluk kotu degil; tersine servislerin algoritma detayina baglanmasini
  engeller.

- `UniqueUsernameGenerator` hem `Trainee` hem `Trainer` storage'ini okur.
  Username benzersizligi tek bir user turu icin degil, tum user havuzu icindir.

- `@Qualifier(StorageBeanNames.TRAINEE_STORAGE)` ve
  `@Qualifier(StorageBeanNames.TRAINER_STORAGE)` injection belirsizligini
  azaltir.

- `normalize` username karsilastirmasinda kullanilir. Boylece `John.Smith`,
  `john.smith` ve `JOHN.SMITH` ayni username gibi kabul edilir.

- `NamePair` firstName ve lastName'i normalize ederek suffix baslangicini
  belirler.

- `unavailableUsernames` bir `HashSet` kullanir. Bu, username var mi kontrolunu
  hizli yapar.

- `SecurePasswordGenerator` static bir alphanumeric karakter dizisi kullanir.
  Her karakter icin rastgele index secer.

- `GeneratorTest`, generator siniflarini Spring context acmadan test eder.
  Setter'lar manuel cagrilarak storage verilir.

- `User.toString()` hassas veriyi yazdirmaz. Bu, logging ve debugging sirasinda
  password sizmasini onlemek icin iyi bir tercihtir.

## 10. Sik Dusulen Hatalar

### Hata 1: Sadece Trainee Storage'i Kontrol Etmek

Yanlis yaklasim:

```text
Username trainee'lerde yoksa uygundur.
```

Neden yanlis?

Trainer tarafinda ayni username olabilir. Bu durumda sistem genelinde
benzersizlik bozulur.

Dogru yaklasim:

```text
Trainee + Trainer birlikte kontrol edilmeli.
```

### Hata 2: Case-sensitive Kontrol Yapmak

Yanlis:

```text
"John.Smith" ve "john.smith" farklidir.
```

Dogru:

```text
"John.Smith", "john.smith", "JOHN.SMITH" ayni username gibi ele alinmali.
```

Bu projede normalize ile bu saglanir.

### Hata 3: Trim Yapmamak

Yanlis:

```text
" John " + "." + " Smith " -> " John . Smith "
```

Dogru:

```text
" John ".trim() + "." + " Smith ".trim() -> "John.Smith"
```

### Hata 4: Suffix'i Direkt 1'den Baslatmak

Bazi algoritmalar cakisma gorunce hemen `base + 1` dener. Bu proje ise once
ayni isim-soyisimden kac kayit olduguna bakar.

Bu fark su senaryoda onemlidir:

```text
Jane Doe adinda bir user var ama username'i Old.Username.
Baska bir user'in username'i Jane.Doe.
Yeni Jane Doe icin sonuc Jane.Doe1 olur.
```

### Hata 5: Password Icin Random Kullanmak

`java.util.Random`, password uretimi icin dogru tercih degildir. Guvenlik
baglaminda `SecureRandom` kullanmak gerekir.

### Hata 6: Password'u toString Icine Koymak

Yanlis:

```java
return "User{username=" + username + ", password=" + password + "}";
```

Bu, loglara password sizdirabilir.

Dogru:

```text
toString icinde hassas veri yazdirma.
```

### Hata 7: Random Testini Deterministik Sanmak

Password rastgele uretilir. Testte belli bir password beklemek yanlistir.

Yanlis:

```text
password == "A1b2C3d4E5"
```

Dogru:

```text
password length == 10
password matches [A-Za-z0-9]{10}
50 uretimde en az iki farkli deger var
```

## 11. Kendini Test Et

1. `UsernameGenerator` interface'i olmasaydi `TraineeServiceImpl` ve
   `TrainerServiceImpl` hangi somut sinifa baglanmak zorunda kalirdi?

2. `UniqueUsernameGenerator` neden hem trainee hem trainer storage'ini kontrol
   ediyor?

3. `generate(" John ", " Smith ")` cagrisi neden `" John . Smith "` sonucunu
   uretmez?

4. `normalize(" JOHN.SMITH1 ")` metodunun sonucu ne olur?

5. Storage'da trainee username'i `john.smith`, trainer username'i
   `JOHN.SMITH1` ise `generate("John", "Smith")` sonucu ne olur?

6. `existingNamePairCount` ile `unavailableUsernames` arasindaki fark nedir?

7. Neden password testinde belirli bir sabit password beklenmez?

8. `SecurePasswordGenerator` neden `SecureRandom` kullanir?

9. `User.toString()` icinde password yazsaydi hangi risk dogardi?

10. `TraineeServiceImpl.update` firstName ve lastName'i degistirdiginde username
    neden yeniden uretilmez? Bu bir bug mi, yoksa is kuralina bagli bir tercih
    mi?

### Cevap Anahtari

1. Servisler `UniqueUsernameGenerator` gibi somut bir sinifa baglanirdi. Bu da
   degistirilebilirligi ve test edilebilirligi azaltirdi.

2. Username sistem genelinde benzersiz olmalidir. Ayni username'in bir trainee
   ve bir trainer tarafindan paylasilmasi istenmez.

3. Cunku algoritma once `firstName.trim()` ve `lastName.trim()` cagirir.

4. `john.smith1`

5. `John.Smith2`

6. `existingNamePairCount`, ayni firstName-lastName cifti kac kez var diye
   sayar. `unavailableUsernames`, kullanilmis username'leri set olarak tutar.

7. Cunku password rastgele uretilir. Test, format ve uzunluk gibi sozlesmeleri
   kontrol etmelidir.

8. Password tahmin edilebilir olmamalidir. `SecureRandom`, guvenlik amacli
   rastgelelik icin daha uygundur.

9. Loglarda veya hata ciktilarinda password gorunebilir. Bu hassas veri sizmasi
   demektir.

10. Bu projede update akisi username'i degistirmez. Bunun bug mu tercih mi
    oldugu gereksinime baglidir; mevcut kodda davranis acikca bu yondedir.

## 12. Mini Pratik

### Pratik 1: Username Senaryosu Ciz

Asagidaki storage durumuna gore `generate("Ali", "Kaya")` sonucunu elle hesapla:

```text
trainee:
  firstName = "Ali", lastName = "Kaya", username = "ali.kaya"

trainer:
  firstName = "Ali", lastName = "Kaya", username = "ALI.KAYA1"
```

Beklenen dusunme:

```text
base = Ali.Kaya
unavailable = ["ali.kaya", "ali.kaya1"]
existingNamePairCount = 2
candidate = Ali.Kaya2
```

Sonuc:

```text
Ali.Kaya2
```

### Pratik 2: Yeni Test Senaryosu Tasarla

`GeneratorTest` icin su davranisi test eden bir test fikri yaz:

```text
Username karsilastirmasi bas ve sondaki bosluklari yok saymali.
```

Ornek dusun:

```text
Mevcut username: "  jane.doe  "
Yeni istek: generate("Jane", "Doe")
Beklenen: Jane.Doe1 veya algoritmanin mevcut sayim mantigina uygun suffix
```

Sonra su soruyu cevapla:

```text
Bu test mevcut algoritmada gecer mi?
```

### Pratik 3: Password Kuralini Degistirmeyi Dusun

Yeni gereksinim:

```text
Password 12 karakter olsun.
```

Hangi dosyada degisiklik yaparsin?

Beklenen cevap:

```text
SecurePasswordGenerator.java icindeki PASSWORD_LENGTH sabiti.
GeneratorTest icindeki uzunluk beklentisi.
```

### Pratik 4: Service Mock Mantigini Incele

`TraineeServiceImplTest` veya `TrainerServiceImplTest` dosyasini ac ve
generator'larin nasil mock'landigini incele.

Kendine su soruyu sor:

```text
Service testleri neden gercek random password uretimine bagli degil?
```

Beklenen cevap:

```text
Cunku service testinin amaci password algoritmasini test etmek degil,
service'in generator sonucunu entity'ye yerlestirdigini test etmektir.
```

## 13. Mulakat / Sinav Sorulari

1. Bu projede `UsernameGenerator` ve `PasswordGenerator` interface'lerinin
   olmasi hangi tasarim prensibine hizmet eder?

2. Strategy pattern ile bu generator yapisi arasindaki benzerlik nedir?

3. `UniqueUsernameGenerator` icinde `HashSet` kullanilmasinin avantaji nedir?

4. Username benzersizligi neden case-insensitive kontrol edilmelidir?

5. `Locale.ROOT` neden `toLowerCase()` icin iyi bir tercihtir?

6. Password uretiminde `SecureRandom` ile `Random` arasindaki fark nedir?

7. Rastgele uretilen password icin nasil test yazilir?

8. `toString()` icinde password yazmak neden tehlikelidir?

9. Bu projede firstName/lastName update edilirse username neden degismez?

10. Ileride password policy degisirse hangi siniflar etkilenir?

### Kisa Cevaplar

1. Dependency Inversion ve Strategy benzeri tasarima hizmet eder.

2. Servisler algoritmanin kendisine degil, algoritmayi temsil eden interface'e
   baglidir.

3. Username var mi kontrolu set ile hizli ve sade yapilir.

4. Kullanici icin `John.Smith` ve `john.smith` pratikte ayni kimlik gibi
   algilanir; cakismayi onlemek gerekir.

5. Locale kaynakli beklenmeyen harf donusumlerini azaltir.

6. `SecureRandom`, guvenlik amacli daha guclu rastgelelik saglar.

7. Sabit sonuc beklenmez; uzunluk, karakter seti ve belli olcude cesitlilik
   test edilir.

8. Loglara veya hata ciktilarina hassas veri sizabilir.

9. Mevcut is kurali boyle uygulanmistir; update akisi generator cagirmaz.

10. Temel olarak `SecurePasswordGenerator` ve onu dogrulayan testler etkilenir.

## 14. Ozet

Bu projede username ve password uretimi service katmanindan ayrilmis, generator
interface'leri arkasina alinmistir. Bu yapi Strategy pattern'e benzer: servis
algoritmanin detayini bilmez, sadece generator sozlesmesini kullanir.

`UniqueUsernameGenerator`, username'i `firstName.lastName` formatinda uretir.
Gelen isimleri trim eder, trainee ve trainer storage'larini birlikte okur,
username karsilastirmasini case-insensitive yapar ve gerekirse numeric suffix
ekler. Suffix baslangici ayni isim-soyisim sayisina gore belirlenir, nihai
benzersizlik ise kullanilmis username set'i ile dogrulanir.

`SecurePasswordGenerator`, `SecureRandom` kullanarak 10 karakterlik alphanumeric
password uretir. Testler password'un belirli bir sabit deger olmasini beklemez;
uzunluk, karakter seti ve bir miktar cesitlilik kontrol eder.

`User.toString()` icinde password veya username yazilmamasi, hassas verinin
loglara sizmasini onleyen onemli bir guvenlik aliskanligidir. Bu kucuk detay,
gercek uygulamalarda buyuk sorunlari engelleyebilir.
