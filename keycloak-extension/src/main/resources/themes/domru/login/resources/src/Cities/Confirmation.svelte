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
  <p class="text-center">Вы находитесь в г. {$city}?</p>
  <div class="flex mt-4">
    <button class="btn btn-main mr-4" on:click={handleConfirm}>Да</button>
    <button class="btn disabled" on:click={handleReject}>Выбрать другой</button>
  </div>
</div>
