import { api } from '../base';
import { mapResponse } from '../base/mappers';
import { mapCitiesResults } from './mappers';

export const fetchCities = () => {
  return api
    .get('/cities')
    .then(response => mapResponse(response, mapCitiesResults))
    .catch(error => {
      console.error('Error:', error);
    });
};

export const fetchCurrentCity = () => {
  return api.get('/cities/current');
};
