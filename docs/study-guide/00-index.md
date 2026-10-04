# Spring Core Gym CRM Calisma Rehberi

Bu klasor projeyi ders calisir gibi tekrar etmek icin hazirlanan konu notlarini icerir.
Her dosya once ilgili kavrami anlatir, sonra bu projedeki somut karsiligini dosya ve kod akisiyla aciklar.

## Okuma Sirasi

1. [Proje gereksinimleri ve mimari](01-project-requirements-and-architecture.md)
2. [Spring Core container ve bean yasam dongusu](02-spring-core-container-and-beans.md)
3. [Domain modeli ve Java modelleme tercihleri](03-domain-model-and-java-basics.md)
4. [DAO katmani ve in-memory Map storage](04-dao-and-map-storage.md)
5. [Service katmani, validasyon ve exception akisi](05-service-validation-and-exceptions.md)
6. [Username ve password uretimi](06-generators-username-and-password.md)
7. [Facade, uygulama girisi ve demo akisi](07-facade-application-flow.md)
8. [Testler, logging ve coverage okuma](08-tests-logging-and-coverage.md)

## Nasil Calisilacak?

- Once dosyayi bastan sona oku.
- "Bu projede nerede?" bolumundeki kaynak dosyalari acip kodu takip et.
- "Kendini test et" sorularini cevaplamadan sonraki konuya gecme.
- "Mini pratik" gorevlerinden en az birini kod uzerinde dene.
- Her konu sonunda kendi cumlenle 3-5 satirlik ozet yaz.

## Kabul Komutlari

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd exec:java
.\mvnw.cmd exec:java "-Dexec.args=--demo"
```

