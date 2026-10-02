const express     = require('express');
const Application = require('../models/Application');
const Job         = require('../models/Job');
const Notification = require('../models/Notification');
const { protect } = require('../middleware/auth');
const { sendPushToUser } = require('../services/pushNotifications');

const router = express.Router();

router.use(protect);

// ── GET /applications ─────────────────────────────────────────────────────────
// Returns applications relevant to the current user (as freelancer or client)
router.get('/', async (req, res) => {
  try {
    const query = req.user.role === 'Freelancer'
      ? { freelancerId: req.user._id }
      : { clientId:     req.user._id };

    const applications = await Application.find(query)
      .populate({
        path: 'jobId',
        select: 'title budget duration clientId',
        populate: { path: 'clientId', select: 'name email phoneNumber contactInformation' },
      })
      .populate('freelancerId', 'name email skills bio experience education phoneNumber contactInformation githubUrl linkedinUrl portfolioUrl')
      .sort({ createdAt: -1 });

    res.json(applications);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── POST /applications ────────────────────────────────────────────────────────
router.post('/', async (req, res) => {
  try {
    const { jobId, coverLetter } = req.body;

    if (req.user.role !== 'Freelancer') {
      return res.status(403).json({ error: 'Only freelancers can apply to jobs.' });
    }

    if (!jobId) return res.status(400).json({ error: 'jobId is required.' });

    const job = await Job.findById(jobId);
    if (!job) return res.status(404).json({ error: 'Job not found.' });

    // Prevent duplicate applications
    const existing = await Application.findOne({
      jobId,
      freelancerId: req.user._id,
    });

    if (existing) {
      return res.status(409).json({ error: 'You have already applied to this job.' });
    }

    const application = await Application.create({
      jobId,
      freelancerId: req.user._id,
      clientId: job.clientId,
      coverLetter:  coverLetter || '',
    });

    const freelancerName = req.user.name || req.user.email;
    await Notification.create({
      userId: job.clientId,
      type: 'New application',
      message: `${freelancerName} applied for ${job.title}.`,
      relatedType: 'application',
      relatedId: application._id,
    });

    await application.populate([
      {
        path: 'jobId',
        select: 'title budget duration clientId',
        populate: { path: 'clientId', select: 'name email phoneNumber contactInformation' },
      },
      { path: 'freelancerId', select: 'name email skills bio experience education phoneNumber contactInformation githubUrl linkedinUrl portfolioUrl' },
    ]);

    sendPushToUser(job.clientId.toString(), {
      type: 'application',
      title: 'New job application',
      body: `${freelancerName} applied for ${job.title}`.slice(0, 180),
      applicationId: application._id.toString(),
      jobId: job._id.toString(),
    }).catch((err) => console.error('[FCM]', err.message));

    res.status(201).json(application);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

router.delete('/:id', async (req, res) => {
  try {
    const application = await Application.findById(req.params.id);
    if (!application) return res.status(404).json({ error: 'Application not found.' });
    if (application.freelancerId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Only the applicant can withdraw this application.' });
    }
    if (application.status !== 'Pending') {
      return res.status(409).json({ error: 'Only pending applications can be withdrawn.' });
    }

    await application.deleteOne();
    await Notification.deleteMany({
      relatedType: 'application',
      relatedId: application._id,
    });
    res.json({ message: 'Application withdrawn.' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── PUT /applications/:id/status ──────────────────────────────────────────────
router.put('/:id/status', async (req, res) => {
  try {
    const { status } = req.body;
    const validStatuses = ['Pending', 'Accepted', 'Rejected'];

    if (!validStatuses.includes(status)) {
      return res.status(400).json({ error: `Status must be one of: ${validStatuses.join(', ')}` });
    }

    const application = await Application.findById(req.params.id);
    if (!application) return res.status(404).json({ error: 'Application not found.' });

    if (application.clientId.toString() !== req.user._id.toString()) {
      return res.status(403).json({ error: 'Only the client can update application status.' });
    }

    application.status = status;
    await application.save();

    Notification.create({
      userId: application.freelancerId,
      type: 'Application update',
      message: `Your application status changed to ${status}.`,
      relatedType: 'application',
      relatedId: application._id,
    }).catch((err) => console.error('[NOTIFICATION]', err.message));

    res.json({ message: `Application ${status.toLowerCase()}.` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
