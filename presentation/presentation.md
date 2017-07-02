# TODO
* definitie middlebox
* functie cu acelasi domeniu si codomeniu

# Motivation
Incepem cu 2 observatii legate de retelele de calculatoare moderne.

In primul rand, sunt foarte complexe iar acest lucru face ca functionalitatea
lor corecta sa nu mai fie certa.  O mare parte din cauza acestei cresteri in
complexitate se datoreaza middlebox-urilor care sunt tot mai comune.  Intr-un
mod informal, middlebox-urile sunt echipamente destinate altui scop decat
transmiterea pachetelor de la sursa la destinatie.

Asta ne aduce la a doua observatie, si anume ca tot mai multe elemente din
retelele moderne, in special cele cu functie de de middlebox, sunt implementare
in software, pe arhitecturi precum x86, in locul echipamentelor dedicate.  Pe
multe dintre acele sisteme care ruleaza Linux, iptables este un tool de baza
pentru configurarea procesarii pachetelor, osciland ca si complexitate intre
firewal-uri minimale pana la intregi componente de networking virtualizate, cum
e cazul Neutron, componenta de networking a OpenStack.

In acest context, se pune problema corectitudinii retelelor.  Cu alte cuvinte,
vrem sa stim daca reteaua noastra respecta anumite reguli, care impreuna
formeaza politica pe care vrem sa o impunem.  In aceasta lucrare obiectivul
specific din cadrul acestei tentative largi este modelarea iptables cu scopul
verificarii retelelor care sunt bazate pe acesta.

# Static Data Plane Verification
Ideea pe care se bazeaza abordarea noastra pentru verificarea retelelor are
doua aspecte definitorii.

In primul rand, este statica, ceea ce inseamna ca ne bazam algoritmii de
verificare pe un model al retelei, si nu pe infrastructura propriu-zisa.

In al doilea rand, modelul acesta este construit doar pe baza data plane-ului
retelei.  Asta inseamna ca ne intereseaza doar regulile pe baza carora
functioneaza reteaua la un moment de timp, si ignoram modul in care acele
reguli au fost configurate sau invatate.  Spre exemplu, in cazul unui router,
ignoram modul in care au ajuns prefixele in tabela de routare.

Odata construit, acest model este trimis la un motor de verificare care ne
poate spune daca politica noastra este sau nu respectata; spre exemplu, daca
utilizatorul A poate stabili o conexiune cu serverul B.

# Static Data Plane Verification - Framework
Acum ca avem ideea high-level a sistemului, putem fi putin mai precisi.

Motorul de verificare pe care il folosim este SymNet.  Acesta foloseste
executie simbolica, o tehnica de analiza statica folosita in verificarea
programelor.  Inputul este modelul retelei, asa cum se poate vedea si in figura,
alaturi de un pachet cu care sa poata fi inceputa executia simbolica.  Outputul
este o lista exhaustiva de pachete generate alaturi de constrangerile acestora.

Modelele retelelor sunt construite folosind SEFL.  Acesta este un DSL proiectat
impreuna cu SymNet cu scopul de a scadea pe cat de mult posibil complexitatea
executiei simbolice.  Instructiunile sale permit exprimarea functiilor uzuale
de retea ca transformari ale flow-urilor.  Exprima practic o functie <> in
spatiul pachetelor: se primeste un pachet pe un port, se aplica o functie, se
returneaza un alt pachet.

Sistemul care rezulta se dovedeste a avea cateva proprietati foarte
interesante, insa probabil cea mai importanta este faptul ca este scalabil *cat
timp* modelele sunt, sa spunem, fine-tuned.  Acest lucru ne motiveaza de altfel
sa vrem sa modelam retele din ce in ce mai complexe.

# Overview of iptables
Revenind la iptables, acesta este un tool de configurare a tabelelor de reguli
expuse de netfilter, un framework customizabil de procesare a pachetelor ce
face parte din kernelul Linux.

Feature-ul sau cel mai important este tocmai aceasta customizabilitate, data de
organizarea interna.  Elementul de baza este *regula*, care precizeaza *pe ce*
trafic, se aplica *ce* actiune.  Regulile sunt organizate in 2 moduri.
Lanturile dau *locul* in stiva de procesare unde se va aplica acea regula (spre
exemplu FORWARD pentru pachete care tranziteaza device-ul), iar *tabelele*
restrictioneaza *ce* se poate face cu traficul respectiv (spre exemplu in
filter se poate face doar ACCEPT si DROP).

Pastrand in minte acest flowgraph, ne proppunem sa construim un model SEFL care
sa il imite.

# Towards a Model (1)
Pornim de la modelul unui simplu router.  Indiferent de portul de intrare pe
care ajunge un pachet, scopul routerului este de a-l ruta.  Astfel ca legam
porturile de intrare la un "element" ascuns, numit aici 'routing decision'.
Acest element are rolul de a implementa in SEFL logica pentru routarea
pachetelor catre unul dintre porturile de iesire sau catre nivele superioare
ale stivei, daca pachetul ii este destinat.  In general, simbolizam
posibilitatea existentei unor instructiuni SEFL folosind aceste box-uri.

