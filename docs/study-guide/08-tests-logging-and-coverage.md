# Tests, Logging ve Coverage Okuma

## Ana Dosyalar

**Ana dosyalar:**

- `pom.xml`
- `src/main/resources/logback.xml`
- `src/test/java/com/example/gymcrm/dao/impl/MapDaoTest.java`
- `src/test/java/com/example/gymcrm/service/impl/TraineeServiceImplTest.java`
- `src/test/java/com/example/gymcrm/service/impl/TrainerServiceImplTest.java`
- `src/test/java/com/example/gymcrm/service/impl/TrainingServiceImplTest.java`
- `src/test/java/com/example/gymcrm/service/ValidationSupportTest.java`
- `src/test/java/com/example/gymcrm/generator/GeneratorTest.java`
- `src/test/java/com/example/gymcrm/initializer/StorageBeanPostProcessorTest.java`
- `src/test/java/com/example/gymcrm/facade/GymFacadeTest.java`
- `src/test/java/com/example/gymcrm/domain/DomainModelTest.java`
- `src/test/java/com/example/gymcrm/logging/LoggingSafetyTest.java`
- `src/test/java/com/example/gymcrm/config/SpringContextIntegrationTest.java`
- `src/test/java/com/example/gymcrm/application/GymCrmApplicationTest.java`

Bu notun amaci, testleri sadece "yesil gecen dosyalar" olarak degil,
projenin davranis sozlesmesi olarak okumayi saglamaktir. Testler sana su
sorunun cevabini verir:

```text
Bu uygulama hangi davranislari garanti ediyor?
```

Logging ve coverage ise bu garantiye iki farkli acidan destek verir:

- Logging, calisma zamaninda ne oldugunu anlamani saglar.
- Coverage, testlerin hangi kod satirlarini calistirdigini gosterir.

Bu projede son kabul komutu:

```powershell
.\mvnw.cmd clean verify
```

basarili gecmistir. Son kabulde **37 test** calismis ve JaCoCo coverage check
basarili olmustur.

## 2. Bu Konunun Amaci

Bu konuyu bitirdiginde sunlari net olarak aciklayabilmelisin:

- JUnit 5 testinin temel yapisi nedir?
- Mockito neden kullanilir ve hangi katmanlarda daha anlamlidir?
- AssertJ neden okunabilir assertion yazmayi kolaylastirir?
- FIRST prensipleri iyi unit test yazarken nasil rehberlik eder?
- Unit test ile integration test arasindaki fark nedir?
- Bu projede hangi test sinifi hangi sorumlulugu guvence altina alir?
- Logback config nasil okunur?
- SLF4J log seviyeleri nasil dusunulur?
- Hassas veri neden loglanmamalidir?
- JaCoCo Maven plugin `clean verify` sirasinda ne yapar?
- HTML ve CSV coverage raporu nasil okunur?

Bu bolum, projeyi profesyonel bir gelistirici gibi okumana yardim eder.
Kodun kendisi "ne yapiyorum?" sorusuna cevap verir. Testler ise "bunu neden bu
sekilde yaptigimdan emin miyim?" sorusunu cevaplar.

## 3. Buyuk Resim

Projede test, logging ve coverage akisi soyle dusunulebilir:

```text
Kod degisir
  |
  v
Unit testler davranisi kontrol eder
  |
  v
Integration test Spring context ve wiring'i kontrol eder
  |
  v
Logging testleri hassas veri guvenligini kontrol eder
  |
  v
Maven verify Surefire + JaCoCo calistirir
  |
  v
Coverage raporu uretilir ve minimum esik kontrol edilir
```

`pom.xml` icindeki test bagimliliklari:

- `junit-jupiter`: Test framework.
- `mockito-junit-jupiter`: Mock nesneler ve JUnit 5 entegrasyonu.
- `assertj-core`: Okunabilir ve zincirlenebilir assertion API.

`pom.xml` icindeki ana test plugin'leri:

- `maven-surefire-plugin`: Unit testleri calistirir.
- `jacoco-maven-plugin`: Coverage agent'i hazirlar, rapor uretir ve coverage
  esigini kontrol eder.

Bu projede JaCoCo kuralinin merkezinde su vardir:

```xml
<counter>LINE</counter>
<value>COVEREDRATIO</value>
<minimum>0.80</minimum>
```

Yani bundle seviyesinde line coverage en az yuzde 80 olmalidir. Coverage
check gecmezse `verify` fazi basarisiz olur.

## 4. Test Kavramlari

### JUnit 5 nedir?

JUnit 5, Java dunyasinda en yaygin test framework'lerinden biridir. Bu projede
test siniflari genellikle su yapidadir:

```java
class SomeTest {
    @Test
    void expectedBehavior() {
        // arrange
        // act
        // assert
    }
}
```

`@Test`, metodun test olarak calistirilacagini soyler. Test metodu `void`
doner ve basarisizlik genellikle assertion hatasi ya da beklenmeyen exception
ile ortaya cikar.

Projede JUnit 5'in kullanildigi bazi yapilar:

- `@Test`: Test metodunu isaretler.
- `@BeforeEach`: Her testten once ortak hazirligi yapar.
- `@ExtendWith(MockitoExtension.class)`: Mockito'nun JUnit 5 ile calismasini
  saglar.
- `@TempDir`: Teste izole gecici klasor verir.

### Arrange, Act, Assert

