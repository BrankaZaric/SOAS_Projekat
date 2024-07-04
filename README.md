# Servisno orijentisana arhitektura sistema (Projektni zadatak)

# Tehnologije za izradu projekta 
  - Maven
  - Java programski jezik
  - Docker
  - H2 in-memory baza podataka

# Aplikacija se sastoji od sledećih mikroservisa i njihove uloge su:

# 1. Naming server 
  Mikroservis koji predstavlja Eureka server, svi mikroservisi u okviru aplikacije moraju biti
  registrovani na spomenuti server.
  Preporučeni port na kome se mikroservis pokreće: 8761

# 2. Users service 
  Mikroservis koji komunicira sa H2 bazom podataka u okviru koje se čuvaju podaci o korisnicima
  aplikacije. Za svakog korisnika se beleži id,email adresa, lozinka i uloga. Svaki korisnik može da
  poseduje samo jednu od sledećih uloga: OWNER,ADMIN,USER.
  Ovlašćenja datih uloga su navedena u okviru svakog mikroservisa.
  Ovaj mikroservis treba da obezbedi i funkcionalnosti dodavanja novih kao i ažuriranje i brisanje
  postojećih korisnika.
  
  Autorizacija:
  * OWNER može da dodaje, ažurira i briše sve korisnike
  * ADMIN može da dodaje i ažurira korisnike sa ulogom USER
  * USER nema pristup ovom servisu
  U sistemu može postojati samo jedan korisnik sa ulogom OWNER.

  Preporučeni port na kome se mikroservis pokreće: 8770
  
# 3. Currency exchange 
  Mikroservis koji komunicira sa H2 bazom podataka u okviru koje se nalaze kursevi “Fiat”
  valuta, baza treba da sadrži kurseve razmene za sledeće valute: EUR (euro), USD (američki
  dolar), GBP(britanska funta), CHF (švajcarski franak) i RSD (srpski dinar). Obavezni su kursevi
  razmene svake valute ka svakoj valuti. Vrednosti kurseva mogu biti proizvoljne.
  
  Autorizacija:
  * Korisnik sa bilo kojom ulogom može da pristupi ovom servisu
    
  Preporučeni port na kome se mikroservis pokreće: 8000

# 4. Currency conversion 
  Mikroservis koji predstavlja end-point korisničkih zahteva za razmenu valuta (FIAT). Prilikom
  korisničkog zahteva ovaj mikroservis identifikuje količinu valute za razmenu i vrši proveru
  korisničkih sredstava na odgovarajućem računu (bank account) – ukoliko korisnik poseduje 
  dovoljnu količinu sredstava vrši se razmena valuta na osnovu postojećeg kursa koji se dobija
  komunikacijom sa currency-exchange mikroservisom.
  Rezultat uspešnog izvršavanja ovog servisa je prikaz stanja bankovnog računa odgovarajućeg
  korisnika, nakon izvršene razmene valuta, uz dodatak String-a u kojim se opisuje rezultat 
  transakcije (npr. Uspešno je izvršena razmena EUR: 100 za RSD:11700).
  
  Autorizacija:
  * OWNER ne može da pristupi datom servisu
  * ADMIN ne može da pristupi datom servisu
  * USER je autorizovan za upotrebu datog servisa
    
  Preporučeni port na kome se mikroservis pokreće: 8100

# 5. Bank account 
  Mikroservis koji komunicira sa H2 bazom podataka u okviru koje se nalaze podaci o korisničkim
  bankovnim računima - koji nose informaciju o količini svake fiat valute koje vlasnik računa
  poseduje. Svaki račun treba da sadrži ID, količinu svake od fiat valuta koje su definisane za
  currency exchange mikroservis kao i e-mail adresu koja se vezuje za pojedinačni račun. 
  Bankovni računi su dozvoljeni samo za korisnike sa ulogom USER. E-mail adresa mora da bude 
  jedinstvena i mora da se poklapa sa email-om korisnika iz baze podataka. Svaki korisnik može
  imati samo jedan bankovni račun.
  Ukoliko se u Users servisu doda novi korisnik sa ulogom USER, automatski se za njega kreira
  bankovni račun sa stanjem 0 za sve valute. Brisanje korisnika u Users servisu mora da dovede do 
  automatskog brisanja korisnikovog računa.
  Ovaj mikroservis mora da obezbedi funkcionalnosti pregledanja, dodavanja, ažuriranja i brisanja 
  računa.
  
  Autorizacija:
  * OWNER nije autorizovan za korišćenje datog servisa
  * ADMIN može da dodaje, ažurira i pregleda sve bankovne račune
  * USER može da pregleda samo svoj račun
  - Ukoliko je tokom korišćena aplikacije izbrisan korisnik sa ulogom USER koji poseduje
  bankovni račun, tom prilikom se briše i bankovni račun sa odgovarajućim email-om.

  Preporučeni port na kome se mikroservis pokreće: 8200

