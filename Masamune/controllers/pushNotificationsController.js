var appCache = require('../parse-server/lib/cache').default;
var adapterLoader = require('../parse-server/lib/Adapters/AdapterLoader');
var parseServerPushAdapter = require('../parse-server/node_modules/parse-server-push-adapter/lib/index').default;
var _PushController = require('../parse-server/lib/Controllers/PushController');

exports.pushConfig = function(req, res) {
    var appId = req.get('X-CSBM-Application-Id');
    var app = appCache.get(appId);
    var senderId = req.body.senderId;
    var apiKey = req.body.apiKey;
    var push = {
        android: {
            senderId: senderId,
            apiKey: apiKey
        }
    }

    var pushControllerAdapter = (0, adapterLoader.loadAdapter)(push && push.adapter, parseServerPushAdapter, push || {});
    var pushController = new _PushController.PushController(pushControllerAdapter, appId, push);
    app.pushController = pushController;

    res.status(200).send();
};
