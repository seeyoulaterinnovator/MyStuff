const path = require('path');

const theme = path.basename(__dirname);
const base = '../..';
const template = path.normalize(`${base}/keycloak-extension/src/main/resources/themes/${theme}`);
const build = process.env.NODE_BUILD_PATH || path.resolve(`${template}/login/resources/build`);

module.exports = {
  theme,
  base,
  template,
  build,
};
