This is the webserver that receives the value from the ISS and repeats it to any client that requests it on port 7000

I mostly made this so I can easier implement the ISS Urine Tank mod in multiple games, as it's easier to get web requests running than it is to incorporate Lightstreamer's API every time

You can build your own Dockerfile, just make sure you've build the mod and there's a version in target/ to copy in