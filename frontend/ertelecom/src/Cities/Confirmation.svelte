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
