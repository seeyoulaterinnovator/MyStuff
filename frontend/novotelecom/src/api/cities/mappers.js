export const mapCity = city => {
  return city.name === 'Холдинг'
    ? {
        ...city,
        name: 'Федеральный Клиент',
      }
    : city;
};

export const mapCities = cities => {
  return cities.map(mapCity);
};

export const mapCitiesResults = results => {
  return {
    cities: mapCities(results?.cities),
  };
};
