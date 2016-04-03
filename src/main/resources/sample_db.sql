DROP TABLE IF EXISTS transfer;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS flight;

CREATE TABLE flight (
  id    INT             NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
  name  VARCHAR(20)     NOT NULL,
  cost  DECIMAL(20, 2)                                              -- full cost of ticket on the flight
);

CREATE TABLE location (
  id    INT             NOT NULL    PRIMARY KEY AUTO_INCREMENT,
  name  VARCHAR(127)    NOT NULL
);

CREATE TABLE transfer (
  flight_id         INT         NOT NULL,
  waiting_time      INT         NOT NULL,   -- time for change in minutes (non-negative value)
  departure_time    TIMESTAMP   NOT NULL,
  duration          INT         NOT NULL,   -- flight duration in minutes (positive value)
  from_location     INT         NOT NULL,
  to_location       INT         NOT NULL,

  PRIMARY KEY(flight_id, from_location, to_location),

  FOREIGN KEY(flight_id)        REFERENCES flight(id),
  FOREIGN KEY(from_location)    REFERENCES location(id),
  FOREIGN KEY(to_location)      REFERENCES location(id)
);

INSERT INTO flight (id, name, cost) VALUES
    (0, 'F1', 10),
    (1, 'F2', 50),
    (2, 'F3', 10),
    (3, 'F4', 40),
    (4, 'F5', 15),
    (5, 'F6', 15),
    (6, 'F7', 15),
    (7, 'F8', 20),
    (8, 'F9', 70);

INSERT INTO location (id, name) VALUES
    (0, 'Moscow'),
    (1, 'Kazan'),
    (2, 'Rostov-on-Don'),
    (3, 'Saint Petersburg'),
    (4, 'Oslo'),
    (5, 'Rome'),
    (6, 'Paris');

INSERT INTO transfer (flight_id, waiting_time, departure_time, duration, from_location, to_location) VALUES
    (0, 0,    '2016-05-12 15:00:00',  60,     0, 1),
    (1, 0,    '2016-05-12 14:00:00',  80,     1, 2),
    (1, 30,   '2016-05-12 15:50:00',  120,    2, 5),
    (2, 0,    '2016-05-12 18:10:00',  90,     5, 6),
    (3, 0,    '2016-05-12 17:00:00',  50,     0, 3),
    (3, 20,   '2016-05-12 18:10:00',  70,     3, 4),
    (4, 0,    '2016-05-12 20:30:00',  70,     4, 6),
    (5, 0,    '2016-05-12 17:20:00',  75,     1, 0),
    (6, 0,    '2016-05-12 19:10:00',  50,     3, 0),
    (7, 0,    '2016-05-12 18:00:00',  45,     3, 2),
    (8, 0,    '2016-05-12 22:00:00',  190,    3, 6);