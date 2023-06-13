<script>
  import Cookie from 'js-cookie';
  import {allCities, city, showModal, status} from './stores.js';
  import {STATUS} from './constants.js';
  import {onMount} from 'svelte';
  import axios from 'axios';

  onMount(() => {
    const url = '/auth/realms/user/cities';

    axios
      .get(url)
      .then(response => {
        const respCities = response.data.results.cities || []; //citiesJson.results.cities || [];
        const replacedCities = respCities.map(city => city.name === 'Холдинг' ? {
          ...city,
          name: 'Федеральный Клиент',
        } : city);
        allCities.set(replacedCities);
      })
      .catch(error => console.error('Error:', error));
  });

  function handleConfirm() {
    console.log($allCities)
    const cityDomain = $allCities.find(obj => obj.name === $city).city;
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
