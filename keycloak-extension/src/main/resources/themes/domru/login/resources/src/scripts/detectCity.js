import Cookie from 'js-cookie';

export default (function() {
  let detectedCity = '';
  if (location.search.indexOf('city') > 0) {
    let params = [];
    decodeURI(location.search)
      .substring(1)
      .split("&")
      .forEach( item => {
        params = item.split("=");
        if (params[0] === 'city') {
          detectedCity = params[1];
        }
        if (params[0] === 'redirect_uri') {
          let subComponent = decodeURIComponent(params[1]);
          if (subComponent.indexOf('city')) {
            detectedCity = subComponent.split("city=")[1];
          }
        }
      });
  }
  Cookie.set('CITY', detectedCity)
})();
