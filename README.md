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
- Solo se rompen los bloques para los que la herramienta es la correcta (drops incluidos). Respeta la protección de spawn,
  los modos aventura/espectador y los bloques irrompibles.
- Los drops de los bloques vecinos caen en el bloque que golpeaste, no repartidos por el área.

### Recetas

| Herramienta | Receta |
|---|---|
| Martillo (cobre, hierro, oro, diamante) | `BBB` / `BSB` / ` S ` con bloques del material y palos |
| Excavador (cobre, hierro, oro, diamante) | ` B ` / `BSB` / ` S ` con bloques del material y palos |
| Martillo / excavador de netherita | mejora en la mesa de herrería desde el de diamante, con lingote de netherita |

## Configuración

`config/areatools.json` se crea al primer arranque. Los valores fuera de rango se corrigen solos y, si el archivo
no se puede leer, se usan los valores por defecto sin modificarlo. Hay que reiniciar el juego para aplicar cambios.

| Clave | Por defecto | Rango | Qué hace |
|---|---|---|---|
| `radius` | 1 | 1–3 | Mitad del lado del cuadrado en modo plano y profundo: 1 = 3x3, 2 = 5x5, 3 = 7x7 |
| `wideRadius` | 2 | 1–3 | Mitad del lado del cuadrado en modo ancho (2 = 5x5) |
| `deepDepth` | 3 | 1–5 | Bloques de profundidad en modo profundo |
| `netheriteMiningSpeed` | 70 | 1–1000 | Velocidad de minado del netherita (vanilla: 9) |
| `durabilityDivisor` | 2 | 1–9 | Cada N bloques extra rotos cuestan 1 de durabilidad. 1 = un punto por bloque |
| `centerDrops` | true | — | Juntar los drops en el bloque golpeado |

Con 70 la piedra sale al instante; deepslate y menas necesitan Eficiencia (III casi, V del todo).

En un servidor dedicado, el contorno que ve el jugador usa la configuración de su propio cliente: conviene que
`radius`, `wideRadius` y `deepDepth` coincidan con los del servidor.

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
| `ToolKind` | Tipo de herramienta (martillo/excavador): qué bloques mina y qué tag de ítems la identifica |
| `AreaMode`, `ModComponents` | Modo plano/profundo guardado en el ítem (componente de datos) |
| `AreaToolsConfig`, `AreaShape` | Configuración inmutable y tamaño del área resultante |
| `BreakArea` | Geometría del área y reglas de qué bloque se puede romper (compartido servidor/cliente) |
| `AreaBreaker` | Rompe el área en el servidor, descuenta durabilidad y junta los drops |
| `AreaToolsHandler`, `ModeToggle` | Conectan los eventos de Fabric (romper bloque, usar ítem) |
| `ModItems` | Registro de los diez ítems y pestaña creativa |
| `client/AreaToolsClient`, `client/AreaOutlineRenderer` | Contorno de los bloques vecinos (con caché de posiciones) |
