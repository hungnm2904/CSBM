const authentication = require('../authentication');
const Application = require('../models/application');
const User = require('../models/user');
var appCache = require('../parse-server/lib/cache').default;

module.exports = function(appHelpers) {

    /* Create new application
    This method will create appliation for current user

    method: POST
    headers: none
    body: applicationName (String)
    */
    var create = function(req, res) {
        var name = req.body.applicationName;
        var newApp = new Application({
            name: name,
            userId: req.user._id
        });

        newApp.save(function(error, application) {
            if (error) {
                console.log(error);
                if (error.code === 11000) {
                    res.status(403).send({
                        message: 'Application name is duplicated'
                    });
                } else {
                    res.status(500).send({
                        message: 'Error while processing'
                    });
                }
            } else {
                appHelpers.runApplication(application);

                // Clear masterKey before response to client
                application.masterKey = '';
                res.send(application);
            }
        });
    };

    /* Remove application
    This method will remove application and its database 

    method: DELETE
    headers: X-CSBM-Application-Id
    body: password (String)
    */
    var remove = function(req, res) {
        var appId = req.get('X-CSBM-Application-Id');

        if (!appId) {
            return res.status(403).send({
                message: 'Unauthorized'
            });
        }

        var username = req.user.username;
        var password = req.body.password;

        User.findOne({ 'username': username },
            function(err, user) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }
                // Username does not exist, log the error and redirect back
                if (!user) {
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                // User exists but wrong password, log the error 
                user.verifyPassword(password, function(err, isMath) {
                    if (err) {
                        console.log(err);
                        return res.status(500).send({ message: 'Error occurred while processing' });
                    }

                    if (!isMath) {
                        return res.status(403).send({
                            message: 'Password is incorrect'
                        });
                    }

                    Application.findOneAndRemove({ '_id': appId }, function(error, application) {
                        if (error) {
                            return res.status(500).send({
                                message: 'Error occurred while processing'
                            });
                        }
                    });

                    var app = appCache.get(appId);
                    var database = app.databaseController.adapter.database

                    database.dropDatabase(function(error, results) {
                        if (error) {
                            console.log(error);
                            return res.status(500).send({
                                message: 'Error occurred while processing'
                            });
                        }
                    });

                    res.status(200).send({
                        message: 'Application was deleted'
                    });
                });
            }
        );
    };

    /* Get all application of a user
    This method will get all applications of current user 

    method: GET
    headers: none
    body: none
    */
    var getAll = function(req, res) {
        Application.find({ 'userId': req.user._id }, function(err, applications) {
            if (err) {
                console.log(err);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            // Clear masterKey before response to client
            applications.forEach(function(application, index) {
                application.masterKey = '';
            });

            res.status(200).send(applications);
        });
    };

    return {
        create: create,
        remove: remove,
        getAll: getAll
    };
};
