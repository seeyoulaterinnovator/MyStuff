import {writable} from 'svelte/store';
import Cookie from 'js-cookie';

import {STATUS} from './constants.js';

export const city = writable(
  (document.getElementById('cities-button') && document.getElementById('cities-button').dataset.city) || Cookie.get('CITY') || ''
);

export const domain = writable(
  Cookie.get('city-domain') || 'yar');

const isFirstVisit = Cookie.get('VISITED') !== '1' || (document.getElementById('withCity') && Cookie.get('changeCity') === undefined);

isFirstVisit && fetch('/auth/realms/user/cities/current')
  .then(response => response.json())
  .then(json => {
    json.results?.title == null ? city.set("Москва") : city.set(json.results.title);
  })
  .catch(error => {
    console.log(error);
    city.set('Москва');
  });

export const status = writable(
  isFirstVisit ? STATUS.INITIAL : STATUS.SELECTING,
);

export const showModal = writable(isFirstVisit);
export const editingStarted = writable(false);
export const allCities = writable([]);

export const quarter = writable(0);
