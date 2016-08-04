exports.downloadiOSFrameWork = function(req, res) {
    var path = __base + 'assets/iOS-Framework/CSBM-iOS-Framework.zip';
    res.download(path);
};

exports.downloadiOSStarterProject = function(req, res) {
    var path = __base + 'assets/iOS-Framework/CSBM-iOS-StarterProject.zip';
    res.download(path);
};

exports.downloadAndroidFrameWork = function(req, res) {
    var path = __base + 'assets/Android-Framework/CSBM-Android-Framework.zip';
    res.download(path);
};

exports.downloadAndroidStarterProject = function(req, res) {
    var path = __base + 'assets/Android-Framework/CSBM-Android-StarterProject.zip';
    res.download(path);
};