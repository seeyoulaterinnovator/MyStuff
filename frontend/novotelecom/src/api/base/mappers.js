export const mapResponse = (response, mapper) => {
  const { data } = response || {};
  const { results } = data || {};

  return {
    ...response,
    data: {
      ...data,
      results: mapper?.(results) || results,
    },
  };
};