Iyi bir test genellikle uc bolume ayrilir:

```text
Arrange: Test verisini ve bagimliliklari hazirla.
Act: Test edilen davranisi calistir.
Assert: Sonucun dogru oldugunu kanitla.
```

Ornek olarak `TraineeServiceImplTest` icinde create akisi:

```text
Arrange:
  usernameGenerator ve passwordGenerator mock'lari hazirlanir.
  traineeDao.create davranisi stub'lanir.

Act:
  service.create(command) cagrilir.

Assert:
  id, firstName, lastName, address, username ve password dogrulanir.
```

Bu ayrim testin ders gibi okunmasini saglar. Teste bakan kisi once senaryoyu,
sonra eylemi, sonra beklenen sonucu gorur.

### Mockito nedir?

Mockito, test sirasinda gercek bagimliliklar yerine kontrollu sahte nesneler
kullanmamizi saglar.

Service testlerinde bunun cok net bir faydasi vardir:

```text
Service test edilirken DAO'nun gercek Map davranisi test edilmez.
DAO yerine mock kullanilir.
Boylece service'in is kurali izole edilir.
```

Projede ornek:

```java
@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {
    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;
}
```

Bu yapiyla service testinde sunlari yapabiliriz:

- `when(...).thenReturn(...)` ile bagimliligin ne donecegini belirleriz.
- `when(...).thenAnswer(...)` ile arguman uzerinden dinamik cevap veririz.
- `verify(...)` ile bir metodun cagrildigini kontrol ederiz.

### Stub ve mock farki

Gunluk kullanimda bu iki kelime karisabilir:

- Stub: Test sirasinda belli input icin belli output donduren sahte davranis.
- Mock: Cagrilip cagrilmadigini, hangi parametreyle cagrildigini da
  dogrulayabildigimiz sahte nesne.

Ornek stub:

```java
when(usernameGenerator.generate("Alice", "Brown")).thenReturn("Alice.Brown");
```

Ornek interaction verification:

```java
verify(traineeDao).deleteById(1L);
```

### AssertJ nedir?

AssertJ, assertion yazmayi okunabilir hale getiren bir kutuphanedir. Bu
projede klasik `assertEquals(expected, actual)` yerine genellikle su stil
kullanilir:

```java
assertThat(result.getUsername()).isEqualTo("Alice.Brown");
assertThat(service.findById(1L)).containsSame(trainee);
assertThatThrownBy(() -> service.create(null))
        .isInstanceOf(ValidationException.class);
```

Bu stilin avantaji:

- Cumle gibi okunur.
- Collection, Optional, String ve exception kontrolleri daha rahattir.
- Hata mesajlari genellikle daha acik olur.

Projede sik gorulen AssertJ kaliplari:

- `assertThat(value).isEqualTo(expected)`
- `assertThat(list).containsExactly(item)`
- `assertThat(optional).containsSame(entity)`
- `assertThatThrownBy(() -> action()).isInstanceOf(...)`
- `assertThatCode(() -> action()).doesNotThrowAnyException()`
- `assertThat(text).doesNotContain(...)`

### FIRST prensipleri

FIRST, iyi unit testlerin temel niteliklerini hatirlatan bir kisaltmadir:

```text
F - Fast
I - Independent
R - Repeatable
S - Self-validating
T - Timely
```

#### Fast

Test hizli calismalidir. Bu projede testler genellikle:

- In-memory `HashMap` kullanir.
- Dis servis kullanmaz.
- Gercek database kullanmaz.
- Spring context'i sadece integration testte acar.

Bu sayede test suite hizli geri bildirim verir.

#### Independent

Testler birbirinden bagimsiz olmalidir. Bir testin sonucu onceki testin
calisip calismamasina bagli olmamalidir.

Projede bu su yollarla saglanir:

- Her test kendi `HashMap` storage'ini kurar.
- Service testlerinde `@BeforeEach` ile temiz service nesnesi olusturulur.
- `@TempDir` ile initializer testlerinde izole dosya sistemi kullanilir.

#### Repeatable

Test ayni kosullarda her zaman ayni sonucu vermelidir. Bu ozellikle random,
tarih ve dosya sistemi kullanan kodlarda onemlidir.

Projede password generator random sonuc uretir. Test bu nedenle tek bir
password degerine baglanmaz. Bunun yerine su davranislari kontrol eder:

- Uzunluk 10 karakter mi?
- Sadece izinli karakterlerden mi olusuyor?
- 50 denemede birden fazla farkli sonuc uretiyor mu?

Boylece test random davranisi makul sekilde dogrular.

#### Self-validating

Testin sonucu gozle yorumlanmamalidir. Test ya gecer ya kalir.

Kotu ornek:

```java
System.out.println(result);
```

Iyi ornek:

```java
assertThat(result.getId()).isEqualTo(1L);
```

Bu projedeki testler assertion tabanlidir; yani test sonucunu Maven kendisi
yorumlar.

#### Timely

Test, davranisla birlikte veya davranistan hemen sonra yazilmalidir. Bu proje
icin ders calisirken "yeni bir is kurali eklersem hangi testi de eklemeliyim?"
sorusunu sormak bu prensibin pratik karsiligidir.

### Unit test nedir?

Unit test, kucuk bir birimi izole sekilde test eder. Birim bazen bir metod,
bazen bir sinif, bazen kucuk bir davranis kumesidir.

