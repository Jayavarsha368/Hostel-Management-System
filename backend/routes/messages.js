const express     = require('express');
const mongoose    = require('mongoose');
const Message     = require('../models/Message');
const Notification = require('../models/Notification');
const User        = require('../models/User');
const { protect } = require('../middleware/auth');
const { sendPushToUser } = require('../services/pushNotifications');

const router = express.Router();

router.use(protect);

// ── GET /messages/conversations ───────────────────────────────────────────────
router.get('/conversations', async (req, res) => {
  try {
    const latestMessages = await Message.aggregate([
      { $match: { $or: [{ senderId: req.user._id }, { receiverId: req.user._id }] } },
      { $sort: { createdAt: -1 } },
      { $group: { _id: '$conversationId', message: { $first: '$$ROOT' } } },
      { $sort: { 'message.createdAt': -1 } },
      { $limit: 100 },
      { $replaceRoot: { newRoot: '$message' } },
    ]);

    const conversations = await Message.populate(latestMessages, [
      { path: 'senderId', select: 'name email' },
      { path: 'receiverId', select: 'name email' },
    ]);
    res.json(conversations);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── GET /messages/:conversationId ─────────────────────────────────────────────
router.get('/:conversationId', async (req, res) => {
  try {
    const messages = await Message.find({
      conversationId: req.params.conversationId,
      $or: [{ senderId: req.user._id }, { receiverId: req.user._id }],
    })
      .populate('senderId',   'name email')
      .populate('receiverId', 'name email')
      .sort({ createdAt: 1 }); // oldest first

    res.json(messages);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── POST /messages ────────────────────────────────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { conversationId, receiverId, text } = req.body;

    if (!conversationId || !receiverId || !text) {
      return res.status(400).json({ error: 'conversationId, receiverId, and text are required.' });
    }

    if (!mongoose.isValidObjectId(receiverId)) {
      return res.status(400).json({ error: 'receiverId is invalid.' });
    }

    const participants = [req.user._id.toString(), receiverId].sort().join('_');
    if (conversationId !== participants || receiverId === req.user._id.toString()) {
      return res.status(400).json({ error: 'Conversation participants do not match the request.' });
    }

    const receiverExists = await User.exists({ _id: receiverId });
    if (!receiverExists) {
      return res.status(404).json({ error: 'Recipient not found.' });
    }

    const message = await Message.create({
      conversationId,
      senderId:   req.user._id,
      receiverId,
      text,
    });

    await message.populate([
      { path: 'senderId', select: 'name email' },
      { path: 'receiverId', select: 'name email' },
    ]);

    const senderName = req.user.name || req.user.email;
    await Notification.create({
      userId: receiverId,
      type: 'New message',
      message: `New message from ${senderName}.`,
      relatedType: 'message',
      relatedId: message._id,
      conversationId,
      peerId: req.user._id,
      peerName: senderName,
    });

    sendPushToUser(receiverId, {
      type: 'message',
      title: `Message from ${senderName}`,
      body: text.slice(0, 180),
      peerId: req.user._id.toString(),
      peerName: senderName,
      conversationId,
      messageId: message._id.toString(),
    }).catch((err) => console.error('[FCM]', err.message));

    res.status(201).json(message);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
