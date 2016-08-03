const User = require('../models/user');
const Token = require('../models/token');
const uid = require('../helpers/uid');

exports.login = function(req, res) {
    var email = req.body.email;
    var password = req.body.password;

    User.findOne({ 'email': email },
        function(err, user) {
            if (err) {
                console.log(err);
                return res.status(500).send({ message: 'Error occurred while processing' });
            }
            // User does not exist
            if (!user) {
                return res.status(403).send({
                    message: 'Invalid Email or Password'
                });
            }
            // User exist but wrong password
            user.verifyPassword(password, function(err, isMath) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({ message: 'Error occurred while processing' });
                }

                if (!isMath) {
                    return res.status(403).send({
                        message: 'Invalid Email or Password'
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

                // Valid email and password
                var token = new Token({
                    value: uid.gen(256),
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
                        email: user.email,
                        token: token.value
                    }
                });
            });
        }
    );
};

exports.signup = function(req, res) {
    var email = req.body.email;
    var password = req.body.password;

    if (!email || !password) {
        return res.status(403).send({
            message: 'Email and Password are required'
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
                email: email,
                password: password
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
                    email: user.email
                }
            });
        }
    );
};

exports.signout = function(req, res) {
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
};