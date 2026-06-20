# Mejoras aplicadas — Trobat Android

Resumen de antipatrones, malas prácticas y código en desuso encontrados y corregidos en este ciclo de bugfixing.

---

## Corregidos

### 1. Typos en nombres de archivo
- `CitiyzenReport.kt` → `CitizenReport.kt` ("Citiyzen" tiene una 'y' de más)
- `CtizenReportRepository.kt` → `CitizenReportRepository.kt` (le faltaba la 'i')

En Kotlin la convención es que el nombre del archivo coincida con la clase que contiene.

---

### 2. Código muerto: `PlaceholderScreen`
`TrobatMainScreen.kt` tenía una función privada `PlaceholderScreen` que nunca fue llamada.
Fue eliminada.

---

### 3. Dependencia sin usar: `accompanist-permissions`
`com.google.accompanist:accompanist-permissions:0.37.3` estaba declarada en `build.gradle.kts`
pero el código usa `ActivityResultContracts.RequestMultiplePermissions()` directamente.
Ningún archivo importa el paquete `accompanist`. Fue removida.

---

### 4. `HttpLoggingInterceptor.Level.BODY` en todos los builds
`NetworkProvider.kt` logueaba headers y bodies HTTP en producción, exponiendo tokens y datos
personales en logcat. Se agregó `buildConfig = true` y se condicionó el nivel con `BuildConfig.DEBUG`.

---

### 5. Imports no usados en `CitizenHomeScreen.kt`
Los imports `Instant`, `ZoneId`, `DateTimeFormatter` y `Locale` de `java.time` estaban presentes
pero pertenecen a `ActiveCaseCard.kt`. Removidos.

---

### 6. Import redundante en `TrobatMainScreen.kt`
`import com.trobat.ui.screen.CitizenHomeScreen` es innecesario porque `TrobatMainScreen` está
en el mismo package (`com.trobat.ui.screen`). Removido.

---

### 7. `ReportStatus.PENDING_SYNC` nunca usado
El enum `ReportStatus` tenía un valor `PENDING_SYNC` que ningún archivo referenciaba.
Fue removido para no generar confusión sobre un estado que nunca se asigna.

---

### 8. Colores hardcodeados en `TrobatBottomBar`
`selectedColor = Color(0xFF5E1DD3)` y `unselectedColor = Color(0xFF6D6778)` estaban hardcodeados,
rompiendo el soporte de dark mode y duplicando valores ya definidos en el tema.
Reemplazados por `MaterialTheme.colorScheme.primary` y `MaterialTheme.colorScheme.onSurfaceVariant`.
El color de fondo del Surface también se unificó con `MaterialTheme.colorScheme.surfaceVariant`.

---

### 9. `expandedCaseId` como estado local en `HeatMapScreen`
El estado de expansión de tarjetas en `HeatMapScreen` vivía en `remember { mutableStateOf() }` a
nivel de composable, por lo que se perdía ante rotaciones o recreación de la actividad.
Movido a `HeatMapViewModel` / `HeatMapUiState`, siguiendo el mismo patrón que `CitizenHomeViewModel`.

---

### 10. `CitizenReport.createdAt` hardcodeado a `"Ahora"`
Al crear un reporte desde `ConfirmReportViewModel`, el campo `createdAt` se fijaba al string
literal `"Ahora"`. Si la lista de reportes se revisaba luego, todos los reportes decían lo mismo.
Ahora se genera con la hora real: `"Hoy, HH:mm"`.

---

---

### 11. Token JWT en `SharedPreferences` sin cifrar
El resumen del parcial indica: *"no se deben guardar datos sensibles ya que los SharedPreferences
se pueden burlar"* y los marca como deprecados (recomienda migrar a DataStore).

`SessionManager.kt` guardaba `token`, `userId` y `userName` en texto plano.
Migrado a `EncryptedSharedPreferences` con cifrado AES-256-GCM.
Archivos: `RepositoryProvider.kt`, `build.gradle.kts` (dependencia `security-crypto:1.1.0-alpha06`).

---

### 12. `takePictureWithLocation()` en archivo de composable
El parcial indica: *"no se deben poner funciones en el mismo archivo o clase en el que está el
composable, sino en archivos a parte"*.

Movida a `ui/utils/CameraUtils.kt`. Imports de GMS Location y `java.io.File` removidos de
`CaptureEvidenceScreen.kt`.

---

### 13. `formatLastSeenDate()` en archivo de composable
Misma regla. Movida a `ui/utils/DateUtils.kt`. Imports de `java.time` removidos de
`ActiveCaseCard.kt`.

---

### 14. Modelos de dominio en `data/model` en lugar de `domain/`
El parcial establece: `Data/Model` = DTOs de red, `Domain` = modelos usados por la UI.

`MissingPersonCase.kt` y `CitizenReport.kt` movidos a `domain/model/` (package
`com.trobat.domain.model`). Imports actualizados en 16 archivos. Los DTOs permanecen en
`data/remote/dto/`.

---

### 15. Mapper `toDomain()` acoplado al repositorio
Movido a `data/repository/mapper/CasoMapper.kt`. `RemoteCaseRepository` ahora solo importa
la función del mapper, sin lógica de transformación propia.

---

### 16. `RemoteCaseRepository` usa `CoroutineScope` no lifecycle-aware
Creada `TrobatApplication.kt` con `applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)`.
Registrada en `AndroidManifest.xml`. `RemoteCaseRepository` ahora recibe el scope por constructor.
`RepositoryProvider.init()` obtiene el scope de la Application y lo inyecta.

---

## Documentados (no corregidos en este ciclo)

### A. `RemoteCaseRepository` crea su propio `CoroutineScope`
`CoroutineScope(Dispatchers.IO + SupervisorJob())` creado en el `init` del repositorio nunca es
cancelado. En un repositorio que vive como singleton esto es aceptable, pero lo correcto sería
recibir un `applicationScope` inyectado desde fuera.

### B. Excepciones silenciosas en repositorios remotos
`RemoteCaseRepository.fetchCases()` y `RemoteCitizenReportRepository.sendReport()` usan
`catch (_: Exception) {}` sin propagar el error al ViewModel ni a la UI. Si el envío de un reporte
falla, el usuario no recibe ningún feedback. Requiere agregar estados de error en los UiState
correspondientes.

### C. `CitizenReport.id` generado en el cliente
`id = System.currentTimeMillis().toString()` en `ConfirmReportViewModel` genera un ID temporal
que el servidor ignora (devuelve su propio ID en `CrearResponseDto`). Ese ID local nunca se
actualiza con el del servidor. Impacto bajo mientras los reportes no se consulten por ID desde
la app, pero es una inconsistencia de datos.

### D. `SplashScreen` y `LoadingScreen` usan colores directos
Ambas pantallas usan `BackgroundPrincipal` y `TrobatBackground` directamente en lugar de
`MaterialTheme.colorScheme.*`. Esto es intencional para que el splash siempre tenga la identidad
visual de la app, pero rompe el soporte de dark mode si en el futuro se quiere adaptar.
