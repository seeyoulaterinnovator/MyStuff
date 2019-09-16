<script>
  import Cookie from 'js-cookie';
  import { status, city, showModal } from './stores.js';
  import { STATUS } from './constants.js';

  console.log('Confirmation');

  function handleConfirm() {
    Cookie.set('VISITED', '1');
    showModal.set(false);
    status.set(STATUS.CONFIRMED);
  }

  function handleReject() {
    Cookie.set('VISITED', '1');
    status.set(STATUS.SELECTING);
  }
</script>

<div>
  <p class="text-center confirm__title">Вы находитесь в г. {$city}?</p>
  <div class="flex mt-4 confirm__btns">
    <button class="btn btn-main mr-8 confirm__btn" on:click={handleConfirm}>Да, верно</button>
    <button class="btn disabled confirm__btn" on:click={handleReject}>Выбрать другой</button>
  </div>
</div>
