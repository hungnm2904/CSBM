const express = require('express');
var router = express.Router();
const authController = require('../controllers/auth');
const User = require('../models/user');
const Application = require('../models/application');
const Token = require('../models/token');

module.exports = function(appHelpers) {

    router.get('/', function(req, res) {
        res.send('Welocome to CSBM server');
    });

    router.post('/login', function(req, res) {
        // Get username and password form request body
        var username = req.body.username;
        var password = req.body.password;

        User.findOne({ 'username': username },
            function(err, user) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({ message: 'Error occurred while processing' });
                }
                // Username does not exist, log the error and redirect back
                if (!user) {
                    return res.status(403).send({
                        message: 'Invalid username'
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
                            message: 'Invalid password'
                        });
                    }

                    // Check and remove previous token from this user.
                    Token.findOneAndRemove({ 'userId': user._id }, function(err) {
                        if (err) {
                            return res.status(500).send({
                                message: 'Error occurred while processing'
                            });
                        }
                    });

                    // Valid username and password
                    var token = new Token({
                        value: uid(256),
                        userId: user._id,
                    });

                    token.save(function(err) {
                        if (err) {
                            console.log(err)
                            return res.status(500).send({
                                message: 'Error occurred while processing'
                            });
                        }
                    });

                    res.status(200).send({
                        data: {
                            userId: user._id,
                            name: user.name,
                            token: token.value
                        }
                    });
                });
            }
        );
    });

    router.post('/signup', function(req, res) {
        var username = req.body.username;
        var password = req.body.password;
        var email = req.body.email;

        if (!username || !password || !email) {
            return res.status(403).send({
                message: 'Username, Password and Email are required'
            });
        }

        User.findOne({ 'username': username },
            function(err, user) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                if (user) {
                    return res.status(403).send({
                        message: 'Username is already exists'
                    });
                }

                User.findOne({ 'email': email },
                    function(err, user) {
                        if (err) {
                            console.log(err);
                            return res.status(500).send({
                                message: 'Error occurred while processing'
                            });
                        }

                        if (user) {
                            return res.status(403).send({
                                message: 'Email is already in use'
                            });
                        }

                        var user = new User({
                            username: username,
                            password: password,
                            email: email
                        });

                        user.save(function(err) {
                            if (err) {
                                return res.status(500).send({
                                    message: 'Error occurred while processing'
                                });
                            }
                        });

                        res.status(200).send({
                            message: 'Signup successfully',
                            data: {
                                userId: user._id,
                                name: user.username,
                                email: user.email
                            }
                        });
                    }
                );
            }
        );
    });

    router.get('/signout', function(req, res) {
        var accessToken = req.body.access_token || req.params.access_token || req.get('Authorization').split(' ')[1]
        if (accessToken) {
            Token.findOneAndRemove({ 'value': accessToken }, function(err) {
                if (err) {
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }
            });
        }

        res.status(200).send({
            message: 'Signout from CSBM Server successfully'
        });
    });

    router.post('/applications', authController.isAuthenticated, function(req, res) {
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
    });

    router.delete('/applications', authController.isAuthenticated, function(req, res) {
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
    });

    router.get('/applications', authController.isAuthenticated, function(req, res) {
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
    });

    router.get('/masterKey', authController.isAuthenticated, function(req, res) {
        var appId = req.get('X-CSBM-Application-Id');
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
                    masterKey: application.masterKey
                }
            });
        });
    });

    return router;
}

function uid(len) {
    var buf = [],
        chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',
        charlen = chars.length;

    for (var i = 0; i < len; ++i) {
        buf.push(chars[getRandomInt(0, charlen - 1)]);
    }

    return buf.join('');
};

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
