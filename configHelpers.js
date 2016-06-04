const Config = require('parse-server/lib/Config');

module.exports = function() {
    var getConfigByAppId = function(appId, callback) {
    	var config = new Config(appId, '/csbm');

    	callback(config);
    };

    return {
    	getConfigByAppId: getConfigByAppId
    };
};