Bu projede unit test ornekleri:

- `ValidationSupportTest`
- `GeneratorTest`
- `TraineeServiceImplTest`
- `TrainerServiceImplTest`
- `TrainingServiceImplTest`
- `MapDaoTest`
- `DomainModelTest`
- `GymFacadeTest`

Unit testlerde amac, tek bir sorumlulugu hizli ve net sekilde dogrulamaktir.

### Integration test nedir?

Integration test, birden fazla parcanin birlikte dogru calisip calismadigini
kontrol eder.

Bu projede en net integration test:

```text
SpringContextIntegrationTest
```

Bu test `AnnotationConfigApplicationContext` ile Spring context'i acar ve
sunlari dogrular:

- `AppConfig` context'i yukleyebiliyor mu?
- Storage bean'leri ayri `Map` instance'lari mi?
- `StorageBeanPostProcessor` initial data yukleyebiliyor mu?
- `GymFacade` bean'i wire edilmis mi?
- Facade uzerinden yeni trainee olusturulunca username cakismasi cozuluyor mu?

Bu test tek bir sinifi degil, config + storage + initializer + facade + service
+ DAO zincirini birlikte kontrol eder.

## 5. Logging Kavramlari

### Logging neden var?

Logging, uygulama calisirken ne oldugunu anlamamizi saglar. Debugger olmadan
calisan bir uygulamada loglar, sistemin iz birakma bicimidir.

Ama log her seyi yazmak degildir. Iyi log:

- Davranisi anlamaya yardim eder.
- Sorun oldugunda iz surdurur.
- Gereksiz gurultu uretmez.
- Hassas veriyi aciga cikarmaz.

Bu proje icin hassas sayilabilecek veriler:

- `password`
- `username`
- `address`
- `dateOfBirth`
- Training adi gibi kullaniciya ozel detaylar

Bu nedenle domain siniflarinin `toString()` metotlari ve DAO loglari guvenli
ozet mantigiyla ele alinmalidir.

### SLF4J nedir?

SLF4J bir logging facade'idir. Yani uygulama kodu dogrudan Logback'e
baglanmak yerine SLF4J API'sini kullanir.

Pratik anlamda:

```java
private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
```

ve sonra:

```java
LOGGER.debug("Created Trainee id={}", trainee.getId());
```

SLF4J sayesinde ileride logging backend degisse bile uygulama kodunun buyuk
kismi degismek zorunda kalmaz.

### Logback nedir?

Logback, SLF4J icin kullanilan logging implementasyonudur. Bu projede config
dosyasi:

```text
src/main/resources/logback.xml
```

Dosyada console appender vardir:

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

Pattern sunlari basar:

- Saat: `%d{HH:mm:ss.SSS}`
- Log seviyesi: `%-5level`
- Thread adi: `[%thread]`
- Logger adi: `%logger{36}`
- Mesaj: `%msg`
- Yeni satir: `%n`

Projeye ozel logger:

```xml
<logger name="com.example.gymcrm" level="DEBUG"/>
```

Root logger:

```xml
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
</root>
```

Yani `com.example.gymcrm` paketi DEBUG seviyesinden itibaren log uretir. Root
logger ise genel olarak INFO seviyesindedir.

### SLF4J log seviyeleri

Log seviyelerini pratikte soyle dusunebilirsin:

- `TRACE`: Cok detayli, adim adim teknik izleme.
- `DEBUG`: Gelistirme ve sorun arastirma icin detay.
- `INFO`: Normal is akisi icin anlamli olaylar.
- `WARN`: Beklenmeyen ama uygulamayi hemen durdurmayan durumlar.
- `ERROR`: Basarisiz islem veya ciddi hata.

Bu projede DAO loglari genellikle teknik davranisi gostermek icin uygundur:

```text
Created Trainee id=...
Finding Trainee id=...
Listed Trainee count=...
```

Bu mesajlar faydalidir cunku storage davranisini izletir. Fakat password veya
address gibi veriler yazilmaz.

### Hassas veri loglamama

Hassas veri loglamak iki nedenle tehlikelidir:

- Guvenlik: Log dosyalari genellikle uygulama verisinden daha genis erisime
  sahip olabilir.
- Gizlilik: Kullaniciya ait kisisel bilgiler hata ayiklama amaciyla bile olsa
  gereksiz yere saklanmamalidir.

Bu projede `LoggingSafetyTest`, bu kuralin testidir. Testin yaptigi sey:

```text
TraineeDaoImpl logger'ina ListAppender baglar.
Bir trainee create/find/list akisi calistirir.
Toplanan log mesajlarini tek metne cevirir.
Mesajlarda password, address ve dateOfBirth olmadigini dogrular.
Ayni zamanda beklenen guvenli log mesajlarinin oldugunu dogrular.
```

Bu test onemlidir cunku "yanlislikla entity'nin tamamini loglamak" gibi cok
kolay yapilabilecek bir hatayi yakalar.

## 6. Coverage Kavramlari

### Coverage nedir?

Coverage, testler calisirken hangi kod bolumlerinin calistigini olcer.

En yaygin coverage turleri:

- Line coverage: Kac kod satiri calisti?
- Branch coverage: `if`, `else`, ternary gibi dallarin kaci calisti?
- Method coverage: Kac metod calisti?
- Class coverage: Kac sinif calisti?

Bu projedeki JaCoCo check, line coverage uzerinden calisir:

