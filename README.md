# Gestión de Contactos — Tarea 1 · Programación de Interfaces Gráficas

> Aplicación de escritorio Java Swing que implementa un CRUD completo de contactos,
> con diseño visual mejorado, soporte multilingüe (ES / EN / JA) y modo claro / oscuro.

---

## Tabla de contenidos
1. [Descripción del proyecto](#descripción-del-proyecto)
2. [Estructura del proyecto](#estructura-del-proyecto)
3. [Arquitectura MVC](#arquitectura-mvc)
4. [Fase 1 – Diseño visual](#fase-1--diseño-visual)
5. [Fase 3 – Desarrollo en Java Swing](#fase-3--desarrollo-en-java-swing)
6. [Fase 4 – Internacionalización](#fase-4--internacionalización)
7. [Modo claro / oscuro](#modo-claro--oscuro)
8. [Cómo compilar y ejecutar](#cómo-compilar-y-ejecutar)
9. [Atajos de teclado](#atajos-de-teclado)
10. [Formato del CSV](#formato-del-csv)

---

## Descripción del proyecto

Aplicación de Gestión de Contactos construida en **Java Swing** siguiendo el
patrón **MVC (Model–View–Controller)**. Permite:

- Agregar, modificar y eliminar contactos.
- Buscar / filtrar contactos en tiempo real.
- Ordenar la tabla haciendo clic en la cabecera.
- Importar y exportar contactos en formato CSV.
- Cambiar el idioma de la interfaz (Español · English · 日本語).
- Alternar entre modo claro y modo oscuro.

---

## Estructura del proyecto

```
src/
├── controlador/
│   └── logica_ventana.java      – Controlador (lógica de eventos y negocio)
├── modelo/
│   ├── persona.java             – Entidad/Modelo de contacto
│   └── personaDAO.java          – Acceso a datos (CSV)
├── vista/
│   ├── ventana.java             – Vista principal (JFrame + componentes Swing)
│   ├── ThemeManager.java        – Gestión de temas (paletas de colores)
│   └── UTF8Control.java         – ResourceBundle.Control para UTF-8
└── i18n/
    ├── messages_es.properties   – Textos en español
    ├── messages_en.properties   – Texts in English
    └── messages_ja.properties   – 日本語テキスト
```

---

## Arquitectura MVC

| Capa         | Clase               | Responsabilidad |
|--------------|---------------------|-----------------|
| **Modelo**   | `persona`           | Representa un contacto con 5 atributos: nombre, teléfono, email, categoría, favorito. |
| **Modelo**   | `personaDAO`        | Lee y escribe el archivo CSV en `~/gestionContactos/datosContactos.csv`. |
| **Vista**    | `ventana`           | Construye toda la interfaz gráfica. Expone componentes públicos al controlador. |
| **Vista**    | `ThemeManager`      | Centraliza todas las constantes de color para los dos temas. |
| **Vista**    | `UTF8Control`       | Permite cargar archivos `.properties` en UTF-8 para soportar japonés. |
| **Controlador** | `logica_ventana` | Conecta la Vista con el Modelo; maneja eventos, filtros, temas e idiomas. |

---

## Fase 1 – Diseño visual

### Paleta de colores

#### Modo Oscuro — Catppuccin Mocha
Paleta rica en tonos púrpura oscuro con acentos pastel.

| Rol            | Nombre       | Hex       | Muestra |
|----------------|--------------|-----------|---------|
| Fondo base     | Base         | `#1E1E2E` | ⬛ |
| Panel          | Mantle       | `#181825` | ⬛ |
| Campo          | Surface 0    | `#313244` | 🟫 |
| Borde          | Surface 1    | `#45475A` | 🔲 |
| Texto          | Text         | `#CDD6F4` | ⬜ |
| Acento ppal.   | Mauve        | `#CBA6F7` | 🟣 |
| Acento 2       | Lavender     | `#B4BEFE` | 🔵 |
| Verde (Agregar)| Green        | `#A6E3A1` | 🟢 |
| Rojo (Eliminar)| Red          | `#F38BA8` | 🔴 |
| Azul (Editar)  | Blue         | `#89B4FA` | 🔵 |
| Teal (Exportar)| Teal         | `#94E2D5` | 🩵 |
| Naranja (Import)| Peach       | `#FAB387` | 🟠 |

#### Modo Claro — Soft Indigo
Fondos blancos/pizarra con familia de índigo/violeta.

| Rol            | Hex       |
|----------------|-----------|
| Fondo          | `#F1F5F9` |
| Panel / campo  | `#FFFFFF` |
| Borde          | `#E2E8F0` |
| Texto          | `#1E293B` |
| Acento ppal.   | `#6366F1` |
| Verde          | `#16A34A` |
| Rojo           | `#DC2626` |
| Azul           | `#2563EB` |

### Tipografía
- **Segoe UI Bold 21 px** — Título de la aplicación.
- **Segoe UI Bold 13 px** — Pestañas y botones de acción.
- **Segoe UI Bold 12 px** — Etiquetas del formulario.
- **Segoe UI Plain 12 px** — Campos de texto y tabla.
- **Segoe UI Bold 15 px** — Estadísticas.

### Iconos (Unicode)
Los iconos se renderizan con caracteres Unicode para no depender de imágenes externas.

| Icono | Unicode | Uso |
|-------|---------|-----|
| ✚    | `\u271A` | Botón Agregar |
| ✎    | `\u270E` | Botón Modificar |
| ✕    | `\u2715` | Botón Eliminar |
| ↑    | `\u2191` | Botón Exportar CSV |
| ↓    | `\u2193` | Botón Importar CSV |
| ★    | `\u2605` | Favorito marcado |
| ☆    | `\u2606` | Favorito sin marcar |
| ▶    | `\u25B6` | Estadística total |
| ❖    | `\u2756` | Título de la app |
| ☽    | `\u263D` | Botón modo oscuro |
| ☀    | `\u2600` | Botón modo claro |

### Administradores de diseño (Layouts)

| Componente           | Layout usado                   | Motivo |
|----------------------|-------------------------------|--------|
| `contentPane`        | `BorderLayout`                | Divide cabecera, contenido central y pestaña |
| `headerPanel`        | `BorderLayout`                | Título a la izquierda, controles a la derecha |
| `panelFormulario`    | **`GridBagLayout`**           | Control preciso de columnas y pesos |
| `panelBotones`       | `FlowLayout(LEFT)`            | Botones alineados a la izquierda |
| `panelInferior`      | `BorderLayout`                | Botones arriba, barra de progreso abajo |
| `panelStats`         | `GridLayout(5,1)`             | Una etiqueta de estadística por fila |

> **Mejora clave:** Se eliminó el posicionamiento absoluto (`setBounds` manual)
> de la versión original y se reemplazó por Layout Managers estándar, lo que
> hace la interfaz escalable y redimensionable.

---

## Fase 3 – Desarrollo en Java Swing

### Componentes principales

- **`JTabbedPane`** — Dos pestañas: *Contactos* y *Estadísticas*.
- **`JTable` + `DefaultTableModel`** — Tabla no editable con ordenamiento por columna.
- **`TableRowSorter`** — Filtrado y ordenamiento sin modificar el modelo.
- **`SwingWorker`** — Carga e importación de CSV en segundo plano para no bloquear el EDT.
- **`JProgressBar`** (indeterminate) — Animación durante operaciones de archivo.
- **`JPopupMenu`** — Menú contextual con clic derecho (Editar / Eliminar).
- **`DocumentListener`** — Búsqueda en tiempo real mientras se escribe.
- **`KeyStroke` + `InputMap/ActionMap`** — Atajos de teclado.

### Renderer personalizado de la tabla
Se implementa un `DefaultTableCellRenderer` propio que:
- Alterna el fondo de filas pares e impares.
- Adapta los colores al tema activo (claro/oscuro).
- Renderiza la columna *Favorito* como ★/☆ en lugar de `true`/`false`.

---

## Fase 4 – Internacionalización

### Idiomas soportados

| Código | Idioma   | Archivo de recursos          |
|--------|----------|------------------------------|
| `es`   | Español  | `i18n/messages_es.properties`|
| `en`   | English  | `i18n/messages_en.properties`|
| `ja`   | 日本語   | `i18n/messages_ja.properties`|

### Implementación técnica

1. **`ResourceBundle`** — Java carga dinámicamente el archivo `.properties`
   correspondiente al `Locale` seleccionado.
2. **`UTF8Control`** — Subclase de `ResourceBundle.Control` que lee los
   `.properties` en UTF-8 en lugar de ISO-8859-1, permitiendo incluir
   caracteres japoneses directamente en el archivo.
3. **`MessageFormat.format()`** — Para cadenas con parámetros dinámicos,
   p. ej. `"Se importaron {0} contactos."`.
4. **Cambio dinámico sin reiniciar** — `ventana.updateTexts(Locale)` actualiza
   etiquetas, cabeceras de tabla, textos de botones y pestañas en caliente.
5. **Categorías canónicas** — Los valores de categoría se almacenan siempre en
   español en el CSV (`Amigo`, `Trabajo`, `Familia`, `Otro`) para mantener
   compatibilidad. El método `translateCategory()` los convierte al idioma
   activo en tiempo de visualización.

### Selector de idioma
En la barra superior aparece un `JComboBox` con tres opciones:
`Español | English | 日本語`. Al cambiar la selección:
- Se actualizan todos los textos de la interfaz.
- Se actualiza el menú contextual.
- La tabla se refresca con las categorías traducidas.
- Las estadísticas se actualizan con los nuevos textos.

---

## Modo claro / oscuro

El botón **☽ Modo Oscuro / ☀ Modo Claro** alterna entre los dos temas.

### Flujo de cambio de tema

```
Usuario pulsa btn_tema
    → logica_ventana llama ThemeManager.toggleTheme()
    → logica_ventana llama ventana.applyTheme()
        → applyTheme() lee colores de ThemeManager
        → Aplica colores a cada componente
        → Llama applyTableRenderer() para recrear renderer con nuevos colores
        → revalidate() + repaint()
```

### `ThemeManager`
Clase estática con dos paletas de constantes `Color`. Expone métodos semánticos:
- `getBg()`, `getPanelBg()`, `getFieldBg()` — capas de fondo
- `getText()`, `getTextSecondary()` — texto
- `getAccent()`, `getAccent2()` — acentos
- `getTableHeaderBg()`, `getTableRowBg()`, `getTableAltRowBg()` — tabla
- `getBtnAdd()`, `getBtnModify()`, `getBtnDelete()`, `getBtnExport()`, `getBtnImport()` — botones

---

## Cómo compilar y ejecutar

### Requisitos
- **Java 8** o superior (JDK).

### Compilación manual

```bash
# Desde la raíz del proyecto
mkdir -p bin

javac -d bin -sourcepath src \
  src/modelo/persona.java \
  src/modelo/personaDAO.java \
  src/vista/UTF8Control.java \
  src/vista/ThemeManager.java \
  src/vista/ventana.java \
  src/controlador/logica_ventana.java

# Copiar los recursos i18n al directorio de salida
cp -r src/i18n bin/
```

### Ejecución

```bash
java -cp bin vista.ventana
```

### Con IDE (IntelliJ IDEA / Eclipse / NetBeans)
1. Importar como proyecto Java con raíz de fuentes en `src/`.
2. Ejecutar `vista.ventana` como clase principal.

---

## Atajos de teclado

| Atajo      | Acción                         |
|------------|-------------------------------|
| `Ctrl + N` | Limpiar formulario (nuevo)    |
| `Ctrl + F` | Enfocar campo de búsqueda     |
| `Ctrl + E` | Exportar contactos a CSV      |
| `Supr`     | Eliminar contacto seleccionado|

---

## Formato del CSV

El archivo se guarda en `~/gestionContactos/datosContactos.csv` con el siguiente formato:

```
NOMBRE;TELEFONO;EMAIL;CATEGORIA;FAVORITO
Juan Pérez;0991234567;juan@mail.com;Amigo;true
Ana Torres;0987654321;ana@mail.com;Trabajo;false
```

> Los valores de `CATEGORIA` se almacenan siempre en **español** (`Amigo`,
> `Trabajo`, `Familia`, `Otro`) para garantizar compatibilidad independientemente
> del idioma que tenga activo el usuario.

---

## Créditos
- **Paleta oscura:** [Catppuccin Mocha](https://github.com/catppuccin/catppuccin)
- **Fuente:** Segoe UI (incluida en Windows; en Linux/Mac se utiliza la fuente sans-serif del sistema)
