# Sensors evaluation

This is an example of Akka.typed application with websocket support. 

Server accepts a message `poll` with a number of sensors to ask and returns message with type `result` which contains sensors info.

![](/img/R.png)

App generates a requested number of sensors and fills them with random data. The boundaries for generated values can be defined in `application.config`:
```
random-generator-constraits {
  lower = -100
  upper = 100
}
```
One generated sensor represents the referenced sensor (verified/certified one). It has its own, more narrowed cotstraits:
```
reference-generator-constraits {
  lower = -80
  upper = 80
  items = 100
}
```
All sensors evaluated on creation, the mean and the standard deviation are calculated. 
Then every sensor compared with the reference. If the count of calculated values or the standard deviation of a sensor is far from the values of the reference sensor then that sensor returns in the response message. Also the reference sensor returns always on first place of the list. The acceptable variance are defined in the config:
```
variance {
  length = 0.7
  deviation = 0.95
}
```
The sensor generation is done asyncroniously in parallel. 

For work with JSON I used Circe, and Cats for messages validation. 

