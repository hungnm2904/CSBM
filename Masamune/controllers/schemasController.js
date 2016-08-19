const Application = require('../models/application');
const MongoClient = require('mongodb').MongoClient;

exports.getMasterKey = function(req, res) {

    var appId = req.get('X-CSBM-Application-Id');
    if (!appId) {
        return res.status(403).send({
            message: 'Unauthorized'
        });
    }

    Application.findOne({ '_id': appId }, function(err, application) {
        if (err) {
            console.log(err);
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        res.status(200).send({
            message: '',
            data: {
                masterKey: application.masterKey,
                appName: application.name
            }
        });
    });
};

exports.getAppId = function(req, res) {
    var appName = req.get('X-CSBM-Application-Name');
    var userId = req.user._id;
    // var databaseName = userId + '--' + appName;

    Application.findOne({ 'name': appName }, function(error, application) {
        if (error) {
            console.log(err);
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        res.status(200).send({
            appId: application._id
        });
    });
};

exports.getAppName = function(req, res) {

    var appId = req.get('X-CSBM-Application-Id');
    if (!appId) {
        return res.status(403).send({
            message: 'Unauthorized'
        });
    }

    Application.findOne({ '_id': appId }, function(err, application) {
        if (err) {
            console.log(err);
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        res.status(200).send({
            message: '',
            data: {
                appName: application.name
            }
        });
    });
};

exports.changeFieldName = function(req, res) {

    var appId = req.body.appId;
    var className = req.body.className;
    var fieldName = req.body.fieldName;
    var newFieldName = req.body.newFieldName;

    Application.findOne({ '_id': appId }, function(error, application) {
        if (error) {
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        if (!application) {
            return res.status(403).send({
                message: 'Application not found'
            });
        }

        var url = 'mongodb://localhost:27017/' + application.databaseName;

        MongoClient.connect(url, function(err, db) {
            if (err) {
                console.log(err);
                return res.status(500).send({
                    message: 'Error occurred while processing'
                });
            }

            var rename = {};
            rename[fieldName] = newFieldName;

            db.collection(className).updateMany({}, { $rename: rename }, function(err, results) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                db.collection('_SCHEMA').update({ _id: className }, { $rename: rename }, function(err, results) {
                    if (err) {
                        console.log(err);
                        return res.status(500).send({
                            message: 'Error occurred while processing'
                        });
                    }

                    db.close();
                    res.status(200).send();
                });

            });
        });
    });
};
