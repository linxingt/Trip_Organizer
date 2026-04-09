# 🌍 Trip Organizer - Application Android

**Trip Organizer** est une application native Android conçue pour faciliter la planification et l'organisation de voyages. Elle permet aux utilisateurs de gérer plusieurs séjours et de lister précisément les lieux à visiter pour chacun d'eux, tout en intégrant des fonctionnalités natives d'Android (Maps, Appels, Notifications, Services).

## Fonctionnalités Principales

* **Gestion Multi-Voyages (Fonctionnalité Bonus) :** Création, consultation, modification et suppression de voyages. *(Règle métier : un voyage ne peut plus être modifié dès qu'un lieu lui est associé).*
* **Gestion des Lieux à visiter (CRUD) :** Pour chaque voyage, ajout de lieux détaillés (titre, description, date, heure, adresse, numéro de téléphone, statut visité/non visité, et photo de la galerie).
* **Persistance des données :** Base de données **SQLite** intégrée, gérant les relations entre voyages et lieux (suppression en cascade).
* **Intégration Native (Intents) :** 
    * Ouverture des adresses directement sur **Google Maps**.
    * Lancement du composeur d'appels pour les numéros de téléphone enregistrés.
    * Partage des informations d'un lieu vers d'autres applications.
* **Notifications Programmées :** Alertes configurables (1 semaine ou 2 jours avant le départ) gérées par `AlarmManager` et `BroadcastReceiver`.
* **Musique d'ambiance :** Un `Service` Android joue une musique de fond, activable ou désactivable selon les préférences de l'utilisateur (`SharedPreferences`).
* **Internationalisation :** Application disponible en Anglais, Français et Chinois.

## Technologies Utilisées

* **Langage :** Java
* **IDE :** Android Studio
* **Base de données :** SQLite (via `SQLiteOpenHelper`)
* **Composants Android :** `Activity`, `Service`, `BroadcastReceiver`, `AlarmManager`, `SharedPreferences`, `ListView` & `Adapter` personnalisés, `Intent` (explicites et implicites), `Parcelable`.

## Documentation et Rapport

Pour comprendre en profondeur les choix architecturaux, les difficultés rencontrées (comme la gestion des URI pour les photos ou l'intégration de Maps) et les solutions apportées, veuillez consulter les documents suivants inclus dans ce dépôt :

* **[Sujet_Projet.pdf](./Projet_Trip_Organizer.pdf)** : Le cahier des charges original de l'application.
* **[Rapport_Projet.pdf](./Rapport_Xingtong_LIN.pdf)** : Mon rapport détaillé incluant la description de l'approche, la résolution des bugs et les captures d'écran.
