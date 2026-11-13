# Changelog

Todos los cambios notables de este mod se documentan aquí. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el versionado, [SemVer](https://semver.org/lang/es/).

## [Unreleased]

### Added
- Martillos (pico) y excavadores (pala) de cobre, hierro, oro, diamante y netherita que rompen un área de bloques.
- Modos plano, ancho y profundo, que se cambian agachado con clic derecho y se guardan en el ítem.
- Contorno de los bloques vecinos que se romperán.
- Configuración en `config/areatools.json` (tamaño del área, profundidad, velocidad de la netherita, durabilidad y
  drops).
- Recetas, mejora a netherita en la mesa de herrería, logros y traducciones (en, es_es, es_ar).

### Changed
- La geometría del área es lógica pura (`AreaGeometry`, `AreaShape`) y `BreakArea` queda como adaptador a Minecraft;
  `AreaMode.shape(config)` sustituye a `AreaShape.of`.
- Los vecinos se rompen en el evento `AFTER` de romper bloque, cuando el central ya se rompió, en lugar de en
  `BEFORE`. La cara se sigue midiendo en `BEFORE`.
- La descripción del mod en `fabric.mod.json` está en inglés y se traduce con `modmenu.descriptionTranslation`;
  los textos ya no prometen un tamaño fijo de área.
- `fabric-api` pasa de `*` a `>=0.165.2+26.3`.
- Licencia CC0 1.0 completa y sección de licencia en el README.

### Fixed
- Un `areatools.json` dañado (JSON inválido, valor del tipo equivocado o bytes que no son UTF-8) se guarda como
  `.bak` y se regenera; los valores corregidos y `NaN` se avisan en el log, y un archivo que solo no se pudo leer no se
  toca.
- Si otro mod veta el bloque central, ya no se rompe el área; si la herramienta se rompe al minar el central, tampoco.
- Se comprueba que la cara apuntada es del bloque que se está rompiendo antes de usarla.
- Los drops solo se juntan si aparecen en el mismo mundo y cerca del área minada, y el estado del rompedor se limpia
  aunque haya una excepción.
- El contorno que dibuja el cliente usa el tamaño de área del servidor (`AreaConfigSyncPayload`).
- Faltaba en el repositorio el logro del excavador de netherita, que las traducciones ya referenciaban.