# 6. Crypto wallet 
  Mikroservis koji komunicira sa H2 bazom podataka u okviru koje se nalaze podaci o korisničkim
  novčanicima - koji nose informaciju o količini svake crypto valute koje vlasnik novčanika
  poseduje. Svaki novčanik treba da sadrži ID, količinu svake od crypto valuta koje su definisane
  za crypto exchange mikroservis kao i e-mail adresu koja se vezuje za pojedinačni novčanik. 
  Novčanici su dozvoljeni samo za korisnike sa ulogom USER. E-mail adresa novčanika mora da 
  bude jedinstvena i mora da se poklapa sa email-om korisnika iz baze podataka. Svaki korisnik
  može imati samo jedan novčanik.
  Ukoliko se u Users servisu doda novi korisnik sa ulogom USER, automatski se za njega kreira
  novčanik sa stanjem 0 za sve valute. Brisanje korisnika u Users servisu mora da dovede do 
  automatskog brisanja korisnikovog novčanika.
  Ovaj mikroservis mora da obezbedi funkcionalnosti pregledanja, dodavanja, ažuriranja i brisanja 
  novčanika.
  
  Autorizacija:
  * OWNER nije autorizovan za korišćenje datog servisa
  * ADMIN može da dodaje, ažurira i pregleda sve korisničke novčanike
  * USER može da pregleda samo svoj novčanik
    
  Preporučeni port na kome se mikroservis pokreće: 8300

# 7. Crypto exchange 
  Mikroservis koji komunicira sa H2 bazom podataka u okviru koje se nalaze kursevi razmene
  crypto valuta za druge crypto valute, baza treba da sadrži kurseve razmene za 3 crypto valute
  po slobodnom izboru.
  
  Autorizacija:
  * Korisnik sa bilo kojom ulogom može da pristupi ovom servisu
    
  Preporučeni port na kome se mikroservis pokreće: 8400

# 8. Crypto conversion 
  Mikroservis koji predstavlja end-point korisničkih zahteva za razmenu crypto valuta. Prilikom
  korisničkog zahteva ovaj mikroservis identifikuje količinu valute za razmenu i vrši proveru
  korisničkih sredstava na odgovarajućem novčaniku (crypto wallet) – ukoliko korisnik poseduje 
  dovoljnu količinu sredstava vrši se razmena valuta na osnovu postojećeg kursa koji se dobija
  komunikacijom sa crypto-exchange mikroservisom.
  Rezultat uspešnog izvršavanja ovog servisa je prikaz stanja novčanika odgovarajućeg
  korisnika, nakon izvršene razmene valuta, uz dodatak String-a kojim se opisuje rezultat 
  transakcije (npr. Uspešno je izvršena razmena BTC: 1 za EUR: 40000).
  
  Autorizacija:
  * OWNER ne može da pristupi datom servisu
  * ADMIN ne može da pristupi datom servisu
  * USER je autorizovan za upotrebu datog servisa
    
  Preporučeni port na kome se mikroservis pokreće: 8500

