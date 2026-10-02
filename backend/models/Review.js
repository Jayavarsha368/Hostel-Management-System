const mongoose = require('mongoose');

const ReviewSchema = new mongoose.Schema(
  {
    reviewerId:     { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    reviewedUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    rating:         { type: Number, min: 1, max: 5, required: true },
    comment:        { type: String, default: '' },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Review', ReviewSchema);
