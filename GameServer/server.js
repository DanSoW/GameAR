const { json } = require("express");
const express = require("express");
const app = express();
const serv = require('http').Server(app);
const jsonParser = express.json();
const posgresql = require("pg");
const db = new posgresql.Client({
    user: "postgres",
    host: "localhost",
    database: "books",
    password: "password",
    port: "5432"
});

db.connect();

app.post("/book", jsonParser, function (req, res) {
    const data = req.body;

    try {
        db.query('INSERT INTO booktable (number, name, author) VALUES ($1, $2, $3);',
            [data.number, data.name, data.author], function (err, result) {
                if (err) {
                    let obj = {
                        message: "Error: unable to add data to the table!"
                    };

                    res.send(JSON.stringify(obj));
                    return;
                }

                res.send(200);
        });
    } catch {}
});

app.get("/book/get/:id", function (req, res) {
    const data = req.params.id;

    try {
        db.query('SELECT * FROM booktable WHERE number=$1', [data], function (err, result) {
            if (err) {
                let obj = {
                    message: "Error: data cannot be read from the database!"
                };

                res.send(JSON.stringify(obj));
                return;
            }

            res.send(result.rows);
        });
    } catch {}
});

serv.listen(8080);