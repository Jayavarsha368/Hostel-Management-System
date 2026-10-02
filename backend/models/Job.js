const mongoose = require('mongoose');

const JobSchema = new mongoose.Schema(
  {
    title:          { type: String, required: true, trim: true },
    description:    { type: String, required: true },
    budget:         { type: String, default: '' },
    skillsRequired: { type: [String], default: [] },
    duration:       { type: String, default: '' },
    deadline:       { type: String, default: '' },
    clientId:       { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Job', JobSchema);
