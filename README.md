# Omsorgspenger midlertidig alene prosessering

![CI / CD](https://github.com/navikt/omsorgspenger-midlertidig-alene-prosessering/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/omsorgspenger-midlertidig-alene-prosessering/workflows/Alerts/badge.svg)

# Innholdsoversikt
* [1. Kontekst](#1-kontekst)
* [2. Funksjonelle Krav](#2-funksjonelle-krav)
* [2.1 Feil ved prosessering](#feil-i-prosessering)
* [3. Begrensninger](#3-begrensninger)
* [4. Prinsipper](#4-prinsipper)
* [5. Programvarearkitektur](#5-programvarearkitektur)
* [6. Kode](#6-kode)
* [7. Data](#7-data)
* [8. Infrastrukturarkitektur](#8-infrastrukturarkitektur)
* [9. Distribusjon av tjenesten (deployment)](#9-distribusjon-av-tjenesten-deployment)
* [10. Utviklingsmiljø](#10-utviklingsmilj)
* [11. Drift og støtte](#11-drift-og-sttte)

# 1. Kontekst
Prosesseringstjeneste for søknad om å bli regnet som midlertidig alene.

# 2. Funksjonelle krav
Tjenesten konsumerer meldinger fra topicen "privat-omsorgspenger-midlertidig-alene-mottatt" som 
[omsorgspenger-midlertidig-alene-api](https://github.com/navikt/omsorgspenger-midlertidig-alene-api) har produsert

Tjenesten journalfører mot [K9-Joark](https://github.com/navikt/k9-joark) og legger til slutt søknaden
på topic "" som k9-fordel/omsorgspenger-rammemeldinger konsumerer.

## Feil i prosessering
Ved feil i en av streamene som håndterer prosesseringen vil streamen stoppe, og tjenesten gi 503 response på liveness etter 15 minutter.
Når tjenenesten restarter vil den forsøke å prosessere søknaden på ny og fortsette slik frem til den lykkes.

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerterator.yml](nais/alerterator.yml).

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.
