const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const randomstring = require('randomstring');

var collaborator = mongoose.Schema({
    email: { type: String, unique: true },
    role: String
}, { _id: false });

var applicationSchema = new Schema({
    name: { type: String, required: true },
    clientKey: String,
    masterKey: String,
    userId: { type: String, require: true },
    databaseName: { type: String, unique: true },
    collaborators: [collaborator],
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

    if (!this.masterKey) {
        this.masterKey = randomstring.generate(48);
    }

    if (!this.databaseName) {
        this.databaseName = this.userId + '--' + this.name;
    }
    next();
});

module.exports = mongoose.model('Application', applicationSchema);