# 9. Trade service 
  Mikroservis koji obezbeđuje funkcionalnosti razmene običnih(fiat) i crypto valuta.
  
  Opis funkcionalnosti:
  
  * Razmena fiat u crypto – Sa bankovnog računa se oduzima odgovarajuća svota fiat valute
  nakon čega se, na osnovu kursa razmene između fiat i crypto valuta, povećava svota 
  željene crypto valute na novčaniku. Crypto valute je moguće kupovatisamo uz
  USD(američki dolar) i EUR(euro), ukoliko se u zahtevu nalazi neka druga valuta izvršava 
  se razmena prosleđene valute u dolar ili euro pa tek onda spomenuta razmena u crypto.
  * Razmena crypto u fiat – Sa novčanika se oduzima odgovarajuća svota crypto valute nakon 
  čega se, na osnovu kursa razmene između fiat i crypto valuta, dodaje odgovarajuća svota 
  željene fiat valute na bankovnom računu. Crypto valute je moguće razmeniti samo za
  USD(američki dolar) i EUR(euro), ukoliko se u zahtevu nalazi neka druga fiat valuta
  izvršava se razmena prosleđene crypto valute u dolar ili euro pa tek onda spomenuta 
  razmena u željenu fiat valutu.

  Za uspešan rad ovog mikroservisa neophodno je postojanje baze podataka koja sadrži kurseve
  razmene crypto valuta za USD i EUR i obrnuto.
  Rezultat uspešnog izvršavanja ovog servisa jeste prikaz:
  - Stanja bankovnog računa korisnika ukoliko se crypto valuta pretvara u fiat valutu uz
  dodatak String-a koji predstavlja izveštaj o transakciji (slično kao u mikroservisima
  crypto-conversion i currency-conversion).
  - Stanja korisničkog crypto novčanika ukoliko se fiat valuta pretvara u crypto valutu
  uz dodatak String-a koji predstavlja izveštaj o transakciji (slično kao u
  mikroservisima crypto-conversion i currency-conversion).

  Autorizacija:
  * OWNER ne može da pristupi datom servisu
  * ADMIN ne može da pristupi datom servisu
  * USER je autorizovan za upotrebu datog servisa
    
  Preporučeni port na kome se mikroservis pokreće: 8600

# 10. API Gateway 
  Mikroservis koji predstavlja ulaznu tačku aplikacije, ka njemu se šalju svi korisnički zahtevi.
  API Gateway mora biti pokrenut na portu 8765.
  
  Korisnički zahtevi ka određenim mikroservisima moraju da imaju sledeću formu:
  * localhost:8765/currency-conversion?from=X&to=Y&quantity=Q
    - Zahtev za razmenu običnih(fiat) valuta, Q predstavlja količinu X valute koja se
    razmenjuje za Y valutu

  * localhost:8765/crypto-conversion/?from=X&to=Y&quantity=Q
    - Zahtev za razmenu kripto valuta, Q predstavlja količinu X valute koja se
    razmenjuje za Y valutu

  * localhost:8765/trade-service?from=X&to=Y&quantity=Q
    - Zahtev za razmenu valuta, Q predstavlja količinu X valute koja se razmenjuje za
    Y valutu.

# Docker 
  - napravljene su slike (image) za sve mikroservise i postavljene su u okviru repozitorijuma koji pripada korisiničkom nalogu u okviru Docker hub-a
  - kreiran je i docker-compose.yaml putem koga se pokreće kompletna aplikacija u okviru Docker-a

  Korišćene komande:
  
  Primer za Naming server (pozicionira se u okviru foldera tog mikroservisa)
  * docker build -t image naming-server-1.0.0.jar .
  * docker tag naming-server-1.0.0.jar:latest brankazaric/naming-server:latest
  * docker login
  * docker push brankazaric/naming-server:latest

# TESTIRANJE

  Kredencijali 
  
  ADMIN: admin@uns.ac.rs || admin123
  USER: user@uns.ac.rs || user123
  OWNER:  owner@uns.ac.rs || owner123
  
  # Putanje zahteva 
  
  1. Users service 
  
    http://localhost:8765/users (GET)
    
    http://localhost:8765/users/newUser (POST)
    
    http://localhost:8765/users/{id} (PUT, DELETE)
  
  2. Currency Exchange
  
    http://localhost:8765/currency-exchange?from=EUR&to=RSD
  
  3. Currency Conversion
  
    http://localhost:8765/currency-conversion-feign?from=EUR&to=RSD&quantity=10
  
  4. Bank Account
  
  - USER pregleda samo svoj racun
    http://localhost:8765/bank-account/user
  
    http://localhost:8765/bank-accounts
  
  5. Crypto Wallet
  
    http://localhost:8765/crypto-wallet
  
  6. Crypto Exchange
  
    http://localhost:8765/crypto-exchange?from=BTC&to=ETH
  
  7. Crypto Conversion
  
    http://localhost:8765/crypto-conversion-feign?from=BTC&to=ETH&quantity=10
  
  8. Trade Service
  
    http://localhost:8765/trade-service?from=EUR&to=BTC&quantity=10
