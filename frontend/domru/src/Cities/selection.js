import Cookie from 'js-cookie';

import {
  status,
  city,
  showModal,
  editingStarted,
  allCities,
} from './stores.js';
import { STATUS } from './constants.js';
import {log} from "tailwindcss/lib/cli/utils";

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
  log.info("selectedDomain = " + selectedDomain);
  const selectedCityName = selectedCity.name;
  log.info("selectedCityName = " + selectedCityName);
  if (selectedDomain && !selectedCity.bss) {
    log.info("!! window.location = `https://lkb2b.dom.ru/login?citydomain=${selectedDomain} ");
      window.location = `https://lkb2b.dom.ru/login?citydomain=${selectedDomain}`;
  }
  setSelectedCity(selectedCityName, selectedDomain);
  setAllSelected();
}
