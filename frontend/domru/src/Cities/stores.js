import { writable } from 'svelte/store';
import Cookie from 'js-cookie';

import { STATUS } from './constants.js';
import { fetchCurrentCity } from '../api/cities';

export const city = writable(
  (document.getElementById('cities-button') && document.getElementById('cities-button').dataset.city) || Cookie.get('CITY') || ''
);

if (document.getElementById('showModalIframe') && document.getElementById('showModalIframe').value === 'TRUE'){
  Cookie.set('VISITED', '0', {sameSite: 'None', secure: document.location.protocol === 'https:'});
  document.getElementById('showModalIframe').value = 'FALSE';
}

export const domain = writable(
  Cookie.get('city-domain') || 'yar');

const isFirstVisit = Cookie.get('VISITED') !== '1';

isFirstVisit &&
  fetchCurrentCity()
    .then(response => {
      const responseResults = response.data.results;
      const { title: currentCityTitle } = responseResults || {};

      currentCityTitle == null
        ? city.set(DEFAULT_CITY.name)
        : city.set(currentCityTitle);
    })
    .catch(error => {
      console.log(error);
      city.set(DEFAULT_CITY.name);
    });

export const status = writable(
  isFirstVisit ? STATUS.INITIAL : STATUS.SELECTING,
);

export const showModal = writable(isFirstVisit);
export const editingStarted = writable(false);
export const allCities = writable([]);

export const quarter = writable(0);
