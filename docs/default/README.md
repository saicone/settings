---
sidebar_position: 1
title: Settings
description: Java library to interpret multiple data formats as flexible configuration.
---

Settings library handle multiple data formats as configuration in a flexible way:

* Node templates and transformation.
* Node value substitution in non-compatible formats (like json and yaml).
* Fallback format reader.
* Iterable nodes.
* Data update parameters.
* Comparable paths to get nodes.
* Multi-layer node values.

Currently supporting the formats:

* [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md)
* [JSON](https://www.json.org/) (with [Gson](https://github.com/google/gson))
* [TOML](https://toml.io/en/v1.0.0)
* [YAML](http://yaml.org/spec/1.1/current.html)

Take in count this library is not focused as an object serializer, the main purpose is making flexible interactions with multiple data formats at the same time.

It also has simple methods to get nodes as multiple data types if you want to implement your own object serializer.

## Introduction
