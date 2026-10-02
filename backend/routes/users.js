const express  = require('express');
const User     = require('../models/User');
const { protect } = require('../middleware/auth');
const fs = require('fs/promises');
const path = require('path');
const mongoose = require('mongoose');

const router = express.Router();
const profileFilesDirectory = process.env.PROFILE_FILES_DIR ||
  path.join(__dirname, '..', 'uploads', 'profiles');
const profileFileFields = { resume: 'resumeUrl', portfolio: 'portfolioUrl' };
const allowedFileExtensions = new Set(['.pdf', '.doc', '.docx', '.jpg', '.jpeg', '.png']);
const profileFilesBucketName = 'profileFiles';

function getProfileFilesBucket() {
  return new mongoose.mongo.GridFSBucket(User.db.db, { bucketName: profileFilesBucketName });
}

// All user routes require authentication
router.use(protect);

router.post('/push-token', async (req, res) => {
  const token = typeof req.body.token === 'string' ? req.body.token.trim() : '';
  if (token.length < 20 || token.length > 4096) {
    return res.status(400).json({ error: 'A valid push token is required.' });
  }

  try {
    await User.updateMany(
      { _id: { $ne: req.user._id } },
      { $pull: { pushTokens: token } }
    );
    await User.updateOne({ _id: req.user._id }, { $addToSet: { pushTokens: token } });
    res.json({ message: 'Push token registered.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete('/push-token', async (req, res) => {
  const token = typeof req.body?.token === 'string' ? req.body.token.trim() : '';
  if (!token) return res.status(400).json({ error: 'A push token is required.' });

  try {
    await User.updateOne({ _id: req.user._id }, { $pull: { pushTokens: token } });
    res.json({ message: 'Push token removed.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── GET /users/freelancers ────────────────────────────────────────────────────
router.get('/freelancers', async (req, res) => {
  if (req.user.role !== 'Client') {
    return res.status(403).json({ error: 'Only clients can browse freelancers.' });
  }

  try {
    const freelancers = await User.find({ role: 'Freelancer' })
      .select('name email skills bio photoUrl experience education phoneNumber contactInformation resumeUrl portfolioUrl githubUrl linkedinUrl')
      .sort({ name: 1 });

    res.json(freelancers);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.put('/me/files/:kind', express.raw({ type: 'application/octet-stream', limit: '10mb' }), async (req, res) => {
  const field = profileFileFields[req.params.kind];
  if (!field) return res.status(400).json({ error: 'File type must be resume or portfolio.' });
  if (req.user.role !== 'Freelancer') {
    return res.status(403).json({ error: 'Only freelancers can upload profile files.' });
  }
  if (!Buffer.isBuffer(req.body) || req.body.length === 0) {
    return res.status(400).json({ error: 'A non-empty file is required.' });
  }

  const extension = String(req.get('X-File-Extension') || '').toLowerCase();
  if (!allowedFileExtensions.has(extension)) {
    return res.status(400).json({ error: 'Supported files are PDF, DOC, DOCX, JPG, and PNG.' });
  }

  try {
    const user = await User.findById(req.user._id).select(field);
    if (!user) return res.status(404).json({ error: 'User not found.' });

    const bucket = getProfileFilesBucket();
    const fileId = new mongoose.mongo.ObjectId();
    const filename = `${req.params.kind}${extension}`;
    const upload = bucket.openUploadStreamWithId(fileId, filename, {
      metadata: {
        userId: req.user._id.toString(),
        kind: req.params.kind,
        extension,
      },
    });
    await new Promise((resolve, reject) => {
      upload.once('error', reject);
      upload.once('finish', resolve);
      upload.end(req.body);
    });

    const previousFilename = user[field];
    user[field] = fileId.toHexString();
    try {
      await user.save();
    } catch (err) {
      await bucket.delete(fileId).catch(() => {});
      throw err;
    }

    if (mongoose.mongo.ObjectId.isValid(previousFilename)) {
      await bucket.delete(new mongoose.mongo.ObjectId(previousFilename)).catch(() => {});
    } else if (previousFilename && path.basename(previousFilename) === previousFilename) {
      await fs.unlink(path.join(profileFilesDirectory, req.user._id.toString(), previousFilename)).catch(() => {});
    }

    res.json({ message: 'Profile file uploaded.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.get('/:id/files/:kind', async (req, res) => {
  const field = profileFileFields[req.params.kind];
  if (!field) return res.status(400).json({ error: 'File type must be resume or portfolio.' });

  try {
    const user = await User.findById(req.params.id).select(`role ${field}`);
    if (!user) return res.status(404).json({ error: 'User not found.' });

    const isOwner = req.user._id.toString() === user._id.toString();
    const isClientViewingFreelancer = req.user.role === 'Client' && user.role === 'Freelancer';
    if (!isOwner && !isClientViewingFreelancer) {
      return res.status(403).json({ error: 'You are not allowed to view this profile file.' });
    }

    const fileIdValue = user[field];
    if (!fileIdValue) {
      return res.status(404).json({ error: 'This file is unavailable. Ask the freelancer to upload it again.' });
    }

    if (mongoose.mongo.ObjectId.isValid(fileIdValue) && fileIdValue.length === 24) {
      const fileId = new mongoose.mongo.ObjectId(fileIdValue);
      const file = await User.db.db.collection(`${profileFilesBucketName}.files`).findOne({
        _id: fileId,
        'metadata.userId': user._id.toString(),
        'metadata.kind': req.params.kind,
      });
      if (!file) return res.status(404).json({ error: 'Profile file not found.' });

      const extension = file.metadata?.extension || path.extname(file.filename);
      res.type(extension);
      res.set('Content-Disposition', `inline; filename="${req.params.kind}${extension}"`);
      const download = getProfileFilesBucket().openDownloadStream(fileId);
      download.on('error', (err) => {
        if (!res.headersSent) res.status(404).json({ error: 'Profile file not found.' });
        else res.destroy(err);
      });
      return download.pipe(res);
    }

    if (path.basename(fileIdValue) !== fileIdValue) {
      return res.status(404).json({ error: 'Profile file not found.' });
    }
    const filePath = path.join(profileFilesDirectory, user._id.toString(), fileIdValue);
    try {
      await fs.access(filePath);
    } catch (_err) {
      return res.status(404).json({ error: 'Profile file not found.' });
    }

    res.type(path.extname(fileIdValue));
    res.set('Content-Disposition', `inline; filename="${req.params.kind}${path.extname(fileIdValue)}"`);
    res.sendFile(filePath);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── GET /users/me ─────────────────────────────────────────────────────────────
router.get('/me', async (req, res) => {
  try {
    res.json(req.user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── GET /users/:id ────────────────────────────────────────────────────────────
router.get('/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id).select('-password');
    if (!user) return res.status(404).json({ error: 'User not found.' });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── PUT /users/:id ────────────────────────────────────────────────────────────
router.put('/:id', async (req, res) => {
  try {
    if (req.params.id !== req.user._id.toString()) {
      return res.status(403).json({ error: 'You can only update your own profile.' });
    }

    // Prevent password updates through this route
    delete req.body.password;
    delete req.body.pushTokens;

    const updated = await User.findByIdAndUpdate(
      req.params.id,
      { $set: req.body },
      { new: true, runValidators: true }
    ).select('-password');

    if (!updated) return res.status(404).json({ error: 'User not found.' });

    res.json(updated);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