```text
Bundle line covered ratio >= 0.80
```

Yani proje geneli satir coverage orani yuzde 80'in altina duserse build
basarisiz olur.

### Coverage neyi kanitlar, neyi kanitlamaz?

Coverage sunu kanitlar:

```text
Bu satir test sirasinda calisti.
```

Coverage sunu kanitlamaz:

```text
Bu satir dogru davranisi uretti.
```

Bu ayrim cok onemlidir. Yuzde 100 coverage olan bir projede bile zayif
assertion varsa bug kalabilir.

Kotu test:

```java
service.create(command);
```

Bu satir coverage'i artirir ama sonucu dogrulamaz.

Daha iyi test:

```java
Trainee result = service.create(command);

assertThat(result.getUsername()).isEqualTo("Alice.Brown");
assertThat(result.getPassword()).isEqualTo("A1b2C3d4E5");
```

Bu hem kodu calistirir hem davranisi dogrular.

### JaCoCo Maven plugin nasil calisir?

`pom.xml` icinde JaCoCo plugin iki ana is yapar:

1. `prepare-agent`
   - Testler calisirken JVM'e JaCoCo agent ekler.
   - Hangi satirlarin calistigini takip eder.

2. `report` ve `check`
   - `verify` fazinda rapor uretir.
   - Coverage kurallarini kontrol eder.

Bu nedenle sadece `mvn test` calistirmak ile `mvn verify` calistirmak ayni
degildir:

```text
mvn test:
  Testleri calistirir.

mvn verify:
  Testleri calistirir.
  JaCoCo raporunu uretir.
  JaCoCo coverage check yapar.
```

Wrapper ile Windows komutu:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS komutu:

```bash
./mvnw clean verify
```

### HTML coverage raporu okuma

`clean verify` sonrasinda JaCoCo HTML raporu su klasorde olusur:

```text
target/site/jacoco/index.html
```

HTML raporda genellikle su bilgileri gorursun:

- Package listesi
- Class listesi
- Instruction coverage
- Branch coverage
- Line coverage
- Method coverage
- Class coverage

Renkleri soyle okuyabilirsin:

- Yesil: Test sirasinda calismis.
- Kirmizi: Test sirasinda calismamis.
- Sari: Kismen calismis, genellikle branch eksigi vardir.

Bir class'a tikladiginda satir satir hangi kodun testte calistigini
gorebilirsin.

### CSV coverage raporu okuma

JaCoCo CSV raporu su dosyada olusur:

```text
target/site/jacoco/jacoco.csv
```

CSV rapor otomasyon veya hizli analiz icin kullanislidir. Tipik kolonlar:

- `GROUP`
- `PACKAGE`
- `CLASS`
- `INSTRUCTION_MISSED`
- `INSTRUCTION_COVERED`
- `BRANCH_MISSED`
- `BRANCH_COVERED`
- `LINE_MISSED`
- `LINE_COVERED`
- `COMPLEXITY_MISSED`
- `COMPLEXITY_COVERED`
- `METHOD_MISSED`
- `METHOD_COVERED`

Line coverage oranini elle dusunmek istersen:

```text
LINE_COVERED / (LINE_COVERED + LINE_MISSED)
```

CSV dosyasinda cok dusuk kalan class'lar, yeni test yazmak icin iyi adaydir.

### Coverage hedefi nasil okunmali?

Bu projede coverage hedefi bir kalite sinyalidir, kalite garantisi degildir.

Dogru yaklasim:

```text
Once kritik davranisi test et.
Sonra coverage raporunda bos kalan anlamli yerleri tamamla.
```

Yanlis yaklasim:

```text
Sadece coverage yuzdesini artirmak icin anlamsiz test yaz.
```

Bir testin degeri, calistirdigi satir sayisindan cok yakaladigi davranis
sozlesmesiyle olculmelidir.

## 7. Bu Projede Nerede?

### Maven test altyapisi

`pom.xml` icinde test ve coverage icin ana bolumler vardir:

- JUnit 5 dependency: Test framework.
- Mockito dependency: Mock destekleri.
- AssertJ dependency: Assertion destekleri.
- Surefire plugin: Testleri calistirir.
- JaCoCo plugin: Coverage agent, report ve check islemlerini yapar.

Surefire config icinde su kisim dikkat ceker:

```xml
<useModulePath>false</useModulePath>
<argLine>@{argLine} -javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar</argLine>
```

Bu ayar Mockito'nun Java agent ihtiyacini Maven test calisma surecine ekler.
`@{argLine}` kismi JaCoCo'nun kendi agent parametresini de korur. Yani Mockito
ve JaCoCo ayni test kosusunda birlikte calisabilir.

### DAO testleri

Dosya:

```text
src/test/java/com/example/gymcrm/dao/impl/MapDaoTest.java
```

Bu testler DAO katmaninin teknik storage davranisini kontrol eder:

- Seed edilmis storage'da yeni id sirasi devam ediyor mu?
- `create`, `findById`, `findAll`, `update`, `deleteById` calisiyor mu?
- Eksik entity update/delete edilirse `EntityNotFoundException` firliyor mu?
- Storage inject edilmeden DAO kullanilirsa hata veriyor mu?
- Trainer ve Training DAO implementasyonlari temel CRUD davranisini sagliyor
  mu?

Burada mock kullanilmamasi dogrudur. Cunku test edilen sey zaten gercek
`HashMap` storage davranisidir.

