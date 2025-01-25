
# Bodenstation

## RoverManager

### roverAlive Datastructure
```json
{
    "amountOfRover": "int",
    "latestId": "int",
    "rover": ["JSONArray, maximum 5 Rover"],
    "alreadyDeployed": ["JSONArray"]
}
```SCAN_MOVE

### Commands structure
```json
{
    "type": "String",
    "content": "String"
}
```

### Commands
- Deploy: {"type": "DEPLOY"\n, "id": "{roverId}"}
- Create: {"type": "CREATE"\n, "id": "{roverId}"}
- Move: {"type": "MOVE"\n, "id": "{roverId}"\n, "direction": {direction}"}
- Land: {"type": "LAND"\n, "id": "{roverId}\n, "x": "{xCoord}"\n, "y": "{yCoord}"}
- Scan: {"type": "SCAN"\n, "id": "{roverId}"}
- Scan and Move: {"type": "SCAN_MOVE"\n, "content": "{roverId}"\n, "direction": "{direction}"}
- rotate: {"type": "ROTATE"\n , "id": "{roverId}"\n, "direction": "{direction}"}
- exit: {"type": "EXIT"\n, "id": "{roverId}"}
- get Position: {"type": "GETPOS"\n, "id": "{roverId}"}
- charge: {"type": "CHARGE"\n , "id": "{roverId}"}
- get Charge: {"type": "GET_CHARGE"\n , "id": "{roverId}"}


## TODOs Kevin
[x] Define all commands Syntax as plain text
### Bodenstation
[] Create left commands
### RoverManager
[] Needs a check if the message reached the server
### DatabaseManager