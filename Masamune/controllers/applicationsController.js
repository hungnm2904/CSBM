const authentication = require('../authentication');
const Application = require('../models/application');

module.exports = function(appHelpers) {

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

    var remove = function(req, res) {
        var appId = req.get('X-CSBM-Application-Id');

        if (!appId) {
            return res.status(403).send();
        }

        Application.findOneAndRemove({ '_id': appId }, function(err, application) {
            if (err) {
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }
        });

        res.status(200).send();
    };

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
