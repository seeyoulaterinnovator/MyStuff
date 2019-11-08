import Cookie from 'js-cookie';
import axios from 'axios';
import { showModal, city as citySvelte } from '../Cities/stores.js';

export default (function() {
  const url = '/auth/realms/user/cities';
  let detectedCity = '';
  if (location.search.indexOf('city') > 0) {
    let params = [];
    decodeURI(location.search)
      .substring(1)
      .split("&")
      .forEach( item => {
        params = item.split("=");
        if (params[0] === 'city') {
          detectedCity = params[1];
        }
        if (params[0] === 'redirect_uri') {
          let subComponent = decodeURIComponent(params[1]);
          if (subComponent.indexOf('city')) {
            detectedCity = subComponent.split("city=")[1];
          }
        }
      });
    Cookie.set('VISITED', '1');
    showModal.set(false);
    axios
      .get(url)
      .then(response => {
        const respCities  = response.data.results.cities || []; //citiesJson.results.cities || [];
        const replacedCities = respCities.map(city => city.name === 'Холдинг' ? {
          ...city,
          name: 'Федеральный Клиент',
        } : city);
        const cityName = replacedCities.filter(city => city.city === detectedCity)[0];
        if (cityName) {
          if (cityName.bss) {
            Cookie.set('CITY', cityName.name);
            Cookie.set('city-domain', detectedCity)
            citySvelte.set(cityName.name);
          } else {
            location.replace(`https://lkb2b.domru.ru/login?citydomain=${cityName.domain}`)
          }
        }
      })
  }
})();
