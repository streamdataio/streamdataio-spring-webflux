# Simple Spring Web Flux app using streamdata.io

This is a sample implementation of a [Spring Web Flux](https://github.com/spring-projects/spring-framework/blob/master/src/docs/asciidoc/web/web-flux.adoc) 
client that connects to an [Server-Sent Events](https://www.w3.org/TR/eventsource/) Source provided
by the [streamdata.io](http://streamdata.io/) service. Apart from Spring Web Flux, this demo uses [zjsonpatch](https://github.com/flipkart-incubator/zjsonpatch)
 as a Java Json-Patch implementation. zjsonpatch relies itself on [Jackson]((http://wiki.fasterxml.com/JacksonHome)) a Java Json library.

## Step by step setup

1. Create an free account on streamdata.io https://portal.streamdata.io/#/register to get an App token.

2. Clone project, edit Main.java and replace ```[YOUR TOKEN HERE]``` with your App token.

3. Make sure you have Java 8+ installed

4. Make sure you have maven 3.0+ installed

5. Build project with maven:

  ```
  mvn clean install
  ```

6. Run sample from a terminal:

  ```
  java -jar target/streamdataio-spring-webflux-0.0.1-SNAPSHOT.jar  
  ```  
  
  or 
  
  ```
  mvn clean spring-boot:run 
  ```

You should see data and patches pushed in your application and displayed on your terminal.

You can use the provided demo example API which simulates updating stocks prices from a financial market:
'http://stockmarket.streamdata.io/prices'

Feel free to test it with any REST/Json API of your choice.

## Notes

This is a fairly basic implementation and can be viewed more as a starting
point for your own implementation.

Dependening on your use-case, you may enhance:

- the error handling (retry, etc.) but by keeping in
mind that is wiser to close the connection if "fatal" errors
are received (errors that should stop any processing, etc.)
- patch handling
- ...
 
A blog post is published [here](http://streamdata.io/using-spring-web-flux-as-a-java-client-of-streamdata-io) 