<script>
  import Cookie from 'js-cookie';
  import { status, city, showModal } from './stores.js';
  import { STATUS } from './constants.js';

  function handleConfirm() {
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    showModal.set(false);
    status.set(STATUS.CONFIRMED);
  }

  function handleReject() {
    Cookie.set('VISITED', '1', {sameSite: 'None', secure: document.location.protocol === 'https:'});
    status.set(STATUS.SELECTING);
  }
</script>

<div>
  <p class="confirm__title">Вы находитесь в г. {$city}?</p>
  <div class="flex confirm__btns">
    <button class="btn btn-main confirm__btn" on:click={handleConfirm}>Да</button>
    <button class="btn disabled confirm__btn" on:click={handleReject}>Выбрать другой</button>
  </div>
</div>
