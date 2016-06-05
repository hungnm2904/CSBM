const mongoose = require('mongoose');
const bcrypt = require('bcrypt-nodejs');
const Schema = mongoose.Schema;

var userSchema = new Schema({
    name: String,
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    created_at: Date,
    updated_at: Date
});

userSchema.pre('save', function(next, done) {
    var user = this;
    var curDate = Date();

    user.updated_at = curDate;
    if (!user.created_at) {
        user.created_at = curDate;
    }

    // Break out if the password hasn't changed
    if (!user.isModified('password')) return next();

    // Password changed so we need to hash it
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