# Towards a Model (2)
Pentru a ne indrepta catre flowgraph-ul anterior care descria functionalitatea
iptables, replicam componenta care implementeaza decizia de routare in 3
locuri: pentru pachetele tocmai ajunse din retea, pentru pachetele tocmai
generate de acest device, si pentru pachetele care se pregatesc sa paraseasca
acest device.

# Towards a Model (3)
In cele din urma, tot ce mai ramane de facut este sa adaugam un nou element
care abstractizeaza parcurgerea regulilor dintr-un lant in fiecare pozitie din
stiva de procesare din netfilter.

In acest moment am redus, practic, problema modelarii iptables la aceea a
modelarii parcurgerii regulilor dintr-un lant.  Fara sa intram in detalii in
legatura cu feature-urilor iptables care fac acest lucru mai complicat decat
pare si cu eventualele optimizari, ne putem gandi ca fiecare astfel de element
implementeaza in SEFL o secventa if/then/else, unde conditiile sunt date de
*match-urile* regulilor, iar instructiunile de executat sunt actiunile
regulilor (ACCEPT, SNAT, etc).

# Design and Implementation
In ceea ce priveste implementarea, tool-ul rezultat, numit, natural,
iptables-to-sefl, primeste ca input un fisier ce contine un dump al regulilor
iptables si returneaza un model, care apoi, impreuna cu modele ale altor
elemente din retea precum si conexiunile dintre acestea, sunt trimise catre
SymNet.

Intern, tool-ul are un design de compilator, cu faze de *parsare* a regulilor,
*validarea* lor, echivalentul analizei semantice in compilatoarele usuale, si
*generarea de cod* pentru fiecare regula si pentru punerea cap la cap a
multiplelor componente interne, cum am vazut in diagrama anterioara a
modelului.

Pe langa asta, am avut in vedere numarul mare de extensii pentru match-uri si
target-uri ale iptables, asa ca design-ul face usor adaugarea suportului pentru
noi extensii.

# Evaluation - Correctness
Evaluarea este impartita in doua categorii: evaluarea corectitudinii modelelor
si evaluarea performantei verificarii acestora.

Prima ne ajuta sa raspundem la intrebarea "Sunt modelele generate de noi
corecte?".  Cu alte cuvinte, surprind ele semantica iptables pentru care au
fost gandite?

Pentru a raspunde la aceasta intrebare folosim teste care ideal ar trebui sa
acopere toate scenariile modelate. In practica, nu le acoperim pe toate, insa
cu cat mai multe cu atat ne creste increderea ca acele feature-uri sunt
modelate corect.

O topologie pe care am folosit-o foarte mult, in special in testarea
functionalitatilor stateful, precum connection tracking, NAT, etc, este cea din
figura de mai jos.  Elementul "IP mirror" este foarte simplu, el doar inversand
adresele sursa si destinatie, modeland astfel un raspuns de la un posibil
server.

# Evaluation - Performance
Cea de-a doua categorie de teste caracterizeaza calitatea modelelor pe baza
timpului de executie al SymNet (deci al executiei simbolice) atunci cand se
ruleaza pe un model generat de noi, sau pe o retea care contine modele generate
de tool-ul nostru.

Am rulat 2 tipuri de teste, aici. In primul am variat numarul de reguli
din chain-ul FORWARD al tabelei filter.  Concluzia este ca pana la un anumit
threshold, de peste 100 de reguli, cresterea este sub-liniara in raport cu
numarul de reguli (axa din figura foloseste scara logaritmica).  De asemenea,
in unul dintre teste (cel colorat cu magenta) am vrut sa surprindem efectul
precizarii unui IP destinatie concret, spre deosebire de unul simbolic, ca in
restul testelor.  Rezultatul este o scadere mare a timpului de verificare.

La aceeasi concluzie, si anume importanta constrangerii cat mai mari a
pachetului de input, am ajuns si in cel de-al doilea test, in care am legat mai
multe device-uri care folosesc iptables.  Din cauza efectului multiplicativ al
numarului de pachete generate pe masura ce avansam in retea, timpul executiei
simbolice creste semnificativ.  Aceasta nu este, totusi, un lucru specific
iptables, insa ca urmare a capabilitatii sale de a implementa functii mai
complexe, numarul de pachete generate poate fi mai mare decat in alte elemente
de retea modelate pana acum.

# Conclusion
In concluzie, *ceea ce am realizat* este un tool care modeleaza configuratii
iptables folosind SEFL.

*Ceea ce obtinem* este un spectru crescut de configuratii de retele pe care le
putem verifica folosind SymNet.

In privinta *calitatii implementarii*, putem spune ca ne-am concentrat in
special pe corectitudinea modelelor generate si pe modelarea extensiilor
folosite in general in practica, *urmand* ca *in viitor* sa ne concentram pe
optimizarea modelelor si al codului generat, precum si pe integrarea intr-un
model al OpenStack, care este un proiect in desfasurare.
