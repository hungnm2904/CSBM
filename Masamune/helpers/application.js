const ParseServer = require('../parse-server/lib/index').ParseServer;
const Application = require('../models/application');

module.exports = function(server) {

    /*Start application on CSBM server*/
    var runApplication = function(application) {
        // var api = new ParseServer({
        //     databaseURI: 'mongodb://localhost:27017/' + application.name,
        //     // cloud: __dirname + '/cloud/main.js',
        //     appId: application._id,
        //     clientKey: application.clientKey,
        //     masterKey: application.masterKey, // Keep it secret!
        //     serverURL: 'http://localhost:1337/csbm'
        // });

        // server.use('/csbm', api);

        new ParseServer({
            databaseURI: 'mongodb://localhost:27017/' + application.databaseName,
            appId: application._id,
            clientKey: application.clientKey,
            masterKey: application.masterKey, // Keep it secret!
            serverURL: 'http://localhost:1337/csbm'
            // liveQuery: {
            //     classNames: ['Message']
            // }
            // push: {
            //     android: {
            //         senderId: '50356323544',
            //         apiKey: 'AIzaSyBYIsJeRnyY_yHgExuEaRWeMdOEwIh5AEo'
            //     },
            // }
            // push: {
            //     android: {
            //         senderId: '1657454733',
            //         apiKey: 'AIzaSyDcmGLrwgfrpuPIfBIrefBTH3PAKxC5wq0'
            //     },
            // }
        });

        console.log(application.name + ' is running on http://localhost:1337/csbm/');
    };

    /*Start all application on CSBM server*/
    var runAllApplication = function() {
        Application.find({}, function(err, results) {
            if (err) {
                console.log(err);
                return;
            }

            if (results.length > 0) {
                results.map(function(s) {
                    runApplication(s);
                    // const Config = require('parse-server/lib/Config');
                    // var config = new Config('57428c90f6036a1e2514a215', '/csbm');
                    // console.log(config);
                });
            } else {
                console.log('No application to run');
            }
        });
    };

    return {
        runApplication: runApplication,
        runAllApplication: runAllApplication
    }
}
