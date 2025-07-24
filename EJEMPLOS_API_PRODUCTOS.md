# Ejemplos de API para Productos

## 1. Crear un Producto con Tamaños

### POST /api/productos

```json
{
  "nombre": "PIZZA MARGHERITA",
  "precio": 12.50,
  "imagenUrl": "https://ejemplo.com/pizza-margherita.jpg",
  "activo": true,
  "subcategoriaId": 1,
  "tamaños": [
    {
      "tamanoId": 1,
      "precio": 10.00
    },
    {
      "tamanoId": 2,
      "precio": 15.00
    },
    {
      "tamanoId": 3,
      "precio": 20.00
    }
  ]
}
```

### Respuesta esperada:

```json
{
  "id": 1,
  "nombre": "PIZZA MARGHERITA",
  "precio": 12.50,
  "imagenUrl": "https://ejemplo.com/pizza-margherita.jpg",
  "activo": true,
  "sucursalesIds": [1],
  "subcategoriaId": 1,
  "subcategoriaNombre": "Pizzas",
  "tamaños": [
    {
      "id": 1,
      "nombre": "Pequeña",
      "descripcion": "Pizza pequeña de 20cm",
      "precio": 10.00
    },
    {
      "id": 2,
      "nombre": "Mediana",
      "descripcion": "Pizza mediana de 25cm",
      "precio": 15.00
    },
    {
      "id": 3,
      "nombre": "Grande",
      "descripcion": "Pizza grande de 30cm",
      "precio": 20.00
    }
  ]
}
```

## 2. Actualizar Tamaños de un Producto

### PUT /api/productos/{id}/tamanos

```json
[
  {
    "tamanoId": 1,
    "precio": 12.00
  },
  {
    "tamanoId": 2,
    "precio": 18.00
  },
  {
    "tamanoId": 4,
    "precio": 25.00
  }
]
```

### Respuesta esperada:

```json
{
  "id": 1,
  "nombre": "PIZZA MARGHERITA",
  "precio": 12.50,
  "imagenUrl": "https://ejemplo.com/pizza-margherita.jpg",
  "activo": true,
  "sucursalesIds": [1],
  "subcategoriaId": 1,
  "subcategoriaNombre": "Pizzas",
  "tamaños": [
    {
      "id": 1,
      "nombre": "Pequeña",
      "descripcion": "Pizza pequeña de 20cm",
      "precio": 12.00
    },
    {
      "id": 2,
      "nombre": "Mediana",
      "descripcion": "Pizza mediana de 25cm",
      "precio": 18.00
    },
    {
      "id": 4,
      "nombre": "Extra Grande",
      "descripcion": "Pizza extra grande de 35cm",
      "precio": 25.00
    }
  ]
}
```

## 3. Crear un Producto sin Tamaños

### POST /api/productos

```json
{
  "nombre": "BEBIDA COLA",
  "precio": 3.50,
  "imagenUrl": "https://ejemplo.com/cola.jpg",
  "activo": true,
  "subcategoriaId": 2
}
```

## 4. Obtener Producto con Tamaños

### GET /api/productos/{id}

### Respuesta esperada:

```json
{
  "id": 1,
  "nombre": "PIZZA MARGHERITA",
  "precio": 12.50,
  "imagenUrl": "https://ejemplo.com/pizza-margherita.jpg",
  "activo": true,
  "sucursalesIds": [1],
  "subcategoriaId": 1,
  "subcategoriaNombre": "Pizzas",
  "tamaños": [
    {
      "id": 1,
      "nombre": "Pequeña",
      "descripcion": "Pizza pequeña de 20cm",
      "precio": 10.00
    },
    {
      "id": 2,
      "nombre": "Mediana",
      "descripcion": "Pizza mediana de 25cm",
      "precio": 15.00
    },
    {
      "id": 3,
      "nombre": "Grande",
      "descripcion": "Pizza grande de 30cm",
      "precio": 20.00
    }
  ]
}
```

## Notas importantes:

1. **Clave Primaria Compuesta**: La tabla `producto_tamanos` usa `producto_id` y `tamano_id` como clave primaria compuesta.
2. **Validaciones**: 
   - El `tamanoId` debe existir en la tabla `tamanos`
   - El `precio` es obligatorio para cada tamaño
   - Solo se pueden asignar tamaños a productos de la sucursal del usuario autenticado
3. **Seguridad**: Todos los endpoints requieren autenticación y el rol `SUCURSAL`
4. **Transacciones**: Las operaciones de tamaños están dentro de transacciones para garantizar consistencia
5. **DTOs Separados**: 
   - `ProductoCreacionDTO`: Para crear productos (incluye campo `tamaños` con `TamanoCreacion`)
   - `ProductoDTO`: Para respuestas (incluye campo `tamaños` con `TamanoInfo`) 