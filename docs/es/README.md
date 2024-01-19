---
sidebar_position: 1
title: Settings
description: Librería de Java para interpretar múltiples formatos de datos como configuraciones flexibles.
---

La librería Settings maneja diferentes formatos de datos como configuración en una forma flexible:

* Plantillas y transformaciones de nodos.
* Sustitución de valores de nodos en formatos no compatibles (como json y yaml).
* Lector de formato opcional.
* Nodos iterables.
* Parámetros para actualizar datos.
* Rutas comparables para obtener nodos.
* Valores de nodos en múltiples capas.

Actualmente soportando los formatos:

* [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md)
* [JSON](https://www.json.org/) (usando [Gson](https://github.com/google/gson))
* [TOML](https://toml.io/en/v1.0.0)
* [YAML](http://yaml.org/spec/1.1/current.html)

Tomar en cuenta que esta librería no está enfocada como un serializador de objetos, el objetivo principal es hacer una interacción flexible con múltiples formatos de data al mismo tiempo.

También tiene métodos simples para obtener nodos como múltiples tipos de datos si quieres implementar tu propio serializador de objetos.

## Introducción
