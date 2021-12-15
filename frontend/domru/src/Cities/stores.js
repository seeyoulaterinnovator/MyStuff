import { writable } from 'svelte/store';
import Cookie from 'js-cookie';

import { STATUS } from './constants.js';

export const city = writable(
  document.getElementById('cities-button').dataset.city ||
    Cookie.get('CITY') ||
    'Ярославль',
);

export const domain = writable(
  Cookie.get('city-domain') || 'yar');

const isFirstVisit =
  Cookie.get('VISITED') === '0' || typeof Cookie.get('VISITED') === 'undefined';

export const status = writable(
  isFirstVisit ? STATUS.INITIAL : STATUS.SELECTING,
);
export const showModal = writable(isFirstVisit);
export const editingStarted = writable(false);
export const allCities = writable([]);

export const quarter = writable(0);
