# Find Flight


### Configuration and start

Prerequisites: Java 8.

Edit `config.properties` to configure app (port binding, routes search params etc.)

For service start just:

```bash
./gradlew run
```

or `gradlew.bat` on Windows.

### Usage

Make HTTP query to `/routes/<from>/<to>/[?order=changes|duration|cost[&desc]]`. E.g.:

http://localhost:8080/routes/moscow-russia/hong-kong-hong-kong/?order=cost&desc

If you need to find available locations, use `/locations/<prefix>/` like

http://localhost:8080/locations/mos/

### Data source

Airports, airlines and routes data for test purposes have been obtained from [OpenFlights](http://openflights.org/data.html).
They contains in `airlines.dat`, `airports.dat` and `routes.dat` CSV files.