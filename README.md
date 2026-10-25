# PM01Examen13142 - Examen Primer Parcial

**Estudiante:** Leany Lizeth Hernandez Martinez  
**Número de cuenta:** 123630142
**Asignatura:** Programación para Dispositivos Móviles I  
**Catedrático:** Ing. Ricardo Enrique Lagos Mendoza  
**Fecha:** 24 de octubre de 2026  

## Descripción
Aplicación móvil desarrollada en Android Studio utilizando Java y SQLite para la gestión de contactos. Proyecto realizado individualmente para el Examen del I Parcial.

### Funcionalidades implementadas:
- **Pantalla 1 (Registro):**
  - Captura de fotografía desde la cámara o galería.
  - Spinner de países con su respectivo código de área (Honduras, Costa Rica, Guatemala, El Salvador, Nicaragua, Panamá).
  - Validaciones con AlertDialog para campos vacíos.
  - Expresiones regulares (RegEx) para restringir el nombre solo a letras y el teléfono solo a números.
  - Guardado de datos e imagen (byte array) en base de datos SQLite local.

- **Pantalla 2 (Lista de Contactos):**
  - ListView conectado a SQLite.
  - Buscador en tiempo real para filtrar la lista de contactos.
  - Botones de acción para el contacto seleccionado:
    - **Compartir:** envía el contacto mediante Intent ACTION_SEND.
    - **Ver Imagen:** muestra la foto guardada del contacto.
    - **Eliminar:** remueve el registro de la BD y refresca la lista.
    - **Actualizar:** pasa los datos del contacto de vuelta a la pantalla de registro para editarlos.

- **Pantalla 3 (Llamadas):**
  - Al presionar un contacto se muestra un AlertDialog para confirmar si se desea realizar la llamada.
  - Verificación y solicitud del permiso `CALL_PHONE` en tiempo de ejecución.
  - Ejecución de la llamada mediante Intent ACTION_CALL concatenando el código de área + número.

## Capturas de Pantalla

| Registro de Contacto | Lista y Buscador | Confirmación de Llamada |
<img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/27b63e11-a178-4577-a012-d9758839d291" />
 <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/97b0de93-9893-4488-9315-6992e8d9488d" />
 <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/484ccfd9-b687-439c-bc9d-ec12968989ff" />
