# Find Flight

## Configuration and start

Prerequisites: Java 8.

Edit `config.properties` to configure app (port binding, database URL, etc).

For service start just:

```bash
./gradlew run
```

or `gradlew.bat` on Windows.

## Usage

Make HTTP query to `localhost:8080/routes/<from>/<to>/[?order=changes|duration|cost[&desc]]`. E.g.:

```bash
http "localhost:8080/routes/moscow/paris/?order=cost&desc"
HTTP/1.1 200 OK
Content-Encoding: gzip
Content-Type: application/json
Date: Sun, 03 Apr 2016 16:26:02 GMT
Server: Jetty(9.3.2.v20150730)
Transfer-Encoding: chunked

[
    {
        "changesNum": 1,
        "duration": 490,
        "totalCost": 110.0,
        "transfers": [
            {
                "departureTime": "2016-05-12T17:00:00",
                "duration": 50,
                "flight": {
                    "cost": 40.0,
                    "id": 3,
                    "name": "F4"
                },
                "from": {
                    "id": 0,
                    "name": "Moscow"
                },
                "to": {
                    "id": 3,
                    "name": "Saint Petersburg"
                },
                "waitingTime": 0
            },
            {
                "departureTime": "2016-05-12T22:00:00",
                "duration": 190,
                "flight": {
                    "cost": 70.0,
                    "id": 8,
                    "name": "F9"
                },
                "from": {
                    "id": 3,
                    "name": "Saint Petersburg"
                },
                "to": {
                    "id": 6,
                    "name": "Paris"
                },
                "waitingTime": 0
            }
        ]
    },
    {
        "changesNum": 2,
        "duration": 280,
        "totalCost": 55.0,
        "transfers": [
            {
                "departureTime": "2016-05-12T17:00:00",
                "duration": 50,
                "flight": {
                    "cost": 40.0,
                    "id": 3,
                    "name": "F4"
                },
                "from": {
                    "id": 0,
                    "name": "Moscow"
                },
                "to": {
                    "id": 3,
                    "name": "Saint Petersburg"
                },
                "waitingTime": 0
            },
            {
                "departureTime": "2016-05-12T18:10:00",
                "duration": 70,
                "flight": {
                    "cost": 40.0,
                    "id": 3,
                    "name": "F4"
                },
                "from": {
                    "id": 3,
                    "name": "Saint Petersburg"
                },
                "to": {
                    "id": 4,
                    "name": "Oslo"
                },
                "waitingTime": 20
            },
            {
                "departureTime": "2016-05-12T20:30:00",
                "duration": 70,
                "flight": {
                    "cost": 15.0,
                    "id": 4,
                    "name": "F5"
                },
                "from": {
                    "id": 4,
                    "name": "Oslo"
                },
                "to": {
                    "id": 6,
                    "name": "Paris"
                },
                "waitingTime": 0
            }
        ]
    }
]

```

## Database schema

See `sample_db.sql`.
