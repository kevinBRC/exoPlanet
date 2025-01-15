
# Bodenstation

## RoverManager

### roverAlive Datastructure
```json
{
    "amountOfRover": "int",
    "latestId": "int",
    "rover": ["JSONArray of maximum 5 Rover"],
    "alreadyDeployed": ["JSONArray"]
}
```

### Commands structure
```json
{
    "type": "String",
    "content": "String"
}
```

## TODOs Kevin
[] Define all commands Syntax as plain text
### Bodenstation
[] Create left commands
### RoverManager
[] Needs a check if the message reached the server
### DatabaseManager