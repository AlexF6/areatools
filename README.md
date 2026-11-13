# Area Tools

Fabric, Minecraft 26.3, Java 25. Herramientas que rompen un área en vez de un solo bloque. Por ahora son martillos
(pico) y excavadores (pala) de cobre, hierro, oro, diamante y netherita; el mod está pensado para sumar más tipos. Hay que tener el mod en cliente y servidor.

## Uso

- **Romper:** golpea un bloque y se rompen también sus vecinos. El área es un cuadrado perpendicular a la cara que miras.
- **Agachado** (shift) al romper: rompe solo un bloque.
- **Agachado + clic derecho:** cambia el modo.
  - *Plano*: solo el cuadrado (3x3 por defecto).
  - *Ancho*: un cuadrado más grande (5x5 por defecto).
  - *Profundo*: además se extiende hacia dentro del bloque (3x3x3 por defecto).
  El modo se guarda en el ítem y aparece en su descripción.
- Los **martillos** minan lo que mina un pico; los **excavadores**, lo que mina una pala.
- Los vecinos se rompen **después** del bloque central y solo si este se rompió: si otro mod o una protección lo
  impide, no se rompe nada del área. Si la herramienta se rompe al minar el central, tampoco se rompe el área.
- Solo se rompen los bloques para los que la herramienta es la correcta (drops incluidos). Respeta la protección de spawn,
  los modos aventura/espectador y los bloques irrompibles.
- Los drops de los bloques vecinos caen en el bloque que golpeaste, no repartidos por el área.

### Recetas

| Herramienta | Receta |
|---|---|
| Martillo (cobre, hierro, oro, diamante) | `BBB` / `BSB` / ` S ` con bloques del material y palos |
| Excavador (cobre, hierro, oro, diamante) | ` B ` / `BSB` / ` S ` con bloques del material y palos |
| Martillo / excavador de netherita | mejora en la mesa de herrería desde el de diamante, con plantilla de mejora y lingote de netherita |

## Configuración

`config/areatools.json` se crea al primer arranque. Hay que reiniciar el juego para aplicar cambios.

- Los valores fuera de rango (y `NaN`) se corrigen al límite más cercano y se avisa en el log.
- Si el archivo está dañado (JSON inválido, un valor del tipo equivocado o bytes que no son UTF-8), se guarda como
  `areatools.json.bak` (sustituyendo un respaldo anterior), se regenera con los valores por defecto y se avisa en el log.
- Un archivo vacío o `{}` usa los valores por defecto. Si no se puede leer (permisos, disco), se usan los valores por
  defecto sin modificarlo.

| Clave | Por defecto | Rango | Qué hace |
|---|---|---|---|
| `radius` | 1 | 1–3 | Mitad del lado del cuadrado en modo plano y profundo: 1 = 3x3, 2 = 5x5, 3 = 7x7 |
| `wideRadius` | 2 | 1–3 | Mitad del lado del cuadrado en modo ancho (2 = 5x5) |
| `deepDepth` | 3 | 1–5 | Bloques de profundidad en modo profundo |
| `netheriteMiningSpeed` | 70 | 1–1000 | Velocidad de minado del netherita (vanilla: 9) |
| `durabilityDivisor` | 2 | 1–9 | Cada N bloques extra rotos cuestan 1 de durabilidad. 1 = un punto por bloque |
| `centerDrops` | true | — | Juntar los drops en el bloque golpeado |

Con 70 la piedra sale al instante; deepslate y menas necesitan Eficiencia (III casi, V del todo).

En multijugador manda la config del servidor: al conectarse envía `radius`, `wideRadius` y `deepDepth` a cada cliente,
y el contorno se dibuja con esos valores, no con los del `areatools.json` local. Lo demás (velocidad del netherita,
durabilidad, drops) lo decide solo el servidor, salvo `netheriteMiningSpeed`: la velocidad de minado de las herramientas
de netherita se fija al registrar el ítem en cada instancia, así que conviene que coincida en cliente y servidor (si
no, solo la predicción de minado del cliente se desajusta).

## Desarrollo

```
./gradlew test        # pruebas de geometría del área y de la configuración
./gradlew build       # compila, prueba y deja el jar en build/libs/
```

El build solo escribe dentro del proyecto. Para que además copie el jar a tu carpeta de mods, define `modsDir` (y,
opcionalmente, `exportDir`) en tu `~/.gradle/gradle.properties` o pásalo con `-PmodsDir=...`. `modsDir` también borra
los jars anteriores de este mod en esa carpeta.

Estructura (una responsabilidad por clase):

| Clase | Responsabilidad |
|---|---|
| `AreaToolsMod` | Inicializador común (`onInitialize`): configuración, componentes de datos, ítems y eventos |
| `ToolKind` | Tipo de herramienta (martillo/excavador): qué bloques mina y qué tag de ítems la identifica |
| `AreaMode`, `ModComponents` | Modo plano/profundo guardado en el ítem (componente de datos) |
| `AreaToolsConfig` | Configuración inmutable: acota los valores y avisa; lee, respalda y regenera `config/areatools.json` |
| `AreaShape`, `AreaGeometry` | Tamaño del área y su geometría como desplazamientos enteros (lógica pura, sin Minecraft) |
| `BreakArea` | Adaptador a Minecraft: convierte los desplazamientos en posiciones y decide qué bloque puede romper un jugador (compartido servidor/cliente) |
| `AreaBreaker` | Rompe el área en el servidor, descuenta durabilidad y junta los drops |
| `network/AreaConfigSyncPayload`, `SyncedAreaConfig` | El servidor manda el tamaño del área al conectar; el cliente la guarda mientras dure la conexión |
| `AreaToolsHandler`, `ModeToggle` | Conectan los eventos de Fabric: la cara se mide antes de romper y el área se rompe después; usar ítem cambia el modo |
| `ModItems` | Registro de los diez ítems y pestaña creativa |
| `client/AreaToolsClient`, `client/AreaOutlineRenderer` | Contorno de los bloques vecinos (con caché de posiciones) |

## Licencia

Dedicado al dominio público bajo [CC0 1.0 Universal](LICENSE). Autor: AlexF6. No incluye código ni recursos de otros
mods; las texturas y los modelos son propios.
