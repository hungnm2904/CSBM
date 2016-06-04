const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const randomstring = require('randomstring');

var applicationSchema = new Schema({
    name: { type: String, required: true, unique: true },
    clientKey: String,
    userId: { type: String, require: true },
    created_at: Date,
    updated_at: Date
});

applicationSchema.pre('save', function(next) {
    var curDate = Date();

    this.updated_at = curDate;
    if (!this.created_at) {
        this.created_at = curDate;
    }

    if (!this.clientKey) {
        this.clientKey = randomstring.generate(24);
    }
    next();
});

var Application = mongoose.model('Application', applicationSchema);
module.exports = Application;
