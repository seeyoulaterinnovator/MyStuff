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
  <svg width="12" height="16" viewBox="0 0 12 16" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path fill-rule="evenodd" clip-rule="evenodd" d="M10.1759 9.05816C10.6963 8.06091 11 7.09159 11 6.20764C11 3.69402 9.13471 1.5 6 1.5C2.86529 1.5 1 3.69402 1 6.20764C1 7.09159 1.30367 8.06091 1.82406 9.05816C2.34117 10.0492 3.04232 11.0107 3.76319 11.8621C4.48167 12.7106 5.20304 13.4303 5.74585 13.9384C5.83595 14.0227 5.92095 14.101 6 14.173C6.07905 14.101 6.16405 14.0227 6.25415 13.9384C6.79696 13.4303 7.51833 12.7106 8.23681 11.8621C8.95767 11.0107 9.65882 10.0492 10.1759 9.05816ZM6 15.5C6 15.5 12 10.6194 12 6.20764C12 3.14706 9.69231 0.5 6 0.5C2.30769 0.5 0 3.14706 0 6.20764C0 10.6194 6 15.5 6 15.5Z" fill="#222222"/>
    <path fill-rule="evenodd" clip-rule="evenodd" d="M6 6.5C6.55228 6.5 7 6.05228 7 5.5C7 4.94772 6.55228 4.5 6 4.5C5.44772 4.5 5 4.94772 5 5.5C5 6.05228 5.44772 6.5 6 6.5ZM6 7.5C7.10457 7.5 8 6.60457 8 5.5C8 4.39543 7.10457 3.5 6 3.5C4.89543 3.5 4 4.39543 4 5.5C4 6.60457 4.89543 7.5 6 7.5Z" fill="#222222"/>
  </svg>
  <span class="border-dashed border-main-600 h-6 leading-normal" style="margin-left: 7px;">
    {$city}
  </span>
</button>
