const express = require('express');
var router = express.Router();
const User = require('../models/user');

module.exports = function() {

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

    return router;
};
