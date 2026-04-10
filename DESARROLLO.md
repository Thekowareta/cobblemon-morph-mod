# Cobblemon Mystery Dungeon — Desarrollo (estado actual)

Este documento describe **el estado actual del desarrollo** del mod en este workspace.  
Nota: el nombre técnico del proyecto/mod en el código hoy es **`cobblemon-morph-dev`** (“Cobblemon Morph (dev)”), y funciona como **base de port** para un sistema de *morph* (reemplazar el render del jugador por un Pokémon y ajustar hitbox/poses). Esto puede ser un sub-sistema del objetivo “Mystery Dungeon”.

## Stack y versiones

- **Loader**: Fabric
- **Minecraft**: `1.21.1`
- **Java/Kotlin target**: `21`
- **Lenguaje**: Kotlin (lógica) + Java (mixins/accesores puntuales)
- **Dependencias**:
  - Fabric API
  - Fabric Language Kotlin
  - Cobblemon (vía Modrinth Maven)

## Qué está implementado hoy (funcional)

### Morph (server/common)

- **Comando**: `/morph <properties>` y `/morph clear`
  - El argumento usa el parser de Cobblemon (`PokemonPropertiesArgumentType`).
  - Hay una **validación de Pokédex**: solo te deja morfear si el jugador ya “conoce” esa especie.
- **Colisión/espacio**: antes de aplicar morph se valida que el bounding box del Pokémon “entre” (si no, falla con mensaje).
- **Dimensiones del jugador (hitbox)**:
  - Server: Mixin a `LivingEntity#getDimensions` devuelve las dimensiones del morph si el jugador está morfeado.
- **Sincronización por red**:
  - `MorphSyncPacket` (S2C): especie + properties + width/height + pose inicial + flags (canFly/canWalk)
  - `SyncPoseS2CPacket` (S2C): actualiza pose
  - `UpdatePoseC2SPacket` (C2S): el cliente reporta su pose calculada
  - `ClearMorphPacket` (C2S): limpiar morph
- **Gestor**: `MorphManager`
  - Mantiene `morphedPlayers` y `lastKnownPoses` (maps concurrentes).
  - Al aplicar morph genera una entidad “de medición” de Cobblemon para obtener `EntityDimensions` y capacidades (p.ej. `canFly`).

### Render (cliente)

- **Reemplazo de render del jugador**:
  - Mixin a `PlayerRenderer#render(...)` cancela el render del jugador y renderiza un **`PokemonEntity` puppet** en su lugar.
- **Entidad puppet (cliente)**:
  - Se crea y mantiene en `MorphRenderManager`.
  - Se le aplican aspects/propiedades y se fuerza `no_ai`.
  - Se evita lógica indeseada:
    - Mixin a `PokemonEntity#tick` para puppets (cancel tick vanilla y tickear delegate + swing/time).
    - Mixin a `PokemonEntity#canBattle` para que puppets no entren en combates.
- **Dimensiones del jugador (cliente)**:
  - Mixin a `LivingEntity#getDimensions` también en cliente para mantener consistencia visual/colisiones locales.
- **Pose/animación**:
  - En cada tick de cliente se calcula una `PoseType` (STAND/WALK/SWIM/FLY/SLEEP) en base a movimiento real.
  - Se envía `UpdatePoseC2SPacket` cuando cambia y se actualiza el puppet.

## Qué NO está (pendiente / incompleto)

Estas son piezas típicas para un “Mystery Dungeon” completo, o para cerrar bien el sistema de morph:

- **Persistencia**: no se guarda morph por jugador entre reinicios/muertes/cambio de dimensión.
- **Reglas de gameplay**:
  - No hay integración con un “modo dungeon”, roles, party, hambre/estados, etc.
  - El comando `/morph` parece orientado a dev/QA; no hay UI ni progression propia.
- **Seguridad/anticheat**:
  - El server acepta updates de pose del cliente (con gating básico: solo si está morfeado). Falta endurecer validaciones si se usa en servidor público.
- **Interacciones**:
  - No hay handling explícito de monturas, swimming/flying edge-cases, hitboxes raras, escalado, shadow/nametag en todos los casos, etc.

## Estructura del proyecto (carpetas clave)

- `build.gradle`, `gradle.properties`, `settings.gradle`: configuración Gradle/versions
- `src/main/kotlin/com/team_aether/morph/`
  - `MorphDevMod.kt`: entrypoint common/server, registro de payloads y handlers
  - `manager/MorphManager.kt`: estado + aplicar/limpiar morph + sync
  - `command/MorphCommand.kt`: comando `/morph`
  - `network/*`: payloads y handler servidor
  - `data/MorphData.kt`: data holder de morph
- `src/main/java/com/team_aether/morph/mixin/`
  - `ServerPlayerDimensionsMixin.java`: dimensiones server
  - `CobblemonTrackedKeys.java`: helper para acceder a tracked keys de `PokemonEntity` desde Kotlin
- `src/client/kotlin/com/team_aether/morph/`
  - `client/MorphDevModClient.kt`: entrypoint cliente
  - `render/MorphRenderManager.kt`: puppets + estado client
  - `event/ClientTickHandler.kt`: cálculo y envío de pose
  - `network/MorphClientPacketHandler.kt`: receptores S2C
- `src/client/java/com/team_aether/morph/mixin/client/`
  - Mixins de render/dimensiones/ticks para puppets
- `src/main/resources/`
  - `fabric.mod.json`
  - `*.mixins.json`

## Cómo correr en dev (Windows / PowerShell)

En la raíz del proyecto:

```powershell
.\gradlew.bat runClient
```

Si necesitas recompilar limpio:

```powershell
.\gradlew.bat clean runClient
```

## Flujo de prueba manual (rápido)

- Entra a un mundo con Cobblemon instalado.
- Asegúrate de **haber “encontrado”** la especie (por la validación de Pokédex).
- Ejecuta:
  - `/morph pikachu`
  - `/morph charizard shiny` (ejemplo de properties adicionales)
  - `/morph clear`
- Verifica:
  - El jugador se renderiza como Pokémon (local y para otros jugadores).
  - La hitbox cambia (colisión/altura).
  - La pose cambia caminando/nadando/volando.

## Backlog sugerido (siguiente iteración)

- **Renombrado/identidad**: alinear ids/nombre (`cobblemon-morph-dev`) con “Cobblemon Mystery Dungeon” si ya es definitivo.
- **Persistencia**: guardar/cargar `MorphData` por jugador (data component / persistent state).
- **Reglas MD**: definir loop de gameplay (dungeons, progreso, party, etc.) y conectar con Cobblemon.
- **UX**: UI/feedback (pantallas, sonidos, cooldowns, restricciones por zona).
- **Hardening**: validación server-side de poses/estados y manejo de edge cases (mounts, elytra, dimension change, respawn).

