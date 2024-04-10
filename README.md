# IMX Fieldlab

Demo URL: https://imx.apps.digilab.network/fieldlab

## Bronnen

- BRP ([API docs](https://brp-api.github.io/Haal-Centraal-BRP-bevragen/v2/redoc) / [Dataset](https://github.com/BRP-API/Haal-Centraal-BRP-bevragen/blob/master/src/config/BrpService/test-data.json)): https://imx.apps.digilab.network/haalcentraal/api/brp
- BAG ([API docs](https://lvbag.github.io/BAG-API/Technische%20specificatie/Redoc/)): https://api.bag.kadaster.nl/lvbag/individuelebevragingen/v2
- BRK ([Dataset](./data/brk))
- BRI ([Dataset](./data/bri))
- WOZ ([Dataset](./data/woz))

## Bevindingen (algemeen)

1. IMX ondersteunt nog geen generalisatie in modellen; dit was reeds bekend. Om toch tot een representatieve
   implementatie te komen zijn de modellen "platter" gespecificeerd dan ze daadwerkelijk zijn.
   Zie issues: [#50](https://github.com/imx-org/imx-orchestrate/issues/50) / [#51](https://github.com/imx-org/imx-orchestrate/issues/51).
2. Er bestaan inconsistenties in de BAG API teb opzichte van het IMBAG model, o.a.:
   - Er zit een dubbele "wrapper" om verblijfsobjecten heen in het JSON schema. Dit kan niet automatisch worden 
    afgehandeld. Er is een standaard patroon (design rule?) nodig voor hoe om te gaan met generalisatie in een API design.   
   - Kenmerk van verblijfsobject heet `gebruiksdoelen` in plaats van `gebruiksdoel` (conform IMBAG).
3. Er is nog geen mechanisme om bepaalde subsets conditioneel te bevragen (in het kader van data minimalisatie).
   Bijvoorbeeld: de BRP dient alleen geraadpleegd te worden wanneer het gebruiksdoel in de BAG "woonfunctie" betreft.
4. Er zijn nog weinig standaard result mappers beschikbaar, waardoor vaak CEL-expressies nodig zijn. Het zou goed zijn
   om de standaard set van mappers verder uit te breiden en CEL alleen nodig te maken voor geavanceerde use cases.

## Use case 1: Inkomensafhankelijke huurverhoging

### Controleer adres op woonfunctie

Als een adres hoort bij een verblijfsobject zonder gebruiksdoel "woonfunctie", dan dient de waarde `false` teruggegeven
te worden voor property `heeftWoonfunctie`.

```graphql
query Woning {
  woning(identificatie: "0518200000747446") {
    identificatie
    postcode
    huisnummer
    heeftWoonfunctie
    totaalinkomen
  }
}
```

Bovenstaande query levert als resultaat het stadhuis van Den Haag op. Het totaalinkomen is 0, omdat er in de BRP geen
personen zijn gevonden die ingeschreven staan op dit adres. Idealiter wil je
de BRP helemaal niet raadplegen, maar dit is momenteel nog niet mogelijk in de mapping (zie bevinding 3).

```json
{
  "woning": {
    "identificatie": "0518200000747446",
    "postcode": "2511BT",
    "huisnummer": 70,
    "heeftWoonfunctie": false,
    "totaalinkomen": 0
  }
}
```

Sidenotes:
- Er is geen rekening gehouden met adressen die in de BAG geregistreerd zijn als "nevenadres".
- Indien `heeftWoonfunctie = false` is er feitelijk geen sprake van een woning, terwijl het objecttype wel zo heet.
  Zou er dan eigenlijk helemaal geen resultaat moeten zijn? Of zou het objecttype hernoemd moeten worden?
