const mongoose = require('mongoose');
const Schema = mongoose.Schema;

var userSchema = new Schema({
    name: String,
    username: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    created_at: Date,
    updated_at: Date
});

userSchema.pre('save', function(next, done) {
    var curDate = Date();

    this.updated_at = curDate;
    if (!this.created_at) {
        this.created_at = curDate;
    }

    next();
});

var User = mongoose.model('User', userSchema);

module.exports = User;
