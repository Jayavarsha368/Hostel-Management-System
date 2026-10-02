const mongoose = require('mongoose');
const bcrypt   = require('bcryptjs');

const UserSchema = new mongoose.Schema(
  {
    name:               { type: String, default: '' },
    email:              { type: String, required: true, unique: true, lowercase: true, trim: true },
    password:           { type: String, required: true, select: false },
    role:               { type: String, enum: ['Freelancer', 'Client'], default: 'Freelancer' },
    skills:             { type: [String], default: [] },
    bio:                { type: String, default: '' },
    photoUrl:           { type: String, default: '' },
    companyName:        { type: String, default: '' },
    companyDescription: { type: String, default: '' },
    industry:           { type: String, default: '' },
    phoneNumber:        { type: String, default: '' },
    contactInformation: { type: String, default: '' },
    experience:         { type: String, default: '' },
    education:          { type: String, default: '' },
    resumeUrl:          { type: String, default: '' },
    portfolioUrl:       { type: String, default: '' },
    githubUrl:          { type: String, default: '' },
    linkedinUrl:        { type: String, default: '' },
    pushTokens:          { type: [String], default: [], select: false },
  },
  { timestamps: true }
);

// Hash password before saving
UserSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

// Compare password helper
UserSchema.methods.comparePassword = async function (candidate) {
  return bcrypt.compare(candidate, this.password);
};

module.exports = mongoose.model('User', UserSchema);
