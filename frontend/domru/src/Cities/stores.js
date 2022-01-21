import {writable} from 'svelte/store';
import Cookie from 'js-cookie';

import {STATUS} from './constants.js';

export const city = writable(
  document.getElementById('cities-button').dataset.city || Cookie.get('CITY') || ''
);

export const domain = writable(
  Cookie.get('city-domain') || 'yar');

const isFirstVisit =
  Cookie.get('VISITED') === '0' || typeof Cookie.get('VISITED') === 'undefined';

isFirstVisit && fetch('/auth/realms/user/cities/current')
  .then(response => response.json())
  .then(json => {
    json.results?.title == null ? city.set("Москва") : city.set(json.results.title);
    showModal.set(isFirstVisit);
  })
  .catch(error => {
    console.log(error);
    city.set('Москва');
    showModal.set(isFirstVisit);
  });

export const status = writable(
  isFirstVisit ? STATUS.INITIAL : STATUS.SELECTING,
);
export const showModal = writable(false);
export const editingStarted = writable(false);
export const allCities = writable([]);

export const quarter = writable(0);
