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
  Cookie.set('CITY', selectedCity, {sameSite: 'None', secure: document.location.protocol === 'https:'});
  Cookie.set('city-domain', selectedDomain, {sameSite: 'None', secure: document.location.protocol === 'https:'});
}
// hardcode https://lkb2b.dom.ru/login
export function selectCity(selectedCity) {
  const selectedDomain = selectedCity.city;
  console.log("selectedDomain = " + selectedDomain);
  const selectedCityName = selectedCity.name;
  console.log("selectedCityName = " + selectedCityName);
  if (selectedDomain && !selectedCity.bss) {
    console.log("!! window.location = `https://lkb2b.dom.ru/login?citydomain=${selectedDomain} ");
      window.location = `https://lkb2b.dom.ru/login?citydomain=${selectedDomain}`;
  }
  setSelectedCity(selectedCityName, selectedDomain);
  setAllSelected();
}
