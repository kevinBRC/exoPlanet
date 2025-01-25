
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

### Commands
- Deploy: {"type": "command"\n , "content": "DEPLOY_ROVER_ID_{roverId}"}
- Create: {"type": "command"\n , "content": "CREATE_ROVER_ID_{roverId}"}
- Move: {"type": "command"\n , "content": "MOVE_ROVER_ID_{roverId}_{direction}"}
- Land: {"type": "command"\n , "content": "LAND_ROVER_ID_{roverId}_X_{xCoord}_Y_{yCoord}"}
- Scan: {"type": "command"\n , "content": "SCAN_ROVER_ID_{roverId}"}
- Scan and Move: {"type": "command"\n , "content": "SCAN_MOVE_ROVER_ID_{roverId}_{direction}"}
- rotate: {"type": "command"\n , "content": "ROTATE_ROVER_ID_{roverId}_{direction}"}
- exit: {"type": "command"\n , "content": "EXIT_ROVER_ID_{roverId}"}
- get Position: {"type": "command"\n , "content": "GETPOS_ROVER_ID_{roverId}"}
- charge: {"type": "command"\n , "content": "CHARGE_ROVER_ID_{roverId}"}
- get Charge: {"type": "command"\n , "content": "GET_CHARGE_ROVER_ID_{roverId}"}


## TODOs Kevin
[x] Define all commands Syntax as plain text
### Bodenstation
[] Create left commands
### RoverManager
[] Needs a check if the message reached the server
### DatabaseManager