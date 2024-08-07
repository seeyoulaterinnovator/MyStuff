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
    import PhoneButton from './PhoneButton.svelte';

    import './selection';
    import {selectCity, setAllSelected, setSelectedCity} from "./selection";
    import CloseButton from "../Common/Buttons/CloseButton.svelte";
    import SearchIcon from "../Common/Icons/SearchIcon.svelte";

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
    console.log($status);
</script>
{#if $showModal}
    <div
            class="flex flex-col fixed transparent-bg w-screen bg-white inset-0 py-4 md:py-6 xl:py-8 {$status === STATUS.INITIAL && 'opacity-90'} scrollable-container overflow-x-hidden overflow-y-auto"
            id="location-selection-window" style={$status === STATUS.SELECTING ? "background-color: white" : ''}>
        <header id="cities-header" class="flex items-center pb-4 px-4 sm:px-6 lg:px-8 xl:px-6">
            <div id="cities-header-div" class="w-full">
              {#if $status === STATUS.SELECTING}
                <div class="w-full">
                  <div class="flex justify-between items-center custom-mb-md">
                    <label for="search-city" class="choose-city-text">Выбрать город</label>
                    <CloseButton on:click={handleClose} class="custom-icon"/>
                  </div>
                  <form on:submit|preventDefault={handleSelectCity} class="flex items-center search-city-input field__input">
                    <input
                      name="Поиск города"
                      id="search-city"
                      placeholder="Название города"
                      bind:value={search}
                      on:input={handleInputChange} />
                    <SearchIcon/>
                  </form>
                </div>
              {/if}

              {#if $status === STATUS.INITIAL}
                  <PhoneButton />
              {/if}
            </div>
        </header>

      {#if $status === STATUS.INITIAL}
          <div class="flex flex-1 justify-center content-box select-city-confirmation flex-col">
              <Confirmation />
          </div>
      {:else if $status === STATUS.SELECTING}
          <div class="flex flex-1 justify-center content-box h-full mt-4 xl:mt-17 scrollable-container overflow-x-hidden overflow-y-auto custom-scroll">
              <Selection {search} />
          </div>
      {/if}
    </div>
{/if}
