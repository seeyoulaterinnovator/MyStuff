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

<button
  class="flex focus:outline-none h-6 items-center"
  id="show-cities"
  on:click={handleClick}
  title="Выбор города">
  <span class="border-dashed border-main-600 h-6 leading-normal text-extra hover:text-700-hover">
    {$city}
  </span>
</button>
