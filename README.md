# Nexup Backend Challenge

En este repositorio, se encuentra la prueba técnica para el puesto de Backend Developer en Nexup.

Este challenge está diseñado para evaluar tus habilidades de Kotlin y resolución de problemas.

## Problema a resolver

Crear las clases y funciones necesarias para resolver el siguiente problema:
- Se tiene una **cadena de supermercados**
- Se tienen **productos**, cada uno con un ID único, nombre y precio
- Se tienen **supermercados**, cada uno con un ID único, nombre, un listado de productos y el stock asociado a cada uno (el stock puede variar entre los distintos supermercados)
    - Los supermercados comparten los distintos productos

- Funcionalidades requeridas para cada _supermercado_:
  - Registrar una venta de un producto
    - Dado un ID de producto y una cantidad a vender, se debe registrar la venta de un producto
    - La función debe retornar el precio total de la venta
  - Obtener la cantidad vendida de un producto
    - Dado un ID de producto, retornar la cantidad vendida de dicho producto
  - Obtener ingresos por ventas de un producto
    - Dado un ID de producto, retornar el dinero obtenido de las ventas de dicho producto
  - Obtener ingresos totales
    - Retornar el dinero total obtenido de todas las ventas realizadas

- Funcionalidades requeridas para la _cadena de supermercados_:
  - Obtener los 5 productos más vendidos
    - Buscar los 5 productos más vendidos en toda la cadena  
    - Retornar un _string_ con el formato `<nombre_producto>: cantidad_vendida`, concatenados con un guión
  - Obtener ingresos totales
    - Retornar el dinero total obtenido de todas las ventas realizadas en toda la cadena
  - Obtener el supermercado con mayor cantidad de ingresos por ventas
    - Retornar un _string_ con el formato `<nombre_supermercado> (<id>). Ingresos totales: <ingresos>`
 
Para cada una de las funcionalidades planteadas:
- Definir los nombres de las funciones, parámetros y demás datos cómo consideres adecuado
- Documentar y comentar el código dónde consideres necesario
- Manejar todos los casos de error que consideres necesarios
- Agregar todos los tests que consideres necesarios

### Objetivo opcional

Se desea manejar para cada supermercado su hora de apertura y cierre, así cómo los días donde se encuentra abierto. Agregar los datos necesarios para manejar dicha información.

Sobre la cadena de supermercados, agregar una funcionalidad que, dado un cierto día y horario, se pueda obtener la lista de supermercados abiertos en ese momento.
Se espera obtener la respuesta como un _string_ con el formato `<nombre_supermercado> (<id>)`, y se concatenen con una coma.


## Pasos a seguir:
1. Clone este repositorio en su máquina local usando Git.
   ```bash
   git clone https://gitlab.com/nexup/nexup-backend-challenge.git
   ```
2. Crea un repositorio vacío en tu cuenta de GitHub con el mismo nombre de este.
   ```bash
    nexup-backend-challenge
   ```
3. Muevesé a la carpeta del proyecto.
   ```bash
   cd ./nexup-backend-challenge
   ```
4. Cambia la URL remota del repositorio clonado de GitHub, por la URL de tu repositorio.
   ```bash
   git remote set-url origin <tu-repositorio.git>
   ```
5. Sube el código a tu repositorio.

## Recomendaciones
- **No** hagas un _fork_ de este repositorio.
- **No** hagas _push_ directamente a este repositorio.
- Crea un commit por cada cambio que realices. Utiliza mensajes **claros** y **descriptivos** para documentar tu proceso.
- No es necesario el uso de base de datos ni archivos para manejar los datos de prueba. Podes utilizar estructuras de datos en memoria.
- Dentro del proyecto se encuentra un archivo de ejemplo para ejecución de las pruebas, modificarlo como sea necesario para adaptarlo al problema.
  - En el archivo de pruebas se encuentra un ejemplo de datos a usar en la ejecución de los Tests

## Entregables
- Un enlace a un repositorio de GitHub con el código resolviendo el problema planteado.
- Opcional: Un archivo README con explicaciones sobre el enfoque utilizado y cualquier otra información relevante.

## Enfoque y decisiones de diseño

### Estructura del proyecto

```
src/main/kotlin/
├── domain/
│   ├── model/          # Objetos de valor: Product, Sale, BusinessHours
│   ├── Supermarket.kt  # Lógica de ventas, stock y horarios por sucursal
│   └── SupermarketChain.kt  # Agregación y consultas sobre la cadena
└── Main.kt             # Demo de uso
```

Los modelos (`Product`, `Sale`, `BusinessHours`) se encuentran en `domain.model` como objetos de valor inmutables (data classes). Las clases con lógica de negocio (`Supermarket`, `SupermarketChain`) se encuentran directamente en `domain`.

### Decisiones técnicas

- **`BigDecimal` para valores monetarios**: Se evita `Double` para prevenir errores de precisión en cálculos de dinero (por ejemplo, `0.1 + 0.2 != 0.3` con punto flotante).
- **Validación fail-fast con `require`**: Todos los constructores validan sus invariantes al momento de creación (IDs positivos, nombres no vacíos, stock no negativo, etc.). Esto garantiza que no existan objetos en estado inválido.
- **Copias defensivas**: `getSales()` retorna una copia de la lista interna para evitar modificaciones externas al estado del supermercado.
- **Horarios nocturnos**: `BusinessHours` soporta horarios que cruzan la medianoche (por ejemplo, 22:00 a 06:00), verificando tanto el día actual como el día anterior.

### Tests

60 tests cubriendo:
- Caminos exitosos para todas las funcionalidades requeridas
- Casos límite (sin ventas, menos de 5 productos, exactamente 5 productos, cadena vacía, venta de todo el stock, productos con precio cero)
- Aislamiento de datos (los ingresos de un producto no afectan a otros, múltiples supermercados abiertos simultáneamente)
- Validación de errores (IDs inválidos, stock insuficiente, datos negativos, IDs duplicados, stock agotado)
