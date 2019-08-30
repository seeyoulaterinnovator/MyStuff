<script>
  import { beforeUpdate, afterUpdate } from 'svelte';
  import { onMount } from 'svelte';
  import Cookie from 'js-cookie';

  import axios from 'axios';

  import {
    status,
    city,
    showModal,
    editingStarted,
    allCities,
  } from './stores.js';
  import { STATUS } from './constants.js';

  let groupedCities = [];

  export let search;

  function handleClick(currentCity) {
    city.set(currentCity);
    Cookie.set('CITY', currentCity);
    status.set(STATUS.CONFIRMED);
    showModal.set(false);
    editingStarted.set(false);
  }

  function groupByFirstCharacter(arr) {
    const groupedCitiesObject = arr.reduce((acc, value) => {
      let firstCharacter = value.name[0];

      if (!acc[firstCharacter])
        acc[firstCharacter] = { firstCharacter, cities: [value.name] };
      else acc[firstCharacter].cities.push(value.name);

      return acc;
    }, []);

    return Object.values(groupedCitiesObject);
  }

  onMount(() => {
    const url = '/auth/realms/user/cities';

    axios
      .get(url)
      .then(response => {
        allCities.set(response.data.results.cities || []);

        groupedCities = groupByFirstCharacter($allCities);
      })
      .catch(error => console.error('Error:', error));
  });

  afterUpdate(() => {
    const displayingCities = $editingStarted
      ? $allCities.filter(cityObject => cityObject.name.startsWith(search))
      : $allCities;
    groupedCities = groupByFirstCharacter(displayingCities);
  });
</script>

<ul
  class="flex md:flex-wrap flex-col overflow-x-hidden overflow-y-auto
  scrollable-container w-full h-full">
  {#each groupedCities as group}
    <li class="flex flex-col sm:flex-row mb-4 px-2">
      <h2 class="text-extra mr-2 capitalize text-center leading-none w-5">
        {group.firstCharacter}
      </h2>
      <ul class="w-full pt-1 sm:pt-0">
        {#each group.cities as city}
          <li class="mb-2 sm:px-2 hover:bg-extra">
            <button on:click={() => handleClick(city)}>{city}</button>
          </li>
        {:else}
          <div />
        {/each}
      </ul>
    </li>
  {:else}
    <p class="m-auto">Нет заданного города</p>
  {/each}
</ul>
