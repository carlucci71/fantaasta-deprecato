# Asta online
Asta online è una applicazione per gestire le asta del fantacalcio. Funziona sia in modalità "admin" singolo utente che con accessi multipli. Sono previsti due stile di presentazioni una per schermi fino a 980px ed una per schermi maggiori.

# Configurazione iniziale
Funziona sia con database MySql che H2 in memory (in questo caso tutte le informazioni si perdono al riavvio del programma, anche se esiste una modalità di recupero da file che verrà spiegata in seguito).

Per creare un database ed un utente su mysql si può usare lo script seguente:
 	
~~~~
CREATE USER 'asta'@'%' IDENTIFIED by 'asta';
GRANT ALL PRIVILEGES ON *.* TO 'asta'@'%' REQUIRE NONE WITH GRANT OPTION MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;GRANT ALL PRIVILEGES ON asta.* TO 'asta'@'%';
CREATE DATABASE asta;
~~~~

Nel file `application.properties` configurare opportunamente le seguenti chiavi (ci sono già dei valori di esempio da scommentare):
~~~~
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
~~~~

Se il database è h2 o la prima esecuzione di mysql configurare le chiavi:
`application.properties`
~~~~
spring.jpa.hibernate.ddl-auto=create
~~~~
`spy.properties`
~~~~
append = false
~~~~

Se il database è mysql, dalla seconda esecuzione cambiare i valori in (se si vuole partire dai dati salvati in precedenza senza perdere tutto ad ogni esecuzione):
~~~~
spring.jpa.hibernate.ddl-auto=update
~~~~
`spy.properties`
~~~~
append = true
~~~~
E' possibile configurare la porta modificando la chiave:
`application.properties`
~~~~
server.port=8081
~~~~

# Attività iniziali dopo il primo accesso
Al primo accesso verrà chiesto di inserire i seguenti valori:
>Numero utenti
Budget
Numero massimo giocatori acquistabili
Numero massimo portieri acquistabili
Numero massimo difensori acquistabili
Numero massimo centrocampisti acquistabili
Numero massimo attaccanti acquistabili
Se le chiamate verranno fatte a turno o chiunque può chiamare senza ordine
Se il fantacalcio sarà in modalità Mantra

Confermando questi dati verrà caricata la pagina di admin in cui sarà possibile:
>Cambiare i nomi degli allenatori
Impostare le password
Attribuire il ruolo di admin agli allenatori
Cambiare l'ordine degli allenatori (per le chiamate a turni)
Modificare altre informazioni già presentate in precedenza

Se si modifica qualche informazione, andrà confermata con il bottone `aggiorna configurazione`.

In questa pagina è presente anche il bottone `AZZERA TUTTO` per cancellare il db e ricominciare.

L'ultima attività di configurazione da effettuare consiste nel caricare la lista dei calciatori. Produrre il file da caricare nelle seguenti modalità:
* fantaservice (se deselezionata la scelta Mantra):
  recuperando l'elenco da https://www.fanta.soccer/it/archivioquotazioni/A/2020-2021/
* leghefantacalcio  (se selezionata la scelta Mantra)
scarica lista svincolati da https://leghe.fantacalcio.it/fanta-viva/lista-svincolati, aprilo con excel, rimuovi le prime 4 righe, -esporta - cambia tipo file - testo delimitato da tabulazione e salva con nome

# Funzionalità di gioco
La pagina è divisa in accordion, in modo da collassare le sezioni che non si vogliono vedere

#### Accordion Link
E' possibile accedere a:
* **pagina di amministrazione** oltre alle funzionalità descritte in precedenza per l'amministratore chiunque potrà personalizzare il proprio nome e la propria password.
* **elenco offerte** oltre alla cronologia delle offerte l'amministratore potrà cancellare una offerta salvata
* **log** cronologia di tutte le operazioni dispositive effettuate con indicazione oraria
* **giocatori liberi** elenco dei giocatori ancora disponibili, filtrabili per nome/ruolo/squadra/quotazione (maggiore di oppure mettendo il - minore di)
* **riepilogo** situazione riassuntiva per allenatore dei giocatori presi

#### Accordion allenatori
Una volta caricato l'url dell'applicazione è possibile connettersi cliccando sull'apposita icona, se si è settata una passowrd verrà richiesta altrimenti l'accesso sarà diretto.
Una volta connessi si potrà uscire scollegarsi tramite l'icona vicino al proprio nome oppure dal cestino in alto a destra. L'amministratore può escludere qualunque altro allenatore tramite l'iconda del cestino. Se un allenatore non contatta il backend per più di 20 secondi (conteggiato da latenza) potrà essere cacciato da chiunque.
In ciascuna riga sarà presente una icona, per segnalare l'effettivo collegamento degli altri allenatori. L'indicazione del giocatore di turno (forzabile dall'amministratore) e un riepilogo dei giocatori presi (dettagliato per ruolo).
Solo l'amministratore, avrà anche la possibilità di avviare un'asta per un altro allenatore, tramite l'icona che appare una volta selezionato un giocatore.

#### Accordion offerte
Se è il proprio turno (o se si è amministratori) la prima attività da fare sarà **selezionare** il calciatore da offrire ed **avviare** l'asta (la durata è personalizzabile dall'amministratore). Dopo che sarà avviata, tutti gli allenatori vedranno il **progressivo del tempo rimanente** per effettuare un rilancio, la **situazione aggiornata** ed avranno la possibilità di **rilanciare** quanto indicato nell'apposito campo. Esiste la possibilità di **allineare in automatico** con i rilanci degli altri allenatori o **manualmente** con l'apposita icona. Inoltre esistono le scorciatoie per puntare **+1, +5 o +10**. 
Chiunque potrà sospendere il conto alla rovescia con il bottone **pausa**.

L'**amministratore** avrà le seguenti possibilità aggiuntive:
* operare per qualunque altro allenatore.
* azzerare il tempo
* terminare l'asta in anticipo

Quando il tempo finisce, o l'amministratore termina l'asta, questo potrà **confermare** o **annullare** l'asta appena conclusa.

#### Accordion log sessione corrente
Verranno elencate tutte le attività effettuate fino all'avvio di una nuova asta.










