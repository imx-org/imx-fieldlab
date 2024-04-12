# IMX Fieldlab

Demo URL: https://imx.apps.digilab.network/fieldlab

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

1. IMX ondersteunt nog geen generalisatie in modellen; dit was reeds bekend. Om toch tot een representatieve
   implementatie te komen zijn de modellen "platter" gespecificeerd dan ze daadwerkelijk zijn.
   Zie issues: [#50](https://github.com/imx-org/imx-orchestrate/issues/50) / [#51](https://github.com/imx-org/imx-orchestrate/issues/51).
2. Er bestaan inconsistenties in de BAG API ten opzichte van het
   [IMBAG model](https://imbag.github.io/catalogus/hoofdstukken/conceptueelmodel), o.a.:
   - Er zit een dubbele "wrapper" om verblijfsobjecten heen in het JSON schema. Dit kan niet automatisch worden 
    afgehandeld. Er is een standaard patroon (design rule?) nodig voor hoe om te gaan met generalisatie in een API design.   
   - Kenmerk van verblijfsobject heet `gebruiksdoelen` in plaats van `gebruiksdoel` (conform IMBAG).
3. Er is nog geen mechanisme om bepaalde subsets conditioneel te bevragen (in het kader van data minimalisatie).
   Bijvoorbeeld: de BRP dient alleen geraadpleegd te worden wanneer het gebruiksdoel in de BAG "woonfunctie" betreft.
4. Er zijn nog weinig standaard result mappers beschikbaar, waardoor vaak CEL-expressies nodig zijn. Het zou goed zijn
   om de standaard set van mappers verder uit te breiden en CEL alleen nodig te maken voor geavanceerde use cases.

## Use case 1: Inkomensafhankelijke huurverhoging

Met de volgende query kunnen alle benodigde gegevens in 1 request worden opgevraagd. De orkestratie engine bevraagt
vervolgens alle verschillende bronnen. De resultaten worden gecombineerd tot 1 document conform het doelmodel.

```graphql
query Woning {
  woning(identificatie: "0518200000821306") {
    identificatie
    postcode
    huisnummer
    heeftWoonfunctie
    totaalinkomen
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
  - `heeftWoonfunctie = false`, want corresponderend verblijfsobject `0518010000821308` heeft gebruiksdoel `Logiesfunctie`
  - Bewoners (BRP) en eigenaren (BRK) zijn identiek.
  - `totaalinkomen = 116500`, want inkomens van bewoner 1 is 85000 en van bewoner 2 is 31500. Beide bewoners zijn ouder dan 23.
- Identificatie: `0518200000772702`
  - `heeftWoonfunctie = true`, want corresponderend verblijfsobject `0518010000772703` heeft gebruiksdoel `kantoorfunctie`
  - Bewoners (BRP) en eigenaren (BRK) is identiek. Er is dus geen sprake van verhuur. 
  - `totaalinkomen = 80000`, want inkomen van deze bewoner is 80000 en bewoner is ouder dan 23.
