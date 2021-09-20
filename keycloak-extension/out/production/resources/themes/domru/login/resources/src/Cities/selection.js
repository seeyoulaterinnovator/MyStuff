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
  const selectedDomain = selectedCity.city;
  const selectedCityName = selectedCity.name;
  if (selectedDomain && !selectedCity.bss) {
      window.location = `https://lkb2b.dom.ru/login?citydomain=${selectedDomain}`;
  }
  setSelectedCity(selectedCityName, selectedDomain);
  setAllSelected();
}
