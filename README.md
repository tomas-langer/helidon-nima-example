# helidon-nima-example

Helidon Níma is a project within Helidon to create a Virtual Thread (coming from Java's project Loom) based web server that provides good performance comparable to existing reactive solutions.

Níma started as a standalone server; after proving the idea, we have decided to remove reactive components from Helidon, and replace them with the blocking implementation.

So since Helidon 4.0.0-M2, there is no "níma" in Helidon, it is "just" Helidon based on virtual threads.

I am keeping the module names in this example project to provide a readable history, please keep in mind:

- Module `nima` is Helidon 4.0.0 based module using blocking web server on Virtual Threads
- Module `reactive` is Helidon 3.x based module using reactive web server

## Latest changes (4.0.0-M2)

1. Moved to Java 21 (`--enable-preview` is no longer required)
2. Removed "níma" - now Helidon 4.0.0-M2 
   (we have removed reactive webserver and use "níma" webserver based on virtual threads)
3. A few API changes to align with Helidon structure

## Description
Helidon Níma Example - Loom based webserver
This example is built on top of an M2 release of Helidon 4. Milestone releases are builds that move towards our production release, we currently plan this as the last milestone (next should be release candidate).

If you encounter an issue with Helidon, please kindly report it at https://github.com/oracle/helidon/issues/new 

## Prerequisites
Java 21 early access build
Maven

## How to

1. Build the project from the repository root
 
        mvn clean package
2. Run the application

        java -jar nima/target/example-nima-blocking.jar
3. Call the endpoints (default count is 3)

        curl -i http://localhost:8080/one
        curl -i http://localhost:8080/sequence
        curl -i http://localhost:8080/sequence?count=4
        curl -i http://localhost:8080/parallel
        curl -i http://localhost:8080/parallel?count=2