### Service testleri

Dosyalar:

```text
src/test/java/com/example/gymcrm/service/impl/TraineeServiceImplTest.java
src/test/java/com/example/gymcrm/service/impl/TrainerServiceImplTest.java
src/test/java/com/example/gymcrm/service/impl/TrainingServiceImplTest.java
src/test/java/com/example/gymcrm/service/ValidationSupportTest.java
```

Service testleri is kurallarini izole eder:

- Create sirasinda alanlar trim ediliyor mu?
- Username ve password generator cagrilari dogru kullaniliyor mu?
- Update sirasinda username/password korunuyor mu?
- Gecersiz input `ValidationException` uretiyor mu?
- Eksik profile `EntityNotFoundException` uretiyor mu?
- Training create sirasinda trainee ve trainer varligi kontrol ediliyor mu?
- Duration pozitif olmak zorunda mi?
- `findById`, `findAll`, `delete` dogru bagimliliga delege ediliyor mu?

Bu testlerde DAO ve generator bagimliliklari mock'tur. Boylece testin konusu
sadece service davranisi olur.

### Generator testleri

Dosya:

```text
src/test/java/com/example/gymcrm/generator/GeneratorTest.java
```

Bu testler iki kritik davranisi kontrol eder:

- `UniqueUsernameGenerator`, trainee ve trainer storage'larini birlikte kontrol
  ediyor mu?
- Username kontrolu case-insensitive mi?
- Cakisma varsa suffix dogru ekleniyor mu?
- Entity silindiginde username tekrar kullanilabiliyor mu?
- Storage eksikse `IllegalStateException` firliyor mu?
- `SecurePasswordGenerator` 10 karakterli ve izinli karakterlerden olusan
  password uretiyor mu?

Burada password testinde tek bir sabit password beklenmez. Cunku algoritma
random calisir. Test, random davranisin sinirlarini dogrular.

### Initializer testleri

Dosya:

```text
src/test/java/com/example/gymcrm/initializer/StorageBeanPostProcessorTest.java
```

Bu testler JSON initial data yukleme davranisini kontrol eder:

- Bilinen storage bean'leri ayni dosyadan populate ediliyor mu?
- Bilinmeyen bean veya alakasiz obje oldugu gibi donuyor mu?
- Eksik dosya fail-fast hata veriyor mu?
- Malformed JSON hata veriyor mu?
- Duplicate id reddediliyor mu?
- Non-positive id reddediliyor mu?
- Required user fields eksikse hata veriliyor mu?
- `InitialData` null section'lari empty list gibi ele aliyor mu?

`@TempDir` burada cok iyi bir tercihtir. Test, gercek repo dosyalarini
degistirmeden gecici JSON dosyalariyla calisir.

### Facade testleri

Dosya:

```text
src/test/java/com/example/gymcrm/facade/GymFacadeTest.java
```

Bu test `GymFacade` sinifinin ana gorevini dogrular:

```text
Facade kendisi is kurali uretmez.
Public operasyonlari dogru service'e delege eder.
```

Bu nedenle testte service'ler mock'tur ve her facade metodunun dogru servis
sonucunu dondurdugu veya dogru service metodunu cagirdigi kontrol edilir.

### Domain testleri

Dosya:

```text
src/test/java/com/example/gymcrm/domain/DomainModelTest.java
```

Bu testler domain nesnelerinin temel getter/setter davranisini ve guvenli
`toString()` politikasini kontrol eder:

- `Trainee` alanlari set/get edilebiliyor mu?
- `Trainer` specialization alanini tasiyor mu?
- `Training` alanlari set/get edilebiliyor mu?
- `toString()` password, address, username veya private session name gibi
  hassas detaylari icermiyor mu?

Bu testler basit gorunebilir ama logging guvenligiyle birlikte dusunuldugunde
onemlidir. Birisi yanlislikla `toString()` icine hassas alan eklerse test
yakalayabilir.

### Logging testleri

Dosya:

```text
src/test/java/com/example/gymcrm/logging/LoggingSafetyTest.java
```

Bu test Logback'in `ListAppender` sinifini kullanarak log mesajlarini bellek
icine toplar. Sonra sunu dogrular:

- Log mesajlarinda password yok.
- Log mesajlarinda address yok.
- Log mesajlarinda date of birth yok.
- Beklenen guvenli operasyon mesajlari var.

Bu testin en kritik bolumu `finally` blogudur:

```java
logger.detachAppender(appender);
appender.stop();
```

Appender temizlenmezse sonraki testlere yan etki kalabilir. Bu da FIRST
prensiplerinden `Independent` maddesine zarar verir.

### Spring context testleri

Dosya:

```text
src/test/java/com/example/gymcrm/config/SpringContextIntegrationTest.java
```

Bu test projenin Spring Core tarafini butun halinde kontrol eder:

- `AnnotationConfigApplicationContext(AppConfig.class)` aciliyor mu?
- `traineeStorage`, `trainerStorage`, `trainingStorage` bean'leri var mi?
- Storage map'leri birbirinden ayri instance'lar mi?
- Initial data yuklendigi icin her storage baslangicta 1 kayit iceriyor mu?
- `GymFacade` bean'i alinabiliyor mu?
- Facade uzerinden yeni trainee create edilebiliyor mu?
- Duplicate isimde username suffix mantigi context icinde de calisiyor mu?
- Password uzunlugu 10 mu?

