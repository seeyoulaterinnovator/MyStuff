<script>
  import { city, status, showModal } from './stores.js';
  import { STATUS } from './constants.js';

  function handleClick() {
    if ($status === STATUS.CONFIRMED) {
      status.set(STATUS.SELECTING);
      showModal.set(true);
    } else showModal.set(true);
  }
</script>

<button
  on:click={handleClick}
  class="flex focus:outline-none mr-10 h-6 items-center"
  title="Выбор города">
  <svg
    class="h-8 w-auto mr-1 outline-none city-icon"
    width="16"
    height="16"
    viewBox="0 0 16 16"
    fill="none"
    xmlns="http://www.w3.org/2000/svg">
    <g opacity="0.2">
      <path
        d="M12 7C12 8.66549 11.1612 10.1872 10.1317 11.331C9.62283 11.8964
        9.09196 12.341 8.64842 12.6367C8.42613 12.7849 8.23957 12.8865 8.10068
        12.9473C8.05723 12.9664 8.02393 12.979 8 12.9873C7.97607 12.979 7.94277
        12.9664 7.89932 12.9473C7.76043 12.8865 7.57387 12.7849 7.35158
        12.6367C6.90804 12.341 6.37717 11.8964 5.86829 11.331C4.83881 10.1872 4
        8.66549 4 7C4 4.79086 5.79086 3 8 3C10.2091 3 12 4.79086 12 7Z"
        stroke="black"
        stroke-width="2" />
      <circle cx="8" cy="7" r="1" fill="black" />
    </g>
  </svg>
  <span class="border-dashed border-b border-main-600 leading-normal">
    {$city}
  </span>
</button>
