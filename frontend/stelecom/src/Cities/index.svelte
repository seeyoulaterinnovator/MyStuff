<script>
    import Cookie from 'js-cookie';

    import {
        status,
        city,
        showModal,
        editingStarted,
        allCities,
    } from './stores.js';
    import { STATUS } from './constants.js';

    import Confirmation from './Confirmation.svelte';
    import Selection from './Selection.svelte';

    import './selection';
    import {selectCity, setAllSelected, setSelectedCity} from "./selection";

    let search;

    const unsubscribeCity = city.subscribe(value => {
        Cookie.set('CITY', value, {sameSite: 'None', secure: document.location.protocol === 'https:'});
        search = value;
    });

    function handleClose() {
        setAllSelected();
    }

    function handleInputChange() {
        editingStarted.set(true);
    }

    function handleSelectCity() {
        const indexOfChosenCity = $allCities.map(obj => obj.name.toLowerCase()).indexOf(search.toLowerCase());
        if (indexOfChosenCity === - 1) return;

        selectCity($allCities[indexOfChosenCity]);
    }
</script>

{#if $showModal}
    <div
            class="flex flex-col fixed transparent-bg w-screen inset-0 py-4 md:py-6 xl:py-8 scrollable-container overflow-x-hidden overflow-y-auto"
            id="location-selection-window">
        <header id="cities-header" class="flex items-center pb-4 px-4 sm:px-6 lg:px-8 xl:px-6">
            <div id="cities-header-div" class="w-full flex justify-between items-center">
                <a id="cities-header-logo" href="http://lkb2b.tcenter.ru/" class={$status === STATUS.SELECTING && 'hidden sm:block'}>
                    <div class="h-30px w-120px md:h-10 md:w-40 xl:h-16 xl:w-64 bg-contain bg-no-repeat logo logo--base" />
                </a>

              {#if $status === STATUS.SELECTING}
                  <form
                          class="md:flex md:flex-wrap md:justify-between"
                          on:submit|preventDefault={handleSelectCity}>
                      <fieldset>
                          <div class="field field--row md:w-full items-center">
                              <label for="search-city" class="mr-4 hidden lg:block">Текущий выбор:</label>
                              <input
                                      name="Поиск города"
                                      id="search-city"
                                      class="field__input field__input--city"
                                      placeholder="Выберите город"
                                      bind:value={search}
                                      on:input={handleInputChange} />
                              <button class="btn btn-main ml-4 p-3 min-w-0 btn__choose" type="submit">
                                  Выбрать
                              </button>
                          </div>
                      </fieldset>
                  </form>

                  <button id="close-cities" on:click={handleClose}>
                      <svg
                              width="32"
                              height="32"
                              viewBox="0 0 32 32"
                              fill="none"
                              xmlns="http://www.w3.org/2000/svg">
                          <g opacity="0.5">
                              <path
                                      fill-rule="evenodd"
                                      clip-rule="evenodd"
                                      d="M17.4142 16.0002L27.7072 5.70718L26.293 4.29297L16
                  14.5859L5.70718 4.29312L4.29297 5.70733L14.5858
                  16.0002L4.29297 26.293L5.70718 27.7072L16 17.4144L26.293
                  27.7073L27.7072 26.2931L17.4142 16.0002Z"
                                      fill="black" />
                          </g>
                      </svg>
                  </button>
              {/if}
            </div>
        </header>

      {#if $status === STATUS.INITIAL}
          <div class="flex flex-1 items-center justify-center content-box h-full mt-4 xl:mt-17 custom-scroll flex-col">
              <Confirmation />
          </div>
      {:else if $status === STATUS.SELECTING}
          <div class="flex flex-1 justify-center content-box h-full mt-4 xl:mt-17 scrollable-container overflow-x-hidden overflow-y-auto custom-scroll">
              <Selection {search} />
          </div>
      {/if}
    </div>
{/if}