Bu test unit test degildir. Cunku birden fazla katmani birlikte calistirir.
Tam da bu nedenle degerlidir.

### Application testleri

Dosya:

```text
src/test/java/com/example/gymcrm/application/GymCrmApplicationTest.java
```

Bu test uygulama giris noktasini kontrol eder:

- `GymCrmApplication.main(new String[0])` exception atmadan calisiyor mu?
- `GymCrmApplication.main(new String[]{"--demo"})` demo senaryosunu exception
  atmadan calistiriyor mu?

Bu testler cok detayli assertion yapmaz. Buradaki amac smoke test'tir:

```text
Uygulama baslayip kapanabiliyor mu?
```

## 8. Test Okuma Rotasi

Projeyi ders gibi tekrar ederken testleri su sirayla oku:

### 1. Domain ve validation

Once en temel davranislari oku:

```text
DomainModelTest
ValidationSupportTest
```

Bu iki test sana sunlari ogretir:

- Domain nesneleri hangi alanlari tasiyor?
- Hassas alanlar `toString()` icinde nasil korunuyor?
- Ortak validation metotlari hangi inputlari reddediyor?

### 2. DAO ve storage

Sonra storage davranisini oku:

```text
MapDaoTest
```

Burada su sorulara cevap ara:

- Id nasil uretiliyor?
- Seed data varsa siradaki id nasil bulunuyor?
- Missing update/delete nasil davranir?
- Storage injection neden zorunlu?

### 3. Generator

Sonra kullanici olusturma desteklerini oku:

```text
GeneratorTest
```

Burada su sorulara cevap ara:

- Username iki storage arasinda nasil unique tutuluyor?
- Case-insensitive kontrol neden gerekli?
- Random password nasil test edilir?

### 4. Service

Sonra is kurallarini oku:

```text
TraineeServiceImplTest
TrainerServiceImplTest
TrainingServiceImplTest
```

Burada en onemli calisma teknigi sudur:

```text
Her test metodunun adini oku.
Sonra production code'da karsilik gelen metodu bul.
Testteki her assertion'in hangi is kuralini korudugunu yaz.
```

### 5. Initializer

Sonra JSON yukleme ve BeanPostProcessor akisini oku:

```text
StorageBeanPostProcessorTest
```

Bu test, Spring lifecycle ve veri yukleme davranisini anlamak icin cok
degerlidir.

### 6. Facade

Sonra disariya acilan basit API katmanini oku:

```text
GymFacadeTest
```

Burada facade pattern'in bu projedeki pratik karsiligini gorursun.

### 7. Logging

Sonra log guvenligini oku:

```text
LoggingSafetyTest
```

Bu test sana "log da test edilebilir bir davranistir" fikrini verir.

### 8. Spring context ve application

En son butun sistemi birlikte calistiran testleri oku:

```text
SpringContextIntegrationTest
GymCrmApplicationTest
```

Bu iki test, parcalarin tek tek degil birlikte de calistigini gosterir.

## 9. Kisa Kod Okuma Notlari

### `@ExtendWith(MockitoExtension.class)`

Bu annotation Mockito'nun JUnit 5 icinde calismasini saglar. Bu sayede
`@Mock` ile isaretlenen alanlar testten once otomatik hazirlanir.

Service testlerinde bu yapi sik gorulur:

```java
@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;
}
```

### `@BeforeEach`

Her testten once yeni service instance'i kurmak icin kullanilir.

```java
@BeforeEach
void setUp() {
    service = new TraineeServiceImpl();
    service.setTraineeDao(traineeDao);
}
```

Bu sayede testler ortak state paylasmaz.

### `when(...).thenReturn(...)`

Mock bagimliligin beklenen cevabini verir:

```java
when(traineeDao.findById(2L)).thenReturn(Optional.of(current));
```

Bu satir "service 2 id'li trainee'yi ararsa bunu bulsun" demektir.

### `thenAnswer(...)`

Donus degeri argumana bagliysa kullanilir:

```java
when(traineeDao.create(any())).thenAnswer(invocation -> {
    Trainee trainee = invocation.getArgument(0);
    trainee.setId(1L);
    return trainee;
});
```

Bu, DAO create davranisini kucuk bir sahte implementasyonla taklit eder.

### `assertThatThrownBy(...)`

Exception beklenen senaryolarda kullanilir:

```java
assertThatThrownBy(() -> service.create(null))
        .isInstanceOf(ValidationException.class);
```

Bu assertion hem exception firladigini hem tipinin dogru oldugunu kontrol eder.

### `assertThatCode(...)`

Exception firlamamasini bekledigimiz smoke testlerde kullanilir:

```java
assertThatCode(() -> GymCrmApplication.main(new String[0]))
        .doesNotThrowAnyException();
```

### `@TempDir`

JUnit 5'in gecici klasor destegidir. Initializer testlerinde dosya sistemi
gerektigi icin kullanilir.

Avantaji:

- Test repo dosyalarini bozmaz.
- Her test izole bir klasor alir.
- Test bittiginde JUnit temizlik yapar.

### `ListAppender`

Logback'in testlerde log yakalamak icin kullanilabilen appender'idir.

`LoggingSafetyTest` su akisi kullanir:

