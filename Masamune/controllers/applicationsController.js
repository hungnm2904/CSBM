const authentication = require('../authentication');
const Application = require('../models/application');
const User = require('../models/user');
var appCache = require('../parse-server/lib/cache').default;

module.exports = function(appHelpers) {

    /* Create new application
    This method will create appliation for current user

    method: POST
    headers: none
    body: 
        - applicationName (String)
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
                        message: 'Error occurred while processing'
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

    var getAll = function(req, res) {
        Application.find({}, function(error, applications) {
            if (error) {
                console.log(error);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            // Clear masterKey, databaseName, clientKey before response to client
            applications.forEach(function(application, index) {
                application.masterKey = '';
                application.databaseName = '';
                application.clientKey = '';
            });

            res.status(200).send(applications);
        });
    };

    /* Get all application of a user
    This method will get all applications of current user 

    method: GET
    headers: none
    body: none
    */
    var getAllUserById = function(req, res) {
        Application.find({ 'userId': req.user._id }, function(err, applications) {
            if (err) {
                console.log(err);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            // Clear masterKey, databaseName, clientKey before response to client
            applications.forEach(function(application, index) {
                application.masterKey = '';
                application.databaseName = '';
                application.clientKey = '';
            });

            res.status(200).send(applications);
        });
    };

    /* Update application

    method: PUT
    headers: none
    body: {object}
    */
    var update = function(req, res) {
        var appId = req.params.appId;

        if (!appId) {
            return res.status(403).send({
                message: 'Unauthorized'
            });
        }

        var data = req.body;

        Application.findOne({ '_id': appId }, function(err, application) {
            if (err) {
                console.log(err);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            if (!application) {
                return res.status(403).send({
                    message: 'Application not found'
                });
            }

            for (var field in data) {
                var value = data[field];
                var newValue = undefined;

                if (Application.schema.paths[field].instance === 'Array') {
                    var op = value.__op;
                    var values = value.objects;
                    newValue = {};

                    if (values) {
                        values.forEach(function(item, index) {
                            if (item instanceof Object) {
                                for (var key in item) {
                                    newValue[key] = item[key];
                                }
                            } else {
                                newValue = item;
                            }
                        });
                    }

                    if (op === 'Add') {
                        application[field].push(newValue);
                        if (field === 'collaborators') {
                            User.findOne({ 'email': newValue.email }, function(error, user) {
                                if (error) {
                                    console.log(error);
                                    return res.status(500).send({
                                        message: 'Error occurred while processing'
                                    });
                                }

                                if (!user) {
                                    return res.status(403).send({
                                        message: 'User not found'
                                    });
                                }

                                user.collaborations.push({
                                    'appId': appId,
                                    'role': newValue.role
                                });

                                user.save(function(error) {
                                    if (error) {
                                        console.log(error);
                                        return res.status(500).send({
                                            message: 'Error occurred while processing'
                                        });
                                    }
                                });
                            });
                        }
                    } else if (op === 'Remove') {
                        if (application[field] && application[field].length > 0) {
                            application[field].some(function(item, index) {
                                if (item.email === newValue) {
                                    application[field].splice(index, 1);

                                    User.findOne({ 'email': newValue }, function(error, user) {
                                        if (error) {
                                            console.log(error);
                                            return res.status(500).send({
                                                message: 'Error occurred while processing'
                                            });
                                        }

                                        if (user) {
                                            var collaborations = user.collaborations;
                                            collaborations.some(function(collaboration, index) {
                                                if (collaboration.appId === appId) {
                                                    user.collaborations.splice(index, 1);

                                                    return true;
                                                }
                                            });

                                            user.save(function(error) {
                                                if (error) {
                                                    console.log(error);
                                                    return res.status(500).send({
                                                        message: 'Error occurred while processing'
                                                    });
                                                }
                                            });
                                        }

                                    });

                                    return true;
                                }
                            });
                        }
                    } else if (op === 'Update') {
                        if (application[field] && application[field].length > 0) {
                            application[field].some(function(item, index) {
                                if (item.email === newValue.email) {
                                    application[field][index] = newValue;

                                    User.findOne({ 'email': newValue.email }, function(error, user) {
                                        if (error) {
                                            console.log(error);
                                            return res.status(500).send({
                                                message: 'Error occurred while processing'
                                            });
                                        }

                                        if (!user) {
                                            return res.status(403).send({
                                                message: 'User not found'
                                            });
                                        }

                                        if (user) {
                                            var collaborations = user.collaborations;
                                            collaborations.some(function(collaboration, index) {
                                                if (collaboration.appId === appId) {
                                                    user.collaborations[index].role = newValue.role;

                                                    return true;
                                                }
                                            });
                                        }

                                        user.save(function(error) {
                                            if (error) {
                                                console.log(error);
                                                return res.status(500).send({
                                                    message: 'Error occurred while processing'
                                                });
                                            }
                                        });
                                    });

                                    return true;
                                }
                            });
                        }
                    }
                } else {
                    application[field] = value;
                }
            }
            application.save(function(error) {
                if (error) {
                    console.log(error);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }
                res.status(200).send()
            });
        });
    };

    /* Get all collaborators of specified application
    This method will get all collaborators of specified application

    method: GET
    headers: none
    body: none
    */
    var getCollaborators = function(req, res) {
        var appId = req.params.appId;
        Application.findOne({ '_id': appId }, function(err, application) {
            if (err) {
                console.log(err);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            if (!application) {
                return res.status(403).send({
                    message: 'Application not found'
                });
            }

            var collaborators = application.collaborators
            res.status(200).send({
                data: collaborators
            });
        });
    };

    return {
        create: create,
        remove: remove,
        getAll: getAll,
        getAllUserById: getAllUserById,
        update: update,
        getCollaborators: getCollaborators
    };
};
