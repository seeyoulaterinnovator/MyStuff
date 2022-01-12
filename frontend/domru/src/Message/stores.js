import {writable} from 'svelte/store';

export const text = writable('');

export const show = writable(false);

export const isBadEmail = writable(false);

export const isBadPhone = writable(false);

export const isRegistration = writable(false);

export const isUpdateProfile = writable(false);

export const loginUrl = writable(document.getElementById('message-modal').getAttribute('data-login-url'));