```text
Logger'i al.
ListAppender olustur.
Appender'i logger'a bagla.
Kod davranisini calistir.
Appender listesi icindeki mesajlari oku.
Assertion yap.
Finally icinde appender'i ayir ve durdur.
```

### Optional assertion

DAO ve service find metotlari `Optional` doner. AssertJ bunu temiz okutur:

```java
assertThat(service.findById(1L)).containsSame(trainee);
assertThat(dao.findById(6L)).isEmpty();
```

### Collection assertion

Liste icerigi ve sirasi onemliyse:

```java
assertThat(dao.findAll()).containsExactly(training);
```

Alan uzerinden kontrol etmek istersen:

```java
assertThat(dao.findAll())
        .extracting(Trainee::getId)
        .containsExactly(5L, 6L);
```

## 10. Sik Dusulen Hatalar

### 1. Unit testte cok fazla gercek bagimlilik calistirmak

Service testinde gercek DAO, gercek storage ve gercek generator kullanirsan
testin konusu bulanir. Service testinin amaci service is kuralidir. DAO
davranisi `MapDaoTest` tarafinda ayrica test edilir.

### 2. Mock'lanan seyi test etmek

Bir mock'un ic davranisini test edemezsin. Mock zaten senin soyledigini yapar.

Kotu yaklasim:

```text
Mock DAO create metodunun gercek id urettigini sanmak.
```

Dogru yaklasim:

```text
DAO id uretimini MapDaoTest ile gercek DAO uzerinden test etmek.
Service testinde DAO'nun beklenen sonucu dondurdugunu varsaymak.
```

### 3. Assertion yazmadan sadece metodu cagirmak

Sadece metodu cagirmak coverage'i artirabilir ama davranisi garanti etmez.

Her test kendine sunu sormalidir:

```text
Bu test hangi sonucu kanitliyor?
```

### 4. Random sonucu sabit deger bekleyerek test etmek

Password generator random oldugu icin test `A1b2C3d4E5` gibi sabit bir gercek
sonuc beklememelidir. Bunun yerine uzunluk, karakter seti ve cesitlilik
kontrol edilmelidir.

### 5. Loglarda entity'nin tamamini yazmak

Su tarz loglar risklidir:

```java
LOGGER.debug("Created trainee {}", trainee);
```

Eger `toString()` ileride hassas alan icerirse loglar da hassas veri tasir.
Daha guvenli yaklasim:

```java
LOGGER.debug("Created Trainee id={}", trainee.getId());
```

### 6. Appender'i test sonunda temizlememek

Logging testlerinde logger'a appender eklersen test sonunda cikarmalisin.
Aksi halde sonraki testlerde beklenmeyen log birikimi olabilir.

### 7. Coverage yuzdesini kaliteyle karistirmak

Yuksek coverage iyi sinyaldir ama tek basina yeterli degildir. Kritik olan:

- Dogru edge case'leri test etmek.
- Dogru exception'lari beklemek.
- Hassas verinin sizmadigini kontrol etmek.
- Integration akisini en az bir smoke test ile guvenceye almak.

### 8. `mvn test` ile `mvn verify` farkini unutmak

Bu projede coverage check `verify` fazinda calisir. Sadece testleri calistirmak
icin `test` yeterli olabilir, ama kabul kontrolu icin:

```powershell
.\mvnw.cmd clean verify
```

kullanilmalidir.

### 9. Test isimlerini belirsiz yazmak

`testCreate` gibi isimler zayiftir. Bu projedeki isimler daha davranis
odaklidir:

```text
createsTraineeWithGeneratedCredentialsAndNormalizedFields
updatesProfileButPreservesCredentials
rejectsMissingTraineeOrTrainer
```

Iyi test ismi, test govdesini okumadan once senaryoyu anlatir.

### 10. Integration testi unit test gibi okumak

`SpringContextIntegrationTest` yavaslamaya daha acik ve daha genis kapsamli bir
testtir. Burada amac tek bir service metodunu izole etmek degil, Spring
configuration ve wiring'in birlikte calistigini kanitlamaktir.

## 11. Kendini Test Et

1. `TraineeServiceImplTest` neden gercek `TraineeDaoImpl` yerine mock
   `TraineeDao` kullanir?

2. `MapDaoTest` neden mock kullanmadan gercek `HashMap` ile calisir?

3. `assertThatThrownBy` ile `assertThatCode` arasindaki fark nedir?

4. FIRST prensiplerinden `Independent` bu projede hangi test teknikleriyle
   destekleniyor?

5. `LoggingSafetyTest` hangi hassas alanlarin loglarda gorunmedigini kontrol
   eder?

6. `@TempDir` initializer testlerinde neden iyi bir tercihtir?

7. `UniqueUsernameGenerator` testlerinde case-insensitive kontrol neden
   onemlidir?

8. `clean verify` komutu `test` komutundan farkli olarak hangi JaCoCo
   islemlerini yapar?

9. Coverage raporunda bir satirin yesil olmasi o satirin dogru oldugunu kesin
   olarak kanitlar mi? Neden?

10. `SpringContextIntegrationTest` neden unit test degil integration testtir?

11. `GymCrmApplicationTest` neden detayli alan assertion'lari yapmak yerine
   exception firlamamasini kontrol eder?

12. Bir testte `verify(traineeDao).deleteById(1L)` neyi kanitlar, neyi
   kanitlamaz?

## 12. Mini Pratik

### Pratik 1: Coverage raporu oku

