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
  <svg width="19" height="19" viewBox="0 0 19 19" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path fill-rule="evenodd" clip-rule="evenodd" d="M10.1759 10.5582C10.6963 9.56091 11 8.59159 11 7.70764C11 5.19402 9.13471 3 6 3C2.86529 3 1 5.19402 1 7.70764C1 8.59159 1.30367 9.56091 1.82406 10.5582C2.34117 11.5492 3.04232 12.5107 3.76319 13.3621C4.48167 14.2106 5.20304 14.9303 5.74585 15.4384C5.83595 15.5227 5.92095 15.601 6 15.673C6.07905 15.601 6.16405 15.5227 6.25415 15.4384C6.79696 14.9303 7.51833 14.2106 8.23681 13.3621C8.95767 12.5107 9.65882 11.5492 10.1759 10.5582ZM6 17C6 17 12 12.1194 12 7.70764C12 4.64706 9.69231 2 6 2C2.30769 2 0 4.64706 0 7.70764C0 12.1194 6 17 6 17Z" fill="black"/>
    <path fill-rule="evenodd" clip-rule="evenodd" d="M6 8C6.55228 8 7 7.55228 7 7C7 6.44772 6.55228 6 6 6C5.44772 6 5 6.44772 5 7C5 7.55228 5.44772 8 6 8ZM6 9C7.10457 9 8 8.10457 8 7C8 5.89543 7.10457 5 6 5C4.89543 5 4 5.89543 4 7C4 8.10457 4.89543 9 6 9Z" fill="black"/>
  </svg>
  <span class="border-dashed border-main-600 h-6 leading-normal">
    {$city}
  </span>
</button>
