require('dotenv').config();

const express  = require('express');
const mongoose = require('mongoose');
const cors     = require('cors');

const authRoutes         = require('./routes/auth');
const userRoutes         = require('./routes/users');
const jobRoutes          = require('./routes/jobs');
const applicationRoutes  = require('./routes/applications');
const messageRoutes      = require('./routes/messages');
const notificationRoutes = require('./routes/notifications');

const app = express();

// ── Middleware ────────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json());

// ── Routes ────────────────────────────────────────────────────────────────────
app.use('/auth',          authRoutes);
app.use('/users',         userRoutes);
app.use('/jobs',          jobRoutes);
app.use('/applications',  applicationRoutes);
app.use('/messages',      messageRoutes);
app.use('/notifications', notificationRoutes);

// ── Health Check ──────────────────────────────────────────────────────────────
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ── 404 Handler ───────────────────────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// ── Global Error Handler ──────────────────────────────────────────────────────
app.use((err, req, res, next) => {
  console.error('[ERROR]', err.message);
  res.status(err.status || 500).json({ error: err.message || 'Internal Server Error' });
});

// ── Database + Server Start ───────────────────────────────────────────────────
const PORT     = process.env.PORT     || 3000;
const HOST     = process.env.HOST     || '0.0.0.0';
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/freelancerconnect';

mongoose
  .connect(MONGO_URI)
  .then(() => {
    console.log('✅ MongoDB connected →', MONGO_URI);
    app.listen(PORT, HOST, () => {
      console.log(`🚀 FreelancerConnect API running on http://localhost:${PORT}`);
      console.log(`🌐 Network access enabled on http://${HOST}:${PORT}`);
    });
  })
  .catch((err) => {
    console.error('❌ MongoDB connection failed:', err.message);
    process.exit(1);
  });