1. Terminalde su komutu calistir:

   ```powershell
   .\mvnw.cmd clean verify
   ```

2. Su dosyayi ac:

   ```text
   target/site/jacoco/index.html
   ```

3. En dusuk line coverage'a sahip package veya class'i bul.

4. Su sorulari cevapla:

   - Eksik kalan satirlar gercek bir davranis riski mi?
   - Bu satirlari test etmek anlamli mi?
   - Yeni test unit test mi olmali, integration test mi?

### Pratik 2: Bir service testini production code ile eslestir

1. `TraineeServiceImplTest` dosyasini ac.
2. `createsTraineeWithGeneratedCredentialsAndNormalizedFields` testini oku.
3. Sonra `TraineeServiceImpl#create` metodunu oku.
4. Testteki her assertion icin su formatta not al:

   ```text
   Assertion:
   Korudugu is kurali:
   Production code'daki karsiligi:
   ```

Bu pratik, testin aslinda nasil bir davranis sozlesmesi oldugunu gosterir.

### Pratik 3: Logging guvenligi kontrolu yap

1. `LoggingSafetyTest` dosyasini oku.
2. `TraineeDaoImpl` icindeki log satirlarini bul.
3. Su sorulari cevapla:

   - Log mesajlari debug icin yeterli bilgi veriyor mu?
   - Hangi alanlar bilerek loglanmiyor?
   - Yanlislikla `trainee` nesnesinin tamamini loglarsan hangi test bunu
     yakalar?

### Pratik 4: Yeni edge case test tasarla

Kod yazmadan once sadece test senaryosu tasarla:

```text
TrainingServiceImpl icin hangi edge case eksik olabilir?
Input ne olur?
Beklenen exception veya sonuc ne olur?
Mock'lar nasil davranmali?
```

Sonra bu senaryonun unit test mi integration test mi olduguna karar ver.

### Pratik 5: CSV coverage hesabi yap

1. `target/site/jacoco/jacoco.csv` dosyasini ac.
2. Bir class satiri sec.
3. `LINE_COVERED` ve `LINE_MISSED` degerlerini bul.
4. Su formulle line coverage'i hesapla:

   ```text
   LINE_COVERED / (LINE_COVERED + LINE_MISSED)
   ```

5. HTML rapordaki yuzdeyle karsilastir.

## 13. Mulakat / Sinav Sorulari

1. Unit test ile integration test arasindaki farki bu projeden iki ornekle
   acikla.

2. Mockito kullanmanin service testlerinde sagladigi ana avantaj nedir?

3. `when(...).thenReturn(...)` ile `verify(...)` arasindaki fark nedir?

4. AssertJ'in klasik JUnit assertion'larina gore okunabilirlik avantaji nedir?

5. FIRST prensiplerini say ve bu projeden her biri icin bir ornek ver.

6. Random password ureten bir sinif nasil test edilir?

7. Logback config icinde root logger ile package logger arasindaki fark nedir?

8. SLF4J neden dogrudan Logback kullanmaktan daha esnek bir yaklasimdir?

9. Hassas veri loglamamak icin hangi kodlama aliskanliklarini edinmelisin?

10. JaCoCo line coverage neyi olcer? Branch coverage ile farki nedir?

11. Coverage check'in Maven lifecycle icinde `verify` fazina baglanmasinin
    pratik sonucu nedir?

12. Yuzde 80 coverage esigi olan bir projede yuzde 85 coverage gormek neden
    tek basina "kalite tamam" anlamina gelmez?

13. `SpringContextIntegrationTest` icinde storage map'lerinin farkli instance
    oldugunu kontrol etmek neden onemlidir?

14. `LoggingSafetyTest` icinde `finally` blogu neden gereklidir?

15. Bir facade testinde hangi davranisi test etmek mantiklidir, hangi davranisi
    tekrar test etmek gereksizdir?

## 14. Ozet

Bu projede testler katman katman dusunulmustur:

- Domain testleri modelin temel davranisini ve guvenli `toString()` kuralini
  korur.
- DAO testleri in-memory `Map` storage davranisini dogrular.
- Service testleri is kurallarini Mockito ile izole eder.
- Generator testleri username/password algoritmalarini kontrol eder.
- Initializer testleri JSON'dan storage doldurma akisini test eder.
- Facade testleri public operasyonlarin dogru service'e delege edildigini
  gosterir.
- Logging testleri hassas verinin loglara sizmadigini kanitlar.
- Spring context testleri configuration, bean wiring ve initial data akisini
  birlikte dogrular.
- Application testleri uygulama giris noktasinin smoke testidir.

Logging tarafinda `src/main/resources/logback.xml`, console appender ve
`com.example.gymcrm` paketi icin DEBUG seviyesi tanimlar. Bu, gelistirme ve
ders calisma sirasinda akisi gormeyi kolaylastirir. Ancak loglar asla password,
address, date of birth gibi hassas bilgileri tasimamali.

Coverage tarafinda JaCoCo, Maven `verify` fazinda rapor uretir ve bundle line
coverage icin en az yuzde 80 esigini kontrol eder. HTML rapor insan gozuyle
inceleme icin, CSV rapor ise tablo ve otomasyon analizi icin kullanislidir.

En onemli fikir sudur:

```text
Iyi test coverage'i sadece arttirmaz; davranisi aciklar ve gelecekteki
degisikliklere karsi projeyi korur.
```
