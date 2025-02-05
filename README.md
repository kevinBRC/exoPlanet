
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

### allRoverInformation Datastructure
```json
{
    "ID{digit}": {
        "position": [x, y],
        "direction": "String",
        "crashed": "Boolean" 
    },
}
```

### mapInformation Datastructure
```json
{
    "Planet": 
    [
        "rows": "int",
        "cols": "int"
        [
            "id": "int",
            "Row": "int",
            "Col": "int",
            "Temp": "int",
            "surface": "int"
        ]
    ]
    

    
}
```


## Commands
### To Server
- Deploy: {"type": "DEPLOY",\n "id": "{roverId}"}
- Create: {"type": "CREATE",\n "id": "{roverId}"}
- Move: {"type": "MOVE",\n "id": "{roverId}",\n}
- Land: {"type": "LAND",\n "id": "{roverId},\n "Coords": "[int, int]",\n}
- Scan: {"type": "SCAN",\n "id": "{roverId}"}
- Move and Scan: {"type": "MOVE_SCAN",\n "content": "{roverId}",\n}
- rotate: {"type": "ROTATE"\n , "id": "{roverId}",\n "rotation": "{direction}"}
- exit: {"type": "EXIT",\n "id": "{roverId}"}
- get Position: {"type": "GETPOS",\n "id": "{roverId}"}
- charge: {"type": "CHARGE"\n , "id": "{roverId}"}
- get Charge: {"type": "GET_CHARGE"\n , "id": "{roverId}"}
- Toggle autopilot {"type": "SWITCH_AUTOPILOT",\n "id": "{roverId}",\n "autopilot": {boolean}"}

### To Client
- answer: {"type": "{command_type}",\n "id": "{roverId}",\n "success": "{boolean}",\n "scanResponse": "{"Coords": "[int, int]",\n "surface": {"String"},\n "temperature": {int}\n}",\n "position": "[int, int]",\n "direction": "{String}", "crashed": "{boolean}"}

## TODOs Kevin
- RoverServer Create
- Manche Buttons disablen, wenn der Advanced Modus aus ist 
- Verarbeitung der Antworten auf Bodenstationseite
- GUI -> Map des Planetens einbauen
- ggf. Datenbank aufsetzen