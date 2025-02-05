
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
```

## Commands
### To Server
- Deploy: {"type": "DEPLOY",\n "id": "{roverId}"}
- Create: {"type": "CREATE",\n "id": "{roverId}"}
- Move: {"type": "MOVE",\n "id": "{roverId}",\n "direction": {direction}"}
- Land: {"type": "LAND",\n "id": "{roverId},\n "Coords": "[int, int]",\n "planet": "{string}"}
- Scan: {"type": "SCAN",\n "id": "{roverId}"}
- Move and Scan: {"type": "MOVE_SCAN",\n "content": "{roverId}",\n "direction": "{direction}"}
- rotate: {"type": "ROTATE"\n , "id": "{roverId}",\n "direction": "{direction}"}
- exit: {"type": "EXIT",\n "id": "{roverId}"}
- get Position: {"type": "GETPOS",\n "id": "{roverId}"}
- charge: {"type": "CHARGE"\n , "id": "{roverId}"}
- get Charge: {"type": "GET_CHARGE"\n , "id": "{roverId}"}
- Toggle autopilot {"type": "SWITCH_AUTOPILOT",\n "id": "{roverId}",\n "autopilot": {boolean}"}

### To Client
- answer: {"type": "{command_type}",\n "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]"}

## TODOs Kevin
[x] Define all commands Syntax as plain text
### Bodenstation
[] Create left commands
### RoverManager
[] Needs a check if the message reached the server
### DatabaseManager