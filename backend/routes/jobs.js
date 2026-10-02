const express     = require('express');
const Job         = require('../models/Job');
const Application = require('../models/Application');
const Notification = require('../models/Notification');
const { protect } = require('../middleware/auth');

const router = express.Router();

router.use(protect);

// ── GET /jobs ─────────────────────────────────────────────────────────────────
router.get('/', async (req, res) => {
  try {
    const jobs = await Job.find()
      .populate('clientId', 'name email companyName phoneNumber contactInformation')
      .sort({ createdAt: -1 });
    res.json(jobs);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── GET /jobs/:id ─────────────────────────────────────────────────────────────
router.get('/:id', async (req, res) => {
  try {
    const job = await Job.findById(req.params.id)
      .populate('clientId', 'name email companyName phoneNumber contactInformation');
    if (!job) return res.status(404).json({ error: 'Job not found.' });
    res.json(job);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── POST /jobs ────────────────────────────────────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { title, description, budget, skillsRequired, duration, deadline } = req.body;

    if (!title || !description) {
      return res.status(400).json({ error: 'Title and description are required.' });
    }

    const job = await Job.create({
      title,
      description,
      budget:         budget         || '',
      skillsRequired: skillsRequired || [],
      duration:       duration       || '',
      deadline:       deadline       || '',
      clientId:       req.user._id,
    });

    await job.populate('clientId', 'name email companyName phoneNumber contactInformation');
    res.status(201).json(job);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── PUT /jobs/:id ─────────────────────────────────────────────────────────────
router.put('/:id', async (req, res) => {
  try {
    const job = await Job.findById(req.params.id);
    if (!job) return res.status(404).json({ error: 'Job not found.' });

    if (job.clientId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Only the job owner can update it.' });
    }

    const updated = await Job.findByIdAndUpdate(
      req.params.id,
      { $set: req.body },
      { new: true, runValidators: true }
    );

    res.json(updated);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── DELETE /jobs/:id ──────────────────────────────────────────────────────────
router.delete('/:id', async (req, res) => {
  try {
    const job = await Job.findById(req.params.id);
    if (!job) return res.status(404).json({ error: 'Job not found.' });

    if (job.clientId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Only the job owner can delete it.' });
    }

    const applications = await Application.find({ jobId: job._id }).select('_id');
    const applicationIds = applications.map((application) => application._id);
    await Application.deleteMany({ jobId: job._id });
    if (applicationIds.length > 0) {
      await Notification.deleteMany({
        relatedType: 'application',
        relatedId: { $in: applicationIds },
      });
    }
    await job.deleteOne();
    res.json({ message: 'Job deleted successfully.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
