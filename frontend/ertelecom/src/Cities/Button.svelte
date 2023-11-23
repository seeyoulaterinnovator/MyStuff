<script>
  import {city, showModal, status} from './stores.js';
  import {STATUS} from './constants.js';

  function handleClick() {
    if ($status === STATUS.CONFIRMED) {
      status.set(STATUS.SELECTING);
      showModal.set(true);
    } else showModal.set(true);
  }
</script>
