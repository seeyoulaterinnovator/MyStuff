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
    quarter,
  } from './stores.js';
  import { STATUS } from './constants.js';

  import * as citiesJson  from '../mock/cities.json'

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
    let quarterStore = 0;
    const unsubscribe = quarter.subscribe(value => {
      quarterStore = value;
    });
    if (quarterStore  < 1) quarter.set(Math.floor(arr.length / 4));
    let partCounter = 0;
    let currentQuarter = quarterStore;

    const groupedCitiesObject = arr.reduce((acc, value, index) => {
      let firstCharacter = value.name[0];

      if (!acc[partCounter]) acc.push([]);
      if (!acc[partCounter][firstCharacter]) {

        if ( index >= currentQuarter && partCounter < 3) {
          partCounter++;
          // acc.push([]);
          currentQuarter = currentQuarter + quarterStore;
        }
        if (!acc[partCounter]) acc.push([]);
        acc[partCounter][firstCharacter] = { firstCharacter, cities: [value.name] };
      }
      else {
        acc[partCounter][firstCharacter].cities.push(value.name);
      }

      return acc;
    }, []);

    console.log(Object.values(groupedCitiesObject.map(part => Object.values(part))));
    return Object.values(groupedCitiesObject.map(part => Object.values(part)));
  }

  onMount(() => {
    const url = '/auth/realms/user/cities';

    axios
      .get(url)
      .then(response => {
        allCities.set(citiesJson.results.cities || []);

        groupedCities = groupByFirstCharacter($allCities);
      })
      .catch(error => console.error('Error:', error));
  });

  afterUpdate(() => {
    const displayingCities = $editingStarted
      ? $allCities.filter(cityObject => cityObject.name.toLowerCase().startsWith(search.toLowerCase()))
      : $allCities;
    groupedCities = groupByFirstCharacter(displayingCities);
  });
</script>

<ul class="flex flex-wrap flex-row cities-container w-full
  scrollable-container overflow-x-hidden overflow-y-auto">
    {#each groupedCities as groupPart}
      <ul class="flex flex-col cities-column">
      {#each groupPart as group}

        <ul class="flex flex-row mb-4 px-2 capital">
          <h2 class="text-extra mr-2 capitalize text-center leading-none w-5 ">
            {group.firstCharacter}
          </h2>

          <ul class="flex flex-col">
          {#each group.cities as city}
            <li class="mb-2 sm:px-2 hover:bg-extra city">
              <button class="city" on:click={() => handleClick(city)}>{city}</button>
            </li>
          {:else}
            <div />
          {/each}
          </ul>
        </ul>

      {/each}
      </ul>
    {:else}
      <p class="m-auto">Нет заданного города</p>
    {/each}
</ul>
