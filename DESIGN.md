# Design System: SISOL Salud - Directorio Médico

## 1. Tema Visual y Atmósfera (Visual Theme & Atmosphere)
La atmósfera del portal es **Cálida, Profesional y Confiable**. Se aleja de los tradicionales tonos azules fríos y estériles de los sitios web clínicos y, en su lugar, adopta una paleta de colores cálidos y terrosos centrada en un granate profundo (maroon) y una crema suave. Esto crea una experiencia acogedora, premium y centrada en el ser humano que enfatiza el cuidado y la accesibilidad, al tiempo que mantiene la autoridad médica. El diseño es estructurado pero espacioso, con un amplio espacio en blanco que reduce la carga cognitiva para el paciente.

## 2. Paleta de Colores y Roles (Color Palette & Roles)
* **Granate Médico Profundo (Deep Medical Maroon - #6b1515 aprox.):** Utilizado como el color principal de la marca, botones de llamada a la acción (CTA) principales, fondo del pie de página (footer) y estados de navegación activos. Transmite estabilidad, seriedad y cuidado premium.
* **Fondo Crema Suave (Soft Cream Background - #fdf7f4 aprox.):** Utilizado como el fondo principal de la página. Proporciona una alternativa cálida y menos agotadora visualmente que el blanco puro, mejorando la sensación de bienvenida.
* **Blanco Puro (Pure White - #ffffff):** Utilizado para las tarjetas de contenido (perfiles de médicos, contenedor de búsqueda) para hacer que resalten sutilmente contra el fondo crema.
* **Carbón para Texto (Text Charcoal - #333333):** Utilizado para la tipografía principal (encabezados, nombres de médicos) para garantizar un alto contraste y legibilidad sin la dureza del negro puro.
* **Gris Sutil (Subtle Gray - #e5e5e5):** Utilizado para bordes muy sutiles, campos de entrada de formularios inactivos y detalles de texto secundarios.

## 3. Reglas de Tipografía (Typography Rules)
* **Encabezados (Headings):** Una tipografía sans-serif limpia y moderna (como Poppins, Montserrat o Manrope) con un peso fuerte (Bold/700 u 800) para los títulos de sección. Ayuda a captar la atención y establecer una jerarquía clara.
* **Cuerpo y Etiquetas (Body & Labels):** Una tipografía sans-serif altamente legible (como Inter o Roboto) para elementos de la interfaz, descripciones y metadatos.
* **Jerarquía Estratégica:** Contraste estricto entre el nombre del médico (Negrita, oscuro) y su especialidad (Regular, más pequeña, color más claro, en mayúsculas para las etiquetas) para permitir un escaneo visual rápido.

## 4. Estilos de Componentes (Component Stylings)
* **Botones (Buttons):**
  * *Primarios:* Forma de píldora o esquinas ligeramente redondeadas (`rounded-md` o `rounded-lg`). Rellenos con el Granate Profundo, texto en Blanco Puro. Usados para "RESERVAR CITA", "FILTRAR" y "MÁS INFORMACIÓN".
  * *Paginación:* Cuadrados sutiles con esquinas redondeadas (`rounded-md`). El estado activo utiliza el fondo Granate, mientras que los inactivos usan un fondo blanco con un borde gris sutil.
* **Tarjetas/Contenedores (Cards/Containers - Perfiles de Médicos):**
  * Esquinas generosamente redondeadas (`rounded-2xl` o `1rem`).
  * Fondo Blanco Puro descansando sobre el fondo Crema de la página.
  * *Sombra (Shadow):* Una sombra difusa muy suave y susurrante (`shadow-md` o `shadow-lg` con una opacidad muy baja, del 5-8%) para proporcionar un levantamiento suave sin que parezca pesado o anticuado. Se prohíbe el uso de bordes sólidos y duros de 1px.
* **Campos de Entrada/Formularios (Inputs/Forms - Barra de Búsqueda):**
  * Fondo suave y claro (ligeramente diferente al de la tarjeta blanca, quizás un gris/crema muy claro) para indicar interactividad.
  * Sin bordes duros. Los íconos (búsqueda, flecha desplegable) son tenues hasta que se enfocan.

## 5. Principios de Diseño (Layout Principles)
* **Sistema de Cuadrícula (Grid System):** Cuadrícula simétrica para mostrar los perfiles de los médicos (ej. 4 columnas en la vista de escritorio).
* **Espacio en Blanco (Whitespace):** Relleno generoso dentro de las tarjetas (al menos 24px) para dejar que el contenido respire. El espacio entre el encabezado, los filtros de búsqueda y la cuadrícula es amplio, creando una experiencia de desplazamiento (scroll) calmada y sin prisas.
* **Imágenes (Imagery):** Los retratos de los médicos son consistentes, bien iluminados y se ubican contra fondos neutros, recortados de manera uniforme a la altura del pecho/hombros para mantener una apariencia unificada y profesional.
