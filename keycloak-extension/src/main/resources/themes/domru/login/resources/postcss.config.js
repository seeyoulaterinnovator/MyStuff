const purgecss = require('@fullhuman/postcss-purgecss')({
  content: ['../**/*.html', '../**/*.ftl', 'src/**/*.svelte', 'src/**/*.js'],
  whitelistPatterns: [/^logo/],

  defaultExtractor: content => content.match(/[A-Za-z0-9-_:/]+/g) || [],
});

const production = !process.env.ROLLUP_WATCH;

// Перед сборкой в production нужно активировать purgecss, чтобы очистить tailwind
// от ненужных проекту css-правил
module.exports = () => ({
  plugins: [
    require('tailwindcss'),
    require('autoprefixer'),
    require('postcss-import'),
    require('postcss-preset-env')({ stage: 2 }),
    require('postcss-nested'),
    require('cssnano')({
      preset: 'default',
    }),
    production && purgecss,
  ],
});
