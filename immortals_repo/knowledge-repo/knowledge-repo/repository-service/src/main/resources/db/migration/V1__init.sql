
CREATE TABLE immortals_context(
    name VARCHAR(55) NOT NULL,
    description VARCHAR(100) NOT NULL,
    PRIMARY KEY (name)
);

INSERT INTO immortals_context(name, description) VALUES ('unassigned', 'I describe graphs that have not been assigned a context yet.');

CREATE TABLE graph (
    name VARCHAR(55) NOT NULL,
    body TEXT not NULL,
    type VARCHAR(55) NOT NULL,
    context VARCHAR(55) NOT NULL DEFAULT 'unassigned',
    PRIMARY KEY (name),
    FOREIGN KEY (context) REFERENCES immortals_context(name)
);


INSERT INTO graph (name, body, type) VALUES ('tester','IMMoRTALS:tester a owl:class', 'tester type');