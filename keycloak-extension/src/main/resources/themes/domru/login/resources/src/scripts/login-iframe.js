export default (function() {
  function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    var results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
  };

  const isIframe = getUrlParameter('iframe') === '1';

  if (isIframe) {
    document.getElementById('page-header').style.display = 'none';
    document.getElementById('content').style.padding = '0';
  }
})();

