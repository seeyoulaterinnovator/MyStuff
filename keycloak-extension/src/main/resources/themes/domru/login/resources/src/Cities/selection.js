import Cookie from 'js-cookie';

import {
  status,
  city,
  showModal,
  editingStarted,
  allCities,
} from './stores.js';
import { STATUS } from './constants.js';

export function setAllSelected() {
  status.set(STATUS.CONFIRMED);
  showModal.set(false);
  editingStarted.set(false);
}

export function setSelectedCity(selectedCity, selectedDomain) {
  city.set(selectedCity);
  Cookie.set('CITY', selectedCity);
  Cookie.set('city-domain', selectedDomain);
}

export function selectCity(selectedCity) {
  const selectedDomain = selectedCity.domain;
  const selectedCityName = selectedCity.name;
  if (selectedDomain) {
    if (selectedCity.bss) {
      window.open(`https://lkb2b.domru.ru/login?citydomain=${selectedDomain}`);
    } else {
      window.location = `https://lkb2b.domru.ru/login?citydomain=${selectedDomain}`;
    }
  } else {
    setSelectedCity(selectedCityName, selectedDomain)
  }
}
