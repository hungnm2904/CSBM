const express = require('express');
var router = express.Router();
const User = require('../models/user');
const Application = require('../models/application');

var isAuthenticated = function(req, res, next) {
    if (req.isAuthenticated())
        return next();
    res.send('Unauthenticated');
}

module.exports = function(appHelpers, passport) {

    router.get('/', isAuthenticated, function(req, res){
        res.send('Welocome to CSBM server');
    });

    router.post('/login', function(req, res, next) {
        passport.authenticate('login', function(err, user, info) {
            if (err) {
                return res.send('Error while processing');
            }

            if (!user) {
                return res.send(info.message);
            }

            req.logIn(user, function(err) {
                if (err) {
                    return next(err);
                }
                return res.send(user);
            });
        })(req, res, next);
    });

    router.post('/signup', function(req, res, next) {
        passport.authenticate('signup', function(err, user, info) {
            if (err) {
                return res.send('Error while processing');
            }

            if (!user) {
                return res.send(info.message);
            }

            return res.send('Signup successfully with username: ' + user.username);
        })(req, res, next);
    });

    router.get('/signout', function(req, res) {
        req.logout();
        res.send('CSBM...');
    });

    router.get('/application/create/:name', isAuthenticated, function(req, res) {
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
