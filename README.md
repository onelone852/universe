# Universe The Backend Framework

## Introduction

**Universe**, a backend framework implemented in Java. Universe aims to provide three things:

- Fast server
- Flawless processing
- Flexible router

These three aims is known as **FFF**.

Note that Universe is immature and still in developement.

## Killer Features

- A complete command system
  - A graceful shutdown system
  - Taking full control of server
- A annotation System
  - Easy to write router
- Highly extensible

## How to use

### Hello, world

1 - Open a Java project and import Universe.

2 - Create a class inside your main class and add `@passport.Galaxy("/")` to the class.

3 - Write a function like this inside the inner class:

```java
@Planet
@DoorWay.Get("/")
public void main(final @ResponseWriter ResponseStream rs) {
    rs.print("Hello, world!");
}
```

4 - Do this in your main function:

```java
public static void main(final String[] args) throws Exception {
    try (final Universe universe = new UniverseBuilder().build()) {
        universe.simulate(Test.class);
    }
}
```

5 - Visit 127.0.0.1, then you will see `Hello, world!`.
