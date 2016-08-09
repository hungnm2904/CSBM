const mongoose = require('mongoose');
const bcrypt = require('bcrypt-nodejs');
const Schema = mongoose.Schema;

var collaboration = mongoose.Schema({
    appId: { type: String, unique: true },
    role: String
}, { _id: false });

var userSchema = new Schema({
    email: { type: String, required: true },
    password: { type: String, required: true },
    role: String,
    collaborations: [collaboration],
    created_at: Date,
    updated_at: Date
});

userSchema.pre('save', function(next, done) {
    var user = this;
    var curDate = Date();

    if (!user.role) {
        user.role = 'Dev'
    }

    user.updated_at = curDate;
    if (!user.created_at) {
        user.created_at = curDate;
    }

    if (!user.isModified('password')) return next();

    bcrypt.genSalt(10, function(err, salt) {
        if (err) return next(err);

        bcrypt.hash(user.password, salt, null, function(err, hash) {
            if (err) return next(err);
            user.password = hash;
            next();
        });
    });
});

userSchema.methods.verifyPassword = function(password, cb) {
    bcrypt.compare(password, this.password, function(err, isMatch) {
        if (err) return cb(err);
        cb(null, isMatch);
    });
};

module.exports = mongoose.model('User', userSchema);
