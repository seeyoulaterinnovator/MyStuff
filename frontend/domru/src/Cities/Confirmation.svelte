<script>
  import Cookie from 'js-cookie';
  import {allCities, city, showModal, status} from './stores.js';
  import {STATUS, DEFAULT_CITY} from './constants.js';
  import {fetchCities} from '../api/cities';
  import {onMount, onDestroy} from 'svelte';

  let unsubscribeCity = null;

  const setDefaultCity = () => {
    Cookie.set('CITY', DEFAULT_CITY.name, {sameSite: 'None', secure: document.location.protocol === 'https:'});
    city.set(DEFAULT_CITY.name);
  }

  onMount(() => {
    fetchCities().then((response) => {
      const fetchedCities = response.data.results?.cities;
      allCities.set(fetchedCities || []);

      unsubscribeCity = city.subscribe(value => {
        if (!$allCities.length) {
          setDefaultCity();
        }
      });
    }).catch(error => {
      setDefaultCity();
    });
  });

  onDestroy(unsubscribeCity);

  function handleConfirm() {
    const cityDomain = $allCities.find(obj => obj.name === $city)?.city || DEFAULT_CITY.city;   
    Cookie.set('city-domain', cityDomain, {sameSite: 'None', secure: document.location.protocol === 'https:'});
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    Cookie.set('changeCity', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    showModal.set(false);
    status.set(STATUS.CONFIRMED);
  }

  function handleReject() {
    console.log(allCities)
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    Cookie.set('changeCity', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    status.set(STATUS.SELECTING);
  }
</script>

<div>
  <p class="confirm__title">Вы находитесь в г. {$city}?</p>
  <div class="flex confirm__btns">
    <button class="btn btn-main mr-8 confirm__btn" on:click={handleConfirm}>Да</button>
    <button class="btn disabled confirm__btn" on:click={handleReject}><p style="color: #16629A;">Выбрать другой</p></button>
  </div>
</div>
