import App from './App.svelte';

import './scripts/login.js';
import './scripts/register.js';
import './scripts/login-reset-password.js';
import './scripts/login-update-password.js';
import './scripts/totp.js';

import './css/tailwind-base.css';
import './css/tailwind-advanced.css';
import './css/fonts.css';
import './css/typography.css';
import './css/animation.css';
import './css/custom.css';
import './css/sth-went-wrong.css';

import './css/components/button.css';
import './css/components/field.css';
import './css/components/link.css';
import './css/components/logo.css';
import './css/components/reference.css';
import './css/components/email-sent.css';
import './css/components/cities.css';
import './css/components/confirmation.css';

const app = new App({
  target: document.body,
});

export default app;
