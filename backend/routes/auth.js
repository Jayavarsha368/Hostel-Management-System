const express = require('express');
const jwt     = require('jsonwebtoken');
const crypto  = require('crypto');
const { OAuth2Client } = require('google-auth-library');
const User    = require('../models/User');

const router = express.Router();
const googleClient = new OAuth2Client();

/** Generate a signed JWT for a user ID */
const signToken = (id) =>
  jwt.sign({ id }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
  });

// ── POST /auth/register ───────────────────────────────────────────────────────
router.post('/register', async (req, res) => {
  try {
    const { email, password, name } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required.' });
    }

    if (password.length < 6) {
      return res.status(400).json({ error: 'Password must be at least 6 characters.' });
    }

    const existing = await User.findOne({ email: email.toLowerCase() });
    if (existing) {
      return res.status(409).json({ error: 'An account with this email already exists.' });
    }

    const user  = await User.create({ email, password, name: name || '' });
    const token = signToken(user._id);

    res.status(201).json({
      token,
      user: {
        id:    user._id,
        email: user.email,
        role:  user.role,
        name:  user.name,
      },
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── POST /auth/login ──────────────────────────────────────────────────────────
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required.' });
    }

    const user = await User.findOne({ email: email.toLowerCase() }).select('+password');
    if (!user) {
      return res.status(401).json({ error: 'Invalid email or password.' });
    }

    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ error: 'Invalid email or password.' });
    }

    const token = signToken(user._id);

    res.json({
      token,
      user: {
        id:    user._id,
        email: user.email,
        role:  user.role,
        name:  user.name,
      },
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.post('/google', async (req, res) => {
  try {
    const clientId = process.env.GOOGLE_WEB_CLIENT_ID;
    const idToken = typeof req.body.idToken === 'string' ? req.body.idToken : '';
    const requestedRole = req.body.role;
    if (!clientId) {
      return res.status(503).json({ error: 'Google sign-in is not configured on the server.' });
    }
    if (!idToken) return res.status(400).json({ error: 'Google ID token is required.' });

    const ticket = await googleClient.verifyIdToken({ idToken, audience: clientId });
    const identity = ticket.getPayload();
    if (!identity?.email || !identity.email_verified) {
      return res.status(401).json({ error: 'Google did not provide a verified email address.' });
    }

    const email = identity.email.toLowerCase();
    let user = await User.findOne({ email });
    if (!user) {
      const role = ['Client', 'Freelancer'].includes(requestedRole) ? requestedRole : 'Freelancer';
      user = await User.create({
        email,
        name: identity.name || '',
        photoUrl: identity.picture || '',
        role,
        password: crypto.randomBytes(48).toString('hex'),
      });
    }

    res.json({
      token: signToken(user._id),
      user: { id: user._id, email: user.email, role: user.role, name: user.name },
    });
  } catch (err) {
    res.status(401).json({ error: 'Google sign-in could not be verified.' });
  }
});

// ── POST /auth/reset-password ─────────────────────────────────────────────────
router.post('/reset-password', async (req, res) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({ error: 'Email is required.' });
    }

    // Always respond with success to prevent email enumeration
    res.json({
      message: 'If an account with that email exists, a reset link has been sent.',
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
