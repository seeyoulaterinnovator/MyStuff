import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { PASSWORD_CHARSET } from '../constants/passwordCharset.js';

function getRandomInt(min, max) {
  let byteArray = new Uint8Array(1);
  const crypto = window.crypto || window.msCrypto;
  crypto.getRandomValues(byteArray);

  const range = max - min + 1;
  const max_range = 256;
  if (byteArray[0] >= Math.floor(max_range / range) * range)
    return getRandomInt(min, max);
  return min + (byteArray[0] % range);
}

function generatePassword(length = 16, charset = PASSWORD_CHARSET) {
  let result = '';
  const keys = Object.keys(charset); // ["lowercase", "uppercase", "numbers", "extraChars"]

  let flags = new Array(keys.length).fill(0);
  const targetFlags = new Array(keys.length).fill(1); // [1, 1, 1, 1]

  while (!isEqual(flags, targetFlags)) {
    result = '';
    flags = new Array(keys.length).fill(0); // [0, 0, 0, 0]

    for (let i = 0; i < length; i++) {
      const charsetIndex = getRandomInt(0, flags.length - 1);
      const currentCharset = charset[keys[charsetIndex]];

      result += currentCharset[getRandomInt(0, currentCharset.length - 1)];
      flags[charsetIndex] = 1;
    }
  }

  return result;
}

export function fetchPassword() {
  const mock = new MockAdapter(axios);
  const url = '/api/get-password';

  mock.onGet(url).reply(200, {
    password: generatePassword(16),
  });

  return axios
    .get(url)
    .then(response => response.data)
    .catch(error => console.error('Error:', error));
}

export function isEqual(value, other) {
  var type = Object.prototype.toString.call(value);

  if (type !== Object.prototype.toString.call(other)) return false;

  if (['[object Array]', '[object Object]'].indexOf(type) < 0) return false;

  var valueLen =
    type === '[object Array]' ? value.length : Object.keys(value).length;
  var otherLen =
    type === '[object Array]' ? other.length : Object.keys(other).length;
  if (valueLen !== otherLen) return false;

  var compare = function(item1, item2) {
    var itemType = Object.prototype.toString.call(item1);

    if (['[object Array]', '[object Object]'].indexOf(itemType) >= 0) {
      if (!isEqual(item1, item2)) return false;
    } else {
      if (itemType !== Object.prototype.toString.call(item2)) return false;

      if (itemType === '[object Function]') {
        if (item1.toString() !== item2.toString()) return false;
      } else {
        if (item1 !== item2) return false;
      }
    }
  };

  if (type === '[object Array]') {
    for (var i = 0; i < valueLen; i++) {
      if (compare(value[i], other[i]) === false) return false;
    }
  } else {
    for (var key in value) {
      if (value.hasOwnProperty(key)) {
        if (compare(value[key], other[key]) === false) return false;
      }
    }
  }

  return true;
}

export function isEmpty(obj) {
  for (let key in obj) {
    return false;
  }
  return true;
}

export function setButtonAvailability(validate, submitElement) {
  submitElement.disabled = !validate();
}
