const jwt  = require('jsonwebtoken');
const User = require('../models/User');

/**
 * Middleware to protect routes with JWT authentication.
 * Expects: Authorization: Bearer <token>
 */
const protect = async (req, res, next) => {
  try {
    const header = req.headers.authorization;

    if (!header || !header.startsWith('Bearer ')) {
      return res.status(401).json({ error: 'Not authorized. No token provided.' });
    }

    const token   = header.split(' ')[1];
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    req.user = await User.findById(decoded.id).select('-password');

    if (!req.user) {
      return res.status(401).json({ error: 'User no longer exists.' });
    }

    next();
  } catch (err) {
    res.status(401).json({ error: 'Invalid or expired token.' });
  }
};

module.exports = { protect };
