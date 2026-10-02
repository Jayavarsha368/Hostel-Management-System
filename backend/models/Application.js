const mongoose = require('mongoose');

const ApplicationSchema = new mongoose.Schema(
  {
    jobId:        { type: mongoose.Schema.Types.ObjectId, ref: 'Job',  required: true },
    freelancerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    clientId:     { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    coverLetter:  { type: String, default: '' },
    status:       { type: String, enum: ['Pending', 'Accepted', 'Rejected'], default: 'Pending' },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Application', ApplicationSchema);
