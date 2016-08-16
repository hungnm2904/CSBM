const express = require('express');
const bodyParser = require('body-parser')
const ParseServer = require('./parse-server/lib/index').ParseServer;
// const path = require('path');
const mongoose = require('mongoose');
const passport = require('passport');
// const configHelpers = require('./configHelpers');
const middlewares = require('./parse-server/lib/middlewares');
const _FilesRouter = require('./parse-server/lib/Routers/FilesRouter');
const _PublicAPIRouter = require('./parse-server/lib/Routers/PublicAPIRouter');
const _PromiseRouter = require('./parse-server/lib/PromiseRouter');
const _ClassesRouter = require('./parse-server/lib/Routers/ClassesRouter');
const _HooksRouter = require('./parse-server/lib/Routers/HooksRouter');
const _UsersRouter = require('./parse-server/lib/Routers/UsersRouter');
const _SessionsRouter = require('./parse-server/lib/Routers/SessionsRouter');
const _RolesRouter = require('./parse-server/lib/Routers/RolesRouter');
const _AnalyticsRouter = require('./parse-server/lib/Routers/AnalyticsRouter');
const _InstallationsRouter = require('./parse-server/lib/Routers/InstallationsRouter');
const _FunctionsRouter = require('./parse-server/lib/Routers/FunctionsRouter');
const _SchemasRouter = require('./parse-server/lib/Routers/SchemasRouter');
const _PushRouter = require('./parse-server/lib/Routers/PushRouter');
const _LogsRouter = require('./parse-server/lib/Routers/LogsRouter');
const _IAPValidationRouter = require('./parse-server/lib/Routers/IAPValidationRouter');
const _FeaturesRouter = require('./parse-server/lib/Routers/FeaturesRouter');
const _GlobalConfigRouter = require('./parse-server/lib/Routers/GlobalConfigRouter');
const _PurgeRouter = require('./parse-server/lib/Routers/PurgeRouter');
const batch = require('./parse-server/lib/batch');

global.__base = __dirname + '/';

var csbm = express();
var parse = express();
const authentication = require('./authentication');

parse.use('/', middlewares.allowCrossDomain, new _FilesRouter.FilesRouter().getExpressRouter({
    maxUploadSize: '20mb'
}));

parse.use('/', bodyParser.urlencoded({ extended: false }), new _PublicAPIRouter.PublicAPIRouter().expressApp());

parse.use(bodyParser.json({ 'type': '*/*', limit: '20mb' }));
parse.use(middlewares.allowCrossDomain);
parse.use(middlewares.allowMethodOverride);
parse.use(middlewares.handleParseHeaders);

let routers = [
    new _ClassesRouter.ClassesRouter(),
    new _UsersRouter.UsersRouter(),
    new _SessionsRouter.SessionsRouter(),
    new _RolesRouter.RolesRouter(),
    new _AnalyticsRouter.AnalyticsRouter(),
    new _InstallationsRouter.InstallationsRouter(),
    new _FunctionsRouter.FunctionsRouter(),
    new _SchemasRouter.SchemasRouter(),
    new _PushRouter.PushRouter(),
    new _LogsRouter.LogsRouter(),
    new _IAPValidationRouter.IAPValidationRouter(),
    new _FeaturesRouter.FeaturesRouter(),
    new _GlobalConfigRouter.GlobalConfigRouter(),
    new _PurgeRouter.PurgeRouter()
];

if (process.env.PARSE_EXPERIMENTAL_HOOKS_ENABLED || process.env.TESTING) {
    routers.push(new _HooksRouter.HooksRouter());
}

let routes = routers.reduce((memo, router) => {
    return memo.concat(router.routes);
}, []);

let appRouter = new _PromiseRouter.default(routes);

batch.mountOnto(appRouter);

parse.use(appRouter.expressApp());
parse.use(middlewares.handleParseErrors);

csbm.use(bodyParser.json({ strict: true }));
csbm.use(bodyParser.urlencoded({ extended: true }));
csbm.use(function(req, res, next) {

    // Website you wish to allow to connect
    res.setHeader('Access-Control-Allow-Origin', 'http://localhost:3000');

    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');

    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type,Authorization,X-CSBM-Application-Id,X-CSBM-Application-Name');

    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader('Access-Control-Allow-Credentials', true);

    // Pass to next layer of middleware
    next();
});
csbm.use(passport.initialize());
const appHelpers = require('./helpers/application')(csbm);
const applicationsController = require('./controllers/applicationsController')(appHelpers);
const usersController = require('./controllers/usersController');
const schemasController = require('./controllers/schemasController');
const pushNotificationsController = require('./controllers/pushNotificationsController');
const filesController = require('./controllers/filesController');
csbm.use('/csbm', parse);
// User Controller
csbm.post('/login', usersController.login);
csbm.post('/signup', usersController.signup);
csbm.get('/signout', usersController.signout);
csbm.get('/collaborations', authentication.isAuthenticated, usersController.getCollaboration);
csbm.get('/collaborations/:appId', authentication.isAuthenticated, usersController.getCollaborationRole);
//

// Application Controller
csbm.post('/applications', authentication.isAuthenticated, applicationsController.create);
csbm.delete('/applications', authentication.isAuthenticated, applicationsController.remove);
csbm.get('/applications', authentication.isAuthenticated, applicationsController.getAllUserById);
csbm.get('/applications/all', authentication.isAuthenticated, applicationsController.getAll);
csbm.put('/applications/:appId', authentication.isAuthenticated, applicationsController.update);
csbm.get('/applications/collaborators/:appId', authentication.isAuthenticated, applicationsController.getCollaborators)
//

// Schema Controller
csbm.get('/masterKey', authentication.isAuthenticated, schemasController.getMasterKey);
csbm.get('/appId', authentication.isAuthenticated, schemasController.getAppId);
csbm.get('/appName', authentication.isAuthenticated, schemasController.getAppName);
csbm.post('/fields', authentication.isAuthenticated, schemasController.changeFieldName);
//

// Push Notification Controller
csbm.post('/pushConfig', authentication.isAuthenticated, pushNotificationsController.pushConfig);

// File Controller (Documents)
csbm.get('/files/framework/ios', filesController.downloadiOSFrameWork);
csbm.get('/files/starter-project/ios', filesController.downloadiOSStarterProject);
csbm.get('/files/framework/android', filesController.downloadAndroidFrameWork);
csbm.get('/files/starter-project/android', filesController.downloadAndroidStarterProject);
//

mongoose.connect('mongodb://localhost:27017/csbm', (err) => {
    if (err) {
        return console.log(err)
    };

    var port = process.env.PORT || 1337;
    var httpServer = require('http').createServer(csbm);
    httpServer.listen(port, function() {
        console.log('CSBM is running on port ' + port + '.');
    });

    // This will enable the Live Query real-time server
    ParseServer.createLiveQueryServer(httpServer);

    appHelpers.runAllApplication();
});
