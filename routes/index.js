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

    router.post('/login', function(req, res, next) {
        var username = req.body.username;
        var password = req.body.password;

        User.findOne({ 'username': username },
            function(err, user) {
                if (err) {
                    console.log(err);
                    return res.json({
                        status: 500,
                        message: 'Error occurred while processing'
                    });
                }
                // Username does not exist, log the error and redirect back
                if (!user) {
                    return res.json({
                        statu: 403,
                        message: 'Invalid username'
                    });
                }
                // User exists but wrong password, log the error 
                user.verifyPassword(password, function(err, isMath) {
                    if (err) {
                        console.log(err);
                        return res.json({
                            status: 500,
                            message: 'Error occurred while processing'
                        });
                    } else if (!isMath) {
                        return res.json({
                            status: 403,
                            message: 'Invalid password'
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
                        return res.json({
                            status: 500,
                            message: 'Error occurred while processing'
                        });
                    }
                });

                res.json({
                    status: 200,
                    message: 'Login successfully',
                    data: {
                        userId: user._id,
                        name: user.name,
                        token: token.value
                    }
                });
            }
        );
    });

    router.post('/signup', function(req, res, next) {
        var username = req.body.username;
        var password = req.body.password;

        if (!username || !password) {
            return res.json({
                status: 403,
                message: 'Username and Password are required'
            });
        }

        User.findOne({ 'username': username },
            function(err, user) {
                if (err) {
                    console.log(err);
                    return res.json({
                        status: 500,
                        message: 'Error occurred while processing'
                    });
                } else if (user) {
                    return res.json({
                        status: 403,
                        message: 'Username is duplicated'
                    });
                }
            }
        );

        var user = new User({
            username: username,
            password: password
        });

        user.save(function(err) {
            if (err) {
                return res.json({
                    status: 500,
                    message: 'Error occurred while processing'
                });
            }
        });

        res.json({
            status: 200,
            message: 'Signup successfully',
            data: {
                userId: user._id,
                name: user.name
            }
        });
    });

    router.get('/signout', function(req, res) {
        var accessToken = req.body.access_token || req.params.access_token || req.get('Authorization').split(' ')[1]
        if (accessToken) {
            Token.findOneAndRemove({ 'value': accessToken }, function(err) {
                if (err) {
                    return res.json({
                        status: 500,
                        message: 'Error occurred while processing'
                    });
                }
            });
        }

        res.json({
            status: 200,
            message: 'Signout from CSBM Server successfully'
        });
    });

    router.get('/application/create/:name', authController.isAuthenticated, function(req, res) {
        var name = req.params.name;
        var newApp = new Application({
            name: name,
            userId: req.user._id
        });

        newApp.save(function(error, application) {
            if (error) {
                console.log(JSON.stringify(error));
                if (error.code === 11000) {
                    res.send('Application name is duplicated');
                } else {
                    res.send('Error while processing');
                }
            } else {
                appHelpers.runApplication(application);
                res.send(application);
            }
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
