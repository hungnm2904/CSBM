const User = require('../models/user');
const Application = require('../models/application');
const Token = require('../models/token');
const uid = require('../helpers/uid');

var acceptedRole = 'Dev';
var clone = function(obj) {
    if (!obj || typeof obj !== 'object') {
        return null;
    }

    var clone = {};
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            clone[key] = obj[key];
        }
    }

    return clone._doc;
};

exports.login = function(req, res) {
    var email = req.body.email;
    var password = req.body.password;

    User.findOne({$and: [{ 'email': email }, { 'role': { $ne: 'Disable' } }]},
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
                        role: user.role,
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
    var role = req.body.role;
    if (!role || !acceptedRole.includes(role)) {
        role = undefined
    }

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
                password: password,
                role: role
            });

            user.save(function(err) {
                if (err) {
                    console.log(err);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                res.status(200).send({
                    message: 'Signup successfully',
                    data: {
                        userId: user._id,
                        role: user.role,
                        email: user.email
                    }
                });
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

exports.getCollaboration = function(req, res) {
    var userId = req.user._id;
    User.findOne({ '_id': userId }, function(error, user) {
        if (error) {
            console.log(error)
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        if (!user) {
            return res.status(403).send({
                message: 'User not found'
            });
        }

        var collaborations = user.collaborations;
        var applications = [];

        if (!collaborations || collaborations.length === 0) {
            return res.status(200).send({
                message: 'No collaboration'
            })
        }

        collaborations.forEach(function(collaboration, index) {
            var appId = collaboration.appId;
            var role = collaboration.role;

            Application.findOne({ $and: [{ '_id': appId }, { 'status': true }] }, function(error, application) {
                if (error) {
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                if (application) {
                    application.masterKey = '';
                    application.databaseName = '';
                    application.clientKey = '';
                    application.collaborators = [];
                    var app = clone(application);
                    app.role = role;

                    applications.push(app);
                }

                if (index === (collaborations.length - 1)) {
                    res.status(200).send({
                        data: {
                            'applications': applications
                        }
                    });
                }
            });
        });
    });
};

exports.getCollaborationRole = function(req, res) {
    var appId = req.params.appId;
    var userId = req.user._id;
    User.findOne({ '_id': userId }, function(error, user) {
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

        var collaborations = user.collaborations;
        var role = undefined;
        collaborations.some(function(collaboration, index) {
            if (collaboration.appId === appId) {
                role = collaboration.role;

                return true;
            }
        });

        if (!role) {
            return res.status(403).send({
                message: 'Unauthorized'
            });
        }

        res.status(200).send({ 'role': role });
    });
};

exports.checkPasswordWithCurrentUser = function(req, res) {
    var password = req.body.password;
    var userId = req.user._id
    User.findOne({ '_id': userId }, function(error, user) {
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

        user.verifyPassword(password, function(err, isMath) {
            if (err) {
                console.log(err);
                return res.status(500).send({ message: 'Error occurred while processing' });
            }

            return res.status(200).send({
                isMath: isMath
            });
        });
    });
};

exports.getAllUser = function(req, res) {
    User.find({ 'role': { $ne: 'Admin' } }, function(error, users) {
        if (error) {
            console.log(error);
            return res.status(500).send({ message: 'Error occurred while processing' });
        }

        return res.status(200).send({
            data: {
                users: users
            }
        });
    });
};

exports.changeUserStatus = function(req, res) {
    userId = req.body.userId;
    status = req.body.status;
    User.findOne({ '_id': userId }, function(error, user) {
        if (error) {
            console.log(error);
            return res.status(500).send({
                message: 'Error occurred while processing'
            });
        }

        if (user) {
            if (status === true) {
                user.role = 'Dev';
            } else {
                user.role = 'Disable';
            }
            user.save(function(error) {
                if (error) {
                    console.log(error);
                    return res.status(500).send({
                        message: 'Error occurred while processing'
                    });
                }

                res.status(200).send();
            });
        }
    });
};
