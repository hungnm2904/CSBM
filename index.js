// Example express application adding the parse-server module to expose Parse
// compatible API routes.

const express = require('express');
const bodyParser = require('body-parser')
const ParseServer = require('parse-server').ParseServer;
const path = require('path');
const mongoose = require('mongoose');
const User = require('./models/user');
const Application = require('./models/application');
const passport = require('passport');
const configHelpers = require('./configHelpers');

var csbm = express();
csbm.use(bodyParser.json({ strict: true }));
csbm.use(bodyParser.urlencoded({ extended: true }));
csbm.use(passport.initialize());
csbm.use(function(req, res, next) {

    // Website you wish to allow to connect
    res.setHeader('Access-Control-Allow-Origin', 'http://localhost:3000');

    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');

    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type,Authorization');

    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader('Access-Control-Allow-Credentials', true);

    // Pass to next layer of middleware
    next();
});

const appHelpers = require('./appHelpers')(csbm);
const routes = require('./routes/index')(appHelpers, passport);
csbm.use('/', routes);

mongoose.connect('mongodb://localhost:27017/csbm', (err) => {
    if (err) {
        return console.log(err)
    };

    var port = process.env.PORT || 1337;
    var httpServer = require('http').createServer(csbm);
    httpServer.listen(port, function() {
        console.log('CSBM is running on port ' + port + '.');
    });

    // This will enable the Live Query real-time server
    ParseServer.createLiveQueryServer(httpServer);

    appHelpers.runAllApplication();
});
