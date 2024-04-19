# IMX Fieldlab

Demo URL: https://imx.apps.digilab.network/fieldlab
Use cases (ReSpec): https://geonovum.github.io/imx-digilab/

## Configuratie

- [Doelmodel](./config/fieldlab.yaml)
- [Model mapping](./config/fieldlab.mapping.yaml)
- Bronnen
  - BRP ([API docs](https://brp-api.github.io/Haal-Centraal-BRP-bevragen/v2/redoc) / [Dataset](https://github.com/BRP-API/Haal-Centraal-BRP-bevragen/blob/master/src/config/BrpService/test-data.json)): https://imx.apps.digilab.network/haalcentraal/api/brp
  - BAG ([API docs](https://lvbag.github.io/BAG-API/Technische%20specificatie/Redoc/)): https://api.bag.kadaster.nl/lvbag/individuelebevragingen/v2
  - BRK ([Dataset](./data/brk))
  - BRI ([Dataset](./data/bri))
  - WOZ ([Dataset](./data/woz))

## Bevindingen (algemeen)

1. IMX ondersteunt nog geen generalisatie in modellen. Om toch tot een representatieve implementatie te komen zijn de 
   modellen "platter" gespecificeerd dan ze daadwerkelijk zijn.
   Zie issues: [#50](https://github.com/imx-org/imx-orchestrate/issues/50) / [#51](https://github.com/imx-org/imx-orchestrate/issues/51).
2. Er bestaan inconsistenties in de BAG API ten opzichte van het
   [IMBAG model](https://imbag.github.io/catalogus/hoofdstukken/conceptueelmodel), o.a.:
   - Er zit een dubbele "wrapper" om verblijfsobjecten heen in het JSON schema. Dit kan niet automatisch worden 
    afgehandeld. Er is een standaard patroon (design rule?) nodig voor hoe om te gaan met generalisatie in een API design.   
   - Kenmerk van verblijfsobject heet `gebruiksdoelen` in plaats van `gebruiksdoel` (conform IMBAG).
3. Er is nog geen mechanisme om bepaalde subsets conditioneel te bevragen (in het kader van data minimalisatie).
   Bijvoorbeeld: de BRP dient alleen geraadpleegd te worden wanneer het gebruiksdoel in de BAG "woonfunctie" betreft.
4. Er is nog maar een beperkte set standaard result mappers beschikbaar, waardoor vaak CEL-expressies nodig zijn. Het
   zou goed zijn om de standaard set van mappers verder uit te breiden en CEL alleen nodig te maken voor geavanceerde
   use cases.
5. Er is nog heen mechanisme om waardes te mappen op basis van  

## Use case 1: Inkomensafhankelijke huurverhoging

Met de volgende query kunnen alle benodigde gegevens in 1 request worden opgevraagd. De orkestratie engine bevraagt
onder de motorkap alle benodigde bronnen en combineert de deelresultaten tot 1 document conform het doelmodel.

```graphql
query Woning {
  woning(identificatie: "0518200000821306") {
    identificatie
    postcode
    huisnummer
    heeftWoonfunctie
    inkomenIAH
    heeftBewoner {
        bsn
        naam
        leeftijd
        inkomen
    }
    heeftEigenaar {
        bsn
        naam
    }
  }
}
```

Sidenotes:
- Er is geen rekening gehouden met adressen die in de BAG geregistreerd zijn als "nevenadres".
- Voor de check op gebruiksdoel is nu een CEL-expressie gebruikt. Het zou beter zijn een standaard "contains" mapper
  (of soortgelijk) beschikbaar te hebben. Zie bevinding 4.
- Indien `heeftWoonfunctie = false` is er feitelijk geen sprake van een woning, terwijl het objecttype wel zo heet.
  Zou er dan eigenlijk helemaal geen resultaat moeten zijn? Of zou het objecttype hernoemd moeten worden?

Met de volgende identificaties kunnen verschillende scenario's gecontroleerd worden:

- Identificatie: `0518200000821306`
  - `heeftWoonfunctie = false`, want corresponderend verblijfsobject `0518010000821308` heeft gebruiksdoel `Logiesfunctie`.
  - Bewoners (BRP) en eigenaren (BRK) zijn identiek. Er is dus geen sprake van verhuur.
  - `inkomenIAH = 116500`, want het inkomen van bewoner 1 is 85000 en van bewoner 2 31500. Beide bewoners zijn ouder dan 23.
- Identificatie: `0518200000772702`
  - `heeftWoonfunctie = true`, want corresponderend verblijfsobject `0518010000772703` heeft gebruiksdoel `Woonfunctie`.
  - Bewoner (BRP) en eigenaar (BRK) is identiek. Er is dus geen sprake van verhuur. 
  - `inkomenIAH = 80000`, want inkomen van deze bewoner is 80000 en de bewoner is ouder dan 23.
- Identificatie: `0518200000617226`
  - `heeftWoonfunctie = false`, want corresponderend verblijfsobject `0518010000617227` heeft gebruiksdoel `Industriefunctie`.
  - De eigenaar (BRK) is geen bewoner (BRK). Er is dus mogelijk sprake van verhuur. 
  - `inkomenIAH = 70000`, want:
    - Inkomen van Jan de Jager (53) is `30000`.
    - Inkomen van Anita Jansen (53) is `30000`.
    - Inkomen van Zeus de Jager (20) is `32356`. Omdat dit persoon onder de 23 jaar is, wordt er `22356` in mindering gebracht en blijft er `10000` over.

## Beproefpunten

### 1. Specificatie eigen informatiebehoefte

_Praten in eigen taal mogelijk?_

Er wordt een query interface aangeboden conform het [doelmodel](./config/fieldlab.yaml).

_Hoe zit de eigen taal – bron model mapping in elkaar?_

De [model mapping](./config/fieldlab.mapping.yaml) is gespecificeerd conform de [IMX model mapping language](https://geonovum.github.io/IMX-ModelMapping/). 

_Welke kennis heb ik nodig om de mapping te maken?_

Er is domeinkennis nodig van de bronmodellen en onderlinge verbanden. Dit dient vertaald te worden naar een mapping conform de [IMX model mapping language](https://geonovum.github.io/IMX-ModelMapping/).

_Kan ik deze in principe zelf maken en zelf toevoegen aan het IMX framework?_

Ja, er kan een eigen instantie gemaakt worden van de IMX orkestratie engine, met een zelf samengestelde mapping.

_Inschatting van complexiteit, wat voor type persoon maakt de mapping?_

N.t.b.

### 2. Betekenis en betrouwbare resultaten, te volgen voor functionele mensen 

_Is de betekenis aanwezig in de modellen, van vraag en bron?_

De bronmodellen kunnen MIM modellen zijn, welke betekenis bevatten voor de model-elementen.

_Waar zit de lineage tussen vraag en bron en tussen antwoord en bron?_

De lineage metadata worden tijdens het orkestratie proces samengesteld conform het [IMX lineage model](https://geonovum.github.io/IMX-LineageModel/). Deze is te raadplegen via de query interface.

_Heeft bron eigenaar me kunnen helpen bij maken mapping bestand van deelvraag?_

N.t.b. betreft de eigenaar van een woning en wie wonen er in de woning.

### 3. Hoe valt het gedane werk uiteen in de IAH casus? 

_Welk deel is configuratie geweest in de casus? Welk deel is programmeren geweest?_

Het doelmodel is volledig met configuratie gerealiseerd, behalve:
- de berekening van leeftijd (dit zou in potentie een standaard mapper kunnen worden)
- de correctie op inkomen op basis van leeftijd

Deze zaken zijn nu geprogrammeerd binnen het standaard raamwerk (door custom mappers/combiners). Door het framework en 
de model mapping language uit te breiden zou dit in de toekomst ook met configuratie mogelijk kunnen worden.

De query interface (API) levert nu geen "kant-en-klaar" antwoord met de maximale huurverhoging, maar dit wordt door de UI
berekend obv de gegevens uit het doelmodel. Mogelijk zou dit ook door de orkestratie mogelijk kunnen worden met een
andere mapping of door orkestraties te "stapelen". Dit zou in een eventueel vervolg meegenomen kunnen worden.

_Kan een ontwikkelaar zelf nog een stukje Java erbij schrijven (een hook) en is dit nodig in de huidige situatie?_ 

Er zijn momenteel 4 "extension" points:

- Result mappers: het converteren van 1 individuele bronwaarde
- Result combiners: het combineren van een (set van) bronwaarde(n) tot 1 resultaatwaarde.
- Matchers: het matchen op bepaalde condities
- IMX extensions: het toevoegen van extra data-types (bijv [geospatial](https://github.com/imx-org/imx-orchestrate/blob/main/ext-spatial/src/main/java/nl/geostandaarden/imx/orchestrate/ext/spatial/SpatialExtension.java))

Mappers en combiners zijn in deze POC reeds toegepast voor [leeftijd](./src/main/java/nl/geostandaarden/imx/fieldlab/mapper/AgeMapperType.java)
 en voor de [correctie op het inkomen](./src/main/java/nl/geostandaarden/imx/fieldlab/combiner/inkomenIAHCombinerType.java).

### 4. Breakdown van hoofdvraag naar deelvragen 

_Is de software om de deelvragen te stellen gegenereerd o.b.v. model of zelf geprogrammeerd mbv bv. SPARQL en ...?_

De deelvragen, en in welke volgorde deze uitgevoerd dienen te worden, worden dynamisch berekend op basis van de set van
gegevens die wordt bevraagd. Dit wordt door middel van de "adapter laag" vertaald naar technische interacties naar
verschillende bron API's. De API types kunnen per bron verschillen, zolang er een adapter voor beschikbaar is.

Er worden diverse open source componenten gebruikt onder de motorkap, o.a. Spring Boot, GraphQL Java en Project Reactor.

_Hoe zie ik de specificatie van de deelvragen terugkomen in de gemaakte configuraties?_

Dit zit volledig in de model mapping.

_Komen de deelvragen en deel antwoorden komen traceerbaar terug in het antwoord?)_

Door de orkestratie engine in debug modus te draaien kun je in het log zien welke API interacties er precies hebben
plaatsgevonden. Ook zijn er ideeën om de technische interacties op een bepaalde manier inzichtelijk te maken als
onderdeel van het lineage model. Momenteel beperkt het lineage model zich tot het gegevensniveau.

