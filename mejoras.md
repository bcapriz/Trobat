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

## Pendientes — buenas prácticas del parcial (a aplicar)

### 11. Token JWT en `SharedPreferences` sin cifrar
El resumen del parcial indica: *"no se deben guardar datos sensibles ya que los SharedPreferences
se pueden burlar"* y los marca como deprecados (recomienda migrar a DataStore).

`SessionManager.kt` guarda `token`, `userId` y `userName` en SharedPreferences en texto plano.
El token JWT es el dato más sensible de la app.

**Plan:** migrar a `EncryptedSharedPreferences` (misma API de SP, agrega cifrado AES-256).
Archivos: `SessionManager.kt`, `RepositoryProvider.kt`, `build.gradle.kts`.

---

### 12. `takePictureWithLocation()` en archivo de composable
El parcial indica: *"no se deben poner funciones en el mismo archivo o clase en el que está el
composable, sino en archivos a parte"*.

`CaptureEvidenceScreen.kt:71` tiene una función top-level no-composable mezclada con la UI.

**Plan:** mover a `ui/utils/CameraUtils.kt`.

---

### 13. `formatLastSeenDate()` en archivo de composable
Misma regla. `ActiveCaseCard.kt:133` tiene una función utilitaria de fechas en el mismo archivo
que el composable.

**Plan:** mover a `ui/utils/DateUtils.kt`.

---

### 14. Modelos de dominio en `data/model` en lugar de `domain/`
El parcial establece la estructura de capas: `Data/Model` = DTOs de red, `Domain` = modelos
usados por la UI.

`MissingPersonCase.kt` y `CitizenReport.kt` son los modelos que consume la UI, no DTOs.
Deberían estar en `domain/model/`. Los DTOs (`CasoDto`, `AuthDto`, etc.) ya están correctamente
en `data/remote/dto/`.

**Plan:** crear carpeta `domain/model/` y mover los dos modelos.

---

### 15. Mapper `toDomain()` acoplado al repositorio
El parcial establece que el repositorio "orquesta datos", no los transforma.
`RemoteCaseRepository.kt:38` tiene una extension function `CasoDto.toDomain()` pegada al final
del archivo del repositorio.

**Plan:** mover a `data/repository/mapper/CasoMapper.kt`.

---

### 16. `RemoteCaseRepository` usa `CoroutineScope` no lifecycle-aware
El parcial dedica una sección a este escenario: *"hilo secundario mal acotado... el GC no puede
liberar la Activity... memory leak"*. `CoroutineScope(Dispatchers.IO + SupervisorJob())` en el
repositorio nunca se cancela.

**Plan:** crear `TrobatApplication.kt`, registrarla en el `AndroidManifest.xml`, exponer un
`applicationScope` y pasarlo al repositorio vía `RepositoryProvider.init()`.

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
