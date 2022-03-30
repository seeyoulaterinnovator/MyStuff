<script>
  import Cookie from 'js-cookie';
  import { status, city, showModal, allCities } from './stores.js';
  import { STATUS } from './constants.js';

  function handleConfirm() {
    console.log($allCities)
    const cityDomain = $allCities.find(obj => obj.name === $city).city;
    Cookie.set('city-domain', cityDomain, {sameSite: 'None', secure: document.location.protocol === 'https:'});
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    showModal.set(false);
    status.set(STATUS.CONFIRMED);
  }

  function handleReject() {
    console.log(allCities)
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    status.set(STATUS.SELECTING);
  }
</script>

<div>
  <p class="text-center confirm__title">Вы находитесь в г. {$city}?</p>
  <div class="flex confirm__btns">
    <button class="btn btn-main mr-8 confirm__btn" on:click={handleConfirm}>Да, верно</button>
    <button class="btn disabled confirm__btn" on:click={handleReject}>Выбрать другой</button>
  </div>
</div>
