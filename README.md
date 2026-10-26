This is the webserver that receives the value from the ISS and stores it in a configured database

I mostly made this so I can easier implement the ISS Urine Tank mod in multiple games, as it's easier to get web requests running than it is to incorporate Lightstreamer's API every time

You can build your own Dockerfile, just make sure you've built the file and there's a version in target/ to copy in

# How to run this?
- Compile the mod
```mvn install```
- run the generated jarfile
```java -jar target/ISSNormalizeServer-1.0.jar```

### How to run this (in Docker)
- Compile the mod
- Build an image
```docker build -t chilly/normalizer:latest . ```
- run the container
```docker run -v ./normalizer:/server chilly/normalizer:latest```
- map a volume to /server in the container to edit config for DB information