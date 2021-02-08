# ManagerApp
## Requisiti Specifici
I casi di studio degli esami nella facoltà di Informatica (ma non solo) sono tanti ed il materiale prodotto si accumula e confonde nei vari supporti anno dopo anno. Si vuole perciò realizzare una app che permette la visione ed organizzazione di questi progetti (spesso costituiti da materiali eterogenei). Si offrano i seguenti servizi:  
- gestire profili utente, distinti tra: ospite, professore/amministratore, studente/partecipante, studente/developer, gruppo/developer ognuno con funzionalità specifiche (registrazione, possibilità di creare progetti e caricare il materiale, possibilità di eliminare o modificare una entry, di scaricare il materiale ecc…) 
- gestire i gruppi: studenti, esami, corso di laurea in modo da poterci associare i progetti 
- gestire le autorizzazioni alla pubblicazione ed alla visualizzazione (anche parziale) dei materiali del progetto 
- inserire segnalazioni, valutazioni e recensioni per ogni progetto 
- caricare, scaricare e visualizzare, per ogni progetto e con le dovute autorizzazioni, i materiali quali: documentazione testuale, immagini e screenshots, video 
- eseguire ricerche base (ad es. lista dei progetti di un developer, lista delle release di un progetto, lista dei progetti con almeno uno screenshot, ecc) 
- scambiare ‘liste’ tra utenti (classifica, progetti valutati, suggerimenti, progetti provati, ecc) 
- condividere elementi di un progetto con post personalizzati (screenshot, testo, ecc) tramite servizi e/o app esterne 
## Requisiti Generali
Ogni app consegnata per l’esame nell’a.a. 2020-2021 deve rispettare le seguenti caratteristiche:
- il linguaggio di programmazione è Java ed il codice è sviluppato in Android Studio con le librerie ufficiali illustrate a lezione (no tools, frameworks, esportazioni, ecc…in poche parole, dovete scrivere tutto il codice a mano o con i tool di Android Studio). E’ possibile richiamare servizi esterni (server-side, database, Play Services) purchè documentati ed inclusi in modo trasparente nel codice Android Studio. Ogni altra libreria deve essere comunicata e motivata al docente che deve approvarla PRIMA del suo utilizzo (con relativa documentazione a parte).
- il codice e la struttura di progetto sono auto-esplicativi ed “amichevoli”: le parti e le funzionalità più importanti del codice sono commentate ed il progetto (files, risorse, cartelle, ..) è organizzato in modo intuitivo ed ordinato
- multilingua (incluso gestione con plurals)
- gestione cambi di configurazione (landscape/portrait, ecc …)
- rispetto delle guideline del Material Design
- persistenza dei dati locale e remota (SQLite, Firebase, …)
- uso di almeno un sensore
- uso di almeno un canale di connettività wireless (BT, BTLE, WiFi, NFC, ...)
- gestione di almeno due tipi di utente (profilo dell’utente e quindi l'interfaccia dell'app cambia a seconda del login, …) 

Aggiornamento: ai fini della valutazione si consiglia di fornire l’app con le credenziali di un utente demo, che abbia già dati
e valori pronti per testare le funzionalità significative dell’app.
Esempi: se un’app permette di caricare e visualizzare documenti, il profilo demo avrà già dei documenti a lui associati; se
l’app permette di scegliere i turni di una palestra l’utente demo sarà già iscritto ad una ‘palestra demo’ che offre dei turni
da poter subito scegliere; se l’app prevede la funzione di scegliere un gruppo di studio a cui unirsi, nell’app ci sono già dei
gruppi creati per tale funzionalità…. e così via).
Si consiglia inoltre di gestire sempre i casi in cui non ci sia connettività sul device oppure essa sia limitata, fornendo opportuni
feedback ed alternative, quando possibile. Si ricorda infine che la valutazione viene fatta sempre utilizzando l’emulatore di
Android Studio
