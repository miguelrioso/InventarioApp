# InventarioApp

Android inventory app built with Kotlin. It works offline first: every change is saved in a local Room database and then synced with Cloud Firestore.

## Features

- Create, edit, delete and search products
- Offline support: data is stored locally in Room and synced with Firestore
- Real-time sync between devices using Firestore snapshot listeners
- Import a product catalog from a REST API (DummyJSON) with Retrofit

## Tech stack

- Kotlin, XML views with Material Components
- Room (with KSP) for local storage
- Cloud Firestore for cloud sync
- Retrofit + Gson for REST calls
- Coroutines and Flow
- ViewModel + Repository pattern

## Architecture

`MainActivity` (UI) -> `ProductoViewModel` (UI state) -> `ProductoRepository` -> Room (`ProductoDao`) and Firestore.

## Setup

This repository does not include `google-services.json`. To run the app, create your own Firebase project, add an Android app with the package name `com.datacore.inventarioapp` and place the downloaded `google-services.json` inside the `app/` folder.

## Status

Work in progress. Planned next: Firebase Authentication, secure Firestore rules and screenshots.