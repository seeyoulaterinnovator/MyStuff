<script>
  import { city, showModal, status } from './stores.js';
  import { STATUS } from './constants.js';
  import LocationIcon from '../Common/Icons/LocationIcon.svelte';

  function handleClick() {
    if ($status === STATUS.CONFIRMED) {
      status.set(STATUS.SELECTING);
      showModal.set(true);
    } else showModal.set(true);
  }
</script>

<button
  class="
    flex
    focus:outline-none
    items-center
    highlighted-hover-text
    highlighted-nested-hover-svg"
  id="show-cities"
  on:click={handleClick}
  title="Выбор города"
>
  <LocationIcon />
  <span
    class="border-dashed border-main-600 h-6 leading-normal"
    style="margin-left: 7px;"
  >
    {$city}
  </span>
</button>
