const mongoose = require('mongoose');

const NotificationSchema = new mongoose.Schema(
  {
    userId:  { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, index: true },
    type:    { type: String, required: true },
    message: { type: String, required: true },
    relatedType: { type: String, enum: ['message', 'application', ''], default: '' },
    relatedId: { type: mongoose.Schema.Types.ObjectId, default: null },
    conversationId: { type: String, default: '' },
    peerId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', default: null },
    peerName: { type: String, default: '' },
    read:    { type: Boolean, default: false },
  },
  { timestamps: true }
);

module.exports = mongoose.model('Notification', NotificationSchema);
