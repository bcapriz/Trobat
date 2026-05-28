# Trobat

Aplicación Android para la búsqueda colaborativa de personas desaparecidas. Permite a ciudadanos consultar casos activos, reportar evidencia fotográfica geoetiquetada de forma anónima y visualizar zonas con mayor concentración de actividad.

Desarrollado por **Bruno Capriz** y **Franco Verón Peralta**.

---

## Diagrama de arquitectura

```mermaid
graph TD
    subgraph UI["🖼  Capa de Presentación"]
        SCREENS["Pantallas · Jetpack Compose\nSplash · Home · Mapa · Cámara · Reporte · Alertas"]
        VMS["ViewModels · MVVM\nUiState  ·  Events  ·  Effects"]
        NAV["Navegación · Navigation Compose\nAppNavigation  ·  TrobatBottomBar"]
    end

    subgraph DATA["📦  Capa de Datos"]
        IFACE["Interfaces de Repositorio\nCaseRepository  ·  CitizenReportRepository"]
        FAKE["Implementación Fake  H1\nDatos en memoria con StateFlow"]
        REAL["Implementación Real  H2\nRetrofit 2  ·  Room  ·  REST API"]
    end

    subgraph PLATFORM["📱  Plataforma Android"]
        CAMERA["CameraX\nCaptura de foto"]
        LOCATION["FusedLocationProvider\nGeolocalización GPS"]
        MAPS["Google Maps SDK\nMapa interactivo de casos"]
        COIL["Coil\nCarga de imágenes"]
    end

    subgraph BACKEND["☁  Backend  —  H2"]
        REST["REST API"]
        DB[("Base de datos")]
    end

    SCREENS -->|"onEvent()"| VMS
    VMS -->|"StateFlow<UiState>"| SCREENS
    VMS -->|"SharedFlow<Effect>"| NAV
    VMS -->|"observe / call"| IFACE
    IFACE --> FAKE
    FAKE -. "se reemplaza en H2" .-> REAL
    REAL -->|"HTTP"| REST
    REST --- DB
    CAMERA -->|"Uri + coordenadas"| SCREENS
    LOCATION --> CAMERA
    MAPS --> SCREENS
    COIL --> SCREENS
```

---

## Flujo de datos — Patrón MVI

```mermaid
sequenceDiagram
    actor Ciudadano
    participant Screen  as Pantalla (Compose)
    participant VM      as ViewModel
    participant Repo    as Repository

    Note over Screen,Repo: Flujo 1 — Consultar casos activos
    Ciudadano->>Screen: Abre la app
    Screen->>VM: init → observeData()
    VM->>Repo: caseRepository.cases (StateFlow)
    Repo-->>VM: List<MissingPersonCase>
    VM-->>Screen: StateFlow<CitizenHomeUiState>
    Screen-->>Ciudadano: Renderiza lista de casos

    Note over Screen,Repo: Flujo 2 — Reportar evidencia
    Ciudadano->>Screen: Toca "Reportar evidencia"
    Screen->>VM: onEvent(CaptureEvidenceClicked)
    VM-->>Screen: SharedFlow<NavigateToCamera>
    Screen->>Screen: navController.navigate(CAMERA)
    Ciudadano->>Screen: Toma foto
    Screen->>VM: onEvent(PhotoCaptured(uri, lat, lng))
    VM->>VM: Actualiza UiState + CapturedEvidenceHolder
    Screen-->>Ciudadano: Muestra preview de la foto
    Ciudadano->>Screen: Completa descripción y envía
    Screen->>VM: onEvent(SendReportClicked)
    VM->>Repo: sendReport(CitizenReport)
    Repo-->>VM: StateFlow actualizado
    VM-->>Screen: SharedFlow<NavigateToHeatMap>
    Screen-->>Ciudadano: Redirige al mapa con reporte visible
```

---

## Stack tecnológico

| Capa | Tecnología | Justificación |
|---|---|---|
| Lenguaje | Kotlin | Estándar de la industria Android, null-safety, coroutines nativo |
| UI | Jetpack Compose + Material 3 | UI declarativa, dark mode y dynamic color sin código extra |
| Arquitectura | MVVM + Repository Pattern | Separación de responsabilidades, testeable por capas |
| Estado | Kotlin Coroutines + StateFlow / SharedFlow | Reactivo, lifecycle-aware, diferencia estado persistente de eventos únicos |
| Navegación | Navigation Component (Compose) | Back stack manejado, rutas tipadas |
| Cámara | CameraX | API de alto nivel sobre Camera2, lifecycle-aware |
| Ubicación | FusedLocationProvider (Google Play Services) | Alta precisión, bajo consumo de batería |
| Mapas | Google Maps SDK (Maps Compose) | Integración nativa con markers y cámara programática |
| Imágenes | Coil | Carga asincrónica con caché, compatible con Compose |
| Backend (H2) | REST API + Retrofit 2 + Room | Retrofit para red, Room para persistencia local y modo offline |

---

## Estructura del proyecto

```
app/src/main/java/com/trobat/
├── data/
│   ├── model/          # CitizenReport, MissingPersonCase, CapturedEvidenceHolder
│   └── repository/     # Interfaces + implementaciones Fake (H1) / Real (H2)
└── ui/
    ├── components/     # FloatingCameraButton y componentes reutilizables
    ├── navigation/     # AppNavigation, TrobatBottomBar, rutas
    ├── screen/         # Una pantalla por archivo (Compose)
    ├── theme/          # Color, Type, Shape, Theme (Material 3)
    └── viewmodel/      # Un ViewModel + UiState + Event + Effect por pantalla
```

---

## Cómo buildear

1. Clonar el repositorio
2. Abrir con Android Studio Hedgehog o superior
3. Agregar la API key de Google Maps en `local.properties`:
   ```
   MAPS_API_KEY=tu_api_key
   ```
4. Correr en emulador o dispositivo físico con API 26+

---

## Links

- Figma (flujo de pantallas y design system): _[agregar link_franco]_
- Tablero de seguimiento: _[agregar link_bruno]_
